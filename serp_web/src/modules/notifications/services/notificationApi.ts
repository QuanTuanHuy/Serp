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
import { createDataTransform } from '@/lib/store/api/utils';

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
      extraOptions: { service: 'ns' },
      providesTags: (result) =>
        result
          ? [
              ...result.notifications.map(({ id }) => ({
                type: 'Notification' as const,
                id,
              })),
              { type: 'Notification' as const, id: 'LIST' },
            ]
          : [{ type: 'Notification' as const, id: 'LIST' }],
      transformResponse: createDataTransform<NotificationListResponse>(),
    }),

    getNotificationById: builder.query<NotificationResponse, number>({
      query: (id) => ({
        url: `/notifications/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'ns' },
      providesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
      ],
      transformResponse: createDataTransform<NotificationResponse>(),
    }),

    markNotificationAsRead: builder.mutation<NotificationResponse, number>({
      query: (id) => ({
        url: `/notifications/${id}`,
        method: 'PATCH',
        body: {
          isRead: true,
        },
      }),
      extraOptions: { service: 'ns' },
      invalidatesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
        { type: 'Notification' as const, id: 'LIST' },
      ],
      transformResponse: createDataTransform<NotificationResponse>(),
    }),

    markAllNotificationsAsRead: builder.mutation<void, void>({
      query: () => ({
        url: '/notifications/read-all',
        method: 'PATCH',
      }),
      extraOptions: { service: 'ns' },
      invalidatesTags: [{ type: 'Notification' as const, id: 'LIST' }],
    }),

    archiveNotification: builder.mutation<NotificationResponse, number>({
      query: (id) => ({
        url: `/notifications/${id}`,
        method: 'PATCH',
        body: {
          isArchived: true,
        },
      }),
      extraOptions: { service: 'ns' },
      invalidatesTags: (result, error, id) => [
        { type: 'Notification' as const, id },
        { type: 'Notification' as const, id: 'LIST' },
      ],
      transformResponse: createDataTransform<NotificationResponse>(),
    }),

    deleteNotification: builder.mutation<void, number>({
      query: (id) => ({
        url: `/notifications/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'ns' },
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
