/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message API endpoints
Optimized with RTK Query best practices:
- Optimistic updates for reactions, edit, delete
- Direct cache updates instead of invalidation for better UX
*/

import { api } from '@/lib/store/api';
import type {
  Message,
  MessageReaction,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';
import { transformMessage } from './transformers';

// Helper to find cache entries for a channel
const findMessagesCacheEntry = (
  state: any,
  channelId: string
): { page: number; limit: number } | undefined => {
  // Get all query cache entries for getMessages
  const queries = state.api?.queries;
  if (!queries) return undefined;

  // Find matching cache entry for this channel
  for (const key of Object.keys(queries)) {
    if (key.startsWith('getMessages(')) {
      const entry = queries[key];
      if (entry?.originalArgs?.channelId === channelId) {
        return {
          page: entry.originalArgs.pagination.page,
          limit: entry.originalArgs.pagination.limit,
        };
      }
    }
  }
  return undefined;
};

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
     * Get messages before a specific message (infinite scroll)
     */
    getMessagesBefore: builder.query<
      APIResponse<PaginatedResponse<Message>>,
      { channelId: string; beforeId: string; limit: number }
    >({
      query: ({ channelId, beforeId, limit }) => ({
        url: `/channels/${channelId}/messages/before/${beforeId}`,
        params: { size: limit },
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
     * Send text message
     * Uses optimistic update - adds message to cache immediately with pending state
     */
    sendMessage: builder.mutation<
      APIResponse<Message>,
      {
        channelId: string;
        content: string;
        parentId?: string;
        currentUserId: string;
        senderInfo?: { id: string; name: string; email: string; avatarUrl?: string };
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
      // Update channel list to show latest message, but don't refetch messages
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
      async onQueryStarted(
        { channelId, content, parentId, currentUserId, senderInfo },
        { dispatch, queryFulfilled, getState }
      ) {
        const cacheInfo = findMessagesCacheEntry(getState(), channelId);
        if (!cacheInfo) return;

        // Create optimistic message with temporary ID
        const tempId = `temp-${Date.now()}`;
        const optimisticMessage: Message = {
          id: tempId,
          channelId,
          senderId: currentUserId,
          tenantId: '',
          content,
          messageType: 'STANDARD',
          type: 'TEXT',
          parentId,
          threadCount: 0,
          mentions: [],
          reactions: [],
          attachments: [],
          isEdited: false,
          isDeleted: false,
          readCount: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          isSentByMe: true,
          sender: senderInfo || { id: currentUserId, name: 'Me', email: '' },
        };

        const patchResult = dispatch(
          messageApi.util.updateQueryData(
            'getMessages',
            { channelId, pagination: cacheInfo },
            (draft) => {
              // Add message at the end (newest messages at end)
              draft.data?.items?.push(optimisticMessage);
            }
          )
        );

        try {
          const { data: result } = await queryFulfilled;
          // Replace temp message with real message from server
          dispatch(
            messageApi.util.updateQueryData(
              'getMessages',
              { channelId, pagination: cacheInfo },
              (draft) => {
                const index = draft.data?.items?.findIndex((m) => m.id === tempId);
                if (index !== undefined && index !== -1 && draft.data?.items) {
                  draft.data.items[index] = result.data;
                }
              }
            )
          );
        } catch {
          // Rollback on error
          patchResult.undo();
        }
      },
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
     * Uses optimistic update - updates message content immediately
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
      // No invalidation - use optimistic update instead
      async onQueryStarted(
        { channelId, messageId, content },
        { dispatch, queryFulfilled, getState }
      ) {
        const cacheInfo = findMessagesCacheEntry(getState(), channelId);
        if (!cacheInfo) return;

        const patchResult = dispatch(
          messageApi.util.updateQueryData(
            'getMessages',
            { channelId, pagination: cacheInfo },
            (draft) => {
              const message = draft.data?.items?.find(
                (m) => m.id === messageId
              );
              if (message) {
                message.content = content;
                message.isEdited = true;
                message.editedAt = new Date().toISOString();
              }
            }
          )
        );

        try {
          await queryFulfilled;
        } catch {
          patchResult.undo();
        }
      },
    }),

    /**
     * Delete message
     * Uses optimistic update - removes message from cache immediately
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
      // Update channel info but not refetch messages
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Channel', id: channelId },
      ],
      async onQueryStarted(
        { channelId, messageId },
        { dispatch, queryFulfilled, getState }
      ) {
        const cacheInfo = findMessagesCacheEntry(getState(), channelId);
        if (!cacheInfo) return;

        const patchResult = dispatch(
          messageApi.util.updateQueryData(
            'getMessages',
            { channelId, pagination: cacheInfo },
            (draft) => {
              if (draft.data?.items) {
                const index = draft.data.items.findIndex(
                  (m) => m.id === messageId
                );
                if (index !== -1) {
                  // Mark as deleted instead of removing (for UI animation)
                  draft.data.items[index].isDeleted = true;
                  draft.data.items[index].content = 'This message was deleted';
                  draft.data.items[index].deletedAt = new Date().toISOString();
                }
              }
            }
          )
        );

        try {
          await queryFulfilled;
        } catch {
          patchResult.undo();
        }
      },
    }),

    /**
     * Add reaction to message
     * Uses optimistic update - updates cache immediately, rolls back on error
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
      // Optimistic update - no invalidation needed
      async onQueryStarted(
        { channelId, messageId, emoji, currentUserId },
        { dispatch, queryFulfilled, getState }
      ) {
        // Find the cache entry for this channel's messages
        const cacheInfo = findMessagesCacheEntry(getState(), channelId);
        if (!cacheInfo) return;

        // Optimistically update the cache
        const patchResult = dispatch(
          messageApi.util.updateQueryData(
            'getMessages',
            { channelId, pagination: cacheInfo },
            (draft) => {
              const message = draft.data?.items?.find(
                (m) => m.id === messageId
              );
              if (message) {
                const existingReaction = message.reactions.find(
                  (r) => r.emoji === emoji
                );
                if (existingReaction) {
                  // Add user to existing reaction if not already present
                  if (!existingReaction.userIds.includes(currentUserId)) {
                    existingReaction.userIds.push(currentUserId);
                    existingReaction.count += 1;
                  }
                } else {
                  // Create new reaction
                  message.reactions.push({
                    emoji,
                    userIds: [currentUserId],
                    count: 1,
                  });
                }
              }
            }
          )
        );

        try {
          await queryFulfilled;
        } catch {
          // Rollback on error
          patchResult.undo();
        }
      },
    }),

    /**
     * Remove reaction from message
     * Uses optimistic update - updates cache immediately, rolls back on error
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
        params: { emoji },
      }),
      extraOptions: { service: 'discuss' },
      // Optimistic update - no invalidation needed
      async onQueryStarted(
        { channelId, messageId, emoji, currentUserId },
        { dispatch, queryFulfilled, getState }
      ) {
        const cacheInfo = findMessagesCacheEntry(getState(), channelId);
        if (!cacheInfo) return;

        const patchResult = dispatch(
          messageApi.util.updateQueryData(
            'getMessages',
            { channelId, pagination: cacheInfo },
            (draft) => {
              const message = draft.data?.items?.find(
                (m) => m.id === messageId
              );
              if (message) {
                const reactionIndex = message.reactions.findIndex(
                  (r) => r.emoji === emoji
                );
                if (reactionIndex !== -1) {
                  const reaction = message.reactions[reactionIndex];
                  const userIndex = reaction.userIds.indexOf(currentUserId);
                  if (userIndex !== -1) {
                    reaction.userIds.splice(userIndex, 1);
                    reaction.count -= 1;
                    // Remove reaction entirely if no users left
                    if (reaction.count <= 0) {
                      message.reactions.splice(reactionIndex, 1);
                    }
                  }
                }
              }
            }
          )
        );

        try {
          await queryFulfilled;
        } catch {
          patchResult.undo();
        }
      },
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
