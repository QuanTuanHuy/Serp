/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification API Service
 */

import { api } from '@/lib/store/api';
import {
  NotificationListResponse,
  NotificationResponse,
  GetNotificationParams,
} from '../types/notification.types';

export const notificationApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getNotifications: builder.query<
      NotificationListResponse,
      GetNotificationParams
    >({
      query: (params) => ({
        url: '/notifications',
        params: {
          page: params.page ?? 0,
          pageSize: params.pageSize ?? 10,
          sortBy: params.sortBy ?? 'createdAt',
          sortOrder: params.sortOrder ?? 'DESC',
          ...(params.type && { type: params.type }),
          ...(params.category && { category: params.category }),
          ...(params.priority && { priority: params.priority }),
          ...(params.isRead !== undefined && { isRead: params.isRead }),
        },
      }),
      providesTags: (result) =>
        result
          ? [
              ...result.data.map(({ id }) => ({
                type: 'Notification' as const,
                id,
              })),
              { type: 'Notification' as const, id: 'LIST' },
            ]
          : [{ type: 'Notification' as const, id: 'LIST' }],
    }),

    getNotificationById: builder.query<NotificationResponse, number>({
      query: (id) => `/notifications/${id}`,
      providesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
      ],
    }),

    markNotificationAsRead: builder.mutation<NotificationResponse, number>({
      query: (id) => ({
        url: `/notifications/${id}/read`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
        { type: 'Notification' as const, id: 'LIST' },
      ],
    }),

    markAllNotificationsAsRead: builder.mutation<void, void>({
      query: () => ({
        url: '/notifications/read-all',
        method: 'PATCH',
      }),
      invalidatesTags: [{ type: 'Notification' as const, id: 'LIST' }],
    }),

    archiveNotification: builder.mutation<NotificationResponse, number>({
      query: (id) => ({
        url: `/notifications/${id}/archive`,
        method: 'PATCH',
      }),
      invalidatesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
        { type: 'Notification' as const, id: 'LIST' },
      ],
    }),

    deleteNotification: builder.mutation<void, number>({
      query: (id) => ({
        url: `/notifications/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
        { type: 'Notification' as const, id: 'LIST' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetNotificationsQuery,
  useGetNotificationByIdQuery,
  useMarkNotificationAsReadMutation,
  useMarkAllNotificationsAsReadMutation,
  useArchiveNotificationMutation,
  useDeleteNotificationMutation,
} = notificationApi;
