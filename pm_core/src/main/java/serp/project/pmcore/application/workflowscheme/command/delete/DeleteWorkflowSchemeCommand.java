/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteWorkflowSchemeCommand(
        Long schemeId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteWorkflowSchemeResult> {
}
