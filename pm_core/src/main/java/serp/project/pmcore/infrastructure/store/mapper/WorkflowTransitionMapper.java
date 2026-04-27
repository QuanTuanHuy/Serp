/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowTransitionModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkflowTransitionMapper extends BaseMapper {

    public WorkflowTransitionModel toModel(WorkflowTransitionEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowTransitionModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workflowVersionId(entity.getWorkflowVersionId())
                .name(entity.getName())
                .fromStepId(entity.getFromStepId())
                .toStepId(entity.getToStepId())
                .screenId(entity.getScreenId())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt()))
                .build();
    }

    public WorkflowTransitionEntity toEntity(WorkflowTransitionModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowTransitionEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workflowVersionId(model.getWorkflowVersionId())
                .name(model.getName())
                .fromStepId(model.getFromStepId())
                .toStepId(model.getToStepId())
                .screenId(model.getScreenId())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt()))
                .build();
    }

    public List<WorkflowTransitionEntity> toEntities(List<WorkflowTransitionModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkflowTransitionModel> toModels(List<WorkflowTransitionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
