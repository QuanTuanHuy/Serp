/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Users API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  UserFilters,
  UserProfile,
  UsersResponse,
  UserResponse,
  UpdateUserInfoRequest,
  CreateUserForOrganizationRequest,
} from '../../types';
import { createPaginatedTransform } from '@/lib/store/api/utils';

export const usersApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getUsers: builder.query<UsersResponse, UserFilters>({
      query: (filters) => {
        const params = new URLSearchParams();

        if (filters.search) params.append('search', filters.search);
        if (filters.organizationId)
          params.append('organizationId', String(filters.organizationId));
        if (filters.status) {
          const status =
            filters.status === 'PENDING'
              ? 'INVITED'
              : (filters.status as string);
          params.append('status', status);
        }
        if (filters.page !== undefined)
          params.append('page', String(filters.page));
        if (filters.pageSize !== undefined)
          params.append('pageSize', String(filters.pageSize));
        if (filters.sortBy) params.append('sortBy', filters.sortBy);
        if (filters.sortDir) params.append('sortDir', filters.sortDir);

        return {
          url: `/users?${params.toString()}`,
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

    updateUserInfo: builder.mutation<
      UserResponse,
      { userId: number; body: UpdateUserInfoRequest }
    >({
      query: ({ userId, body }) => ({
        url: `/users/${userId}/info`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: (_res, _err, args) => [
        { type: 'admin/User', id: args.userId },
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
      invalidatesTags: [{ type: 'admin/User', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetUsersQuery,
  useLazyGetUsersQuery,
  useUpdateUserInfoMutation,
  useCreateUserForOrganizationMutation,
} = usersApi;
