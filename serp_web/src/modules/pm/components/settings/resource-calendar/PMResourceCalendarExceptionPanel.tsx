/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar exceptions
 */

import { FormEvent, useEffect, useState } from 'react';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
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

import { PMDateTimePicker } from '../../shared';
import type {
  PMCreateResourceCalendarExceptionRequest,
  PMResourceCalendarExceptionApi,
  PMResourceCalendarExceptionType,
} from '../../../types/api';
import { normalizeOptionalText } from '../settings-page.types';
import { PMResourceCalendarUserCombobox } from './PMResourceCalendarUserCombobox';

type ExceptionDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMResourceCalendarExceptionApi };

export function PMResourceCalendarExceptionPanel({
  exceptions,
  isSubmitting,
  onSubmit,
  onDelete,
}: {
  exceptions: PMResourceCalendarExceptionApi[];
  isSubmitting: boolean;
  onSubmit: (
    mode: ExceptionDialogState['mode'],
    id: number | undefined,
    body: PMCreateResourceCalendarExceptionRequest
  ) => Promise<void>;
  onDelete: (exception: PMResourceCalendarExceptionApi) => void;
}) {
  const [dialog, setDialog] = useState<ExceptionDialogState | null>(null);

  return (
    <Card className='border-border/60 bg-background/90 shadow-sm'>
      <CardHeader className='flex flex-row items-center justify-between border-b py-4'>
        <CardTitle className='text-sm'>Upcoming exceptions</CardTitle>
        <Button
          type='button'
          variant='outline'
          onClick={() => setDialog({ mode: 'create' })}
        >
          <Plus className='mr-2 h-4 w-4' />
          Add exception
        </Button>
      </CardHeader>
      <CardContent className='p-0'>
        {exceptions.length === 0 ? (
          <div className='p-6 text-sm text-muted-foreground'>
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
                  <TableHead className='text-right'>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {exceptions.map((exception) => (
                  <TableRow key={exception.id}>
                    <TableCell>{exception.userId}</TableCell>
                    <TableCell>
                      <Badge variant='secondary'>
                        {exception.exceptionType}
                      </Badge>
                    </TableCell>
                    <TableCell className='min-w-[260px]'>
                      {formatDateTime(exception.startAt)} -{' '}
                      {formatDateTime(exception.endAt)}
                    </TableCell>
                    <TableCell>{exception.capacityFactor ?? '-'}</TableCell>
                    <TableCell className='max-w-xs text-muted-foreground'>
                      {exception.reason || '-'}
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-1'>
                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          aria-label='Edit exception'
                          onClick={() =>
                            setDialog({ mode: 'edit', item: exception })
                          }
                        >
                          <Edit className='h-4 w-4' />
                        </Button>
                        <Button
                          type='button'
                          variant='ghost'
                          size='icon'
                          aria-label='Delete exception'
                          onClick={() => onDelete(exception)}
                        >
                          <Trash2 className='h-4 w-4 text-destructive' />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
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
    setStartAt(state.item?.startAt ?? null);
    setEndAt(state.item?.endAt ?? null);
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
                  onChange={(date) => setStartAt(date?.getTime() ?? null)}
                  placeholder='Start date and time'
                  defaultTime='09:00'
                />
              </div>
              <div className='space-y-2'>
                <Label>End</Label>
                <PMDateTimePicker
                  value={endAt}
                  onChange={(date) => setEndAt(date?.getTime() ?? null)}
                  placeholder='End date and time'
                  defaultTime='17:00'
                />
              </div>
            </div>
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
                disabled={exceptionType === 'UNAVAILABLE'}
              />
            </div>
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
