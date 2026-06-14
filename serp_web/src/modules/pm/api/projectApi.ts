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
  PMCreateProjectComponentRequest,
  PMDeleteProjectComponentResponse,
  PMListProjectsParams,
  PMListProjectComponentsParams,
  PMProjectPersonApi,
  PMProjectPermissionSettingsApi,
  PMProjectRoleApi,
  PMProjectBlueprintApi,
  PMProjectCategoryApi,
  PMProjectComponentApi,
  PMProjectSettingsOverviewApi,
  PMProjectSummaryDashboardApi,
  PMProjectSummaryApi,
  PMProjectDetailApi,
  PMReplaceProjectPersonRolesRequest,
  PMProjectSummaryFilterParams,
  PMReplaceProjectPermissionGrantsRequest,
  PMUpdateProjectComponentRequest,
  PMUpdateProjectRequest,
  PMUpdateProjectResponse,
} from '../types/api';
import {
  buildProjectListParams,
  buildProjectSummaryParams,
} from './queryParams';

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

    getPmProjectRoles: builder.query<
      PaginatedResponse<PMProjectRoleApi>,
      { page?: number; pageSize?: number; search?: string; isSystem?: boolean }
    >({
      query: ({ page = 0, pageSize = 100, search, isSystem } = {}) => ({
        url: '/roles',
        method: 'GET',
        params: {
          page,
          pageSize,
          ...(search ? { search } : {}),
          ...(typeof isSystem === 'boolean' ? { isSystem } : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMProjectRoleApi>(),
      providesTags: [{ type: 'pm/ProjectRole' as const, id: 'LIST' }],
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

    getPmProjectSettingsOverview: builder.query<
      PMProjectSettingsOverviewApi,
      string
    >({
      query: (id) => ({
        url: `/projects/${id}/settings-overview`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectSettingsOverviewApi>(),
      providesTags: (_result, _error, id) => [
        { type: 'pm/ProjectSettingsOverview' as const, id },
      ],
    }),

    getPmProjectPermissions: builder.query<
      PMProjectPermissionSettingsApi,
      number
    >({
      query: (projectId) => ({
        url: `/projects/${projectId}/permissions`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectPermissionSettingsApi>(),
      providesTags: (_result, _error, projectId) => [
        { type: 'pm/ProjectPermission' as const, id: projectId },
      ],
    }),

    getPmProjectSummary: builder.query<
      PMProjectSummaryDashboardApi,
      { projectId: number; params?: PMProjectSummaryFilterParams }
    >({
      query: ({ projectId, params }) => ({
        url: `/projects/${projectId}/summary`,
        method: 'GET',
        params: buildProjectSummaryParams(params),
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectSummaryDashboardApi>(),
      providesTags: (_result, _error, { projectId }) => [
        { type: 'pm/ProjectSummary' as const, id: projectId },
      ],
    }),

    getPmProjectPeople: builder.query<PMProjectPersonApi[], number>({
      query: (projectId) => ({
        url: `/projects/${projectId}/people`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectPersonApi[]>(),
      providesTags: (_result, _error, projectId) => [
        { type: 'pm/ProjectPeople' as const, id: projectId },
      ],
    }),

    getPmProjectComponents: builder.query<
      PaginatedResponse<PMProjectComponentApi>,
      { projectId: number; params?: PMListProjectComponentsParams }
    >({
      query: ({ projectId, params }) => ({
        url: `/projects/${projectId}/components`,
        method: 'GET',
        params: {
          page: params?.page ?? 0,
          pageSize: params?.pageSize ?? 50,
          ...(params?.search ? { search: params.search } : {}),
          ...(params?.sortBy ? { sortBy: params.sortBy } : {}),
          ...(params?.sortDirection
            ? { sortDirection: params.sortDirection }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMProjectComponentApi>(),
      providesTags: (result, _error, { projectId }) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'pm/ProjectComponent' as const,
                id,
              })),
              { type: 'pm/ProjectComponent' as const, id: projectId },
            ]
          : [{ type: 'pm/ProjectComponent' as const, id: projectId }],
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

    createPmProjectComponent: builder.mutation<
      PMProjectComponentApi,
      { projectId: number; body: PMCreateProjectComponentRequest }
    >({
      query: ({ projectId, body }) => ({
        url: `/projects/${projectId}/components`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectComponentApi>(),
      invalidatesTags: (_result, _error, { projectId }) => [
        { type: 'pm/ProjectComponent' as const, id: projectId },
      ],
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

    updatePmProjectComponent: builder.mutation<
      PMProjectComponentApi,
      {
        projectId: number;
        componentId: number;
        body: PMUpdateProjectComponentRequest;
      }
    >({
      query: ({ projectId, componentId, body }) => ({
        url: `/projects/${projectId}/components/${componentId}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectComponentApi>(),
      invalidatesTags: (_result, _error, { projectId, componentId }) => [
        { type: 'pm/ProjectComponent' as const, id: componentId },
        { type: 'pm/ProjectComponent' as const, id: projectId },
      ],
    }),

    replacePmProjectPersonRoles: builder.mutation<
      void,
      {
        projectId: number;
        userId: number;
        body: PMReplaceProjectPersonRolesRequest;
      }
    >({
      query: ({ projectId, userId, body }) => ({
        url: `/projects/${projectId}/people/${userId}/roles`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      invalidatesTags: (_result, _error, { projectId }) => [
        { type: 'pm/ProjectPeople' as const, id: projectId },
      ],
    }),

    replacePmProjectPermissionGrants: builder.mutation<
      PMProjectPermissionSettingsApi,
      {
        projectId: number;
        body: PMReplaceProjectPermissionGrantsRequest;
      }
    >({
      query: ({ projectId, body }) => ({
        url: `/projects/${projectId}/permissions/grants`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMProjectPermissionSettingsApi>(),
      invalidatesTags: (_result, _error, { projectId }) => [
        { type: 'pm/ProjectPermission' as const, id: projectId },
        { type: 'pm/ProjectSettingsOverview' as const, id: String(projectId) },
      ],
    }),

    removePmProjectPerson: builder.mutation<
      void,
      { projectId: number; userId: number }
    >({
      query: ({ projectId, userId }) => ({
        url: `/projects/${projectId}/people/${userId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      invalidatesTags: (_result, _error, { projectId }) => [
        { type: 'pm/ProjectPeople' as const, id: projectId },
      ],
    }),

    deletePmProjectComponent: builder.mutation<
      PMDeleteProjectComponentResponse,
      { projectId: number; componentId: number }
    >({
      query: ({ projectId, componentId }) => ({
        url: `/projects/${projectId}/components/${componentId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse:
        createDataTransform<PMDeleteProjectComponentResponse>(),
      invalidatesTags: (_result, _error, { projectId, componentId }) => [
        { type: 'pm/ProjectComponent' as const, id: componentId },
        { type: 'pm/ProjectComponent' as const, id: projectId },
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
        { type: 'pm/ProjectSettingsOverview' as const, id },
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
        { type: 'pm/ProjectSettingsOverview' as const, id },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetProjectBlueprintsQuery,
  useGetProjectCategoriesQuery,
  useGetPmProjectRolesQuery,
  useGetPmProjectsQuery,
  useGetPmProjectByIdQuery,
  useGetPmProjectPermissionsQuery,
  useGetPmProjectSettingsOverviewQuery,
  useGetPmProjectSummaryQuery,
  useGetPmProjectPeopleQuery,
  useGetPmProjectComponentsQuery,
  useCreatePmProjectMutation,
  useCreatePmProjectComponentMutation,
  useUpdatePmProjectMutation,
  useUpdatePmProjectComponentMutation,
  useReplacePmProjectPersonRolesMutation,
  useReplacePmProjectPermissionGrantsMutation,
  useRemovePmProjectPersonMutation,
  useDeletePmProjectComponentMutation,
  useArchivePmProjectMutation,
  useUnarchivePmProjectMutation,
} = pmProjectApi;
