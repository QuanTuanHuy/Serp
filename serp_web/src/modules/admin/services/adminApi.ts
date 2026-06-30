/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin API barrel exports
 */

// Dashboard API
export {
  dashboardApi,
  useGetAdminDashboardQuery,
} from './dashboard/dashboardApi';

// Global Search API
export {
  globalSearchApi,
  useGetAdminGlobalSearchQuery,
} from './global-search/globalSearchApi';

// Organizations API
export {
  organizationsApi,
  useGetOrganizationsQuery,
  useGetOrganizationByIdQuery,
  useGetOrganizationUserStatsQuery,
  useLazyGetOrganizationsQuery,
  useLazyGetOrganizationByIdQuery,
  useLazyGetOrganizationUserStatsQuery,
  useUpdateOrganizationStatusMutation,
} from './organizations/organizationsApi';

// Users API
export {
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
} from './users/usersApi';

// Departments API
export {
  departmentsApi,
  useGetDepartmentsQuery,
  useLazyGetDepartmentsQuery,
} from './departments/departmentsApi';

// Subscriptions API
export {
  subscriptionsApi,
  useGetSubscriptionsQuery,
  useGetSubscriptionByIdQuery,
  useLazyGetSubscriptionsQuery,
  useLazyGetSubscriptionByIdQuery,
  useActivateSubscriptionMutation,
  useRejectSubscriptionMutation,
  useExpireSubscriptionMutation,
} from './subscriptions/subscriptionsApi';

// Subscription Plans API
export {
  plansApi,
  useGetSubscriptionPlansQuery,
  useGetSubscriptionPlanByIdQuery,
  useCreateSubscriptionPlanMutation,
  useUpdateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,
} from './plans/plansApi';

// Modules API
export {
  modulesApi,
  useGetModulesQuery,
  useGetModuleByIdQuery,
  useCreateModuleMutation,
  useUpdateModuleMutation,
} from './modules/modulesApi';

// Roles API
export {
  rolesApi,
  useGetAllRolesQuery,
  useLazyGetAllRolesQuery,
  useGetRoleByIdQuery,
  useLazyGetRoleByIdQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useAddPermissionsToRoleMutation,
  useDeleteRoleMutation,
} from './roles/rolesApi';

// Menu Displays API
export {
  menuDisplaysApi,
  useGetAllMenuDisplaysQuery,
  useGetMenuDisplaysByModuleQuery,
  useGetMenuDisplaysByRoleIdsQuery,
  useLazyGetAllMenuDisplaysQuery,
  useLazyGetMenuDisplaysByModuleQuery,
  useCreateMenuDisplayMutation,
  useUpdateMenuDisplayMutation,
  useDeleteMenuDisplayMutation,
  useUpdateMenuDisplayRolesMutation,
} from './menu-displays/menuDisplaysApi';
