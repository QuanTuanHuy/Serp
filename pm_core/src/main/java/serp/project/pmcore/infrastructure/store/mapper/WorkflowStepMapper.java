/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowStepEntity;
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
                .workflowId(entity.getWorkflowId())
                .statusId(entity.getStatusId())
                .sequence(entity.getSequence())
                .isInitial(entity.getIsInitial())
                .isFinal(entity.getIsFinal())
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
                .workflowId(model.getWorkflowId())
                .statusId(model.getStatusId())
                .sequence(model.getSequence())
                .isInitial(model.getIsInitial())
                .isFinal(model.getIsFinal())
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
}
