/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowVersionEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowVersionModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkflowVersionMapper extends BaseMapper {

    public WorkflowVersionModel toModel(WorkflowVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowVersionModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workflowId(entity.getWorkflowId())
                .versionNo(entity.getVersionNo())
                .versionState(entity.getVersionState())
                .baseVersionId(entity.getBaseVersionId())
                .publishedAt(longToLocalDateTime(entity.getPublishedAt()))
                .publishedBy(entity.getPublishedBy())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkflowVersionEntity toEntity(WorkflowVersionModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowVersionEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workflowId(model.getWorkflowId())
                .versionNo(model.getVersionNo())
                .versionState(model.getVersionState())
                .baseVersionId(model.getBaseVersionId())
                .publishedAt(localDateTimeToLong(model.getPublishedAt()))
                .publishedBy(model.getPublishedBy())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<WorkflowVersionEntity> toEntities(List<WorkflowVersionModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkflowVersionModel> toModels(List<WorkflowVersionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
