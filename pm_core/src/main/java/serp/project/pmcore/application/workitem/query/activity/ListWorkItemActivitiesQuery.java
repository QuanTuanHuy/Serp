/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.activity;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record ListWorkItemActivitiesQuery(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        int page,
        int size,
        String type
) implements IQuery<PageView<WorkItemActivityView>> {

    public ListWorkItemActivitiesQuery {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
