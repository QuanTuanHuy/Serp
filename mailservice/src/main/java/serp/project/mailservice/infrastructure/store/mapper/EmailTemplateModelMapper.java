/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.mapper;

import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.infrastructure.store.model.EmailTemplateModel;

import java.util.List;
import java.util.stream.Collectors;

public class EmailTemplateModelMapper {
    
    public static EmailTemplateEntity toEntity(EmailTemplateModel model) {
        if (model == null) {
            return null;
        }
        
        return EmailTemplateEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .code(model.getCode())
                .description(model.getDescription())
                .subject(model.getSubject())
                .bodyTemplate(model.getBodyTemplate())
                .isHtml(model.getIsHtml())
                .variablesSchema(model.getVariablesSchema())
                .defaultValues(model.getDefaultValues())
                .type(model.getType())
                .language(model.getLanguage())
                .category(model.getCategory())
                .isGlobal(model.getIsGlobal())
                .version(model.getVersion())
                .isActive(model.getIsActive())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .activeStatus(model.getActiveStatus())
                .build();
    }
    
    public static EmailTemplateModel toModel(EmailTemplateEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return EmailTemplateModel.builder()
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
                .activeStatus(entity.getActiveStatus())
                .build();
    }
    
    public static List<EmailTemplateEntity> toEntities(List<EmailTemplateModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(EmailTemplateModelMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    public static List<EmailTemplateModel> toModels(List<EmailTemplateEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(EmailTemplateModelMapper::toModel)
                .collect(Collectors.toList());
    }
    
    private EmailTemplateModelMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
