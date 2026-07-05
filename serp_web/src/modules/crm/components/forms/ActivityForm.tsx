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
    });
  });

  return (
    <Card className={cn('w-full max-w-4xl', className)}>
      <CardHeader>
        <CardTitle className='text-xl'>Edit Activity</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          <div className='space-y-4'>
            <CardTitle className='text-lg'>Activity Details</CardTitle>

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

            <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
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
                    <SelectItem value='PLANNED'>Planned</SelectItem>
                    <SelectItem value='COMPLETED'>Completed</SelectItem>
                    <SelectItem value='CANCELLED'>Cancelled</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className='rounded-lg border border-border bg-muted/30 p-4 text-sm'>
              <p className='font-medium text-foreground'>Related To</p>
              <p className='mt-1 text-muted-foreground'>
                {activity.relatedTo.type}: {activity.relatedTo.name}
              </p>
            </div>

            <div className='grid grid-cols-1 gap-4 md:grid-cols-3'>
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
                        field.onChange(date ? toLocalDateInputValue(date) : '')
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
                <Label htmlFor='durationMinutes'>Duration (minutes)</Label>
                <Input
                  id='durationMinutes'
                  type='number'
                  min='1'
                  {...register('durationMinutes')}
                  className={errors.durationMinutes ? 'border-destructive' : ''}
                  disabled={isLoading}
                />
                {errors.durationMinutes && (
                  <p className='text-sm text-destructive'>
                    {errors.durationMinutes.message}
                  </p>
                )}
              </div>
            </div>

            <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
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
                    <SelectItem value='LOW'>Low</SelectItem>
                    <SelectItem value='MEDIUM'>Medium</SelectItem>
                    <SelectItem value='HIGH'>High</SelectItem>
                    <SelectItem value='URGENT'>Urgent</SelectItem>
                  </SelectContent>
                </Select>
              </div>

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

            {watch('status') === 'COMPLETED' && (
              <div className='space-y-4 p-4 border rounded-lg bg-muted/20'>
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
                        <SelectItem value='WRONG_NUMBER'>
                          Wrong Number
                        </SelectItem>
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
          </div>

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
