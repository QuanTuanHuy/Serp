/**
 * PTM - Task Dialogs Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Reusable hook for managing task dialog states
 */

'use client';

import { useState } from 'react';
import type { Task } from '../types';

export interface TaskWithTitle {
  id: number;
  title?: string;
}

export function useTaskDialogs<T extends TaskWithTitle = Task>() {
  const [createOpen, setCreateOpen] = useState(false);
  const [editTaskId, setEditTaskId] = useState<number | null>(null);
  const [deleteTask, setDeleteTask] = useState<{
    id: number | null;
    title?: string;
  }>({ id: null });

  // Create dialog
  const openCreate = () => setCreateOpen(true);
  const closeCreate = () => setCreateOpen(false);

  // Edit dialog
  const openEdit = (taskId: number) => setEditTaskId(taskId);
  const closeEdit = () => setEditTaskId(null);

  // Delete dialog
  const openDelete = (taskId: number, taskTitle?: string) => {
    setDeleteTask({ id: taskId, title: taskTitle });
  };
  const closeDelete = () => setDeleteTask({ id: null });

  return {
    // Create dialog state
    createDialog: {
      open: createOpen,
      onOpenChange: setCreateOpen,
      openCreate,
      closeCreate,
    },

    // Edit dialog state
    editDialog: {
      taskId: editTaskId,
      open: !!editTaskId,
      onOpenChange: (open: boolean) => !open && closeEdit(),
      openEdit,
      closeEdit,
    },

    // Delete dialog state
    deleteDialog: {
      taskId: deleteTask.id,
      taskTitle: deleteTask.title,
      open: !!deleteTask.id,
      onOpenChange: (open: boolean) => !open && closeDelete(),
      openDelete,
      closeDelete,
    },
  };
}
