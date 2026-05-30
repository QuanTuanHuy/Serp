/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project schedule allocation detail
 */

'use client';

import type React from 'react';
import { CalendarClock, ExternalLink } from 'lucide-react';
import {
  Badge,
  Button,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui';
import type { PMWorkItemScheduleAllocationCalendarItemApi } from '../../../types/api';
import {
  formatCalendarDateRange,
  formatEffort,
} from './pmProjectCalendar.utils';

interface PMProjectScheduleAllocationSheetProps {
  allocation?: PMWorkItemScheduleAllocationCalendarItemApi | null;
  relatedAllocations: PMWorkItemScheduleAllocationCalendarItemApi[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onOpenWorkItem: (workItemId: number) => void;
}

export function PMProjectScheduleAllocationSheet({
  allocation,
  relatedAllocations,
  open,
  onOpenChange,
  onOpenWorkItem,
}: PMProjectScheduleAllocationSheetProps) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className='w-full overflow-y-auto sm:max-w-xl'>
        <SheetHeader className='border-b px-5 py-4'>
          <SheetTitle className='flex items-center gap-2'>
            <CalendarClock className='h-4 w-4' />
            Schedule block
          </SheetTitle>
          <SheetDescription>
            Exact planned allocation for this work item.
          </SheetDescription>
        </SheetHeader>

        {allocation ? (
          <div className='space-y-5 px-5 py-4'>
            <section className='space-y-2'>
              <div className='flex items-start justify-between gap-3'>
                <div className='min-w-0'>
                  <div className='font-semibold'>{allocation.key}</div>
                  <div className='mt-1 text-sm text-muted-foreground'>
                    {allocation.summary}
                  </div>
                </div>
                <Button
                  type='button'
                  size='sm'
                  variant='outline'
                  className='shrink-0 gap-2'
                  onClick={() => onOpenWorkItem(allocation.workItemId)}
                >
                  <ExternalLink className='h-4 w-4' />
                  Open
                </Button>
              </div>
              <div className='flex flex-wrap gap-2'>
                {allocation.source ? (
                  <Badge variant='secondary'>{allocation.source}</Badge>
                ) : null}
                {allocation.sourceRunId ? (
                  <Badge variant='outline'>Run #{allocation.sourceRunId}</Badge>
                ) : null}
                {allocation.status?.name ? (
                  <Badge variant='outline'>{allocation.status.name}</Badge>
                ) : null}
              </div>
            </section>

            <section className='rounded-lg border bg-muted/10 p-4'>
              <div className='grid gap-4 text-sm sm:grid-cols-2'>
                <Field label='Start'>
                  {formatCalendarDateRange(allocation.start)}
                </Field>
                <Field label='End'>
                  {formatCalendarDateRange(allocation.end)}
                </Field>
                <Field label='Effort'>
                  {formatEffort(allocation.effortMillis)}
                </Field>
                <Field label='Assignee'>
                  {allocation.assigneeName ||
                    (allocation.assigneeId
                      ? `User #${allocation.assigneeId}`
                      : 'Unassigned')}
                </Field>
                <Field label='Source item'>
                  {allocation.sourceRunItemId
                    ? `#${allocation.sourceRunItemId}`
                    : 'None'}
                </Field>
                <Field label='Work type'>
                  {allocation.issueType?.name || 'None'}
                </Field>
              </div>
            </section>

            <section className='space-y-2'>
              <h3 className='text-sm font-semibold'>Other blocks</h3>
              <div className='space-y-2'>
                {relatedAllocations.length > 0 ? (
                  relatedAllocations.map((item) => (
                    <div
                      key={item.allocationId}
                      className='rounded-md border bg-background px-3 py-2 text-sm'
                    >
                      <div className='font-medium'>
                        {formatCalendarDateRange(item.start)} to{' '}
                        {formatCalendarDateRange(item.end)}
                      </div>
                      <div className='mt-1 text-xs text-muted-foreground'>
                        {formatEffort(item.effortMillis)}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className='rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
                    No other blocks in the current viewport.
                  </div>
                )}
              </div>
            </section>
          </div>
        ) : null}
      </SheetContent>
    </Sheet>
  );
}

function Field({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <div className='text-xs text-muted-foreground'>{label}</div>
      <div className='mt-1 font-medium'>{children}</div>
    </div>
  );
}
