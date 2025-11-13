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
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.mapper.EmailMapper;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
import serp.project.mailservice.core.port.client.IKafkaProducerPort;
import serp.project.mailservice.core.port.store.IEmailPort;
import serp.project.mailservice.core.service.IEmailProviderService;
import serp.project.mailservice.core.service.IEmailService;
import serp.project.mailservice.core.service.IEmailStatsService;
import serp.project.mailservice.core.service.IEmailTemplateService;
import serp.project.mailservice.core.service.IRateLimitService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendingUseCases {

    private final IEmailService emailService;
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
        
        emailService.validateEmail(email);
        emailService.enrichEmail(email);

        if (email.getTemplateId() != null) {
            Map<String, Object> variables = email.getTemplateVariables();
            String body = emailTemplateService.renderTemplate(email.getTemplateId(), variables);
            email.setBody(body);
        }

        email.setStatus(EmailStatus.PENDING);
        EmailEntity savedEmail = emailPort.save(email);

        IEmailProviderPort provider = emailProviderService.selectProvider(savedEmail);
        
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> providerResponse = savedEmail.getIsHtml() 
                    ? provider.sendHtmlEmail(savedEmail) 
                    : provider.sendEmail(savedEmail);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            savedEmail.setStatus(EmailStatus.SENT);
            savedEmail.setSentAt(LocalDateTime.now());
            savedEmail.setProviderMessageId((String) providerResponse.get("messageId"));
            savedEmail.setProviderResponse(providerResponse);
            
            emailStatsService.recordEmailSent(savedEmail, responseTime);
            
            log.info("Email sent successfully: {}, provider: {}, time: {}ms", 
                    savedEmail.getMessageId(), provider.getProviderName(), responseTime);
            
        } catch (Exception e) {
            log.error("Failed to send email: {}", savedEmail.getMessageId(), e);
            
            savedEmail.setStatus(EmailStatus.RETRY);
            savedEmail.setRetryCount(0);
            savedEmail.setNextRetryAt(LocalDateTime.now().plusMinutes(1));
            savedEmail.setErrorMessage(e.getMessage());
            savedEmail.setFailedAt(LocalDateTime.now());
            
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
                EmailEntity email = new EmailEntity();
                email.setTenantId(tenantId);
                email.setUserId(userId);
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

                emailService.validateEmail(email);
                emailService.enrichEmail(email);

                if (email.getTemplateId() != null && variables != null) {
                    String body = emailTemplateService.renderTemplate(email.getTemplateId(), variables);
                    email.setBody(body);
                }

                email.setStatus(EmailStatus.PENDING);
                EmailEntity savedEmail = emailPort.save(email);

                responses.add(EmailMapper.toSendEmailResponse(savedEmail));
                
            } catch (Exception e) {
                log.error("Failed to create bulk email for recipient: {}", recipient.getEmail(), e);
            }
        }

        log.info("Bulk email created: {} out of {} emails", responses.size(), request.getRecipients().size());
        return responses;
    }

    @Transactional
    public SendEmailResponse resendFailedEmail(String messageId) {
        log.info("Resending failed email with messageId: {}", messageId);

        EmailEntity email = emailPort.findByMessageId(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Email not found with messageId: " + messageId));

        if (email.getStatus() != EmailStatus.FAILED && email.getStatus() != EmailStatus.RETRY) {
            throw new IllegalStateException("Email status is not FAILED or RETRY, cannot resend");
        }

        if (email.getRetryCount() >= email.getMaxRetries()) {
            throw new IllegalStateException("Max retry attempts exceeded");
        }

        try {
            IEmailProviderPort provider = emailProviderService.getHealthyProvider();

            long startTime = System.currentTimeMillis();
            provider.sendEmail(email);
            long responseTime = System.currentTimeMillis() - startTime;

            email.setStatus(EmailStatus.SENT);
            email.setSentAt(LocalDateTime.now());
            email.setRetryCount(email.getRetryCount() + 1);

            EmailEntity updatedEmail = emailPort.save(email);

            emailStatsService.recordEmailSent(updatedEmail, responseTime);

            kafkaPublisher.sendMessageAsync(updatedEmail.getMessageId(), updatedEmail, KafkaTopic.EMAIL_STATUS_TOPIC);

            log.info("Email resent successfully: {}", messageId);
            return EmailMapper.toSendEmailResponse(updatedEmail);

        } catch (Exception e) {
            log.error("Failed to resend email: {}", messageId, e);

            email.setRetryCount(email.getRetryCount() + 1);
            email.setStatus(email.getRetryCount() >= email.getMaxRetries() ? EmailStatus.FAILED : EmailStatus.RETRY);
            email.setErrorMessage(e.getMessage());

            long nextRetryDelayMinutes = (long) Math.pow(2, email.getRetryCount());
            email.setNextRetryAt(LocalDateTime.now().plusMinutes(nextRetryDelayMinutes));

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
