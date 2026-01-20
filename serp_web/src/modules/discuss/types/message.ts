/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message types
*/

import type { BaseEntity } from './common';

// ==================== Enums ====================

export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
export type BackendMessageType = 'STANDARD' | 'SYSTEM';

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
}

// ==================== Request Types ====================

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

export interface MessageFilters {
  userId?: string;
  type?: MessageType;
  dateFrom?: string;
  dateTo?: string;
  hasAttachments?: boolean;
  search?: string;
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
