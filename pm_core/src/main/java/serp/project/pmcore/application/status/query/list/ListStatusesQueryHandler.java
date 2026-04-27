/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;
import serp.project.pmcore.domain.workitem.service.IStatusService;

@Service
@RequiredArgsConstructor
public class ListStatusesQueryHandler implements IQueryHandler<ListStatusesQuery, PageView<StatusView>> {

    private final IStatusService statusService;

    @Override
    @Transactional(readOnly = true)
    public PageView<StatusView> handle(ListStatusesQuery query) {
        StatusListCriteria criteria = query.toCriteria();
        return PageViews.from(
                statusService.listVisibleStatuses(query.tenantId(), criteria),
                criteria,
                status -> StatusView.from(status, Boolean.TRUE.equals(status.getIsSystem()))
        );
    }
}
