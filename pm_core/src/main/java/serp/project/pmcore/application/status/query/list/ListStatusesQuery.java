/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;

public record ListStatusesQuery(
        Long tenantId,
        String search,
        Long statusCategoryId,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<StatusView>> {
    public StatusListCriteria toCriteria() {
        return StatusListCriteria.builder()
                .search(search)
                .statusCategoryId(statusCategoryId)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
