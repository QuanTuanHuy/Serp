/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings module barrel exports
 */

// Layout Components
export { SettingsLayout } from './components/layout/SettingsLayout';
export { SettingsSidebar } from './components/layout/SettingsSidebar';
export { SettingsHeader } from './components/layout/SettingsHeader';

// Auth Guard
export {
  SettingsAuthGuard,
  withSettingsAuth,
} from './components/SettingsAuthGuard';

// Contexts
export {
  SettingsSidebarProvider,
  useSettingsSidebar,
} from './contexts/SettingsSidebarContext';

// Shared Components
export { SettingsStatsCard } from './components/shared/SettingsStatsCard';
export type { SettingsStatsCardProps } from './components/shared/SettingsStatsCard';

export { SettingsStatusBadge } from './components/shared/SettingsStatusBadge';
export type { SettingsStatusBadgeProps } from './components/shared/SettingsStatusBadge';

export { SettingsActionMenu } from './components/shared/SettingsActionMenu';
export type {
  SettingsActionMenuProps,
  SettingsActionMenuItem,
} from './components/shared/SettingsActionMenu';

// Types
export type * from './types';
