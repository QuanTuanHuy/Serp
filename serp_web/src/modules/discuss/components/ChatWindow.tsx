/*
Author: QuanTuanHuy
Description: Part of Serp Project - Chat window component for discuss module
*/

'use client';

import React, {
  useState,
  useRef,
  useCallback,
  useMemo,
  useEffect,
} from 'react';
import { toast } from 'sonner';
import { cn, getAvatarColor } from '@/shared/utils';
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
import { ScrollToBottomButton } from './ScrollToBottomButton';
import { useDiscussWebSocket } from '../hooks/useDiscussWebSocket';
import { useWebSocketOptional } from '../context/WebSocketContext';
import {
  useGetMessagesQuery,
  useLazyGetMessagesBeforeQuery,
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
  const [searchOpen, setSearchOpen] = useState(false);
  const [membersPanelOpen, setMembersPanelOpen] = useState(false);
  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set());
  const messageListRef = useRef<MessageListRef>(null);

  // Cursor-based pagination state
  const [allMessages, setAllMessages] = useState<Message[]>([]);
  const [hasMoreMessages, setHasMoreMessages] = useState(true);
  const [isNearBottom, setIsNearBottom] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0);
  const [lastReadMessageId, setLastReadMessageId] = useState<string | null>(
    null
  );

  // Refs for stable references in callbacks
  const isNearBottomRef = useRef(true);
  const isInitialLoadRef = useRef(true);

  // Initial load - always page 1 for latest messages
  const {
    data: messagesResponse,
    isLoading: isInitialLoading,
    isError,
  } = useGetMessagesQuery({
    channelId: channel.id,
    pagination: { page: 1, limit: 50 },
  });

  // Lazy query for loading more (cursor-based)
  const [fetchMoreMessages, { isLoading: isLoadingMore }] =
    useLazyGetMessagesBeforeQuery();

  const isLoading = isInitialLoading || isLoadingMore;

  // Send message mutations
  const [sendMessage] = useSendMessageMutation();
  const [sendMessageWithFiles] = useSendMessageWithFilesMutation();
  const [editMessage] = useEditMessageMutation();
  const [deleteMessage] = useDeleteMessageMutation();
  const [addReaction] = useAddReactionMutation();
  const [removeReaction] = useRemoveReactionMutation();

  // Get WebSocket API (optional)
  const wsApi = useWebSocketOptional();

  // Populate allMessages from initial load
  useEffect(() => {
    if (messagesResponse?.data?.items) {
      console.log('[ChatWindow] Initial messages loaded:', {
        count: messagesResponse.data.items.length,
        hasNext: messagesResponse.data.hasNext,
        totalPages: messagesResponse.data.totalPages,
        page: messagesResponse.data.page,
      });
      setAllMessages(messagesResponse.data.items);
      setHasMoreMessages(messagesResponse.data.hasNext);
      isInitialLoadRef.current = false;
    }
  }, [messagesResponse]);

  // Reset state on channel change
  useEffect(() => {
    setAllMessages([]);
    setHasMoreMessages(true);
    setUnreadCount(0);
    setLastReadMessageId(null);
    setIsNearBottom(true);
    isInitialLoadRef.current = true;
  }, [channel.id]);

  // Sync isNearBottom to ref
  useEffect(() => {
    isNearBottomRef.current = isNearBottom;
    // Clear unread when user scrolls to bottom
    if (isNearBottom) {
      setUnreadCount(0);
      setLastReadMessageId(null);
    }
  }, [isNearBottom]);

  // WebSocket connection for real-time updates
  const { isConnected, sendTypingIndicator } = useDiscussWebSocket({
    channelId: channel.id,
    onMessage: (message) => {
      console.log('[ChatWindow] Received real-time message:', message);

      // Deduplicate and append new message
      setAllMessages((prev) => {
        // Check if message already exists (prevents duplicates)
        if (prev.some((m) => m.id === message.id)) {
          console.log('[ChatWindow] Duplicate message ignored:', message.id);
          return prev;
        }

        // Track unread if user scrolled away from bottom
        if (!isNearBottomRef.current) {
          setUnreadCount((count) => count + 1);
          // Mark position of last read message for separator
          if (prev.length > 0) {
            setLastReadMessageId(
              (current) => current || prev[prev.length - 1].id
            );
          }
        }

        // Append new message to end (newest at bottom)
        return [...prev, message];
      });
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

  const messages = allMessages;
  const hasMore = hasMoreMessages;

  const handleSendMessage = useCallback(
    async (
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
          // Send message with files - always use REST for file uploads
          await sendMessageWithFiles({
            channelId: channel.id,
            content,
            files,
            parentId: replyingTo?.id,
          }).unwrap();
        } else {
          // Text-only message - try WebSocket first, fallback to REST
          if (wsApi?.isConnected) {
            console.log('[ChatWindow] Using WebSocket to send message');
            wsApi.sendMessage(content, replyingTo?.id);
          } else {
            console.log('[ChatWindow] WebSocket not connected, using REST');
            await sendMessage({
              channelId: channel.id,
              content,
              parentId: replyingTo?.id,
              currentUserId,
            }).unwrap();
          }
        }

        // Clear reply state
        setReplyingTo(null);
      } catch (error) {
        console.error('Failed to send/edit message:', error);
        alert('Failed to send message. Please try again.');
      }
    },
    [
      channel.id,
      editingMessage,
      replyingTo,
      wsApi?.isConnected,
      currentUserId,
      editMessage,
      sendMessageWithFiles,
      sendMessage,
      wsApi,
    ]
  );

  const handleLoadMore = useCallback(async () => {
    if (!hasMoreMessages || isLoadingMore) {
      console.log('[ChatWindow] Skip load more:', {
        hasMoreMessages,
        isLoadingMore,
      });
      return;
    }

    // Find the oldest message by createdAt timestamp
    // API returns messages DESC (newest first), so we need the one with smallest timestamp
    const oldestMessage = allMessages.reduce((oldest, current) => {
      if (!oldest) return current;
      return new Date(current.createdAt).getTime() <
        new Date(oldest.createdAt).getTime()
        ? current
        : oldest;
    }, allMessages[0]);

    if (!oldestMessage) {
      console.warn('[ChatWindow] No messages to load from');
      return;
    }

    console.log('[ChatWindow] Loading more messages before:', oldestMessage.id);

    // Retry logic with exponential backoff
    const maxRetries = 3;
    let attempt = 0;

    while (attempt < maxRetries) {
      try {
        const result = await fetchMoreMessages({
          channelId: channel.id,
          beforeId: oldestMessage.id,
          limit: 50,
        }).unwrap();

        console.log(
          '[ChatWindow] Loaded',
          result.data.length,
          'older messages'
        );

        if (result.data && result.data.length > 0) {
          // Append older messages to array (MessageList will sort by createdAt)
          setAllMessages((prev) => [...result.data, ...prev]);

          // If we got fewer than requested, no more messages exist
          setHasMoreMessages(result.data.length === 50);
        } else {
          setHasMoreMessages(false);
        }
        return; // Success - exit retry loop
      } catch (error) {
        attempt++;
        console.error(
          `[ChatWindow] Load more attempt ${attempt}/${maxRetries} failed:`,
          error
        );

        if (attempt >= maxRetries) {
          // Final failure - show error toast
          toast.error('Failed to load messages', {
            description: 'Please check your connection and try again',
            action: {
              label: 'Retry',
              onClick: () => handleLoadMore(),
            },
          });
          return;
        }

        // Wait before retry (exponential backoff: 1s, 2s, 4s)
        const delayMs = 1000 * Math.pow(2, attempt - 1);
        console.log(`[ChatWindow] Retrying in ${delayMs}ms...`);
        await new Promise((resolve) => setTimeout(resolve, delayMs));
      }
    }
  }, [
    allMessages,
    hasMoreMessages,
    isLoadingMore,
    channel.id,
    fetchMoreMessages,
  ]);

  const scrollToBottom = useCallback(() => {
    if (messageListRef.current) {
      messageListRef.current.scrollToBottom();
    }
    // Clear unread tracking
    setUnreadCount(0);
    setLastReadMessageId(null);
  }, []);

  const handleEditMessage = useCallback((message: Message) => {
    setEditingMessage(message);
    setReplyingTo(null);
  }, []);

  const handleDeleteMessage = useCallback(
    async (message: Message) => {
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
    },
    [channel.id, deleteMessage]
  );

  const handleReplyMessage = useCallback((message: Message) => {
    setReplyingTo(message);
    setEditingMessage(null);
  }, []);

  const handleReaction = useCallback(
    async (messageId: string, emoji: string) => {
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
    },
    [channel.id, currentUserId, addReaction]
  );

  const handleRemoveReaction = useCallback(
    async (messageId: string, emoji: string) => {
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
    },
    [channel.id, currentUserId, removeReaction]
  );

  const handleCancelReply = useCallback(() => {
    setReplyingTo(null);
  }, []);

  const handleCancelEdit = useCallback(() => {
    setEditingMessage(null);
  }, []);

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
                {channel.avatarUrl && (
                  <AvatarImage src={channel.avatarUrl} alt={channel.name} />
                )}
                <AvatarFallback
                  className={cn(
                    'text-sm font-semibold text-white bg-gradient-to-br',
                    getAvatarColor(channel.name)
                  )}
                >
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
      <div className='flex-1 overflow-hidden relative'>
        <MessageList
          ref={messageListRef}
          messages={messages}
          currentUserId={currentUserId}
          isLoading={isLoading}
          isError={isError}
          hasMore={hasMore}
          onLoadMore={handleLoadMore}
          lastReadMessageId={lastReadMessageId}
          onScrollPositionChange={setIsNearBottom}
          onEditMessage={handleEditMessage}
          onDeleteMessage={handleDeleteMessage}
          onReplyMessage={handleReplyMessage}
          onReaction={handleReaction}
          onRemoveReaction={handleRemoveReaction}
        />

        <ScrollToBottomButton
          visible={!isNearBottom}
          unreadCount={unreadCount}
          onClick={scrollToBottom}
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
