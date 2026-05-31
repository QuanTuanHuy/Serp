# PM Project Calendar Deadline And Schedule Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reuse existing work item search for deadline calendar mode, add a schedule allocation API, then refactor the frontend calendar to render deadline items and allocation chunks with clear click behavior.

**Architecture:** Keep timeline APIs for work-item-centric screens. Deadline mode uses `SearchWorkItemsQueryHandler` with due-date filters. Schedule mode uses a calendar read model backed by allocation queries. The frontend uses the existing search RTK Query hook for deadline mode, a schedule-calendar hook for schedule mode, and a custom week/month grid instead of a time-grid calendar view.

**Tech Stack:** Java 21, Spring Boot 3.5, JDBC read adapter, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query.

---

### Task 1: Backend Calendar Read Contracts

**Files:**

- Create: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemScheduleCalendarCriteria.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemScheduleAllocationCalendarProjection.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/port/read/IWorkItemReadPort.java`

- [ ] Add schedule criteria with viewport, filters, keyword, and pagination.
- [ ] Add schedule projection shaped around one plan allocation row.
- [ ] Add read-port method for the schedule allocation calendar query.

### Task 2: Backend Calendar Query Handlers

**Files:**

- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/calendar/ListWorkItemScheduleCalendarQuery.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/calendar/ListWorkItemScheduleCalendarQueryHandler.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/calendar/WorkItemScheduleCalendarPageView.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/calendar/WorkItemScheduleAllocationCalendarItemView.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/workitem/query/calendar/ListWorkItemScheduleCalendarQueryHandlerTest.java`

- [ ] Write failing handler tests for permission checks and response mapping.
- [ ] Reuse the project browse permission flow from timeline query handling.
- [ ] Map projections into page views.
- [ ] Return page metadata using the existing page view convention.
- [ ] Run focused calendar query handler tests.

### Task 3: Backend SQL And REST Routes

**Files:**

- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/WorkItemScheduleAllocationCalendarRowMapper.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemCalendarController.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapterTest.java`

- [ ] Write failing read-adapter tests for schedule allocation overlap filtering.
- [ ] Write failing read-adapter test that schedule assignee filtering uses allocation assignee.
- [ ] Implement schedule SQL from `work_item_plan_allocations` joined to active `work_item_plans` and `work_items`.
- [ ] Add REST route: `GET /projects/{projectId}/calendar/schedule-allocations`
- [ ] Run `mvnw.cmd -Dtest=WorkItemReadAdapterTest test` from `pm_core`.

### Task 4: Frontend API Types And Hooks

**Files:**

- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/queryParams.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`

- [ ] Add schedule allocation calendar request and response types.
- [ ] Add query param builders for common calendar filters.
- [ ] Add `useGetPmWorkItemScheduleCalendarQuery`.
- [ ] Use `useSearchPmWorkItemsQuery` for deadline mode with `dueDateFrom`, `dueDateTo`, `enriched=true`, and due-date sorting.
- [ ] Keep timeline types and hooks intact.
- [ ] Run `npm run type-check` from `serp_web` when frontend tasks are complete.

### Task 5: Calendar Page Refactor

**Files:**

- Modify: `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`
- Create: `serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarGrid.tsx`
- Create: `serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarFilters.tsx`
- Create: `serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarChips.tsx`
- Create: `serp_web/src/modules/pm/components/projects/calendar/pmProjectCalendar.utils.ts`
- Remove or stop using: `serp_web/src/modules/pm/components/projects/project-calendar.css`

- [ ] Replace `react-big-calendar` time-grid usage with a custom week/month day grid.
- [ ] Remove day view from the view selector.
- [ ] Add mode switch for `Schedule` and `Deadline`.
- [ ] For Deadline mode, fetch the existing search API and group items by `dueDate`.
- [ ] For Schedule mode, fetch schedule allocation API and group chunks by allocation `start`.
- [ ] Add filters for assignee, issue type, and status using the list/board filter pattern.
- [ ] Keep search keyword wired to both APIs.
- [ ] Ensure week/month cells do not show hourly borders.

### Task 6: Calendar Click Behavior

**Files:**

- Modify: `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`
- Create: `serp_web/src/modules/pm/components/projects/calendar/PMProjectScheduleAllocationSheet.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemScheduleSection.tsx`
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`

- [ ] Deadline chip click opens `PMWorkItemDetailDialog`.
- [ ] Schedule allocation chip click opens allocation detail sheet or popover.
- [ ] Allocation detail shows work item, assignee, start, end, effort, source, run id, and source run item id.
- [ ] Allocation detail includes `Open work item` action.
- [ ] Add schedule allocation chunks to work item detail types if not already present.
- [ ] Render allocation chunks in `PMWorkItemScheduleSection`.

### Task 7: Verification

**Backend:**

- [ ] Run focused calendar backend tests.
- [ ] Run `mvnw.cmd clean compile` from `pm_core`.

**Frontend:**

- [ ] Run `npm run type-check` from `serp_web`.
- [ ] Run `npm run lint` from `serp_web`.
- [ ] Run `npm run format:check` from `serp_web`.
- [ ] Manually verify:
  - Deadline week/month grouping.
  - Schedule week/month grouping.
  - Split work item appears as multiple schedule blocks.
  - Deadline click opens work item detail.
  - Schedule click opens allocation detail.
  - Allocation detail can open work item detail.

### Open Questions

1. Should week view show five work days only or all seven days?
2. Should schedule allocation detail include all chunks for the same work item in the current viewport only, or fetch full plan detail?
3. Should unplanned work stay on this calendar page, or move to a separate scheduling backlog after schedule allocation APIs are introduced?
