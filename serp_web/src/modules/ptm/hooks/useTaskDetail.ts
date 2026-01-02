/**
 * PTM - Task Detail Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task detail operations (update, delete, copy link)
 */

'use client';

import { useState } from 'react';
import {
  useGetTaskQuery,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
} from '../api';
import { toast } from 'sonner';
import type { UpdateTaskRequest } from '../types';

export interface UseTaskDetailOptions {
  taskId: number | null;
  onDeleteSuccess?: () => void;
}

/**
 * Centralizes task detail operations
 *
 * @example
 * const { task, isLoading, handleUpdate, handleDelete, handleCopyLink } = useTaskDetail({
 *   taskId,
 *   onDeleteSuccess: () => router.back()
 * });
 */
export function useTaskDetail({
  taskId,
  onDeleteSuccess,
}: UseTaskDetailOptions) {
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const { data: task, isLoading } = useGetTaskQuery(taskId!, {
    skip: !taskId,
  });

  const [updateTask, { isLoading: isUpdating }] = useUpdateTaskMutation();
  const [deleteTask, { isLoading: isDeleting }] = useDeleteTaskMutation();

  const handleUpdate = async (data: Partial<UpdateTaskRequest>) => {
    if (!taskId) return;

    try {
      await updateTask({
        id: taskId,
        ...data,
      }).unwrap();

      toast.success('Task updated successfully');
    } catch (error) {
      toast.error('Failed to update task');
      throw error;
    }
  };

  const openDeleteDialog = () => {
    setDeleteDialogOpen(true);
  };

  const handleDelete = async () => {
    if (!taskId) return;

    try {
      await deleteTask(taskId).unwrap();
      toast.success('Task deleted');
      setDeleteDialogOpen(false);
      onDeleteSuccess?.();
    } catch (error) {
      toast.error('Failed to delete task');
      throw error;
    }
  };

  const handleCopyLink = async () => {
    if (!taskId) return;

    const url = `${window.location.origin}/ptm/tasks/${taskId}`;
    try {
      await navigator.clipboard.writeText(url);
      toast.success('Link copied to clipboard');
    } catch (error) {
      toast.error('Failed to copy link');
    }
  };

  return {
    task,
    isLoading,
    isUpdating,
    isDeleting,
    deleteDialogOpen,
    setDeleteDialogOpen,
    openDeleteDialog,
    handleUpdate,
    handleDelete,
    handleCopyLink,
  };
}
