/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.mailservice.core.domain.constant.KafkaTopic;
import serp.project.mailservice.core.domain.dto.request.BulkEmailRequest;
import serp.project.mailservice.core.domain.dto.request.SendEmailRequest;
import serp.project.mailservice.core.domain.dto.response.EmailStatusResponse;
import serp.project.mailservice.core.domain.dto.response.SendEmailResponse;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.domain.mapper.EmailMapper;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.core.port.client.IKafkaProducerPort;
import serp.project.mailservice.core.port.store.IEmailPort;
import serp.project.mailservice.core.service.IEmailProviderService;
import serp.project.mailservice.core.service.IEmailStatsService;
import serp.project.mailservice.core.service.IEmailTemplateService;
import serp.project.mailservice.core.service.IRateLimitService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendingUseCases {

    private final IEmailProviderService emailProviderService;
    private final IEmailTemplateService emailTemplateService;
    private final IRateLimitService rateLimitService;
    private final IEmailStatsService emailStatsService;
    private final IEmailPort emailPort;
    private final IKafkaProducerPort kafkaPublisher;

    @Transactional
    public SendEmailResponse sendEmail(SendEmailRequest request, Long tenantId, Long userId) {
        log.info("Sending email for tenant: {}, user: {}, to: {}", tenantId, userId, request.getToEmails());

        if (!rateLimitService.allowRequest(tenantId)) {
            throw new IllegalStateException("Rate limit exceeded for tenant: " + tenantId);
        }

        EmailEntity email = EmailMapper.toEntity(request, tenantId, userId);
        email.enrichDefaults();
        email.validate();

        if (email.getTemplateId() != null) {
            EmailTemplateEntity template = emailTemplateService.getTemplateById(email.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + email.getTemplateId()));
            String body = emailTemplateService.renderTemplate(
                    template.getBodyTemplate(), template.getDefaultValues(), email.getTemplateVariables());
            email.setBody(body);
        }

        EmailEntity savedEmail = emailPort.save(email);

        IEmailProviderPort provider = emailProviderService.selectProvider(savedEmail);

        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> providerResponse = savedEmail.getIsHtml()
                    ? provider.sendHtmlEmail(savedEmail)
                    : provider.sendEmail(savedEmail);

            long responseTime = System.currentTimeMillis() - startTime;

            savedEmail.markAsSent(
                    (String) providerResponse.get("messageId"),
                    providerResponse
            );

            emailStatsService.recordEmailSent(savedEmail, responseTime);

            log.info("Email sent successfully: {}, provider: {}, time: {}ms",
                    savedEmail.getMessageId(), provider.getProviderName(), responseTime);

        } catch (Exception e) {
            log.error("Failed to send email: {}", savedEmail.getMessageId(), e);

            savedEmail.scheduleRetry(e.getMessage());

            emailProviderService.markProviderDown(savedEmail.getProvider(), Duration.ofMinutes(5));
            emailStatsService.recordEmailFailed(savedEmail);
        }

        emailPort.save(savedEmail);

        kafkaPublisher.sendMessageAsync(
                savedEmail.getMessageId(),
                EmailMapper.toEmailStatusResponse(savedEmail),
                KafkaTopic.EMAIL_STATUS_TOPIC,
                null
        );

        return EmailMapper.toSendEmailResponse(savedEmail);
    }

    @Transactional
    public List<SendEmailResponse> sendBulkEmail(BulkEmailRequest request, Long tenantId, Long userId) {
        log.info("Sending bulk email for tenant: {}, recipients: {}", tenantId, request.getRecipients().size());

        if (!rateLimitService.allowRequest(tenantId)) {
            throw new IllegalStateException("Rate limit exceeded for tenant: " + tenantId);
        }

        List<SendEmailResponse> responses = new ArrayList<>();

        for (var recipient : request.getRecipients()) {
            try {
                EmailEntity email = EmailEntity.createNew(tenantId, userId);
                email.setToEmails(List.of(recipient.getEmail()));
                email.setSubject(request.getSubject());
                email.setBody(request.getBody());
                email.setIsHtml(request.getIsHtml());
                email.setTemplateId(request.getTemplateId());
                email.setPriority(request.getPriority());
                email.setType(request.getType());
                email.setMetadata(request.getMetadata());

                Map<String, Object> variables = recipient.getVariables();
                email.setTemplateVariables(variables);

                email.validate();

                if (email.getTemplateId() != null && variables != null) {
                    EmailTemplateEntity template = emailTemplateService.getTemplateById(email.getTemplateId())
                            .orElseThrow(() -> new IllegalArgumentException("Template not found: " + email.getTemplateId()));
                    String body = emailTemplateService.renderTemplate(
                            template.getBodyTemplate(), template.getDefaultValues(), variables);
                    email.setBody(body);
                }

                EmailEntity savedEmail = emailPort.save(email);

                IEmailProviderPort provider = emailProviderService.selectProvider(savedEmail);

                long startTime = System.currentTimeMillis();
                try {
                    Map<String, Object> providerResponse = savedEmail.getIsHtml()
                            ? provider.sendHtmlEmail(savedEmail)
                            : provider.sendEmail(savedEmail);
                    long responseTime = System.currentTimeMillis() - startTime;

                    savedEmail.markAsSent(
                            (String) providerResponse.get("messageId"),
                            providerResponse
                    );
                    emailPort.save(savedEmail);
                    emailStatsService.recordEmailSent(savedEmail, responseTime);

                } catch (Exception sendEx) {
                    log.error("Failed to send bulk email for recipient: {}", recipient.getEmail(), sendEx);
                    savedEmail.scheduleRetry(sendEx.getMessage());
                    emailPort.save(savedEmail);
                    emailStatsService.recordEmailFailed(savedEmail);
                }

                responses.add(EmailMapper.toSendEmailResponse(savedEmail));

            } catch (Exception e) {
                log.error("Failed to create bulk email for recipient: {}", recipient.getEmail(), e);
            }
        }

        log.info("Bulk email processed: {} out of {} emails", responses.size(), request.getRecipients().size());
        return responses;
    }

    @Transactional
    public SendEmailResponse resendFailedEmail(String messageId) {
        log.info("Resending failed email with messageId: {}", messageId);

        EmailEntity email = emailPort.findByMessageId(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Email not found with messageId: " + messageId));

        if (!email.isRetryable()) {
            throw new IllegalStateException("Email is not retryable (status: " + email.getStatus()
                    + ", retryCount: " + email.getRetryCount() + "/" + email.getMaxRetries() + ")");
        }

        try {
            IEmailProviderPort provider = emailProviderService.getHealthyProvider();

            long startTime = System.currentTimeMillis();
            Map<String, Object> providerResponse = email.getIsHtml() != null && email.getIsHtml()
                    ? provider.sendHtmlEmail(email)
                    : provider.sendEmail(email);
            long responseTime = System.currentTimeMillis() - startTime;

            email.markAsSent(
                    (String) providerResponse.get("messageId"),
                    providerResponse
            );

            EmailEntity updatedEmail = emailPort.save(email);

            emailStatsService.recordEmailSent(updatedEmail, responseTime);

            kafkaPublisher.sendMessageAsync(updatedEmail.getMessageId(), updatedEmail, KafkaTopic.EMAIL_STATUS_TOPIC);

            log.info("Email resent successfully: {}", messageId);
            return EmailMapper.toSendEmailResponse(updatedEmail);

        } catch (Exception e) {
            log.error("Failed to resend email: {}", messageId, e);

            email.scheduleRetry(e.getMessage());

            EmailEntity updatedEmail = emailPort.save(email);

            emailStatsService.recordEmailFailed(updatedEmail);

            throw new RuntimeException("Failed to resend email: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public EmailStatusResponse getEmailStatus(String messageId) {
        log.debug("Getting email status for messageId: {}", messageId);

        EmailEntity email = emailPort.findByMessageId(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Email not found: " + messageId));

        return EmailMapper.toEmailStatusResponse(email);
    }
}
