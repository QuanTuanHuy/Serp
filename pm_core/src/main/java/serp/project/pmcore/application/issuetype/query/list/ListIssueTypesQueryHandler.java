/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
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
public class ListIssueTypesQueryHandler implements IQueryHandler<ListIssueTypesQuery, PageView<IssueTypeView>> {

    private final IIssueTypeService issueTypeService;
    private final IProjectReadPort projectReadPort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;

    @Override
    @Transactional(readOnly = true)
    public PageView<IssueTypeView> handle(ListIssueTypesQuery query) {
        IssueTypeListCriteria criteria = query.toCriteria();
        if (query.projectId() != null) {
            return PageViews.from(
                    listProjectIssueTypes(query, criteria),
                    criteria,
                    issueType -> IssueTypeView.from(issueType, issueType.isSystem())
            );
        }
        return PageViews.from(
                issueTypeService.listVisibleIssueTypes(query.tenantId(), criteria),
                criteria,
                issueType -> IssueTypeView.from(issueType, issueType.isSystem())
        );
    }

    private PageResult<IssueTypeEntity> listProjectIssueTypes(ListIssueTypesQuery query,
                                                             IssueTypeListCriteria criteria) {
        ProjectEntity project = projectReadPort.getProjectById(query.projectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(query.projectId()));
        if (project.getIssueTypeSchemeId() == null) {
            return new PageResult<>(List.of(), 0);
        }

        List<IssueTypeSchemeItemEntity> schemeItems = issueTypeSchemeItemPort
                .getIssueTypeSchemeItemsBySchemeId(project.getIssueTypeSchemeId(), query.tenantId());
        List<Long> issueTypeIds = schemeItems.stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .distinct()
                .toList();
        Map<Long, Integer> sequenceByIssueTypeId = schemeItems.stream()
                .collect(Collectors.toMap(
                        IssueTypeSchemeItemEntity::getIssueTypeId,
                        item -> item.getSequence() != null ? item.getSequence() : Integer.MAX_VALUE,
                        Math::min
                ));

        List<IssueTypeEntity> filtered = issueTypeService.getVisibleIssueTypesByIds(issueTypeIds, query.tenantId()).stream()
                .filter(issueType -> criteria.getHierarchyLevel() == null
                        || criteria.getHierarchyLevel().equals(issueType.getHierarchyLevel()))
                .filter(issueType -> matchesSearch(issueType.getName(), issueType.getTypeKey(), criteria.getSearch()))
                .sorted(Comparator
                        .comparing((IssueTypeEntity issueType) -> sequenceByIssueTypeId.getOrDefault(issueType.getId(), Integer.MAX_VALUE))
                        .thenComparing(IssueTypeEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
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

    private <T> PageResult<T> page(List<T> items, IssueTypeListCriteria criteria) {
        int page = Math.max(criteria.getPage(), 0);
        int pageSize = Math.max(criteria.getPageSize(), 1);
        int fromIndex = Math.min(page * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageResult<>(items.subList(fromIndex, toIndex), items.size());
    }
}
