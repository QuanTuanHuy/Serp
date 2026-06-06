/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project schedule allocation detail
 */

'use client';

import type React from 'react';
import { useEffect, useState } from 'react';
import { CalendarClock, Edit3, ExternalLink, Save, X } from 'lucide-react';
import {
  Badge,
  Button,
  Checkbox,
  Input,
  Label,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui';
import type { PMWorkItemScheduleAllocationCalendarItemApi } from '../../../types/api';
import {
  fromCalendarDateTimeInputValue,
  formatCalendarDateRange,
  formatEffort,
  toCalendarDateTimeInputValue,
} from './pmProjectCalendar.utils';

interface PMProjectScheduleAllocationSaveInput {
  allocationId: number;
  start: number;
  end: number;
  effortMillis: number;
  assigneeId: number;
  locked: boolean;
}

interface PMProjectScheduleAllocationSheetProps {
  allocation?: PMWorkItemScheduleAllocationCalendarItemApi | null;
  relatedAllocations: PMWorkItemScheduleAllocationCalendarItemApi[];
  open: boolean;
  isSaving?: boolean;
  onOpenChange: (open: boolean) => void;
  onOpenWorkItem: (workItemId: number) => void;
  onSaveSchedule?: (
    input: PMProjectScheduleAllocationSaveInput
  ) => Promise<void> | void;
}

export function PMProjectScheduleAllocationSheet({
  allocation,
  relatedAllocations,
  open,
  isSaving,
  onOpenChange,
  onOpenWorkItem,
  onSaveSchedule,
}: PMProjectScheduleAllocationSheetProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [startInput, setStartInput] = useState('');
  const [endInput, setEndInput] = useState('');
  const [effortMinutesInput, setEffortMinutesInput] = useState('');
  const [assigneeIdInput, setAssigneeIdInput] = useState('');
  const [locked, setLocked] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setStartInput(toCalendarDateTimeInputValue(allocation?.start));
    setEndInput(toCalendarDateTimeInputValue(allocation?.end));
    setEffortMinutesInput(
      allocation?.effortMillis
        ? String(Math.round(allocation.effortMillis / 60000))
        : ''
    );
    setAssigneeIdInput(
      allocation?.assigneeId ? String(allocation.assigneeId) : ''
    );
    setLocked(allocation?.locked ?? true);
    setError(null);
    setIsEditing(false);
  }, [allocation]);

  const handleSave = async () => {
    if (!allocation || !onSaveSchedule) return;

    const start = fromCalendarDateTimeInputValue(startInput);
    const end = fromCalendarDateTimeInputValue(endInput);
    const assigneeId = Number(assigneeIdInput);
    const effortMinutes = Number(effortMinutesInput);

    if (!start || !end) {
      setError('Start and end are required.');
      return;
    }
    if (start >= end) {
      setError('Start must be before end.');
      return;
    }
    if (!Number.isFinite(assigneeId) || assigneeId <= 0) {
      setError('Assignee is required.');
      return;
    }
    if (!Number.isFinite(effortMinutes) || effortMinutes <= 0) {
      setError('Effort must be greater than zero.');
      return;
    }

    setError(null);
    await onSaveSchedule({
      allocationId: allocation.allocationId,
      start,
      end,
      effortMillis: Math.round(effortMinutes * 60000),
      assigneeId,
      locked,
    });
    setIsEditing(false);
  };

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
              <div className='mb-4 flex items-center justify-between gap-3'>
                <h3 className='text-sm font-semibold'>Schedule</h3>
                {isEditing ? (
                  <div className='flex items-center gap-2'>
                    <Button
                      type='button'
                      size='sm'
                      variant='outline'
                      className='gap-2'
                      disabled={isSaving}
                      onClick={() => {
                        setError(null);
                        setIsEditing(false);
                      }}
                    >
                      <X className='h-4 w-4' />
                      Cancel
                    </Button>
                    <Button
                      type='button'
                      size='sm'
                      className='gap-2'
                      disabled={isSaving}
                      onClick={handleSave}
                    >
                      <Save className='h-4 w-4' />
                      {isSaving ? 'Saving' : 'Save'}
                    </Button>
                  </div>
                ) : (
                  <Button
                    type='button'
                    size='sm'
                    variant='outline'
                    className='gap-2'
                    onClick={() => setIsEditing(true)}
                  >
                    <Edit3 className='h-4 w-4' />
                    Edit
                  </Button>
                )}
              </div>

              {isEditing ? (
                <div className='space-y-4'>
                  {error ? (
                    <div className='rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive'>
                      {error}
                    </div>
                  ) : null}
                  <div className='grid gap-4 sm:grid-cols-2'>
                    <div className='space-y-2'>
                      <Label htmlFor='schedule-start'>Start</Label>
                      <Input
                        id='schedule-start'
                        type='datetime-local'
                        value={startInput}
                        disabled={isSaving}
                        onChange={(event) => setStartInput(event.target.value)}
                      />
                    </div>
                    <div className='space-y-2'>
                      <Label htmlFor='schedule-end'>End</Label>
                      <Input
                        id='schedule-end'
                        type='datetime-local'
                        value={endInput}
                        disabled={isSaving}
                        onChange={(event) => setEndInput(event.target.value)}
                      />
                    </div>
                    <div className='space-y-2'>
                      <Label htmlFor='schedule-effort'>Effort minutes</Label>
                      <Input
                        id='schedule-effort'
                        type='number'
                        min='1'
                        step='1'
                        value={effortMinutesInput}
                        disabled={isSaving}
                        onChange={(event) =>
                          setEffortMinutesInput(event.target.value)
                        }
                      />
                    </div>
                    <div className='space-y-2'>
                      <Label htmlFor='schedule-assignee'>Assignee ID</Label>
                      <Input
                        id='schedule-assignee'
                        type='number'
                        min='1'
                        step='1'
                        value={assigneeIdInput}
                        disabled={isSaving}
                        onChange={(event) =>
                          setAssigneeIdInput(event.target.value)
                        }
                      />
                    </div>
                  </div>
                  <label className='flex items-center gap-2 text-sm font-medium'>
                    <Checkbox
                      checked={locked}
                      disabled={isSaving}
                      onCheckedChange={(checked) => setLocked(checked === true)}
                    />
                    Lock manual plan
                  </label>
                </div>
              ) : (
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
                  <Field label='Locked'>
                    {(allocation.locked ?? true) ? 'Yes' : 'No'}
                  </Field>
                </div>
              )}
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
