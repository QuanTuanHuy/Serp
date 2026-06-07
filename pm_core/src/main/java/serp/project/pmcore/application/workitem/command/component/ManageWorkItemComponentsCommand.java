/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.component;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workitem.WorkItemComponentView;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ManageWorkItemComponentsCommand(
        Long projectId,
        Long workItemId,
        List<Long> componentIds,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<List<WorkItemComponentView>> {

    public ManageWorkItemComponentsCommand {
        componentIds = componentIds == null
                ? List.of()
                : List.copyOf(new LinkedHashSet<>(componentIds));
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
