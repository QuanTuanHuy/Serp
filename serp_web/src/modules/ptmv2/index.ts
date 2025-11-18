/**
 * PTM v2 - Module Index
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Main module exports
 */

// Components
export * from './components/layout';
export * from './components/shared';
export * from './components/dashboard';
export * from './components/tasks';
export * from './components/projects';
export * from './components/schedule';

// Hooks
export * from './hooks';

// Store
export { ptmReducer } from './store';
export {
  uiSlice,
  selectUI,
  selectActiveView,
  selectSidebarState,
} from './store/uiSlice';

// Services
export { ptmApi, taskApi, projectApi, scheduleApi } from './services';

// Types
export type * from './types';

// Constants
export { PTM_COLORS, LAYOUT_CONSTANTS } from './constants/colors';
