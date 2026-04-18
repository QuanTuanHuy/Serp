/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;

@Service
@RequiredArgsConstructor
public class ListProjectCategoriesQueryHandler implements IQueryHandler<ListProjectCategoriesQuery, PageView<ProjectCategoryView>> {

    private final IProjectCategoryService projectCategoryService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectCategoryView> handle(ListProjectCategoriesQuery query) {
        ProjectCategoryListCriteria criteria = query.toCriteria();
        return PageViews.from(
                projectCategoryService.listCategories(query.tenantId(), criteria),
                criteria,
                ProjectCategoryView::from
        );
    }
}
