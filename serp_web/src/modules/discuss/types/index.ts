/*
Author: QuanTuanHuy
Description: Part of Serp Project - TypeScript types for Discuss module
*/

export * from './common';
export * from './channel';
export * from './message';

import type { BaseEntity } from './common';
import type { Channel, ChannelType } from './channel';
import type { Message, MessageType } from './message';

// ==================== Enums ====================

export type ActivityAction =
  | 'MESSAGE_SENT'
  | 'USER_JOINED'
  | 'USER_LEFT'
  | 'CHANNEL_CREATED'
  | 'FILE_SHARED'
  | 'MENTION_RECEIVED';

// ==================== Activity Feed ====================

export interface Activity extends BaseEntity {
  userId: string;
  userName: string;
  userAvatar?: string;
  action: ActivityAction;
  entityType: string; // 'channel', 'message', 'customer', 'task'
  entityId: string;
  entityName: string;
  metadata: Record<string, any>;
  isRead: boolean;
  tenantId: string;
}

// ==================== Presence ====================

export interface UserPresence {
  userId: string;
  userName: string;
  userAvatar?: string;
  isOnline: boolean;
  lastSeenAt?: string;
}

export interface TypingIndicator {
  channelId: string;
  userId: string;
  userName: string;
  isTyping: boolean;
}

// ==================== Filter Types ====================

export interface ActivityFilters {
  action?: ActivityAction;
  entityType?: string;
  isRead?: boolean;
  dateFrom?: string;
  dateTo?: string;
}

// ==================== Search ====================

export interface SearchFilters {
  channelIds?: string[];
  userId?: string;
  dateFrom?: string;
  dateTo?: string;
  hasAttachments?: boolean;
  messageType?: MessageType;
}

export interface SearchResult {
  message: Message;
  channel: Channel;
  highlights: string[];
  relevanceScore: number;
}

export interface GroupedSearchResults {
  channelId: string;
  channelName: string;
  channelType: ChannelType;
  results: SearchResult[];
}

// ==================== WebSocket Events ====================

/** Matches backend WsEvent<T> envelope */
export interface WsEvent<T = any> {
  type: WSEventType;
  payload: T;
  channelId: number | null;
  timestamp: number;
}

export interface MessageReadPayload {
  messageId: string | number;
  channelId: string | number;
  userId: string | number;
  readBy?: Array<string | number>;
  readCount?: number;
}

/** Matches backend WsEventType enum */
export type WSEventType =
  // Message events
  | 'MESSAGE_NEW'
  | 'MESSAGE_UPDATED'
  | 'MESSAGE_DELETED'
  // Reaction events
  | 'REACTION_ADDED'
  | 'REACTION_REMOVED'
  // Typing events
  | 'TYPING_START'
  | 'TYPING_STOP'
  // Presence events
  | 'USER_ONLINE'
  | 'USER_OFFLINE'
  | 'USER_PRESENCE_CHANGED'
  // Channel events
  | 'CHANNEL_CREATED'
  | 'CHANNEL_UPDATED'
  | 'CHANNEL_ARCHIVED'
  // Member events
  | 'MEMBER_JOINED'
  | 'MEMBER_LEFT'
  | 'MEMBER_REMOVED'
  | 'MEMBER_ROLE_CHANGED'
  // Read status
  | 'MESSAGE_READ'
  // Error
  | 'ERROR';

// ==================== UI State ====================

export interface DiscussUIState {
  selectedChannelId: string | null;
  isSidebarOpen: boolean;
  isEmojiPickerOpen: boolean;
  replyToMessage: Message | null;
  editingMessage: Message | null;
}
