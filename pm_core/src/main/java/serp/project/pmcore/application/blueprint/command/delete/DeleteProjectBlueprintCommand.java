/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteProjectBlueprintCommand(
        Long blueprintId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteProjectBlueprintResult> {
}
