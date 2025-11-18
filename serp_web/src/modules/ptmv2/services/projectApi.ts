/**
 * PTM v2 - Project API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Project CRUD operations
 */

import { ptmApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type {
  Project,
  CreateProjectRequest,
  UpdateProjectRequest,
} from '../types';

export const projectApi = ptmApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get all projects
    getProjects: builder.query<Project[], { status?: string }>({
      queryFn: async (params) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.projects.getAll(params);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
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
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.projects.getById(id);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: (_result, _error, id) => [{ type: 'ptm/Project', id }],
    }),

    // Create project
    createProject: builder.mutation<Project, CreateProjectRequest>({
      queryFn: async (body) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.projects.create(body);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [{ type: 'ptm/Project', id: 'LIST' }],
    }),

    // Update project
    updateProject: builder.mutation<Project, UpdateProjectRequest>({
      queryFn: async ({ id, ...patch }) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.projects.update(id, patch);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'ptm/Project', id },
        { type: 'ptm/Project', id: 'LIST' },
      ],
    }),

    // Delete project
    deleteProject: builder.mutation<void, string>({
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          await mockApiHandlers.projects.delete(id);
          return { data: undefined };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
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
