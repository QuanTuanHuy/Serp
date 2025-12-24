/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification Redux Slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import {
  NotificationState,
  NotificationResponse,
  UnreadCountPayload,
  WSMessage,
} from '../types/notification.types';
import {
  wsMessageReceived,
  wsConnected,
  wsDisconnected,
} from '@/lib/store/actions/websocketActions';

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  unreadByCategory: {},
  hasUrgent: false,
  totalCount: 0,
  page: 0,
  pageSize: 10,
  isLoading: false,
  wsConnected: false,
  error: null,
};

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    setNotifications: (
      state,
      action: PayloadAction<{
        notifications: NotificationResponse[];
        totalCount: number;
        page: number;
        pageSize: number;
      }>
    ) => {
      state.notifications = action.payload.notifications;
      state.totalCount = action.payload.totalCount;
      state.page = action.payload.page;
      state.pageSize = action.payload.pageSize;
    },
    appendNotifications: (
      state,
      action: PayloadAction<NotificationResponse[]>
    ) => {
      state.notifications = [...state.notifications, ...action.payload];
    },
    addNotification: (state, action: PayloadAction<NotificationResponse>) => {
      // Add to the beginning
      state.notifications = [action.payload, ...state.notifications];
      state.totalCount += 1;
      // Update unread count
      if (!action.payload.isRead) {
        state.unreadCount += 1;
        const category = action.payload.category;
        state.unreadByCategory[category] =
          (state.unreadByCategory[category] || 0) + 1;
        if (action.payload.priority === 'URGENT') {
          state.hasUrgent = true;
        }
      }
    },
    updateUnreadCount: (state, action: PayloadAction<UnreadCountPayload>) => {
      state.unreadCount = action.payload.totalUnread;
      state.unreadByCategory = action.payload.byCategory;
      state.hasUrgent = action.payload.hasUrgent;
    },
    markAsRead: (state, action: PayloadAction<number | number[]>) => {
      const ids = Array.isArray(action.payload)
        ? action.payload
        : [action.payload];
      state.notifications = state.notifications.map((n) => {
        if (ids.includes(n.id) && !n.isRead) {
          state.unreadCount = Math.max(0, state.unreadCount - 1);
          if (state.unreadByCategory[n.category]) {
            state.unreadByCategory[n.category] = Math.max(
              0,
              state.unreadByCategory[n.category] - 1
            );
          }
          return { ...n, isRead: true, readAt: Date.now() };
        }
        return n;
      });
      // Recalculate hasUrgent
      state.hasUrgent = state.notifications.some(
        (n) => !n.isRead && n.priority === 'URGENT'
      );
    },
    markAllAsRead: (state) => {
      state.notifications = state.notifications.map((n) => ({
        ...n,
        isRead: true,
        readAt: n.readAt || Date.now(),
      }));
      state.unreadCount = 0;
      state.unreadByCategory = {};
      state.hasUrgent = false;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
    clearNotifications: (state) => {
      state.notifications = [];
      state.totalCount = 0;
      state.page = 0;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(wsConnected, (state) => {
        state.wsConnected = true;
      })
      .addCase(wsDisconnected, (state) => {
        state.wsConnected = false;
      })
      .addCase(wsMessageReceived, (state, action: PayloadAction<WSMessage>) => {
        const message = action.payload;
        switch (message.type) {
          case 'INITIAL_DATA':
            if (message.payload) {
              const payload = message.payload as UnreadCountPayload;
              state.unreadCount = payload.totalUnread;
              state.unreadByCategory = payload.byCategory;
              state.hasUrgent = payload.hasUrgent;
            }
            break;
          case 'NEW_NOTIFICATION':
            if (message.payload) {
              const notification = message.payload as NotificationResponse;
              // Add to the beginning
              state.notifications = [notification, ...state.notifications];
              state.totalCount += 1;
              // Update unread count
              if (!notification.isRead) {
                state.unreadCount += 1;
                const category = notification.category;
                state.unreadByCategory[category] =
                  (state.unreadByCategory[category] || 0) + 1;
                if (notification.priority === 'URGENT') {
                  state.hasUrgent = true;
                }
              }
            }
            break;
        }
      });
  },
});

export const {
  setNotifications,
  appendNotifications,
  addNotification,
  updateUnreadCount,
  markAsRead,
  markAllAsRead,
  setLoading,
  setError,
  clearNotifications,
} = notificationSlice.actions;

// Selectors
export const selectNotifications = (state: {
  notifications: NotificationState;
}) => state.notifications.notifications;
export const selectUnreadCount = (state: {
  notifications: NotificationState;
}) => state.notifications.unreadCount;
export const selectUnreadByCategory = (state: {
  notifications: NotificationState;
}) => state.notifications.unreadByCategory;
export const selectHasUrgent = (state: { notifications: NotificationState }) =>
  state.notifications.hasUrgent;
export const selectWsConnected = (state: {
  notifications: NotificationState;
}) => state.notifications.wsConnected;
export const selectNotificationLoading = (state: {
  notifications: NotificationState;
}) => state.notifications.isLoading;

export default notificationSlice.reducer;
