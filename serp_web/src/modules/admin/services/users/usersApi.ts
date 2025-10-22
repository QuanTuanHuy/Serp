/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Users API endpoints
 */

import { api } from '@/lib/store/api';
import type { UserFilters, UserProfile, UsersResponse } from '../../types';
import { createPaginatedTransform } from '@/lib/store/api/utils';

export const usersApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getUsers: builder.query<UsersResponse, UserFilters>({
      query: (filters) => {
        const params = new URLSearchParams();

        if (filters.search) params.append('search', filters.search);
        if (filters.organizationId)
          params.append('organizationId', String(filters.organizationId));
        if (filters.status) params.append('status', filters.status);
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
  }),
  overrideExisting: false,
});

export const { useGetUsersQuery, useLazyGetUsersQuery } = usersApi;
