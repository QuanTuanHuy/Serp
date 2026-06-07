# PM Project List Redesign Design

## Context

The PM projects page currently renders a list-only table with inline category,
status, and sort filters. Project progress in the list row is deterministic fake
data derived from the project id. The redesign should make the filter surface
more compact, match the PM module's dialog-based filter pattern, support both
list and grid views, and use only data already returned by the existing project
list API.

## Scope

- Redesign `PMProjectsPage` using the current project list endpoint.
- Group project filters into a dialog similar to
  `PMWorkItemListFilters.tsx`.
- Add list and grid view modes.
- Remove all fake progress UI and fake progress calculations.
- Do not change `ProjectController.java` or add backend API fields in this
  iteration.

## UI Design

The page header keeps the projects title and create action. The toolbar becomes
a compact control strip:

- Search input remains directly visible because it is a frequent action.
- A `Filters` button opens a dialog and shows a badge when category or status
  filters are active.
- Sort remains directly visible because it changes list ordering rather than
  narrowing the result set.
- A segmented icon control switches between list and grid view.
- Clear filters resets search, category, status, sort, and paging.

The filter dialog follows the existing PM work item pattern: criteria on the
left, values on the right. Initial project criteria are `Status` and `Category`.
The dialog has a clear action and active-count badges.

List view keeps the table shape but removes progress. The category/status cell
only shows real category and archived/active state. Grid view renders responsive
project cards with project key, name, description, lead, category, status,
updated timestamp, and actions.

## Data Flow

`PMProjectsPage` remains the state owner for:

- search query and debounced search query
- category filter
- status filter
- sort option
- current page
- view mode

The existing `useGetPmProjectsQuery` call continues to pass `search`,
`categoryId`, `archived`, sort, and pagination. Category options continue to
come from `useGetProjectCategoriesQuery`.

No backend changes are required. The page maps `PMProjectSummaryApi` into
`PMProjectListItem` as it does today.

## Components

- `PMProjectListToolbar`: compact toolbar for search, sort, filter trigger,
  view toggle, result count, and clear action.
- `PMProjectListFilters`: new dialog component for status/category filters.
- `PMProjectListTable`: existing table container, adjusted only as needed for
  removed progress semantics.
- `PMProjectListRow`: table row without fake progress calculation or progress
  bar.
- `PMProjectListGrid`: new responsive grid view using the same item model and
  action handlers as the table.

## Error Handling

Existing list loading and error behavior stays unchanged. Both list and grid
views receive the same empty title and description from `PMProjectsPage`.

## Verification

Run from `serp_web/`:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

Frontend tests are not configured in this module today, so no single test command
is available.
