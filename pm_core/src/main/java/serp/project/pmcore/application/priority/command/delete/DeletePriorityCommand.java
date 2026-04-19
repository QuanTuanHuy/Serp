/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeletePriorityCommand(
        Long priorityId,
        Long tenantId,
        Long userId
) implements ICommand<DeletePriorityResult> {
}
