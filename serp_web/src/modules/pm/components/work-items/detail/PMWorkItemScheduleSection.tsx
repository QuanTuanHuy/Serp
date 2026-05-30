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
