/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication services exports
 */

export { authApi } from './authApi';
export {
  useRegisterMutation,
  useLoginMutation,
  useGetTokenMutation,
  useRefreshTokenMutation,
  useRevokeTokenMutation,
  useValidatePasswordResetTokenQuery,
  useConfirmPasswordResetMutation,
} from './authApi';

export {
  useGetCurrentUserQuery,
  useLazyGetCurrentUserQuery,
  useGetUserPermissionsQuery,
  useGetUserMenusQuery,
} from './userApi';

export { moduleApi, useGetMyModulesQuery } from './moduleApi';

export {
  menuApi,
  useGetMenuDisplaysByModuleAndUserQuery,
  useLazyGetMenuDisplaysByModuleAndUserQuery,
} from './menuApi';
