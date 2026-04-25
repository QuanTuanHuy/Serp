/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.command.publish;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflow.WorkflowView;

public record PublishWorkflowCommand(
        Long workflowId,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowView> {
}
