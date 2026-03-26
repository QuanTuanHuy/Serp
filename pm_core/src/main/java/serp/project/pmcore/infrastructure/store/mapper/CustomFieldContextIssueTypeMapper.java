/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextIssueTypeEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextIssueTypeModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomFieldContextIssueTypeMapper extends BaseMapper {

    public CustomFieldContextIssueTypeEntity toEntity(CustomFieldContextIssueTypeModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldContextIssueTypeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .contextId(model.getContextId())
                .issueTypeId(model.getIssueTypeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public CustomFieldContextIssueTypeModel toModel(CustomFieldContextIssueTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustomFieldContextIssueTypeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .contextId(entity.getContextId())
                .issueTypeId(entity.getIssueTypeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextIssueTypeEntity> toEntities(List<CustomFieldContextIssueTypeModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<CustomFieldContextIssueTypeModel> toModels(List<CustomFieldContextIssueTypeEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
