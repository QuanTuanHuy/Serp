/**
 * PTM v2 - Task Actions Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shared task actions logic
 */

'use client';

import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  useUpdateTaskMutation,
  useDeleteTaskMutation,
  usePromoteSubtaskMutation,
} from '../api';
import type { Task, TaskStatus } from '../types';

interface UseTaskActionsOptions {
  onEditOpen?: (taskId: number) => void;
  onDeleteOpen?: (taskId: number, title: string) => void;
}

export function useTaskActions(options: UseTaskActionsOptions = {}) {
  const router = useRouter();
  const [updateTask] = useUpdateTaskMutation();
  const [deleteTask] = useDeleteTaskMutation();
  const [promoteSubtask] = usePromoteSubtaskMutation();

  const handleToggleComplete = async (
    taskId: number,
    currentStatus: TaskStatus
  ) => {
    const newStatus = currentStatus === 'DONE' ? 'TODO' : 'DONE';
    try {
      await updateTask({ id: taskId, status: newStatus }).unwrap();
      toast.success(newStatus === 'DONE' ? 'Task completed' : 'Task reopened');
    } catch (error) {
      toast.error('Failed to update task');
    }
  };

  const handleStart = async (taskId: number) => {
    try {
      await updateTask({ id: taskId, status: 'IN_PROGRESS' }).unwrap();
      toast.success('Task started');
    } catch (error) {
      toast.error('Failed to start task');
    }
  };

  const handlePause = async (taskId: number) => {
    try {
      await updateTask({ id: taskId, status: 'TODO' }).unwrap();
      toast.success('Task paused');
    } catch (error) {
      toast.error('Failed to pause task');
    }
  };

  const handleEdit = (taskId: number) => {
    options.onEditOpen?.(taskId);
  };

  const handleDelete = async (taskId: number, taskTitle: string) => {
    if (options.onDeleteOpen) {
      options.onDeleteOpen(taskId, taskTitle);
    } else {
      // Direct delete with confirmation
      if (!confirm(`Delete "${taskTitle}"?`)) return;

      try {
        await deleteTask(taskId).unwrap();
        toast.success('Task deleted');
      } catch (error) {
        toast.error('Failed to delete task');
      }
    }
  };

  const handleOpenDetail = (taskId: number) => {
    router.push(`/ptm/tasks/${taskId}`);
  };

  const handlePromote = async (taskId: number) => {
    try {
      await promoteSubtask(taskId).unwrap();
      toast.success('Converted to independent task');
    } catch (error) {
      toast.error('Failed to convert');
    }
  };

  return {
    handleToggleComplete,
    handleStart,
    handlePause,
    handleEdit,
    handleDelete,
    handleOpenDetail,
    handlePromote,
  };
}
