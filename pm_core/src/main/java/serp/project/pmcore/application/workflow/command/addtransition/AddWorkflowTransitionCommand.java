/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.addtransition;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;

public record AddWorkflowTransitionCommand(
        Long workflowId,
        String name,
        Long fromStepId,
        Long toStepId,
        Long screenId,
        Integer sequence,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowTransitionView> {
}
