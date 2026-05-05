/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.command.create;

import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record CreateProjectComponentCommand(
        Long projectId,
        String name,
        String description,
        Long leadUserId,
        String assigneeType,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<ProjectComponentView> {
    public CreateProjectComponentCommand {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
