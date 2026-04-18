/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.service.IPriorityService;

@Service
@RequiredArgsConstructor
public class ListPrioritiesQueryHandler implements IQueryHandler<ListPrioritiesQuery, PageView<PriorityView>> {

    private final IPriorityService priorityService;

    @Override
    @Transactional(readOnly = true)
    public PageView<PriorityView> handle(ListPrioritiesQuery query) {
        PriorityListCriteria criteria = query.toCriteria();
        return PageViews.from(
                priorityService.listVisiblePriorities(query.tenantId(), criteria),
                criteria,
                priority -> PriorityView.from(priority, priority.isSystem())
        );
    }
}
