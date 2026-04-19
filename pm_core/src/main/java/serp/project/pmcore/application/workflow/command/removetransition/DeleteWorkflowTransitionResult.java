/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.removetransition;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;

public record DeleteWorkflowTransitionResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteWorkflowTransitionResult from(WorkflowTransitionEntity entity) {
        return new DeleteWorkflowTransitionResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
