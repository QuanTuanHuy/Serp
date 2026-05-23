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
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { cn } from '@/shared/utils';
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Filter,
  Search,
} from 'lucide-react';
import moment from 'moment';
import { useParams } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';
import {
  Calendar,
  momentLocalizer,
  Views,
  type EventProps,
  type View,
} from 'react-big-calendar';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import { useGetPmWorkItemTimelineQuery } from '../api/workItemApi';
import '../components/projects/project-calendar.css';
import type { PMWorkItemTimelineItemApi } from '../types/api';

const localizer = momentLocalizer(moment);
const DEFAULT_PAGE_SIZE = 200;

type PMProjectCalendarEvent = {
  id: number;
  title: string;
  start: Date;
  end: Date;
  allDay: boolean;
  resource: PMWorkItemTimelineItemApi;
};

function toDateOrNull(value?: number | null) {
  if (typeof value !== 'number') return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function getEventRange(item: PMWorkItemTimelineItemApi) {
  const start = toDateOrNull(item.startDate);
  const due = toDateOrNull(item.dueDate);

  if (start && due && start.getTime() <= due.getTime()) {
    return { start, end: due, allDay: false };
  }

  if (start) {
    const end = new Date(start.getTime() + 60 * 60 * 1000);
    return { start, end, allDay: false };
  }

  if (due) {
    return { start: due, end: due, allDay: true };
  }

  const now = new Date();
  return { start: now, end: now, allDay: true };
}

function mapTimelineItemToEvent(
  item: PMWorkItemTimelineItemApi
): PMProjectCalendarEvent {
  const range = getEventRange(item);
  return {
    id: item.id,
    title: `${item.key} ${item.summary}`,
    start: range.start,
    end: range.end,
    allDay: range.allDay,
    resource: item,
  };
}

function getEventTone(item: PMWorkItemTimelineItemApi) {
  if (item.isUnscheduled) return 'border-l-slate-500 bg-slate-500/10';
  if (item.priority?.color) return 'border-l-primary bg-primary/10';
  if (item.status?.id) return 'border-l-blue-500 bg-blue-500/10';
  return 'border-l-muted-foreground bg-muted/60';
}

function CalendarEventCard({ event }: EventProps<PMProjectCalendarEvent>) {
  const resource = event.resource;

  return (
    <div
      className={cn(
        'h-full rounded-md border-l-4 px-2 py-1 text-xs shadow-sm',
        getEventTone(resource)
      )}
    >
      <div className='flex items-center gap-2'>
        <span className='font-semibold'>{resource.key}</span>
        {resource.isUnscheduled ? (
          <Badge variant='secondary' className='h-5 px-1.5 text-[10px]'>
            Unscheduled
          </Badge>
        ) : null}
      </div>
      <div className='line-clamp-2 text-muted-foreground'>
        {resource.summary}
      </div>
    </div>
  );
}

export function PMProjectCalendarPage() {
  const params = useParams<{ projectId?: string | string[] }>();
  const projectId = Array.isArray(params.projectId)
    ? params.projectId[0]
    : params.projectId;
  const [view, setView] = useState<View>(Views.MONTH);
  const [date, setDate] = useState(new Date());
  const [keyword, setKeyword] = useState('');

  const viewport = useMemo(() => {
    const rangeUnit =
      view === Views.MONTH ? 'month' : view === Views.DAY ? 'day' : 'week';
    const start = moment(date).startOf(rangeUnit);
    const end = moment(date).endOf(rangeUnit);

    if (view === Views.MONTH) {
      start.subtract(7, 'days');
      end.add(7, 'days');
    }

    return {
      viewportStart: start.valueOf(),
      viewportEnd: end.valueOf(),
      includeUnscheduled: true,
      includeDependencies: false,
      page: 0,
      pageSize: DEFAULT_PAGE_SIZE,
      keyword: keyword.trim() || undefined,
    };
  }, [date, keyword, view]);

  const {
    data: response,
    isLoading,
    isFetching,
    error,
  } = useGetPmWorkItemTimelineQuery(
    {
      projectId: Number(projectId),
      params: viewport,
    },
    {
      skip: !projectId || Number.isNaN(Number(projectId)),
    }
  );

  const calendarEvents = useMemo(
    () =>
      (response?.items || [])
        .filter((item) => !item.isUnscheduled)
        .map(mapTimelineItemToEvent),
    [response]
  );

  const unscheduledItems = useMemo(
    () => (response?.items || []).filter((item) => item.isUnscheduled),
    [response]
  );

  useEffect(() => {
    if (!projectId || Number.isNaN(Number(projectId))) return;
  }, [projectId]);

  const activeDateLabel = useMemo(() => {
    if (view === Views.MONTH) return moment(date).format('MMMM YYYY');
    if (view === Views.WEEK)
      return moment(date).format('[Week of] MMM D, YYYY');
    return moment(date).format('ddd, MMM D, YYYY');
  }, [date, view]);

  const eventStyleGetter = (event: PMProjectCalendarEvent) => ({
    className: cn(
      'overflow-hidden border-transparent text-foreground',
      event.resource.isUnscheduled && 'opacity-80'
    ),
    style: {
      backgroundColor: 'transparent',
    },
  });

  const emptyState =
    !isLoading &&
    !isFetching &&
    calendarEvents.length === 0 &&
    unscheduledItems.length === 0;

  return (
    <div className='space-y-4'>
      <div className='flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between'>
        <div className='space-y-2'>
          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <CalendarDays className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>Calendar</h1>
              <p className='text-sm text-muted-foreground'>
                Track scheduled work, scan unscheduled items, and move tasks by
                date.
              </p>
            </div>
          </div>
          <div className='flex flex-wrap items-center gap-2 text-sm text-muted-foreground'>
            <span className='font-medium text-foreground'>
              {activeDateLabel}
            </span>
            <span>•</span>
            <span>{response?.totalItems ?? 0} items</span>
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
          <Button
            variant='outline'
            size='sm'
            onClick={() =>
              setDate(
                moment(date)
                  .subtract(
                    1,
                    view === Views.MONTH
                      ? 'month'
                      : view === Views.WEEK
                        ? 'week'
                        : 'day'
                  )
                  .toDate()
              )
            }
          >
            <ChevronLeft className='h-4 w-4' />
          </Button>
          <Button
            variant='outline'
            size='sm'
            onClick={() =>
              setDate(
                moment(date)
                  .add(
                    1,
                    view === Views.MONTH
                      ? 'month'
                      : view === Views.WEEK
                        ? 'week'
                        : 'day'
                  )
                  .toDate()
              )
            }
          >
            <ChevronRight className='h-4 w-4' />
          </Button>
          <div className='flex items-center gap-1 rounded-md border bg-background p-1'>
            {[Views.MONTH, Views.WEEK, Views.DAY].map((candidate) => (
              <Button
                key={candidate}
                type='button'
                variant={view === candidate ? 'default' : 'ghost'}
                size='sm'
                className='h-8 px-3'
                onClick={() => setView(candidate)}
              >
                {candidate}
              </Button>
            ))}
          </div>
        </div>
      </div>

      <div className='grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]'>
        <div className='space-y-4'>
          <Card>
            <CardHeader className='pb-3'>
              <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
                <CardTitle className='text-base'>Project work items</CardTitle>
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
                  <Button variant='outline' size='sm' disabled>
                    <Filter className='mr-2 h-4 w-4' />
                    Filters
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {error ? (
                <div className='rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
                  {getErrorMessage(error)}
                </div>
              ) : null}

              <div className='overflow-hidden rounded-lg border'>
                <div className='project-calendar-wrapper h-[760px]'>
                  <Calendar
                    localizer={localizer}
                    events={calendarEvents}
                    view={view}
                    date={date}
                    onView={setView}
                    onNavigate={setDate}
                    startAccessor='start'
                    endAccessor='end'
                    style={{ height: '100%' }}
                    components={{
                      toolbar: () => null,
                      event: CalendarEventCard,
                    }}
                    eventPropGetter={eventStyleGetter}
                    popup
                    selectable={false}
                    views={[Views.MONTH, Views.WEEK, Views.DAY]}
                    showMultiDayTimes
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className='xl:sticky xl:top-4 xl:h-[calc(100vh-10rem)]'>
          <CardHeader className='border-b pb-4'>
            <CardTitle className='text-lg'>Unscheduled work</CardTitle>
            <p className='text-sm text-muted-foreground'>
              Work items without schedule stay here until phase 4 drop flow.
            </p>
          </CardHeader>
          <CardContent className='p-0'>
            <div className='border-b px-4 py-3'>
              <Input placeholder='Search unscheduled items' disabled />
            </div>
            <ScrollArea className='h-[620px] xl:h-[calc(100vh-18rem)]'>
              <div className='space-y-2 p-4'>
                {isLoading ? (
                  <div className='space-y-3'>
                    <div className='h-20 animate-pulse rounded-lg bg-muted' />
                    <div className='h-20 animate-pulse rounded-lg bg-muted' />
                  </div>
                ) : unscheduledItems.length > 0 ? (
                  unscheduledItems.map((item) => (
                    <div
                      key={item.id}
                      className='rounded-lg border bg-card p-3 shadow-sm'
                    >
                      <div className='flex items-start justify-between gap-3'>
                        <div className='min-w-0 space-y-1'>
                          <div className='truncate font-medium'>{item.key}</div>
                          <div className='line-clamp-2 text-sm text-muted-foreground'>
                            {item.summary}
                          </div>
                        </div>
                        <Badge variant='secondary'>Unscheduled</Badge>
                      </div>
                      <div className='mt-2 flex flex-wrap gap-2 text-xs text-muted-foreground'>
                        {item.status?.name ? (
                          <span>{item.status.name}</span>
                        ) : null}
                        {item.priority?.name ? (
                          <span>• {item.priority.name}</span>
                        ) : null}
                        {item.issueType?.name ? (
                          <span>• {item.issueType.name}</span>
                        ) : null}
                      </div>
                    </div>
                  ))
                ) : emptyState ? (
                  <div className='rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
                    No work items in current viewport.
                  </div>
                ) : (
                  <div className='rounded-lg border border-dashed p-6 text-sm text-muted-foreground'>
                    No unscheduled work.
                  </div>
                )}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
