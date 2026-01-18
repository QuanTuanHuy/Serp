/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel API endpoints
*/

import { api } from '@/lib/store/api';
import type {
  Channel,
  CreateChannelRequest,
  UpdateChannelRequest,
  ChannelFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';
import { transformChannel } from './transformers';

export const channelApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // ==================== Channels ====================

    /**
     * Get list of channels with filters and pagination
     */
    getChannels: builder.query<
      APIResponse<PaginatedResponse<Channel>>,
      { filters?: ChannelFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/channels',
        params: {
          page: pagination.page - 1,
          size: pagination.limit,
          type: filters.type,
          isArchived: filters.isArchived,
          entityType: filters.entityType,
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: {
          ...response.data,
          items: response.data.items.map(transformChannel),
        },
      }),
      providesTags: (result) =>
        result?.data?.items
          ? [
              ...result.data.items.map(({ id }) => ({
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
      query: (id) => ({
        url: `/channels/${id}`,
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformChannel(response.data),
      }),
      providesTags: (result, error, id) => [{ type: 'Channel', id }],
    }),

    /**
     * Create new GROUP channel
     */
    createChannel: builder.mutation<APIResponse<Channel>, CreateChannelRequest>(
      {
        query: (request) => ({
          url: '/channels/group',
          method: 'POST',
          body: {
            name: request.name,
            description: request.description,
            memberUserIds: request.memberIds.map((id) => parseInt(id)),
          },
        }),
        extraOptions: { service: 'discuss' },
        transformResponse: (response: any) => ({
          ...response,
          data: transformChannel(response.data),
        }),
        invalidatesTags: [{ type: 'Channel', id: 'LIST' }],
      }
    ),

    /**
     * Create DIRECT channel (1-on-1 chat)
     */
    createDirectChannel: builder.mutation<APIResponse<Channel>, string>({
      query: (otherUserId) => ({
        url: '/channels/direct',
        method: 'POST',
        body: {
          otherUserId: parseInt(otherUserId),
        },
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformChannel(response.data),
      }),
      invalidatesTags: [{ type: 'Channel', id: 'LIST' }],
    }),

    /**
     * Update channel
     */
    updateChannel: builder.mutation<
      APIResponse<Channel>,
      { id: string; data: UpdateChannelRequest }
    >({
      query: ({ id, data }) => ({
        url: `/channels/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'discuss' },
      transformResponse: (response: any) => ({
        ...response,
        data: transformChannel(response.data),
      }),
      invalidatesTags: (result, error, { id }) => [
        { type: 'Channel', id },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Archive channel
     */
    archiveChannel: builder.mutation<APIResponse<void>, string>({
      query: (id) => ({
        url: `/channels/${id}/archive`,
        method: 'POST',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, id) => [
        { type: 'Channel', id },
        { type: 'Channel', id: 'LIST' },
      ],
    }),

    /**
     * Delete channel
     */
    deleteChannel: builder.mutation<APIResponse<void>, string>({
      query: (id) => ({
        url: `/channels/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: [{ type: 'Channel', id: 'LIST' }],
    }),

    /**
     * Get channel members
     */
    getChannelMembers: builder.query<APIResponse<any[]>, string>({
      query: (channelId) => ({
        url: `/channels/${channelId}/members`,
      }),
      extraOptions: { service: 'discuss' },
      providesTags: (result, error, channelId) => [
        { type: 'Channel', id: channelId },
      ],
    }),

    /**
     * Add member to channel
     */
    addChannelMember: builder.mutation<
      APIResponse<void>,
      { channelId: string; userId: string }
    >({
      query: ({ channelId, userId }) => ({
        url: `/channels/${channelId}/members`,
        method: 'POST',
        params: {
          userId: parseInt(userId),
        }
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Channel', id: channelId },
      ],
    }),

    /**
     * Remove member from channel
     */
    removeChannelMember: builder.mutation<
      APIResponse<void>,
      { channelId: string; userId: string }
    >({
      query: ({ channelId, userId }) => ({
        url: `/channels/${channelId}/members/${userId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, { channelId }) => [
        { type: 'Channel', id: channelId },
      ],
    }),

    /**
     * Leave channel
     */
    leaveChannel: builder.mutation<APIResponse<void>, string>({
      query: (channelId) => ({
        url: `/channels/${channelId}/leave`,
        method: 'POST',
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: (result, error, channelId) => [
        { type: 'Channel', id: channelId },
        { type: 'Channel', id: 'LIST' },
      ],
    }),
  }),
});

export const {
  useGetChannelsQuery,
  useLazyGetChannelsQuery,
  useGetChannelQuery,
  useCreateChannelMutation,
  useCreateDirectChannelMutation,
  useUpdateChannelMutation,
  useArchiveChannelMutation,
  useDeleteChannelMutation,
  useGetChannelMembersQuery,
  useAddChannelMemberMutation,
  useRemoveChannelMemberMutation,
  useLeaveChannelMutation,
} = channelApi;
