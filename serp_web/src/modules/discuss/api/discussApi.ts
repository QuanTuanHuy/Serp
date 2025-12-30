/*
Author: QuanTuanHuy
Description: Part of Serp Project - RTK Query API for Discuss module
*/

import { api } from '@/lib/store/api';
import type {
  Channel,
  Message,
  Activity,
  UserPresence,
  Attachment,
  CreateChannelRequest,
  UpdateChannelRequest,
  SendMessageRequest,
  EditMessageRequest,
  AddReactionRequest,
  UploadAttachmentRequest,
  ChannelFilters,
  MessageFilters,
  ActivityFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';
import {
  MOCK_CHANNELS,
  MOCK_MESSAGES,
  MOCK_ACTIVITIES,
  MOCK_PRESENCE,
  getChannelById,
  getMessagesByChannelId,
  createMockMessage,
  createMockChannel,
  CURRENT_USER_ID,
} from '../mocks/mockData';

// ==================== Mock API Delay ====================
const MOCK_DELAY = 500; // 500ms delay to simulate network

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// ==================== Discuss API ====================

export const discussApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ==================== Channels ====================

    /**
     * Get list of channels with filters and pagination
     */
    getChannels: builder.query<
      APIResponse<PaginatedResponse<Channel>>,
      { filters?: ChannelFilters; pagination: PaginationParams }
    >({
      queryFn: async ({ filters = {}, pagination }) => {
        await delay(MOCK_DELAY);

        let filtered = [...MOCK_CHANNELS];

        // Apply filters
        if (filters.type) {
          filtered = filtered.filter((ch) => ch.type === filters.type);
        }
        if (filters.search) {
          const search = filters.search.toLowerCase();
          filtered = filtered.filter((ch) =>
            ch.name.toLowerCase().includes(search)
          );
        }
        if (typeof filters.isArchived === 'boolean') {
          filtered = filtered.filter(
            (ch) => ch.isArchived === filters.isArchived
          );
        }
        if (filters.entityType) {
          filtered = filtered.filter(
            (ch) => ch.entityType === filters.entityType
          );
        }

        // Apply pagination
        const { page, limit } = pagination;
        const start = (page - 1) * limit;
        const end = start + limit;
        const paginated = filtered.slice(start, end);

        return {
          data: {
            success: true,
            message: 'Channels retrieved successfully',
            data: {
              data: paginated,
              pagination: {
                page,
                limit,
                total: filtered.length,
                totalPages: Math.ceil(filtered.length / limit),
              },
            },
          },
        };
      },
      // Mock: No real service routing needed
      // extraOptions: { service: 'discuss' },
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Channel' as const,
                id,
              })),
              { type: 'Channel', id: 'LIST' },
            ]
          : [{ type: 'Channel', id: 'LIST' }],
    }),

    /**
     * Get single channel by ID
     */
    getChannel: builder.query<APIResponse<Channel>, string>({
      queryFn: async (id) => {
        await delay(MOCK_DELAY);

        const channel = getChannelById(id);
        if (!channel) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Channel not found' },
            },
          };
        }

        return {
          data: {
            success: true,
            message: 'Channel retrieved successfully',
            data: channel,
          },
        };
      },
      providesTags: (result, error, id) => [{ type: 'Channel', id }],
    }),

    /**
     * Create new channel
     */
    createChannel: builder.mutation<APIResponse<Channel>, CreateChannelRequest>(
      {
        queryFn: async (request) => {
          await delay(MOCK_DELAY);

          const newChannel = createMockChannel(
            request.name,
            request.type,
            request.memberIds
          );

          if (request.description) {
            newChannel.description = request.description;
          }
          if (request.entityType) {
            newChannel.entityType = request.entityType;
          }
          if (request.entityId) {
            newChannel.entityId = request.entityId;
          }

          // Add to mock data
          MOCK_CHANNELS.unshift(newChannel);

          return {
            data: {
              success: true,
              message: 'Channel created successfully',
              data: newChannel,
            },
          };
        },
        invalidatesTags: [{ type: 'Channel', id: 'LIST' }],
      }
    ),

    /**
     * Update channel
     */
    updateChannel: builder.mutation<
      APIResponse<Channel>,
      { id: string; data: UpdateChannelRequest }
    >({
      queryFn: async ({ id, data }) => {
        await delay(MOCK_DELAY);

        const channel = getChannelById(id);
        if (!channel) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Channel not found' },
            },
          };
        }

        // Update channel
        const updated = {
          ...channel,
          ...data,
          updatedAt: new Date().toISOString(),
        };

        // Update in mock data
        const index = MOCK_CHANNELS.findIndex((ch) => ch.id === id);
        if (index !== -1) {
          MOCK_CHANNELS[index] = updated;
        }

        return {
          data: {
            success: true,
            message: 'Channel updated successfully',
            data: updated,
          },
        };
      },
      invalidatesTags: (result, error, { id }) => [
        { type: 'Channel', id },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Archive channel
     */
    archiveChannel: builder.mutation<APIResponse<Channel>, string>({
      queryFn: async (id) => {
        await delay(MOCK_DELAY);

        const channelIndex = MOCK_CHANNELS.findIndex((ch) => ch.id === id);
        if (channelIndex === -1) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Channel not found' },
            },
          };
        }

        const channel = {
          ...MOCK_CHANNELS[channelIndex],
          isArchived: true,
          updatedAt: new Date().toISOString(),
        };
        MOCK_CHANNELS[channelIndex] = channel;

        return {
          data: {
            success: true,
            message: 'Channel archived successfully',
            data: channel,
          },
        };
      },
      invalidatesTags: (result, error, id) => [
        { type: 'Channel', id },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    // ==================== Messages ====================

    /**
     * Get messages for a channel
     */
    getMessages: builder.query<
      APIResponse<PaginatedResponse<Message>>,
      {
        channelId: string;
        filters?: MessageFilters;
        pagination: PaginationParams;
      }
    >({
      queryFn: async ({ channelId, filters = {}, pagination }) => {
        await delay(MOCK_DELAY);

        let messages = getMessagesByChannelId(channelId);

        // Apply filters
        if (filters.userId) {
          messages = messages.filter((msg) => msg.userId === filters.userId);
        }
        if (filters.type) {
          messages = messages.filter((msg) => msg.type === filters.type);
        }
        if (filters.search) {
          const search = filters.search.toLowerCase();
          messages = messages.filter((msg) =>
            msg.content.toLowerCase().includes(search)
          );
        }

        // Sort by createdAt ascending (oldest first for chat display)
        messages.sort(
          (a, b) =>
            new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        );

        // Apply pagination
        const { page, limit } = pagination;
        const start = (page - 1) * limit;
        const end = start + limit;
        const paginated = messages.slice(start, end);

        return {
          data: {
            success: true,
            message: 'Messages retrieved successfully',
            data: {
              data: paginated,
              pagination: {
                page,
                limit,
                total: messages.length,
                totalPages: Math.ceil(messages.length / limit),
              },
            },
          },
        };
      },
      providesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Send message
     */
    sendMessage: builder.mutation<
      APIResponse<Message>,
      {
        channelId: string;
        data: SendMessageRequest;
        attachments?: Attachment[];
      }
    >({
      queryFn: async ({ channelId, data, attachments = [] }) => {
        await delay(MOCK_DELAY);

        const newMessage = createMockMessage(
          channelId,
          data.content,
          attachments
        );

        if (data.type) {
          newMessage.type = data.type;
        }
        if (data.parentId) {
          newMessage.parentId = data.parentId;
        }

        // Add to mock messages
        if (!MOCK_MESSAGES[channelId]) {
          MOCK_MESSAGES[channelId] = [];
        }
        MOCK_MESSAGES[channelId].push(newMessage);

        // Update channel's last message (create new object to avoid mutation)
        const channelIndex = MOCK_CHANNELS.findIndex(
          (ch) => ch.id === channelId
        );
        if (channelIndex !== -1) {
          MOCK_CHANNELS[channelIndex] = {
            ...MOCK_CHANNELS[channelIndex],
            lastMessage: data.content,
            lastMessageAt: newMessage.createdAt,
            updatedAt: newMessage.createdAt,
          };
        }

        return {
          data: {
            success: true,
            message: 'Message sent successfully',
            data: newMessage,
          },
        };
      },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Edit message
     */
    editMessage: builder.mutation<
      APIResponse<Message>,
      { messageId: string; channelId: string; data: EditMessageRequest }
    >({
      queryFn: async ({ messageId, channelId, data }) => {
        await delay(MOCK_DELAY);

        const messages = getMessagesByChannelId(channelId);
        const message = messages.find((msg) => msg.id === messageId);

        if (!message) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Message not found' },
            },
          };
        }

        message.content = data.content;
        message.isEdited = true;
        message.editedAt = new Date().toISOString();
        message.updatedAt = new Date().toISOString();

        return {
          data: {
            success: true,
            message: 'Message edited successfully',
            data: message,
          },
        };
      },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Delete message
     */
    deleteMessage: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      { messageId: string; channelId: string }
    >({
      queryFn: async ({ messageId, channelId }) => {
        await delay(MOCK_DELAY);

        const messages = getMessagesByChannelId(channelId);
        const message = messages.find((msg) => msg.id === messageId);

        if (!message) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Message not found' },
            },
          };
        }

        message.isDeleted = true;
        message.deletedAt = new Date().toISOString();
        message.content = '[Message deleted]';

        return {
          data: {
            success: true,
            message: 'Message deleted successfully',
            data: { deleted: true },
          },
        };
      },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Add reaction to message
     */
    addReaction: builder.mutation<
      APIResponse<Message>,
      { messageId: string; channelId: string; data: AddReactionRequest }
    >({
      queryFn: async ({ messageId, channelId, data }) => {
        await delay(MOCK_DELAY);

        const messages = getMessagesByChannelId(channelId);
        const messageIndex = messages.findIndex((msg) => msg.id === messageId);

        if (messageIndex === -1) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Message not found' },
            },
          };
        }

        const message = messages[messageIndex];

        // Find existing reaction
        const existingReaction = message.reactions.find(
          (r) => r.emoji === data.emoji
        );

        let newReactions;
        if (existingReaction) {
          // Add user to existing reaction
          if (!existingReaction.userIds.includes(CURRENT_USER_ID)) {
            newReactions = message.reactions.map((r) =>
              r.emoji === data.emoji
                ? {
                    ...r,
                    userIds: [...r.userIds, CURRENT_USER_ID],
                    count: r.userIds.length + 1,
                  }
                : r
            );
          } else {
            newReactions = message.reactions;
          }
        } else {
          // Create new reaction
          newReactions = [
            ...message.reactions,
            {
              emoji: data.emoji,
              userIds: [CURRENT_USER_ID],
              count: 1,
            },
          ];
        }

        // Create new message object
        const updatedMessage = {
          ...message,
          reactions: newReactions,
        };

        // Update in MOCK_MESSAGES
        if (!MOCK_MESSAGES[channelId]) {
          MOCK_MESSAGES[channelId] = [];
        }
        MOCK_MESSAGES[channelId][messageIndex] = updatedMessage;

        return {
          data: {
            success: true,
            message: 'Reaction added successfully',
            data: updatedMessage,
          },
        };
      },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Remove reaction from message
     */
    removeReaction: builder.mutation<
      APIResponse<Message>,
      { messageId: string; channelId: string; emoji: string }
    >({
      queryFn: async ({ messageId, channelId, emoji }) => {
        await delay(MOCK_DELAY);

        const messages = getMessagesByChannelId(channelId);
        const messageIndex = messages.findIndex((msg) => msg.id === messageId);

        if (messageIndex === -1) {
          return {
            error: {
              status: 404,
              data: { success: false, message: 'Message not found' },
            },
          };
        }

        const message = messages[messageIndex];
        const reaction = message.reactions.find((r) => r.emoji === emoji);

        let newReactions;
        if (reaction) {
          const newUserIds = reaction.userIds.filter(
            (id) => id !== CURRENT_USER_ID
          );
          const newCount = newUserIds.length;

          // Remove reaction if no users left
          if (newCount === 0) {
            newReactions = message.reactions.filter((r) => r.emoji !== emoji);
          } else {
            newReactions = message.reactions.map((r) =>
              r.emoji === emoji
                ? { ...r, userIds: newUserIds, count: newCount }
                : r
            );
          }
        } else {
          newReactions = message.reactions;
        }

        // Create new message object
        const updatedMessage = {
          ...message,
          reactions: newReactions,
        };

        // Update in MOCK_MESSAGES
        if (!MOCK_MESSAGES[channelId]) {
          MOCK_MESSAGES[channelId] = [];
        }
        MOCK_MESSAGES[channelId][messageIndex] = updatedMessage;

        return {
          data: {
            success: true,
            message: 'Reaction removed successfully',
            data: updatedMessage,
          },
        };
      },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Message', id: `CHANNEL-${channelId}` },
      ],
    }),

    /**
     * Mark messages as read
     */
    markAsRead: builder.mutation<
      APIResponse<{ success: boolean }>,
      { channelId: string }
    >({
      queryFn: async ({ channelId }) => {
        await delay(MOCK_DELAY);

        const channelIndex = MOCK_CHANNELS.findIndex(
          (ch) => ch.id === channelId
        );
        if (channelIndex !== -1) {
          MOCK_CHANNELS[channelIndex] = {
            ...MOCK_CHANNELS[channelIndex],
            unreadCount: 0,
          };
        }

        return {
          data: {
            success: true,
            message: 'Messages marked as read',
            data: { success: true },
          },
        };
      },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    // ==================== Activity Feed ====================

    /**
     * Get activity feed
     */
    getActivityFeed: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { filters?: ActivityFilters; pagination: PaginationParams }
    >({
      queryFn: async ({ filters = {}, pagination }) => {
        await delay(MOCK_DELAY);

        let activities = [...MOCK_ACTIVITIES];

        // Apply filters
        if (filters.action) {
          activities = activities.filter(
            (act) => act.action === filters.action
          );
        }
        if (filters.entityType) {
          activities = activities.filter(
            (act) => act.entityType === filters.entityType
          );
        }
        if (typeof filters.isRead === 'boolean') {
          activities = activities.filter(
            (act) => act.isRead === filters.isRead
          );
        }

        // Sort by createdAt descending
        activities.sort(
          (a, b) =>
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );

        // Apply pagination
        const { page, limit } = pagination;
        const start = (page - 1) * limit;
        const end = start + limit;
        const paginated = activities.slice(start, end);

        return {
          data: {
            success: true,
            message: 'Activity feed retrieved successfully',
            data: {
              data: paginated,
              pagination: {
                page,
                limit,
                total: activities.length,
                totalPages: Math.ceil(activities.length / limit),
              },
            },
          },
        };
      },
      providesTags: [{ type: 'DiscussActivity', id: 'LIST' }],
    }),

    // ==================== Attachments ====================

    /**
     * Upload attachment
     */
    uploadAttachment: builder.mutation<
      APIResponse<Attachment>,
      UploadAttachmentRequest
    >({
      queryFn: async ({ file, channelId }) => {
        await delay(MOCK_DELAY * 2); // Longer delay for file upload

        // Mock file upload - in real app, this would upload to S3/MinIO
        const mockAttachment: Attachment = {
          id: `att-${Date.now()}`,
          messageId: '', // Will be set when message is sent
          fileName: file.name,
          fileType: file.type,
          fileSize: file.size,
          s3Key: `${channelId}/${Date.now()}-${file.name}`,
          s3Bucket: 'discuss-attachments',
          downloadUrl: URL.createObjectURL(file), // Temporary blob URL
          thumbnailUrl: file.type.startsWith('image/')
            ? URL.createObjectURL(file)
            : undefined,
          virusScanStatus: 'CLEAN',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };

        return {
          data: {
            success: true,
            message: 'File uploaded successfully',
            data: mockAttachment,
          },
        };
      },
    }),

    // ==================== Presence ====================

    /**
     * Get online users
     */
    getOnlineUsers: builder.query<APIResponse<UserPresence[]>, string[]>({
      queryFn: async (userIds) => {
        await delay(MOCK_DELAY);

        const presence = MOCK_PRESENCE.filter((p) =>
          userIds.includes(p.userId)
        );

        return {
          data: {
            success: true,
            message: 'Online users retrieved successfully',
            data: presence,
          },
        };
      },
      providesTags: [{ type: 'Presence', id: 'LIST' }],
    }),
  }),
});

// ==================== Export Hooks ====================

export const {
  // Channels
  useGetChannelsQuery,
  useGetChannelQuery,
  useCreateChannelMutation,
  useUpdateChannelMutation,
  useArchiveChannelMutation,

  // Messages
  useGetMessagesQuery,
  useSendMessageMutation,
  useEditMessageMutation,
  useDeleteMessageMutation,
  useAddReactionMutation,
  useRemoveReactionMutation,
  useMarkAsReadMutation,

  // Attachments
  useUploadAttachmentMutation,

  // Activity
  useGetActivityFeedQuery,

  // Presence
  useGetOnlineUsersQuery,
} = discussApi;
