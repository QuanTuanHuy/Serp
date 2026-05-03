/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.create;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeView;

public record CreateWorkflowSchemeCommand(
        String name,
        String description,
        Long defaultWorkflowId,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowSchemeView> {
}
