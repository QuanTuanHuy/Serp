/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.mapper;

import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.infrastructure.store.model.EmailModel;

import java.util.List;
import java.util.stream.Collectors;

public class EmailModelMapper {
    
    public static EmailEntity toEntity(EmailModel model) {
        if (model == null) {
            return null;
        }
        
        return EmailEntity.builder()
                .id(model.getId())
                .messageId(model.getMessageId())
                .tenantId(model.getTenantId())
                .userId(model.getUserId())
                .provider(model.getProvider())
                .status(model.getStatus())
                .priority(model.getPriority())
                .type(model.getType())
                .fromEmail(model.getFromEmail())
                .toEmails(model.getToEmails())
                .ccEmails(model.getCcEmails())
                .bccEmails(model.getBccEmails())
                .subject(model.getSubject())
                .body(model.getBody())
                .isHtml(model.getIsHtml())
                .templateId(model.getTemplateId())
                .templateVariables(model.getTemplateVariables())
                .metadata(model.getMetadata())
                .providerMessageId(model.getProviderMessageId())
                .providerResponse(model.getProviderResponse())
                .sentAt(model.getSentAt())
                .failedAt(model.getFailedAt())
                .nextRetryAt(model.getNextRetryAt())
                .retryCount(model.getRetryCount())
                .maxRetries(model.getMaxRetries())
                .errorMessage(model.getErrorMessage())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .activeStatus(model.getActiveStatus())
                .build();
    }
    
    public static EmailModel toModel(EmailEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return EmailModel.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .provider(entity.getProvider())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .type(entity.getType())
                .fromEmail(entity.getFromEmail())
                .toEmails(entity.getToEmails())
                .ccEmails(entity.getCcEmails())
                .bccEmails(entity.getBccEmails())
                .subject(entity.getSubject())
                .body(entity.getBody())
                .isHtml(entity.getIsHtml())
                .templateId(entity.getTemplateId())
                .templateVariables(entity.getTemplateVariables())
                .metadata(entity.getMetadata())
                .providerMessageId(entity.getProviderMessageId())
                .providerResponse(entity.getProviderResponse())
                .sentAt(entity.getSentAt())
                .failedAt(entity.getFailedAt())
                .nextRetryAt(entity.getNextRetryAt())
                .retryCount(entity.getRetryCount())
                .maxRetries(entity.getMaxRetries())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .activeStatus(entity.getActiveStatus())
                .build();
    }
    
    public static List<EmailEntity> toEntities(List<EmailModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(EmailModelMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    public static List<EmailModel> toModels(List<EmailEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(EmailModelMapper::toModel)
                .collect(Collectors.toList());
    }
    
    private EmailModelMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
