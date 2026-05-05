/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.command.update;

import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.project.dto.ProjectComponentUpdateData;

import java.util.Set;

public record UpdateProjectComponentCommand(
        Long projectId,
        Long componentId,
        ProjectComponentUpdateData data,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<ProjectComponentView> {
    public UpdateProjectComponentCommand {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
