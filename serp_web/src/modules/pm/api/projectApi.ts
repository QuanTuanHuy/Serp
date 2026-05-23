/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project API endpoints
 */

import { api } from '@/lib/store/api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type { PaginatedResponse } from '@/lib/store/api/types';
import type {
  PMCreateProjectRequest,
  PMCreateProjectResponse,
  PMListProjectsParams,
  PMProjectBlueprintApi,
  PMProjectCategoryApi,
  PMProjectSummaryApi,
  PMProjectDetailApi,
  PMUpdateProjectRequest,
  PMUpdateProjectResponse,
} from '../types/api';
import { buildProjectListParams } from './queryParams';

export const pmProjectApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProjectBlueprints: builder.query<
      PaginatedResponse<PMProjectBlueprintApi>,
      {
        page?: number;
        pageSize?: number;
        projectTypeKey?: string;
        isSystem?: boolean;
      }
    >({
      query: ({ page = 0, pageSize = 100, projectTypeKey, isSystem } = {}) => ({
        url: '/project-blueprints',
        method: 'GET',
        params: {
          page,
          pageSize,
          ...(projectTypeKey ? { projectTypeKey } : {}),
          ...(typeof isSystem === 'boolean' ? { isSystem } : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMProjectBlueprintApi>(),
    }),

    getProjectCategories: builder.query<
      PaginatedResponse<PMProjectCategoryApi>,
      { page?: number; pageSize?: number; search?: string }
    >({
      query: ({ page = 0, pageSize = 100, search } = {}) => ({
        url: '/project-categories',
        method: 'GET',
        params: {
          page,
          pageSize,
          ...(search ? { search } : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMProjectCategoryApi>(),
    }),

    getPmProjects: builder.query<
      PaginatedResponse<PMProjectSummaryApi>,
      PMListProjectsParams
    >({
      query: (params = {}) => ({
        url: '/projects',
        method: 'GET',
        params: buildProjectListParams(params),
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMProjectSummaryApi>(),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }: { id: number }) => ({
                type: 'pm/Project' as const,
                id,
              })),
              { type: 'pm/Project' as const, id: 'LIST' },
            ]
          : [{ type: 'pm/Project' as const, id: 'LIST' }],
    }),

    getPmProjectById: builder.query<PMProjectDetailApi, string>({
      query: (id) => ({
        url: `/projects/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectDetailApi>(),
      providesTags: (result, error, id) => [
        { type: 'pm/Project' as const, id },
      ],
    }),

    createPmProject: builder.mutation<
      PMCreateProjectResponse,
      PMCreateProjectRequest
    >({
      query: (body) => ({
        url: '/projects',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMCreateProjectResponse>(),
      invalidatesTags: [{ type: 'pm/Project' as const, id: 'LIST' }],
    }),

    updatePmProject: builder.mutation<
      PMUpdateProjectResponse,
      { id: string; body: PMUpdateProjectRequest }
    >({
      query: ({ id, body }) => ({
        url: `/projects/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUpdateProjectResponse>(),
      invalidatesTags: (result, error, { id }) => [
        { type: 'pm/Project' as const, id },
        { type: 'pm/Project' as const, id: 'LIST' },
      ],
    }),

    archivePmProject: builder.mutation<PMUpdateProjectResponse, string>({
      query: (id) => ({
        url: `/projects/${id}/archive`,
        method: 'POST',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUpdateProjectResponse>(),
      invalidatesTags: (result, error, id) => [
        { type: 'pm/Project' as const, id },
        { type: 'pm/Project' as const, id: 'LIST' },
      ],
    }),

    unarchivePmProject: builder.mutation<PMUpdateProjectResponse, string>({
      query: (id) => ({
        url: `/projects/${id}/unarchive`,
        method: 'POST',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUpdateProjectResponse>(),
      invalidatesTags: (result, error, id) => [
        { type: 'pm/Project' as const, id },
        { type: 'pm/Project' as const, id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetProjectBlueprintsQuery,
  useGetProjectCategoriesQuery,
  useGetPmProjectsQuery,
  useGetPmProjectByIdQuery,
  useCreatePmProjectMutation,
  useUpdatePmProjectMutation,
  useArchivePmProjectMutation,
  useUnarchivePmProjectMutation,
} = pmProjectApi;
