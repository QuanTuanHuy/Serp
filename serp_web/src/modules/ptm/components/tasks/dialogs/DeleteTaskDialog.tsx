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
import { useDeleteTaskMutation } from '../../../api';
import { toast } from 'sonner';

interface DeleteTaskDialogProps {
  taskId: number | null;
  taskTitle?: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DeleteTaskDialog({
  taskId,
  taskTitle,
  open,
  onOpenChange,
}: DeleteTaskDialogProps) {
  const [deleteTask, { isLoading }] = useDeleteTaskMutation();

  const handleDelete = async () => {
    if (!taskId) return;

    try {
      await deleteTask(taskId).unwrap();
      toast.success('Task deleted successfully');
      onOpenChange(false);
    } catch (error) {
      toast.error('Failed to delete task');
    }
  };

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Delete Task?</AlertDialogTitle>
          <AlertDialogDescription>
            {taskTitle ? (
              <>
                This will permanently delete{' '}
                <strong>&quot;{taskTitle}&quot;</strong>. This action cannot be
                undone.
              </>
            ) : (
              <>
                This will permanently delete this task. This action cannot be
                undone.
              </>
            )}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancel</AlertDialogCancel>
          <AlertDialogAction
            onClick={handleDelete}
            disabled={isLoading}
            className='bg-red-600 hover:bg-red-700'
          >
            {isLoading ? 'Deleting...' : 'Delete Task'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
