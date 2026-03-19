/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionEntity;
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
                .workflowId(entity.getWorkflowId())
                .name(entity.getName())
                .fromStatusId(entity.getFromStatusId())
                .toStatusId(entity.getToStatusId())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkflowTransitionEntity toEntity(WorkflowTransitionModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowTransitionEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workflowId(model.getWorkflowId())
                .name(model.getName())
                .fromStatusId(model.getFromStatusId())
                .toStatusId(model.getToStatusId())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
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
