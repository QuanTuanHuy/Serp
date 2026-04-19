/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;

public record WorkflowTransitionView(
        Long id,
        Long tenantId,
        Long workflowVersionId,
        String name,
        Long fromStepId,
        Long toStepId,
        Long screenId,
        Integer sequence,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorkflowTransitionView from(WorkflowTransitionEntity entity) {
        return new WorkflowTransitionView(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkflowVersionId(),
                entity.getName(),
                entity.getFromStepId(),
                entity.getToStepId(),
                entity.getScreenId(),
                entity.getSequence(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
