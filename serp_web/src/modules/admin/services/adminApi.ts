/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin API barrel exports
 */

// Organizations API
export {
  organizationsApi,
  useGetOrganizationsQuery,
  useGetOrganizationByIdQuery,
  useLazyGetOrganizationsQuery,
  useLazyGetOrganizationByIdQuery,
} from './organizations/organizationsApi';

// Users API
export {
  usersApi,
  useGetUsersQuery,
  useLazyGetUsersQuery,
  useUpdateUserInfoMutation,
  useCreateUserForOrganizationMutation,
} from './users/usersApi';

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
  useAssignMenuDisplaysToRoleMutation,
  useUnassignMenuDisplaysFromRoleMutation,
} from './menu-displays/menuDisplaysApi';
