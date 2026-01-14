/*
Author: QuanTuanHuy
Description: Part of Serp Project - Data transformers for Discuss API
*/

import type {
  Message,
  Channel,
  Attachment,
  ChannelMember,
  SenderInfo,
  UserInfo,
} from '../types';
import {
  transformReactionsFromBackend,
  mapMessageTypeFromBackend,
} from '../types';

// ==================== Transform Functions ====================

/**
 * Transform backend message to frontend format
 * Maps senderId, sender object, and reactions
 */
export const transformMessage = (backendMsg: any): Message => {
  return {
    ...backendMsg,
    id: String(backendMsg.id),
    channelId: String(backendMsg.channelId),
    senderId: String(backendMsg.senderId),
    tenantId: String(backendMsg.tenantId),
    parentId: backendMsg.parentId ? String(backendMsg.parentId) : undefined,
    messageType: backendMsg.messageType,
    type: mapMessageTypeFromBackend(
      backendMsg.messageType,
      backendMsg.attachments?.length > 0
    ),
    reactions: transformReactionsFromBackend(backendMsg.reactions),
    attachments: backendMsg.attachments?.map(transformAttachment) || [],
    mentions: backendMsg.mentions?.map(String) || [],
    deletedBy: backendMsg.deletedBy ? String(backendMsg.deletedBy) : undefined,
    sender: backendMsg.sender
      ? transformSenderInfo(backendMsg.sender)
      : undefined,
  };
};

/**
 * Transform backend channel to frontend format
 * Maps members with user info
 */
export const transformChannel = (backendChannel: any): Channel => {
  return {
    ...backendChannel,
    id: String(backendChannel.id),
    entityId: backendChannel.entityId
      ? String(backendChannel.entityId)
      : undefined,
    tenantId: String(backendChannel.tenantId),
    members: backendChannel.members?.map(transformChannelMember),
  };
};

/**
 * Transform backend channel member to frontend format
 */
export const transformChannelMember = (backendMember: any): ChannelMember => {
  return {
    ...backendMember,
    id: String(backendMember.id),
    channelId: String(backendMember.channelId),
    userId: String(backendMember.userId),
    tenantId: String(backendMember.tenantId),
    removedBy: backendMember.removedBy
      ? String(backendMember.removedBy)
      : undefined,
    lastReadMsgId: backendMember.lastReadMsgId
      ? String(backendMember.lastReadMsgId)
      : undefined,
    user: backendMember.user
      ? transformUserInfo(backendMember.user)
      : undefined,
  };
};

/**
 * Transform backend attachment to frontend format
 */
export const transformAttachment = (backendAttachment: any): Attachment => {
  return {
    ...backendAttachment,
    id: String(backendAttachment.id),
    messageId: String(backendAttachment.messageId),
  };
};

/**
 * Transform backend sender info to frontend format
 */
export const transformSenderInfo = (backendSender: any): SenderInfo => {
  return {
    id: String(backendSender.id),
    name: backendSender.name,
    email: backendSender.email,
    avatarUrl: backendSender.avatarUrl,
  };
};

/**
 * Transform backend user info to frontend format
 */
export const transformUserInfo = (backendUser: any): UserInfo => {
  return {
    id: String(backendUser.id),
    name: backendUser.name,
    email: backendUser.email,
    avatarUrl: backendUser.avatarUrl,
  };
};
