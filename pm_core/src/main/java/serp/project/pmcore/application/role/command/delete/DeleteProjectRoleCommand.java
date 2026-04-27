/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DeleteProjectRoleCommand(
        Long roleId,
        Long tenantId,
        Long userId
) implements ICommand<DeleteProjectRoleResult> {
}
