/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.update;

import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.project.dto.ProjectRoleUpdateData;

public record UpdateProjectRoleCommand(
        Long roleId,
        ProjectRoleUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<ProjectRoleView> {
}
