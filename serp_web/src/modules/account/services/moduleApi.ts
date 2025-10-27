/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module API endpoints
 */

import { api } from '@/lib';
import type { UserModuleAccess } from '../types';
import { createDataTransform } from '@/lib/store/api/utils';

export const moduleApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMyModules: builder.query<UserModuleAccess[], void>({
      query: () => '/modules/my-modules',
      providesTags: ['account/modules'],
      transformResponse: createDataTransform<UserModuleAccess[]>(),
    }),
  }),
  overrideExisting: false,
});

export const { useGetMyModulesQuery } = moduleApi;
