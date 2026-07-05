// ActivityForm Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import { useMemo } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Textarea,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { CRMDatePicker, CRMUserSelect } from '../shared';
import { toLocalDateInputValue } from '../../utils';
import type {
  Activity,
  BackendActivityStatus,
  BackendActivityType,
  Priority,
  UpdateActivityRequest,
  ActivityOutcome,
} from '../../types';

const editableActivityTypes = ['CALL', 'EMAIL', 'MEETING', 'TASK'] as const;
const editableActivityStatuses = ['PLANNED', 'COMPLETED', 'CANCELLED'] as const;
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'] as const;

const activityFormSchema = z.object({
  subject: z
    .string()
    .trim()
    .min(1, 'Subject is required.')
    .max(255, 'Subject must not exceed 255 characters.'),
  activityType: z.enum(editableActivityTypes),
  status: z.enum(editableActivityStatuses),
  scheduledDate: z.string().optional(),
  scheduledTime: z.string().optional(),
  durationMinutes: z
    .string()
    .trim()
    .optional()
    .refine(
      (value) => !value || (/^\d+$/.test(value) && Number(value) > 0),
      'Duration must be a positive number.'
    ),
  priority: z.enum(priorities),
  assignedTo: z
    .string()
    .trim()
    .optional()
    .refine(
      (value) => !value || /^\d+$/.test(value),
      'Assigned user must be a numeric user ID.'
    ),
  location: z
    .string()
    .trim()
    .max(255, 'Location must not exceed 255 characters.')
    .optional(),
  description: z
    .string()
    .trim()
    .max(1000, 'Description must not exceed 1000 characters.')
    .optional(),
  notes: z
    .string()
    .trim()
    .max(1000, 'Notes must not exceed 1000 characters.')
    .optional(),
  outcome: z.string().optional(),
  progressPercent: z.number().min(0).max(100).optional(),
});

type ActivityFormData = z.infer<typeof activityFormSchema>;

interface ActivityFormProps {
  activity: Activity;
  onSubmit: (data: UpdateActivityRequest) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

const toDateInputValue = (value?: string) => {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const toTimeInputValue = (value?: string) => {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
};

export const ActivityForm: React.FC<ActivityFormProps> = ({
  activity,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const defaultValues = useMemo<ActivityFormData>(
    () => ({
      subject: activity.subject,
      activityType: (editableActivityTypes.includes(
        activity.type as BackendActivityType
      )
        ? activity.type
        : 'TASK') as BackendActivityType,
      status: activity.status,
      scheduledDate: toDateInputValue(activity.scheduledDate),
      scheduledTime: toTimeInputValue(activity.scheduledDate),
      durationMinutes: activity.duration ? String(activity.duration) : '',
      priority: activity.priority,
      assignedTo: activity.assignedTo || '',
      location: activity.location || '',
      description: activity.description || '',
      notes:
        typeof activity.customFields?.notes === 'string'
          ? activity.customFields.notes
          : '',
      outcome: activity.outcome || '',
      progressPercent: activity.progressPercent ?? 0,
    }),
    [activity]
  );

  const {
    register,
    handleSubmit,
    control,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<ActivityFormData>({
    resolver: zodResolver(activityFormSchema),
    defaultValues,
  });

  const watchedActivityType = watch('activityType');

  const onFormSubmit = handleSubmit(async (data) => {
    const scheduledAt = data.scheduledDate
      ? new Date(
          `${data.scheduledDate}T${data.scheduledTime?.trim() || '00:00'}`
        ).getTime()
      : undefined;

    await onSubmit({
      subject: data.subject,
      activityType: data.activityType,
      status: data.status as BackendActivityStatus,
      activityDate: scheduledAt,
      dueDate: data.activityType === 'TASK' ? scheduledAt : undefined,
      durationMinutes: data.durationMinutes?.trim()
        ? Number(data.durationMinutes)
        : undefined,
      priority: data.priority as Priority,
      assignedTo: data.assignedTo?.trim() ? Number(data.assignedTo) : undefined,
      location:
        data.activityType === 'MEETING' && data.location?.trim()
          ? data.location.trim()
          : undefined,
      description: data.description?.trim() || undefined,
      notes: data.notes?.trim() || undefined,
      outcome:
        data.status === 'COMPLETED' && data.outcome
          ? (data.outcome as ActivityOutcome)
          : undefined,
      progressPercent:
        data.activityType === 'TASK' ? data.progressPercent : undefined,
    });
  });

  return (
    <Card className={cn('w-full max-w-5xl', className)}>
      <CardHeader>
        <CardTitle className='text-xl'>Edit Activity</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
            {/* Cột trái: General Information */}
            <div className='space-y-4'>
              <CardTitle className='text-lg'>Activity Details</CardTitle>

              {/* Subject */}
              <div className='space-y-2'>
                <Label htmlFor='subject'>Subject *</Label>
                <Input
                  id='subject'
                  {...register('subject')}
                  placeholder='Enter activity subject'
                  className={errors.subject ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.subject && (
                  <p className='text-sm text-destructive'>
                    {errors.subject.message}
                  </p>
                )}
              </div>

              {/* Activity Type */}
              <div className='space-y-2'>
                <Label>Activity Type *</Label>
                <div className='flex gap-1 p-1 bg-muted rounded-lg w-full'>
                  {editableActivityTypes.map((type) => (
                    <button
                      key={type}
                      type='button'
                      onClick={() => setValue('activityType', type)}
                      className={cn(
                        'flex-1 py-1.5 px-3 rounded-md text-xs font-medium transition-all cursor-pointer text-center',
                        watchedActivityType === type
                          ? 'bg-background text-foreground shadow-sm'
                          : 'text-muted-foreground hover:text-foreground'
                      )}
                      disabled={isLoading}
                    >
                      {type === 'CALL'
                        ? 'Call'
                        : type === 'EMAIL'
                          ? 'Email'
                          : type === 'MEETING'
                            ? 'Meeting'
                            : 'Task'}
                    </button>
                  ))}
                </div>
              </div>

              {/* Related To */}
              <div className='rounded-lg border border-border bg-muted/30 p-4 text-sm min-w-0'>
                <p className='font-medium text-foreground'>Related To</p>
                <p className='mt-1 text-muted-foreground break-words whitespace-normal'>
                  {activity.relatedTo.type}: {activity.relatedTo.name}
                </p>
              </div>

              {/* Location (for meetings) */}
              {watchedActivityType === 'MEETING' && (
                <div className='space-y-2'>
                  <Label htmlFor='location'>Location</Label>
                  <Input
                    id='location'
                    {...register('location')}
                    placeholder='Enter meeting location or link'
                    className={errors.location ? 'border-destructive' : ''}
                    disabled={isLoading}
                  />
                  {errors.location && (
                    <p className='text-sm text-destructive'>
                      {errors.location.message}
                    </p>
                  )}
                </div>
              )}

              {/* Description */}
              <div className='space-y-2'>
                <Label htmlFor='description'>Description</Label>
                <Textarea
                  id='description'
                  {...register('description')}
                  placeholder='Describe the activity'
                  rows={4}
                  className={errors.description ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.description && (
                  <p className='text-sm text-destructive'>
                    {errors.description.message}
                  </p>
                )}
              </div>
            </div>

            {/* Cột phải: Scheduling & Assignee */}
            <div className='space-y-4'>
              <CardTitle className='text-lg'>Scheduling & Assignment</CardTitle>

              {/* Status */}
              <div className='space-y-2'>
                <Label>Status *</Label>
                <Select
                  value={watch('status')}
                  onValueChange={(value: BackendActivityStatus) =>
                    setValue('status', value)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='PLANNED'>
                      <span className='inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300'>
                        Planned
                      </span>
                    </SelectItem>
                    <SelectItem value='COMPLETED'>
                      <span className='inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300'>
                        Completed
                      </span>
                    </SelectItem>
                    <SelectItem value='CANCELLED'>
                      <span className='inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300'>
                        Cancelled
                      </span>
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Date & Time Grid */}
              <div className='grid grid-cols-3 gap-4'>
                <div className='space-y-2'>
                  <Label htmlFor='scheduledDate'>Date</Label>
                  <Controller
                    name='scheduledDate'
                    control={control}
                    render={({ field }) => (
                      <CRMDatePicker
                        id='scheduledDate'
                        value={field.value}
                        onChange={(date) =>
                          field.onChange(
                            date ? toLocalDateInputValue(date) : ''
                          )
                        }
                        disabled={isLoading}
                      />
                    )}
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='scheduledTime'>Time</Label>
                  <Input
                    id='scheduledTime'
                    type='time'
                    {...register('scheduledTime')}
                    disabled={isLoading}
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='durationMinutes'>Duration (mins)</Label>
                  <Input
                    id='durationMinutes'
                    type='number'
                    min='1'
                    {...register('durationMinutes')}
                    className={
                      errors.durationMinutes ? 'border-destructive' : ''
                    }
                    disabled={isLoading}
                  />
                  {errors.durationMinutes && (
                    <p className='text-sm text-destructive'>
                      {errors.durationMinutes.message}
                    </p>
                  )}
                </div>
              </div>

              {/* Priority */}
              <div className='space-y-2'>
                <Label>Priority</Label>
                <Select
                  value={watch('priority')}
                  onValueChange={(value: Priority) =>
                    setValue('priority', value)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select priority' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='LOW'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-green-500' />
                        Low
                      </div>
                    </SelectItem>
                    <SelectItem value='MEDIUM'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-blue-500' />
                        Medium
                      </div>
                    </SelectItem>
                    <SelectItem value='HIGH'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-orange-500' />
                        High
                      </div>
                    </SelectItem>
                    <SelectItem value='URGENT'>
                      <div className='flex items-center gap-2'>
                        <span className='h-2.5 w-2.5 rounded-full bg-red-500' />
                        Urgent
                      </div>
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Progress Slider (for Tasks) */}
              {watchedActivityType === 'TASK' && (
                <div className='space-y-2'>
                  <div className='flex justify-between items-center'>
                    <Label htmlFor='progressPercent'>Progress</Label>
                    <span className='text-xs font-semibold text-muted-foreground'>
                      {watch('progressPercent')}%
                    </span>
                  </div>
                  <div className='flex items-center gap-4'>
                    <input
                      id='progressPercent'
                      type='range'
                      min='0'
                      max='100'
                      value={watch('progressPercent') ?? 0}
                      onChange={(e) =>
                        setValue('progressPercent', Number(e.target.value))
                      }
                      className='flex-1 h-1.5 bg-muted rounded-lg appearance-none cursor-pointer accent-primary'
                    />
                  </div>
                </div>
              )}

              {/* Assigned To */}
              <div className='space-y-2'>
                <Label htmlFor='assignedTo'>Assigned To</Label>
                <Controller
                  name='assignedTo'
                  control={control}
                  render={({ field }) => (
                    <CRMUserSelect
                      id='assignedTo'
                      value={field.value}
                      onChange={field.onChange}
                      fallbackUserName={activity.assignedToName}
                      disabled={isLoading}
                      className={errors.assignedTo ? 'border-destructive' : ''}
                    />
                  )}
                />
                {errors.assignedTo && (
                  <p className='text-sm text-destructive'>
                    {errors.assignedTo.message}
                  </p>
                )}
              </div>
            </div>
          </div>

          {/* Completion Details Card (Expands when status === COMPLETED) */}
          {watch('status') === 'COMPLETED' && (
            <div className='space-y-4 p-4 border rounded-lg bg-muted/20 animate-fade-in'>
              <Label className='text-sm font-semibold block'>
                Completion Details
              </Label>

              {watchedActivityType === 'CALL' && (
                <div className='space-y-2'>
                  <Label htmlFor='outcome'>Call Outcome</Label>
                  <Select
                    value={watch('outcome')}
                    onValueChange={(value) => setValue('outcome', value)}
                    disabled={isLoading}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select outcome' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='REACHED'>Reached</SelectItem>
                      <SelectItem value='VOICEMAIL'>Voicemail</SelectItem>
                      <SelectItem value='NO_ANSWER'>No Answer</SelectItem>
                      <SelectItem value='BUSY'>Busy</SelectItem>
                      <SelectItem value='WRONG_NUMBER'>Wrong Number</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              {watchedActivityType === 'MEETING' && (
                <div className='space-y-2'>
                  <Label htmlFor='outcome'>Meeting Outcome</Label>
                  <Select
                    value={watch('outcome')}
                    onValueChange={(value) => setValue('outcome', value)}
                    disabled={isLoading}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select outcome' />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value='OCCURRED'>Occurred</SelectItem>
                      <SelectItem value='NO_SHOW'>No Show</SelectItem>
                      <SelectItem value='RESCHEDULED'>Rescheduled</SelectItem>
                      <SelectItem value='CANCELLED_BY_CUSTOMER'>
                        Cancelled by Customer
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              <div className='space-y-2'>
                <Label htmlFor='notes'>Notes / Summary</Label>
                <Textarea
                  id='notes'
                  {...register('notes')}
                  placeholder='Add summary or notes...'
                  rows={4}
                  className={errors.notes ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.notes && (
                  <p className='text-sm text-destructive'>
                    {errors.notes.message}
                  </p>
                )}
              </div>
            </div>
          )}

          <div className='flex items-center justify-end gap-3'>
            <Button
              type='button'
              variant='outline'
              onClick={onCancel}
              disabled={isLoading || isSubmitting}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isLoading || isSubmitting}>
              Save Changes
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};
