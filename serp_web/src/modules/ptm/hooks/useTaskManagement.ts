/**
 * PTM - Task Management Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Centralized task CRUD operations with selection and bulk actions
 */

'use client';

import { useState, useCallback } from 'react';
import {
  useGetTasksQuery,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
} from '../api';
import { toast } from 'sonner';
import type { TaskStatus, TaskPriority } from '../types';

// TODO: Implement bulk delete in API
// import { useBulkDeleteTasksMutation } from '../api';

export interface UseTaskManagementOptions {
  projectId?: number;
  parentTaskId?: number;
  status?: TaskStatus;
  priority?: TaskPriority;
  initialPageSize?: number;
}

export function useTaskManagement(options: UseTaskManagementOptions = {}) {
  const {
    projectId,
    parentTaskId,
    status,
    priority,
    initialPageSize = 20,
  } = options;

  const [page, setPage] = useState(1);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  // RTK Query
  const { data, isLoading, error, refetch } = useGetTasksQuery({
    projectId,
    parentTaskId,
    status,
    priority,
    page,
    pageSize: initialPageSize,
  });

  const [updateTask] = useUpdateTaskMutation();
  // TODO: Implement bulk delete mutation in API
  // const [bulkDelete] = useBulkDeleteTasksMutation();

  const tasks = data?.data?.items || [];
  const pagination = {
    currentPage: data?.data?.currentPage || 1,
    totalPages: data?.data?.totalPages || 0,
    totalItems: data?.data?.totalItems || 0,
  };

  // Quick status updates
  const handleToggleComplete = useCallback(
    async (taskId: number, currentStatus: TaskStatus) => {
      try {
        await updateTask({
          id: taskId,
          status: currentStatus === 'DONE' ? 'TODO' : 'DONE',
          progressPercentage: currentStatus === 'DONE' ? 0 : 100,
        }).unwrap();

        toast.success(
          currentStatus === 'DONE'
            ? 'Task marked as incomplete'
            : 'Task completed!'
        );
      } catch (error) {
        toast.error('Failed to update task');
      }
    },
    [updateTask]
  );

  const handleStart = useCallback(
    async (taskId: number) => {
      try {
        await updateTask({
          id: taskId,
          status: 'IN_PROGRESS',
        }).unwrap();
        toast.success('Task started!');
      } catch (error) {
        toast.error('Failed to start task');
      }
    },
    [updateTask]
  );

  const handlePause = useCallback(
    async (taskId: number) => {
      try {
        await updateTask({
          id: taskId,
          status: 'TODO',
        }).unwrap();
        toast.success('Task paused');
      } catch (error) {
        toast.error('Failed to pause task');
      }
    },
    [updateTask]
  );

  // Bulk operations
  // TODO: Implement when bulk delete API is ready
  const handleBulkDelete = useCallback(async () => {
    if (selectedIds.length === 0) {
      toast.warning('No tasks selected');
      return;
    }

    toast.info('Bulk delete not yet implemented');
    // try {
    //   await bulkDelete(selectedIds).unwrap();
    //   toast.success(`${selectedIds.length} tasks deleted`);
    //   setSelectedIds([]);
    //   refetch();
    // } catch (error) {
    //   toast.error('Failed to delete tasks');
    // }
  }, [selectedIds, refetch]);

  // Selection management
  const toggleSelection = useCallback((taskId: number) => {
    setSelectedIds((prev) =>
      prev.includes(taskId)
        ? prev.filter((id) => id !== taskId)
        : [...prev, taskId]
    );
  }, []);

  const selectAll = useCallback(() => {
    const allIds = tasks.map((t) => t.id);
    setSelectedIds(allIds);
  }, [tasks]);

  const clearSelection = useCallback(() => {
    setSelectedIds([]);
  }, []);

  return {
    // Data
    tasks,
    pagination,
    isLoading,
    error,

    // Pagination
    page,
    setPage,

    // Selection
    selectedIds,
    toggleSelection,
    selectAll,
    clearSelection,
    hasSelection: selectedIds.length > 0,

    // Quick actions
    handleToggleComplete,
    handleStart,
    handlePause,

    // Bulk actions
    handleBulkDelete,

    // Refetch
    refetch,
  };
}
