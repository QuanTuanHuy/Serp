/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Modules API endpoints
 */

import { api } from '@/lib/store/api';
import type { Module } from '../../types';
import { createDataTransform } from '@/lib/store/api/utils';

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

    getModuleById: builder.query<Module, string>({
      query: (moduleId) => ({
        url: `/modules/${moduleId}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<Module>(),
      providesTags: (_result, _error, id) => [{ type: 'admin/Module', id }],
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
      invalidatesTags: [{ type: 'admin/Module', id: 'LIST' }],
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
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetModulesQuery,
  useGetModuleByIdQuery,
  useCreateModuleMutation,
  useUpdateModuleMutation,
} = modulesApi;
