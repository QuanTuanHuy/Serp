/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin departments API endpoints
 */

import { api } from '@/lib/store/api';
import { createPaginatedTransform } from '@/lib/store/api/utils';
import type { PaginatedResponse } from '@/lib/store/api/types';
import type { DepartmentOption } from '../../types';

export const departmentsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDepartments: builder.query<
      PaginatedResponse<DepartmentOption>,
      { organizationId: number }
    >({
      query: ({ organizationId }) => ({
        url: `/organizations/${organizationId}/departments?page=0&pageSize=100&sortBy=name&sortDir=ASC`,
        method: 'GET',
      }),
      transformResponse: createPaginatedTransform<DepartmentOption>(),
      providesTags: (_result, _error, { organizationId }) => [
        { type: 'settings/Department', id: `LIST-${organizationId}` },
      ],
    }),
  }),
  overrideExisting: false,
});

export const { useGetDepartmentsQuery, useLazyGetDepartmentsQuery } =
  departmentsApi;
