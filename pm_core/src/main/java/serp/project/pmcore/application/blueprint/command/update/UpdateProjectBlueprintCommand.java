/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.update;

import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.blueprint.dto.ProjectBlueprintUpdateData;

public record UpdateProjectBlueprintCommand(
        Long blueprintId,
        ProjectBlueprintUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<ProjectBlueprintView> {
}
