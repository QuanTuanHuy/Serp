/*
Author: QuanTuanHuy
Description: Part of Serp Project - Chat window component for discuss module
*/

'use client';

import React, { useState } from 'react';
import { cn } from '@/shared/utils';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
  Button,
} from '@/shared/components/ui';
import {
  Hash,
  Users,
  MessageSquare,
  Phone,
  Video,
  Info,
  Settings,
  Pin,
  Search,
  MoreVertical,
} from 'lucide-react';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { OnlineStatusIndicator } from './OnlineStatusIndicator';
import {
  useGetMessagesQuery,
  useSendMessageMutation,
  useAddReactionMutation,
  useRemoveReactionMutation,
} from '../api/discussApi';
import type { Channel, Message } from '../types';

interface ChatWindowProps {
  channel: Channel;
  currentUserId: string;
  className?: string;
}

const getChannelIcon = (type: Channel['type']) => {
  switch (type) {
    case 'DIRECT':
      return <MessageSquare className='h-5 w-5 text-blue-500' />;
    case 'GROUP':
      return <Users className='h-5 w-5 text-violet-500' />;
    case 'TOPIC':
      return <Hash className='h-5 w-5 text-emerald-500' />;
  }
};

const getUserInitials = (name: string) => {
  return name
    .split(' ')
    .map((word) => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const ChatWindow: React.FC<ChatWindowProps> = ({
  channel,
  currentUserId,
  className,
}) => {
  const [replyingTo, setReplyingTo] = useState<Message | null>(null);
  const [editingMessage, setEditingMessage] = useState<Message | null>(null);
  const [page, setPage] = useState(1);

  // Fetch messages
  const {
    data: messagesResponse,
    isLoading,
    isError,
  } = useGetMessagesQuery({
    channelId: channel.id,
    pagination: { page, limit: 50 },
  });

  // Send message mutation
  const [sendMessage] = useSendMessageMutation();
  const [addReaction] = useAddReactionMutation();
  const [removeReaction] = useRemoveReactionMutation();

  const messages = messagesResponse?.data?.data || [];
  const pagination = messagesResponse?.data?.pagination;
  const hasMore = pagination ? page < pagination.totalPages : false;

  const handleSendMessage = async (content: string) => {
    try {
      await sendMessage({
        channelId: channel.id,
        data: {
          content,
          type: 'TEXT',
          parentId: replyingTo?.id,
        },
      }).unwrap();

      // Clear reply state
      setReplyingTo(null);
      setEditingMessage(null);
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  };

  const handleLoadMore = () => {
    setPage((prev) => prev + 1);
  };

  const handleEditMessage = (message: Message) => {
    setEditingMessage(message);
    setReplyingTo(null);
  };

  const handleDeleteMessage = (message: Message) => {
    // TODO: Implement delete message
    console.log('Delete message:', message);
  };

  const handleReplyMessage = (message: Message) => {
    setReplyingTo(message);
    setEditingMessage(null);
  };

  const handleReaction = async (messageId: string, emoji: string) => {
    try {
      await addReaction({
        messageId,
        channelId: channel.id,
        data: { emoji },
      }).unwrap();
    } catch (error) {
      console.error('Failed to add reaction:', error);
    }
  };

  const handleRemoveReaction = async (messageId: string, emoji: string) => {
    try {
      await removeReaction({
        messageId,
        channelId: channel.id,
        emoji,
      }).unwrap();
    } catch (error) {
      console.error('Failed to remove reaction:', error);
    }
  };

  const handleCancelReply = () => {
    setReplyingTo(null);
  };

  const handleCancelEdit = () => {
    setEditingMessage(null);
  };

  return (
    <div
      className={cn(
        'flex flex-col h-full bg-slate-50 dark:bg-slate-900',
        className
      )}
    >
      {/* Header */}
      <div className='flex-shrink-0 px-6 py-4 bg-white dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700'>
        <div className='flex items-center justify-between'>
          {/* Channel info */}
          <div className='flex items-center gap-3'>
            {/* Avatar/Icon */}
            {channel.type === 'DIRECT' || channel.avatarUrl ? (
              <Avatar className='h-11 w-11 ring-2 ring-white dark:ring-slate-900 shadow-sm'>
                <AvatarImage src={channel.avatarUrl} alt={channel.name} />
                <AvatarFallback className='bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white text-sm font-semibold'>
                  {getUserInitials(channel.name)}
                </AvatarFallback>
              </Avatar>
            ) : (
              <div className='h-11 w-11 rounded-full bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-800 dark:to-slate-700 flex items-center justify-center ring-2 ring-white dark:ring-slate-900 shadow-sm'>
                {getChannelIcon(channel.type)}
              </div>
            )}

            {/* Name & description */}
            <div className='flex flex-col'>
              <div className='flex items-center gap-2'>
                <h2 className='text-lg font-bold text-slate-900 dark:text-slate-100'>
                  {channel.name}
                </h2>
              </div>
              <div className='flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400'>
                {channel.type === 'DIRECT' ? (
                  <span className='flex items-center gap-1.5'>
                    <OnlineStatusIndicator status='online' size='sm' />
                    <span className='text-emerald-600 dark:text-emerald-400 font-medium'>
                      Online
                    </span>
                  </span>
                ) : (
                  <>
                    <Users className='h-3.5 w-3.5' />
                    <span>{channel.memberCount} members</span>
                  </>
                )}
                {channel.description && (
                  <>
                    <span>•</span>
                    <span className='truncate max-w-md'>
                      {channel.description}
                    </span>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* Action buttons */}
          <div className='flex items-center gap-2'>
            {channel.type === 'DIRECT' && (
              <>
                <Button
                  variant='ghost'
                  size='sm'
                  className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
                >
                  <Phone className='h-5 w-5' />
                </Button>
                <Button
                  variant='ghost'
                  size='sm'
                  className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
                >
                  <Video className='h-5 w-5' />
                </Button>
              </>
            )}

            <Button
              variant='ghost'
              size='sm'
              className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
            >
              <Search className='h-5 w-5' />
            </Button>

            <Button
              variant='ghost'
              size='sm'
              className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
            >
              <Info className='h-5 w-5' />
            </Button>

            <Button
              variant='ghost'
              size='sm'
              className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
            >
              <MoreVertical className='h-5 w-5' />
            </Button>
          </div>
        </div>

        {/* Typing indicator */}
        {/* TODO: Implement real-time typing indicator */}
        {false && (
          <div className='mt-2 text-xs text-slate-500 dark:text-slate-400 italic'>
            <span className='font-semibold'>John Doe</span> is typing...
          </div>
        )}
      </div>

      {/* Messages */}
      <div className='flex-1 overflow-hidden'>
        <MessageList
          messages={messages}
          currentUserId={currentUserId}
          isLoading={isLoading}
          isError={isError}
          hasMore={hasMore}
          onLoadMore={handleLoadMore}
          onEditMessage={handleEditMessage}
          onDeleteMessage={handleDeleteMessage}
          onReplyMessage={handleReplyMessage}
          onReaction={handleReaction}
          onRemoveReaction={handleRemoveReaction}
        />
      </div>

      {/* Input */}
      <div className='flex-shrink-0'>
        <MessageInput
          channelId={channel.id}
          onSendMessage={handleSendMessage}
          replyingTo={replyingTo}
          onCancelReply={handleCancelReply}
          editingMessage={editingMessage}
          onCancelEdit={handleCancelEdit}
          placeholder={`Message ${channel.type === 'DIRECT' ? channel.name : `#${channel.name}`}`}
        />
      </div>
    </div>
  );
};
