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
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.service.IPriorityService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListPrioritiesQueryHandler implements IQueryHandler<ListPrioritiesQuery, PageView<PriorityView>> {

    private final IPriorityService priorityService;
    private final IProjectReadPort projectReadPort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;

    @Override
    @Transactional(readOnly = true)
    public PageView<PriorityView> handle(ListPrioritiesQuery query) {
        PriorityListCriteria criteria = query.toCriteria();
        if (query.projectId() != null) {
            return PageViews.from(
                    listProjectPriorities(query, criteria),
                    criteria,
                    priority -> PriorityView.from(priority, priority.isSystem())
            );
        }
        return PageViews.from(
                priorityService.listVisiblePriorities(query.tenantId(), criteria),
                criteria,
                priority -> PriorityView.from(priority, priority.isSystem())
        );
    }

    private PageResult<PriorityEntity> listProjectPriorities(ListPrioritiesQuery query,
                                                            PriorityListCriteria criteria) {
        ProjectEntity project = projectReadPort.getProjectById(query.projectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(query.projectId()));
        if (project.getPrioritySchemeId() == null) {
            return new PageResult<>(List.of(), 0);
        }

        List<PrioritySchemeItemEntity> schemeItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeIdIncludingSystem(project.getPrioritySchemeId(), query.tenantId());
        List<Long> priorityIds = schemeItems.stream()
                .map(PrioritySchemeItemEntity::getPriorityId)
                .distinct()
                .toList();
        Map<Long, Integer> sequenceByPriorityId = schemeItems.stream()
                .collect(Collectors.toMap(
                        PrioritySchemeItemEntity::getPriorityId,
                        item -> item.getSequence() != null ? item.getSequence() : Integer.MAX_VALUE,
                        Math::min
                ));

        List<PriorityEntity> filtered = priorityService.getVisiblePrioritiesByIds(priorityIds, query.tenantId()).stream()
                .filter(priority -> matchesSearch(priority.getName(), priority.getPriorityKey(), criteria.getSearch()))
                .sorted(Comparator
                        .comparing((PriorityEntity priority) -> sequenceByPriorityId.getOrDefault(priority.getId(), Integer.MAX_VALUE))
                        .thenComparing(PriorityEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        return page(filtered, criteria);
    }

    private boolean matchesSearch(String name, String key, String search) {
        if (search == null) {
            return true;
        }
        String needle = search.toLowerCase(Locale.ROOT);
        return contains(name, needle) || contains(key, needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private <T> PageResult<T> page(List<T> items, PriorityListCriteria criteria) {
        int page = Math.max(criteria.getPage(), 0);
        int pageSize = Math.max(criteria.getPageSize(), 1);
        int fromIndex = Math.min(page * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageResult<>(items.subList(fromIndex, toIndex), items.size());
    }
}
