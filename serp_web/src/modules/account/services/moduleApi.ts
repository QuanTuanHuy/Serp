/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module API endpoints
 */

import { api } from '@/lib';
import { createRtkTransformResponse } from '@/lib';
import type { UserModuleAccess } from '../types';
import type { ApiResponse } from '@/lib';

export const moduleApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMyModules: builder.query<ApiResponse<UserModuleAccess[]>, void>({
      query: () => '/modules/my-modules',
      providesTags: ['account/modules'],
      transformResponse: createRtkTransformResponse(),
    }),
  }),
  overrideExisting: false,
});

export const { useGetMyModulesQuery } = moduleApi;
