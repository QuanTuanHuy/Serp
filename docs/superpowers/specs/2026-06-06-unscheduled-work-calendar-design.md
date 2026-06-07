# Unscheduled Work Calendar Design

## Context

Project calendar schedule mode already reads scheduled plan allocations from
`GET /projects/{projectId}/calendar/schedule-allocations` and updates manual
plans through `PUT /projects/{projectId}/work-items/{id}/schedule`.
`WorkItemPlanEntity`, `WorkItemPlanAllocationEntity`, `WorkItemPlanModel`, and
`WorkItemPlanAllocationModel` already exist, so this feature should not add a
new table or persistence model.

The missing behavior is listing work items without an active schedule plan and
allowing users to create a manual plan by dropping one onto the calendar.

## Goals

- Show unscheduled work items in a right-side panel on the project calendar.
- Keep existing calendar keyword and filters useful for both scheduled and
  unscheduled work.
- Let a user drag an unscheduled item onto a calendar day to create a manual
  `WorkItemPlanEntity` and one `WorkItemPlanAllocationModel`.
- Reuse the existing schedule update endpoint and permission checks.
- Refresh the schedule calendar and unscheduled panel after a successful drop.

## Non-Goals

- No schema migration.
- No optimization-run changes.
- No multi-assignee split allocation from the unscheduled panel.
- No support for scheduling unassigned work items.

## Backend Design

Extend work item search with a nullable `hasActivePlan` filter:

- `hasActivePlan=true` returns work items with an active `work_item_plans` row.
- `hasActivePlan=false` returns work items without an active plan.
- `null` keeps existing search behavior.

The filter is implemented inside the work item read query using `EXISTS` or
`NOT EXISTS` against `work_item_plans` with matching `tenant_id`,
`project_id`, `work_item_id`, and `deleted_at IS NULL`.

The existing search handler remains responsible for project browse permission.
The existing `PUT /projects/{projectId}/work-items/{id}/schedule` command
continues to create or update `WorkItemPlanEntity` and replace allocations. It
also keeps the current schedule permission and issue-security checks.

## Frontend Design

In `PMProjectCalendarPage`, schedule mode loads unscheduled work through
`useSearchPmWorkItemsQuery` with:

- `hasActivePlan: false`
- current keyword
- current assignee, issue type, and status filters
- `enriched: true`
- a practical page size for the side panel

The page renders a right-side "Unscheduled work" panel only in schedule mode.
Each row shows the work item key, summary, status, and assignee. The row is
draggable and can still be clicked to open the existing work item detail dialog.

`PMProjectCalendarGrid` accepts an optional drop callback. Each day cell in
schedule mode becomes a drop target for unscheduled work item IDs.

## Drop Behavior

When a user drops an unscheduled item on a calendar day:

- If the item has no `assigneeId`, show a toast error and do not call the API.
- Create a manual schedule plan from 09:00 to 17:00 for the dropped day in
  UTC+7.
- Use one allocation for the current work item assignee.
- Use `timeRemainingEstimate`, then `timeOriginalEstimate`, then 8 hours as
  the `effortMillis` fallback.
- Send `locked: true`.

On success, refetch the schedule calendar and unscheduled work query so the
item disappears from the panel and appears on the calendar.

## Error Handling

Frontend API failures use the existing `getErrorMessage` helper and toast
feedback. Backend validation failures continue to use
`DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID` through the existing schedule
command.

## Testing

Backend:

- Add a focused unit or adapter-level test for `hasActivePlan=false` search
  query behavior.
- Add coverage that `hasActivePlan=null` preserves existing behavior if the
  query builder test style supports it.

Frontend:

- The repo has no configured frontend test framework, so verify with
  `npm run lint`, `npm run type-check`, and `npm run format:check`.
- Manually inspect drag/drop behavior in the browser if a dev server is used.

