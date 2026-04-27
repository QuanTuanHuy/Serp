/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;

@Service
@RequiredArgsConstructor
public class GetProjectCategoryByIdQueryHandler implements IQueryHandler<GetProjectCategoryByIdQuery, ProjectCategoryView> {

    private final IProjectCategoryService projectCategoryService;

    @Override
    @Transactional(readOnly = true)
    public ProjectCategoryView handle(GetProjectCategoryByIdQuery query) {
        return ProjectCategoryView.from(projectCategoryService.getCategoryById(query.categoryId(), query.tenantId()));
    }
}
