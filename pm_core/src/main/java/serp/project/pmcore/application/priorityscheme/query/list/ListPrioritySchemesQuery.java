/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.query.list;

import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;

public record ListPrioritySchemesQuery(
        Long tenantId,
        String search,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<PrioritySchemeView>> {
    public PrioritySchemeListCriteria toCriteria() {
        return PrioritySchemeListCriteria.builder()
                .search(search)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
