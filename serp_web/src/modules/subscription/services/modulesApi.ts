/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules API endpoints for subscription
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type { Module } from '../types';

export const modulesApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAvailableModules: builder.query<Module[], void>({
      query: () => ({
        url: '/modules',
        method: 'GET',
      }),
      transformResponse: createDataTransform<Module[]>(),
      providesTags: [{ type: 'subscription/Module', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const { useGetAvailableModulesQuery } = modulesApi;
