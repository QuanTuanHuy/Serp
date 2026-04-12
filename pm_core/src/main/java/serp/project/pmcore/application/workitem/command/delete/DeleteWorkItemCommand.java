/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record DeleteWorkItemCommand(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<DeleteWorkItemResult> {
    public DeleteWorkItemCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
