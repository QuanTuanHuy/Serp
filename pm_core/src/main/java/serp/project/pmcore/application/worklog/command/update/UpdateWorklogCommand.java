/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record UpdateWorklogCommand(
        Long projectId,
        Long workItemId,
        Long worklogId,
        Long timeSpent,
        Long startDate,
        String comment,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<UpdateWorklogResult> {

    public UpdateWorklogCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
