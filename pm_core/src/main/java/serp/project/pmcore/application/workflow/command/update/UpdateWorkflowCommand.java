/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflow.WorkflowView;

public record UpdateWorkflowCommand(
        Long workflowId,
        String name,
        String description,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowView> {
}
