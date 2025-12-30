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

export const MOCK_MESSAGES: Record<string, Message[]> = {
  'ch-1': [
    {
      id: 'msg-1',
      channelId: 'ch-1',
      userId: '2',
      userName: 'Jane Smith',
      userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
      content: 'Hey John! Did you get a chance to review the proposal?',
      type: 'TEXT' as MessageType,
      threadCount: 0,
      mentions: ['1'],
      reactions: [{ emoji: '👍', userIds: ['1'], count: 1 }],
      attachments: [],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(Date.now() - 60 * 60000).toISOString(),
      updatedAt: new Date(Date.now() - 60 * 60000).toISOString(),
    },
    {
      id: 'msg-2',
      channelId: 'ch-1',
      userId: '1',
      userName: 'John Doe',
      userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
      content:
        'Yes! I just finished reviewing it. Overall looks great, but I have a few suggestions.',
      type: 'TEXT' as MessageType,
      threadCount: 0,
      mentions: [],
      reactions: [],
      attachments: [],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(Date.now() - 30 * 60000).toISOString(),
      updatedAt: new Date(Date.now() - 30 * 60000).toISOString(),
    },
    {
      id: 'msg-3',
      channelId: 'ch-1',
      userId: '2',
      userName: 'Jane Smith',
      userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
      content: "Sounds good! Let me know when you're ready.",
      type: 'TEXT' as MessageType,
      threadCount: 0,
      mentions: [],
      reactions: [],
      attachments: [],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(Date.now() - 5 * 60000).toISOString(),
      updatedAt: new Date(Date.now() - 5 * 60000).toISOString(),
    },
  ],
  'ch-2': [
    {
      id: 'msg-4',
      channelId: 'ch-2',
      userId: '3',
      userName: 'Bob Wilson',
      userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob',
      content: 'Great work everyone on the Q4 release! 🎉',
      type: 'TEXT' as MessageType,
      threadCount: 2,
      mentions: [],
      reactions: [
        { emoji: '🎉', userIds: ['1', '2', '4'], count: 3 },
        { emoji: '👏', userIds: ['1', '5'], count: 2 },
      ],
      attachments: [],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(Date.now() - 15 * 60000).toISOString(),
      updatedAt: new Date(Date.now() - 15 * 60000).toISOString(),
    },
    {
      id: 'msg-5',
      channelId: 'ch-2',
      userId: '4',
      userName: 'Alice Johnson',
      userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alice',
      content: 'Thanks @Bob Wilson! The team really pulled together.',
      type: 'TEXT' as MessageType,
      parentId: 'msg-4',
      threadCount: 0,
      mentions: ['3'],
      reactions: [],
      attachments: [],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(Date.now() - 10 * 60000).toISOString(),
      updatedAt: new Date(Date.now() - 10 * 60000).toISOString(),
    },
  ],
  'ch-3': [
    {
      id: 'msg-6',
      channelId: 'ch-3',
      userId: '4',
      userName: 'Alice Johnson',
      userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alice',
      content: "I'll schedule a follow-up call next week.",
      type: 'TEXT' as MessageType,
      threadCount: 0,
      mentions: [],
      reactions: [],
      attachments: [],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(Date.now() - 2 * 60 * 60000).toISOString(),
      updatedAt: new Date(Date.now() - 2 * 60 * 60000).toISOString(),
    },
  ],
};

// ==================== Mock Activities ====================

export const MOCK_ACTIVITIES: Activity[] = [
  {
    id: 'act-1',
    userId: '2',
    userName: 'Jane Smith',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
    action: 'MESSAGE_SENT' as ActivityAction,
    entityType: 'channel',
    entityId: 'ch-1',
    entityName: 'Jane Smith',
    metadata: { messagePreview: "Sounds good! Let me know when you're ready." },
    isRead: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 5 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 5 * 60000).toISOString(),
  },
  {
    id: 'act-2',
    userId: '3',
    userName: 'Bob Wilson',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob',
    action: 'MESSAGE_SENT' as ActivityAction,
    entityType: 'channel',
    entityId: 'ch-2',
    entityName: 'Product Team',
    metadata: { messagePreview: 'Great work everyone on the Q4 release! 🎉' },
    isRead: true,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 15 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 15 * 60000).toISOString(),
  },
  {
    id: 'act-3',
    userId: '5',
    userName: 'Charlie Brown',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Charlie',
    action: 'FILE_SHARED' as ActivityAction,
    entityType: 'channel',
    entityId: 'ch-4',
    entityName: 'Task: Q1 Report',
    metadata: { fileName: 'Q1_Report_Final.pdf', fileSize: '2.4 MB' },
    isRead: false,
    tenantId: 'tenant-1',
    createdAt: new Date(Date.now() - 24 * 60 * 60000).toISOString(),
    updatedAt: new Date(Date.now() - 24 * 60 * 60000).toISOString(),
  },
];

// ==================== Mock Presence ====================

export const MOCK_PRESENCE: UserPresence[] = [
  {
    userId: '2',
    userName: 'Jane Smith',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
    isOnline: true,
  },
  {
    userId: '3',
    userName: 'Bob Wilson',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob',
    isOnline: true,
  },
  {
    userId: '4',
    userName: 'Alice Johnson',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alice',
    isOnline: false,
    lastSeenAt: new Date(Date.now() - 2 * 60 * 60000).toISOString(),
  },
  {
    userId: '5',
    userName: 'Charlie Brown',
    userAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Charlie',
    isOnline: false,
    lastSeenAt: new Date(Date.now() - 24 * 60 * 60000).toISOString(),
  },
];

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
  userId: string = CURRENT_USER_ID
): Message {
  const user = getUserById(userId) || MOCK_USERS[0];
  return {
    id: `msg-${Date.now()}`,
    channelId,
    userId,
    userName: user.name,
    userAvatar: user.avatar,
    content,
    type: 'TEXT',
    threadCount: 0,
    mentions: [],
    reactions: [],
    attachments: [],
    isEdited: false,
    isDeleted: false,
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
