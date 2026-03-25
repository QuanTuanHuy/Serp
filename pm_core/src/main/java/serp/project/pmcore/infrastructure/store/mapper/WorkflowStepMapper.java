/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowStepModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkflowStepMapper extends BaseMapper {

    public WorkflowStepModel toModel(WorkflowStepEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowStepModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workflowVersionId(entity.getWorkflowVersionId())
                .stepKey(entity.getStepKey())
                .name(entity.getName())
                .statusId(entity.getStatusId())
                .stepOrder(entity.getStepOrder())
                .isInitial(entity.getIsInitial())
                .isTerminal(entity.getIsTerminal())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkflowStepEntity toEntity(WorkflowStepModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowStepEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workflowVersionId(model.getWorkflowVersionId())
                .stepKey(model.getStepKey())
                .name(model.getName())
                .statusId(model.getStatusId())
                .stepOrder(model.getStepOrder())
                .isInitial(model.getIsInitial())
                .isTerminal(model.getIsTerminal())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<WorkflowStepEntity> toEntities(List<WorkflowStepModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkflowStepModel> toModels(List<WorkflowStepEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
