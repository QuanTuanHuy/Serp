/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.assign;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record AssignWorkItemCommand(
        Long projectId,
        Long workItemId,
        Long assigneeId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<AssignWorkItemResult> {

    public AssignWorkItemCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
