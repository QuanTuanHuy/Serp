/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.remove;

import serp.project.pmcore.application.shared.cqrs.Unit;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record RemoveProjectRoleActorCommand(
        Long projectId,
        Long roleId,
        String subjectType,
        String subjectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<Unit> {
    public RemoveProjectRoleActorCommand {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
