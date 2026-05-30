/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.people.replace;

import serp.project.pmcore.application.shared.cqrs.Unit;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.List;
import java.util.Set;

public record ReplaceProjectPersonRolesCommand(
        Long projectId,
        Long personUserId,
        List<Long> roleIds,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<Unit> {
}
