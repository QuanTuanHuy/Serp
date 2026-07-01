/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar exceptions
 */

import { FormEvent, useEffect, useMemo, useState } from 'react';
import { ChevronLeft, ChevronRight, Calendar, List, Plus, Edit, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { cn } from '@/shared/utils';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Textarea } from '@/shared/components/ui/textarea';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui/tooltip';

import { PMDatePicker, PMDateTimePicker } from '../../shared';
import type {
  PMCreateResourceCalendarExceptionRequest,
  PMResourceCalendarExceptionApi,
  PMResourceCalendarExceptionType,
} from '../../../types/api';
import type { UserProfile } from '@/modules/admin/types';
import { normalizeOptionalText } from '../settings-page.types';
import { PMResourceCalendarUserCombobox } from './PMResourceCalendarUserCombobox';

type ExceptionDialogState =
  | { mode: 'create'; item?: undefined; defaultStart?: number; defaultEnd?: number }
  | { mode: 'edit'; item: PMResourceCalendarExceptionApi };

export function PMResourceCalendarExceptionPanel({
  exceptions,
  isSubmitting,
  onSubmit,
  onDelete,
  userMap = new Map(),
}: {
  exceptions: PMResourceCalendarExceptionApi[];
  isSubmitting: boolean;
  onSubmit: (
    mode: ExceptionDialogState['mode'],
    id: number | undefined,
    body: PMCreateResourceCalendarExceptionRequest
  ) => Promise<void>;
  onDelete: (exception: PMResourceCalendarExceptionApi) => void;
  userMap?: Map<number, UserProfile>;
}) {
  const [dialog, setDialog] = useState<ExceptionDialogState | null>(null);
  const [viewMode, setViewMode] = useState<'calendar' | 'list'>('calendar');
  const [currentDate, setCurrentDate] = useState(new Date());

  // 1. Resolve User Name helper
  const getUserName = (userId: number) => {
    const user = userMap.get(userId);
    if (user) {
      return `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.email || `User #${userId}`;
    }
    return `User #${userId}`;
  };

  // 2. Month Calendar Grid construction
  const calendarCells = useMemo(() => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    let startDayOfWeek = firstDayOfMonth.getDay() - 1; // 0 = Mon, 6 = Sun
    if (startDayOfWeek === -1) startDayOfWeek = 6;

    const prevMonthLastDate = new Date(year, month, 0).getDate();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const cells: { date: Date; isCurrentMonth: boolean }[] = [];

    // Previous month padding
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      cells.push({
        date: new Date(year, month - 1, prevMonthLastDate - i),
        isCurrentMonth: false,
      });
    }

    // Current month days
    for (let i = 1; i <= daysInMonth; i++) {
      cells.push({
        date: new Date(year, month, i),
        isCurrentMonth: true,
      });
    }

    // Next month padding
    const totalCells = cells.length > 35 ? 42 : 35;
    const remaining = totalCells - cells.length;
    for (let i = 1; i <= remaining; i++) {
      cells.push({
        date: new Date(year, month + 1, i),
        isCurrentMonth: false,
      });
    }

    return cells;
  }, [currentDate]);

  const handlePrevMonth = () => {
    setCurrentDate((current) => {
      const next = new Date(current);
      next.setMonth(current.getMonth() - 1);
      return next;
    });
  };

  const handleNextMonth = () => {
    setCurrentDate((current) => {
      const next = new Date(current);
      next.setMonth(current.getMonth() + 1);
      return next;
    });
  };

  const monthName = currentDate.toLocaleString('en-US', {
    month: 'long',
    year: 'numeric',
  });

  return (
    <Card className='border-border/60 bg-background/90 shadow-sm'>
      <CardHeader className='flex flex-col gap-3 border-b py-4 sm:flex-row sm:items-center sm:justify-between'>
        <div className='flex items-center gap-2.5'>
          <CardTitle className='text-sm'>Upcoming exceptions</CardTitle>
          <div className='flex items-center rounded-md border p-0.5 bg-muted/20'>
            <Button
              type='button'
              variant={viewMode === 'calendar' ? 'secondary' : 'ghost'}
              size='sm'
              className='h-7 px-2.5 text-xs'
              onClick={() => setViewMode('calendar')}
            >
              <Calendar className='mr-1.5 h-3.5 w-3.5' />
              Calendar
            </Button>
            <Button
              type='button'
              variant={viewMode === 'list' ? 'secondary' : 'ghost'}
              size='sm'
              className='h-7 px-2.5 text-xs'
              onClick={() => setViewMode('list')}
            >
              <List className='mr-1.5 h-3.5 w-3.5' />
              List
            </Button>
          </div>
        </div>

        <div className='flex items-center justify-between gap-3 sm:justify-end'>
          {viewMode === 'calendar' && (
            <div className='flex items-center gap-2'>
              <Button type='button' variant='outline' size='icon' className='h-8 w-8' onClick={handlePrevMonth}>
                <ChevronLeft className='h-4 w-4' />
              </Button>
              <span className='text-sm font-semibold min-w-[120px] text-center'>{monthName}</span>
              <Button type='button' variant='outline' size='icon' className='h-8 w-8' onClick={handleNextMonth}>
                <ChevronRight className='h-4 w-4' />
              </Button>
            </div>
          )}
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={() => {
              const startOfDay = new Date();
              startOfDay.setHours(9, 0, 0, 0);
              const endOfDay = new Date();
              endOfDay.setHours(17, 0, 0, 0);
              setDialog({
                mode: 'create',
                defaultStart: startOfDay.getTime(),
                defaultEnd: endOfDay.getTime(),
              });
            }}
          >
            <Plus className='mr-2 h-4 w-4' />
            Add exception
          </Button>
        </div>
      </CardHeader>

      <CardContent className='p-0'>
        {viewMode === 'list' ? (
          exceptions.length === 0 ? (
            <div className='p-6 text-sm text-muted-foreground text-center'>
              No upcoming resource exceptions.
            </div>
          ) : (
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>User</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Window</TableHead>
                    <TableHead>Factor</TableHead>
                    <TableHead>Reason</TableHead>
                    <TableHead className='text-right pr-4'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {exceptions.map((exception) => (
                    <TableRow key={exception.id}>
                      <TableCell className='font-medium'>{getUserName(exception.userId)}</TableCell>
                      <TableCell>
                        <Badge variant='secondary'>{exception.exceptionType}</Badge>
                      </TableCell>
                      <TableCell className='min-w-[260px] text-muted-foreground'>
                        {formatDateTime(exception.startAt)} - {formatDateTime(exception.endAt)}
                      </TableCell>
                      <TableCell>{exception.capacityFactor ?? '-'}</TableCell>
                      <TableCell className='max-w-xs text-muted-foreground truncate'>
                        {exception.reason || '-'}
                      </TableCell>
                      <TableCell className='text-right pr-4'>
                        <div className='flex justify-end gap-1'>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8 rounded-full'
                            onClick={() => setDialog({ mode: 'edit', item: exception })}
                          >
                            <Edit className='h-4 w-4' />
                          </Button>
                          <Button
                            type='button'
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10 rounded-full'
                            onClick={() => onDelete(exception)}
                          >
                            <Trash2 className='h-4 w-4' />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )
        ) : (
          <TooltipProvider>
            <div className='grid grid-cols-7 border-b text-center text-xs font-semibold py-2 bg-muted/30 text-muted-foreground'>
              <div>Mon</div>
              <div>Tue</div>
              <div>Wed</div>
              <div>Thu</div>
              <div>Fri</div>
              <div>Sat</div>
              <div>Sun</div>
            </div>
            <div className='grid grid-cols-7 grid-rows-5 bg-border gap-[1px] border-b'>
              {calendarCells.map((cell, idx) => {
                const dayStart = new Date(
                  cell.date.getFullYear(),
                  cell.date.getMonth(),
                  cell.date.getDate(),
                  0,
                  0,
                  0
                ).getTime();
                const dayEnd = new Date(
                  cell.date.getFullYear(),
                  cell.date.getMonth(),
                  cell.date.getDate(),
                  23,
                  59,
                  59,
                  999
                ).getTime();

                // Find exceptions for this day
                const dayExceptions = exceptions.filter((exc) => {
                  return exc.startAt <= dayEnd && exc.endAt >= dayStart;
                });

                return (
                  <div
                    key={idx}
                    className={cn(
                      'min-h-[100px] p-1.5 bg-background flex flex-col justify-between hover:bg-muted/10 transition-colors group relative select-none',
                      !cell.isCurrentMonth && 'opacity-40 bg-muted/10'
                    )}
                    onDoubleClick={() => {
                      const start = new Date(cell.date);
                      start.setHours(9, 0, 0, 0);
                      const end = new Date(cell.date);
                      end.setHours(17, 0, 0, 0);
                      setDialog({
                        mode: 'create',
                        defaultStart: start.getTime(),
                        defaultEnd: end.getTime(),
                      });
                    }}
                  >
                    <span className='text-xs font-medium text-muted-foreground self-end pr-1'>
                      {cell.date.getDate()}
                    </span>

                    <div className='flex flex-col gap-1 mt-1 flex-grow overflow-y-auto max-h-[72px] scrollbar-none'>
                      {dayExceptions.map((exc) => {
                        const isUnavailable = exc.exceptionType === 'UNAVAILABLE';
                        return (
                          <Tooltip key={exc.id}>
                            <TooltipTrigger asChild>
                              <div
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setDialog({ mode: 'edit', item: exc });
                                }}
                                className={cn(
                                  'text-[10px] px-1.5 py-0.5 rounded border leading-none cursor-pointer truncate font-medium transition-all hover:brightness-95',
                                  isUnavailable
                                    ? 'bg-destructive/10 text-destructive border-destructive/20 hover:bg-destructive/20'
                                    : 'bg-amber-500/10 text-amber-700 border-amber-500/20 hover:bg-amber-500/20'
                                )}
                              >
                                {isUnavailable ? 'Nghỉ: ' : 'Cap: '}
                                {getUserName(exc.userId).split(' ')[0]}
                              </div>
                            </TooltipTrigger>
                            <TooltipContent side='top' className='space-y-1 p-2 w-56'>
                              <div className='font-semibold text-xs'>{getUserName(exc.userId)}</div>
                              <div className='flex items-center gap-1.5 text-[10px] opacity-80'>
                                <Badge variant='outline' className='text-[9px] px-1 py-0 leading-none'>
                                  {exc.exceptionType}
                                </Badge>
                                {!isUnavailable && <span>Factor: {exc.capacityFactor}</span>}
                              </div>
                              <div className='text-[10px] text-muted-foreground leading-snug'>
                                {formatDateTime(exc.startAt)} - {formatDateTime(exc.endAt)}
                              </div>
                              {exc.reason && (
                                <div className='border-t pt-1 mt-1 text-[10px] italic leading-tight text-muted-foreground'>
                                  "{exc.reason}"
                                </div>
                              )}
                            </TooltipContent>
                          </Tooltip>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          </TooltipProvider>
        )}
      </CardContent>

      <ExceptionDialog
        state={dialog}
        isSubmitting={isSubmitting}
        onOpenChange={(open) => {
          if (!open) {
            setDialog(null);
          }
        }}
        onSubmit={async (mode, id, body) => {
          await onSubmit(mode, id, body);
          setDialog(null);
        }}
      />
    </Card>
  );
}

function ExceptionDialog({
  state,
  isSubmitting,
  onOpenChange,
  onSubmit,
}: {
  state: ExceptionDialogState | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (
    mode: ExceptionDialogState['mode'],
    id: number | undefined,
    body: PMCreateResourceCalendarExceptionRequest
  ) => Promise<void>;
}) {
  const [userId, setUserId] = useState<number | null>(null);
  const [exceptionType, setExceptionType] =
    useState<PMResourceCalendarExceptionType>('UNAVAILABLE');
  const [startAt, setStartAt] = useState<number | null>(null);
  const [endAt, setEndAt] = useState<number | null>(null);
  const [capacityFactor, setCapacityFactor] = useState('');
  const [reason, setReason] = useState('');

  useEffect(() => {
    if (!state) {
      return;
    }

    setUserId(state.item?.userId ?? null);
    setExceptionType(state.item?.exceptionType ?? 'UNAVAILABLE');
    const defaultStart = state.mode === 'create' ? state.defaultStart : undefined;
    const defaultEnd = state.mode === 'create' ? state.defaultEnd : undefined;
    setStartAt(state.item?.startAt ?? defaultStart ?? null);
    setEndAt(state.item?.endAt ?? defaultEnd ?? null);
    setCapacityFactor(
      state.item?.capacityFactor === null ||
        state.item?.capacityFactor === undefined
        ? ''
        : String(state.item.capacityFactor)
    );
    setReason(state.item?.reason ?? '');
  }, [state]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!state) {
      return;
    }

    const parsedUserId = userId;
    const parsedStart = startAt;
    const parsedEnd = endAt;
    const parsedFactor =
      capacityFactor.trim().length > 0 ? Number(capacityFactor) : null;

    if (
      parsedUserId === null ||
      !Number.isInteger(parsedUserId) ||
      parsedUserId <= 0 ||
      parsedStart === null ||
      parsedEnd === null ||
      !Number.isFinite(parsedStart) ||
      !Number.isFinite(parsedEnd) ||
      parsedStart >= parsedEnd
    ) {
      toast.error('Exception needs a valid user and time window.');
      return;
    }
    if (
      exceptionType === 'CAPACITY_OVERRIDE' &&
      (parsedFactor === null ||
        !Number.isFinite(parsedFactor) ||
        parsedFactor < 0 ||
        parsedFactor > 2)
    ) {
      toast.error('Capacity override requires a factor from 0 to 2.');
      return;
    }

    await onSubmit(state.mode, state.item?.id, {
      userId: parsedUserId,
      exceptionType,
      startAt: parsedStart,
      endAt: parsedEnd,
      capacityFactor:
        exceptionType === 'UNAVAILABLE' ? null : (parsedFactor ?? null),
      reason: normalizeOptionalText(reason),
    });
  };

  return (
    <Dialog open={state !== null} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <DialogHeader>
            <DialogTitle>
              {state?.mode === 'edit' ? 'Edit exception' : 'Add exception'}
            </DialogTitle>
            <DialogDescription>
              Add user-specific unavailable time or capacity overrides.
            </DialogDescription>
          </DialogHeader>

          <div className='grid gap-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label>User</Label>
                <PMResourceCalendarUserCombobox
                  value={userId}
                  onChange={setUserId}
                  placeholder='Search users by name or email...'
                />
              </div>
              <div className='space-y-2'>
                <Label>Type</Label>
                <Select
                  value={exceptionType}
                  onValueChange={(value) =>
                    setExceptionType(value as PMResourceCalendarExceptionType)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='UNAVAILABLE'>Unavailable</SelectItem>
                    <SelectItem value='CAPACITY_OVERRIDE'>
                      Capacity override
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label>Start</Label>
                <PMDateTimePicker
                  value={startAt}
                  onChange={(date?: Date) => setStartAt(date?.getTime() ?? null)}
                  placeholder='Start date and time'
                  defaultTime='09:00'
                />
              </div>
              <div className='space-y-2'>
                <Label>End</Label>
                <PMDateTimePicker
                  value={endAt}
                  onChange={(date?: Date) => setEndAt(date?.getTime() ?? null)}
                  placeholder='End date and time'
                  defaultTime='17:00'
                />
              </div>
            </div>
            {exceptionType === 'CAPACITY_OVERRIDE' && (
              <div className='space-y-2'>
                <Label htmlFor='resource-exception-factor'>Capacity factor</Label>
                <Input
                  id='resource-exception-factor'
                  type='number'
                  min='0'
                  max='2'
                  step='0.01'
                  value={capacityFactor}
                  onChange={(event) => setCapacityFactor(event.target.value)}
                />
              </div>
            )}
            <div className='space-y-2'>
              <Label htmlFor='resource-exception-reason'>Reason</Label>
              <Textarea
                id='resource-exception-reason'
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                rows={3}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function formatDateTime(value: number) {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}
