/*
Author: QuanTuanHuy
Description: Part of Serp Project - Chat window component for discuss module
*/

'use client';

import React, { useState, useRef } from 'react';
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
import { MessageList, type MessageListRef } from './MessageList';
import { MessageInput } from './MessageInput';
import { OnlineStatusIndicator } from './OnlineStatusIndicator';
import { SearchDialog } from './SearchDialog';
import { ChannelMembersPanel } from './ChannelMembersPanel';
import { useDiscussWebSocket } from '../hooks';
import {
  useGetMessagesQuery,
  useSendMessageMutation,
  useSendMessageWithFilesMutation,
  useEditMessageMutation,
  useDeleteMessageMutation,
  useAddReactionMutation,
  useRemoveReactionMutation,
} from '../api/discussApi';
import type { Channel, Message, Attachment } from '../types';

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
  const [searchOpen, setSearchOpen] = useState(false);
  const [membersPanelOpen, setMembersPanelOpen] = useState(false);
  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set());
  const messageListRef = useRef<MessageListRef>(null);

  // WebSocket connection for real-time updates
  const { isConnected, sendTypingIndicator } = useDiscussWebSocket({
    channelId: channel.id,
    onMessage: (message) => {
      console.log('[ChatWindow] Received real-time message:', message);
      // Messages will be auto-updated via RTK Query cache invalidation
    },
    onTypingUpdate: (userId, isTyping) => {
      console.log('[ChatWindow] Typing update:', userId, isTyping);
      setTypingUsers((prev) => {
        const updated = new Set(prev);
        if (isTyping) {
          updated.add(userId);
        } else {
          updated.delete(userId);
        }
        return updated;
      });
    },
  });

  // Fetch messages
  const {
    data: messagesResponse,
    isLoading,
    isError,
  } = useGetMessagesQuery({
    channelId: channel.id,
    pagination: { page, limit: 50 },
  });

  // Send message mutations
  const [sendMessage] = useSendMessageMutation();
  const [sendMessageWithFiles] = useSendMessageWithFilesMutation();
  const [editMessage] = useEditMessageMutation();
  const [deleteMessage] = useDeleteMessageMutation();
  const [addReaction] = useAddReactionMutation();
  const [removeReaction] = useRemoveReactionMutation();

  const messages = messagesResponse?.data?.items || [];
  const hasMore = messagesResponse?.data?.hasNext ?? false;

  const handleSendMessage = async (
    content: string,
    filesOrAttachments?: any[] // Can be File[] or Attachment[]
  ) => {
    try {
      // Check if we're editing a message
      if (editingMessage) {
        await editMessage({
          channelId: channel.id,
          messageId: editingMessage.id,
          content,
        }).unwrap();

        setEditingMessage(null);
        return;
      }

      // Check if files are File objects (new) or Attachment objects (old)
      const files = filesOrAttachments?.filter(
        (item) => item instanceof File
      ) as File[];

      if (files && files.length > 0) {
        // Send message with files
        await sendMessageWithFiles({
          channelId: channel.id,
          content,
          files,
          parentId: replyingTo?.id,
        }).unwrap();
      } else {
        // Send text-only message with optimistic update params
        await sendMessage({
          channelId: channel.id,
          content,
          parentId: replyingTo?.id,
          currentUserId,
        }).unwrap();
      }

      // Clear reply state
      setReplyingTo(null);
    } catch (error) {
      console.error('Failed to send/edit message:', error);
      alert('Failed to send message. Please try again.');
    }
  };

  const handleLoadMore = () => {
    setPage((prev) => prev + 1);
  };

  const handleEditMessage = (message: Message) => {
    setEditingMessage(message);
    setReplyingTo(null);
  };

  const handleDeleteMessage = async (message: Message) => {
    if (!confirm('Are you sure you want to delete this message?')) {
      return;
    }

    try {
      await deleteMessage({
        channelId: channel.id,
        messageId: message.id,
      }).unwrap();
    } catch (error) {
      console.error('Failed to delete message:', error);
      alert('Failed to delete message. Please try again.');
    }
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
        emoji,
        currentUserId,
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
        currentUserId,
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
              onClick={() => setSearchOpen(true)}
              className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
            >
              <Search className='h-5 w-5' />
            </Button>

            <Button
              variant='ghost'
              size='sm'
              onClick={() => setMembersPanelOpen(true)}
              className='text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100'
              title='View members'
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
          ref={messageListRef}
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

      {/* Search Dialog */}
      <SearchDialog
        open={searchOpen}
        onOpenChange={setSearchOpen}
        channelId={channel.id}
        onResultClick={(clickedChannelId, messageId) => {
          if (clickedChannelId === channel.id) {
            // Same channel - scroll to message
            messageListRef.current?.scrollToMessage(messageId);
          } else {
            // Different channel - show notification
            console.log('Message is in different channel:', {
              clickedChannelId,
              messageId,
            });
            // TODO: Navigate to different channel or show toast
            alert(
              'This message is in a different channel. Please switch channels to view it.'
            );
          }
        }}
      />

      {/* Channel Members Panel */}
      <ChannelMembersPanel
        open={membersPanelOpen}
        onOpenChange={setMembersPanelOpen}
        channelId={channel.id}
        channelName={channel.name}
        currentUserId={currentUserId}
      />
    </div>
  );
};
