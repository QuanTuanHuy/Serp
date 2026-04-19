/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

@Service
@RequiredArgsConstructor
public class GetStatusCategoryByIdQueryHandler implements IQueryHandler<GetStatusCategoryByIdQuery, StatusCategoryView> {

    private final IStatusCategoryService statusCategoryService;

    @Override
    @Transactional(readOnly = true)
    public StatusCategoryView handle(GetStatusCategoryByIdQuery query) {
        StatusCategoryEntity category = statusCategoryService.getVisibleStatusCategoryById(
                query.statusCategoryId(),
                query.tenantId()
        );
        return StatusCategoryView.from(category, Boolean.TRUE.equals(category.getIsSystem()));
    }
}
