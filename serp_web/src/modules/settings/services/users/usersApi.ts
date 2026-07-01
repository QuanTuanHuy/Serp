/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings Users API endpoints (organization-scoped)
 */

import { api } from '@/lib/store/api';
import {
  createPaginatedTransform,
  createRtkTransformResponse,
  createApiResponseTransform,
} from '@/lib/store/api/utils';
import type { ApiResponse, PaginatedResponse } from '@/lib/store/api/types';
import type {
  UserProfile,
  UsersResponse,
  UserResponse,
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
  Role,
} from '@/modules/admin/types';
import type {
  UserStats,
  UserDetail,
  SettingsUserFilters,
  InviteUserRequest,
  UserInvitation,
  InvitationFilters,
  UpdateUserRolesRequest,
  UpdateUserTypeRequest,
} from '../../types/user.types';

const buildUserQueryParams = (filters: SettingsUserFilters): string => {
  const params = new URLSearchParams();
  if (filters.search) params.append('search', filters.search);
  if (filters.status) {
    params.append(
      'status',
      filters.status === 'PENDING' ? 'INVITED' : filters.status
    );
  }
  if (filters.userType) params.append('userType', filters.userType);
  if (filters.roleId !== undefined)
    params.append('roleId', String(filters.roleId));
  if (filters.departmentId !== undefined)
    params.append('departmentId', String(filters.departmentId));
  if (filters.moduleId !== undefined)
    params.append('moduleId', String(filters.moduleId));
  if (filters.page !== undefined) params.append('page', String(filters.page));
  if (filters.pageSize !== undefined)
    params.append('pageSize', String(filters.pageSize));
  if (filters.sortBy) params.append('sortBy', filters.sortBy);
  if (filters.sortDir) params.append('sortDir', filters.sortDir);
  return params.toString();
};

export const settingsUsersApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ==================== Users ====================

    getOrganizationUsers: builder.query<UsersResponse, SettingsUserFilters>({
      query: (filters) => ({
        url: `/users?organizationId=${filters.organizationId}&${buildUserQueryParams(filters)}`,
        method: 'GET',
      }),
      transformResponse: createPaginatedTransform<UserProfile>(),
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'settings/User' as const,
                id,
              })),
              { type: 'settings/User', id: 'LIST' },
            ]
          : [{ type: 'settings/User', id: 'LIST' }],
    }),

    getSettingsOrganizationRoles: builder.query<
      ApiResponse<Role[]>,
      { organizationId: number }
    >({
      query: ({ organizationId }) => ({
        url: `/organizations/${organizationId}/roles`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: [{ type: 'settings/User', id: 'ROLES' }],
    }),

    getSettingsUserStats: builder.query<
      ApiResponse<UserStats>,
      { organizationId: number }
    >({
      query: ({ organizationId }) => ({
        url: `/organizations/${organizationId}/users/stats`,
        method: 'GET',
      }),
      transformResponse: createApiResponseTransform<UserStats>(),
      providesTags: [{ type: 'settings/User', id: 'STATS' }],
    }),

    getUserDetail: builder.query<
      ApiResponse<UserDetail>,
      { organizationId: number; userId: number }
    >({
      query: ({ organizationId, userId }) => ({
        url: `/organizations/${organizationId}/users/${userId}/detail`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_res, _err, { userId }) => [
        { type: 'settings/User', id: `detail-${userId}` },
      ],
    }),

    updateUserStatus: builder.mutation<
      UserResponse,
      { organizationId: number; userId: number; status: string }
    >({
      query: ({ organizationId, userId, status }) => ({
        url: `/organizations/${organizationId}/users/${userId}/status`,
        method: 'PATCH',
        body: { status },
      }),
      invalidatesTags: (_res, _err, { userId }) => [
        { type: 'settings/User', id: userId },
        { type: 'settings/User', id: `detail-${userId}` },
        { type: 'settings/User', id: 'LIST' },
        { type: 'settings/User', id: 'STATS' },
      ],
    }),

    updateUserRoles: builder.mutation<
      UserResponse,
      { organizationId: number; userId: number; body: UpdateUserRolesRequest }
    >({
      query: ({ organizationId, userId, body }) => ({
        url: `/organizations/${organizationId}/users/${userId}/roles`,
        method: 'PUT',
        body,
      }),
      invalidatesTags: (_res, _err, { userId }) => [
        { type: 'settings/User', id: userId },
        { type: 'settings/User', id: `detail-${userId}` },
        { type: 'settings/User', id: 'LIST' },
      ],
    }),

    updateUserType: builder.mutation<
      UserResponse,
      { organizationId: number; userId: number; body: UpdateUserTypeRequest }
    >({
      query: ({ organizationId, userId, body }) => ({
        url: `/organizations/${organizationId}/users/${userId}/type`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (_res, _err, { userId }) => [
        { type: 'settings/User', id: userId },
        { type: 'settings/User', id: `detail-${userId}` },
        { type: 'settings/User', id: 'LIST' },
      ],
    }),

    resetUserPassword: builder.mutation<
      ApiResponse<void>,
      { organizationId: number; userId: number }
    >({
      query: ({ organizationId, userId }) => ({
        url: `/organizations/${organizationId}/users/${userId}/reset-password`,
        method: 'POST',
      }),
    }),

    exportUsers: builder.query<
      Blob,
      { organizationId: number; format?: string }
    >({
      query: ({ organizationId, format = 'csv' }) => ({
        url: `/organizations/${organizationId}/users/export?format=${format}`,
        method: 'GET',
        responseHandler: (response: Response) => response.blob(),
      }),
    }),

    updateOrganizationUser: builder.mutation<
      UserResponse,
      { userId: number; body: UpdateUserInfoRequest }
    >({
      query: ({ userId, body }) => ({
        url: `/users/${userId}/info`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (_res, _err, { userId }) => [
        { type: 'settings/User', id: userId },
        { type: 'settings/User', id: `detail-${userId}` },
        { type: 'settings/User', id: 'LIST' },
      ],
    }),

    createUserForOrganization: builder.mutation<
      UserResponse,
      { organizationId: number; body: CreateUserForOrganizationRequest }
    >({
      query: ({ organizationId, body }) => ({
        url: `/organizations/${organizationId}/users`,
        method: 'POST',
        body,
      }),
      invalidatesTags: [
        { type: 'settings/User', id: 'LIST' },
        { type: 'settings/User', id: 'STATS' },
      ],
    }),

    // ==================== Invitations ====================

    inviteUser: builder.mutation<
      ApiResponse<UserInvitation>,
      { organizationId: number; body: InviteUserRequest }
    >({
      query: ({ organizationId, body }) => ({
        url: `/organizations/${organizationId}/invitations`,
        method: 'POST',
        body,
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: [
        { type: 'settings/Invitation', id: 'LIST' },
        { type: 'settings/User', id: 'STATS' },
      ],
    }),

    getInvitations: builder.query<
      PaginatedResponse<UserInvitation>,
      InvitationFilters
    >({
      query: (filters) => {
        const params = new URLSearchParams();
        if (filters.status) params.append('status', filters.status);
        if (filters.page !== undefined)
          params.append('page', String(filters.page));
        if (filters.pageSize !== undefined)
          params.append('pageSize', String(filters.pageSize));
        return {
          url: `/organizations/${filters.organizationId}/invitations?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createPaginatedTransform<UserInvitation>(),
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'settings/Invitation' as const,
                id,
              })),
              { type: 'settings/Invitation', id: 'LIST' },
            ]
          : [{ type: 'settings/Invitation', id: 'LIST' }],
    }),

    cancelInvitation: builder.mutation<
      ApiResponse<void>,
      { organizationId: number; invitationId: number }
    >({
      query: ({ organizationId, invitationId }) => ({
        url: `/organizations/${organizationId}/invitations/${invitationId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_res, _err, { invitationId }) => [
        { type: 'settings/Invitation', id: invitationId },
        { type: 'settings/Invitation', id: 'LIST' },
        { type: 'settings/User', id: 'STATS' },
      ],
    }),

    resendInvitation: builder.mutation<
      ApiResponse<UserInvitation>,
      { organizationId: number; invitationId: number }
    >({
      query: ({ organizationId, invitationId }) => ({
        url: `/organizations/${organizationId}/invitations/${invitationId}/resend`,
        method: 'POST',
      }),
      invalidatesTags: (_res, _err, { invitationId }) => [
        { type: 'settings/Invitation', id: invitationId },
        { type: 'settings/Invitation', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  // Users
  useGetOrganizationUsersQuery,
  useLazyGetOrganizationUsersQuery,
  useGetSettingsOrganizationRolesQuery: useGetOrganizationRolesQuery,
  useGetSettingsUserStatsQuery: useGetUserStatsQuery,
  useGetUserDetailQuery,
  useLazyGetUserDetailQuery,
  useUpdateUserStatusMutation,
  useUpdateUserRolesMutation,
  useUpdateUserTypeMutation,
  useResetUserPasswordMutation,
  useLazyExportUsersQuery,
  useUpdateOrganizationUserMutation,
  useCreateUserForOrganizationMutation:
    useSettingsCreateUserForOrganizationMutation,
  // Invitations
  useInviteUserMutation,
  useGetInvitationsQuery,
  useCancelInvitationMutation,
  useResendInvitationMutation,
} = settingsUsersApi;
