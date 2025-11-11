/**
 * Shared Hooks Barrel Exports
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

export { useTheme } from './use-theme';
export { useLocalStorage } from './use-local-storage';
export { useNotification } from './use-notification';
export { useColumnVisibility } from './use-column-visibility';
export { useDebounce } from './use-debounce';
export {
  useAppDispatch,
  useAppSelector,
  useAppLoading,
  useAppErrors,
} from './redux';
export { useModuleSidebar } from './useModuleSidebar';
export { useModuleRouteGuard } from './useModuleRouteGuard';

// Export types
export type { NotificationType, NotificationOptions } from './use-notification';
export type { SidebarMenuItem } from './useModuleSidebar';
