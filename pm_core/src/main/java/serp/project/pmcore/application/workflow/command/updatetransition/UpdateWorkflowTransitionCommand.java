/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.updatetransition;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;

public record UpdateWorkflowTransitionCommand(
        Long workflowId,
        Long transitionId,
        String name,
        Long screenId,
        Integer sequence,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowTransitionView> {
}
