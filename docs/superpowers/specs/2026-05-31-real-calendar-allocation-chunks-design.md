# Real Calendar Allocation Chunks Design

## Goal

Optimization must read real user working capacity before falling back to the UTC+7 weekday calendar, and each generated schedule must expose the exact allocation chunks used to satisfy a work item's duration.

## Design

Add a persisted calendar availability source named `resource_calendar_slots`. Each row represents one available interval for one user, scoped by tenant, with `slot_start`, `slot_end`, and `capacity_millis`. A new database-backed `IResourceCalendarPort` implementation reads overlapping slots for the requested planning window. Users without real calendar slots continue to use the existing fallback calendar; the capacity result keeps `fallbackUserIds` so review reasons still explain fallback usage.

Scheduling remains capacity-aware and can split one work item across multiple available intervals. Instead of returning only `plannedStart` and `plannedEnd`, `GreedySchedulingPolicy` also returns allocation chunks. Each chunk stores `assigneeId`, `start`, `end`, and `effortMillis`. The first chunk start and last chunk end remain the summary dates shown in existing fields.

Generated run items persist allocation chunks as JSON on `optimization_run_items` to avoid a broader workflow migration. Review responses deserialize that JSON so the UI can show, for example, a 300-minute task as separate work blocks across several days.

## Boundaries

- The calendar adapter only reads availability slots; it does not create or manage user calendars.
- Existing `work_item_plans` remain summary plans with one start/end pair.
- Existing workload deduction still subtracts committed plans and unplanned assigned work from the calendar capacity.
- Fallback calendar remains 09:00-17:00 UTC+7 on weekdays.

## Testing

- Unit test the real calendar adapter for full real coverage and partial fallback coverage.
- Unit test scheduling chunks for a task split across multiple slots.
- Unit test generation/review mapping so allocation chunks are persisted and returned in the review view.
