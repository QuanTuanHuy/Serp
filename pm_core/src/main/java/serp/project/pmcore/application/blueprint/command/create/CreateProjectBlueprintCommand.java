/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.create;

import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateProjectBlueprintCommand(
        String name,
        String description,
        String projectTypeKey,
        String avatarUrl,
        Long tenantId,
        Long userId
) implements ICommand<ProjectBlueprintView> {
}
