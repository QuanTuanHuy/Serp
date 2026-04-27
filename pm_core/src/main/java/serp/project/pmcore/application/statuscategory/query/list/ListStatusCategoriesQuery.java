/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;

public record ListStatusCategoriesQuery(
        Long tenantId,
        String search,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<StatusCategoryView>> {
    public StatusCategoryListCriteria toCriteria() {
        return StatusCategoryListCriteria.builder()
                .search(search)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
