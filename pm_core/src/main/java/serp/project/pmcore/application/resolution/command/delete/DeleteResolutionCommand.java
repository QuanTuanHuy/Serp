/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteResolutionCommand(
        Long id,
        Long tenantId,
        Long userId
) implements ICommand<DeleteResolutionResult> {
}
