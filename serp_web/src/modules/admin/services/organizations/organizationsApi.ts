/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organizations API endpoints
 */

import { api } from '@/lib/store/api';
import type {
  Organization,
  OrganizationFilters,
  OrganizationsResponse,
} from '../../types';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';

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
  }),
  overrideExisting: true,
});

export const {
  useGetOrganizationsQuery,
  useGetOrganizationByIdQuery,
  useLazyGetOrganizationsQuery,
  useLazyGetOrganizationByIdQuery,
} = organizationsApi;
