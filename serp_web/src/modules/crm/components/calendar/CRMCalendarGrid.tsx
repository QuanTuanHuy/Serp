/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Calendar Grid view
 */

'use client';

import React from 'react';
import { useDroppable, useDraggable } from '@dnd-kit/core';
import type moment from 'moment';
import { Plus } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { Activity } from '../../types';
import { CRMCalendarChip } from './CRMCalendarChips';

interface CRMCalendarGridProps {
  days: moment.Moment[];
  view: 'month' | 'week';
  activitiesByDay: Map<string, Activity[]>;
  onActivityClick: (activity: Activity) => void;
  onAddActivity: (date: string) => void;
}

export const CRMCalendarGrid: React.FC<CRMCalendarGridProps> = ({
  days,
  view,
  activitiesByDay,
  onActivityClick,
  onAddActivity,
}) => {
  const dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  return (
    <div className='overflow-hidden rounded-lg border bg-background'>
      <div className='grid grid-cols-7 border-b bg-muted/20'>
        {dayLabels.map((label) => (
          <div
            key={label}
            className='border-r px-3 py-2 text-center text-xs font-semibold text-muted-foreground last:border-r-0'
          >
            {label}
          </div>
        ))}
      </div>
      <div className='grid grid-cols-7'>
        {days.map((day) => {
          const dayKey = day.format('YYYY-MM-DD');
          const items = activitiesByDay.get(dayKey) || [];
          return (
            <CalendarDayCell
              key={dayKey}
              day={day}
              dayKey={dayKey}
              view={view}
              items={items}
              onActivityClick={onActivityClick}
              onAddActivity={onAddActivity}
            />
          );
        })}
      </div>
    </div>
  );
};

const CalendarDayCell = React.memo(
  ({
    day,
    dayKey,
    view,
    items,
    onActivityClick,
    onAddActivity,
  }: {
    day: moment.Moment;
    dayKey: string;
    view: 'month' | 'week';
    items: Activity[];
    onActivityClick: (activity: Activity) => void;
    onAddActivity: (date: string) => void;
  }) => {
    const { isOver, setNodeRef } = useDroppable({
      id: `calendar-day-${dayKey}`,
      data: {
        type: 'calendar-day',
        dayKey,
      },
    });

    const isToday = day.isSame(new Date(), 'day');

    return (
      <div
        ref={setNodeRef}
        className={cn(
          'min-h-[145px] border-r border-b p-2 last:border-r-0 group/cell relative transition-colors flex flex-col',
          view === 'week' && 'min-h-[300px]',
          isToday && 'bg-primary/5',
          isOver && 'bg-primary/10 ring-2 ring-inset ring-primary/30'
        )}
      >
        <div className='mb-2 flex items-center justify-between gap-2'>
          <span
            className={cn(
              'text-xs font-medium',
              isToday ? 'text-primary font-bold' : 'text-muted-foreground'
            )}
          >
            {day.date() === 1 ? day.format('MMM D') : day.format('D')}
          </span>
          <button
            type='button'
            onClick={() => onAddActivity(dayKey)}
            className='opacity-0 group-hover/cell:opacity-100 p-0.5 rounded hover:bg-muted text-muted-foreground transition-opacity cursor-pointer'
          >
            <Plus className='h-3.5 w-3.5' />
          </button>
        </div>
        <div className='space-y-1.5 flex-1 overflow-y-auto pr-0.5 max-h-[110px]'>
          {items.map((activity) => (
            <DraggableActivityChip
              key={activity.id}
              activity={activity}
              onClick={() => onActivityClick(activity)}
            />
          ))}
        </div>
      </div>
    );
  }
);

CalendarDayCell.displayName = 'CalendarDayCell';

const DraggableActivityChip = ({
  activity,
  onClick,
}: {
  activity: Activity;
  onClick: () => void;
}) => {
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({
      id: `activity-${activity.id}`,
      data: {
        type: 'activity',
        activity,
      },
    });

  const style = transform
    ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
        zIndex: 50,
      }
    : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className={cn(isDragging && 'opacity-40 cursor-grabbing')}
    >
      <CRMCalendarChip activity={activity} onClick={onClick} />
    </div>
  );
};
