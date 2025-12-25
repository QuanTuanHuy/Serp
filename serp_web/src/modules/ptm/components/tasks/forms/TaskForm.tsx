/**
 * PTM - Task Form Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Reusable task form with validation
 */

'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
  FormDescription,
  Input,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Button,
  Checkbox,
} from '@/shared/components/ui';
import { useGetProjectsQuery } from '../../../api';
import type { Task, TaskPriority, RepeatConfig } from '../../../types';
import { RecurringTaskConfig } from '../RecurringTaskConfig';

const taskSchema = z.object({
  title: z.string().min(1, 'Title is required').max(200, 'Title too long'),
  description: z.string().max(2000, 'Description too long').optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']).default('MEDIUM'),
  estimatedDurationHours: z.coerce.number().min(0).max(1000).optional(),
  deadlineMs: z.coerce.number().optional(),
  projectId: z.coerce.number().optional(),
  parentTaskId: z.coerce.number().optional(),
  category: z.string().max(50).optional(),
  tags: z.array(z.string()).default([]),
  isDeepWork: z.boolean().default(false),
  repeatConfig: z
    .object({
      frequency: z.enum(['daily', 'weekly', 'monthly']),
      interval: z.number().min(1),
      endDate: z.string().optional(),
      daysOfWeek: z.array(z.number()).optional(),
    })
    .optional(),
});

export type TaskFormValues = z.infer<typeof taskSchema>;

interface TaskFormProps {
  defaultValues?: Partial<Task>;
  onSubmit: (data: TaskFormValues) => void | Promise<void>;
  isLoading?: boolean;
  submitLabel?: string;
}

export function TaskForm({
  defaultValues,
  onSubmit,
  isLoading,
  submitLabel = 'Save',
}: TaskFormProps) {
  const form = useForm<TaskFormValues>({
    resolver: zodResolver(taskSchema) as any,
    defaultValues: {
      title: '',
      description: '',
      priority: 'MEDIUM',
      estimatedDurationHours: undefined,
      tags: [],
      isDeepWork: false,
      ...defaultValues,
    },
  });

  const { data: projectsResponse } = useGetProjectsQuery({});
  const projects = projectsResponse?.data?.items || [];

  const handleTagsChange = (tagsString: string) => {
    const tagsArray = tagsString
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean);
    form.setValue('tags', tagsArray);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit as any)} className='space-y-4'>
        {/* Title */}
        <FormField
          control={form.control as any}
          name='title'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Title *</FormLabel>
              <FormControl>
                <Input placeholder='Enter task title' {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Description */}
        <FormField
          control={form.control as any}
          name='description'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Description</FormLabel>
              <FormControl>
                <Textarea
                  placeholder='Add task description...'
                  rows={3}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className='grid grid-cols-2 gap-4'>
          {/* Priority */}
          <FormField
            control={form.control as any}
            name='priority'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Priority</FormLabel>
                <Select onValueChange={field.onChange} value={field.value}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder='Select priority' />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value='LOW'>Low</SelectItem>
                    <SelectItem value='MEDIUM'>Medium</SelectItem>
                    <SelectItem value='HIGH'>High</SelectItem>
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* Estimated Hours */}
          <FormField
            control={form.control as any}
            name='estimatedDurationHours'
            render={({ field }) => (
              <FormItem>
                <FormLabel>Estimated Hours</FormLabel>
                <FormControl>
                  <Input
                    type='number'
                    step='0.5'
                    min='0'
                    placeholder='0'
                    {...field}
                    value={field.value || ''}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        {/* Project */}
        <FormField
          control={form.control as any}
          name='projectId'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Project</FormLabel>
              <Select
                onValueChange={(value) =>
                  field.onChange(value === 'none' ? undefined : Number(value))
                }
                value={field.value?.toString() || 'none'}
              >
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder='Select project (optional)' />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectItem value='none'>No Project</SelectItem>
                  {projects.map((project: any) => (
                    <SelectItem key={project.id} value={project.id.toString()}>
                      {project.name || project.title}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Category */}
        <FormField
          control={form.control as any}
          name='category'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Category</FormLabel>
              <FormControl>
                <Input placeholder='e.g. Development, Design' {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Tags */}
        <FormField
          control={form.control as any}
          name='tags'
          render={({ field }) => (
            <FormItem>
              <FormLabel>Tags</FormLabel>
              <FormControl>
                <Input
                  placeholder='Enter tags separated by commas'
                  value={field.value?.join(', ') || ''}
                  onChange={(e) => handleTagsChange(e.target.value)}
                />
              </FormControl>
              <FormDescription>Separate tags with commas</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Deep Work */}
        <FormField
          control={form.control as any}
          name='isDeepWork'
          render={({ field }) => (
            <FormItem className='flex items-center space-x-2'>
              <FormControl>
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              </FormControl>
              <FormLabel className='!mt-0'>Deep Work Task</FormLabel>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Recurring Config */}
        <FormField
          control={form.control as any}
          name='repeatConfig'
          render={({ field }) => (
            <FormItem>
              <RecurringTaskConfig
                value={field.value}
                onChange={field.onChange}
              />
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Submit */}
        <Button type='submit' disabled={isLoading} className='w-full'>
          {isLoading ? 'Saving...' : submitLabel}
        </Button>
      </form>
    </Form>
  );
}
