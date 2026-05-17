/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

@Service
@RequiredArgsConstructor
public class ListResolutionsQueryHandler implements IQueryHandler<ListResolutionsQuery, PageView<ResolutionView>> {

    private final IResolutionService resolutionService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ResolutionView> handle(ListResolutionsQuery query) {
        ResolutionListCriteria criteria = query.toCriteria();
        return PageViews.from(
                resolutionService.listVisibleResolutions(query.tenantId(), criteria),
                criteria,
                ResolutionView::from
        );
    }
}
