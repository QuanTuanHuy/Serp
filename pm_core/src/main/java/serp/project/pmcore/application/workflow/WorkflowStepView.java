/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

public record WorkflowStepView(
        Long id,
        Long tenantId,
        Long workflowVersionId,
        String stepKey,
        String name,
        Long statusId,
        Integer stepOrder,
        boolean isInitial,
        boolean isTerminal,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorkflowStepView from(WorkflowStepEntity entity) {
        return new WorkflowStepView(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkflowVersionId(),
                entity.getStepKey(),
                entity.getName(),
                entity.getStatusId(),
                entity.getStepOrder(),
                Boolean.TRUE.equals(entity.getIsInitial()),
                Boolean.TRUE.equals(entity.getIsTerminal()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
