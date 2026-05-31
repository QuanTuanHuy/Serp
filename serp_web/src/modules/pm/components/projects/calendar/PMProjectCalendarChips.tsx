/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project calendar chips
 */

'use client';

import { Clock } from 'lucide-react';
import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  PMWorkItemSearchApi,
  PMWorkItemScheduleAllocationCalendarItemApi,
} from '../../../types/api';
import { formatCalendarTime, formatEffort } from './pmProjectCalendar.utils';

export function DeadlineCalendarChip({
  item,
  onClick,
}: {
  item: PMWorkItemSearchApi;
  onClick: () => void;
}) {
  return (
    <button
      type='button'
      onClick={onClick}
      className={cn(
        'group w-full rounded-md border border-blue-500/20 bg-blue-500/10 px-2 py-1.5 text-left text-xs shadow-sm transition-colors hover:bg-blue-500/15'
      )}
    >
      <div className='flex min-w-0 items-center gap-1.5'>
        <span className='shrink-0 font-semibold text-foreground'>
          {item.key}
        </span>
        <span className='truncate text-muted-foreground'>{item.summary}</span>
      </div>
      <div className='mt-1 flex items-center gap-1.5 text-[11px] text-muted-foreground'>
        {item.statusName ? <span>{item.statusName}</span> : null}
        {item.assigneeName ? (
          <span className='truncate'>{item.assigneeName}</span>
        ) : null}
      </div>
    </button>
  );
}

export function ScheduleAllocationCalendarChip({
  item,
  onClick,
}: {
  item: PMWorkItemScheduleAllocationCalendarItemApi;
  onClick: () => void;
}) {
  const start = formatCalendarTime(item.start);
  const end = formatCalendarTime(item.end);

  return (
    <button
      type='button'
      onClick={onClick}
      className='group w-full rounded-md border border-primary/20 bg-primary/10 px-2 py-1.5 text-left text-xs shadow-sm transition-colors hover:bg-primary/15'
    >
      <div className='flex min-w-0 items-center gap-1.5'>
        <span className='shrink-0 font-semibold text-foreground'>
          {item.key}
        </span>
        <span className='truncate text-muted-foreground'>{item.summary}</span>
      </div>
      <div className='mt-1 flex flex-wrap items-center gap-1.5 text-[11px] text-muted-foreground'>
        <span className='inline-flex items-center gap-1'>
          <Clock className='h-3 w-3' />
          {start && end ? `${start}-${end}` : 'No time'}
        </span>
        <Badge variant='secondary' className='h-4 px-1 text-[10px]'>
          {formatEffort(item.effortMillis)}
        </Badge>
      </div>
    </button>
  );
}
