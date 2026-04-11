/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;

import java.util.*;

public record TransitionWorkItemStatusCommand(
    Long projectId,
    Long workItemId,
    Long transitionId,
    Long resolutionId,
    Map<String, Object> fields,
    Long tenantId,
    Long userId,
    Set<String> groupKeys
) implements ICommand<TransitionWorkItemStatusResult> {

    public TransitionWorkItemStatusCommand {
        fields = fields == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }

    public TransitionWorkItemStatusData toData() {
        return new TransitionWorkItemStatusData(transitionId, resolutionId, fields);
    }

}
