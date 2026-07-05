/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Specific Notes API
 */

import { api } from '@/lib/store/api';
import { mapNoteListResponse, mapSingleNoteResponse } from './mappers';
import type { Note, CreateNoteRequest, UpdateNoteRequest, APIResponse, PaginatedResponse } from '../types';

export const noteApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCrmNotes: builder.query<
      APIResponse<PaginatedResponse<Note>>,
      { entityType: string; entityId: string; page?: number; size?: number }
    >({
      query: ({ entityType, entityId, page = 1, size = 20 }) => ({
        url: '/notes',
        method: 'GET',
        params: { entityType, entityId, page, size },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapNoteListResponse,
      providesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note' as const, id: `${entityType}-${entityId}-LIST` },
      ],
    }),

    createCrmNote: builder.mutation<APIResponse<Note>, CreateNoteRequest>({
      query: (data) => ({
        url: '/notes',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleNoteResponse,
      invalidatesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note', id: `${entityType}-${entityId}-LIST` },
      ],
    }),

    updateCrmNote: builder.mutation<
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
      transformResponse: mapSingleNoteResponse,
      invalidatesTags: (result, error, { entityType, entityId }) => [
        { type: 'Note', id: `${entityType}-${entityId}-LIST` },
      ],
    }),

    deleteCrmNote: builder.mutation<
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
  useGetCrmNotesQuery,
  useCreateCrmNoteMutation,
  useUpdateCrmNoteMutation,
  useDeleteCrmNoteMutation,
} = noteApi;
