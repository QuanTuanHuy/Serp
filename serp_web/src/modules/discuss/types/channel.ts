/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel types
*/

import type { BaseEntity, UserInfo } from './common';

// ==================== Enums ====================

export type ChannelType = 'DIRECT' | 'GROUP' | 'TOPIC';
export type ChannelRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type MemberStatus = 'ACTIVE' | 'INACTIVE' | 'REMOVED';
export type NotificationLevel = 'ALL' | 'MENTIONS' | 'NONE';

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

// ==================== Filter Types ====================

export interface ChannelFilters {
  type?: ChannelType;
  search?: string;
  isArchived?: boolean;
  entityType?: string;
}
