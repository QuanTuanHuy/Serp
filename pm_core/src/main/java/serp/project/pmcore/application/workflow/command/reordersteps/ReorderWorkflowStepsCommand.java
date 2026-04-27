/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.reordersteps;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflow.WorkflowStepView;

import java.util.List;

public record ReorderWorkflowStepsCommand(
        Long workflowId,
        List<Long> stepIds,
        Long tenantId,
        Long userId
) implements ICommand<List<WorkflowStepView>> {
}
