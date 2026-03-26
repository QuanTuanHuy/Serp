/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomFieldContextMapper extends BaseMapper {

    public CustomFieldContextEntity toEntity(CustomFieldContextModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldContextEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .customFieldId(model.getCustomFieldId())
                .name(model.getName())
                .description(model.getDescription())
                .isGlobalContext(model.getIsGlobalContext())
                .appliesToAllProjects(model.getAppliesToAllProjects())
                .appliesToAllIssueTypes(model.getAppliesToAllIssueTypes())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextEntity> toEntities(List<CustomFieldContextModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public CustomFieldContextModel toModel(CustomFieldContextEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustomFieldContextModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .customFieldId(entity.getCustomFieldId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isGlobalContext(entity.getIsGlobalContext())
                .appliesToAllProjects(entity.getAppliesToAllProjects())
                .appliesToAllIssueTypes(entity.getAppliesToAllIssueTypes())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextModel> toModels(List<CustomFieldContextEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
