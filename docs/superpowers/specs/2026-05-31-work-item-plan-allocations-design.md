# Work Item Plan Allocations Design

## Goal

After an optimization run is applied, users must be able to track the exact time blocks when a work item is planned, not only the summary `planned_start` and `planned_end` range.

## Design

Keep `work_item_plans` as the summary planning record for one work item. Add `work_item_plan_allocations` as child records for the actual scheduled blocks. Each allocation stores tenant, project, plan id, work item id, assignee id, start, end, effort millis, source, source run id, and source run item id.

Optimization review continues to store proposed chunks in `optimization_run_items.allocation_chunks_json`. When the user applies a schedule suggestion, the apply handler upserts the active `work_item_plans` row, deletes existing allocations for that plan, and inserts allocation rows from the run item chunks. If the user overrode only summary start/end, existing chunks are not reused.

Capacity resolution should prefer `work_item_plan_allocations` for existing planned workload. If a plan has no allocations, it falls back to the legacy `planned_start`/`planned_end` overlap so older plans remain compatible.

## API Impact

Work item detail and timeline can return allocation chunks from active plans. The summary dates still come from `work_item_plans`.

## Testing

- Applying an accepted optimization schedule persists allocation chunks.
- Capacity deduction uses allocation chunks instead of the whole summary plan range.
- Work item detail/timeline can expose active plan allocations.
