/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project calendar page
 */

'use client';

import { getErrorMessage } from '@/lib/store/api';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { cn } from '@/shared/utils';
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Filter,
  Search,
} from 'lucide-react';
import moment from 'moment';
import {
  useParams,
  usePathname,
  useRouter,
  useSearchParams,
} from 'next/navigation';
import { useMemo, useState } from 'react';
import {
  useGetPmWorkItemScheduleCalendarQuery,
  useSearchPmWorkItemsQuery,
} from '../api/workItemApi';
import { PMProjectCalendarFilters } from '../components/projects/calendar/PMProjectCalendarFilters';
import { PMProjectCalendarGrid } from '../components/projects/calendar/PMProjectCalendarGrid';
import { PMProjectScheduleAllocationSheet } from '../components/projects/calendar/PMProjectScheduleAllocationSheet';
import {
  countCalendarFilters,
  getCalendarDayKey,
  getCalendarViewport,
  getVisibleCalendarDays,
  parseNumberList,
  toVietnamMoment,
  type PMProjectCalendarMode,
  type PMProjectCalendarView,
} from '../components/projects/calendar/pmProjectCalendar.utils';
import { PMWorkItemDetailDialog } from '../components/work-items/detail';
import type {
  PMWorkItemSearchApi,
  PMWorkItemScheduleAllocationCalendarItemApi,
} from '../types/api';

const DEFAULT_PAGE_SIZE = 1000;

export function PMProjectCalendarPage() {
  const params = useParams<{ projectId?: string | string[] }>();
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const projectId = Array.isArray(params.projectId)
    ? params.projectId[0]
    : params.projectId;
  const numericProjectId = Number(projectId);

  const [calendarMode, setCalendarMode] =
    useState<PMProjectCalendarMode>('schedule');
  const [view, setView] = useState<PMProjectCalendarView>('month');
  const [date, setDate] = useState(new Date());
  const [keyword, setKeyword] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [selectedWorkItemId, setSelectedWorkItemId] = useState<number>();
  const [workItemDetailOpen, setWorkItemDetailOpen] = useState(false);
  const [selectedAllocation, setSelectedAllocation] =
    useState<PMWorkItemScheduleAllocationCalendarItemApi | null>(null);

  const assigneeIds = parseNumberList(searchParams.get('assigneeIds'));
  const issueTypeIds = parseNumberList(searchParams.get('issueTypeIds'));
  const statusIds = parseNumberList(searchParams.get('statusIds'));
  const activeFilterCount = countCalendarFilters([
    assigneeIds,
    issueTypeIds,
    statusIds,
  ]);

  const days = useMemo(() => getVisibleCalendarDays(date, view), [date, view]);
  const viewport = useMemo(() => {
    const range = getCalendarViewport(date, view);
    return {
      ...range,
      page: 0,
      pageSize: DEFAULT_PAGE_SIZE,
      keyword: keyword.trim() || undefined,
      assigneeIds,
      issueTypeIds,
      statusIds,
    };
  }, [assigneeIds, date, issueTypeIds, keyword, statusIds, view]);

  const deadlineQuery = useSearchPmWorkItemsQuery(
    {
      projectId: numericProjectId,
      params: {
        keyword: viewport.keyword,
        assigneeIds: viewport.assigneeIds,
        issueTypeIds: viewport.issueTypeIds,
        statusIds: viewport.statusIds,
        dueDateFrom: viewport.viewportStart,
        dueDateTo: viewport.viewportEnd,
        enriched: true,
        page: viewport.page,
        pageSize: viewport.pageSize,
        sortField: 'due_date',
        sortDirection: 'ASC',
      },
    },
    {
      skip:
        !numericProjectId ||
        Number.isNaN(numericProjectId) ||
        calendarMode !== 'deadline',
    }
  );
  const scheduleQuery = useGetPmWorkItemScheduleCalendarQuery(
    {
      projectId: numericProjectId,
      params: viewport,
    },
    {
      skip:
        !numericProjectId ||
        Number.isNaN(numericProjectId) ||
        calendarMode !== 'schedule',
    }
  );

  const deadlineItems = deadlineQuery.data?.data.items || [];
  const scheduleItems = scheduleQuery.data?.items || [];
  const isLoading =
    calendarMode === 'deadline'
      ? deadlineQuery.isLoading || deadlineQuery.isFetching
      : scheduleQuery.isLoading || scheduleQuery.isFetching;
  const error =
    calendarMode === 'deadline' ? deadlineQuery.error : scheduleQuery.error;

  const deadlineItemsByDay = useMemo(
    () => groupDeadlineItemsByDay(deadlineItems),
    [deadlineItems]
  );
  const scheduleItemsByDay = useMemo(
    () => groupScheduleItemsByDay(scheduleItems),
    [scheduleItems]
  );

  const activeDateLabel = useMemo(() => {
    const anchor = toVietnamMoment(date);
    if (view === 'month') return anchor.format('MMMM YYYY');
    return `${anchor.clone().startOf('isoWeek').format('MMM D')} - ${anchor
      .clone()
      .endOf('isoWeek')
      .format('MMM D, YYYY')}`;
  }, [date, view]);

  const relatedAllocations = useMemo(() => {
    if (!selectedAllocation) return [];
    return scheduleItems.filter(
      (item) =>
        item.workItemId === selectedAllocation.workItemId &&
        item.allocationId !== selectedAllocation.allocationId
    );
  }, [scheduleItems, selectedAllocation]);

  const updateFilterParams = (updates: Record<string, string | undefined>) => {
    const nextParams = new URLSearchParams(searchParams.toString());
    Object.entries(updates).forEach(([key, value]) => {
      if (value) {
        nextParams.set(key, value);
      } else {
        nextParams.delete(key);
      }
    });
    const queryString = nextParams.toString();
    router.replace(queryString ? `${pathname}?${queryString}` : pathname, {
      scroll: false,
    });
  };

  const clearFilters = () =>
    updateFilterParams({
      assigneeIds: undefined,
      issueTypeIds: undefined,
      statusIds: undefined,
    });

  const navigate = (direction: -1 | 1) => {
    setDate((current) =>
      moment(current)
        .add(direction, view === 'month' ? 'month' : 'week')
        .toDate()
    );
  };

  const openWorkItemDetail = (workItemId: number) => {
    setSelectedWorkItemId(workItemId);
    setWorkItemDetailOpen(true);
  };

  const totalItems =
    calendarMode === 'deadline'
      ? (deadlineQuery.data?.data.totalItems ?? 0)
      : (scheduleQuery.data?.totalItems ?? 0);
  const emptyState = !isLoading && !error && totalItems === 0;

  return (
    <div className='space-y-4'>
      <div className='flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between'>
        <div className='space-y-2'>
          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-lg bg-primary/10 text-primary'>
              <CalendarDays className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>Calendar</h1>
              <p className='text-sm text-muted-foreground'>
                Track deadlines and planned schedule blocks.
              </p>
            </div>
          </div>
          <div className='flex flex-wrap items-center gap-2 text-sm text-muted-foreground'>
            <span className='font-medium text-foreground'>
              {activeDateLabel}
            </span>
            <span>/</span>
            <span>{totalItems} items</span>
            <span>/</span>
            <span>UTC+7</span>
          </div>
          <div className='flex flex-wrap items-center gap-2'>
            {(['schedule', 'deadline'] as const).map((mode) => (
              <Button
                key={mode}
                type='button'
                variant={calendarMode === mode ? 'default' : 'outline'}
                size='sm'
                onClick={() => setCalendarMode(mode)}
              >
                {mode === 'schedule' ? 'Schedule' : 'Deadline'}
              </Button>
            ))}
          </div>
        </div>

        <div className='flex flex-wrap items-center gap-2'>
          <Button
            variant='outline'
            size='sm'
            onClick={() => setDate(new Date())}
          >
            Today
          </Button>
          <Button variant='outline' size='icon' onClick={() => navigate(-1)}>
            <ChevronLeft className='h-4 w-4' />
          </Button>
          <Button variant='outline' size='icon' onClick={() => navigate(1)}>
            <ChevronRight className='h-4 w-4' />
          </Button>
          <div className='flex items-center gap-1 rounded-md border bg-background p-1'>
            {(['week', 'month'] as const).map((candidate) => (
              <Button
                key={candidate}
                type='button'
                variant={view === candidate ? 'default' : 'ghost'}
                size='sm'
                className='h-8 px-3 capitalize'
                onClick={() => setView(candidate)}
              >
                {candidate}
              </Button>
            ))}
          </div>
        </div>
      </div>

      <Card>
        <CardHeader className='pb-3'>
          <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
            <CardTitle className='text-base'>
              {calendarMode === 'schedule'
                ? 'Schedule allocations'
                : 'Work item deadlines'}
            </CardTitle>
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

      {numericProjectId && !Number.isNaN(numericProjectId) ? (
        <>
          <PMProjectCalendarFilters
            projectId={numericProjectId}
            open={filterOpen}
            assigneeIds={assigneeIds}
            issueTypeIds={issueTypeIds}
            statusIds={statusIds}
            onOpenChange={setFilterOpen}
            onUpdate={updateFilterParams}
            onClear={clearFilters}
          />
          <PMProjectScheduleAllocationSheet
            allocation={selectedAllocation}
            relatedAllocations={relatedAllocations}
            open={Boolean(selectedAllocation)}
            onOpenChange={(open) => {
              if (!open) setSelectedAllocation(null);
            }}
            onOpenWorkItem={(workItemId) => {
              setSelectedAllocation(null);
              openWorkItemDetail(workItemId);
            }}
          />
          <PMWorkItemDetailDialog
            projectId={numericProjectId}
            workItemId={selectedWorkItemId}
            open={workItemDetailOpen}
            onOpenChange={setWorkItemDetailOpen}
          />
        </>
      ) : null}
    </div>
  );
}

function groupDeadlineItemsByDay(items: PMWorkItemSearchApi[]) {
  const grouped = new Map<string, PMWorkItemSearchApi[]>();
  items.forEach((item) => {
    const key = getCalendarDayKey(item.dueDate);
    if (!key) return;
    grouped.set(key, [...(grouped.get(key) || []), item]);
  });
  return grouped;
}

function groupScheduleItemsByDay(
  items: PMWorkItemScheduleAllocationCalendarItemApi[]
) {
  const grouped = new Map<
    string,
    PMWorkItemScheduleAllocationCalendarItemApi[]
  >();
  items.forEach((item) => {
    const key = getCalendarDayKey(item.start);
    if (!key) return;
    grouped.set(key, [...(grouped.get(key) || []), item]);
  });
  return grouped;
}
