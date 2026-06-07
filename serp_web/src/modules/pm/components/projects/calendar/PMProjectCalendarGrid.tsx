/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project calendar grid
 */

'use client';

import { useDroppable } from '@dnd-kit/core';
import type moment from 'moment';

import { cn } from '@/shared/utils';
import type {
  PMWorkItemSearchApi,
  PMWorkItemScheduleAllocationCalendarItemApi,
} from '../../../types/api';
import {
  DeadlineCalendarChip,
  ScheduleAllocationCalendarChip,
} from './PMProjectCalendarChips';
import type {
  PMProjectCalendarMode,
  PMProjectCalendarView,
} from './pmProjectCalendar.utils';

interface PMProjectCalendarGridProps {
  days: moment.Moment[];
  mode: PMProjectCalendarMode;
  view: PMProjectCalendarView;
  showWeekends: boolean;
  deadlineItemsByDay: Map<string, PMWorkItemSearchApi[]>;
  scheduleItemsByDay: Map<
    string,
    PMWorkItemScheduleAllocationCalendarItemApi[]
  >;
  dropEnabled?: boolean;
  onDeadlineClick: (item: PMWorkItemSearchApi) => void;
  onScheduleClick: (item: PMWorkItemScheduleAllocationCalendarItemApi) => void;
}

export function PMProjectCalendarGrid({
  days,
  mode,
  view,
  showWeekends,
  deadlineItemsByDay,
  scheduleItemsByDay,
  dropEnabled,
  onDeadlineClick,
  onScheduleClick,
}: PMProjectCalendarGridProps) {
  const dayLabels = showWeekends
    ? ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];

  return (
    <div className='overflow-hidden rounded-lg border bg-background'>
      <div
        className={cn(
          'grid border-b bg-muted/20',
          showWeekends ? 'grid-cols-7' : 'grid-cols-5'
        )}
      >
        {dayLabels.map((label) => (
          <div
            key={label}
            className='border-r px-3 py-2 text-center text-xs font-semibold text-muted-foreground last:border-r-0'
          >
            {label}
          </div>
        ))}
      </div>
      <div className={cn('grid', showWeekends ? 'grid-cols-7' : 'grid-cols-5')}>
        {days.map((day) => {
          const dayKey = day.format('YYYY-MM-DD');
          const deadlineItems = deadlineItemsByDay.get(dayKey) || [];
          const scheduleItems = scheduleItemsByDay.get(dayKey) || [];
          const visibleItems =
            mode === 'deadline' ? deadlineItems : scheduleItems;

          return (
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
          );
        })}
      </div>
    </div>
  );
}

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
