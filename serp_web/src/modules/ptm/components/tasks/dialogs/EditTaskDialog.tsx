/**
 * PTM - Edit Task Dialog
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dialog for editing existing tasks
 */

'use client';

import { useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { Skeleton } from '@/shared/components/ui';
import { TaskForm, TaskFormValues } from '../forms/TaskForm';
import { useGetTaskQuery, useUpdateTaskMutation } from '../../../api';
import { toast } from 'sonner';

interface EditTaskDialogProps {
  taskId: number | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function EditTaskDialog({
  taskId,
  open,
  onOpenChange,
}: EditTaskDialogProps) {
  const { data: task, isLoading: isFetching } = useGetTaskQuery(taskId!, {
    skip: !taskId,
  });

  const [updateTask, { isLoading }] = useUpdateTaskMutation();

  const handleSubmit = async (formData: TaskFormValues) => {
    if (!taskId) return;

    try {
      await updateTask({
        ...formData,
        id: taskId,
      }).unwrap();

      toast.success('Task updated successfully');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to update task');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[600px] max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Edit Task</DialogTitle>
        </DialogHeader>

        {isFetching ? (
          <div className='space-y-4'>
            <Skeleton className='h-10 w-full' />
            <Skeleton className='h-20 w-full' />
            <Skeleton className='h-10 w-full' />
            <Skeleton className='h-10 w-full' />
          </div>
        ) : task ? (
          <TaskForm
            defaultValues={task}
            onSubmit={handleSubmit}
            isLoading={isLoading}
            submitLabel='Update Task'
          />
        ) : (
          <p className='text-sm text-muted-foreground'>Task not found</p>
        )}
      </DialogContent>
    </Dialog>
  );
}
