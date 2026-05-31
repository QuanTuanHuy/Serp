'use client';

import * as React from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import {
  Button,
  Checkbox,
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
import { SHIFT_TYPE_OPTIONS } from '../../constants';
import { schoolBusUi } from '../../theme';
import type {
  SchoolBusSchedule,
  SchoolBusScheduleUpsertRequest,
} from '../../types';
import { SchoolBusFormDialog } from '../SchoolBusFormDialog';

const DAY_OF_WEEK_OPTIONS = [
  { value: 'MONDAY', label: 'Mon' },
  { value: 'TUESDAY', label: 'Tue' },
  { value: 'WEDNESDAY', label: 'Wed' },
  { value: 'THURSDAY', label: 'Thu' },
  { value: 'FRIDAY', label: 'Fri' },
  { value: 'SATURDAY', label: 'Sat' },
  { value: 'SUNDAY', label: 'Sun' },
];

const DEFAULT_WEEKDAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];

const scheduleSchema = z.object({
  scheduleName: z.string().min(1, 'Schedule name is required'),
  shiftType: z.string().min(1, 'Shift type is required'),
  educationLevel: z.string().optional(),
  grade: z.string().optional(),
  daysOfWeek: z.array(z.string()).min(1, 'At least one applicable day is required'),
  arrivalDeadline: z.string().min(1, 'Arrival deadline is required'),
  departureTime: z.string().min(1, 'Departure time is required'),
  effectiveFrom: z.string().min(1, 'Effective from is required'),
  effectiveTo: z.string().optional(),
  isDefault: z.boolean().default(false),
  isActive: z.boolean().default(true),
});

type ScheduleFormValues = z.infer<typeof scheduleSchema>;

interface ScheduleFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialData?: SchoolBusSchedule | null;
  onSubmit: (values: SchoolBusScheduleUpsertRequest) => Promise<void>;
  isLoading?: boolean;
}

export function ScheduleFormDialog({
  open,
  onOpenChange,
  initialData,
  onSubmit,
  isLoading = false,
}: ScheduleFormDialogProps) {
  const form = useForm<ScheduleFormValues>({
    resolver: zodResolver(scheduleSchema) as any,
    defaultValues: {
      scheduleName: initialData?.scheduleName || '',
      shiftType: initialData?.shiftType || SHIFT_TYPE_OPTIONS[0].value,
      educationLevel: initialData?.educationLevel || '',
      grade: initialData?.grade || '',
      daysOfWeek: initialData?.daysOfWeek || (initialData ? [] : DEFAULT_WEEKDAYS),
      arrivalDeadline: initialData?.arrivalDeadline || '',
      departureTime: initialData?.departureTime || '',
      effectiveFrom: initialData?.effectiveFrom || '',
      effectiveTo: initialData?.effectiveTo || '',
      isDefault: initialData?.isDefault ?? false,
      isActive: initialData?.isActive ?? true,
    },
  });

  React.useEffect(() => {
    form.reset({
      scheduleName: initialData?.scheduleName || '',
      shiftType: initialData?.shiftType || SHIFT_TYPE_OPTIONS[0].value,
      educationLevel: initialData?.educationLevel || '',
      grade: initialData?.grade || '',
      daysOfWeek: initialData?.daysOfWeek || (initialData ? [] : DEFAULT_WEEKDAYS),
      arrivalDeadline: initialData?.arrivalDeadline || '',
      departureTime: initialData?.departureTime || '',
      effectiveFrom: initialData?.effectiveFrom || '',
      effectiveTo: initialData?.effectiveTo || '',
      isDefault: initialData?.isDefault ?? false,
      isActive: initialData?.isActive ?? true,
    });
  }, [form, initialData]);

  return (
    <SchoolBusFormDialog
      open={open}
      onOpenChange={onOpenChange}
      title={initialData ? 'Edit schedule' : 'Create schedule'}
      description='Define academic shift schedules with arrival and departure windows.'
    >
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(async (values) =>
            onSubmit({
              scheduleName: values.scheduleName,
              shiftType: values.shiftType,
              educationLevel: values.educationLevel || null,
              grade: values.grade || null,
              daysOfWeek: values.daysOfWeek.length > 0 ? values.daysOfWeek : undefined,
              arrivalDeadline: values.arrivalDeadline || null,
              departureTime: values.departureTime || null,
              effectiveFrom: values.effectiveFrom,
              effectiveTo: values.effectiveTo || null,
              isDefault: values.isDefault,
              isActive: values.isActive,
            })
          )}
          className='space-y-4'
        >
          <div className='grid gap-4 md:grid-cols-2'>
            <FormField
              control={form.control}
              name='scheduleName'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Schedule name *</FormLabel>
                  <FormControl>
                    <Input {...field} value={field.value ?? ''} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            {initialData?.scheduleCode ? (
              <div className='space-y-2'>
                <FormLabel>Schedule code</FormLabel>
                <div className='rounded-xl border border-slate-200 bg-slate-50/70 px-3 py-2 text-sm font-semibold text-slate-700'>
                  {initialData.scheduleCode}
                </div>
              </div>
            ) : null}
            <FormField
              control={form.control}
              name='shiftType'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Shift type *</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger className='h-11 rounded-xl border-slate-200 bg-white shadow-sm'>
                        <SelectValue placeholder='Select shift' />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className='z-[120] rounded-xl border-slate-200 bg-white'>
                      {SHIFT_TYPE_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='educationLevel'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Education level</FormLabel>
                  <FormControl>
                    <Input {...field} value={field.value ?? ''} placeholder='e.g. Primary, Secondary' />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='grade'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Grade</FormLabel>
                  <FormControl>
                    <Input {...field} value={field.value ?? ''} placeholder='e.g. Grade 1, Grade 10' />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='arrivalDeadline'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Arrival deadline *</FormLabel>
                  <FormControl>
                    <Input {...field} type='time' value={field.value ?? ''} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='departureTime'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Departure time *</FormLabel>
                  <FormControl>
                    <Input {...field} type='time' value={field.value ?? ''} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='effectiveFrom'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Effective from *</FormLabel>
                  <FormControl>
                    <Input {...field} type='date' value={field.value ?? ''} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name='effectiveTo'
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Effective to</FormLabel>
                  <FormControl>
                    <Input {...field} type='date' value={field.value ?? ''} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>

          {/* Days of week multi-select */}
          <FormField
            control={form.control}
            name='daysOfWeek'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Applicable days *</FormLabel>
                <div className='flex flex-wrap gap-3'>
                  {DAY_OF_WEEK_OPTIONS.map((day) => {
                    const checked = field.value?.includes(day.value) ?? false;
                    return (
                      <label
                        key={day.value}
                        className={`flex cursor-pointer items-center gap-1.5 rounded-lg border px-3 py-1.5 text-sm transition-colors ${
                          checked
                            ? 'border-slate-900 bg-slate-900 text-white font-medium'
                            : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300'
                        }`}
                      >
                        <Checkbox
                          checked={checked}
                          onCheckedChange={(c) => {
                            const current = field.value || [];
                            field.onChange(
                              c
                                ? [...current, day.value]
                                : current.filter((d: string) => d !== day.value)
                            );
                          }}
                          className='h-3.5 w-3.5'
                        />
                        {day.label}
                      </label>
                    );
                  })}
                </div>
                <p className='text-xs text-slate-400 mt-1'>
                  Leave empty for default (Mon–Fri).
                </p>
                <FormMessage />
              </FormItem>
            )}
          />

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
              {isLoading ? 'Saving...' : 'Save'}
            </Button>
          </div>
        </form>
      </Form>
    </SchoolBusFormDialog>
  );
}
