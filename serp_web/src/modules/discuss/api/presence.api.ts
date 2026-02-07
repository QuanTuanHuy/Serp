/*
Author: QuanTuanHuy
Description: Part of Serp Project - Presence API endpoints
*/

import { api } from '@/lib/store/api';
import type { APIResponse } from '../types';

// ==================== Types ====================

export type UserStatus = 'ONLINE' | 'BUSY' | 'OFFLINE';

export interface UserPresenceResponse {
  userId: number;
  userName: string;
  avatarUrl: string;
  status: UserStatus;
  statusMessage: string;
  lastSeenAt: number;
  isOnline: boolean;
  lastSeenText: string;
}

export interface ChannelPresenceResponse {
  channelId: number;
  totalMembers: number;
  onlineCount: number;
  statusGroups: Record<string, UserPresenceResponse[]>;
}

export interface UpdatePresenceRequest {
  status: UserStatus;
  statusMessage?: string;
}

// ==================== API Endpoints ====================

export const presenceApi = api.injectEndpoints({
  endpoints: (builder) => ({
    /**
     * Get presence info for all members of a channel
     */
    getChannelPresence: builder.query<
      APIResponse<ChannelPresenceResponse>,
      string
    >({
      query: (channelId) => ({
        url: `/channels/${channelId}/presence`,
      }),
      extraOptions: { service: 'discuss' },
      providesTags: (result, error, channelId) => [
        { type: 'Presence', id: `CHANNEL-${channelId}` },
        'Presence',
      ],
    }),

    /**
     * Get presence info for a specific user
     */
    getUserPresence: builder.query<APIResponse<UserPresenceResponse>, string>({
      query: (userId) => ({
        url: `/users/${userId}/presence`,
      }),
      extraOptions: { service: 'discuss' },
      providesTags: (result, error, userId) => [
        { type: 'Presence', id: `USER-${userId}` },
        'Presence',
      ],
    }),

    /**
     * Get current user's own presence info
     */
    getMyPresence: builder.query<APIResponse<UserPresenceResponse>, void>({
      query: () => ({
        url: `/users/me/presence`,
      }),
      extraOptions: { service: 'discuss' },
      providesTags: [{ type: 'Presence', id: 'ME' }],
    }),

    /**
     * Update current user's presence status
     */
    updateMyPresence: builder.mutation<
      APIResponse<UserPresenceResponse>,
      UpdatePresenceRequest
    >({
      query: (body) => ({
        url: `/users/me/presence`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'discuss' },
      invalidatesTags: [{ type: 'Presence', id: 'ME' }],
    }),
  }),
});

export const {
  useGetChannelPresenceQuery,
  useGetUserPresenceQuery,
  useGetMyPresenceQuery,
  useUpdateMyPresenceMutation,
} = presenceApi;
