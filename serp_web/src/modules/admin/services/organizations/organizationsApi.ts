/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organizations API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  Organization,
  OrganizationFilters,
  OrganizationsResponse,
  OrganizationStatus,
  OrganizationStatusUpdateResponse,
  UserStats,
} from '../../types';
import {
  createDataTransform,
  createPaginatedTransform,
  createRtkTransformResponse,
} from '@/lib/store/api/utils';
import type { ApiResponse } from '@/lib/store/api/types';

export const organizationsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getOrganizations: builder.query<OrganizationsResponse, OrganizationFilters>(
      {
        query: (filters) => {
          const params = new URLSearchParams();

          if (filters.search) params.append('search', filters.search);
          if (filters.status) params.append('status', filters.status);
          if (filters.type) params.append('type', filters.type);
          if (filters.page !== undefined)
            params.append('page', String(filters.page));
          if (filters.pageSize !== undefined)
            params.append('pageSize', String(filters.pageSize));
          if (filters.sortBy) params.append('sortBy', filters.sortBy);
          if (filters.sortDir) params.append('sortDir', filters.sortDir);

          return {
            url: `/admin/organizations?${params.toString()}`,
            method: 'GET',
          };
        },
        transformResponse: createPaginatedTransform<Organization>(),
        providesTags: (result) =>
          result?.data.items
            ? [
                ...result.data.items.map(({ id }) => ({
                  type: 'admin/Organization' as const,
                  id,
                })),
                { type: 'admin/Organization', id: 'LIST' },
              ]
            : [{ type: 'admin/Organization', id: 'LIST' }],
      }
    ),

    getOrganizationById: builder.query<Organization, string>({
      query: (organizationId) => ({
        url: `/admin/organizations/${organizationId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<Organization>(),
      providesTags: (_result, _error, id) => [
        { type: 'admin/Organization', id },
      ],
    }),

    getOrganizationUserStats: builder.query<ApiResponse<UserStats>, number>({
      query: (organizationId) => ({
        url: `/admin/organizations/${organizationId}/users/stats`,
        method: 'GET',
      }),
      transformResponse: createRtkTransformResponse(),
      providesTags: (_result, _error, organizationId) => [
        { type: 'admin/Organization', id: `${organizationId}-user-stats` },
      ],
    }),

    updateOrganizationStatus: builder.mutation<
      ApiResponse<OrganizationStatusUpdateResponse>,
      { organizationId: number; status: OrganizationStatus }
    >({
      query: ({ organizationId, status }) => ({
        url: `/admin/organizations/${organizationId}/status`,
        method: 'PATCH',
        body: { status },
      }),
      transformResponse: createRtkTransformResponse(),
      invalidatesTags: (_result, _error, { organizationId }) => [
        { type: 'admin/Organization', id: organizationId },
        { type: 'admin/Organization', id: 'LIST' },
        { type: 'admin/Organization', id: `${organizationId}-user-stats` },
        { type: 'admin/User', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: true,
});

export const {
  useGetOrganizationsQuery,
  useGetOrganizationByIdQuery,
  useGetOrganizationUserStatsQuery,
  useLazyGetOrganizationsQuery,
  useLazyGetOrganizationByIdQuery,
  useLazyGetOrganizationUserStatsQuery,
  useUpdateOrganizationStatusMutation,
} = organizationsApi;
