# PM Work Item Plan Governance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose active work item plans in work item detail and calendar so the UI can show the real scheduled start/end while keeping optimization as the only current plan-writing path.

**Architecture:** Add a small schedule summary contract to work item detail and timeline responses. The backend remains the source of truth for `WorkItemPlanEntity`; the frontend reads plan data and renders schedule-first detail and calendar views while keeping `dueDate` as a separate business deadline.

**Tech Stack:** Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS 4, lucide-react.

---

### Task 1: Add active plan snapshots to work item detail and timeline contracts

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/get/WorkItemDetailView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/get/GetWorkItemByIdQueryHandler.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemDetailProjection.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/WorkItemTimelineItemView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/ListWorkItemTimelineQueryHandler.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemTimelineItemProjection.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IWorkItemRepository.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/workitem/query/get/GetWorkItemByIdQueryHandlerTest.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/workitem/query/timeline/ListWorkItemTimelineQueryHandlerTest.java`

- [ ] **Step 1: Write a failing detail test for plan visibility**

Add a unit test that stubs `IWorkItemPlanPort.getActivePlanByWorkItemId(...)` and asserts the returned work item detail includes a schedule summary with:

```java
assertEquals(1_700_000_000_000L, result.schedule().plannedStart());
assertEquals(1_710_000_000_000L, result.schedule().plannedEnd());
assertEquals(WorkItemPlanSource.OPTIMIZATION, result.schedule().source());
assertEquals(Boolean.FALSE, result.schedule().locked());
```

- [ ] **Step 2: Make the detail query pass with a minimal schedule summary**

Add a nested `ScheduleSummaryView` to `WorkItemDetailView` and populate it from `GetWorkItemByIdQueryHandler` using `IWorkItemPlanPort`.

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScheduleSummaryView(
        Long plannedStart,
        Long plannedEnd,
        String source,
        Boolean locked,
        Long sourceRunId
) {
}
```

- [ ] **Step 3: Write a failing timeline test for plan visibility**

Add a unit test that stubs timeline projections with plan-backed schedule fields and asserts the timeline item view exposes them for calendar rendering.

```java
assertEquals(1_700_000_000_000L, result.items().getFirst().plannedStart());
assertEquals(1_710_000_000_000L, result.items().getFirst().plannedEnd());
assertEquals("OPTIMIZATION", result.items().getFirst().planSource());
```

- [ ] **Step 4: Make the timeline query pass with plan-backed fields**

Extend the timeline projection/view to include plan start/end/source/locked/sourceRunId and join active plans in `WorkItemReadAdapter.listTimelineWorkItems(...)`.

```java
LEFT JOIN work_item_plans wp
  ON wp.work_item_id = w.id
 AND wp.tenant_id = w.tenant_id
 AND wp.deleted_at IS NULL
```

- [ ] **Step 5: Run backend verification**

Run:

```bash
mvn -Dtest=GetWorkItemByIdQueryHandlerTest,ListWorkItemTimelineQueryHandlerTest test
```

Expected: both tests pass with the new schedule fields exposed.

---

### Task 2: Teach the PM frontend about work item plan data

**Files:**
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/pmWorkItemDetail.types.ts`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`
- Create: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemScheduleSection.tsx`

- [ ] **Step 1: Extend frontend types with a schedule summary**

Add a plan summary type to the work item contracts and surface it on detail and timeline items.

```ts
export interface PMWorkItemPlanSummaryApi {
  plannedStart?: number | null;
  plannedEnd?: number | null;
  source?: string | null;
  locked?: boolean | null;
  sourceRunId?: number | null;
}
```

- [ ] **Step 2: Render schedule in work item detail**

Add a dedicated schedule section that shows:

```tsx
<DetailField label='Schedule'>
  {item.schedule ? (
    <div className='space-y-1'>
      <div>{formatDate(item.schedule.plannedStart)} -> {formatDate(item.schedule.plannedEnd)}</div>
      <div className='text-xs text-muted-foreground'>
        {item.schedule.source || 'UNKNOWN'} {item.schedule.locked ? '· Locked' : ''}
      </div>
    </div>
  ) : (
    <span>Unscheduled</span>
  )}
</DetailField>
```

- [ ] **Step 3: Verify the work item detail dialog still compiles**

Run:

```bash
npm run type-check
```

Expected: the detail dialog compiles with the new schedule fields.

---

### Task 3: Make the calendar schedule-first with deadline as a secondary mode

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`

- [ ] **Step 1: Switch event range calculation to planned dates when available**

Use `plannedStart/plannedEnd` as the primary range for rendered events.

```tsx
function getEventRange(item: PMWorkItemTimelineItemApi) {
  const plannedStart = toDateOrNull(item.plannedStart);
  const plannedEnd = toDateOrNull(item.plannedEnd);

  if (plannedStart && plannedEnd && plannedStart.getTime() <= plannedEnd.getTime()) {
    return { start: plannedStart, end: plannedEnd, allDay: false };
  }

  return fallbackDeadlineRange(item);
}
```

- [ ] **Step 2: Add a deadline mode for due-date scanning**

Render a simple mode toggle so the user can switch between:

```tsx
const [calendarMode, setCalendarMode] = useState<'schedule' | 'deadline'>('schedule');
```

In deadline mode, `dueDate` drives the range; in schedule mode, plan dates drive the range.

- [ ] **Step 3: Keep unscheduled items separate**

If an item has no active plan, keep it in the unscheduled list instead of fabricating a time block from `dueDate`.

- [ ] **Step 4: Run frontend verification**

Run:

```bash
npm run lint
npm run type-check
```

Expected: the calendar compiles and uses plan-backed event timing.

---

### Task 4: Final verification and commit

**Files:**
- Modify: any touched backend/frontend files from Tasks 1-3

- [ ] **Step 1: Run the narrow backend and frontend checks**

Run:

```bash
mvn -Dtest=GetWorkItemByIdQueryHandlerTest,ListWorkItemTimelineQueryHandlerTest test
npm run type-check
npm run lint
```

- [ ] **Step 2: Commit the implementation**

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/workitem/query/get/WorkItemDetailView.java pm_core/src/main/java/serp/project/pmcore/application/workitem/query/get/GetWorkItemByIdQueryHandler.java pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemDetailProjection.java pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/WorkItemTimelineItemView.java pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/ListWorkItemTimelineQueryHandler.java pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemTimelineItemProjection.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IWorkItemRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemReadAdapter.java serp_web/src/modules/pm/types/work-item-api.types.ts serp_web/src/modules/pm/api/workItemApi.ts serp_web/src/modules/pm/components/work-items/detail/pmWorkItemDetail.types.ts serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx serp_web/src/modules/pm/components/work-items/detail/PMWorkItemScheduleSection.tsx serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx
git commit -m "feat(pm): expose work item schedule data"
```

## Coverage Check

This plan covers:

- work item detail showing when a work item will start
- calendar using `WorkItemPlanEntity` as the primary source
- due date remaining visible but secondary
- optimization remaining the only current plan-writing path

