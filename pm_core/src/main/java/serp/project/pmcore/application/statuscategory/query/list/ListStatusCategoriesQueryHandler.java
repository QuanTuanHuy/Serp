/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

@Service
@RequiredArgsConstructor
public class ListStatusCategoriesQueryHandler implements IQueryHandler<ListStatusCategoriesQuery, PageView<StatusCategoryView>> {

    private final IStatusCategoryService statusCategoryService;

    @Override
    @Transactional(readOnly = true)
    public PageView<StatusCategoryView> handle(ListStatusCategoriesQuery query) {
        StatusCategoryListCriteria criteria = query.toCriteria();
        return PageViews.from(
                statusCategoryService.listVisibleStatusCategories(query.tenantId(), criteria),
                criteria,
                category -> StatusCategoryView.from(category, Boolean.TRUE.equals(category.getIsSystem()))
        );
    }
}
