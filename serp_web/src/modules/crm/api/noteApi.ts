/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { api } from '@/lib/store/api';
import type { APIResponse, PaginatedResponse } from '../types';

export interface Note {
  id: string;
  tenantId: string;
  entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
  entityId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface CreateNoteRequest {
  entityType: 'LEAD' | 'ACCOUNT' | 'OPPORTUNITY' | 'ACTIVITY';
  entityId: number;
  content: string;
}

export interface UpdateNoteRequest {
  content: string;
}

export const noteApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getNotes: builder.query<
      APIResponse<PaginatedResponse<Note>>,
      { entityType: string; entityId: string; page?: number; size?: number }
    >({
      query: ({ entityType, entityId, page = 1, size = 20 }) => ({
        url: '/notes',
        method: 'GET',
        params: { entityType, entityId, page, size },
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note' as const, id: `${entityType}-${entityId}-LIST` },
      ],
    }),

    createNote: builder.mutation<APIResponse<Note>, CreateNoteRequest>({
      query: (data) => ({
        url: '/notes',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note', id: `${entityType}-${entityId}-LIST` },
      ],
    }),

    updateNote: builder.mutation<
      APIResponse<Note>,
      {
        id: string;
        data: UpdateNoteRequest;
        entityType: string;
        entityId: string;
      }
    >({
      query: ({ id, data }) => ({
        url: `/notes/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note', id: `${entityType}-${entityId}-LIST` },
      ],
    }),

    deleteNote: builder.mutation<
      APIResponse<any>,
      { id: string; entityType: string; entityId: string }
    >({
      query: ({ id }) => ({
        url: `/notes/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note', id: `${entityType}-${entityId}-LIST` },
      ],
    }),
  }),
});

export const {
  useGetNotesQuery,
  useCreateNoteMutation,
  useUpdateNoteMutation,
  useDeleteNoteMutation,
} = noteApi;
