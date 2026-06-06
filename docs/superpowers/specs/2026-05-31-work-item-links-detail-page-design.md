# Work Item Links And Detail Page Design

## Context

The PM frontend currently shows linked work items inside
`PMWorkItemDetailDialog`, but the list is card-like and does not make link
direction obvious. `pm_core` migrations model issue links as one typed relation
with two display descriptions:

- `Blocks`: outward description `blocks`, inward description `is blocked by`.
- `Clones`: outward description `clones`, inward description `is cloned by`.
- `Relates`: both directions use `relates to`.

Therefore `is blocked by` is not a fourth link type. It is the inward display
label for the existing `Blocks` type.

## Goals

- Make linked work items visually clearer and closer to Jira's grouped link
  presentation.
- Show both directions of a link clearly, including `is blocked by`.
- Allow the linked work items section to collapse and expand like other detail
  panels.
- Navigate from a linked work item row to a full work item detail page.
- Reuse the existing detail dialog content for the new detail page instead of
  duplicating behavior.

## Non-Goals

- Do not add a new backend link type for `is blocked by`.
- Do not change `pm_core` issue link migrations or persistence schema.
- Do not introduce a new test framework for `serp_web`.
- Do not redesign unrelated project or work item list screens.

## Recommended Approach

Use the existing API contract and make the change in `serp_web`.

The frontend will group `PMWorkItemLinkApi` rows by the display label already
returned by the API in `link.linkType.description`. When the backend returns an
`INWARD` link for a `Blocks` type, that description should be `is blocked by`.
This matches the `issue_link_types.inward_desc` seed value from `pm_core`.

## UI Design

The `Linked work items` section in `PMWorkItemDetailDialog` becomes
collapsible. It defaults to expanded and uses the same chevron affordance as
the existing details sidebar panel.

Inside the expanded section, links are grouped by label:

- `blocks`
- `is blocked by`
- `clones`
- `is cloned by`
- `relates to`
- any custom link description returned by the API

Each group has a small heading and compact rows. A row contains:

- a work item type icon using the existing available icon style,
- linked item key,
- linked item summary,
- linked item status badge,
- priority indicator when present,
- delete action when `onDeleteLink` is provided.

Rows are keyboard and pointer navigable. The delete button must stop event
propagation so deleting a link does not navigate.

## Navigation

Clicking a linked work item navigates to:

```text
/pm/projects/:projectId/work-items/:workItemId
```

If the linked work item payload includes `projectId`, use that value. Otherwise
fall back to the current detail view's `projectId`.

The new page route lives under the PM project route tree:

```text
serp_web/src/app/pm/projects/[projectId]/(detail)/work-items/[workItemId]/page.tsx
```

The page parses `projectId` and `workItemId`, validates that both are numeric,
and renders the shared work item detail content.

## Component Structure

Extract the current dialog body into reusable detail components:

- `PMWorkItemDetailContent`: owns data fetching and renders loading, error,
  header, main column, and sidebar.
- `PMWorkItemDetailDialog`: keeps only dialog shell concerns and renders
  `PMWorkItemDetailContent`.
- `PMWorkItemDetailPage`: renders the same content in a full-page container and
  provides a close/back behavior using Next navigation.

`WorkItemLinksList` receives `projectId` so it can build navigation targets. It
keeps delete behavior optional, matching the existing component contract.

## Data Flow

The existing RTK Query hooks remain the source of truth:

- `useGetPmWorkItemByIdQuery`
- `useGetPmWorkItemChildrenQuery`
- `useGetPmWorkItemLinksQuery`
- `useGetPmWorkItemCommentsQuery`
- `useGetPmWorkItemActivitiesQuery`
- update, transition, comment, worklog, subtask, and link mutations already used
  by the dialog

The full page should not add a new backend endpoint. It uses the same project
and work item ID query parameters as the dialog.

## Error Handling

Invalid route params show a local destructive alert instead of firing invalid
API calls.

API errors keep the current detail dialog behavior: use `getErrorMessage(...)`
inside the existing alert state.

Mutation failures keep existing toast behavior.

## Testing And Verification

Frontend tests are not configured in this repository, so verification is:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

If route wiring or shared content extraction causes uncertainty, also run:

- `npm run build`

Manual checks:

- `blocks` and `is blocked by` links appear as separate groups when both
  directions exist.
- The linked work items section collapses and expands.
- Clicking a linked row navigates to the full detail URL.
- Clicking delete removes the link and does not navigate.
- The full detail page renders the same content as the dialog.
