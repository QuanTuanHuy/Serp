/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.query.list;

import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;

public record ListPrioritiesQuery(
        Long tenantId,
        String search,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<PriorityView>> {
    public PriorityListCriteria toCriteria() {
        return PriorityListCriteria.builder()
                .search(search)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
