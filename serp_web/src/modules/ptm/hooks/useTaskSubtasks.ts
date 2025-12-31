/**
 * PTM - Task Subtasks Hook
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Get subtask statistics from task entity
 */

'use client';

import type { Task } from '../types';

interface UseTaskSubtasksParams {
  task: Task | undefined;
}

/**
 * Returns subtask statistics directly from task entity
 * No longer needs to query all tasks - backend provides this data
 *
 * @example
 * const { completedSubtasks, totalSubtasks, subtasksProgress } = useTaskSubtasks({
 *   task
 * });
 */
export function useTaskSubtasks({ task }: UseTaskSubtasksParams) {
  if (!task) {
    return {
      completedSubtasks: 0,
      totalSubtasks: 0,
      subtasksProgress: 0,
      hasSubtasks: false,
    };
  }

  const completedSubtasks = task.completedSubtaskCount || 0;
  const totalSubtasks = task.totalSubtaskCount || 0;
  const subtasksProgress =
    totalSubtasks > 0 ? (completedSubtasks / totalSubtasks) * 100 : 0;

  return {
    completedSubtasks,
    totalSubtasks,
    subtasksProgress,
    hasSubtasks: task.hasSubtasks || totalSubtasks > 0,
  };
}
