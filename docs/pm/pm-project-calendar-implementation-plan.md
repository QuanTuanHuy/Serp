# PM Project Calendar Implementation Plan

## Scope

Target screen:

- `serp_web/src/app/pm/projects/calendar/page.tsx`
- `serp_web/src/app/pm/projects/[projectId]/(detail)/calendar/page.tsx`

Reference direction:

- JIRA-style project calendar with month/week/day switch
- Unschedule side panel on right
- Drag work items onto calendar to set schedule

Main goals:

1. Replace legacy redirect page with real PM calendar UI.
2. Reuse existing PM timeline data before adding new BE API.
3. Support project-scoped calendar inside project detail area.
4. Support unscheduled work queue and drag/drop scheduling flow.
5. Keep implementation small, stepwise, and mergeable.

Non-goals for this plan:

1. Full Gantt implementation.
2. New permission model.
3. Full custom drag-and-drop task editor.
4. Bulk scheduling actions.
5. New persistent user calendar preferences.

## Current State Summary

Current implementation already has:

1. Legacy redirect at `src/app/pm/projects/calendar/page.tsx`.
2. Project top tabs already include `calendar` entry.
3. BE already exposes timeline endpoint:
   - `GET /api/v1/projects/{projectId}/timeline/work-items`
4. BE already exposes schedule update path via work item update flow.
5. PM work item data already carries fields needed for calendar:
   - `startDate`
   - `dueDate`
   - `status`
   - `priority`
   - `assigneeId`
   - `issueType`

Current gaps:

1. Project calendar page is not implemented yet.
2. No PM calendar API hook on FE.
3. No calendar-specific event mapping layer.
4. No view switch / date navigation / sidebar UI for PM calendar.
5. No FE drag/drop integration for PM schedule updates.
6. No explicit calendar-specific backend payload if timeline response is too heavy.

## Decision

Default path:

1. Reuse current BE timeline endpoint.
2. Build FE calendar on top of that endpoint.
3. Add BE only if FE hits a hard data gap.

Why:

1. Timeline response already contains enough core fields.
2. Calendar UX can be built without new domain write model.
3. Safer to ship FE first, then tighten API only if needed.

## Delivery Strategy

Implement in phases. Each phase should be mergeable on its own.

Recommended sequence:

1. Confirm route and data contract.
2. Add FE API + types for timeline feed.
3. Build calendar page shell and layout.
4. Build event mapping and view navigation.
5. Add unscheduled work sidebar.
6. Add drag/drop schedule updates.
7. Add BE only if calendar filters or payload need extra support.
8. Final polish and verification.

## Phase 0 - Route And Data Audit

### Goal

Lock target route and payload shape before code changes.

### Tasks

1. Confirm whether calendar lives only under project detail route:
   - `src/app/pm/projects/[projectId]/(detail)/calendar/page.tsx`
2. Decide whether legacy route stays as redirect or becomes global calendar entry.
3. Inspect timeline endpoint response shape against needed calendar fields.
4. Identify which filters must be in URL and which can stay local state.

### Files

1. `serp_web/src/app/pm/projects/calendar/page.tsx`
2. `serp_web/src/app/pm/projects/[projectId]/(detail)/calendar/page.tsx`
3. `serp_web/src/modules/pm/types/api.ts`
4. `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/*`

### Done Criteria

1. Route ownership clear.
2. Data contract clear.
3. No code behavior changed yet.

## Phase 1 - FE API And Types

### Goal

Expose calendar-ready PM timeline data to frontend.

### Tasks

1. Add FE RTK Query endpoint for timeline work items.
2. Export endpoint hook from PM API barrel.
3. Add TS types for:
   - timeline query params
   - timeline item view
   - timeline page response
4. Keep work item schedule update path reusable.
5. Keep request params serializable and URL-friendly.

### Suggested FE API Shape

1. Query params:
   - `viewportStart`
   - `viewportEnd`
   - `includeUnscheduled`
   - `parentId`
   - `depth`
   - `statusIds`
   - `assigneeIds`
   - `issueTypeIds`
   - `priorityIds`
   - `keyword`
   - `page`
   - `pageSize`
2. Response fields:
   - `items`
   - `dependencies`
   - `totalItems`
   - `totalPages`
   - `currentPage`
   - `pageSize`

### Files

1. `serp_web/src/modules/pm/api/workItemApi.ts`
2. `serp_web/src/modules/pm/api/index.ts`
3. `serp_web/src/modules/pm/types/api.ts`

### Done Criteria

1. Calendar data available through hook.
2. Types compile.
3. No UI yet.

## Phase 2 - Calendar Page Shell

### Goal

Replace placeholder page with real PM calendar layout.

### Tasks

1. Build page container for project calendar.
2. Reuse PM layout and top tabs.
3. Add top command row:
   - search
   - assignee filter
   - issue type filter
   - status filter
   - date nav
   - view selector
4. Add calendar viewport area.
5. Add right sidebar for unscheduled work.

### Layout Requirements

1. Main calendar area should dominate viewport.
2. Sidebar should be fixed width on desktop and stacked on mobile.
3. Toolbar should wrap cleanly at narrow widths.

### Files

1. `serp_web/src/app/pm/projects/[projectId]/(detail)/calendar/page.tsx`
2. new module files under `serp_web/src/modules/pm/components/projects/` or `.../calendar/`

### Done Criteria

1. Page no longer shows placeholder card.
2. Basic layout matches target screenshot direction.

## Phase 3 - Calendar Event Model

### Goal

Map timeline items into calendar events.

### Tasks

1. Convert timeline items into calendar event objects.
2. Decide event start/end rule:
   - if both dates exist, use range
   - if only `dueDate`, render as one-day or all-day item
   - if unscheduled, keep in sidebar
3. Style events by status and priority.
4. Show key labels:
   - key
   - summary
   - assignee hint if needed
5. Handle multi-day spanning correctly in month/week views.

### Mapping Rules

1. `startDate` and `dueDate` in epoch millis.
2. `unscheduled = true` items do not enter main calendar grid.
3. If `startDate > dueDate`, treat as invalid data and hide or flag.
4. `hasChildren` may affect badge or grouping only.

### Files

1. new calendar helper file under PM module
2. maybe `serp_web/src/modules/pm/types/api.ts`

### Done Criteria

1. Events render from real data.
2. Sidebar split between scheduled and unscheduled works.

## Phase 4 - Calendar Interaction

### Goal

Make calendar usable for scheduling actions.

### Tasks

1. Add month/week/day switch.
2. Add today/prev/next navigation.
3. Sync viewport range to data fetch.
4. Add drag/drop from unscheduled sidebar to calendar grid.
5. Add drag/drop move between dates for already scheduled items.
6. Call schedule update mutation on drop.

### Interaction Rules

1. Drop on day cell sets schedule range.
2. Dragging existing event updates dates.
3. Failed mutation reverts UI or refreshes data.
4. Keep sidebar item draggable only when allowed.

### Files

1. new calendar component file(s)
2. `serp_web/src/modules/pm/api/workItemApi.ts`

### Done Criteria

1. Can move work item by drag/drop.
2. Calendar refreshes after schedule change.

## Phase 5 - BE Only If Needed

### Goal

Add backend support only for real missing data or write gaps.

### Candidate BE Additions

1. Calendar-specific lightweight endpoint if timeline payload is too heavy.
2. Extra filter fields in `WorkItemTimelineCriteria` if FE needs them.
3. Dedicated schedule patch endpoint if current update flow is not clean enough for drag/drop.
4. Aggregation fields for sidebar counts if FE should not compute them client-side.

### Default Recommendation

1. Do not add new BE route first.
2. Reuse `WorkItemTimelineController` endpoint.
3. Reuse existing work item update schedule flow.

### Files If BE Changes

1. `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemTimelineController.java`
2. `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemTimelineCriteria.java`
3. `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/*`
4. `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemController.java`

### Done Criteria

1. BE change justified by gap, not speculation.
2. FE can consume payload without adapter hacks.

## Phase 6 - Polish And Verification

### Goal

Stabilize UX and verify no regressions.

### Tasks

1. Add loading state.
2. Add empty state for no scheduled items.
3. Add sidebar search for unscheduled work.
4. Add responsive behavior for mobile.
5. Add error state with retry.
6. Verify tab still sits inside project detail flow.

### Verification

FE:

1. `npm run lint`
2. `npm run type-check`
3. `npm run format:check`
4. `npm run build`

BE only if changed:

1. `./mvnw.cmd clean compile`
2. focused test for new query/handler if added

## Suggested File List

### Frontend

1. `serp_web/src/app/pm/projects/[projectId]/(detail)/calendar/page.tsx`
2. `serp_web/src/app/pm/projects/calendar/page.tsx`
3. `serp_web/src/modules/pm/api/workItemApi.ts`
4. `serp_web/src/modules/pm/api/index.ts`
5. `serp_web/src/modules/pm/types/api.ts`
6. new calendar component(s) under `serp_web/src/modules/pm/components/projects/`

### Backend

1. only if needed after FE spike

## Implementation Order For Next Session

1. Build FE API hook and types.
2. Implement page shell.
3. Add event mapper.
4. Add sidebar and filters.
5. Add drag/drop updates.
6. Review BE gap again.

## Open Questions

1. Global calendar route or project-only calendar route.
2. Need timezone-aware range query or pure epoch millis.
3. Need unscheduled bucket from BE or FE local split is enough.
4. Need editable duration on resize or only date move.
