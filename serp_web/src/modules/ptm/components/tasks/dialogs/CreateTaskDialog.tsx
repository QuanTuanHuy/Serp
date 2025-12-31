/**
 * PTM - Create Task Dialog
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Dialog for creating new tasks
 */

'use client';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/shared/components/ui';
import { TaskForm } from '../forms/TaskForm';
import { useCreateTaskMutation } from '../../../api';
import { toast } from 'sonner';
import type { CreateTaskRequest } from '../../../types';

interface CreateTaskDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  projectId?: number;
  parentTaskId?: number;
}

export function CreateTaskDialog({
  open,
  onOpenChange,
  projectId,
  parentTaskId,
}: CreateTaskDialogProps) {
  const [createTask, { isLoading }] = useCreateTaskMutation();

  const handleSubmit = async (data: CreateTaskRequest) => {
    try {
      await createTask({
        ...data,
        projectId: data.projectId || projectId,
        parentTaskId: data.parentTaskId || parentTaskId,
      }).unwrap();

      toast.success('Task created successfully');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to create task');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[600px] max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Create New Task</DialogTitle>
          <DialogDescription>
            Add a new task to your workspace. Fill in the details below.
          </DialogDescription>
        </DialogHeader>
        <TaskForm
          onSubmit={handleSubmit}
          isLoading={isLoading}
          submitLabel='Create Task'
          defaultValues={{
            projectId,
            parentTaskId,
          }}
        />
      </DialogContent>
    </Dialog>
  );
}
