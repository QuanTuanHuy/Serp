/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.create;

import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateProjectRoleCommand(
        String name,
        String description,
        Long tenantId,
        Long userId
) implements ICommand<ProjectRoleView> {
}
