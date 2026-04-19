/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record DeleteWorklogCommand(
        Long projectId,
        Long workItemId,
        Long worklogId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<DeleteWorklogResult> {

    public DeleteWorklogCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
