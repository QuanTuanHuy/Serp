/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules API endpoints
 */

import { api } from '@/lib/store/api';
import type { Module } from '../../types';
import { createDataTransform } from '@/lib/store/api/utils';

export interface ModuleQueryParams {
  page: number; // 0-based page index
  pageSize: number;
  search?: string;
  status?: string;
  moduleType?: string;
  sortBy?: string;
  sortDirection?: string;
}

export interface PaginatedModulesResponse {
  items: Module[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export interface ModuleStats {
  total: number;
  enabled: number;
  disabled: number;
}

export const modulesApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getModules: builder.query<Module[], void>({
      query: () => ({
        url: '/modules',
        method: 'GET',
      }),
      transformResponse: createDataTransform<Module[]>(),
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({
                type: 'admin/Module' as const,
                id,
              })),
              { type: 'admin/Module', id: 'LIST' },
            ]
          : [{ type: 'admin/Module', id: 'LIST' }],
    }),

    getModulesV2: builder.query<PaginatedModulesResponse, ModuleQueryParams>({
      query: (params) => ({
        url: '/modules',
        method: 'GET',
        params,
      }),
      extraOptions: { version: 'v2' },
      transformResponse: createDataTransform<PaginatedModulesResponse>(),
      providesTags: (result) =>
        result?.items
          ? [
              ...result.items.map(({ id }) => ({
                type: 'admin/Module' as const,
                id,
              })),
              { type: 'admin/Module', id: 'LIST' },
            ]
          : [{ type: 'admin/Module', id: 'LIST' }],
    }),

    getModuleById: builder.query<Module, string>({
      query: (moduleId) => ({
        url: `/modules/${moduleId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<Module>(),
      providesTags: (_result, _error, id) => [{ type: 'admin/Module', id }],
    }),

    getModuleStats: builder.query<ModuleStats, void>({
      query: () => ({
        url: '/modules/stats',
        method: 'GET',
      }),
      transformResponse: createDataTransform<ModuleStats>(),
      providesTags: [{ type: 'admin/Module', id: 'STATS' }],
    }),

    createModule: builder.mutation<
      Module,
      Omit<Module, 'id' | 'createdAt' | 'updatedAt'>
    >({
      query: (moduleData) => ({
        url: '/modules',
        method: 'POST',
        body: moduleData,
      }),
      transformResponse: createDataTransform<Module>(),
      invalidatesTags: [
        { type: 'admin/Module', id: 'LIST' },
        { type: 'admin/Module', id: 'STATS' },
      ],
    }),

    updateModule: builder.mutation<
      Module,
      { id: string; data: Partial<Module> }
    >({
      query: ({ id, data }) => ({
        url: `/modules/${id}`,
        method: 'PUT',
        body: data,
      }),
      transformResponse: createDataTransform<Module>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'admin/Module', id },
        { type: 'admin/Module', id: 'LIST' },
        { type: 'admin/Module', id: 'STATS' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetModulesQuery,
  useGetModulesV2Query,
  useGetModuleByIdQuery,
  useGetModuleStatsQuery,
  useCreateModuleMutation,
  useUpdateModuleMutation,
} = modulesApi;
