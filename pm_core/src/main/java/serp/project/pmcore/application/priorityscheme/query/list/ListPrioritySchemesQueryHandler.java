/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;

@Service
@RequiredArgsConstructor
public class ListPrioritySchemesQueryHandler implements IQueryHandler<ListPrioritySchemesQuery, PageView<PrioritySchemeView>> {

    private final IPrioritySchemeService prioritySchemeService;

    @Override
    @Transactional(readOnly = true)
    public PageView<PrioritySchemeView> handle(ListPrioritySchemesQuery query) {
        PrioritySchemeListCriteria criteria = query.toCriteria();
        return PageViews.from(
                prioritySchemeService.listVisiblePrioritySchemes(query.tenantId(), criteria),
                criteria,
                scheme -> PrioritySchemeView.from(scheme, scheme.isSystem())
        );
    }
}
