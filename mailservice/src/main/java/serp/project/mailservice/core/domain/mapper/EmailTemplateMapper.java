/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.mapper;

import serp.project.mailservice.core.domain.dto.request.EmailTemplateRequest;
import serp.project.mailservice.core.domain.dto.response.EmailTemplateResponse;
import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.domain.enums.ActiveStatus;

public class EmailTemplateMapper {

    public static EmailTemplateEntity toEntity(EmailTemplateRequest request, Long tenantId, Long userId) {
        return EmailTemplateEntity.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .subject(request.getSubject())
                .bodyTemplate(request.getBodyTemplate())
                .isHtml(request.getIsHtml() != null ? request.getIsHtml() : true)
                .variablesSchema(request.getVariablesSchema())
                .defaultValues(request.getDefaultValues())
                .type(request.getType())
                .language(request.getLanguage())
                .category(request.getCategory())
                .isGlobal(request.getIsGlobal() != null ? request.getIsGlobal() : false)
                .version(1)
                .isActive(true)
                .createdBy(userId)
                .updatedBy(userId)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    public static EmailTemplateResponse toResponse(EmailTemplateEntity entity) {
        return EmailTemplateResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .subject(entity.getSubject())
                .bodyTemplate(entity.getBodyTemplate())
                .isHtml(entity.getIsHtml())
                .variablesSchema(entity.getVariablesSchema())
                .defaultValues(entity.getDefaultValues())
                .type(entity.getType())
                .language(entity.getLanguage())
                .category(entity.getCategory())
                .isGlobal(entity.getIsGlobal())
                .version(entity.getVersion())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void updateEntity(EmailTemplateEntity entity, EmailTemplateRequest request, Long userId) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setSubject(request.getSubject());
        entity.setBodyTemplate(request.getBodyTemplate());
        entity.setIsHtml(request.getIsHtml() != null ? request.getIsHtml() : entity.getIsHtml());
        entity.setVariablesSchema(request.getVariablesSchema());
        entity.setDefaultValues(request.getDefaultValues());
        entity.setType(request.getType());
        entity.setLanguage(request.getLanguage());
        entity.setCategory(request.getCategory());
        entity.setVersion(entity.getVersion() + 1);
        entity.setUpdatedBy(userId);
    }

    private EmailTemplateMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
