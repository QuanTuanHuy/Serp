/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type {
  Project,
  CreateProjectRequest,
  UpdateProjectRequest,
  Task,
  ProjectFilterParams,
} from '../types';
import type { PaginatedResponse } from '@/lib/store/api/types';
import { api } from '@/lib';

export const projectApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProjects: builder.query<PaginatedResponse<Project>, ProjectFilterParams>(
      {
        query: (params = {}) => ({
          url: '/projects',
          method: 'GET',
          params: {
            ...(params.status && { status: params.status }),
            ...(params.priority && { priority: params.priority }),
            page: params.page ?? 0,
            pageSize: params.pageSize ?? 20,
          },
        }),
        extraOptions: { service: 'ptm' },
        transformResponse: createPaginatedTransform<Project>(),
        providesTags: (result) =>
          result?.data.items
            ? [
                ...result.data.items.map(({ id }) => ({
                  type: 'ptm/Project' as const,
                  id,
                })),
                { type: 'ptm/Project', id: 'LIST' },
              ]
            : [{ type: 'ptm/Project', id: 'LIST' }],
      }
    ),

    getProject: builder.query<Project, number>({
      query: (id) => ({
        url: `/projects/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<Project>(),
      providesTags: (_result, _error, id) => [{ type: 'ptm/Project', id }],
    }),

    getProjectTasks: builder.query<Task[], number>({
      query: (projectId) => ({
        url: `/projects/${projectId}/tasks`,
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<Task[]>(),
      providesTags: (result, _error, projectId) =>
        result
          ? [
              ...result.map(({ id }) => ({ type: 'ptm/Task' as const, id })),
              { type: 'ptm/Task', id: `PROJECT_${projectId}` },
            ]
          : [{ type: 'ptm/Task', id: `PROJECT_${projectId}` }],
    }),

    createProject: builder.mutation<Project, CreateProjectRequest>({
      query: (body) => ({
        url: '/projects',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<Project>(),
      invalidatesTags: [{ type: 'ptm/Project', id: 'LIST' }],
    }),

    updateProject: builder.mutation<void, UpdateProjectRequest>({
      query: ({ id, ...patch }) => ({
        url: `/projects/${id}`,
        method: 'PATCH',
        body: patch,
      }),
      extraOptions: { service: 'ptm' },
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'ptm/Project', id },
        { type: 'ptm/Project', id: 'LIST' },
      ],
    }),

    deleteProject: builder.mutation<void, number>({
      query: (id) => ({
        url: `/projects/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ptm' },
      invalidatesTags: (_result, _error, id) => [
        { type: 'ptm/Project', id },
        { type: 'ptm/Project', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetProjectsQuery,
  useGetProjectQuery,
  useGetProjectTasksQuery,
  useCreateProjectMutation,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} = projectApi;
