/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.removetransition;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record RemoveWorkflowTransitionCommand(
        Long workflowId,
        Long transitionId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteWorkflowTransitionResult> {
}
