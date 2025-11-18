/**
 * PTM v2 - Project API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project CRUD operations
 */

import { ptmApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  Project,
  CreateProjectRequest,
  UpdateProjectRequest,
} from '../types';

export const projectApi = ptmApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get all projects
    getProjects: builder.query<Project[], { status?: string }>({
      query: (params) => ({
        url: '/api/v2/projects',
        method: 'GET',
        params,
      }),
      transformResponse: createDataTransform<Project[]>(),
      providesTags: (result) =>
        result
          ? [
              ...result.map(({ id }) => ({ type: 'ptm/Project' as const, id })),
              { type: 'ptm/Project', id: 'LIST' },
            ]
          : [{ type: 'ptm/Project', id: 'LIST' }],
    }),

    // Get single project
    getProject: builder.query<Project, string>({
      query: (id) => ({
        url: `/api/v2/projects/${id}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<Project>(),
      providesTags: (_result, _error, id) => [{ type: 'ptm/Project', id }],
    }),

    // Create project
    createProject: builder.mutation<Project, CreateProjectRequest>({
      query: (body) => ({
        url: '/api/v2/projects',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<Project>(),
      invalidatesTags: [{ type: 'ptm/Project', id: 'LIST' }],
    }),

    // Update project
    updateProject: builder.mutation<Project, UpdateProjectRequest>({
      query: ({ id, ...patch }) => ({
        url: `/api/v2/projects/${id}`,
        method: 'PUT',
        body: patch,
      }),
      transformResponse: createDataTransform<Project>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'ptm/Project', id },
        { type: 'ptm/Project', id: 'LIST' },
      ],
    }),

    // Delete project
    deleteProject: builder.mutation<void, string>({
      query: (id) => ({
        url: `/api/v2/projects/${id}`,
        method: 'DELETE',
      }),
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
  useCreateProjectMutation,
  useUpdateProjectMutation,
  useDeleteProjectMutation,
} = projectApi;
