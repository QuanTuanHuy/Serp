/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.people.remove;

import serp.project.pmcore.application.shared.cqrs.Unit;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record RemoveProjectPersonCommand(
        Long projectId,
        Long personUserId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<Unit> {
}
