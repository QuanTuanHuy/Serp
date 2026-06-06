/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.schedule;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.List;
import java.util.Set;

public record UpdateWorkItemPlanCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long workItemId,
        Long plannedStart,
        Long plannedEnd,
        Boolean locked,
        List<UpdateWorkItemPlanAllocationCommand> allocations,
        Set<String> groupKeys
) implements ICommand<UpdateWorkItemPlanResult> {
    public UpdateWorkItemPlanCommand {
        allocations = allocations == null ? List.of() : List.copyOf(allocations);
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
