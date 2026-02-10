/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.mapper;

import serp.project.mailservice.core.domain.dto.request.SendEmailRequest;
import serp.project.mailservice.core.domain.dto.response.EmailStatusResponse;
import serp.project.mailservice.core.domain.dto.response.SendEmailResponse;
import serp.project.mailservice.core.domain.entity.EmailEntity;

public class EmailMapper {

    public static EmailEntity toEntity(SendEmailRequest request, Long tenantId, Long userId) {
        return EmailEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .provider(request.getProvider())
                .priority(request.getPriority())
                .type(request.getType())
                .fromEmail(request.getFromEmail())
                .toEmails(request.getToEmails())
                .ccEmails(request.getCcEmails())
                .bccEmails(request.getBccEmails())
                .subject(request.getSubject())
                .body(request.getBody())
                .isHtml(request.getIsHtml() != null ? request.getIsHtml() : true)
                .templateId(request.getTemplateId())
                .templateVariables(request.getTemplateVariables())
                .metadata(request.getMetadata())
                .build();
    }

    public static SendEmailResponse toSendEmailResponse(EmailEntity entity) {
        return SendEmailResponse.builder()
                .messageId(entity.getMessageId())
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .sentAt(entity.getSentAt())
                .providerMessageId(entity.getProviderMessageId())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    public static EmailStatusResponse toEmailStatusResponse(EmailEntity entity) {
        return EmailStatusResponse.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .status(entity.getStatus())
                .provider(entity.getProvider())
                .type(entity.getType())
                .priority(entity.getPriority())
                .toEmails(entity.getToEmails())
                .subject(entity.getSubject())
                .createdAt(entity.getCreatedAt())
                .sentAt(entity.getSentAt())
                .failedAt(entity.getFailedAt())
                .nextRetryAt(entity.getNextRetryAt())
                .retryCount(entity.getRetryCount())
                .maxRetries(entity.getMaxRetries())
                .errorMessage(entity.getErrorMessage())
                .providerResponse(entity.getProviderResponse())
                .build();
    }

    private EmailMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
