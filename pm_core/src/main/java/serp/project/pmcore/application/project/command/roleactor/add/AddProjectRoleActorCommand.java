/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.add;

import serp.project.pmcore.application.project.roleactor.model.ProjectRoleActorView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record AddProjectRoleActorCommand(
        Long projectId,
        Long roleId,
        String subjectType,
        String subjectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<ProjectRoleActorView> {
    public AddProjectRoleActorCommand {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
