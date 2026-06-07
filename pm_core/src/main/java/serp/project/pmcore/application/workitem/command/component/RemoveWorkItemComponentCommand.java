/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.component;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record RemoveWorkItemComponentCommand(
        Long projectId,
        Long workItemId,
        Long componentId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<RemoveWorkItemComponentResult> {

    public RemoveWorkItemComponentCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
