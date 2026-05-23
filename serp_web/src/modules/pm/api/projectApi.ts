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
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetProjectBlueprintsQuery,
  useGetProjectCategoriesQuery,
  useGetPmProjectsQuery,
  useCreatePmProjectMutation,
} = pmProjectApi;
