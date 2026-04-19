/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.removestep;

import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

public record DeleteWorkflowStepResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteWorkflowStepResult from(WorkflowStepEntity entity) {
        return new DeleteWorkflowStepResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
