/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.search;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.workitem.query.WorkItemSearchCriteria;

import java.util.Set;

public record SearchWorkItemsQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        WorkItemSearchCriteria criteria
) implements IQuery<PageView<WorkItemSearchView>> {
}
