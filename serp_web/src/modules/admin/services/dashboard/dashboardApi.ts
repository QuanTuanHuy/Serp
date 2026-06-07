/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type { AdminDashboard } from '../../types';

export const dashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAdminDashboard: builder.query<AdminDashboard, void>({
      query: () => ({
        url: '/admin/dashboard',
        method: 'GET',
      }),
      transformResponse: createDataTransform<AdminDashboard>(),
      providesTags: [{ type: 'admin/Dashboard', id: 'OVERVIEW' }],
    }),
  }),
  overrideExisting: false,
});

export const { useGetAdminDashboardQuery } = dashboardApi;
