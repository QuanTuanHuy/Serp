# PM Global Search Design

## Goal

Add global search for the Project Management module across projects and work items. The search should work from the PM header for quick navigation and from a full results page for deeper browsing.

The first version prioritizes correctness, permission safety, and low implementation risk over advanced ranking or full-text indexing.

## Scope

In scope:

- Search visible projects.
- Search visible work items.
- Prioritize the current project when the user searches from a project route.
- Show quick grouped results from the PM header.
- Provide a full `/pm/search` results page.

Out of scope:

- Fuzzy typo matching.
- Comments, descriptions, components, users, people, workflows, statuses, priorities, or settings object search.
- Persistent recent searches.
- Search analytics.
- Dedicated search index or external search engine.

## Backend API

Add a PM-owned read endpoint in `pm_core`:

```http
GET /api/v1/search?q=<query>&limit=5&currentProjectId=<optional>
```

The controller should live under a new search-focused REST package, for example `ui/rest/search`, and delegate to an application query handler such as `application/search/query/global`.

Response shape should match the existing admin/settings global search pattern:

```ts
interface PmGlobalSearchResponse {
  query: string;
  limit: number;
  groups: PmGlobalSearchGroup[];
}

type PmGlobalSearchType =
  | 'CURRENT_PROJECT_WORK_ITEM'
  | 'WORK_ITEM'
  | 'PROJECT';

interface PmGlobalSearchGroup {
  type: PmGlobalSearchType;
  title: string;
  total: number;
  items: PmGlobalSearchItem[];
}

interface PmGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
  meta?: Record<string, string | number | boolean | null>;
}
```

Group order:

1. `CURRENT_PROJECT_WORK_ITEM`, only when `currentProjectId` is visible and has matches.
2. `WORK_ITEM`.
3. `PROJECT`.

Canonical URLs:

- Project: `/pm/projects/{projectId}/summary`
- Work item: `/pm/projects/{projectId}/work-items/{workItemId}`

## Backend Behavior

The query handler must be read-only and tenant-scoped.

It should:

- Resolve `tenantId`, `userId`, and groups from `AuthUtils`.
- Trim and normalize `q`.
- Return empty groups for blank or one-character queries.
- Default `limit` to `5`.
- Clamp `limit` to a safe maximum. Use `10` for the header path; allow the full search page to request a larger limit only if the endpoint explicitly supports it.
- Search project keys and names for project results.
- Search work item keys and summaries for work item results.
- Exclude archived projects and deleted records by default.
- Return empty groups, not errors, when there are no matches.
- Silently omit the current-project group if `currentProjectId` is missing, invalid, inaccessible, or not visible.

Permission behavior:

- Project results must use the same visibility logic as the existing project list read path.
- Current-project work item search may reuse the existing `SearchWorkItemsQueryHandler`, because it checks `BROWSE_PROJECTS`.
- Cross-project work item search should avoid N+1 calls. Add a dedicated read-port method, for example `searchVisibleWorkItems(tenantId, userId, groupKeys, keyword, excludedProjectId, limit)`, backed by SQL that joins or filters through the same visible-project rules used by project listing.
- No result may reveal a project or work item that the current user cannot browse.

MVP ranking:

1. Exact key match.
2. Prefix key/name/summary match.
3. Contains match.
4. Newer updated or created records as tie-breaker.

## Frontend API

Add PM-owned search types and RTK Query endpoint under `src/modules/pm`, for example:

- `types/global-search.types.ts`
- `api/globalSearchApi.ts`

Endpoint:

```ts
getPmGlobalSearch({ q, limit, currentProjectId })
```

It should call `/search` with:

```ts
extraOptions: { service: 'pm' }
```

The response should use the same grouped contract as the backend.

## Header UX

Update `PMHeader` so the existing search input becomes a quick global search.

Behavior:

- Debounce input by roughly `250-300ms`.
- Query only when trimmed input length is at least `2`.
- Pass `currentProjectId` when the pathname matches `/pm/projects/:projectId/...`.
- Show a dropdown anchored to the input.
- Render loading, empty, error, and grouped result states.
- Clicking a result navigates to `item.url`.
- Pressing `Enter` opens the first result when available.
- Pressing `Enter` without a result navigates to `/pm/search?q=<query>`.
- Pressing `Escape` closes the dropdown.
- Include a "View all results" action that navigates to `/pm/search?q=<query>`.

The dropdown should be compact and operational in style, matching the PM header rather than introducing a new visual language.

## Full Results Page

Add a thin route:

```text
src/app/pm/search/page.tsx
```

The route should render a module-owned page component such as `PMGlobalSearchPage`.

Behavior:

- Read `q` from search params.
- Call the same global search API with a larger result limit.
- Render grouped results using titles, subtitles, and links.
- Show clear loading, empty, and error states.
- Do not add filters in MVP.

## Implementation Slices

1. Add backend response records, query, query handler, and controller for `/api/v1/search`.
2. Add a cross-project visible work item search read-port method and infrastructure query.
3. Add backend focused tests for query normalization, permission behavior, group ordering, and limit clamping.
4. Add frontend PM global search types/API.
5. Update `PMHeader` quick search behavior.
6. Add `/pm/search` and `PMGlobalSearchPage`.

## Verification

Backend:

- Run focused tests for the new search handler and read query.
- Run the narrow Maven command for the new test class.
- Run broader `pm_core` tests if the SQL or permission logic touches shared read paths.

Frontend:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

Manual smoke checks:

- Blank and one-character queries do not call the search API.
- Matching current-project work items appear first inside project routes.
- The current-project group is absent outside project routes.
- Search results do not expose inaccessible projects or work items.
- Clicking project and work item results navigates to the canonical PM routes.
- `/pm/search?q=...` renders the same grouped result contract with a larger limit.
