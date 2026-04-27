/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.addstep;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflow.WorkflowStepView;

public record AddWorkflowStepCommand(
        Long workflowId,
        Long statusId,
        Boolean isInitial,
        Boolean isTerminal,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowStepView> {
}
