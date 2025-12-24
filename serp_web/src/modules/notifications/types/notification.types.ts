/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification types
 */

// Enums
export type NotificationType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';
export type NotificationCategory = 'SYSTEM' | 'EMAIL' | 'CRM' | 'PTM';
export type NotificationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';
export type DeliveryChannel = 'IN_APP' | 'EMAIL' | 'PUSH' | 'SMS';

// Entity
export interface NotificationResponse {
  id: number;
  userId: number;
  tenantId: number;
  title: string;
  message: string;
  type: NotificationType;
  category: NotificationCategory;
  priority: NotificationPriority;
  sourceService: string;
  sourceEventId?: string;
  actionUrl?: string;
  actionType?: string;
  entityType?: string;
  entityId?: number;
  isRead: boolean;
  readAt?: number;
  isArchived: boolean;
  status: NotificationStatus;
  deliveryChannels?: DeliveryChannel[];
  deliveryAt?: number;
  expireAt?: number;
  metadata?: Record<string, any>;
  createdAt: number;
  updatedAt?: number;
}

// Update Request
export interface UpdateNotificationRequest {
  isRead?: boolean;
  isArchived?: boolean;
}

// List Response
export interface NotificationListResponse {
  notifications: NotificationResponse[];
  totalCount: number;
  unreadCount: number;
  page: number;
  pageSize: number;
}

// Query Params
export interface GetNotificationParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  type?: NotificationType;
  category?: NotificationCategory;
  priority?: NotificationPriority;
  isRead?: boolean;
}

// WebSocket Message Types
export type WSMessageType =
  | 'PING'
  | 'PONG'
  | 'ACK'
  | 'SUBSCRIBE'
  | 'UNSUBSCRIBE'
  | 'SUBSCRIPTION_CONFIRMED'
  | 'NEW_NOTIFICATION'
  | 'INITIAL_DATA';

export interface WSMessage<T = any> {
  type: WSMessageType;
  payload?: T;
  timestamp: number;
  messageId?: string;
}

export interface UnreadCountPayload {
  totalUnread: number;
  byCategory: Record<string, number>;
  hasUrgent: boolean;
}

export interface SubscribePayload {
  categories: string[];
}

export interface AckPayload {
  messageId: string;
}

// Notification State
export interface NotificationState {
  notifications: NotificationResponse[];
  unreadCount: number;
  unreadByCategory: Record<string, number>;
  hasUrgent: boolean;
  totalCount: number;
  page: number;
  pageSize: number;
  isLoading: boolean;
  wsConnected: boolean;
  error: string | null;
}
