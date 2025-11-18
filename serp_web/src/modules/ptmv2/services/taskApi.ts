/**
 * PTM v2 - Task API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task CRUD operations
 */

import { ptmApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type {
  Task,
  TaskTemplate,
  CreateTaskRequest,
  UpdateTaskRequest,
} from '../types';

export const taskApi = ptmApi.injectEndpoints({
  endpoints: (builder) => ({
    // Get all tasks
    getTasks: builder.query<Task[], { status?: string; projectId?: string }>({
      queryFn: async (params) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getAll(params);
          return { data };
        }
        // Real API call would go here
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
              ...result.map(({ id }) => ({ type: 'ptm/Task' as const, id })),
              { type: 'ptm/Task', id: 'LIST' },
            ]
          : [{ type: 'ptm/Task', id: 'LIST' }],
    }),

    // Get single task
    getTask: builder.query<Task, string>({
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getById(id);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: (_result, _error, id) => [{ type: 'ptm/Task', id }],
    }),

    // Create task
    createTask: builder.mutation<Task, CreateTaskRequest>({
      queryFn: async (body) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.create(body);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Quick add task (one-click)
    quickAddTask: builder.mutation<Task, { title: string }>({
      queryFn: async (body) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.quickAdd(body);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Update task
    updateTask: builder.mutation<Task, UpdateTaskRequest>({
      queryFn: async ({ id, ...patch }) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.update(id, patch);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },

      // Optimistic update
      async onQueryStarted({ id, ...patch }, { dispatch, queryFulfilled }) {
        const patchResult = dispatch(
          taskApi.util.updateQueryData('getTask', id, (draft) => {
            Object.assign(draft, patch);
          })
        );

        try {
          await queryFulfilled;
        } catch {
          patchResult.undo();
        }
      },

      invalidatesTags: (_result, _error, { id }) => [
        { type: 'ptm/Task', id },
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Delete task
    deleteTask: builder.mutation<void, string>({
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          await mockApiHandlers.tasks.delete(id);
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
        { type: 'ptm/Task', id },
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Get task templates
    getTaskTemplates: builder.query<TaskTemplate[], void>({
      queryFn: async () => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getTemplates();
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: [{ type: 'ptm/Task', id: 'TEMPLATES' }],
    }),

    // Create task from template
    createFromTemplate: builder.mutation<
      Task,
      { templateId: string; variables?: Record<string, string> }
    >({
      query: ({ templateId, variables }) => ({
        url: `/api/v2/tasks/from-template/${templateId}`,
        method: 'POST',
        body: { variables },
      }),
      transformResponse: createDataTransform<Task>(),
      invalidatesTags: [
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetTasksQuery,
  useGetTaskQuery,
  useCreateTaskMutation,
  useQuickAddTaskMutation,
  useUpdateTaskMutation,
  useDeleteTaskMutation,
  useGetTaskTemplatesQuery,
  useCreateFromTemplateMutation,
} = taskApi;
