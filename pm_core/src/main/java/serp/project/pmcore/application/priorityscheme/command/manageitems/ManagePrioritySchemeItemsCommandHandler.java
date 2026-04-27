/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.manageitems;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.priorityscheme.PrioritySchemeDetailView;
import serp.project.pmcore.application.priorityscheme.PrioritySchemePriorityView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.priority.service.IPriorityService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManagePrioritySchemeItemsCommandHandler implements ICommandHandler<ManagePrioritySchemeItemsCommand, PrioritySchemeDetailView> {

    private final IPrioritySchemeService prioritySchemeService;
    private final IPriorityService priorityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrioritySchemeDetailView handle(ManagePrioritySchemeItemsCommand command) {
        PrioritySchemeEntity updated = prioritySchemeService.replacePrioritySchemeItems(
                command.schemeId(),
                command.priorityIds(),
                command.tenantId(),
                command.userId()
        );
        return PrioritySchemeDetailView.from(updated, buildPriorityMap(updated, command.tenantId()));
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
