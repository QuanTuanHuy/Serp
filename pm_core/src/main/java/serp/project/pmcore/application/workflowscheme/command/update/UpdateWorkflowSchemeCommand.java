/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeView;
import serp.project.pmcore.domain.workflow.dto.WorkflowSchemeUpdateData;

public record UpdateWorkflowSchemeCommand(
        Long schemeId,
        WorkflowSchemeUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<WorkflowSchemeView> {
}
