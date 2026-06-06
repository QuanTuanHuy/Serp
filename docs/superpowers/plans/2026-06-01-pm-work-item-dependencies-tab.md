# Work Item Dependencies Tab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a read-only project-level Dependencies tab that shows filtered work items, outside blockers, and optimization-aligned dependency chains.

**Architecture:** Add a dedicated PM Core dependency read model that reuses the existing work-item search port for focused nodes and a batch issue-link read for graph expansion. On the frontend, add a new project tab route that renders a hybrid page with summary counters, a depth-limited ReactFlow graph, and an edge table driven by RTK Query. Keep dependency semantics aligned with optimization by normalizing `SOURCE_BLOCKS_TARGET` and `SOURCE_DEPENDS_ON_TARGET` the same way the optimization model builder does.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, ReactFlow, Tailwind/Shadcn primitives.

---

## File Structure

- Create `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemDependencyCriteria.java`
  - Mutable query-param model for dependency filters, `depth`, `includeOutside`, and `includeRelatedLinks`.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQuery.java`
  - CQRS query wrapper with tenant, user, groups, and criteria.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependencyNodeView.java`
  - API-facing node row with work-item metadata, blocker counts, and cycle flags.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependencyEdgeView.java`
  - API-facing edge row with normalized predecessor/successor ids and link flags.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependencySummaryView.java`
  - Aggregate counters for blockers, blocked items, outside dependencies, related links, and cycles.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependenciesPageView.java`
  - Full read response with nodes, edges, summary, and paging metadata.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/support/WorkItemDependencyGraphBuilder.java`
  - Expand seed items to the configured depth, normalize link direction, and mark cycles.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQueryHandler.java`
  - Permission check, focused search, link expansion, node/edge mapping, and response assembly.
- Create `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemDependenciesController.java`
  - Thin GET endpoint that binds `WorkItemDependencyCriteria` and delegates to the handler.
- Modify `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java`
  - Add `/projects/{projectId}/work-items/dependencies`.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/port/IIssueLinkPort.java`
  - Add a batch list method for all links touching a set of work item ids.
- Modify `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IIssueLinkRepository.java`
  - Add the SQL query that returns link details for a list of work item ids.
- Modify `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/IssueLinkAdapter.java`
  - Map the new batch repository result into `IssueLinkDetailEntity`.
- Create `pm_core/src/test/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQueryHandlerTest.java`
  - Focused regression tests for direction normalization, outside blockers, related links, depth, and cycles.
- Modify or create `pm_core/src/test/java/serp/project/pmcore/ui/rest/workitem/WorkItemDependenciesControllerTest.java`
  - Verify auth context, query-param defaults, and request-to-query mapping.
- Create `serp_web/src/app/pm/projects/[projectId]/(detail)/dependencies/page.tsx`
  - Route wrapper that renders the module page.
- Modify `serp_web/src/modules/pm/types/work-item-api.types.ts`
  - Add dependency response and query-param contracts.
- Modify `serp_web/src/modules/pm/api/queryParams.ts`
  - Serialize dependency filters, `depth`, and toggle defaults.
- Modify `serp_web/src/modules/pm/api/workItemApi.ts`
  - Add the new GET endpoint and hook export.
- Modify `serp_web/src/modules/pm/api/index.ts`
  - Re-export the new hook and types.
- Modify `serp_web/src/modules/pm/components/projects/PMProjectsTopTabs.tsx`
  - Add the `Dependencies` project tab and active-tab routing.
- Modify `serp_web/src/modules/pm/index.ts`
  - Export the new project page component.
- Create `serp_web/src/modules/pm/pages/PMProjectDependenciesPage.tsx`
  - Main hybrid page with toolbar, filters, summary, graph, table, and selection actions.
- Create `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesToolbar.tsx`
  - Search, refresh, filter button, `show outside`, `include related`, and depth selector.
- Create `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesFilters.tsx`
  - Reusable filter drawer for keyword, status, assignee, issue type, priority, parent, and component.
- Create `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesSummary.tsx`
  - Counter strip for blockers, blocked items, outside dependencies, related links, and cycles.
- Create `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesGraph.tsx`
  - ReactFlow view with cycle highlighting and outside-node emphasis.
- Create `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesTable.tsx`
  - Precise edge table with selection and navigation.
- Create `serp_web/src/modules/pm/components/projects/dependencies/pmProjectDependencies.utils.ts`
  - Query-param parsing, filter counting, graph layout helpers, and edge labeling.

## Task 1: Backend Dependency Read Model

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemDependencyCriteria.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQuery.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependencyNodeView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependencyEdgeView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependencySummaryView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/WorkItemDependenciesPageView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/support/WorkItemDependencyGraphBuilder.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQueryHandler.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/port/IIssueLinkPort.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IIssueLinkRepository.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/IssueLinkAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQueryHandlerTest.java`

- [ ] **Step 1: Write the failing handler tests**

Create tests that pin the graph semantics before implementation:

```java
@Test
void handleShouldNormalizeBlockAndDependsOnDirections() {
    // SOURCE_BLOCKS_TARGET => source is predecessor
}

@Test
void handleShouldIncludeOutsideBlockersByDefault() {
    // focus scope stays filtered, outside endpoint is still returned
}

@Test
void handleShouldExcludeNoneLinksUnlessRelatedLinksIsEnabled() {
    // NONE links stay out of blocker counts by default
}

@Test
void handleShouldMarkCyclesWithinTheExpandedDepth() {
    // A -> B -> C -> A flags the cycle summary and edge flags
}
```

Assert all of the following:

- `depth` defaults to `2`.
- `includeOutside` defaults to `true`.
- `includeRelatedLinks` defaults to `false`.
- `SOURCE_BLOCKS_TARGET` becomes `predecessorId = sourceId`, `successorId = targetId`.
- `SOURCE_DEPENDS_ON_TARGET` flips direction.
- `NONE` links are excluded unless `includeRelatedLinks = true`.
- outside endpoints are included when they are needed to preserve blocker visibility.
- cycles are marked on both the edges and the summary.

- [ ] **Step 2: Run the focused backend test and verify RED**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=ListWorkItemDependenciesQueryHandlerTest test
```

Expected: compile failure or test failure because the new criteria, query, views, and port method do not exist yet.

- [ ] **Step 3: Implement the dependency query model**

Create the criteria class with the same binding style as `WorkItemTimelineCriteria`:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemDependencyCriteria extends PageCriteria {
    private static final int DEFAULT_DEPTH = 2;
    private static final int MAX_DEPTH = 5;

    private Long projectId;
    private String keyword;
    private Long parentId;
    private List<Long> statusIds;
    private List<Long> assigneeIds;
    private List<Long> issueTypeIds;
    private List<Long> priorityIds;
    private List<Long> componentIds;
    private Boolean includeOutside;
    private Boolean includeRelatedLinks;
    private Integer depth;

    public boolean isIncludeOutside() {
        return !Boolean.FALSE.equals(includeOutside);
    }

    public boolean isIncludeRelatedLinks() {
        return Boolean.TRUE.equals(includeRelatedLinks);
    }

    public int getEffectiveDepth() {
        if (depth == null) {
            return DEFAULT_DEPTH;
        }
        if (depth < 1) {
            return 1;
        }
        return Math.min(depth, MAX_DEPTH);
    }
}
```

Add the query and view records:

```java
public record ListWorkItemDependenciesQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        WorkItemDependencyCriteria criteria
) { }
```

```java
public record WorkItemDependencyEdgeView(
        Long linkId,
        Long sourceId,
        Long targetId,
        Long predecessorId,
        Long successorId,
        Long linkTypeId,
        String linkTypeName,
        String dependencyBehavior,
        boolean outsideFilter,
        boolean externalProject,
        boolean relatedLink,
        boolean cycle
) { }
```

```java
public record WorkItemDependencyNodeView(
        Long id,
        Long projectId,
        String key,
        String summary,
        Long statusId,
        String statusName,
        Long issueTypeId,
        String issueTypeName,
        Long priorityId,
        String priorityName,
        Long assigneeId,
        String assigneeName,
        Long dueDate,
        Long plannedStart,
        Long plannedEnd,
        boolean outsideFilter,
        int blockedByCount,
        int blocksCount,
        boolean hasCycle
) { }
```

```java
public record WorkItemDependencySummaryView(
        long nodeCount,
        long dependencyCount,
        long outsideDependencyCount,
        long blockerCount,
        long blockedItemCount,
        long relatedLinkCount,
        long cycleCount
) { }
```

```java
public record WorkItemDependenciesPageView(
        Long projectId,
        List<WorkItemDependencyNodeView> nodes,
        List<WorkItemDependencyEdgeView> edges,
        WorkItemDependencySummaryView summary,
        long totalItems,
        int totalPages,
        int currentPage,
        int pageSize,
        int depth,
        boolean includeOutside,
        boolean includeRelatedLinks
) { }
```

Implement the batch port on the existing issue-link path:

```java
List<IssueLinkDetailEntity> listByWorkItemIds(Long tenantId, List<Long> workItemIds);
```

The repository SQL should mirror the existing single-work-item query, but switch the filter to `il.source_id IN (:workItemIds) OR il.target_id IN (:workItemIds)`.

Implement the graph builder so it:

- expands from the filtered seed set out to `getEffectiveDepth()`;
- normalizes dependency direction using `IssueLinkDependencyBehavior`;
- marks edges and nodes that are outside the filtered scope;
- marks external-project endpoints;
- detects cycles after normalization;
- keeps related links out of blocker counts unless `includeRelatedLinks` is true.

Hydrate display fields the same way the existing search and timeline queries do so nodes can show `assigneeName` and the optional planned dates from the active work-item plan.

- [ ] **Step 4: Run the backend test again and verify GREEN**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=ListWorkItemDependenciesQueryHandlerTest test
```

Expected: the dependency handler tests pass.

- [ ] **Step 5: Commit the backend read model**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemDependencyCriteria.java pm_core/src/main/java/serp/project/pmcore/application/workitem/query/dependencies pm_core/src/main/java/serp/project/pmcore/domain/issuelink/port/IIssueLinkPort.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IIssueLinkRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/IssueLinkAdapter.java pm_core/src/test/java/serp/project/pmcore/application/workitem/query/dependencies/ListWorkItemDependenciesQueryHandlerTest.java
git commit -m "feat(pm): add dependency read model"
```

## Task 2: Backend REST Endpoint

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemDependenciesController.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/ui/rest/workitem/WorkItemDependenciesControllerTest.java`

- [ ] **Step 1: Write the failing controller test**

Add a controller test that verifies the endpoint forwards auth context and query defaults:

```java
@Test
void listWorkItemDependenciesShouldDelegateWithDefaultDepthAndFlags() {
    WorkItemDependencyCriteria criteria = new WorkItemDependencyCriteria();
    criteria.setProjectId(PROJECT_ID);

    when(authUtils.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
    when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(TENANT_ID));
    when(authUtils.getCurrentGroups()).thenReturn(Set.of("pm"));
    when(handler.handle(any())).thenReturn(result);
    when(responseUtils.success(result)).thenReturn(body);

    ResponseEntity<GeneralResponse<WorkItemDependenciesPageView>> response =
            controller.listWorkItemDependencies(PROJECT_ID, criteria);

    ArgumentCaptor<ListWorkItemDependenciesQuery> captor =
            ArgumentCaptor.forClass(ListWorkItemDependenciesQuery.class);
    verify(handler).handle(captor.capture());
    assertEquals(2, captor.getValue().criteria().getEffectiveDepth());
    assertTrue(captor.getValue().criteria().isIncludeOutside());
    assertFalse(captor.getValue().criteria().isIncludeRelatedLinks());
}
```

The test must also assert:

- `tenantId` and `userId` come from `AuthUtils`;
- `projectId` is injected from the path variable;
- `groupKeys` are forwarded unchanged;
- the handler result is wrapped with `responseUtils.success(result)`.

- [ ] **Step 2: Run the controller test and verify RED**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=WorkItemDependenciesControllerTest test
```

Expected: compile failure because the controller and path constant do not exist yet.

- [ ] **Step 3: Implement the REST endpoint**

Add the path constant:

```java
public static final String WORKITEM_DEPENDENCIES = WORKITEMS + "/dependencies";
```

Add the controller method:

```java
@RestController
@RequestMapping(PathConstants.WORKITEM_DEPENDENCIES)
@RequiredArgsConstructor
public class WorkItemDependenciesController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListWorkItemDependenciesQueryHandler listWorkItemDependenciesQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<WorkItemDependenciesPageView>> listWorkItemDependencies(
            @PathVariable Long projectId,
            @ModelAttribute WorkItemDependencyCriteria criteria
    ) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        criteria.setProjectId(projectId);
        WorkItemDependenciesPageView result = listWorkItemDependenciesQueryHandler.handle(
                new ListWorkItemDependenciesQuery(
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups(),
                        criteria
                )
        );
        return ResponseEntity.ok(responseUtils.success(result));
    }
}
```

The method should:

- resolve `tenantId` and `userId` from `AuthUtils`;
- set `criteria.projectId` from the path variable;
- leave `depth`, `includeOutside`, and `includeRelatedLinks` on their criteria defaults;
- delegate to `ListWorkItemDependenciesQueryHandler`;
- return `responseUtils.success(result)`.

- [ ] **Step 4: Run the backend tests again and verify GREEN**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=ListWorkItemDependenciesQueryHandlerTest,WorkItemDependenciesControllerTest test
```

Expected: both dependency tests pass.

- [ ] **Step 5: Commit the backend endpoint**

```bash
git add pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemDependenciesController.java pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java pm_core/src/test/java/serp/project/pmcore/ui/rest/workitem/WorkItemDependenciesControllerTest.java
git commit -m "feat(pm): expose dependency tab endpoint"
```

## Task 3: Frontend API Contracts and Tab Routing

**Files:**
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/queryParams.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`
- Modify: `serp_web/src/modules/pm/components/projects/PMProjectsTopTabs.tsx`
- Modify: `serp_web/src/modules/pm/index.ts`
- Create: `serp_web/src/app/pm/projects/[projectId]/(detail)/dependencies/page.tsx`

- [ ] **Step 1: Add the TypeScript contracts**

Add these interfaces to `src/modules/pm/types/work-item-api.types.ts`:

```ts
export interface PMWorkItemDependencyNodeApi {
  id: number;
  projectId: number;
  key: string;
  summary: string;
  statusId?: number | null;
  statusName?: string | null;
  issueTypeId?: number | null;
  issueTypeName?: string | null;
  priorityId?: number | null;
  priorityName?: string | null;
  assigneeId?: number | null;
  assigneeName?: string | null;
  dueDate?: number | null;
  plannedStart?: number | null;
  plannedEnd?: number | null;
  outsideFilter: boolean;
  blockedByCount: number;
  blocksCount: number;
  hasCycle: boolean;
}

export interface PMWorkItemDependencyEdgeApi {
  id: number;
  sourceId: number;
  targetId: number;
  predecessorId: number;
  successorId: number;
  linkTypeId?: number | null;
  linkTypeName?: string | null;
  dependencyBehavior?: string | null;
  outsideFilter: boolean;
  externalProject: boolean;
  relatedLink: boolean;
  cycle: boolean;
}

export interface PMWorkItemDependencySummaryApi {
  nodeCount: number;
  dependencyCount: number;
  outsideDependencyCount: number;
  blockerCount: number;
  blockedItemCount: number;
  relatedLinkCount: number;
  cycleCount: number;
}

export interface PMWorkItemDependenciesResponse {
  projectId: number;
  nodes: PMWorkItemDependencyNodeApi[];
  edges: PMWorkItemDependencyEdgeApi[];
  summary: PMWorkItemDependencySummaryApi;
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  depth: number;
  includeOutside: boolean;
  includeRelatedLinks: boolean;
}

export interface PMGetWorkItemDependenciesParams {
  keyword?: string;
  statusIds?: number[];
  assigneeIds?: number[];
  issueTypeIds?: number[];
  priorityIds?: number[];
  parentId?: number;
  componentIds?: number[];
  includeOutside?: boolean;
  includeRelatedLinks?: boolean;
  depth?: number;
  page?: number;
  pageSize?: number;
}
```

Add the serializer:

```ts
export function buildWorkItemDependencyParams(
  params?: PMGetWorkItemDependenciesParams
): QueryParams {
  return {
    ...optionalString('keyword', params?.keyword),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalNumberList('priorityIds', params?.priorityIds),
    ...optionalNumber('parentId', params?.parentId),
    ...optionalNumberList('componentIds', params?.componentIds),
    ...optionalBoolean('includeOutside', params?.includeOutside),
    ...optionalBoolean('includeRelatedLinks', params?.includeRelatedLinks),
    ...optionalNumber('depth', params?.depth),
    page: params?.page ?? 0,
    pageSize: params?.pageSize ?? 100,
  };
}
```

- [ ] **Step 2: Run a frontend type check and verify RED**

Run:

```powershell
cd serp_web
npm run type-check
```

Expected: missing-type and missing-export errors until the new contracts and endpoint wiring are added.

- [ ] **Step 3: Add the API endpoint and tab route**

Add the RTK Query endpoint:

```ts
getPmWorkItemDependencies: builder.query<
  PMWorkItemDependenciesResponse,
  { projectId: number; params?: PMGetWorkItemDependenciesParams }
>({
  query: ({ projectId, params }) => ({
    url: `/projects/${projectId}/work-items/dependencies`,
    method: 'GET',
    params: buildWorkItemDependencyParams(params),
  }),
  extraOptions: { service: 'pm' },
  transformResponse: createDataTransform<PMWorkItemDependenciesResponse>(),
  providesTags: (_result, _error, { projectId }) => [
    { type: 'pm/WorkItemDependencies', id: projectId },
  ],
})
```

Export the hook from `api/index.ts` and the new page from `src/modules/pm/index.ts`.

Add the `Dependencies` tab to `PMProjectsTopTabs.tsx` and make `getActiveTabKey(pathname)` return `dependencies` for the new route.

Add the route wrapper:

```tsx
export default async function PMProjectDependenciesPageRoute({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  return <PMProjectDependenciesPage projectId={projectId} />;
}
```

- [ ] **Step 4: Run the frontend type check again and verify GREEN**

Run:

```powershell
cd serp_web
npm run type-check
```

Expected: the new dependencies endpoint and route compile cleanly.

- [ ] **Step 5: Commit the routing and contract work**

```bash
git add serp_web/src/modules/pm/types/work-item-api.types.ts serp_web/src/modules/pm/api/queryParams.ts serp_web/src/modules/pm/api/workItemApi.ts serp_web/src/modules/pm/api/index.ts serp_web/src/modules/pm/components/projects/PMProjectsTopTabs.tsx serp_web/src/modules/pm/index.ts serp_web/src/app/pm/projects/[projectId]/(detail)/dependencies/page.tsx
git commit -m "feat(pm-web): add dependencies tab route"
```

## Task 4: Frontend Hybrid Dependencies Page

**Files:**
- Create: `serp_web/src/modules/pm/pages/PMProjectDependenciesPage.tsx`
- Create: `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesToolbar.tsx`
- Create: `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesFilters.tsx`
- Create: `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesSummary.tsx`
- Create: `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesGraph.tsx`
- Create: `serp_web/src/modules/pm/components/projects/dependencies/PMProjectDependenciesTable.tsx`
- Create: `serp_web/src/modules/pm/components/projects/dependencies/pmProjectDependencies.utils.ts`

- [ ] **Step 1: Build the page shell with typed props**

Start with a compilable shell that loads the query and exposes the read-only controls:

```tsx
export function PMProjectDependenciesPage({
  projectId,
}: {
  projectId: string;
}) {
  const numericProjectId = Number(projectId);
  const [keyword, setKeyword] = useState('');
  const [depth, setDepth] = useState(2);
  const [includeOutside, setIncludeOutside] = useState(true);
  const [includeRelatedLinks, setIncludeRelatedLinks] = useState(false);
  const [selectedWorkItemIds, setSelectedWorkItemIds] = useState<number[]>([]);

  const {
    data,
    error,
    isFetching,
    refetch,
  } = useGetPmWorkItemDependenciesQuery(
    {
      projectId: numericProjectId,
      params: {
        keyword: keyword.trim() || undefined,
        depth,
        includeOutside,
        includeRelatedLinks,
        page: 0,
        pageSize: 100,
      },
    },
    { skip: !Number.isFinite(numericProjectId) }
  );
}
```

Wire the page to `useGetPmWorkItemDependenciesQuery` with:

- `depth` defaulting to `2`;
- `includeOutside` defaulting to `true`;
- `includeRelatedLinks` defaulting to `false`;
- the same filter fields used by list/board views: `keyword`, `statusIds`, `assigneeIds`, `issueTypeIds`, `priorityIds`, `parentId`, `componentIds`.

The page should also:

- show a compact summary strip;
- render a ReactFlow graph;
- render a precise edge table;
- keep the page read-only;
- allow selecting work items and navigating to optimization with `selected=` ids.

- [ ] **Step 2: Add the toolbar and filters**

Implement the toolbar with:

- search input;
- refresh button;
- filter button;
- `Show outside dependencies` toggle, default `on`;
- `Include related links` toggle, default `off`;
- depth selector defaulting to `2`.

Implement the filter drawer with the same filter vocabulary as the other project work-item views. Keep it project-scoped and URL-driven so the tab state is shareable.

- [ ] **Step 3: Add the summary strip, graph, and table**

The summary strip should show:

- dependency count;
- blocker count;
- blocked item count;
- outside dependency count;
- related link count;
- cycle count.

The graph should:

- render directed edges from predecessor to successor;
- highlight cycle nodes and edges;
- highlight outside-filter nodes;
- keep related links visually distinct but not counted as blockers;
- degrade gracefully when the graph is dense by keeping the table as the exact source of truth.

The table should show:

- predecessor work item;
- relation or link type;
- successor work item;
- status;
- assignee;
- due date or planned dates;
- flags for `Cycle`, `Outside filter`, `External project`, and `Related`.

Use the existing `PMWorkItemDetailDialog` for row navigation rather than inventing a new detail surface.

- [ ] **Step 4: Run the frontend checks**

Run:

```powershell
cd serp_web
npm run lint
npm run type-check
npm run format:check
npm run build
```

Expected: the dependencies page compiles, formats, and bundles cleanly.

- [ ] **Step 5: Commit the page implementation**

```bash
git add serp_web/src/modules/pm/pages/PMProjectDependenciesPage.tsx serp_web/src/modules/pm/components/projects/dependencies serp_web/src/modules/pm/components/projects/dependencies/pmProjectDependencies.utils.ts
git commit -m "feat(pm-web): add dependencies hybrid page"
```

## Task 5: Final Verification

**Files:**
- All files touched in Tasks 1-4.

- [ ] **Step 1: Run the focused backend and frontend checks**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=ListWorkItemDependenciesQueryHandlerTest,WorkItemDependenciesControllerTest test
.\mvnw.cmd clean compile

cd ..\serp_web
npm run lint
npm run type-check
npm run format:check
npm run build
```

Expected: all commands pass, or any failure is pre-existing and unrelated to the dependency tab work.

- [ ] **Step 2: Inspect the diff**

Run:

```powershell
git diff --stat
git diff --check
```

Expected: only dependency-tab files changed and no whitespace errors are reported.

- [ ] **Step 3: Self-review against the spec**

Confirm the plan and implementation path covers:

- project-level route at `/pm/projects/{projectId}/dependencies`;
- hybrid presentation with summary, graph, and table;
- same core filters as the other project work-item views;
- `Show outside dependencies` default `on`;
- depth default `2` so blockers stay visible;
- `includeRelatedLinks` default `off`;
- read-only behavior in phase one;
- dependency semantics aligned with optimization;
- navigation to optimization from selected items, without mutating dependencies.

Also confirm there are no placeholder steps, no ambiguous file paths, and no type names introduced later that were not defined earlier.
