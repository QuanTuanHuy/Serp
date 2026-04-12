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
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.service.IPriorityService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

@Service
@RequiredArgsConstructor
public class ListPrioritiesQueryHandler implements IQueryHandler<ListPrioritiesQuery, PageView<PriorityView>> {

    private final IPriorityService priorityService;

    @Override
    @Transactional(readOnly = true)
    public PageView<PriorityView> handle(ListPrioritiesQuery query) {
        PriorityListCriteria criteria = query.toCriteria();
        PageResult<PriorityView> result = priorityService.listVisiblePriorities(query.tenantId(), criteria)
                .map(priority -> PriorityView.from(priority, priority.isSystem()));
        int pageSize = criteria.getPageSize();
        int currentPage = criteria.getPage();
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) result.total() / pageSize);

        return new PageView<>(
                result.items(),
                result.total(),
                totalPages,
                currentPage,
                pageSize
        );
    }
}
