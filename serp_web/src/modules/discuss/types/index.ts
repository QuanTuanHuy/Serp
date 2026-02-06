/*
Author: QuanTuanHuy
Description: Part of Serp Project - TypeScript types for Discuss module
*/

// ==================== Enums ====================

export type ChannelType = 'DIRECT' | 'GROUP' | 'TOPIC';
export type ChannelRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type MemberStatus = 'ACTIVE' | 'INACTIVE' | 'REMOVED';
export type NotificationLevel = 'ALL' | 'MENTIONS' | 'NONE';
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
export type BackendMessageType = 'STANDARD' | 'SYSTEM';
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

export interface UserInfo {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
}

export interface ChannelMember extends BaseEntity {
  channelId: string;
  userId: string;
  tenantId: string;
  role: ChannelRole;
  status: MemberStatus;
  joinedAt: string;
  leftAt?: string;
  removedBy?: string;
  lastReadMsgId?: string;
  unreadCount: number;
  isMuted: boolean;
  isPinned: boolean;
  notificationLevel: NotificationLevel;
  metadata?: Record<string, any>;

  // Computed fields
  isOnline?: boolean;
  user?: UserInfo;
}

// ==================== Message ====================

export interface SenderInfo {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
}

export interface Message extends BaseEntity {
  channelId: string;
  senderId: string;
  tenantId: string;
  content: string;
  messageType: 'STANDARD' | 'SYSTEM';
  type: MessageType; // Frontend type (TEXT/IMAGE/FILE/SYSTEM)
  parentId?: string; // For threaded replies
  threadCount: number;
  mentions: string[]; // User IDs mentioned
  reactions: MessageReaction[];
  attachments: Attachment[];
  isEdited: boolean;
  editedAt?: string;
  isDeleted: boolean;
  deletedAt?: string;
  deletedBy?: string;
  readCount: number;
  metadata?: Record<string, any>;

  // Computed fields
  isSentByMe?: boolean;
  isReadByMe?: boolean;
  sender?: SenderInfo;
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
  // virusScanStatus removed - backend V8 migration deleted this field
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

// Backend PaginatedResponse structure
export interface PaginatedResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// ==================== API Response ====================

export interface APIResponse<T> {
  status: string; // "success" | "error" from backend
  code: number; // HTTP status code (200, 201, 400, etc.)
  message: string;
  data: T;
}

// Helper to check if response is successful
export const isSuccessResponse = (response: APIResponse<any>): boolean => {
  return response.code >= 200 && response.code < 300;
};

// ==================== WebSocket Events ====================

/** Matches backend WsEvent<T> envelope */
export interface WsEvent<T = any> {
  type: WSEventType;
  payload: T;
  channelId: number | null;
  timestamp: number;
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

// ==================== Type Transformers (FE ↔ BE) ====================

/**
 * Transform frontend MessageReaction[] to backend Map<String, List<Long>>
 * Backend format: { "👍": [1, 2, 3], "❤️": [2, 4] }
 */
export const transformReactionsToBackend = (
  reactions: MessageReaction[]
): Record<string, number[]> => {
  return reactions.reduce(
    (acc, r) => {
      acc[r.emoji] = r.userIds.map((id) => parseInt(id));
      return acc;
    },
    {} as Record<string, number[]>
  );
};

/**
 * Transform backend Map<String, List<Long>> to frontend MessageReaction[]
 */
export const transformReactionsFromBackend = (
  reactions: Record<string, number[]> | null | undefined
): MessageReaction[] => {
  if (!reactions) return [];

  return Object.entries(reactions).map(([emoji, userIds]) => ({
    emoji,
    userIds: Array.isArray(userIds) ? userIds.map(String) : [],
    count: Array.isArray(userIds) ? userIds.length : 0,
  }));
};

/**
 * Map frontend MessageType to backend type
 * FE: TEXT/IMAGE/FILE → BE: STANDARD
 * FE: SYSTEM → BE: SYSTEM
 */
export const mapMessageTypeToBackend = (
  type: MessageType
): 'STANDARD' | 'SYSTEM' => {
  return type === 'SYSTEM' ? 'SYSTEM' : 'STANDARD';
};

/**
 * Map backend MessageType to frontend type
 * BE: STANDARD → FE: TEXT (default)
 * BE: SYSTEM → FE: SYSTEM
 */
export const mapMessageTypeFromBackend = (
  type: 'STANDARD' | 'SYSTEM',
  hasAttachments?: boolean
): MessageType => {
  if (type === 'SYSTEM') return 'SYSTEM';

  // For STANDARD messages, infer type from attachments
  if (hasAttachments) {
    // Could be IMAGE or FILE - components will handle rendering
    return 'FILE';
  }

  return 'TEXT';
};
