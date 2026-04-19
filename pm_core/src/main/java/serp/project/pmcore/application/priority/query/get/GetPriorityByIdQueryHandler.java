/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.service.IPriorityService;

@Service
@RequiredArgsConstructor
public class GetPriorityByIdQueryHandler implements IQueryHandler<GetPriorityByIdQuery, PriorityView> {

    private final IPriorityService priorityService;

    @Override
    @Transactional(readOnly = true)
    public PriorityView handle(GetPriorityByIdQuery query) {
        PriorityEntity priority = priorityService.getVisiblePriorityById(query.priorityId(), query.tenantId());
        return PriorityView.from(priority, priority.isSystem());
    }
}
