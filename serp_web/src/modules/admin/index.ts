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
export { AdminFilterDialog } from './components/shared/AdminFilterDialog';
export type {
  AdminFilterCriterion,
  AdminFilterDialogProps,
} from './components/shared/AdminFilterDialog';
export { AdminFilterChips } from './components/shared/AdminFilterChips';
export type {
  AdminFilterChip,
  AdminFilterChipsProps,
} from './components/shared/AdminFilterChips';
export { AdminConfirmStatusDialog } from './components/shared/AdminConfirmStatusDialog';
export type { AdminConfirmStatusDialogProps } from './components/shared/AdminConfirmStatusDialog';

// Organization Components
export { OrganizationDetailsDrawer } from './components/organizations/OrganizationDetailsDrawer';
export { OrganizationStatusDialog } from './components/organizations/OrganizationStatusDialog';
export { OrganizationUsersPreview } from './components/organizations/OrganizationUsersPreview';

// Role Components
export { RoleForm, RoleFormDialog } from './components/roles';

// Menu Display Components
export {
  IconPicker,
  MenuDisplayTree,
  MenuDisplayFormDialog,
} from './components/menu-displays';

// Hooks
export { usePlans } from './hooks/usePlans';
export type { UsePlansReturn } from './hooks/usePlans';
export { useModules } from './hooks/useModules';
export type { UseModulesReturn } from './hooks/useModules';
export { useOrganizations } from './hooks/useOrganizations';
export type { UseOrganizationsReturn } from './hooks/useOrganizations';
export { useRoles } from './hooks/useRoles';
export type { UseRolesReturn } from './hooks/useRoles';
export { useSubscriptions } from './hooks/useSubscriptions';
export type { UseSubscriptionsReturn } from './hooks/useSubscriptions';
export { useUsers } from './hooks/useUsers';
export { useMenuDisplays } from './hooks/useMenuDisplays';

// Types
export type {
  Organization,
  OrganizationStatusUpdateResponse,
  OrganizationSubscription,
  SubscriptionPlan,
  Module,
  UserProfile,
  UserStats,
  UserDetailResponse,
  DepartmentOption,
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
  OrganizationType,
  SubscriptionStatus,
  BillingCycle,
  ModuleStatus,
  UserStatus,
  UserType,
  RoleScope,
  RoleType,
  AdminDashboard,
  AdminDashboardActionQueue,
  AdminDashboardConfigurationCoverage,
  AdminDashboardMetrics,
  AdminDashboardRecentOrganization,
  AdminDashboardStatusCount,
  MenuType,
  MenuRoleInfo,
  MenuDisplayDetail,
  MenuDisplayTreeNode,
  CreateMenuDisplayRequest,
  UpdateMenuDisplayRequest,
  AssignMenuDisplayToRoleRequest,
  GetMenuDisplayParams,
  MenuDisplayFilters,
  MenuDisplayStats,
} from './types';

// API Services
export {
  // Dashboard
  dashboardApi,
  useGetAdminDashboardQuery,

  // Organizations
  organizationsApi,
  useGetOrganizationsQuery,
  useGetOrganizationByIdQuery,
  useGetOrganizationUserStatsQuery,
  useLazyGetOrganizationsQuery,
  useLazyGetOrganizationByIdQuery,
  useLazyGetOrganizationUserStatsQuery,
  useUpdateOrganizationStatusMutation,

  // Users
  usersApi,
  useGetUsersQuery,
  useLazyGetUsersQuery,
  useGetUserStatsQuery,
  useGetUserDetailQuery,
  useLazyGetUserDetailQuery,
  useGetOrganizationRolesQuery,
  useUpdateUserInfoMutation,
  useUpdateUserStatusMutation,
  useUpdateUserRolesMutation,
  useUpdateUserTypeMutation,
  useCreateUserForOrganizationMutation,

  // Departments
  departmentsApi,
  useGetDepartmentsQuery,
  useLazyGetDepartmentsQuery,

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

  // Menu Displays
  menuDisplaysApi,
  useGetAllMenuDisplaysQuery,
  useGetMenuDisplaysByModuleQuery,
  useGetMenuDisplaysByRoleIdsQuery,
  useCreateMenuDisplayMutation,
  useUpdateMenuDisplayMutation,
  useDeleteMenuDisplayMutation,
  useAssignMenuDisplaysToRoleMutation,
  useUnassignMenuDisplaysFromRoleMutation,
} from './services/adminApi';

// Users UI Components
export { UserDialog } from './components/users/UserDialog';
export { UserDetailsDrawer } from './components/users/UserDetailsDrawer';
export { UserAccessDialog } from './components/users/UserAccessDialog';
export { UserStatusDialog } from './components/users/UserStatusDialog';
