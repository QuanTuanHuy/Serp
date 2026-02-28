/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkflowMapper extends BaseMapper {

    public WorkflowModel toModel(WorkflowEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .versionNo(entity.getVersionNo())
                .isActive(entity.getIsActive())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkflowEntity toEntity(WorkflowModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .versionNo(model.getVersionNo())
                .isActive(model.getIsActive())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<WorkflowEntity> toEntities(List<WorkflowModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
