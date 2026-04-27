/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priorityscheme.PrioritySchemeDetailView;
import serp.project.pmcore.application.priorityscheme.PrioritySchemePriorityView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.priority.service.IPriorityService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetPrioritySchemeByIdQueryHandler implements IQueryHandler<GetPrioritySchemeByIdQuery, PrioritySchemeDetailView> {

    private final IPrioritySchemeService prioritySchemeService;
    private final IPriorityService priorityService;

    @Override
    @Transactional(readOnly = true)
    public PrioritySchemeDetailView handle(GetPrioritySchemeByIdQuery query) {
        PrioritySchemeEntity scheme = prioritySchemeService.getVisiblePrioritySchemeDetailById(
                query.schemeId(),
                query.tenantId()
        );
        return PrioritySchemeDetailView.from(scheme, buildPriorityMap(scheme, query.tenantId()));
    }

    private Map<Long, PrioritySchemePriorityView> buildPriorityMap(PrioritySchemeEntity scheme, Long tenantId) {
        Map<Long, PrioritySchemePriorityView> prioritiesById = new LinkedHashMap<>();
        if (scheme.getItems() == null) {
            return prioritiesById;
        }

        List<Long> priorityIds = scheme.getItems().stream()
                .map(PrioritySchemeItemEntity::getPriorityId)
                .distinct()
                .toList();
        priorityService.getVisiblePrioritiesByIds(priorityIds, tenantId)
                .forEach(priority -> prioritiesById.put(priority.getId(), PrioritySchemePriorityView.from(priority)));
        return prioritiesById;
    }
}
