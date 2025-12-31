/**
 * PTM v2 - Hooks Index
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Barrel exports for hooks
 */

export { useDashboardStats } from './useDashboardStats';
export type { DashboardStats } from './useDashboardStats';

export { useProjects } from './useProjects';
export { useProjectStats } from './useProjectStats';
export { useProjectForm } from './useProjectForm';

export { useTasks } from './useTasks';
export type { UseTasksOptions, UseTasksResult } from './useTasks';

export { useTaskDialogs } from './useTaskDialogs';
export type { TaskWithTitle } from './useTaskDialogs';

export { useTaskManagement } from './useTaskManagement';
export type { UseTaskManagementOptions } from './useTaskManagement';

export { useTaskActions } from './useTaskActions';

// Task detail operations
export { useTaskDetail } from './useTaskDetail';
export type { UseTaskDetailOptions } from './useTaskDetail';

// Note operations
export { useNoteOperations } from './useNoteOperations';
export type { UseNoteOperationsOptions } from './useNoteOperations';

// Task edit form
export { useTaskEditForm } from './useTaskEditForm';

// Task card operations
export { useTaskCardActions } from './useTaskCardActions';
export { useTaskSubtasks } from './useTaskSubtasks';

// Keyboard shortcuts (optional enhancement)
export {
  useKeyboardShortcuts,
  useGlobalShortcuts,
  useConditionalShortcuts,
  formatShortcut,
} from './useKeyboardShortcuts';
export type { KeyboardShortcut } from './useKeyboardShortcuts';
