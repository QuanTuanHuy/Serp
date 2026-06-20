# PM Global Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build PM global search for visible projects and work items, with current-project prioritization in the PM header and a full `/pm/search` results page.

**Architecture:** Add a `pm_core` read endpoint at `/api/v1/search` that owns tenant and permission filtering, returns grouped search results, and delegates cross-project work item lookup to a dedicated read-port method. The frontend consumes that endpoint through PM-owned RTK Query code, renders quick grouped results in `PMHeader`, and renders the same contract on a thin App Router search page.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Spring JDBC, Next.js 15 App Router, React 19, TypeScript, Redux Toolkit Query, Tailwind/Shadcn UI.

---

## File Structure

Backend files to create:

- `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQuery.java` - query input for PM global search.
- `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchType.java` - result group type enum.
- `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchItemView.java` - normalized result item.
- `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchGroupView.java` - grouped result collection.
- `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchResponseView.java` - endpoint response payload.
- `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQueryHandler.java` - application query handler and ranking orchestration.
- `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/VisibleWorkItemSearchCriteria.java` - cross-project visible work item search criteria.
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/search/PmGlobalSearchController.java` - REST controller.

Backend files to modify:

- `pm_core/src/main/java/serp/project/pmcore/domain/workitem/port/read/IWorkItemReadPort.java` - add visible cross-project work item search method.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java` - implement visible cross-project SQL query.
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java` - add `SEARCH` path constant.

Backend tests to create:

- `pm_core/src/test/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQueryHandlerTest.java`

Backend tests to modify:

- `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapterTest.java` - add coverage for the new adapter method when the existing test setup supports JDBC mocking.

Frontend files to create:

- `serp_web/src/modules/pm/types/global-search.types.ts` - PM global search API contract.
- `serp_web/src/modules/pm/api/globalSearchApi.ts` - PM global search RTK Query endpoint.
- `serp_web/src/modules/pm/components/search/PMGlobalSearchDropdown.tsx` - header dropdown renderer and keyboard result list.
- `serp_web/src/modules/pm/components/search/PMGlobalSearchResults.tsx` - shared grouped result renderer.
- `serp_web/src/modules/pm/components/search/index.ts` - search component exports.
- `serp_web/src/modules/pm/pages/PMGlobalSearchPage.tsx` - full PM search page.
- `serp_web/src/app/pm/search/page.tsx` - thin App Router entry point.

Frontend files to modify:

- `serp_web/src/modules/pm/api/index.ts` - export search API hook and types.
- `serp_web/src/modules/pm/types/api.ts` - export global search types.
- `serp_web/src/modules/pm/index.ts` - export `PMGlobalSearchPage`.
- `serp_web/src/modules/pm/components/layout/PMHeader.tsx` - replace no-op search with quick search.

---

### Task 1: Backend Global Search Contract And Handler Test

**Files:**

- Create: `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQuery.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchType.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchItemView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchGroupView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchResponseView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQueryHandler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQueryHandlerTest.java`

- [ ] **Step 1: Write the failing handler test**

Create `PmGlobalSearchQueryHandlerTest.java` with these test cases:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQuery;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQueryHandler;
import serp.project.pmcore.application.workitem.query.search.WorkItemSearchView;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmGlobalSearchQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;

    @Mock
    private IProjectReadPort projectReadPort;

    @Mock
    private IWorkItemReadPort workItemReadPort;

    @Mock
    private SearchWorkItemsQueryHandler searchWorkItemsQueryHandler;

    private PmGlobalSearchQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PmGlobalSearchQueryHandler(
                projectReadPort,
                workItemReadPort,
                searchWorkItemsQueryHandler
        );
    }

    @Test
    void handleShouldReturnEmptyGroupsForBlankOrShortQuery() {
        PmGlobalSearchResponseView response = handler.handle(new PmGlobalSearchQuery(
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                " s ",
                5,
                10L
        ));

        assertEquals("s", response.query());
        assertEquals(5, response.limit());
        assertTrue(response.groups().isEmpty());
        verify(projectReadPort, never()).getProjects(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(workItemReadPort, never()).searchVisibleWorkItems(any());
    }

    @Test
    void handleShouldOrderCurrentProjectWorkItemsThenGlobalWorkItemsThenProjects() {
        WorkItemSearchView currentItem = new WorkItemSearchView(
                100L, 10L, 1L, 7L, "SERP-7", "Search from current project", null,
                null, 1L, 1L, null, null, null, null, null, null, null, null,
                null, null, null, 1000L, null, 2000L, null, "Task", null, null,
                "High", null, null, null, "open", "Open", null, "backlog", "Backlog",
                null, null, null, null
        );
        when(searchWorkItemsQueryHandler.handle(any(SearchWorkItemsQuery.class)))
                .thenReturn(new PageView<>(List.of(currentItem), 1L, 1, 0, 3));
        when(workItemReadPort.searchVisibleWorkItems(any())).thenReturn(List.of(
                WorkItemEntity.builder()
                        .id(101L)
                        .projectId(11L)
                        .key("CORE-1")
                        .summary("Search from another project")
                        .statusName("In Progress")
                        .updatedAt(3000L)
                        .build()
        ));
        when(projectReadPort.getProjects(TENANT_ID, USER_ID, Set.of("devs"), "search", null, null, false, 0, 3, "name", "asc"))
                .thenReturn(new PageResult<>(List.of(
                        ProjectEntity.builder()
                                .id(20L)
                                .key("SERP")
                                .name("SERP Platform")
                                .isArchived(false)
                                .updatedAt(4000L)
                                .build()
                ), 1L));

        PmGlobalSearchResponseView response = handler.handle(new PmGlobalSearchQuery(
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                " search ",
                3,
                10L
        ));

        assertEquals("search", response.query());
        assertEquals(3, response.limit());
        assertEquals(3, response.groups().size());
        assertEquals(PmGlobalSearchType.CURRENT_PROJECT_WORK_ITEM, response.groups().get(0).type());
        assertEquals(PmGlobalSearchType.WORK_ITEM, response.groups().get(1).type());
        assertEquals(PmGlobalSearchType.PROJECT, response.groups().get(2).type());
        assertEquals("/pm/projects/10/work-items/100", response.groups().get(0).items().getFirst().url());
        assertEquals("/pm/projects/11/work-items/101", response.groups().get(1).items().getFirst().url());
        assertEquals("/pm/projects/20/summary", response.groups().get(2).items().getFirst().url());
    }

    @Test
    void handleShouldClampLimitAndExcludeCurrentProjectFromGlobalWorkItems() {
        when(searchWorkItemsQueryHandler.handle(any(SearchWorkItemsQuery.class)))
                .thenReturn(new PageView<>(List.of(), 0L, 0, 0, 10));
        when(workItemReadPort.searchVisibleWorkItems(any())).thenReturn(List.of());
        when(projectReadPort.getProjects(TENANT_ID, USER_ID, Set.of(), "abc", null, null, false, 0, 10, "name", "asc"))
                .thenReturn(new PageResult<>(List.of(), 0L));

        handler.handle(new PmGlobalSearchQuery(
                TENANT_ID,
                USER_ID,
                null,
                "abc",
                99,
                10L
        ));

        ArgumentCaptor<serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria> captor =
                ArgumentCaptor.forClass(serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria.class);
        verify(workItemReadPort).searchVisibleWorkItems(captor.capture());
        assertEquals(10, captor.getValue().limit());
        assertEquals(10L, captor.getValue().excludedProjectId());
    }
}
```

- [ ] **Step 2: Run the handler test and verify it fails**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=PmGlobalSearchQueryHandlerTest test
```

Expected: compile fails because the new global search classes and `searchVisibleWorkItems` method do not exist.

- [ ] **Step 3: Create the query and response contract**

Create `PmGlobalSearchQuery.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record PmGlobalSearchQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String query,
        Integer limit,
        Long currentProjectId
) implements IQuery {
}
```

Create `PmGlobalSearchType.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

public enum PmGlobalSearchType {
    CURRENT_PROJECT_WORK_ITEM,
    WORK_ITEM,
    PROJECT
}
```

Create `PmGlobalSearchItemView.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PmGlobalSearchItemView(
        String id,
        String title,
        String subtitle,
        String url,
        Map<String, Object> meta
) {
}
```

Create `PmGlobalSearchGroupView.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import java.util.List;

public record PmGlobalSearchGroupView(
        PmGlobalSearchType type,
        String title,
        long total,
        List<PmGlobalSearchItemView> items
) {
}
```

Create `PmGlobalSearchResponseView.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import java.util.List;

public record PmGlobalSearchResponseView(
        String query,
        int limit,
        List<PmGlobalSearchGroupView> groups
) {
}
```

- [ ] **Step 4: Add visible work item criteria shell**

Create `VisibleWorkItemSearchCriteria.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import java.util.Set;

public record VisibleWorkItemSearchCriteria(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String keyword,
        Long excludedProjectId,
        int limit
) {
}
```

- [ ] **Step 5: Add the read-port method signature**

Modify `IWorkItemReadPort.java`:

```java
List<WorkItemEntity> searchVisibleWorkItems(VisibleWorkItemSearchCriteria criteria);
```

Add the import:

```java
import serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria;
```

- [ ] **Step 6: Add a temporary adapter implementation that returns no results**

Modify `WorkItemReadAdapter.java` so the project compiles before the real SQL task:

```java
@Override
public List<WorkItemEntity> searchVisibleWorkItems(VisibleWorkItemSearchCriteria criteria) {
    return List.of();
}
```

Add the import:

```java
import serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria;
```

- [ ] **Step 7: Implement the query handler**

Create `PmGlobalSearchQueryHandler.java`:

```java
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
                projects.totalItems(),
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
```

- [ ] **Step 8: Run the handler test and verify it passes**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=PmGlobalSearchQueryHandlerTest test
```

Expected: PASS.

- [ ] **Step 9: Commit backend contract and handler**

```powershell
git add pm_core/src/main/java/serp/project/pmcore/application/search/query/global pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/VisibleWorkItemSearchCriteria.java pm_core/src/main/java/serp/project/pmcore/domain/workitem/port/read/IWorkItemReadPort.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java pm_core/src/test/java/serp/project/pmcore/application/search/query/global/PmGlobalSearchQueryHandlerTest.java
git commit -m "feat(pm): add global search query contract"
```

---

### Task 2: Backend Visible Cross-Project Work Item Search

**Files:**

- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapterTest.java`

- [ ] **Step 1: Add an adapter test for the SQL parameters and result mapping**

Add a test method to `WorkItemReadAdapterTest.java`. If the existing test uses a real repository setup instead of a mocked `NamedParameterJdbcTemplate`, create a new nested setup inside the same file using Mockito for this method.

Use this assertion shape:

```java
@Test
void searchVisibleWorkItemsShouldUseVisibleProjectPermissionFilterAndExcludeCurrentProject() {
    VisibleWorkItemSearchCriteria criteria = new VisibleWorkItemSearchCriteria(
            1L,
            2L,
            Set.of("dev-team"),
            "serp",
            10L,
            5
    );

    when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(rowMapper)))
            .thenReturn(List.of(WorkItemEntity.builder()
                    .id(100L)
                    .projectId(11L)
                    .key("SERP-1")
                    .summary("Cross project search")
                    .build()));

    List<WorkItemEntity> result = adapter.searchVisibleWorkItems(criteria);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
    verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), eq(rowMapper));

    String sql = sqlCaptor.getValue().replaceAll("\\s+", " ");
    MapSqlParameterSource params = paramsCaptor.getValue();
    assertTrue(sql.contains("FROM work_items w"));
    assertTrue(sql.contains("JOIN projects p ON p.id = w.project_id"));
    assertTrue(sql.contains("UPPER(TRIM(pse.permission_key)) = 'BROWSE_PROJECTS'"));
    assertTrue(sql.contains("w.project_id <> :excludedProjectId"));
    assertEquals(1L, params.getValue("tenantId"));
    assertEquals(2L, params.getValue("userId"));
    assertEquals(",dev-team,", params.getValue("groupKeysCsv"));
    assertEquals("serp", params.getValue("keyword"));
    assertEquals(10L, params.getValue("excludedProjectId"));
    assertEquals(5, params.getValue("limit"));
    assertEquals("SERP-1", result.getFirst().getKey());
}
```

- [ ] **Step 2: Run the adapter test and verify it fails**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=WorkItemReadAdapterTest#searchVisibleWorkItemsShouldUseVisibleProjectPermissionFilterAndExcludeCurrentProject test
```

Expected: FAIL because `searchVisibleWorkItems` still returns `List.of()` and does not query JDBC.

- [ ] **Step 3: Implement `searchVisibleWorkItems` with visible-project SQL**

Replace the temporary method in `WorkItemReadAdapter.java` with:

```java
@Override
public List<WorkItemEntity> searchVisibleWorkItems(VisibleWorkItemSearchCriteria criteria) {
    if (criteria == null || criteria.keyword() == null || criteria.keyword().trim().length() < 2) {
        return List.of();
    }
    String sql = """
            SELECT
                w.id, w.tenant_id, w.project_id, w.issue_type_id,
                w.issue_no, w.key, w.summary, w.description,
                w.workflow_step_id, w.status_id, w.priority_id, w.resolution_id,
                w.assignee_id, w.reporter_id, w.parent_id,
                w.security_level_id, w.start_date, w.due_date, w.rank,
                w.time_original_estimate, w.time_remaining_estimate, w.time_spent,
                w.created_at, w.updated_at, w.created_by, w.updated_by,
                it.name AS issue_type_name, it.icon_url AS issue_type_icon_url,
                it.hierarchy_level AS issue_type_hierarchy_level,
                pr.name AS priority_name, pr.icon_url AS priority_icon_url,
                pr.color AS priority_color, pr.sequence AS priority_sequence,
                st.status_key AS status_key, st.name AS status_name,
                st.icon_url AS status_icon_url,
                sc.key AS status_category_key, sc.name AS status_category_name
            FROM work_items w
            JOIN projects p ON p.id = w.project_id
                AND p.tenant_id = w.tenant_id
                AND p.deleted_at IS NULL
                AND p.archived = false
            LEFT JOIN issue_types it ON w.issue_type_id = it.id
                AND it.tenant_id = w.tenant_id
                AND it.deleted_at IS NULL
            LEFT JOIN priorities pr ON w.priority_id = pr.id
                AND pr.tenant_id = w.tenant_id
                AND pr.deleted_at IS NULL
            LEFT JOIN statuses st ON w.status_id = st.id
                AND st.tenant_id = w.tenant_id
                AND st.deleted_at IS NULL
            LEFT JOIN status_categories sc ON st.category_id = sc.id
                AND sc.tenant_id = w.tenant_id
                AND sc.deleted_at IS NULL
            WHERE w.tenant_id = :tenantId
              AND w.deleted_at IS NULL
              AND (:excludedProjectId IS NULL OR w.project_id <> :excludedProjectId)
              AND (w.key ILIKE CONCAT('%', :keyword, '%') OR w.summary ILIKE CONCAT('%', :keyword, '%'))
              AND EXISTS (
                    SELECT 1
                    FROM permission_scheme_entries pse
                    WHERE pse.scheme_id = p.permission_scheme_id
                      AND pse.tenant_id = p.tenant_id
                      AND pse.deleted_at IS NULL
                      AND UPPER(TRIM(pse.permission_key)) = 'BROWSE_PROJECTS'
                      AND (
                            (UPPER(TRIM(pse.grantee_type)) = 'USER' AND CAST(:userId AS TEXT) = pse.grantee_ref)
                            OR (
                                UPPER(TRIM(pse.grantee_type)) = 'PROJECT_LEAD'
                                AND p.lead_user_id = :userId
                            )
                            OR (
                                UPPER(TRIM(pse.grantee_type)) = 'GROUP'
                                AND :groupKeysCsv <> ''
                                AND POSITION(CONCAT(',', LOWER(TRIM(pse.grantee_ref)), ',') IN :groupKeysCsv) > 0
                            )
                            OR (
                                UPPER(TRIM(pse.grantee_type)) IN ('ANY_LOGGED_IN_USER', 'LOGGED_IN_USER', 'AUTHENTICATED')
                                AND :userId IS NOT NULL
                            )
                            OR (
                                UPPER(TRIM(pse.grantee_type)) = 'PROJECT_ROLE'
                                AND EXISTS (
                                    SELECT 1
                                    FROM project_roles prj
                                    JOIN project_role_actors pra ON pra.project_role_id = prj.id
                                    WHERE pra.project_id = p.id
                                      AND pra.tenant_id = p.tenant_id
                                      AND pra.deleted_at IS NULL
                                      AND prj.deleted_at IS NULL
                                      AND (prj.tenant_id = p.tenant_id OR prj.tenant_id = 0)
                                      AND prj.name = pse.grantee_ref
                                      AND (
                                            (UPPER(TRIM(pra.subject_type)) = 'USER' AND CAST(:userId AS TEXT) = pra.subject_id)
                                            OR (
                                                UPPER(TRIM(pra.subject_type)) = 'GROUP'
                                                AND :groupKeysCsv <> ''
                                                AND POSITION(CONCAT(',', LOWER(TRIM(pra.subject_id)), ',') IN :groupKeysCsv) > 0
                                            )
                                      )
                                )
                            )
                      )
              )
            ORDER BY
                CASE WHEN LOWER(w.key) = LOWER(:keyword) THEN 0 ELSE 1 END,
                CASE WHEN LOWER(w.key) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END,
                CASE WHEN LOWER(w.summary) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END,
                COALESCE(w.updated_at, w.created_at) DESC NULLS LAST,
                w.id DESC
            LIMIT :limit
            """;
    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tenantId", criteria.tenantId())
            .addValue("userId", criteria.userId())
            .addValue("groupKeysCsv", toNormalizedCsv(criteria.groupKeys()))
            .addValue("keyword", criteria.keyword().trim())
            .addValue("excludedProjectId", criteria.excludedProjectId())
            .addValue("limit", Math.max(1, Math.min(criteria.limit(), 10)));
    return jdbcTemplate.query(sql, params, rowMapper);
}
```

Add this helper to `WorkItemReadAdapter.java`:

```java
private String toNormalizedCsv(Set<String> groupKeys) {
    if (groupKeys == null || groupKeys.isEmpty()) {
        return "";
    }
    return groupKeys.stream()
            .filter(groupKey -> groupKey != null && !groupKey.isBlank())
            .map(groupKey -> groupKey.trim().toLowerCase())
            .distinct()
            .sorted()
            .reduce(",", (csv, groupKey) -> csv + groupKey + ",");
}
```

Add the import:

```java
import java.util.Set;
```

- [ ] **Step 4: Run the adapter test and handler test**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=WorkItemReadAdapterTest#searchVisibleWorkItemsShouldUseVisibleProjectPermissionFilterAndExcludeCurrentProject,PmGlobalSearchQueryHandlerTest test
```

Expected: PASS.

- [ ] **Step 5: Commit visible work item search**

```powershell
git add pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapterTest.java
git commit -m "feat(pm): search visible work items across projects"
```

---

### Task 3: Backend REST Endpoint

**Files:**

- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/search/PmGlobalSearchController.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/ui/rest/search/PmGlobalSearchControllerTest.java`

- [ ] **Step 1: Write the failing controller test**

Create `PmGlobalSearchControllerTest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQuery;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQueryHandler;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchResponseView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmGlobalSearchControllerTest {

    @Mock
    private AuthUtils authUtils;

    @Mock
    private ResponseUtils responseUtils;

    @Mock
    private PmGlobalSearchQueryHandler handler;

    private PmGlobalSearchController controller;

    @BeforeEach
    void setUp() {
        controller = new PmGlobalSearchController(authUtils, responseUtils, handler);
    }

    @Test
    void searchShouldResolveAuthAndDelegateToHandler() {
        PmGlobalSearchResponseView view = new PmGlobalSearchResponseView("serp", 5, List.of());
        GeneralResponse<PmGlobalSearchResponseView> envelope = GeneralResponse.<PmGlobalSearchResponseView>builder()
                .data(view)
                .build();

        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(1L));
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(2L));
        when(authUtils.getCurrentGroups()).thenReturn(Set.of("devs"));
        when(handler.handle(any(PmGlobalSearchQuery.class))).thenReturn(view);
        when(responseUtils.success(view)).thenReturn(envelope);

        ResponseEntity<GeneralResponse<PmGlobalSearchResponseView>> response = controller.search("serp", 5, 10L);

        assertEquals(envelope, response.getBody());
        verify(handler).handle(new PmGlobalSearchQuery(1L, 2L, Set.of("devs"), "serp", 5, 10L));
    }

    @Test
    void searchShouldRejectMissingTenant() {
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> controller.search("serp", 5, null));

        assertEquals(DomainErrorCode.TENANT_NOT_FOUND, exception.getErrorCode());
    }
}
```

- [ ] **Step 2: Run the controller test and verify it fails**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=PmGlobalSearchControllerTest test
```

Expected: compile fails because `PmGlobalSearchController` and `PathConstants.SEARCH` do not exist.

- [ ] **Step 3: Add the path constant**

Modify `PathConstants.java`:

```java
public static final String SEARCH = API_BASE_PATH + "/search";
```

Place it after `API_BASE_PATH`.

- [ ] **Step 4: Add the controller**

Create `PmGlobalSearchController.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQuery;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchQueryHandler;
import serp.project.pmcore.application.search.query.global.PmGlobalSearchResponseView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.SEARCH)
@RequiredArgsConstructor
public class PmGlobalSearchController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final PmGlobalSearchQueryHandler handler;

    @GetMapping
    public ResponseEntity<GeneralResponse<PmGlobalSearchResponseView>> search(
            @RequestParam String q,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long currentProjectId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        PmGlobalSearchResponseView response = handler.handle(new PmGlobalSearchQuery(
                tenantId,
                userId,
                authUtils.getCurrentGroups(),
                q,
                limit,
                currentProjectId
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
```

- [ ] **Step 5: Run backend search tests**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=PmGlobalSearchControllerTest,PmGlobalSearchQueryHandlerTest test
```

Expected: PASS.

- [ ] **Step 6: Commit backend REST endpoint**

```powershell
git add pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java pm_core/src/main/java/serp/project/pmcore/ui/rest/search/PmGlobalSearchController.java pm_core/src/test/java/serp/project/pmcore/ui/rest/search/PmGlobalSearchControllerTest.java
git commit -m "feat(pm): expose global search endpoint"
```

---

### Task 4: Frontend Search API And Types

**Files:**

- Create: `serp_web/src/modules/pm/types/global-search.types.ts`
- Create: `serp_web/src/modules/pm/api/globalSearchApi.ts`
- Modify: `serp_web/src/modules/pm/types/api.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`

- [ ] **Step 1: Add PM global search types**

Create `global-search.types.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search types
 */

export type PMGlobalSearchType =
  | 'CURRENT_PROJECT_WORK_ITEM'
  | 'WORK_ITEM'
  | 'PROJECT';

export interface PMGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
  meta?: Record<string, string | number | boolean | null>;
}

export interface PMGlobalSearchGroup {
  type: PMGlobalSearchType;
  title: string;
  total: number;
  items: PMGlobalSearchItem[];
}

export interface PMGlobalSearchResponse {
  query: string;
  limit: number;
  groups: PMGlobalSearchGroup[];
}

export interface PMGlobalSearchParams {
  q: string;
  limit?: number;
  currentProjectId?: number;
}
```

- [ ] **Step 2: Export the types from PM type barrel**

Modify `types/api.ts`:

```ts
export type * from './global-search.types';
```

Place it with the other `export type *` statements.

- [ ] **Step 3: Add the RTK Query endpoint**

Create `globalSearchApi.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type {
  PMGlobalSearchParams,
  PMGlobalSearchResponse,
} from '../types/api';

export const pmGlobalSearchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmGlobalSearch: builder.query<
      PMGlobalSearchResponse,
      PMGlobalSearchParams
    >({
      query: ({ q, limit = 5, currentProjectId }) => ({
        url: '/search',
        method: 'GET',
        params: {
          q,
          limit,
          ...(typeof currentProjectId === 'number'
            ? { currentProjectId }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMGlobalSearchResponse>(),
    }),
  }),
  overrideExisting: false,
});

export const { useGetPmGlobalSearchQuery, useLazyGetPmGlobalSearchQuery } =
  pmGlobalSearchApi;
```

- [ ] **Step 4: Export the API**

Modify `api/index.ts`:

```ts
export {
  pmGlobalSearchApi,
  useGetPmGlobalSearchQuery,
  useLazyGetPmGlobalSearchQuery,
} from './globalSearchApi';
```

Add these type exports to the existing type export list:

```ts
PMGlobalSearchGroup,
PMGlobalSearchItem,
PMGlobalSearchParams,
PMGlobalSearchResponse,
PMGlobalSearchType,
```

- [ ] **Step 5: Run frontend type check**

Run from `serp_web/`:

```powershell
npm run type-check
```

Expected: PASS.

- [ ] **Step 6: Commit frontend API**

```powershell
git add serp_web/src/modules/pm/types/global-search.types.ts serp_web/src/modules/pm/types/api.ts serp_web/src/modules/pm/api/globalSearchApi.ts serp_web/src/modules/pm/api/index.ts
git commit -m "feat(pm): add global search api"
```

---

### Task 5: Frontend Header Quick Search

**Files:**

- Create: `serp_web/src/modules/pm/components/search/PMGlobalSearchDropdown.tsx`
- Create: `serp_web/src/modules/pm/components/search/index.ts`
- Modify: `serp_web/src/modules/pm/components/layout/PMHeader.tsx`

- [ ] **Step 1: Add search component exports**

Create `components/search/index.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM search component exports
 */

export { PMGlobalSearchDropdown } from './PMGlobalSearchDropdown';
```

- [ ] **Step 2: Add the dropdown component**

Create `PMGlobalSearchDropdown.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search dropdown
 */

'use client';

import { Search } from 'lucide-react';

import type { PMGlobalSearchGroup, PMGlobalSearchItem } from '../../types/api';

interface PMGlobalSearchDropdownProps {
  groups: PMGlobalSearchGroup[];
  isLoading: boolean;
  isError: boolean;
  query: string;
  onSelect: (item: PMGlobalSearchItem) => void;
  onViewAll: () => void;
}

export function PMGlobalSearchDropdown({
  groups,
  isLoading,
  isError,
  query,
  onSelect,
  onViewAll,
}: PMGlobalSearchDropdownProps) {
  const hasResults = groups.some((group) => group.items.length > 0);

  return (
    <div className='absolute left-0 right-0 top-full z-50 mt-2 overflow-hidden rounded-md border bg-background shadow-lg'>
      {isLoading && (
        <div className='px-3 py-2 text-sm text-muted-foreground'>
          Searching...
        </div>
      )}

      {isError && !isLoading && (
        <div className='px-3 py-2 text-sm text-destructive'>
          Search failed. Try again.
        </div>
      )}

      {!isLoading && !isError && !hasResults && (
        <div className='px-3 py-2 text-sm text-muted-foreground'>
          No results for "{query}"
        </div>
      )}

      {!isLoading &&
        !isError &&
        groups.map((group) =>
          group.items.length ? (
            <div key={group.type} className='border-b last:border-b-0'>
              <div className='bg-muted/50 px-3 py-1.5 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
                {group.title}
              </div>
              <div className='py-1'>
                {group.items.map((item) => (
                  <button
                    key={`${group.type}-${item.id}`}
                    type='button'
                    className='flex w-full items-start gap-2 px-3 py-2 text-left text-sm hover:bg-muted'
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => onSelect(item)}
                  >
                    <Search className='mt-0.5 h-4 w-4 text-muted-foreground' />
                    <span className='min-w-0 flex-1'>
                      <span className='block truncate font-medium'>
                        {item.title}
                      </span>
                      {item.subtitle && (
                        <span className='block truncate text-xs text-muted-foreground'>
                          {item.subtitle}
                        </span>
                      )}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          ) : null
        )}

      <button
        type='button'
        className='w-full px-3 py-2 text-left text-sm font-medium text-primary hover:bg-muted'
        onMouseDown={(event) => event.preventDefault()}
        onClick={onViewAll}
      >
        View all results
      </button>
    </div>
  );
}
```

- [ ] **Step 3: Add debounce and search behavior to `PMHeader`**

Modify `PMHeader.tsx` imports:

```tsx
import React, { useEffect, useMemo, useState } from 'react';
import { useLazyGetPmGlobalSearchQuery } from '@/modules/pm/api';
import type { PMGlobalSearchItem } from '@/modules/pm/types/api';
import { PMGlobalSearchDropdown } from '../search';
```

Add state and API hook inside `PMHeader`:

```tsx
const [isSearchOpen, setIsSearchOpen] = useState(false);
const [triggerSearch, searchResult] = useLazyGetPmGlobalSearchQuery();
const trimmedSearchQuery = searchQuery.trim();
```

Add this effect after `initialProjectId`:

```tsx
useEffect(() => {
  if (trimmedSearchQuery.length < 2) {
    setIsSearchOpen(false);
    return;
  }

  const timeoutId = window.setTimeout(() => {
    triggerSearch({
      q: trimmedSearchQuery,
      limit: 5,
      currentProjectId: initialProjectId,
    });
    setIsSearchOpen(true);
  }, 300);

  return () => window.clearTimeout(timeoutId);
}, [initialProjectId, triggerSearch, trimmedSearchQuery]);
```

Replace `handleSearch`:

```tsx
const handleSearch = (e: React.FormEvent) => {
  e.preventDefault();
  if (!trimmedSearchQuery) return;

  const firstResult = searchResult.data?.groups
    .flatMap((group) => group.items)
    .at(0);

  if (firstResult) {
    router.push(firstResult.url);
    setIsSearchOpen(false);
    return;
  }

  router.push(`/pm/search?q=${encodeURIComponent(trimmedSearchQuery)}`);
  setIsSearchOpen(false);
};
```

Add these handlers:

```tsx
const handleSearchItemSelect = (item: PMGlobalSearchItem) => {
  router.push(item.url);
  setSearchQuery('');
  setIsSearchOpen(false);
};

const handleViewAllSearchResults = () => {
  if (!trimmedSearchQuery) return;
  router.push(`/pm/search?q=${encodeURIComponent(trimmedSearchQuery)}`);
  setIsSearchOpen(false);
};
```

Update the search input props:

```tsx
onFocus={() => {
  if (trimmedSearchQuery.length >= 2) {
    setIsSearchOpen(true);
  }
}}
onKeyDown={(event) => {
  if (event.key === 'Escape') {
    setIsSearchOpen(false);
  }
}}
```

Render the dropdown inside the search form, after `Input`:

```tsx
{isSearchOpen && trimmedSearchQuery.length >= 2 && (
  <PMGlobalSearchDropdown
    groups={searchResult.data?.groups ?? []}
    isLoading={searchResult.isFetching}
    isError={searchResult.isError}
    query={trimmedSearchQuery}
    onSelect={handleSearchItemSelect}
    onViewAll={handleViewAllSearchResults}
  />
)}
```

- [ ] **Step 4: Run frontend checks**

Run from `serp_web/`:

```powershell
npm run lint
npm run type-check
```

Expected: PASS.

- [ ] **Step 5: Commit header quick search**

```powershell
git add serp_web/src/modules/pm/components/search serp_web/src/modules/pm/components/layout/PMHeader.tsx
git commit -m "feat(pm): add header global search dropdown"
```

---

### Task 6: Frontend Full Search Page

**Files:**

- Create: `serp_web/src/modules/pm/components/search/PMGlobalSearchResults.tsx`
- Modify: `serp_web/src/modules/pm/components/search/index.ts`
- Create: `serp_web/src/modules/pm/pages/PMGlobalSearchPage.tsx`
- Create: `serp_web/src/app/pm/search/page.tsx`
- Modify: `serp_web/src/modules/pm/index.ts`

- [ ] **Step 1: Add shared grouped results component**

Create `PMGlobalSearchResults.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search grouped results
 */

'use client';

import Link from 'next/link';
import { Search } from 'lucide-react';

import type { PMGlobalSearchGroup } from '../../types/api';

interface PMGlobalSearchResultsProps {
  groups: PMGlobalSearchGroup[];
}

export function PMGlobalSearchResults({ groups }: PMGlobalSearchResultsProps) {
  const visibleGroups = groups.filter((group) => group.items.length > 0);

  if (!visibleGroups.length) {
    return (
      <div className='rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground'>
        No results found.
      </div>
    );
  }

  return (
    <div className='space-y-6'>
      {visibleGroups.map((group) => (
        <section key={group.type} className='space-y-3'>
          <div className='flex items-center justify-between'>
            <h2 className='text-base font-semibold'>{group.title}</h2>
            <span className='text-sm text-muted-foreground'>
              {group.total} result{group.total === 1 ? '' : 's'}
            </span>
          </div>
          <div className='divide-y rounded-md border'>
            {group.items.map((item) => (
              <Link
                key={`${group.type}-${item.id}`}
                href={item.url}
                className='flex items-start gap-3 p-4 transition-colors hover:bg-muted/60'
              >
                <Search className='mt-1 h-4 w-4 text-muted-foreground' />
                <span className='min-w-0 flex-1'>
                  <span className='block font-medium'>{item.title}</span>
                  {item.subtitle && (
                    <span className='mt-1 block text-sm text-muted-foreground'>
                      {item.subtitle}
                    </span>
                  )}
                </span>
              </Link>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}
```

- [ ] **Step 2: Export the results component**

Modify `components/search/index.ts`:

```ts
export { PMGlobalSearchResults } from './PMGlobalSearchResults';
```

- [ ] **Step 3: Add the page component**

Create `PMGlobalSearchPage.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM global search page
 */

'use client';

import { useSearchParams } from 'next/navigation';

import { useGetPmGlobalSearchQuery } from '../api';
import { PMGlobalSearchResults } from '../components/search';

export function PMGlobalSearchPage() {
  const searchParams = useSearchParams();
  const query = searchParams.get('q')?.trim() ?? '';
  const shouldSearch = query.length >= 2;
  const { data, isFetching, isError } = useGetPmGlobalSearchQuery(
    { q: query, limit: 10 },
    { skip: !shouldSearch }
  );

  if (!shouldSearch) {
    return (
      <div className='mx-auto w-full max-w-4xl px-6 py-8'>
        <h1 className='text-2xl font-bold'>Search PM</h1>
        <p className='mt-2 text-sm text-muted-foreground'>
          Enter at least two characters from the PM search box.
        </p>
      </div>
    );
  }

  return (
    <div className='mx-auto w-full max-w-4xl px-6 py-8'>
      <div className='mb-6'>
        <h1 className='text-2xl font-bold'>Search results</h1>
        <p className='mt-2 text-sm text-muted-foreground'>
          Results for "{query}"
        </p>
      </div>

      {isFetching && (
        <div className='rounded-md border p-6 text-sm text-muted-foreground'>
          Searching...
        </div>
      )}

      {isError && !isFetching && (
        <div className='rounded-md border border-destructive/30 p-6 text-sm text-destructive'>
          Search failed. Refresh the page or try another query.
        </div>
      )}

      {!isFetching && !isError && (
        <PMGlobalSearchResults groups={data?.groups ?? []} />
      )}
    </div>
  );
}
```

- [ ] **Step 4: Add the route entry**

Create `src/app/pm/search/page.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM search route
 */

import { PMGlobalSearchPage } from '@/modules/pm';

export default function Page() {
  return <PMGlobalSearchPage />;
}
```

- [ ] **Step 5: Export the page from PM module**

Modify `modules/pm/index.ts`:

```ts
export { PMGlobalSearchPage } from './pages/PMGlobalSearchPage';
```

- [ ] **Step 6: Run frontend verification**

Run from `serp_web/`:

```powershell
npm run lint
npm run type-check
npm run format:check
```

Expected: PASS.

- [ ] **Step 7: Commit full search page**

```powershell
git add serp_web/src/modules/pm/components/search/PMGlobalSearchResults.tsx serp_web/src/modules/pm/components/search/index.ts serp_web/src/modules/pm/pages/PMGlobalSearchPage.tsx serp_web/src/app/pm/search/page.tsx serp_web/src/modules/pm/index.ts
git commit -m "feat(pm): add global search results page"
```

---

### Task 7: Final Verification

**Files:**

- Verify all files touched by Tasks 1-6.

- [ ] **Step 1: Run backend focused tests**

Run from `pm_core/`:

```powershell
.\mvnw.cmd -Dtest=PmGlobalSearchQueryHandlerTest,PmGlobalSearchControllerTest,WorkItemReadAdapterTest test
```

Expected: PASS.

- [ ] **Step 2: Run backend compile**

Run from `pm_core/`:

```powershell
.\mvnw.cmd clean compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Run frontend checks**

Run from `serp_web/`:

```powershell
npm run lint
npm run type-check
npm run format:check
```

Expected: all commands exit 0.

- [ ] **Step 4: Manual smoke test**

Start the frontend and backend in the usual local environment. Then verify:

- In `/pm/dashboard`, typing one character does not open results.
- In `/pm/dashboard`, typing two or more characters opens grouped results without the current-project group.
- In `/pm/projects/10/summary`, typing a matching query sends `currentProjectId=10`.
- Current-project work item results appear before global work items and projects.
- Clicking a project result navigates to `/pm/projects/{projectId}/summary`.
- Clicking a work item result navigates to `/pm/projects/{projectId}/work-items/{workItemId}`.
- Pressing `Escape` closes the dropdown.
- Pressing `Enter` opens the first result when one exists.
- Pressing `Enter` with no result navigates to `/pm/search?q=<query>`.
- `/pm/search?q=serp` renders grouped results.

- [ ] **Step 5: Commit final verification fixes**

If verification required small fixes:

```powershell
git add pm_core serp_web
git commit -m "fix(pm): polish global search verification"
```

If no fixes were required, do not create an empty commit.
