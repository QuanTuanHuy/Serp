/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQuery;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQueryHandler;
import serp.project.pmcore.application.workitem.query.search.WorkItemSearchView;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PmGlobalSearchQueryHandler implements IQueryHandler<PmGlobalSearchQuery, PmGlobalSearchResponseView> {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;
    private static final int MIN_QUERY_LENGTH = 2;

    private final IProjectReadPort projectReadPort;
    private final IWorkItemReadPort workItemReadPort;
    private final SearchWorkItemsQueryHandler searchWorkItemsQueryHandler;

    @Override
    @Transactional(readOnly = true)
    public PmGlobalSearchResponseView handle(PmGlobalSearchQuery query) {
        String normalizedQuery = normalize(query.query());
        int limit = clampLimit(query.limit());
        if (normalizedQuery.length() < MIN_QUERY_LENGTH) {
            return new PmGlobalSearchResponseView(normalizedQuery, limit, List.of());
        }

        Set<String> groupKeys = query.groupKeys() == null ? Set.of() : query.groupKeys();
        List<PmGlobalSearchGroupView> groups = new ArrayList<>();
        addCurrentProjectWorkItems(groups, query, groupKeys, normalizedQuery, limit);
        addGlobalWorkItems(groups, query, groupKeys, normalizedQuery, limit);
        addProjects(groups, query, groupKeys, normalizedQuery, limit);
        return new PmGlobalSearchResponseView(normalizedQuery, limit, groups);
    }

    private void addCurrentProjectWorkItems(List<PmGlobalSearchGroupView> groups,
                                             PmGlobalSearchQuery query,
                                             Set<String> groupKeys,
                                             String normalizedQuery,
                                             int limit) {
        if (query.currentProjectId() == null) {
            return;
        }
        try {
            WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                    .projectId(query.currentProjectId())
                    .keyword(normalizedQuery)
                    .enriched(true)
                    .page(0)
                    .pageSize(limit)
                    .build();
            PageView<WorkItemSearchView> result = searchWorkItemsQueryHandler.handle(new SearchWorkItemsQuery(
                    query.tenantId(),
                    query.userId(),
                    groupKeys,
                    criteria
            ));
            if (result.items().isEmpty()) {
                return;
            }
            groups.add(new PmGlobalSearchGroupView(
                    PmGlobalSearchType.CURRENT_PROJECT_WORK_ITEM,
                    "This project",
                    result.totalItems(),
                    result.items().stream().map(this::fromCurrentProjectWorkItem).toList()
            ));
        } catch (RuntimeException ignored) {
            // An inaccessible current project should not reveal project existence.
        }
    }

    private void addGlobalWorkItems(List<PmGlobalSearchGroupView> groups,
                                    PmGlobalSearchQuery query,
                                    Set<String> groupKeys,
                                    String normalizedQuery,
                                    int limit) {
        List<WorkItemEntity> items = workItemReadPort.searchVisibleWorkItems(new VisibleWorkItemSearchCriteria(
                query.tenantId(),
                query.userId(),
                groupKeys,
                normalizedQuery,
                query.currentProjectId(),
                limit
        ));
        if (items.isEmpty()) {
            return;
        }
        groups.add(new PmGlobalSearchGroupView(
                PmGlobalSearchType.WORK_ITEM,
                "Work items",
                items.size(),
                items.stream().map(this::fromGlobalWorkItem).toList()
        ));
    }

    private void addProjects(List<PmGlobalSearchGroupView> groups,
                             PmGlobalSearchQuery query,
                             Set<String> groupKeys,
                             String normalizedQuery,
                             int limit) {
        PageResult<ProjectEntity> projects = projectReadPort.getProjects(
                query.tenantId(),
                query.userId(),
                groupKeys,
                normalizedQuery,
                null,
                null,
                false,
                0,
                limit,
                "name",
                "asc"
        );
        if (projects.items().isEmpty()) {
            return;
        }
        groups.add(new PmGlobalSearchGroupView(
                PmGlobalSearchType.PROJECT,
                "Projects",
                projects.total(),
                projects.items().stream().map(this::fromProject).toList()
        ));
    }

    private PmGlobalSearchItemView fromCurrentProjectWorkItem(WorkItemSearchView item) {
        return new PmGlobalSearchItemView(
                String.valueOf(item.id()),
                item.key() + " " + item.summary(),
                item.statusName(),
                "/pm/projects/" + item.projectId() + "/work-items/" + item.id(),
                Map.of("projectId", item.projectId(), "key", item.key())
        );
    }

    private PmGlobalSearchItemView fromGlobalWorkItem(WorkItemEntity item) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("projectId", item.getProjectId());
        meta.put("key", item.getKey());
        return new PmGlobalSearchItemView(
                String.valueOf(item.getId()),
                item.getKey() + " " + item.getSummary(),
                item.getStatusName(),
                "/pm/projects/" + item.getProjectId() + "/work-items/" + item.getId(),
                meta
        );
    }

    private PmGlobalSearchItemView fromProject(ProjectEntity project) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("projectId", project.getId());
        meta.put("key", project.getKey());
        return new PmGlobalSearchItemView(
                String.valueOf(project.getId()),
                project.getName(),
                project.getKey(),
                "/pm/projects/" + project.getId() + "/summary",
                meta
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static int clampLimit(Integer value) {
        if (value == null || value < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(value, MAX_LIMIT);
    }
}
