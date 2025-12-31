/**
 * PTM - Delete Task Dialog
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Confirmation dialog for deleting tasks
 */

'use client';

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/shared/components/ui';
import { useDeleteTaskMutation, useGetTaskQuery } from '../../../api';
import { toast } from 'sonner';
import type { Task } from '../../../types';

interface DeleteTaskDialogProps {
  taskId: number | null;
  taskTitle?: string;
  task?: Task | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess?: () => void;
}

/**
 * DeleteTaskDialog - Flexible API for different use cases
 *
 * Use Case 1 - With full task object (from useTaskDetail):
 * <DeleteTaskDialog task={task} open={...} onOpenChange={...} />
 *
 * Use Case 2 - With taskId only (from useTaskDialogs):
 * <DeleteTaskDialog taskId={taskId} taskTitle={title} open={...} onOpenChange={...} />
 */
export function DeleteTaskDialog({
  taskId,
  taskTitle,
  task: providedTask,
  open,
  onOpenChange,
  onSuccess,
}: DeleteTaskDialogProps) {
  // If task object provided, use it. Otherwise query by taskId
  const { data: queriedTask } = useGetTaskQuery(taskId!, {
    skip: !taskId || !!providedTask,
  });

  const task = providedTask || queriedTask;
  const [deleteTask, { isLoading: isDeleting }] = useDeleteTaskMutation();

  const handleConfirm = async () => {
    if (!task) return;

    try {
      await deleteTask(task.id).unwrap();
      toast.success('Task deleted');
      onOpenChange(false);
      onSuccess?.();
    } catch (error) {
      toast.error('Failed to delete task');
    }
  };

  if (!task && !taskTitle) return null;

  const displayTitle = task?.title || taskTitle || 'this task';
  const hasSubtasks = task?.hasSubtasks || (task?.totalSubtaskCount || 0) > 0;
  const subtaskCount = task?.totalSubtaskCount || 0;

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Delete Task</AlertDialogTitle>
          <AlertDialogDescription className='space-y-2'>
            <p>
              Are you sure you want to delete{' '}
              <span className='font-semibold text-foreground'>
                &quot;{displayTitle}&quot;
              </span>
              ?
            </p>
            {hasSubtasks && (
              <p className='text-destructive font-medium'>
                ⚠️ This task has {subtaskCount} subtask(s). They will also be
                deleted.
              </p>
            )}
            <p className='text-xs text-muted-foreground'>
              This action cannot be undone.
            </p>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
          <AlertDialogAction
            onClick={handleConfirm}
            disabled={isDeleting}
            className='bg-destructive text-destructive-foreground hover:bg-destructive/90'
          >
            {isDeleting ? 'Deleting...' : 'Delete'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
