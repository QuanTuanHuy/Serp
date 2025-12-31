/**
 * PTM - Task Card Actions Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task card action handlers
 */

'use client';

import { useRouter } from 'next/navigation';
import type { Task, TaskStatus } from '../types';

interface UseTaskCardActionsParams {
  task: Task;
  onClick?: (taskId: number) => void;
  onNavigate?: boolean;
  onToggleComplete?: (taskId: number, currentStatus: TaskStatus) => void;
  onStart?: (taskId: number) => void;
  onPause?: (taskId: number) => void;
  onEdit?: (taskId: number) => void;
  onDelete?: (task: Task) => void;
}

/**
 * Centralizes task card action handlers
 *
 * @example
 * const { handleCardClick, handleToggleComplete, handleStart, ... } = useTaskCardActions({
 *   task,
 *   onClick,
 *   onNavigate,
 *   ...handlers
 * });
 */
export function useTaskCardActions({
  task,
  onClick,
  onNavigate = false,
  onToggleComplete,
  onStart,
  onPause,
  onEdit,
  onDelete,
}: UseTaskCardActionsParams) {
  const router = useRouter();

  const handleToggleComplete = () => {
    onToggleComplete?.(task.id, task.status);
  };

  const handleStart = (e: React.MouseEvent) => {
    e.stopPropagation();
    onStart?.(task.id);
  };

  const handlePause = (e: React.MouseEvent) => {
    e.stopPropagation();
    onPause?.(task.id);
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(task.id);
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete?.(task);
  };

  const handleCardClick = (e: React.MouseEvent) => {
    // Cmd/Ctrl + Click: Open in new tab
    if (e.metaKey || e.ctrlKey) {
      e.preventDefault();
      window.open(`/ptm/tasks/${task.id}`, '_blank');
      return;
    }

    // Normal click: Open panel or navigate
    if (onNavigate) {
      router.push(`/ptm/tasks/${task.id}`);
    } else {
      onClick?.(task.id);
    }
  };

  const handleOpenDetail = (e: React.MouseEvent) => {
    e.stopPropagation();
    router.push(`/ptm/tasks/${task.id}`);
  };

  return {
    handleCardClick,
    handleToggleComplete,
    handleStart,
    handlePause,
    handleEdit,
    handleDelete,
    handleOpenDetail,
  };
}
