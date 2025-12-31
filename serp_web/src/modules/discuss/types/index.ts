/*
Author: QuanTuanHuy
Description: Part of Serp Project - TypeScript types for Discuss module
*/

// ==================== Enums ====================

export type ChannelType = 'DIRECT' | 'GROUP' | 'TOPIC';
export type ChannelRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
export type ActivityAction =
  | 'MESSAGE_SENT'
  | 'USER_JOINED'
  | 'USER_LEFT'
  | 'CHANNEL_CREATED'
  | 'FILE_SHARED'
  | 'MENTION_RECEIVED';

// ==================== Base Entities ====================

export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
}

// ==================== Channel ====================

export interface Channel extends BaseEntity {
  name: string;
  description?: string;
  type: ChannelType;
  entityType?: string; // 'customer', 'task', 'order' for TOPIC channels
  entityId?: string;
  avatarUrl?: string;
  lastMessageAt?: string;
  lastMessage?: string;
  unreadCount: number;
  memberCount: number;
  isArchived: boolean;
  tenantId: string;
  members?: ChannelMember[];
}

export interface ChannelMember extends BaseEntity {
  channelId: string;
  userId: string;
  userName: string;
  userAvatar?: string;
  role: ChannelRole;
  unreadCount: number;
  lastReadAt?: string;
  joinedAt: string;
}

// ==================== Message ====================

export interface Message extends BaseEntity {
  channelId: string;
  userId: string;
  userName: string;
  userAvatar?: string;
  content: string;
  type: MessageType;
  parentId?: string; // For threaded replies
  threadCount: number;
  mentions: string[]; // User IDs mentioned
  reactions: MessageReaction[];
  attachments: Attachment[];
  isEdited: boolean;
  editedAt?: string;
  isDeleted: boolean;
  deletedAt?: string;
}

export interface MessageReaction {
  emoji: string;
  userIds: string[];
  count: number;
}

export interface Attachment extends BaseEntity {
  messageId: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  s3Key: string;
  s3Bucket: string;
  downloadUrl: string;
  thumbnailUrl?: string;
  virusScanStatus: 'PENDING' | 'CLEAN' | 'INFECTED';
}

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

// ==================== Request Types ====================

export interface CreateChannelRequest {
  name: string;
  description?: string;
  type: ChannelType;
  entityType?: string;
  entityId?: string;
  memberIds: string[];
}

export interface UpdateChannelRequest {
  name?: string;
  description?: string;
  avatarUrl?: string;
}

export interface SendMessageRequest {
  content: string;
  type?: MessageType;
  parentId?: string;
  attachmentIds?: string[];
}

export interface EditMessageRequest {
  content: string;
}

export interface AddReactionRequest {
  emoji: string;
}

export interface UploadAttachmentRequest {
  file: File;
  channelId: string;
}

// ==================== Filter Types ====================

export interface ChannelFilters {
  type?: ChannelType;
  search?: string;
  isArchived?: boolean;
  entityType?: string;
}

export interface MessageFilters {
  userId?: string;
  type?: MessageType;
  dateFrom?: string;
  dateTo?: string;
  hasAttachments?: boolean;
  search?: string;
}

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

// ==================== Pagination ====================

export interface PaginationParams {
  page: number;
  limit: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

// ==================== API Response ====================

export interface APIResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

// ==================== WebSocket Events ====================

export interface WSMessage {
  type: WSEventType;
  payload: any;
}

export type WSEventType =
  | 'CONNECTED'
  | 'NEW_MESSAGE'
  | 'MESSAGE_EDITED'
  | 'MESSAGE_DELETED'
  | 'REACTION_ADDED'
  | 'REACTION_REMOVED'
  | 'TYPING_INDICATOR'
  | 'USER_ONLINE'
  | 'USER_OFFLINE'
  | 'CHANNEL_UPDATED'
  | 'MEMBER_ADDED'
  | 'MEMBER_REMOVED'
  | 'UNREAD_COUNT_UPDATED'
  | 'ERROR';

// ==================== UI State ====================

export interface DiscussUIState {
  selectedChannelId: string | null;
  isSidebarOpen: boolean;
  isEmojiPickerOpen: boolean;
  replyToMessage: Message | null;
  editingMessage: Message | null;
}
