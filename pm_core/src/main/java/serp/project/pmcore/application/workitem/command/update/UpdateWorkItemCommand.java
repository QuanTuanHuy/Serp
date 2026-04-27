/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record UpdateWorkItemCommand(
        Long projectId,
        Long workItemId,
        UpdateWorkItemData data,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<UpdateWorkItemResult> {

    public UpdateWorkItemCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
