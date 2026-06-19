/*
Author: QuanTuanHuy
Description: Part of Serp Project - Attachment API endpoints
*/

import { api } from '@/lib/store/api';
import type {
  Attachment,
  APIResponse,
  PaginatedResponse,
  PaginationParams,
} from '../types';
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
        data: response.data?.map(transformAttachment) || [],
      }),
    }),

    /**
     * Get paginated attachment history for a channel.
     */
    getChannelAttachments: builder.query<
      APIResponse<PaginatedResponse<Attachment>>,
      { channelId: string; pagination: PaginationParams }
    >({
      query: ({ channelId, pagination }) => ({
        url: `/channels/${channelId}/attachments`,
        params: {
          page: pagination.page - 1,
          size: pagination.limit,
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: {
          ...response.data,
          items: response.data?.items?.map(transformAttachment) || [],
        },
      }),
      providesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-FILES-${channelId}` },
      ],
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
  useGetChannelAttachmentsQuery,
  useGetAttachmentDownloadUrlQuery,
  useLazyGetAttachmentDownloadUrlQuery,
  useDeleteAttachmentMutation,
} = attachmentApi;
