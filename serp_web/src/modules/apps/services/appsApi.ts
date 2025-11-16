/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Apps API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type { Module, GetModulesParams } from '../types';

export const appsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAllModules: builder.query<Module[], GetModulesParams>({
      query: (params) => ({
        url: '/modules',
        method: 'GET',
        params: {
          category: params?.category,
          pricingModel: params?.pricingModel,
          status: params?.status,
          search: params?.search,
          isGlobal: params?.isGlobal,
          page: params?.page,
          pageSize: params?.pageSize,
        },
      }),
      transformResponse: createDataTransform<Module[]>(),
      providesTags: [{ type: 'subscription/Module', id: 'LIST' }],
    }),

    getModuleById: builder.query<Module, number>({
      query: (id) => ({
        url: `/modules/${id}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<Module>(),
      providesTags: (_result, _error, id) => [
        { type: 'subscription/Module', id },
      ],
    }),
  }),
  overrideExisting: false,
});

export const { useGetAllModulesQuery, useGetModuleByIdQuery } = appsApi;
