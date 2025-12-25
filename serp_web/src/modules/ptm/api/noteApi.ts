/**
 * PTM v2 - Note API Endpoints
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Note CRUD operations
 */

import { api } from '@/lib';
import { USE_MOCK_DATA, mockApiHandlers } from '../mocks/mockHandlers';
import type { Note, CreateNoteRequest, UpdateNoteRequest } from '../types';

export const noteApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Get notes by task
    getNotesByTask: builder.query<Note[], number>({
      queryFn: async (taskId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.notes.getByTask(taskId);
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
        { type: 'ptm/Note', id: `TASK_${taskId}` },
      ],
    }),

    // Get notes by project
    getNotesByProject: builder.query<Note[], number>({
      queryFn: async (projectId) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.notes.getByProject(projectId);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: (_result, _error, projectId) => [
        { type: 'ptm/Note', id: `PROJECT_${projectId}` },
      ],
    }),

    // Get single note
    getNote: builder.query<Note, number>({
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.notes.getById(id);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      providesTags: (_result, _error, id) => [{ type: 'ptm/Note', id }],
    }),

    // Create note
    createNote: builder.mutation<Note, CreateNoteRequest>({
      queryFn: async (body) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.notes.create(body);
          return { data };
        }
        return {
          error: {
            status: 'CUSTOM_ERROR',
            error: 'API not implemented',
          } as any,
        };
      },
      invalidatesTags: (_result, _error, { taskId, projectId }) => {
        const tags: any[] = [];
        if (taskId) tags.push({ type: 'ptm/Note', id: `TASK_${taskId}` });
        if (projectId)
          tags.push({ type: 'ptm/Note', id: `PROJECT_${projectId}` });
        return tags;
      },
    }),

    // Update note
    updateNote: builder.mutation<Note, UpdateNoteRequest>({
      queryFn: async ({ id, ...patch }) => {
        if (USE_MOCK_DATA) {
          const data = await mockApiHandlers.notes.update(id, patch);
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
          noteApi.util.updateQueryData('getNote', id, (draft) => {
            Object.assign(draft, patch);
          })
        );

        try {
          await queryFulfilled;
        } catch {
          patchResult.undo();
        }
      },

      invalidatesTags: (_result, _error, { id }) => [{ type: 'ptm/Note', id }],
    }),

    // Delete note
    deleteNote: builder.mutation<void, number>({
      queryFn: async (id) => {
        if (USE_MOCK_DATA) {
          await mockApiHandlers.notes.delete(id);
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
        { type: 'ptm/Note', id },
        { type: 'ptm/Note', id: 'TASK_*' },
        { type: 'ptm/Note', id: 'PROJECT_*' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetNotesByTaskQuery,
  useGetNotesByProjectQuery,
  useGetNoteQuery,
  useCreateNoteMutation,
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} = noteApi;
