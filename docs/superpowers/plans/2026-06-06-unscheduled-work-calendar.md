# Unscheduled Work Calendar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an unscheduled-work side panel to the PM project calendar and let users create manual schedule plans by dragging unscheduled work items onto calendar days.

**Architecture:** Backend search gets a nullable `hasActivePlan` filter implemented in `WorkItemQueryBuilder` with an active-plan `EXISTS`/`NOT EXISTS` predicate. Frontend reuses the search endpoint for unscheduled items and the existing schedule update mutation to create `WorkItemPlanEntity` plus one allocation after a successful calendar drop. Calendar drag/drop uses the existing `@dnd-kit/core` dependency already used by the PM board.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, Tailwind, `@dnd-kit/core`, Shadcn/Radix primitives.

---

## File Map

- Modify `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemSearchCriteria.java`
  - Add nullable `Boolean hasActivePlan`.
- Modify `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/query/WorkItemQueryBuilder.java`
  - Add active-plan predicate to generated search SQL.
- Create `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/query/WorkItemQueryBuilderTest.java`
  - Test `hasActivePlan=false`, `hasActivePlan=true`, and null/default behavior.
- Modify `serp_web/src/modules/pm/types/work-item-api.types.ts`
  - Add `hasActivePlan?: boolean` to `PMSearchWorkItemsParams`.
- Modify `serp_web/src/modules/pm/api/queryParams.ts`
  - Send `hasActivePlan` when provided.
- Modify `serp_web/src/modules/pm/components/projects/calendar/pmProjectCalendar.utils.ts`
  - Add constants/helpers for unscheduled drag data and default dropped schedule range.
- Create `serp_web/src/modules/pm/components/projects/calendar/PMProjectUnscheduledWorkPanel.tsx`
  - Render right-side panel and draggable unscheduled work rows.
- Modify `serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarGrid.tsx`
  - Make schedule-mode day cells droppable.
- Modify `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`
  - Load unscheduled work, wrap schedule view in `DndContext`, handle drop, call existing update mutation, and refetch.

---

### Task 1: Backend Search Filter

**Files:**
- Create: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/query/WorkItemQueryBuilderTest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemSearchCriteria.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/query/WorkItemQueryBuilder.java`

- [ ] **Step 1: Write the failing query builder tests**

Create `WorkItemQueryBuilderTest.java` with these tests:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkItemQueryBuilderTest {

    private WorkItemQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        queryBuilder = new WorkItemQueryBuilder(new BaseQueryBuilder());
    }

    @Test
    void buildShouldFilterWorkItemsWithoutActivePlan() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(10L)
                .hasActivePlan(false)
                .build();

        QueryResult result = queryBuilder.build(1L, criteria);
        String dataSql = normalizeSql(result.dataSql());
        String countSql = normalizeSql(result.countSql());

        assertTrue(dataSql.contains("NOT EXISTS (SELECT 1 FROM work_item_plans plan"));
        assertTrue(dataSql.contains("plan.tenant_id = w.tenant_id"));
        assertTrue(dataSql.contains("plan.project_id = w.project_id"));
        assertTrue(dataSql.contains("plan.work_item_id = w.id"));
        assertTrue(dataSql.contains("plan.deleted_at IS NULL"));
        assertTrue(countSql.contains("NOT EXISTS (SELECT 1 FROM work_item_plans plan"));
    }

    @Test
    void buildShouldFilterWorkItemsWithActivePlan() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(10L)
                .hasActivePlan(true)
                .build();

        QueryResult result = queryBuilder.build(1L, criteria);
        String dataSql = normalizeSql(result.dataSql());

        assertTrue(dataSql.contains("EXISTS (SELECT 1 FROM work_item_plans plan"));
        assertFalse(dataSql.contains("NOT EXISTS (SELECT 1 FROM work_item_plans plan"));
    }

    @Test
    void buildShouldPreserveDefaultSearchWhenActivePlanFilterIsNull() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(10L)
                .build();

        QueryResult result = queryBuilder.build(1L, criteria);
        String dataSql = normalizeSql(result.dataSql());
        String countSql = normalizeSql(result.countSql());

        assertFalse(dataSql.contains("work_item_plans plan"));
        assertFalse(countSql.contains("work_item_plans plan"));
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
```

- [ ] **Step 2: Run tests to verify RED**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=WorkItemQueryBuilderTest test
```

Expected: compile failure because `WorkItemSearchCriteria` does not have `hasActivePlan`.

- [ ] **Step 3: Add the nullable search criterion**

In `WorkItemSearchCriteria.java`, add the field near other boolean filters:

```java
private Boolean hasActivePlan;
```

- [ ] **Step 4: Add the query builder predicate**

In `WorkItemQueryBuilder.build(...)`, after `hasTimeLogged` handling and before sort creation, call:

```java
appendActivePlanFilter(where, f.getHasActivePlan());
```

Add this private method before the closing brace:

```java
private void appendActivePlanFilter(StringBuilder where, Boolean hasActivePlan) {
    if (hasActivePlan == null) {
        return;
    }
    where.append(Boolean.TRUE.equals(hasActivePlan) ? " AND EXISTS (" : " AND NOT EXISTS (")
            .append("""
                    SELECT 1 FROM work_item_plans plan
                    WHERE plan.tenant_id = w.tenant_id
                      AND plan.project_id = w.project_id
                      AND plan.work_item_id = w.id
                      AND plan.deleted_at IS NULL
                    """)
            .append(")");
}
```

- [ ] **Step 5: Run tests to verify GREEN**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=WorkItemQueryBuilderTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit backend search filter**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/workitem/dto/WorkItemSearchCriteria.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/query/WorkItemQueryBuilder.java pm_core/src/test/java/serp/project/pmcore/infrastructure/store/query/WorkItemQueryBuilderTest.java
git commit -m "feat: filter work items by active plan"
```

---

### Task 2: Frontend API Contract

**Files:**
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/queryParams.ts`

- [ ] **Step 1: Add `hasActivePlan` to search params type**

In `PMSearchWorkItemsParams`, add:

```ts
hasActivePlan?: boolean;
```

- [ ] **Step 2: Send the query parameter**

In `buildWorkItemSearchParams`, add the boolean parameter near the existing boolean filters:

```ts
...optionalBoolean('hasActivePlan', params?.hasActivePlan),
```

- [ ] **Step 3: Verify TypeScript still parses the API contract**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: no TypeScript error caused by the new parameter. Existing unrelated errors, if any, must be recorded before continuing.

- [ ] **Step 4: Commit frontend API contract**

```bash
git add serp_web/src/modules/pm/types/work-item-api.types.ts serp_web/src/modules/pm/api/queryParams.ts
git commit -m "feat: expose active plan work item filter"
```

---

### Task 3: Calendar Drop Helpers

**Files:**
- Modify: `serp_web/src/modules/pm/components/projects/calendar/pmProjectCalendar.utils.ts`

- [ ] **Step 1: Add drag data types and schedule constants**

Add these exports near existing calendar types:

```ts
export const DEFAULT_SCHEDULE_EFFORT_MILLIS = 8 * 60 * 60 * 1000;

export type PMProjectCalendarDragData =
  | {
      type: 'unscheduled-work-item';
      workItemId: number;
    }
  | {
      type: 'calendar-day';
      dayStart: number;
    };
```

- [ ] **Step 2: Add parser and default schedule helpers**

Add these functions after `toVietnamMoment`:

```ts
export function getProjectCalendarDragData(
  value: unknown
): PMProjectCalendarDragData | undefined {
  if (!value || typeof value !== 'object' || !('type' in value)) {
    return undefined;
  }

  const data = value as PMProjectCalendarDragData;
  if (
    data.type === 'unscheduled-work-item' &&
    typeof data.workItemId === 'number'
  ) {
    return data;
  }
  if (data.type === 'calendar-day' && typeof data.dayStart === 'number') {
    return data;
  }
  return undefined;
}

export function getDefaultDroppedScheduleRange(dayStart: number) {
  const start = toVietnamMoment(dayStart)
    .startOf('day')
    .hour(9)
    .minute(0)
    .second(0)
    .millisecond(0);
  const end = start.clone().hour(17);

  return {
    plannedStart: start.valueOf(),
    plannedEnd: end.valueOf(),
  };
}

export function getDefaultDroppedScheduleEffort(
  timeRemainingEstimate?: number | null,
  timeOriginalEstimate?: number | null
) {
  if (typeof timeRemainingEstimate === 'number' && timeRemainingEstimate > 0) {
    return timeRemainingEstimate;
  }
  if (typeof timeOriginalEstimate === 'number' && timeOriginalEstimate > 0) {
    return timeOriginalEstimate;
  }
  return DEFAULT_SCHEDULE_EFFORT_MILLIS;
}
```

- [ ] **Step 3: Verify utility type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: no new errors from `pmProjectCalendar.utils.ts`.

- [ ] **Step 4: Commit calendar helpers**

```bash
git add serp_web/src/modules/pm/components/projects/calendar/pmProjectCalendar.utils.ts
git commit -m "feat: add calendar drop scheduling helpers"
```

---

### Task 4: Unscheduled Work Panel

**Files:**
- Create: `serp_web/src/modules/pm/components/projects/calendar/PMProjectUnscheduledWorkPanel.tsx`

- [ ] **Step 1: Create the panel component**

Create `PMProjectUnscheduledWorkPanel.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project unscheduled work panel
 */

'use client';

import { useDraggable } from '@dnd-kit/core';
import { GripVertical, ListFilter, SearchX } from 'lucide-react';
import { Badge, ScrollArea, Skeleton } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMWorkItemSearchApi } from '../../../types/api';

interface PMProjectUnscheduledWorkPanelProps {
  items: PMWorkItemSearchApi[];
  totalItems: number;
  isLoading: boolean;
  isFetching: boolean;
  activeWorkItemId?: number;
  onOpenWorkItem: (workItemId: number) => void;
}

export function PMProjectUnscheduledWorkPanel({
  items,
  totalItems,
  isLoading,
  isFetching,
  activeWorkItemId,
  onOpenWorkItem,
}: PMProjectUnscheduledWorkPanelProps) {
  return (
    <aside className='rounded-lg border bg-card text-card-foreground'>
      <div className='border-b p-4'>
        <div className='flex items-center justify-between gap-3'>
          <div>
            <h2 className='text-base font-semibold'>Unscheduled work</h2>
            <p className='mt-1 text-sm text-muted-foreground'>
              Drag work onto the calendar to set its schedule.
            </p>
          </div>
          <Badge variant='secondary'>{totalItems}</Badge>
        </div>
      </div>

      <div className='flex items-center gap-2 border-b px-4 py-3 text-xs font-medium text-muted-foreground'>
        <ListFilter className='h-4 w-4' />
        Current calendar filters apply
        {isFetching && !isLoading ? <span className='ml-auto'>Refreshing</span> : null}
      </div>

      <ScrollArea className='h-[560px]'>
        <div className='space-y-2 p-3'>
          {isLoading ? (
            Array.from({ length: 5 }).map((_, index) => (
              <Skeleton key={index} className='h-[74px] rounded-md' />
            ))
          ) : items.length ? (
            items.map((item) => (
              <UnscheduledWorkItemRow
                key={item.id}
                item={item}
                isDragging={activeWorkItemId === item.id}
                onOpenWorkItem={onOpenWorkItem}
              />
            ))
          ) : (
            <div className='flex min-h-[220px] flex-col items-center justify-center rounded-md border border-dashed px-4 text-center text-sm text-muted-foreground'>
              <SearchX className='mb-2 h-5 w-5' />
              No unscheduled work matches current filters.
            </div>
          )}
        </div>
      </ScrollArea>
    </aside>
  );
}

function UnscheduledWorkItemRow({
  item,
  isDragging,
  onOpenWorkItem,
}: {
  item: PMWorkItemSearchApi;
  isDragging: boolean;
  onOpenWorkItem: (workItemId: number) => void;
}) {
  const { attributes, listeners, setNodeRef, transform } = useDraggable({
    id: `unscheduled-work-item-${item.id}`,
    data: {
      type: 'unscheduled-work-item',
      workItemId: item.id,
    },
  });

  const style = transform
    ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
      }
    : undefined;

  return (
    <button
      ref={setNodeRef}
      type='button'
      style={style}
      className={cn(
        'w-full rounded-md border bg-background p-3 text-left shadow-sm transition-colors hover:bg-muted/40',
        isDragging && 'opacity-50'
      )}
      onClick={() => onOpenWorkItem(item.id)}
      {...listeners}
      {...attributes}
    >
      <div className='flex min-w-0 items-start gap-2'>
        <GripVertical className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
        <div className='min-w-0 flex-1'>
          <div className='flex min-w-0 items-center gap-2'>
            <span className='shrink-0 text-xs font-semibold text-primary'>
              {item.key}
            </span>
            <span className='truncate text-sm font-medium'>{item.summary}</span>
          </div>
          <div className='mt-2 flex flex-wrap items-center gap-1.5 text-[11px] text-muted-foreground'>
            {item.statusName ? <Badge variant='outline'>{item.statusName}</Badge> : null}
            {item.assigneeName ? <span className='truncate'>{item.assigneeName}</span> : null}
            {!item.assigneeId ? <span>Unassigned</span> : null}
          </div>
        </div>
      </div>
    </button>
  );
}
```

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: no new errors from the new component.

- [ ] **Step 3: Commit panel component**

```bash
git add serp_web/src/modules/pm/components/projects/calendar/PMProjectUnscheduledWorkPanel.tsx
git commit -m "feat: add unscheduled work panel"
```

---

### Task 5: Droppable Calendar Grid

**Files:**
- Modify: `serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarGrid.tsx`

- [ ] **Step 1: Add droppable support**

Import `useDroppable` and refactor the day cell into a component:

```tsx
import { useDroppable } from '@dnd-kit/core';
```

Add prop:

```ts
dropEnabled?: boolean;
```

Replace the day cell `div` inside `days.map(...)` with:

```tsx
<CalendarDayCell
  key={dayKey}
  day={day}
  dayKey={dayKey}
  days={days}
  mode={mode}
  view={view}
  dropEnabled={Boolean(dropEnabled && mode === 'schedule')}
  deadlineItems={deadlineItems}
  scheduleItems={scheduleItems}
  visibleItemCount={visibleItems.length}
  onDeadlineClick={onDeadlineClick}
  onScheduleClick={onScheduleClick}
/>
```

Add this component below `PMProjectCalendarGrid`:

```tsx
function CalendarDayCell({
  day,
  dayKey,
  days,
  mode,
  view,
  dropEnabled,
  deadlineItems,
  scheduleItems,
  visibleItemCount,
  onDeadlineClick,
  onScheduleClick,
}: {
  day: moment.Moment;
  dayKey: string;
  days: moment.Moment[];
  mode: PMProjectCalendarMode;
  view: PMProjectCalendarView;
  dropEnabled: boolean;
  deadlineItems: PMWorkItemSearchApi[];
  scheduleItems: PMWorkItemScheduleAllocationCalendarItemApi[];
  visibleItemCount: number;
  onDeadlineClick: (item: PMWorkItemSearchApi) => void;
  onScheduleClick: (item: PMWorkItemScheduleAllocationCalendarItemApi) => void;
}) {
  const { isOver, setNodeRef } = useDroppable({
    id: `calendar-day-${dayKey}`,
    data: {
      type: 'calendar-day',
      dayStart: day.clone().startOf('day').valueOf(),
    },
    disabled: !dropEnabled,
  });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        'min-h-[145px] border-r border-b p-2 last:border-r-0',
        view === 'week' && 'min-h-[680px]',
        day.isSame(new Date(), 'day') && 'bg-primary/5',
        day.month() !== days[Math.floor(days.length / 2)]?.month() &&
          view === 'month' &&
          'bg-muted/20',
        dropEnabled && 'transition-colors',
        isOver && 'bg-primary/10 ring-2 ring-inset ring-primary/30'
      )}
    >
      <div className='mb-2 flex items-center justify-between gap-2'>
        <span className='text-xs font-medium text-muted-foreground'>
          {view === 'week'
            ? day.format('ddd D')
            : day.date() === 1
              ? day.format('MMM D')
              : day.format('D')}
        </span>
        {visibleItemCount > 0 ? (
          <span className='rounded bg-muted px-1.5 py-0.5 text-[10px] text-muted-foreground'>
            {visibleItemCount}
          </span>
        ) : null}
      </div>
      <div className='space-y-1.5'>
        {mode === 'deadline'
          ? deadlineItems.map((item) => (
              <DeadlineCalendarChip
                key={item.id}
                item={item}
                onClick={() => onDeadlineClick(item)}
              />
            ))
          : scheduleItems.map((item) => (
              <ScheduleAllocationCalendarChip
                key={item.allocationId}
                item={item}
                onClick={() => onScheduleClick(item)}
              />
            ))}
      </div>
    </div>
  );
}
```

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: no new errors from `PMProjectCalendarGrid.tsx`.

- [ ] **Step 3: Commit droppable calendar grid**

```bash
git add serp_web/src/modules/pm/components/projects/calendar/PMProjectCalendarGrid.tsx
git commit -m "feat: make calendar days droppable"
```

---

### Task 6: Calendar Page Integration

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`

- [ ] **Step 1: Add imports**

Add:

```tsx
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core';
```

Import the new panel:

```tsx
import { PMProjectUnscheduledWorkPanel } from '../components/projects/calendar/PMProjectUnscheduledWorkPanel';
```

Import helpers:

```tsx
  getDefaultDroppedScheduleEffort,
  getDefaultDroppedScheduleRange,
  getProjectCalendarDragData,
```

- [ ] **Step 2: Add unscheduled query and drag state**

Add state and sensors near existing state:

```tsx
const [activeUnscheduledWorkItemId, setActiveUnscheduledWorkItemId] =
  useState<number>();
const sensors = useSensors(
  useSensor(PointerSensor, {
    activationConstraint: { distance: 6 },
  })
);
```

Add query after `scheduleQuery`:

```tsx
const unscheduledQuery = useSearchPmWorkItemsQuery(
  {
    projectId: numericProjectId,
    params: {
      keyword: viewport.keyword,
      assigneeIds: viewport.assigneeIds,
      issueTypeIds: viewport.issueTypeIds,
      statusIds: viewport.statusIds,
      hasActivePlan: false,
      enriched: true,
      page: 0,
      pageSize: 100,
      sortField: 'updated_at',
      sortDirection: 'DESC',
    },
  },
  {
    skip:
      !numericProjectId ||
      Number.isNaN(numericProjectId) ||
      calendarMode !== 'schedule',
  }
);
```

Add derived values:

```tsx
const unscheduledItems = unscheduledQuery.data?.data.items || [];
const activeUnscheduledWorkItem = useMemo(
  () =>
    activeUnscheduledWorkItemId
      ? unscheduledItems.find((item) => item.id === activeUnscheduledWorkItemId)
      : undefined,
  [activeUnscheduledWorkItemId, unscheduledItems]
);
```

- [ ] **Step 3: Add drag handlers**

Add handlers before `saveScheduleAllocation`:

```tsx
const handleCalendarDragStart = (event: DragStartEvent) => {
  const activeData = getProjectCalendarDragData(event.active.data.current);
  if (activeData?.type === 'unscheduled-work-item') {
    setActiveUnscheduledWorkItemId(activeData.workItemId);
  }
};

const handleCalendarDragEnd = async (event: DragEndEvent) => {
  setActiveUnscheduledWorkItemId(undefined);

  const activeData = getProjectCalendarDragData(event.active.data.current);
  const overData = getProjectCalendarDragData(event.over?.data.current);
  if (
    activeData?.type !== 'unscheduled-work-item' ||
    overData?.type !== 'calendar-day'
  ) {
    return;
  }

  const item = unscheduledItems.find(
    (candidate) => candidate.id === activeData.workItemId
  );
  if (!item) {
    toast.error('Unable to locate unscheduled work item.');
    return;
  }
  if (!item.assigneeId) {
    toast.error('Assign this work item before scheduling it.');
    return;
  }

  const { plannedStart, plannedEnd } = getDefaultDroppedScheduleRange(
    overData.dayStart
  );
  const effortMillis = getDefaultDroppedScheduleEffort(
    item.timeRemainingEstimate,
    item.timeOriginalEstimate
  );

  try {
    await updateWorkItemSchedule({
      projectId: numericProjectId,
      workItemId: item.id,
      body: {
        plannedStart,
        plannedEnd,
        locked: true,
        allocations: [
          {
            assigneeId: item.assigneeId,
            start: plannedStart,
            end: plannedEnd,
            effortMillis,
          },
        ],
      },
    }).unwrap();
    toast.success('Work item scheduled.');
    await Promise.all([scheduleQuery.refetch(), unscheduledQuery.refetch()]);
  } catch (error) {
    toast.error('Failed to schedule work item', {
      description: getErrorMessage(error),
    });
  }
};

const handleCalendarDragCancel = () => {
  setActiveUnscheduledWorkItemId(undefined);
};
```

- [ ] **Step 4: Render schedule grid and side panel in `DndContext`**

For schedule mode, wrap the existing calendar card and side panel. Move the
current calendar `<Card>` block into the left column unchanged except for
passing `dropEnabled={calendarMode === 'schedule'}` to
`PMProjectCalendarGrid`:

```tsx
<DndContext
  sensors={sensors}
  onDragCancel={handleCalendarDragCancel}
  onDragEnd={handleCalendarDragEnd}
  onDragStart={handleCalendarDragStart}
>
  <div className='grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]'>
    <div className='min-w-0'>
      <Card>
        <CardHeader className='pb-3'>
          <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
            <CardTitle className='text-base'>Schedule allocations</CardTitle>
            <div className='flex flex-1 flex-wrap items-center gap-2 lg:justify-end'>
              <div className='relative w-full max-w-sm'>
                <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  placeholder='Search calendar'
                  className='pl-9'
                />
              </div>
              <Button
                variant='outline'
                size='sm'
                className='gap-2'
                onClick={() => setFilterOpen(true)}
              >
                <Filter className='h-4 w-4' />
                Filters
                {activeFilterCount > 0 ? (
                  <Badge variant='secondary'>{activeFilterCount}</Badge>
                ) : null}
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {error ? (
            <div className='mb-4 rounded-lg border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive'>
              {getErrorMessage(error)}
            </div>
          ) : null}

          <div className={cn(isLoading && 'opacity-60')}>
            <PMProjectCalendarGrid
              days={days}
              mode={calendarMode}
              view={view}
              showWeekends={showWeekends}
              dropEnabled={calendarMode === 'schedule'}
              deadlineItemsByDay={deadlineItemsByDay}
              scheduleItemsByDay={scheduleItemsByDay}
              onDeadlineClick={(item) => openWorkItemDetail(item.id)}
              onScheduleClick={setSelectedAllocation}
            />
          </div>

          {emptyState ? (
            <div className='mt-4 rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
              No calendar items in the current viewport.
            </div>
          ) : null}
        </CardContent>
      </Card>
    </div>
    <PMProjectUnscheduledWorkPanel
      items={unscheduledItems}
      totalItems={unscheduledQuery.data?.data.totalItems ?? 0}
      isLoading={unscheduledQuery.isLoading}
      isFetching={unscheduledQuery.isFetching}
      activeWorkItemId={activeUnscheduledWorkItemId}
      onOpenWorkItem={openWorkItemDetail}
    />
  </div>
  <DragOverlay>
    {activeUnscheduledWorkItem ? (
      <div className='w-[320px] rounded-md border bg-background p-3 text-sm shadow-lg'>
        <div className='font-semibold'>{activeUnscheduledWorkItem.key}</div>
        <div className='truncate text-muted-foreground'>
          {activeUnscheduledWorkItem.summary}
        </div>
      </div>
    ) : null}
  </DragOverlay>
</DndContext>
```

Keep deadline mode rendering as a single card without the unscheduled panel. Pass `dropEnabled={calendarMode === 'schedule'}` to `PMProjectCalendarGrid`.

- [ ] **Step 5: Run frontend verification**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
npm run format:check
```

Expected: all commands pass, or only pre-existing unrelated issues are reported.

- [ ] **Step 6: Commit calendar integration**

```bash
git add serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx
git commit -m "feat: schedule unscheduled work from calendar"
```

---

### Task 7: Final Verification

**Files:**
- No new source files unless verification finds a defect.

- [ ] **Step 1: Run backend focused test**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=WorkItemQueryBuilderTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run backend compile**

Run from `pm_core/`:

```bash
./mvnw.cmd clean compile
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Run frontend quality checks**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
npm run format:check
```

Expected: all pass, or record exact pre-existing failures.

- [ ] **Step 4: Inspect git status**

Run from repo root:

```bash
git status --short
```

Expected: only intentional files changed, or a clean tree if all task commits were created.
