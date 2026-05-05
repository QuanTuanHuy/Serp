/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record DeleteProjectComponentCommand(
        Long projectId,
        Long componentId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<DeleteProjectComponentResult> {
    public DeleteProjectComponentCommand {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
