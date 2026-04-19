/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.removestep;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record RemoveWorkflowStepCommand(
        Long workflowId,
        Long stepId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteWorkflowStepResult> {
}
