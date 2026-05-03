/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.delete;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;

public record DeleteWorkflowSchemeResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteWorkflowSchemeResult from(WorkflowSchemeEntity entity) {
        return new DeleteWorkflowSchemeResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
