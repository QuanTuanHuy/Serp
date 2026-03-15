/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.mailservice.core.domain.dto.request.BulkEmailRequest;
import serp.project.mailservice.core.domain.dto.request.SendEmailRequest;
import serp.project.mailservice.core.domain.dto.provider.ProviderSendResult;
import serp.project.mailservice.core.domain.dto.response.EmailStatusResponse;
import serp.project.mailservice.core.domain.dto.response.SendEmailResponse;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.core.domain.mapper.EmailMapper;
import serp.project.mailservice.core.exception.AppException;
import serp.project.mailservice.core.exception.ErrorCode;
import serp.project.mailservice.core.port.client.IEmailProviderPort;
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

    @Transactional
    public SendEmailResponse sendEmail(SendEmailRequest request, Long tenantId, Long userId) {
        log.info("Sending email for tenant: {}, user: {}, to: {}", tenantId, userId, request.getToEmails());

        if (!rateLimitService.allowRequest(tenantId)) {
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED,
                    "Rate limit exceeded for tenant: " + tenantId);
        }

        EmailEntity email = EmailMapper.toEntity(request, tenantId, userId);
        email.enrichDefaults();
        if (email.getType() == null) {
            email.setType(EmailType.TRANSACTIONAL);
        }
        applyTemplateIfNeeded(email);
        email.validate();

        IEmailProviderPort provider = emailProviderService.selectProvider(email.getProvider());
        email.setProvider(provider.getProviderType());

        EmailEntity savedEmail = emailPort.save(email);

        long startTime = System.currentTimeMillis();
        try {
            ProviderSendResult providerResponse = Boolean.TRUE.equals(savedEmail.getIsHtml())
                    ? provider.sendHtmlEmail(savedEmail)
                    : provider.sendEmail(savedEmail);
            ensureProviderSendSucceeded(providerResponse, provider, savedEmail.getMessageId());

            long responseTime = System.currentTimeMillis() - startTime;

            savedEmail.markAsSent(
                    providerResponse.resolveProviderMessageId(savedEmail.getMessageId()),
                    providerResponse.toPersistenceResponse());

            emailStatsService.recordEmailSent(savedEmail, responseTime);

            log.info("Email sent successfully: {}, provider: {}, time: {}ms",
                    savedEmail.getMessageId(), provider.getProviderName(), responseTime);

        } catch (Exception e) {
            log.error("Failed to send email: {}", savedEmail.getMessageId(), e);
            handleSendFailure(savedEmail, provider.getProviderType(), e);
        }

        EmailEntity updatedEmail = emailPort.save(savedEmail);

        return EmailMapper.toSendEmailResponse(updatedEmail);
    }

    @Transactional
    public List<SendEmailResponse> sendBulkEmail(BulkEmailRequest request, Long tenantId, Long userId,
            String userEmail) {
        log.info("Sending bulk email for tenant: {}, recipients: {}", tenantId, request.getRecipients().size());

        if (!rateLimitService.allowRequest(tenantId)) {
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED,
                    "Rate limit exceeded for tenant: " + tenantId);
        }

        List<SendEmailResponse> responses = new ArrayList<>();
        EmailTemplateEntity template = getTemplateIfAny(request.getTemplateId());

        for (var recipient : request.getRecipients()) {
            EmailEntity savedEmail = null;
            EmailProvider selectedProvider = null;
            try {
                EmailEntity email = EmailEntity.createNew(tenantId, userId);
                email.setFromEmail(userEmail);
                email.setToEmails(List.of(recipient.getEmail()));
                email.setSubject(request.getSubject());
                email.setBody(request.getBody());
                email.setIsHtml(request.getIsHtml());
                email.setTemplateId(request.getTemplateId());
                email.setPriority(request.getPriority());
                email.setType(request.getType() != null ? request.getType() : EmailType.TRANSACTIONAL);
                email.setMetadata(request.getMetadata());

                Map<String, Object> variables = recipient.getVariables();
                email.setTemplateVariables(variables);

                if (template != null) {
                    if (email.getSubject() == null || email.getSubject().isBlank()) {
                        email.setSubject(template.getSubject());
                    }
                    String body = emailTemplateService.renderTemplate(
                            template.getBodyTemplate(),
                            template.getDefaultValues(),
                            variables);
                    email.setBody(body);
                }

                email.validate();

                IEmailProviderPort provider = emailProviderService.selectProvider(email.getProvider());
                selectedProvider = provider.getProviderType();
                email.setProvider(selectedProvider);
                savedEmail = emailPort.save(email);

                long startTime = System.currentTimeMillis();
                ProviderSendResult providerResponse = Boolean.TRUE.equals(savedEmail.getIsHtml())
                        ? provider.sendHtmlEmail(savedEmail)
                        : provider.sendEmail(savedEmail);
                ensureProviderSendSucceeded(providerResponse, provider, savedEmail.getMessageId());
                long responseTime = System.currentTimeMillis() - startTime;

                savedEmail.markAsSent(
                        providerResponse.resolveProviderMessageId(savedEmail.getMessageId()),
                        providerResponse.toPersistenceResponse());
                emailStatsService.recordEmailSent(savedEmail, responseTime);

            } catch (Exception e) {
                log.error("Failed to create bulk email for recipient: {}", recipient.getEmail(), e);
                if (savedEmail != null) {
                    handleSendFailure(savedEmail, selectedProvider, e);
                }
            } finally {
                if (savedEmail != null) {
                    EmailEntity updatedEmail = emailPort.save(savedEmail);
                    responses.add(EmailMapper.toSendEmailResponse(updatedEmail));
                }
            }
        }

        log.info("Bulk email processed: {} out of {} emails", responses.size(), request.getRecipients().size());
        return responses;
    }

    @Transactional
    public SendEmailResponse resendFailedEmail(String messageId, Long tenantId) {
        log.info("Resending failed email with messageId: {}, tenant: {}", messageId, tenantId);

        EmailEntity email = emailPort.findByMessageIdAndTenantId(messageId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND,
                        "Email not found with messageId: " + messageId + " for tenant: " + tenantId));

        if (!email.isRetryable()) {
            throw new AppException(ErrorCode.EMAIL_NOT_RETRYABLE,
                    "Email is not retryable (status: " + email.getStatus()
                            + ", retryCount: " + email.getRetryCount() + "/" + email.getMaxRetries() + ")");
        }

        try {
            IEmailProviderPort provider = emailProviderService.selectProvider(email.getProvider());
            email.setProvider(provider.getProviderType());

            long startTime = System.currentTimeMillis();
            ProviderSendResult providerResponse = Boolean.TRUE.equals(email.getIsHtml())
                    ? provider.sendHtmlEmail(email)
                    : provider.sendEmail(email);
            ensureProviderSendSucceeded(providerResponse, provider, email.getMessageId());
            long responseTime = System.currentTimeMillis() - startTime;

            email.markAsSent(
                    providerResponse.resolveProviderMessageId(email.getMessageId()),
                    providerResponse.toPersistenceResponse());

            EmailEntity updatedEmail = emailPort.save(email);

            emailStatsService.recordEmailSent(updatedEmail, responseTime);

            log.info("Email resent successfully: {}", messageId);
            return EmailMapper.toSendEmailResponse(updatedEmail);

        } catch (Exception e) {
            log.error("Failed to resend email: {}", messageId, e);

            handleSendFailure(email, email.getProvider(), e);

            emailPort.save(email);

            throw new AppException(ErrorCode.EMAIL_RESEND_FAILED,
                    "Failed to resend email: " + toErrorMessage(e));
        }
    }

    @Transactional(readOnly = true)
    public EmailStatusResponse getEmailStatus(String messageId, Long tenantId) {
        log.debug("Getting email status for messageId: {}, tenant: {}", messageId, tenantId);

        EmailEntity email = emailPort.findByMessageIdAndTenantId(messageId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND,
                        "Email not found: " + messageId + " for tenant: " + tenantId));

        return EmailMapper.toEmailStatusResponse(email);
    }

    private void applyTemplateIfNeeded(EmailEntity email) {
        if (email.getTemplateId() == null) {
            return;
        }

        EmailTemplateEntity template = emailTemplateService.getTemplateById(email.getTemplateId())
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found: " + email.getTemplateId()));

        if (email.getSubject() == null || email.getSubject().isBlank()) {
            email.setSubject(template.getSubject());
        }

        String body = emailTemplateService.renderTemplate(
                template.getBodyTemplate(),
                template.getDefaultValues(),
                email.getTemplateVariables());
        email.setBody(body);
    }

    private EmailTemplateEntity getTemplateIfAny(Long templateId) {
        if (templateId == null) {
            return null;
        }
        return emailTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found: " + templateId));
    }

    private void ensureProviderSendSucceeded(ProviderSendResult providerResponse,
            IEmailProviderPort provider,
            String messageId) {
        boolean success = providerResponse != null && providerResponse.success();
        if (success) {
            return;
        }

        String providerError = providerResponse != null
                ? providerResponse.resolveErrorMessage()
                : "Unknown provider error";

        throw new IllegalStateException(
                "Provider " + provider.getProviderName() + " failed to send messageId=" + messageId + ": "
                        + providerError);
    }

    private void handleSendFailure(EmailEntity email, EmailProvider provider, Exception exception) {
        email.scheduleRetry(toErrorMessage(exception));

        if (provider != null) {
            emailProviderService.markProviderDown(provider, Duration.ofMinutes(5));
        }

        emailStatsService.recordEmailFailed(email);
    }

    private String toErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error";
        }
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getMessage();
    }
}
