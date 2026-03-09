/*
Author: QuanTuanHuy
Description: Part of Serp Project - Attachment API endpoints
*/

import { api } from '@/lib/store/api';
import type { Attachment, APIResponse } from '../types';
import { transformAttachment } from './transformers';

export const attachmentApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ==================== Attachments ====================

    /**
     * Get attachment by ID
     */
    getAttachment: builder.query<APIResponse<Attachment>, string>({
      query: (attachmentId) => ({
        url: `/attachments/${attachmentId}`,
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformAttachment(response.data),
      }),
    }),

    /**
     * Get all attachments for a message
     */
    getMessageAttachments: builder.query<APIResponse<Attachment[]>, string>({
      query: (messageId) => ({
        url: `/attachments/message/${messageId}`,
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: response.data.map(transformAttachment),
      }),
    }),

    /**
     * Get download URL for an attachment (presigned URL)
     */
    getAttachmentDownloadUrl: builder.query<
      APIResponse<{ downloadUrl: string }>,
      string
    >({
      query: (attachmentId) => ({
        url: `/attachments/${attachmentId}/download-url`,
      }),
      extraOptions: { service: 'discuss' },
    }),

    /**
     * Delete attachment
     */
    deleteAttachment: builder.mutation<APIResponse<void>, string>({
      query: (attachmentId) => ({
        url: `/attachments/${attachmentId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: ['Message'],
    }),
  }),
});

export const {
  useGetAttachmentQuery,
  useGetMessageAttachmentsQuery,
  useGetAttachmentDownloadUrlQuery,
  useDeleteAttachmentMutation,
} = attachmentApi;
