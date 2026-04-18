/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeletePrioritySchemeCommand(
        Long schemeId,
        Long tenantId,
        Long userId
) implements ICommand<DeletePrioritySchemeResult> {
}
