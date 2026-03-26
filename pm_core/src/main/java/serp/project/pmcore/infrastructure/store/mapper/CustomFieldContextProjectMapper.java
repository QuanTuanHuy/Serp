/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextProjectEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextProjectModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomFieldContextProjectMapper extends BaseMapper {

    public CustomFieldContextProjectEntity toEntity(CustomFieldContextProjectModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldContextProjectEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .contextId(model.getContextId())
                .projectId(model.getProjectId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public CustomFieldContextProjectModel toModel(CustomFieldContextProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustomFieldContextProjectModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .contextId(entity.getContextId())
                .projectId(entity.getProjectId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextProjectEntity> toEntities(List<CustomFieldContextProjectModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<CustomFieldContextProjectModel> toModels(List<CustomFieldContextProjectEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
