# PM Project Calendar Deadline And Schedule Design

## Goal

The PM project calendar must show two different date semantics clearly:

1. Deadline mode shows when work items are due.
2. Schedule mode shows the exact planned time blocks when work will be performed.

Deadline mode can reuse the existing work item search source because a deadline belongs to a work item. Schedule mode needs a dedicated allocation source because a schedule block belongs to a work item plan allocation.

## Design

Keep the existing work item timeline endpoint for work-item-centric views. Reuse work item search for deadline mode and add one calendar-specific read API for schedule allocations:

1. `GET /projects/{projectId}/work-items` with `dueDateFrom`, `dueDateTo`, `enriched=true`, and calendar filters.
2. `GET /projects/{projectId}/calendar/schedule-allocations`.

Deadline calendar items are sourced from `work_items.due_date`. Each work item appears at most once in the requested viewport. Filters apply to work item fields: assignee, issue type, status, and keyword.

Schedule calendar items are sourced from `work_item_plan_allocations`. Each allocation chunk is one calendar block. A single work item can appear multiple times when the plan is split across multiple working intervals. Filters apply to allocation assignee for assignee filtering, and work item fields for issue type, status, and keyword.

The frontend calendar supports only week and month views. It renders day columns/cells with compact work item blocks and does not render an hourly time grid. Schedule mode groups allocation chunks by allocation start date. Deadline mode groups work items by due date.

## Interaction

Deadline mode:

1. Clicking a deadline item opens `PMWorkItemDetailDialog`.
2. The dialog can use the clicked item as fallback data while loading full detail.
3. The existing work item detail dialog remains the source for editing summary, status, assignee, due date, comments, and activity.

Schedule mode:

1. Clicking an allocation block opens a schedule allocation detail popover or sheet.
2. The allocation detail shows work item key, summary, assignee, start, end, effort, source, run id, and related chunks for the same work item when available.
3. The allocation detail includes an `Open work item` action that opens `PMWorkItemDetailDialog`.
4. Work item detail must show schedule allocation chunks in its Schedule section so users can inspect the complete plan from either mode.

## API Contracts

Deadline mode uses the existing work item search item shape. The frontend groups search results by `dueDate`.

```json
{
  "id": 1,
  "projectId": 10,
  "key": "KAN-2",
  "summary": "Optimization schedule",
  "assigneeId": 100,
  "assigneeName": "User",
  "dueDate": 1780160400000,
  "issueTypeName": "Task",
  "statusName": "In Progress",
  "priorityName": "Medium",
  "priorityColor": "#64748b"
}
```

Schedule allocation response item:

```json
{
  "allocationId": 900,
  "workItemPlanId": 800,
  "workItemId": 1,
  "projectId": 10,
  "key": "KAN-2",
  "summary": "Optimization schedule",
  "assigneeId": 100,
  "assigneeName": "User",
  "start": 1780630800000,
  "end": 1780641600000,
  "effortMillis": 10800000,
  "source": "OPTIMIZATION",
  "sourceRunId": 50,
  "sourceRunItemId": 51,
  "issueType": { "id": 1, "name": "Task", "iconUrl": null },
  "status": { "id": 2, "name": "In Progress" },
  "priority": { "id": 3, "name": "Medium", "color": "#64748b" }
}
```

The schedule allocation API accepts:

1. `viewportStart`
2. `viewportEnd`
3. `assigneeIds`
4. `issueTypeIds`
5. `statusIds`
6. `keyword`
7. `page`
8. `pageSize`

## Query Rules

Deadline mode filters the viewport through existing search criteria:

1. `dueDateFrom = viewportStart`
2. `dueDateTo = viewportEnd`
3. `sortField = due_date`
4. `sortDirection = ASC`
5. `enriched = true`

Schedule mode filters the viewport by allocation overlap:

```sql
a.start_time < :viewportEnd
AND a.end_time > :viewportStart
```

Schedule assignee filtering must use `work_item_plan_allocations.assignee_id`, not `work_items.assignee_id`.

## Boundaries

1. This design does not change optimization generation.
2. This design does not change how plans and allocations are applied.
3. This design does not add drag and drop scheduling.
4. This design does not remove the existing timeline endpoint.
5. This design does not add day view or hourly grid rendering.

## Testing

1. Deadline search returns only work items with due dates in the viewport.
2. Schedule API returns one row per allocation chunk overlapping the viewport.
3. Schedule API assignee filter uses allocation assignee.
4. A work item split across multiple days renders multiple schedule blocks.
5. Deadline item click opens `PMWorkItemDetailDialog`.
6. Schedule allocation click opens allocation detail and can open `PMWorkItemDetailDialog`.
