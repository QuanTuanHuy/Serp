/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings API barrel exports
 */

// // General Settings API
// export {
//   generalApi,
//   useGetOrganizationProfileQuery,
//   useUpdateOrganizationProfileMutation,
//   useGetOrganizationBrandingQuery,
//   useUpdateOrganizationBrandingMutation,
//   useGetOrganizationPreferencesQuery,
//   useUpdateOrganizationPreferencesMutation,
// } from './general/generalApi';

// Users API
export {
  settingsUsersApi,
  useGetOrganizationUsersQuery,
  useLazyGetOrganizationUsersQuery,
  useSettingsCreateUserForOrganizationMutation,
  useUpdateOrganizationUserMutation,
} from './users/usersApi';

// // Departments API
// export {
//   departmentsApi,
//   useGetDepartmentsQuery,
//   useGetDepartmentByIdQuery,
//   useCreateDepartmentMutation,
//   useUpdateDepartmentMutation,
//   useDeleteDepartmentMutation,
//   useGetDepartmentMembersQuery,
//   useAddDepartmentMemberMutation,
//   useRemoveDepartmentMemberMutation,
// } from './departments/departmentsApi';

// // Module Access API
// export {
//   moduleAccessApi,
//   useGetOrganizationModulesQuery,
//   useGetModuleAccessSettingsQuery,
//   useUpdateModuleAccessSettingsMutation,
// } from './modules/moduleAccessApi';

// // Subscription API
// export {
//   settingsSubscriptionApi,
//   useGetOrganizationSubscriptionQuery,
//   useGetInvoicesQuery,
//   useUpgradeSubscriptionMutation,
//   useCancelSubscriptionMutation,
// } from './subscription/subscriptionApi';

// // Security API
// export {
//   securityApi,
//   useGetSecuritySettingsQuery,
//   useUpdateSecuritySettingsMutation,
//   useGetAuditLogsQuery,
//   useGetApiKeysQuery,
//   useCreateApiKeyMutation,
//   useUpdateApiKeyMutation,
//   useDeleteApiKeyMutation,
// } from './security/securityApi';

// // Integrations API
// export {
//   integrationsApi,
//   useGetIntegrationsQuery,
//   useGetAvailableIntegrationsQuery,
//   useConfigureIntegrationMutation,
//   useUpdateIntegrationMutation,
//   useDeleteIntegrationMutation,
// } from './integrations/integrationsApi';

// // Roles API (Organization-level)

// Organizations API (me)
export {
  settingsOrganizationsApi,
  useGetMyOrganizationQuery,
  useLazyGetMyOrganizationQuery,
} from './organizations/organizationsApi';
// export {
//   settingsRolesApi,
//   useGetOrganizationRolesQuery,
//   useGetOrganizationRoleByIdQuery,
//   useCreateOrganizationRoleMutation,
//   useUpdateOrganizationRoleMutation,
//   useDeleteOrganizationRoleMutation,
// } from './roles/rolesApi';
