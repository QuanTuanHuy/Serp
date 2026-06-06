'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { schoolBusUi } from '../../theme';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import type {
  PickupPointWindowDirection,
  SchoolBusSchedule,
  SchoolBusSchoolPickupPointWindow,
  SchoolPickupPointWindowUpsertRequest,
} from '../../types';
import { SchoolBusFormDialog } from '../SchoolBusFormDialog';

const windowSchema = z.object({
  schoolScheduleId: z.coerce.number().min(1, 'Schedule is required'),
  direction: z.enum(['PICKUP_TO_SCHOOL', 'DROPOFF_FROM_SCHOOL']),
  windowStart: z.string().min(1, 'Start time is required'),
  windowEnd: z.string().min(1, 'End time is required'),
  estimatedDistanceToSchoolKm: z.string().optional(),
  estimatedDurationToSchoolMin: z.string().optional(),
}).refine(
  (data) => !data.windowStart || !data.windowEnd || data.windowEnd >= data.windowStart,
  { message: 'End time must be on or after start time', path: ['windowEnd'] }
);

type WindowFormValues = z.infer<typeof windowSchema>;

const ALL_DIRECTIONS: { value: PickupPointWindowDirection; label: string }[] = [
  { value: 'PICKUP_TO_SCHOOL', label: '🏫 Pickup → School' },
  { value: 'DROPOFF_FROM_SCHOOL', label: '🏠 School → Drop-off' },
];

/** Given a pickup point usageType, return allowed directions */
function getAllowedDirections(usageType?: string | null): PickupPointWindowDirection[] {
  switch (usageType) {
    case 'PICKUP_ONLY':
      return ['PICKUP_TO_SCHOOL'];
    case 'DROPOFF_ONLY':
      return ['DROPOFF_FROM_SCHOOL'];
    case 'PICKUP_DROPOFF':
    default:
      return ['PICKUP_TO_SCHOOL', 'DROPOFF_FROM_SCHOOL'];
  }
}

/** Add minutes to a HH:mm time string. Returns HH:mm string. */
function addMinutesToTime(time: string, minutes: number): string {
  const [h, m] = time.split(':').map(Number);
  const total = h * 60 + m + minutes;
  const nh = Math.floor(total / 60) % 24;
  const nm = total % 60;
  return `${String(nh).padStart(2, '0')}:${String(nm).padStart(2, '0')}`;
}

/** Build schedule-aware warnings for the current form values. */
function getScheduleWarnings(
  direction: string,
  windowStart: string,
  windowEnd: string,
  durationMin: number | null,
  schedule: SchoolBusSchedule | undefined,
): string[] {
  if (!schedule || !windowStart || !windowEnd) return [];
  const warnings: string[] = [];

  if (direction === 'PICKUP_TO_SCHOOL' && schedule.arrivalDeadline) {
    const deadline = schedule.arrivalDeadline;
    if (windowEnd > deadline) {
      warnings.push(`Pickup window end (${windowEnd}) exceeds arrival deadline (${deadline}).`);
    } else if (durationMin && durationMin > 0) {
      const estimatedArrival = addMinutesToTime(windowEnd, durationMin);
      if (estimatedArrival > deadline) {
        warnings.push(
          `Pickup end (${windowEnd}) + travel ${durationMin} min = ${estimatedArrival}, exceeds arrival deadline (${deadline}).`
        );
      }
    }
  }

  if (direction === 'DROPOFF_FROM_SCHOOL' && schedule.departureTime) {
    const departure = schedule.departureTime;
    if (windowStart < departure) {
      warnings.push(`Drop-off window start (${windowStart}) is before departure time (${departure}).`);
    } else if (durationMin && durationMin > 0) {
      const earliestArrival = addMinutesToTime(departure, durationMin);
      if (earliestArrival > windowStart) {
        warnings.push(
          `Departure (${departure}) + travel ${durationMin} min = ${earliestArrival}, later than window start (${windowStart}).`
        );
      }
    }
  }

  return warnings;
}

interface WindowFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  schedules: SchoolBusSchedule[];
  existingWindow?: SchoolBusSchoolPickupPointWindow | null;
  /** usageType of the parent PickupPoint — controls which directions are available */
  usageType?: string | null;
  onSubmit: (values: Omit<SchoolPickupPointWindowUpsertRequest, 'schoolPickupPointId'>) => Promise<void>;
  isLoading?: boolean;
}

export function WindowFormDialog({
  open,
  onOpenChange,
  schedules,
  existingWindow,
  usageType,
  onSubmit,
  isLoading = false,
}: WindowFormDialogProps) {
  const isEditing = !!existingWindow;
  const allowedDirections = getAllowedDirections(usageType);
  const directionOptions = ALL_DIRECTIONS.filter((d) => allowedDirections.includes(d.value));

  const form = useForm<WindowFormValues>({
    resolver: zodResolver(windowSchema) as any,
    defaultValues: {
      schoolScheduleId: existingWindow?.schoolScheduleId ?? (schedules[0]?.id ?? 0),
      direction: existingWindow?.direction ?? allowedDirections[0] ?? 'PICKUP_TO_SCHOOL',
      windowStart: existingWindow?.windowStart ?? '',
      windowEnd: existingWindow?.windowEnd ?? '',
      estimatedDistanceToSchoolKm: existingWindow?.estimatedDistanceToSchoolKm != null
        ? String(existingWindow.estimatedDistanceToSchoolKm) : '',
      estimatedDurationToSchoolMin: existingWindow?.estimatedDurationToSchoolMin != null
        ? String(existingWindow.estimatedDurationToSchoolMin) : '',
    },
  });

  React.useEffect(() => {
    if (open) {
      form.reset({
        schoolScheduleId: existingWindow?.schoolScheduleId ?? (schedules[0]?.id ?? 0),
        direction: existingWindow?.direction ?? allowedDirections[0] ?? 'PICKUP_TO_SCHOOL',
        windowStart: existingWindow?.windowStart ?? '',
        windowEnd: existingWindow?.windowEnd ?? '',
        estimatedDistanceToSchoolKm: existingWindow?.estimatedDistanceToSchoolKm != null
          ? String(existingWindow.estimatedDistanceToSchoolKm) : '',
        estimatedDurationToSchoolMin: existingWindow?.estimatedDurationToSchoolMin != null
          ? String(existingWindow.estimatedDurationToSchoolMin) : '',
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, existingWindow?.id]);

  // Watch values for live schedule warnings
  const watchedScheduleId = form.watch('schoolScheduleId');
  const watchedDirection = form.watch('direction');
  const watchedWindowStart = form.watch('windowStart');
  const watchedWindowEnd = form.watch('windowEnd');
  const watchedDurationStr = form.watch('estimatedDurationToSchoolMin');

  const selectedSchedule = schedules.find((s) => s.id === watchedScheduleId);
  const durationMin = watchedDurationStr ? Number(watchedDurationStr) : null;
  const scheduleWarnings = getScheduleWarnings(
    watchedDirection, watchedWindowStart, watchedWindowEnd, durationMin, selectedSchedule,
  );

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={isEditing ? 'Edit time window' : 'Add time window'}
      description='Configure a pickup/drop-off time window for a specific schedule.'
    >
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(async (values) => {
            // Extra FE check: direction must match usageType
            if (!allowedDirections.includes(values.direction as PickupPointWindowDirection)) {
              form.setError('direction', {
                message: `Direction ${values.direction} is not allowed for usage type ${usageType || 'PICKUP_DROPOFF'}.`,
              });
              return;
            }
            await onSubmit({
              schoolScheduleId: values.schoolScheduleId,
              direction: values.direction as PickupPointWindowDirection,
              windowStart: values.windowStart,
              windowEnd: values.windowEnd,
              estimatedDistanceToSchoolKm: values.estimatedDistanceToSchoolKm
                ? Number(values.estimatedDistanceToSchoolKm) : null,
              estimatedDurationToSchoolMin: values.estimatedDurationToSchoolMin
                ? Number(values.estimatedDurationToSchoolMin) : null,
            });
          })}
          className='space-y-4'
        >
          <div className='grid gap-4 md:grid-cols-2'>
            {/* Schedule selector */}
            <FormField
              control={form.control}
              name='schoolScheduleId'
              render={({ field }) => (
                <FormItem className='md:col-span-2'>
                  <FormLabel>Schedule *</FormLabel>
                  <SchoolBusSelect
                    fullWidth
                    size='md'
                    className='h-11 rounded-xl w-full text-slate-900 border-slate-200 shadow-sm'
                    value={field.value ? String(field.value) : ''}
                    onChange={(v) => field.onChange(v ? Number(v) : null)}
                    placeholder='Select schedule'
                    options={schedules.map((s) => ({
                      label: `${s.scheduleName} (${s.shiftType})${s.arrivalDeadline ? ` • Arrival: ${s.arrivalDeadline}` : ''}${s.departureTime ? ` • Departure: ${s.departureTime}` : ''}`,
                      value: String(s.id),
                    }))}
                  />
                  {/* Show selected schedule deadline/departure as hint */}
                  {selectedSchedule && (
                    <p className='text-xs text-slate-500 mt-1'>
                      {selectedSchedule.arrivalDeadline && `Arrival deadline: ${selectedSchedule.arrivalDeadline}`}
                      {selectedSchedule.arrivalDeadline && selectedSchedule.departureTime && ' • '}
                      {selectedSchedule.departureTime && `Departure: ${selectedSchedule.departureTime}`}
                    </p>
                  )}
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Direction */}
            <FormField
              control={form.control}
              name='direction'
              render={({ field }) => (
                <FormItem className='md:col-span-2'>
                  <FormLabel>Direction *</FormLabel>
                  <SchoolBusSelect
                    fullWidth
                    size='md'
                    className='h-11 rounded-xl w-full text-slate-900 border-slate-200 shadow-sm'
                    value={field.value}
                    onChange={field.onChange}
                    placeholder='Select direction'
                    options={directionOptions.map((d) => ({
                      label: d.label,
                      value: d.value,
                    }))}
                  />
                  {usageType && usageType !== 'PICKUP_DROPOFF' && (
                    <p className='text-xs text-amber-600 mt-1'>
                      Restricted by usage type: {usageType}
                    </p>
                  )}
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Window Start */}
            <FormField
              control={form.control}
              name='windowStart'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Window start *</FormLabel>
                  <FormControl>
                    <Input type='time' className='h-11 rounded-xl' {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Window End */}
            <FormField
              control={form.control}
              name='windowEnd'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Window end *</FormLabel>
                  <FormControl>
                    <Input type='time' className='h-11 rounded-xl' {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Estimated Distance */}
            <FormField
              control={form.control}
              name='estimatedDistanceToSchoolKm'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Est. distance (km)</FormLabel>
                  <FormControl>
                    <Input type='number' step='0.1' min='0' placeholder='e.g. 3.5'
                      className='h-11 rounded-xl' {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Estimated Duration */}
            <FormField
              control={form.control}
              name='estimatedDurationToSchoolMin'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Est. duration (min)</FormLabel>
                  <FormControl>
                    <Input type='number' step='1' min='0' placeholder='e.g. 15'
                      className='h-11 rounded-xl' {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>

          {/* Schedule-aware warnings */}
          {scheduleWarnings.length > 0 && (
            <div className='rounded-lg border border-amber-200 bg-amber-50 p-3'>
              <p className='text-sm font-medium text-amber-800 mb-1'>⚠️ Schedule conflict</p>
              {scheduleWarnings.map((w, i) => (
                <p key={i} className='text-xs text-amber-700'>{w}</p>
              ))}
            </div>
          )}

          <div className='flex justify-end gap-2 border-t pt-4'>
            <Button
              type='button'
              variant='outline'
              className={schoolBusUi.outlineButton}
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type='submit'
              className={schoolBusUi.primaryButton}
              disabled={isLoading}
            >
              {isLoading ? 'Saving...' : isEditing ? 'Update window' : 'Add window'}
            </Button>
          </div>
        </form>
      </Form>
    </SchoolBusFormDialog>
  );
}
