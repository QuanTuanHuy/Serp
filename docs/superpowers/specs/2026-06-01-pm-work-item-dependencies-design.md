# PM Work Item Dependencies Tab Design

## Context

SERP already has the core domain pieces needed to model dependency relationships between work items.

- `IssueLinkEntity` stores directed issue links as `sourceId`, `targetId`, and `linkTypeId`.
- `IssueLinkTypeEntity` stores `dependencyBehavior`.
- `IssueLinkDependencyBehavior` supports `NONE`, `SOURCE_BLOCKS_TARGET`, and `SOURCE_DEPENDS_ON_TARGET`.
- The optimization domain already builds `OptimizationDependencyGraph` from issue links.
- The scheduling policy already respects predecessor and successor order when dependency cycles are absent.
- The frontend currently shows linked work items on the work item detail surface, but it does not provide a project-level dependency view.

The new feature is a read-only project-level `Dependencies` tab that helps users inspect dependency structure before using optimization.

## Goals

- Add a `Dependencies` project tab at the same navigation level as `List`, `Board`, `Timeline`, and `Calendar`.
- Show a hybrid dependency view: graph summary plus precise edge table.
- Reuse familiar project work item filters where possible.
- Default to showing outside-filter dependencies so blockers are not hidden.
- Keep the first phase read-only.
- Use the same dependency semantics as the optimization domain.

## Non-Goals

- Creating, editing, or deleting dependency edges from the new tab.
- Applying optimization suggestions or mutating work item plans from the new tab.
- Replacing the existing work item detail linked-item list.
- Replacing the timeline dependency response.

## Route And Scope

The frontend route should be:

```text
/pm/projects/{projectId}/dependencies
```

This is a project-level route, not a sub-view of `PMWorkItemListTab`. This keeps the list/detail view state separate from dependency analysis and matches the existing project-level views such as board and timeline.

The first implementation is read-only. Allowed actions are limited to navigation and analysis:

- Open a work item detail route or dialog.
- Select work items or edges for follow-up actions.
- Start optimization with selected work item ids.
- Refresh and change filters.

## Dependency Semantics

The dependency view must use the same semantics as optimization:

- `SOURCE_BLOCKS_TARGET`: `sourceId` is the predecessor and `targetId` is the successor.
- `SOURCE_DEPENDS_ON_TARGET`: `targetId` is the predecessor and `sourceId` is the successor.
- `NONE`: not treated as an optimization dependency.

By default, the dependency tab shows only links where `dependencyBehavior != NONE`.

The UI can offer an `Include related links` toggle. When enabled, `NONE` links can appear in the table as relationship links, but they must not be counted as dependency blockers, blocked items, cycles, or optimization constraints.

## Filter Behavior

The dependency tab should support the same core filters users expect from project work item views:

- `keyword`
- `statusIds`
- `assigneeIds`
- `issueTypeIds`
- `priorityIds`
- `parentId`
- `componentIds`

The default filter mode is focused scope plus outside dependencies:

- Work items that match the filter form the focused scope.
- If a focused work item has a dependency edge to a work item outside the filter, that outside endpoint is still returned.
- Outside endpoints and edges are marked as `outsideFilter`.

The UI should include a toggle equivalent to strict mode:

- `Show outside dependencies`: default `true`.
- When disabled, only edges where both endpoints match the active filter are returned or shown.

This default prevents hidden blockers. For example, if a filtered story depends on an unfiltered task, the unfiltered blocker remains visible by default.

## Backend API

Add a dedicated PM Core read endpoint:

```text
GET /projects/{projectId}/work-items/dependencies
```

Recommended query params:

- `keyword`
- `statusIds`
- `assigneeIds`
- `issueTypeIds`
- `priorityIds`
- `parentId`
- `componentIds`
- `includeOutside`, default `true`
- `includeNonDependencyLinks`, default `false`
- `page` and `pageSize` if the implementation needs bounded graph scope

Use a dedicated read model instead of reusing `IssueLinkView`.

Suggested response shape:

```json
{
  "projectId": 10,
  "nodes": [
    {
      "id": 101,
      "key": "PM-101",
      "summary": "Implement API",
      "status": {},
      "assignee": {},
      "issueType": {},
      "priority": {},
      "dueDate": 1770000000000,
      "plannedStart": 1769000000000,
      "plannedEnd": 1769500000000,
      "outsideFilter": false,
      "blockedByCount": 0,
      "blocksCount": 2,
      "hasCycle": false
    }
  ],
  "edges": [
    {
      "id": 501,
      "predecessorId": 101,
      "successorId": 102,
      "sourceId": 101,
      "targetId": 102,
      "linkType": {},
      "dependencyBehavior": "SOURCE_BLOCKS_TARGET",
      "outsideFilter": false,
      "externalProject": false,
      "cycle": false
    }
  ],
  "summary": {
    "nodeCount": 2,
    "dependencyCount": 1,
    "outsideDependencyCount": 0,
    "blockerCount": 1,
    "blockedItemCount": 1,
    "cycleCount": 0
  },
  "cycles": [[101, 102, 101]]
}
```

Backend responsibilities:

- Check tenant and project browse permission once in the query handler.
- Load filtered work items through existing read patterns.
- Load dependency links in batch; do not loop over per-work-item link endpoints for the project tab.
- Resolve link types and dependency behavior.
- Convert link direction into predecessor and successor ids.
- Include outside-filter endpoints when `includeOutside=true`.
- Detect cycles over included dependency edges.
- Return summary counts derived from dependency edges, not from all issue links.

If existing ports only support listing links by one work item id, add a batch read method to avoid N+1 query behavior.

## Frontend Design

Add a project-level page:

- `PMProjectDependenciesPage`
- `PMDependencyToolbar`
- `PMDependencyFilters`
- `PMDependencySummaryStrip`
- `PMDependencyGraphPanel`
- `PMDependencyEdgeTable`
- `pmDependencyGraph.utils.ts`

Add frontend API contract types:

- `PMWorkItemDependencyNodeApi`
- `PMWorkItemDependencyEdgeApi`
- `PMWorkItemDependencyGraphResponse`
- `PMGetWorkItemDependenciesParams`

Add API wiring:

- `getPmWorkItemDependencies` in `workItemApi.ts`
- `buildWorkItemDependencyParams` in `queryParams.ts`
- `extraOptions: { service: 'pm' }`
- cache tag such as `pm/WorkItemDependencies`

The toolbar should include:

- keyword search
- refresh
- filter button
- `Show outside dependencies` toggle, default on
- `Include related links` toggle, default off

The summary strip should show:

- dependency count
- blocker count
- blocked item count
- outside filter count
- cycle count

The graph panel should:

- render directed edges from predecessor to successor
- highlight blockers
- highlight blocked nodes
- highlight outside-filter nodes
- highlight cycle nodes and cycle edges
- degrade gracefully for large graphs by making the table the precise view

The table should show each edge with:

- predecessor work item
- relation or link type
- successor work item
- status
- assignee
- due date or planned dates
- flags such as `Cycle`, `Outside filter`, `External project`, and `Related link`

## Optimization Integration

This feature exposes dependency input that optimization already uses.

Existing optimization behavior includes:

- building a dependency graph from issue links
- detecting cycles
- computing critical path
- increasing priority score for blockers
- scheduling by topological readiness
- enforcing successor start after predecessor end
- validating dependency violations
- delaying selected successors based on outside predecessor active plans

The dependency tab should make these relationships visible before users run optimization.

Read-only optimization-related actions:

- Select work items and navigate to optimization with `selected` ids.
- Warn when selected scope has outside blockers.
- Highlight cycles because scheduling cannot generate a schedule when cycles exist.

The dependency tab must not run optimization or apply optimization results in the first phase.

## Validation

Backend tests should cover:

- `SOURCE_BLOCKS_TARGET` conversion into predecessor and successor.
- `SOURCE_DEPENDS_ON_TARGET` conversion by flipping direction.
- `NONE` links excluded by default.
- `NONE` links included only when `includeNonDependencyLinks=true`.
- outside-filter endpoints included when `includeOutside=true`.
- strict mode excludes outside-filter edges.
- simple cycle detection, such as `A -> B -> C -> A`.
- permission and tenant scoping.

Frontend verification should include:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`
- `npm run build` if route wiring or build-sensitive code changes

There is currently no configured frontend test framework in `serp_web`, so the initial frontend phase relies on static checks and manual QA.

Manual QA cases:

- no dependency edges
- one blocker with multiple successors
- outside-filter blocker visible by default
- strict toggle hides outside-filter edges
- dependency cycle highlighted
- related links toggle includes non-dependency links without counting them as optimization blockers

## Risks

- Large project graphs can become unreadable. The table should remain the reliable analysis surface.
- Reusing timeline response directly would overload timeline semantics. The dependency tab should have its own read model.
- Reimplementing graph semantics differently from optimization can cause drift. Direction conversion must match optimization rules.
- Calling link listing per work item would create N+1 behavior and should be avoided.

## Rollout

1. Add the backend dependency read model and endpoint.
2. Add frontend API contract and route.
3. Build hybrid UI: toolbar, filters, summary, graph, and table.
4. Wire read-only navigation and optional optimize-selected action.
5. Later phase: dependency editing and pre-save cycle warnings.
