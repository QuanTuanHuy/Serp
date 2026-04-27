/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.worklog.WorklogListPageView;
import serp.project.pmcore.domain.worklog.query.WorklogListCriteria;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record ListWorklogsQuery(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        Long authorId,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<WorklogListPageView> {

    public ListWorklogsQuery {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }

    public WorklogListCriteria toCriteria() {
        return WorklogListCriteria.builder()
                .workItemId(workItemId)
                .authorId(authorId)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
