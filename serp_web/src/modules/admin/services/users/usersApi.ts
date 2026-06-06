/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Users API endpoints
 */

import { api } from '@/lib/store/api';
import {
  createPaginatedTransform,
  createRtkTransformResponse,
} from '@/lib/store/api/utils';
import type { ApiResponse } from '@/lib/store/api/types';
import type {
  CreateUserForOrganizationRequest,
  Role,
  UserDetailResponse,
  UserFilters,
  UserProfile,
  UserResponse,
  UserStats,
  UserStatus,
  UserType,
  UsersResponse,
  UpdateUserInfoRequest,
} from '../../types';

const buildUserQueryParams = (filters: UserFilters): string => {
  const params = new URLSearchParams();

  if (filters.search) params.append('search', filters.search);
  if (filters.status) params.append('status', filters.status);
  if (filters.userType) params.append('userType', filters.userType);
  if (filters.roleId !== undefined)
    params.append('roleId', String(filters.roleId));
  if (filters.departmentId !== undefined)
    params.append('departmentId', String(filters.departmentId));
  if (filters.organizationId !== undefined)
    params.append('organizationId', String(filters.organizationId));
  if (filters.page !== undefined) params.append('page', String(filters.page));
  if (filters.pageSize !== undefined)
    params.append('pageSize', String(filters.pageSize));
  if (filters.sortBy) params.append('sortBy', filters.sortBy);
  if (filters.sortDir) params.append('sortDir', filters.sortDir);

  return params.toString();
};

export const usersApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getUsers: builder.query<UsersResponse, UserFilters>({
      query: (filters) => {
        const queryParams = buildUserQueryParams(filters);

        return {
          url: queryParams ? `/users?${queryParams}` : '/users',
          method: 'GET',
        };
      },
      transformResponse: createPaginatedTransform<UserProfile>(),
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'admin/User' as const,
                id,
              })),
              { type: 'admin/User', id: 'LIST' },
            ]
          : [{ type: 'admin/User', id: 'LIST' }],
    }),

    getUserStats: builder.query<ApiResponse<UserStats>, number>({
      query: (organizationId) => ({
        url: `/organizations/${organizationId}/users/stats`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_result, _error, organizationId) => [
        { type: 'admin/User', id: `STATS-${organizationId}` },
      ],
    }),

    getUserDetail: builder.query<
      ApiResponse<UserDetailResponse>,
      { organizationId: number; userId: number }
    >({
      query: ({ organizationId, userId }) => ({
        url: `/organizations/${organizationId}/users/${userId}/detail`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_result, _error, { userId }) => [
        { type: 'admin/User', id: `detail-${userId}` },
      ],
    }),

    getOrganizationRoles: builder.query<ApiResponse<Role[]>, number>({
      query: (organizationId) => ({
        url: `/organizations/${organizationId}/roles`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_result, _error, organizationId) => [
        { type: 'admin/Role', id: `ORG-${organizationId}` },
      ],
    }),

    updateUserInfo: builder.mutation<
      UserResponse,
      { userId: number; body: UpdateUserInfoRequest }
    >({
      query: ({ userId, body }) => ({
        url: `/users/${userId}/info`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (_result, _error, { userId }) => [
        { type: 'admin/User', id: userId },
        { type: 'admin/User', id: `detail-${userId}` },
        { type: 'admin/User', id: 'LIST' },
      ],
    }),

    updateUserStatus: builder.mutation<
      UserResponse,
      { organizationId: number; userId: number; status: UserStatus }
    >({
      query: ({ organizationId, userId, status }) => ({
        url: `/organizations/${organizationId}/users/${userId}/status`,
        method: 'PATCH',
        body: { status },
      }),
      invalidatesTags: (_result, _error, { userId, organizationId }) => [
        { type: 'admin/User', id: userId },
        { type: 'admin/User', id: `detail-${userId}` },
        { type: 'admin/User', id: 'LIST' },
        { type: 'admin/User', id: `STATS-${organizationId}` },
      ],
    }),

    updateUserRoles: builder.mutation<
      UserResponse,
      { organizationId: number; userId: number; body: { roleIds: number[] } }
    >({
      query: ({ organizationId, userId, body }) => ({
        url: `/organizations/${organizationId}/users/${userId}/roles`,
        method: 'PUT',
        body,
      }),
      invalidatesTags: (_result, _error, { userId }) => [
        { type: 'admin/User', id: userId },
        { type: 'admin/User', id: `detail-${userId}` },
        { type: 'admin/User', id: 'LIST' },
      ],
    }),

    updateUserType: builder.mutation<
      UserResponse,
      { organizationId: number; userId: number; body: { userType: UserType } }
    >({
      query: ({ organizationId, userId, body }) => ({
        url: `/organizations/${organizationId}/users/${userId}/type`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (_result, _error, { userId }) => [
        { type: 'admin/User', id: userId },
        { type: 'admin/User', id: `detail-${userId}` },
        { type: 'admin/User', id: 'LIST' },
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
      invalidatesTags: (_result, _error, { organizationId }) => [
        { type: 'admin/User', id: 'LIST' },
        { type: 'admin/User', id: `STATS-${organizationId}` },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
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
} = usersApi;
