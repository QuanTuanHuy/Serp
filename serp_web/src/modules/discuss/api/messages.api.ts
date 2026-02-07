/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message API endpoints
Refactored to use cursor-based pagination without optimistic updates
*/

import { api } from '@/lib/store/api';
import type {
  Message,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';
import { transformMessage } from './transformers';

export const messageApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ==================== Messages ====================

    /**
     * Get messages for a channel with pagination
     */
    getMessages: builder.query<
      APIResponse<PaginatedResponse<Message>>,
      { channelId: string; pagination: PaginationParams }
    >({
      query: ({ channelId, pagination }) => ({
        url: `/channels/${channelId}/messages`,
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
          items: response.data.items.map(transformMessage),
        },
      }),
      providesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Get messages before a specific message (infinite scroll - cursor-based)
     * Backend returns List<MessageResponse>, not PaginatedResponse
     */
    getMessagesBefore: builder.query<
      APIResponse<Message[]>,
      { channelId: string; beforeId: string; limit: number }
    >({
      query: ({ channelId, beforeId, limit }) => ({
        url: `/channels/${channelId}/messages/before/${beforeId}`,
        params: { limit },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: response.data.map(transformMessage),
      }),
    }),

    /**
     * Send text message
     * No optimistic updates - waits for server response
     */
    sendMessage: builder.mutation<
      APIResponse<Message>,
      {
        channelId: string;
        content: string;
        parentId?: string;
        currentUserId: string;
        senderInfo?: {
          id: string;
          name: string;
          email: string;
          avatarUrl?: string;
        };
      }
    >({
      query: ({ channelId, content, parentId }) => ({
        url: `/channels/${channelId}/messages`,
        method: 'POST',
        body: {
          content,
          parentId: parentId ? parseInt(parentId) : undefined,
          type: 'STANDARD',
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformMessage(response.data),
      }),
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Send message with file attachments (multipart/form-data)
     */
    sendMessageWithFiles: builder.mutation<
      APIResponse<Message>,
      {
        channelId: string;
        content?: string;
        files: File[];
        parentId?: string;
      }
    >({
      query: ({ channelId, content, files, parentId }) => {
        const formData = new FormData();

        // Add content if provided
        if (content && content.trim()) {
          formData.append('content', content.trim());
        }

        // Add parentId if provided
        if (parentId) {
          formData.append('parentId', parentId);
        }

        // Add all files with the same key 'files' (backend expects List<MultipartFile>)
        files.forEach((file) => {
          formData.append('files', file);
        });

        return {
          url: `/channels/${channelId}/messages/with-files`,
          method: 'POST',
          body: formData,
          // RTK Query automatically sets Content-Type: multipart/form-data for FormData
        };
      },
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformMessage(response.data),
      }),
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Send reply to a message
     */
    sendReply: builder.mutation<
      APIResponse<Message>,
      {
        channelId: string;
        content: string;
        parentId: string;
      }
    >({
      query: ({ channelId, content, parentId }) => ({
        url: `/channels/${channelId}/messages/replies`,
        method: 'POST',
        body: {
          content,
          parentId: parseInt(parentId),
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformMessage(response.data),
      }),
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Get thread replies for a message
     */
    getThreadReplies: builder.query<
      APIResponse<PaginatedResponse<Message>>,
      { channelId: string; messageId: string; pagination: PaginationParams }
    >({
      query: ({ channelId, messageId, pagination }) => ({
        url: `/channels/${channelId}/messages/${messageId}/replies`,
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
          items: response.data.items.map(transformMessage),
        },
      }),
      providesTags: (result, error, { messageId }) => [
        { type: 'Message', id: `THREAD-${messageId}` },
      ],
    }),

    /**
     * Edit message
     * No optimistic updates - waits for server response
     */
    editMessage: builder.mutation<
      APIResponse<Message>,
      { channelId: string; messageId: string; content: string }
    >({
      query: ({ channelId, messageId, content }) => ({
        url: `/channels/${channelId}/messages/${messageId}`,
        method: 'PUT',
        body: { content },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformMessage(response.data),
      }),
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
        { type: 'Channel', id: channelId },
      ],
    }),

    /**
     * Delete message
     * No optimistic updates - waits for server response
     */
    deleteMessage: builder.mutation<
      APIResponse<void>,
      { channelId: string; messageId: string }
    >({
      query: ({ channelId, messageId }) => ({
        url: `/channels/${channelId}/messages/${messageId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
        { type: 'Channel', id: channelId },
      ],
    }),

    /**
     * Add reaction to message
     * No optimistic updates - waits for server response
     */
    addReaction: builder.mutation<
      APIResponse<void>,
      {
        channelId: string;
        messageId: string;
        emoji: string;
        currentUserId: string;
      }
    >({
      query: ({ channelId, messageId, emoji }) => ({
        url: `/channels/${channelId}/messages/${messageId}/reactions`,
        method: 'POST',
        body: { emoji },
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Remove reaction from message
     * No optimistic updates - waits for server response
     */
    removeReaction: builder.mutation<
      APIResponse<void>,
      {
        channelId: string;
        messageId: string;
        emoji: string;
        currentUserId: string;
      }
    >({
      query: ({ channelId, messageId, emoji }) => ({
        url: `/channels/${channelId}/messages/${messageId}/reactions`,
        method: 'DELETE',
        params: { emoji: encodeURIComponent(emoji) },
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Mark messages as read up to a specific message
     */
    markAsRead: builder.mutation<
      APIResponse<void>,
      { channelId: string; messageId: string }
    >({
      query: ({ channelId, messageId }) => ({
        url: `/channels/${channelId}/messages/${messageId}/read`,
        method: 'POST',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Get unread message count for a channel
     */
    getUnreadCount: builder.query<APIResponse<{ count: number }>, string>({
      query: (channelId) => ({
        url: `/channels/${channelId}/messages/unread/count`,
      }),
      extraOptions: { service: 'discuss' },
      providesTags: (result, error, channelId) => [
        { type: 'Channel', id: channelId },
      ],
    }),

    /**
     * Send typing indicator
     */
    sendTypingIndicator: builder.mutation<
      APIResponse<void>,
      { channelId: string }
    >({
      query: ({ channelId }) => ({
        url: `/channels/${channelId}/messages/typing`,
        method: 'POST',
      }),
      extraOptions: { service: 'discuss' },
    }),

    /**
     * Get users currently typing in a channel
     */
    getTypingUsers: builder.query<APIResponse<any[]>, string>({
      query: (channelId) => ({
        url: `/channels/${channelId}/messages/typing`,
      }),
      extraOptions: { service: 'discuss' },
    }),

    /**
     * Search messages in a channel
     */
    searchMessages: builder.query<
      APIResponse<PaginatedResponse<Message>>,
      { channelId: string; query: string; pagination: PaginationParams }
    >({
      query: ({ channelId, query, pagination }) => ({
        url: `/channels/${channelId}/messages/search`,
        params: {
          query,
          page: pagination.page - 1,
          size: pagination.limit,
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: {
          ...response.data,
          items: response.data.items.map(transformMessage),
        },
      }),
      providesTags: ['Message'],
    }),
  }),
});

export const {
  useGetMessagesQuery,
  useLazyGetMessagesQuery,
  useGetMessagesBeforeQuery,
  useLazyGetMessagesBeforeQuery,
  useSendMessageMutation,
  useSendMessageWithFilesMutation,
  useSendReplyMutation,
  useGetThreadRepliesQuery,
  useEditMessageMutation,
  useDeleteMessageMutation,
  useAddReactionMutation,
  useRemoveReactionMutation,
  useMarkAsReadMutation,
  useGetUnreadCountQuery,
  useSendTypingIndicatorMutation,
  useGetTypingUsersQuery,
  useSearchMessagesQuery,
} = messageApi;
