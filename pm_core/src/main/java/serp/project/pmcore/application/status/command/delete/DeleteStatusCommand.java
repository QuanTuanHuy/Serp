/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteStatusCommand(
        Long statusId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteStatusResult> {
}
