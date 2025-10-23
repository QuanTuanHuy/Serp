/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin module barrel exports
 */

// Layout Components
export { AdminLayout } from './components/layout/AdminLayout';
export { AdminSidebar } from './components/layout/AdminSidebar';
export { AdminHeader } from './components/layout/AdminHeader';

// Auth Guard
export { AdminAuthGuard, withAdminAuth } from './components/AdminAuthGuard';

// Contexts
export {
  AdminSidebarProvider,
  useAdminSidebar,
} from './contexts/AdminSidebarContext';

// Shared Components
export { AdminStatsCard } from './components/shared/AdminStatsCard';
export type { AdminStatsCardProps } from './components/shared/AdminStatsCard';

export { AdminStatusBadge } from './components/shared/AdminStatusBadge';
export type { AdminStatusBadgeProps } from './components/shared/AdminStatusBadge';

export { AdminActionMenu } from './components/shared/AdminActionMenu';
export type {
  AdminActionMenuProps,
  AdminActionMenuItem,
} from './components/shared/AdminActionMenu';

// Types
export type {
  Organization,
  OrganizationSubscription,
  SubscriptionPlan,
  Module,
  UserProfile,
  OrganizationFilters,
  SubscriptionFilters,
  ModuleFilters,
  UserFilters,
  OrganizationStatus,
  SubscriptionStatus,
  BillingCycle,
  ModuleStatus,
  UserStatus,
} from './types';

// API Services
export {
  // Organizations
  organizationsApi,
  useGetOrganizationsQuery,
  useGetOrganizationByIdQuery,
  useLazyGetOrganizationsQuery,
  useLazyGetOrganizationByIdQuery,

  // Users
  usersApi,
  useGetUsersQuery,
  useLazyGetUsersQuery,

  // Subscriptions
  subscriptionsApi,
  useGetSubscriptionsQuery,
  useGetSubscriptionByIdQuery,
  useLazyGetSubscriptionsQuery,
  useLazyGetSubscriptionByIdQuery,

  // Subscription Plans
  plansApi,
  useGetSubscriptionPlansQuery,
  useGetSubscriptionPlanByIdQuery,
  useCreateSubscriptionPlanMutation,
  useUpdateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,

  // Modules
  modulesApi,
  useGetModulesQuery,
  useGetModuleByIdQuery,
  useCreateModuleMutation,
  useUpdateModuleMutation,
} from './services/adminApi';
