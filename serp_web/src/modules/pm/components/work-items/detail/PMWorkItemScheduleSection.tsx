/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item schedule section
 */

'use client';

import { useState } from 'react';
import { CalendarDays, ChevronDown, ChevronRight } from 'lucide-react';
import { Badge } from '@/shared/components/ui';
import { DetailField } from './PMWorkItemDetailPrimitives';
import { formatDetailDate } from './pmWorkItemDetail.utils';
import type { WorkItemDetailModel } from './pmWorkItemDetail.types';

export function PMWorkItemScheduleSection({
  item,
}: {
  item: WorkItemDetailModel;
}) {
  const [open, setOpen] = useState(true);
  const schedule = item.schedule;

  return (
    <section className='mt-3 rounded-lg border bg-background'>
      <button
        type='button'
        className='flex w-full items-center justify-between gap-3 px-4 py-3 text-left hover:bg-muted/40'
        onClick={() => setOpen((current) => !current)}
        aria-expanded={open}
      >
        <span className='inline-flex min-w-0 items-center gap-3 font-semibold'>
          {open ? (
            <ChevronDown className='h-4 w-4 shrink-0 text-muted-foreground' />
          ) : (
            <ChevronRight className='h-4 w-4 shrink-0 text-muted-foreground' />
          )}
          <span className='truncate'>Schedule</span>
          <CalendarDays className='h-4 w-4 shrink-0 text-muted-foreground' />
        </span>
      </button>

      {open ? (
        <div className='space-y-5 border-t p-4'>
          <DetailField label='Plan'>
            {schedule ? (
              <div className='space-y-2'>
                <div className='flex flex-wrap items-center gap-2'>
                  <Badge variant='secondary' className='h-6 px-2'>
                    {schedule.source || 'UNKNOWN'}
                  </Badge>
                  {schedule.locked ? (
                    <Badge variant='destructive' className='h-6 px-2'>
                      Locked
                    </Badge>
                  ) : null}
                  {schedule.sourceRunId ? (
                    <Badge variant='outline' className='h-6 px-2'>
                      Run #{schedule.sourceRunId}
                    </Badge>
                  ) : null}
                </div>
                <div className='font-medium'>
                  {formatDetailDate(schedule.plannedStart)} to{' '}
                  {formatDetailDate(schedule.plannedEnd)}
                </div>
                {schedule.allocations?.length ? (
                  <div className='space-y-1.5 pt-1'>
                    {schedule.allocations.map((allocation, index) => (
                      <div
                        key={`${allocation.start ?? index}-${allocation.end ?? index}`}
                        className='rounded-md border bg-muted/20 px-2 py-1.5 text-xs'
                      >
                        <div className='font-medium'>
                          {formatDetailDateTime(allocation.start)} to{' '}
                          {formatDetailDateTime(allocation.end)}
                        </div>
                        <div className='mt-0.5 text-muted-foreground'>
                          {formatEffort(allocation.effortMillis)}
                          {allocation.assigneeId
                            ? ` / User #${allocation.assigneeId}`
                            : ''}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : null}
              </div>
            ) : (
              <span className='text-muted-foreground'>Unscheduled</span>
            )}
          </DetailField>
          <DetailField label='Deadline'>
            <span className='text-muted-foreground'>
              {formatDetailDate(item.dueDate)}
            </span>
          </DetailField>
        </div>
      ) : null}
    </section>
  );
}

function formatDetailDateTime(value?: number | string | null): string {
  if (!value) return 'None';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'None';
  return date.toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatEffort(value?: number | null): string {
  if (!value) return '0m';
  const minutes = Math.round(value / 60000);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours}h ${remainder}m` : `${hours}h`;
}
