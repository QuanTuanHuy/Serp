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

// Role Components
export { RoleForm, RoleFormDialog } from './components/roles';

// Hooks
export { usePlans } from './hooks/usePlans';
export type { UsePlansReturn } from './hooks/usePlans';
export { useOrganizations } from './hooks/useOrganizations';
export type { UseOrganizationsReturn } from './hooks/useOrganizations';
export { useRoles } from './hooks/useRoles';
export type { UseRolesReturn } from './hooks/useRoles';
export { useSubscriptions } from './hooks/useSubscriptions';
export type { UseSubscriptionsReturn } from './hooks/useSubscriptions';

// Types
export type {
  Organization,
  OrganizationSubscription,
  SubscriptionPlan,
  Module,
  UserProfile,
  Role,
  Permission,
  CreateRoleRequest,
  UpdateRoleRequest,
  OrganizationFilters,
  SubscriptionFilters,
  ModuleFilters,
  UserFilters,
  RoleFilters,
  OrganizationStatus,
  SubscriptionStatus,
  BillingCycle,
  ModuleStatus,
  UserStatus,
  RoleScope,
  RoleType,
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
  useActivateSubscriptionMutation,
  useRejectSubscriptionMutation,
  useExpireSubscriptionMutation,

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

  // Roles
  rolesApi,
  useGetAllRolesQuery,
  useGetRoleByIdQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useAddPermissionsToRoleMutation,
  useDeleteRoleMutation,
} from './services/adminApi';
