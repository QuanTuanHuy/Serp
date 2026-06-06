# Work Item Schedule Editing Design

## Context

Project schedules are currently created by optimization apply flows and displayed on the project calendar from `work_item_plans` and `work_item_plan_allocations`. Users can inspect schedule allocations in `PMProjectCalendarPage`, but they cannot edit the resulting plan. The domain already has the right persistence shape for manual edits:

- `WorkItemPlanEntity` stores one active plan per work item, including `plannedStart`, `plannedEnd`, `source`, `sourceRunId`, and `locked`.
- `WorkItemPlanAllocationEntity` stores the concrete schedule blocks for a plan.
- `ApplyOptimizationRunCommandHandler` already skips locked active plans, so manually locked schedules can be protected from future optimization applies.

## Goals

- Let users manually edit a work item's schedule from the project calendar.
- Keep `work_item_plans` as the source of truth for schedule dates.
- Keep allocations consistent with their parent plan.
- Preserve the optimization contract: generated schedules can be accepted, then manually adjusted, and locked manual plans are not overwritten by later optimization applies.

## Non-Goals

- No draft/publish schedule workflow.
- No drag-and-drop calendar interaction in this slice.
- No partial allocation-only API that can leave plan dates and allocation dates inconsistent.
- No frontend test framework setup.

## Recommended Approach

Use a plan-level replace API. The client edits from a selected allocation, but the mutation submits the complete intended state for the work item's active plan:

- `plannedStart`
- `plannedEnd`
- `locked`
- `allocations[]`

The backend upserts the active plan with `source = MANUAL`, clears optimization source metadata, and replaces all allocations for the plan in one transaction.

This matches the existing `IWorkItemPlanPort.upsertActivePlan(...)` and `IWorkItemPlanAllocationPort.replaceForPlan(...)` abstractions. It also avoids a separate allocation patch API that would need extra reconciliation logic to keep plan boundaries valid.

## Backend Design

Add a command package under `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/`:

- `UpdateWorkItemPlanCommand`
- `UpdateWorkItemPlanAllocationCommand`
- `UpdateWorkItemPlanCommandHandler`
- `UpdateWorkItemPlanResult`

Add a REST request DTO under `ui/rest/workitem/dto/request/`:

- `UpdateWorkItemPlanRequest`
- nested allocation request type

Expose:

```text
PUT /api/v1/projects/{projectId}/work-items/{workItemId}/schedule
```

The controller resolves `tenantId`, `userId`, and groups from `AuthUtils`, delegates to the command handler, and returns `GeneralResponse<UpdateWorkItemPlanResult>`.

Validation and authorization:

- Require positive `projectId`, `workItemId`, `plannedStart`, and `plannedEnd`.
- Require `plannedStart < plannedEnd`.
- Require each allocation to have positive `assigneeId`, `start`, `end`, and `effortMillis`.
- Require each allocation `start < end`.
- Require each allocation to be inside `[plannedStart, plannedEnd]`.
- Require the project to exist, be in the tenant, and not be archived.
- Require the work item to exist, belong to the project, and pass issue security.
- Require `BROWSE_PROJECTS` and `SCHEDULE_ISSUES`.

Persistence behavior:

- Upsert `WorkItemPlanEntity` for the work item.
- Set `source = MANUAL`.
- Set `sourceRunId = null`.
- Set `locked` from the request, defaulting to `true` when omitted.
- Replace all allocations for the saved plan.
- Set each allocation `source = MANUAL`, `sourceRunId = null`, and `sourceRunItemId = null`.

## Frontend Design

Extend `serp_web/src/modules/pm/api/workItemApi.ts`:

- Add `updatePmWorkItemSchedule` mutation.
- Invalidate the edited work item and the work item list/calendar tags.

Extend `work-item-api.types.ts`:

- `PMUpdateWorkItemScheduleRequest`
- `PMUpdateWorkItemScheduleAllocationRequest`
- `PMUpdateWorkItemScheduleResponse`

Update `PMProjectScheduleAllocationSheet.tsx`:

- Keep the read-only summary as the default state.
- Add an `Edit` action for schedule allocations.
- In edit mode, show fields for selected block start, end, effort, assignee, and locked state.
- On save, submit the whole plan using the selected allocation plus related allocations for the same work item currently available in the calendar payload.

Update `PMProjectCalendarPage.tsx`:

- Wire the mutation and toast feedback.
- Refetch/refresh the schedule query after a successful save.
- Keep the existing "Open work item" behavior.

The first implementation will edit the selected allocation in the context of the allocations loaded in the current viewport. If a work item has allocations outside the current viewport, the backend still treats the request as full replacement, so the UI should make this explicit by deriving the editable allocation list from the same calendar response. A future enhancement can add a dedicated "get full plan" query before editing.

## Error Handling

Backend business failures use existing domain exceptions:

- `PROJECT_ARCHIVED` for archived projects.
- `WORK_ITEM_NOT_FOUND` or `NOT_FOUND` for missing/mismatched work items.
- `WORK_ITEM_SCHEDULE_INVALID` for invalid plan/allocation ranges.
- Existing access denied exceptions for missing permissions or security access.

Frontend uses `.unwrap()` inside `try/catch` and `getErrorMessage(...)` for failure toasts.

## Testing

Backend tests:

- Handler saves a manual locked plan and replaces allocations.
- Handler defaults `locked` to `true` when the request omits it.
- Handler rejects invalid plan range.
- Handler rejects allocation outside the plan range.
- Controller maps auth/request data into the command.

Frontend verification:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

There is no checked-in frontend test runner in `serp_web`, so no frontend unit test is added in this slice.

## Open Follow-Ups

- Add a dedicated full-plan read endpoint if users need to edit work items whose allocations extend outside the current calendar viewport.
- Add drag/drop resizing after the plan-level API exists.
- Add audit/history entries for schedule plan edits if product requires a visible change log.
