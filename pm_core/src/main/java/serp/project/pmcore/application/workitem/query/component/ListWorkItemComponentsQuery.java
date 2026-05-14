/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.component;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.workitem.WorkItemComponentView;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ListWorkItemComponentsQuery(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<List<WorkItemComponentView>> {

    public ListWorkItemComponentsQuery {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
