/*
Author: QuanTuanHuy
Description: Part of Serp Project - Mock data for Discuss module development
*/

import type {
  Channel,
  Message,
  Activity,
  ChannelMember,
  UserPresence,
  Attachment,
  ChannelType,
  MessageType,
  ActivityAction,
} from '../types';

// ==================== Mock Users ====================

export const MOCK_USERS = [
  {
    id: '1',
    name: 'John Doe',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
  },
  {
    id: '2',
    name: 'Jane Smith',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
  },
  {
    id: '3',
    name: 'Bob Wilson',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob',
  },
  {
    id: '4',
    name: 'Alice Johnson',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alice',
  },
  {
    id: '5',
    name: 'Charlie Brown',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Charlie',
  },
];

export const CURRENT_USER_ID = '1'; // John Doe

// ==================== Mock Channels ====================

export const MOCK_CHANNELS: Channel[] = [
  {
    id: 'ch-1',
    name: 'Jane Smith',
    description: 'Direct message',
    type: 'DIRECT' as ChannelType,
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
    lastMessageAt: new Date(Date.now() - 5 * 60000).toISOString(),
    lastMessage: "Sounds good! Let me know when you're ready.",
    unreadCount: 2,
    memberCount: 2,
    isArchived: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 5 * 60000).toISOString(),
  },
  {
    id: 'ch-2',
    name: 'Product Team',
    description: 'Team discussions and updates',
    type: 'GROUP' as ChannelType,
    avatarUrl: 'https://api.dicebear.com/7.x/shapes/svg?seed=ProductTeam',
    lastMessageAt: new Date(Date.now() - 15 * 60000).toISOString(),
    lastMessage: 'Bob Wilson: Great work everyone on the Q4 release!',
    unreadCount: 5,
    memberCount: 8,
    isArchived: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 90 * 24 * 60 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 15 * 60000).toISOString(),
  },
  {
    id: 'ch-3',
    name: 'Customer: Acme Corp',
    description: 'Discussion about Acme Corp account',
    type: 'TOPIC' as ChannelType,
    entityType: 'customer',
    entityId: 'cust-123',
    avatarUrl: 'https://api.dicebear.com/7.x/initials/svg?seed=AC',
    lastMessageAt: new Date(Date.now() - 2 * 60 * 60000).toISOString(),
    lastMessage: "Alice Johnson: I'll schedule a follow-up call next week.",
    unreadCount: 0,
    memberCount: 4,
    isArchived: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 45 * 24 * 60 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 60 * 60000).toISOString(),
  },
  {
    id: 'ch-4',
    name: 'Task: Q1 Report',
    description: 'Discussion about Q1 Report task',
    type: 'TOPIC' as ChannelType,
    entityType: 'task',
    entityId: 'task-456',
    avatarUrl: 'https://api.dicebear.com/7.x/initials/svg?seed=Q1',
    lastMessageAt: new Date(Date.now() - 24 * 60 * 60000).toISOString(),
    lastMessage: "Charlie Brown: I've uploaded the final draft.",
    unreadCount: 1,
    memberCount: 3,
    isArchived: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 14 * 24 * 60 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 24 * 60 * 60000).toISOString(),
  },
  {
    id: 'ch-5',
    name: 'Engineering',
    description: 'Engineering team chat',
    type: 'GROUP' as ChannelType,
    avatarUrl: 'https://api.dicebear.com/7.x/shapes/svg?seed=Engineering',
    lastMessageAt: new Date(Date.now() - 3 * 24 * 60 * 60000).toISOString(),
    lastMessage: 'Code review needed for PR #234',
    unreadCount: 0,
    memberCount: 12,
    isArchived: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 120 * 24 * 60 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 3 * 24 * 60 * 60000).toISOString(),
  },
];

// ==================== Mock Messages ====================

// TODO: Update mock data to use senderId and sender object instead of userId/userName/userAvatar
export const MOCK_MESSAGES: Record<string, Message[]> = {
  // Temporarily disabled - needs update to match new Message type
  // 'ch-1': [...],
};

// ==================== Mock Activities ====================
// TODO: Update mock activities
export const MOCK_ACTIVITIES: Activity[] = [];

// ==================== Mock Presence ====================
// TODO: Update mock presence
export const MOCK_PRESENCE: UserPresence[] = [];

// ==================== Helper Functions ====================

export function getChannelById(id: string): Channel | undefined {
  return MOCK_CHANNELS.find((ch) => ch.id === id);
}

export function getMessagesByChannelId(channelId: string): Message[] {
  return MOCK_MESSAGES[channelId] || [];
}

export function getUserById(id: string) {
  return MOCK_USERS.find((u) => u.id === id);
}

export function createMockMessage(
  channelId: string,
  content: string,
  attachments: Attachment[] = [],
  senderId: string = CURRENT_USER_ID
): Message {
  const user = getUserById(senderId) || MOCK_USERS[0];
  const messageId = `msg-${Date.now()}`;
  const messageAttachments = attachments.map((att) => ({
    ...att,
    messageId,
  }));

  return {
    id: messageId,
    channelId,
    senderId,
    tenantId: 'tenant-1',
    content,
    messageType: 'STANDARD',
    type: attachments.length > 0 ? 'FILE' : 'TEXT',
    threadCount: 0,
    mentions: [],
    reactions: [],
    attachments: messageAttachments,
    isEdited: false,
    isDeleted: false,
    readCount: 0,
    sender: {
      id: user.id,
      name: user.name,
      email: `${user.name.toLowerCase().replace(' ', '.')}@example.com`,
      avatarUrl: user.avatar,
    },
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

export function createMockChannel(
  name: string,
  type: ChannelType,
  memberIds: string[]
): Channel {
  return {
    id: `ch-${Date.now()}`,
    name,
    type,
    avatarUrl: `https://api.dicebear.com/7.x/shapes/svg?seed=${name}`,
    unreadCount: 0,
    memberCount: memberIds.length,
    isArchived: false,
    tenantId: 'tenant-1',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}
