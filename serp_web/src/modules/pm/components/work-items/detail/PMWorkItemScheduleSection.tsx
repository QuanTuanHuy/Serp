/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item schedule section
 */

'use client';

import { Badge } from '@/shared/components/ui';
import { DetailField, DetailSection } from './PMWorkItemDetailPrimitives';
import { formatDetailDate } from './pmWorkItemDetail.utils';
import type { WorkItemDetailModel } from './pmWorkItemDetail.types';

export function PMWorkItemScheduleSection({
  item,
}: {
  item: WorkItemDetailModel;
}) {
  const schedule = item.schedule;

  return (
    <DetailSection title='Schedule'>
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
    </DetailSection>
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
