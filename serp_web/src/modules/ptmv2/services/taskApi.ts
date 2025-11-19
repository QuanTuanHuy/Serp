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
    getTasks: builder.query<
      Task[],
      { status?: string; projectId?: number | string }
    >({
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
    getTask: builder.query<Task, number>({
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
    deleteTask: builder.mutation<void, number>({
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

    // ========================================
    // PHASE 1: Subtask Operations
    // ========================================

    // Get subtasks for a parent task
    getSubtasks: builder.query<Task[], number>({
      queryFn: async (parentTaskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getSubtasks(parentTaskId);
          return { data };
        }
        // Real API call
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: (_result, _error, parentTaskId) => [
        { type: 'ptm/Task', id: `SUBTASKS_${parentTaskId}` },
        { type: 'ptm/Task', id: 'LIST' },
      ],
    }),

    // Get full task tree (task + all descendants + ancestors)
    getTaskTree: builder.query<
      {
        task: Task;
        children: Task[];
        descendants: Task[];
        ancestors: Task[];
      },
      number
    >({
      queryFn: async (taskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getTaskTree(taskId);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: (_result, _error, taskId) => [
        { type: 'ptm/Task', id: taskId },
        { type: 'ptm/Task', id: `TREE_${taskId}` },
      ],
    }),

    // Convert subtask to independent task (remove parent)
    promoteSubtask: builder.mutation<Task, number>({
      queryFn: async (taskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.update(taskId, {
            parentTaskId: undefined,
          });
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: (_result, _error, taskId) => [
        { type: 'ptm/Task', id: taskId },
        { type: 'ptm/Task', id: 'LIST' },
      ],
    }),

    // Bulk reparent tasks (move to different parent or make independent)
    reparentTasks: builder.mutation<
      void,
      { taskIds: number[]; newParentId?: number }
    >({
      queryFn: async ({ taskIds, newParentId }) => {
        if (USE_MOCK_DATA) {
          for (const taskId of taskIds) {
            await mockApiHandlers.tasks.update(taskId, {
              parentTaskId: newParentId,
            });
          }
          return { data: undefined };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: [{ type: 'ptm/Task', id: 'LIST' }],
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
  // Phase 1: Subtask hooks
  useGetSubtasksQuery,
  useGetTaskTreeQuery,
  usePromoteSubtaskMutation,
  useReparentTasksMutation,
} = taskApi;
