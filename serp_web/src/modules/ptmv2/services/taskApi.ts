/**
 * PTM v2 - Task API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task CRUD operations
 */

import { ptmApi } from './api';
import { createDataTransform } from '@/lib/store/api/utils';
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
      query: (params) => ({
        url: '/api/v2/tasks',
        method: 'GET',
        params,
      }),
      transformResponse: createDataTransform<Task[]>(),
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
      query: (id) => ({
        url: `/api/v2/tasks/${id}`,
        method: 'GET',
      }),
      transformResponse: createDataTransform<Task>(),
      providesTags: (_result, _error, id) => [{ type: 'ptm/Task', id }],
    }),

    // Create task
    createTask: builder.mutation<Task, CreateTaskRequest>({
      query: (body) => ({
        url: '/api/v2/tasks',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<Task>(),
      invalidatesTags: [
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Quick add task (one-click)
    quickAddTask: builder.mutation<Task, { title: string }>({
      query: (body) => ({
        url: '/api/v2/tasks/quick-add',
        method: 'POST',
        body,
      }),
      transformResponse: createDataTransform<Task>(),
      invalidatesTags: [
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Update task
    updateTask: builder.mutation<Task, UpdateTaskRequest>({
      query: ({ id, ...patch }) => ({
        url: `/api/v2/tasks/${id}`,
        method: 'PUT',
        body: patch,
      }),
      transformResponse: createDataTransform<Task>(),

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
      query: (id) => ({
        url: `/api/v2/tasks/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'ptm/Task', id },
        { type: 'ptm/Task', id: 'LIST' },
        { type: 'ptm/Schedule', id: 'LIST' },
      ],
    }),

    // Get task templates
    getTaskTemplates: builder.query<TaskTemplate[], void>({
      query: () => ({
        url: '/api/v2/tasks/templates',
        method: 'GET',
      }),
      transformResponse: createDataTransform<TaskTemplate[]>(),
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
