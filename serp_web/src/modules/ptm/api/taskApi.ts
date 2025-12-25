/**
 * PTM v2 - Task API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task CRUD operations
 */

import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type {
  Task,
  TaskTemplate,
  CreateTaskRequest,
  UpdateTaskRequest,
  TaskDependency,
  CreateDependencyRequest,
  DependencyValidationResult,
  TaskFilterParams,
} from '../types';
import type { ApiResponse, PaginatedResponse } from '@/lib/store/api/types';
import { api } from '@/lib';

export const taskApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Get all tasks (with pagination)
    getTasks: builder.query<PaginatedResponse<Task>, TaskFilterParams>({
      query: (params = {}) => ({
        url: '/tasks',
        method: 'GET',
        params: {
          ...(params.status && { status: params.status }),
          ...(params.priority && { priority: params.priority }),
          ...(params.projectId && { projectId: params.projectId }),
          ...(params.parentTaskId && { parentTaskId: params.parentTaskId }),
          ...(params.category && { category: params.category }),
          ...(params.tags && { tags: params.tags }),
          ...(params.isDeepWork !== undefined && {
            isDeepWork: params.isDeepWork,
          }),
          ...(params.isMeeting !== undefined && {
            isMeeting: params.isMeeting,
          }),
          ...(params.isRecurring !== undefined && {
            isRecurring: params.isRecurring,
          }),
          ...(params.deadlineFrom && { deadlineFrom: params.deadlineFrom }),
          ...(params.deadlineTo && { deadlineTo: params.deadlineTo }),
          page: params.page ?? 0,
          pageSize: params.pageSize ?? 20,
          ...(params.sortBy && { sortBy: params.sortBy }),
          ...(params.sortOrder && { sortOrder: params.sortOrder }),
        },
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createPaginatedTransform<Task>(),
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'ptm/Task' as const,
                id,
              })),
              { type: 'ptm/Task', id: 'LIST' },
            ]
          : [{ type: 'ptm/Task', id: 'LIST' }],
    }),

    // Get single task
    getTask: builder.query<Task, number>({
      query: (id) => ({
        url: `/tasks/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<Task>(),
      providesTags: (_result, _error, id) => [{ type: 'ptm/Task', id }],
    }),

    // Create task
    createTask: builder.mutation<Task, CreateTaskRequest>({
      query: (body) => ({
        url: '/tasks',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: createDataTransform<Task>(),
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
    updateTask: builder.mutation<Task, { id: number } & UpdateTaskRequest>({
      query: ({ id, ...patch }) => ({
        url: `/tasks/${id}`,
        method: 'PATCH',
        body: patch,
      }),
      extraOptions: { service: 'ptm' },
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
    deleteTask: builder.mutation<void, number>({
      query: (id) => ({
        url: `/tasks/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ptm' },
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
      query: (parentTaskId) => ({
        url: '/tasks',
        method: 'GET',
        params: {
          parentTaskId,
          page: 0,
          pageSize: 100,
        },
      }),
      extraOptions: { service: 'ptm' },
      transformResponse: (response: any) => {
        // Extract items from paginated response
        return response?.data?.items || [];
      },
      providesTags: (_result, _error, parentTaskId) => [
        { type: 'ptm/Task', id: `SUBTASKS_${parentTaskId}` },
        { type: 'ptm/Task', id: 'LIST' },
      ],
    }),

    // Get full task tree (task + all descendants as nested subTasks)
    getTaskTree: builder.query<ApiResponse<Task>, number>({
      query: (taskId) => ({
        url: `/tasks/${taskId}/tree`,
        method: 'GET',
      }),
      extraOptions: { service: 'ptm' },
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
      async queryFn({ taskIds, newParentId }, _api, _extraOptions, baseQuery) {
        try {
          // Call updateTask for each task to change parentTaskId
          for (const taskId of taskIds) {
            await baseQuery({
              url: `/tasks/${taskId}`,
              method: 'PATCH',
              body: { parentTaskId: newParentId || null },
            });
          }
          return { data: undefined };
        } catch (error: any) {
          return {
            error: {
              status: error.status || 'CUSTOM_ERROR',
              error: error.message || 'Failed to reparent tasks',
            },
          };
        }
      },
      extraOptions: { service: 'ptm' },
      invalidatesTags: [{ type: 'ptm/Task', id: 'LIST' }],
    }),

    // ========================================
    // Dependency Operations
    // ========================================

    // Get dependencies for a task
    getTaskDependencies: builder.query<TaskDependency[], number>({
      queryFn: async (taskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getDependencies(taskId);
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
        { type: 'ptm/Dependency', id: taskId },
        { type: 'ptm/Dependency', id: 'LIST' },
      ],
    }),

    // Add dependency
    addDependency: builder.mutation<TaskDependency, CreateDependencyRequest>({
      queryFn: async (request) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.addDependency(request);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: (_result, _error, { taskId }) => [
        { type: 'ptm/Dependency', id: taskId },
        { type: 'ptm/Dependency', id: 'LIST' },
        { type: 'ptm/Task', id: taskId },
        { type: 'ptm/Task', id: 'LIST' },
      ],
    }),

    // Remove dependency
    removeDependency: builder.mutation<
      void,
      { taskId: number; dependencyId: number }
    >({
      queryFn: async ({ taskId, dependencyId }) => {
        if (USE_MOCK_DATA) {
          await mockApiHandlers.tasks.removeDependency(taskId, dependencyId);
          return { data: undefined };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: (_result, _error, { taskId }) => [
        { type: 'ptm/Dependency', id: taskId },
        { type: 'ptm/Dependency', id: 'LIST' },
        { type: 'ptm/Task', id: taskId },
        { type: 'ptm/Task', id: 'LIST' },
      ],
    }),

    // Validate dependency (check for cycles)
    validateDependency: builder.mutation<
      DependencyValidationResult,
      CreateDependencyRequest
    >({
      queryFn: async (request) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.validateDependency(request);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
    }),

    // Get tasks that block this task (tasks this depends on)
    getBlockedTasks: builder.query<Task[], number>({
      queryFn: async (taskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getBlockedBy(taskId);
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
        { type: 'ptm/Task', id: `BLOCKED_${taskId}` },
      ],
    }),

    // Get tasks that this task blocks
    getBlockingTasks: builder.query<Task[], number>({
      queryFn: async (taskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.tasks.getBlocking(taskId);
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
        { type: 'ptm/Task', id: `BLOCKING_${taskId}` },
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
  // Phase 1: Subtask hooks
  useGetSubtasksQuery,
  useGetTaskTreeQuery,
  usePromoteSubtaskMutation,
  useReparentTasksMutation,
  // Phase 2: Dependency hooks
  useGetTaskDependenciesQuery,
  useAddDependencyMutation,
  useRemoveDependencyMutation,
  useValidateDependencyMutation,
  useGetBlockedTasksQuery,
  useGetBlockingTasksQuery,
} = taskApi;
