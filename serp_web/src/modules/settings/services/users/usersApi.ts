/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings Users API endpoints (organization-scoped)
 */

import { api } from '@/lib/store/api';
import { createPaginatedTransform } from '@/lib/store/api/utils';
import type {
  UsersResponse,
  UserResponse,
  UserProfile,
  UserFilters as AdminUserFilters,
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
} from '@/modules/admin/types';

export type SettingsUserFilters = AdminUserFilters & {
  organizationId: number;
};

export const settingsUsersApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getOrganizationUsers: builder.query<UsersResponse, SettingsUserFilters>({
      query: (filters) => {
        const params = new URLSearchParams();
        if (filters.search) params.append('search', filters.search);
        if (filters.status)
          params.append(
            'status',
            filters.status === 'PENDING'
              ? 'INVITED'
              : (filters.status as string)
          );
        if (filters.page !== undefined)
          params.append('page', String(filters.page));
        if (filters.pageSize !== undefined)
          params.append('pageSize', String(filters.pageSize));
        if (filters.sortBy) params.append('sortBy', filters.sortBy);
        if (filters.sortDir) params.append('sortDir', filters.sortDir);
        // Organization scope
        params.append('organizationId', String(filters.organizationId));

        return { url: `/users?${params.toString()}`, method: 'GET' };
      },
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

    updateOrganizationUser: builder.mutation<
      UserResponse,
      { userId: number; body: UpdateUserInfoRequest }
    >({
      query: ({ userId, body }) => ({
        url: `/users/${userId}/info`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (_res, _err, args) => [
        { type: 'settings/User', id: args.userId },
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
      invalidatesTags: [{ type: 'settings/User', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetOrganizationUsersQuery,
  useLazyGetOrganizationUsersQuery,
  useCreateUserForOrganizationMutation:
    useSettingsCreateUserForOrganizationMutation,
  useUpdateOrganizationUserMutation,
} = settingsUsersApi;
