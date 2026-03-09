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
import { useWebSocket } from '../context/WebSocketContext';
import {
  useGetMessagesQuery,
  useLazyGetMessagesBeforeQuery,
  useSendMessageMutation,
  useSendMessageWithFilesMutation,
  useEditMessageMutation,
  useDeleteMessageMutation,
  useAddReactionMutation,
  useRemoveReactionMutation,
  useGetChannelPresenceQuery,
} from '../api/discussApi';
import type { Channel, Message, Attachment } from '../types';
import type { UserStatus } from '../api/presence.api';

interface ChatWindowProps {
  channel: Channel;
  currentUserId: string;
  currentUserName?: string;
  currentUserAvatarUrl?: string;
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
  currentUserName,
  currentUserAvatarUrl,
  className,
}) => {
  const [replyingTo, setReplyingTo] = useState<Message | null>(null);
  const [editingMessage, setEditingMessage] = useState<Message | null>(null);
  const [searchOpen, setSearchOpen] = useState(false);
  const [membersPanelOpen, setMembersPanelOpen] = useState(false);
  const [typingUsers, setTypingUsers] = useState<Map<string, string>>(
    new Map()
  );
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
  const prevChannelIdRef = useRef(channel.id);
  const typingTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(new Map());

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

  // Presence query for channel
  const { data: presenceData } = useGetChannelPresenceQuery(channel.id);

  // Helper: map backend UserStatus to FE OnlineStatus
  const mapUserStatus = (status: UserStatus): 'online' | 'busy' | 'offline' => {
    switch (status) {
      case 'ONLINE':
        return 'online';
      case 'BUSY':
        return 'busy';
      case 'OFFLINE':
        return 'offline';
      default:
        return 'offline';
    }
  };

  // Send message mutations
  const [sendMessage] = useSendMessageMutation();
  const [sendMessageWithFiles] = useSendMessageWithFilesMutation();
  const [editMessage] = useEditMessageMutation();
  const [deleteMessage] = useDeleteMessageMutation();
  const [addReaction] = useAddReactionMutation();
  const [removeReaction] = useRemoveReactionMutation();

  // Get WebSocket API from context (single connection managed by page.tsx)
  const wsApi = useWebSocket();

  // Unified effect: handles both channel changes and RTK cache updates
  // Combines the old channel-change reset and messagesResponse sync to avoid
  // the flash of "No messages yet" when switching channels.
  useEffect(() => {
    const isChannelChange = prevChannelIdRef.current !== channel.id;
    prevChannelIdRef.current = channel.id;

    if (isChannelChange) {
      // Reset pagination/UI state on channel change
      setHasMoreMessages(true);
      setUnreadCount(0);
      setLastReadMessageId(null);
      setIsNearBottom(true);
      isInitialLoadRef.current = true;
    }

    if (messagesResponse?.data?.items) {
      if (isChannelChange || isInitialLoadRef.current) {
        // Channel change or initial load: replace all messages with cached/fetched data
        setAllMessages(messagesResponse.data.items);
      } else {
        // RTK cache update (refetch from invalidation): merge strategy
        // Keeps WS-added messages and load-more messages that aren't in the refetched page
        setAllMessages((prev) => {
          const responseItems = messagesResponse.data.items;
          const responseIds = new Set(responseItems.map((m: Message) => m.id));

          const latestResponseTime =
            responseItems.length > 0
              ? Math.max(
                  ...responseItems.map((m: Message) =>
                    new Date(m.createdAt).getTime()
                  )
                )
              : 0;

          // Keep messages not in the response
          const keptMessages = prev.filter((m) => !responseIds.has(m.id));

          // Older messages (loaded via load-more) stay before response items
          const olderMessages = keptMessages.filter(
            (m) =>
              !m.id.startsWith('temp-') &&
              new Date(m.createdAt).getTime() <= latestResponseTime
          );

          // Newer messages (WS-added or temp/optimistic) stay after response items
          const newerMessages = keptMessages.filter(
            (m) =>
              m.id.startsWith('temp-') ||
              new Date(m.createdAt).getTime() > latestResponseTime
          );

          return [...olderMessages, ...responseItems, ...newerMessages];
        });
      }
      setHasMoreMessages(messagesResponse.data.hasNext);
      isInitialLoadRef.current = false;
    } else if (isChannelChange) {
      // Channel changed but no cached data yet (loading state)
      setAllMessages([]);
    }
  }, [channel.id, messagesResponse]);

  // Sync isNearBottom to ref
  useEffect(() => {
    isNearBottomRef.current = isNearBottom;
    // Clear unread when user scrolls to bottom
    if (isNearBottom) {
      setUnreadCount(0);
      setLastReadMessageId(null);
    }
  }, [isNearBottom]);

  // Register active channel with WebSocket context
  useEffect(() => {
    wsApi.setActiveChannel(channel.id);
    return () => wsApi.setActiveChannel(undefined);
  }, [channel.id, wsApi]);

  // Register onMessage callback for real-time message handling
  useEffect(() => {
    wsApi.setOnMessage((message) => {
      console.log('[ChatWindow] Received real-time message:', message);

      setAllMessages((prev) => {
        // Check if this message matches an optimistic temp message
        const tempIndex = prev.findIndex(
          (m) =>
            m.id.startsWith('temp-') &&
            m.senderId === message.senderId &&
            m.content === message.content
        );

        if (tempIndex !== -1) {
          // Replace temp with real message from server
          const updated = [...prev];
          updated[tempIndex] = message;
          return updated;
        }

        // Normal duplicate check
        if (prev.some((m) => m.id === message.id)) {
          console.log('[ChatWindow] Duplicate message ignored:', message.id);
          return prev;
        }

        if (!isNearBottomRef.current) {
          setUnreadCount((count) => count + 1);
          if (prev.length > 0) {
            setLastReadMessageId(
              (current) => current || prev[prev.length - 1].id
            );
          }
        }

        return [...prev, message];
      });
    });
    return () => wsApi.setOnMessage(undefined);
  }, [wsApi]);

  // Register onTypingUpdate callback for typing indicators
  useEffect(() => {
    wsApi.setOnTypingUpdate((userId, userName, isTyping) => {
      // Don't show typing indicator for the current user
      if (userId === currentUserId) return;

      console.log('[ChatWindow] Typing update:', userId, userName, isTyping);

      // Clear any existing timeout for this user
      const existingTimeout = typingTimeoutsRef.current.get(userId);
      if (existingTimeout) {
        clearTimeout(existingTimeout);
        typingTimeoutsRef.current.delete(userId);
      }

      if (isTyping) {
        setTypingUsers((prev) => {
          const updated = new Map(prev);
          updated.set(userId, userName);
          return updated;
        });

        // Auto-clear after 5 seconds if no new TYPING_START
        const timeout = setTimeout(() => {
          setTypingUsers((prev) => {
            const updated = new Map(prev);
            updated.delete(userId);
            return updated;
          });
          typingTimeoutsRef.current.delete(userId);
        }, 5000);
        typingTimeoutsRef.current.set(userId, timeout);
      } else {
        setTypingUsers((prev) => {
          const updated = new Map(prev);
          updated.delete(userId);
          return updated;
        });
      }
    });

    return () => {
      wsApi.setOnTypingUpdate(undefined);
      // Clear all timeouts on cleanup
      typingTimeoutsRef.current.forEach((timeout) => clearTimeout(timeout));
      typingTimeoutsRef.current.clear();
    };
  }, [wsApi, currentUserId]);

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

            // Optimistic update: add temp message so sender sees it immediately
            const now = new Date().toISOString();
            const tempMessage: Message = {
              id: `temp-${Date.now()}`,
              channelId: channel.id,
              senderId: currentUserId,
              tenantId: '',
              content,
              messageType: 'STANDARD',
              type: 'TEXT',
              parentId: replyingTo?.id,
              threadCount: 0,
              mentions: [],
              reactions: [],
              attachments: [],
              isEdited: false,
              isDeleted: false,
              readCount: 0,
              isSentByMe: true,
              sender: {
                id: currentUserId,
                name: currentUserName || '',
                email: '',
                avatarUrl: currentUserAvatarUrl,
              },
              createdAt: now,
              updatedAt: now,
            };
            setAllMessages((prev) => [...prev, tempMessage]);
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
      currentUserName,
      currentUserAvatarUrl,
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
                  (() => {
                    // Find the other user's presence from channel presence data
                    const presence = presenceData?.data;
                    const allUsers = presence?.statusGroups
                      ? Object.values(presence.statusGroups).flat()
                      : [];
                    const otherUser = allUsers.find(
                      (u) => String(u.userId) !== currentUserId
                    );
                    const status = otherUser
                      ? mapUserStatus(otherUser.status)
                      : 'offline';
                    const statusText = otherUser?.isOnline
                      ? status === 'busy'
                        ? 'Busy'
                        : 'Online'
                      : otherUser?.lastSeenText || 'Offline';

                    return (
                      <span className='flex items-center gap-1.5'>
                        <OnlineStatusIndicator status={status} size='sm' />
                        <span
                          className={cn(
                            'font-medium',
                            status === 'online'
                              ? 'text-emerald-600 dark:text-emerald-400'
                              : status === 'busy'
                                ? 'text-amber-600 dark:text-amber-400'
                                : 'text-slate-500 dark:text-slate-400'
                          )}
                        >
                          {statusText}
                        </span>
                      </span>
                    );
                  })()
                ) : (
                  <>
                    <Users className='h-3.5 w-3.5' />
                    <span>
                      {presenceData?.data?.onlineCount != null
                        ? `${presenceData.data.onlineCount} online · `
                        : ''}
                      {channel.memberCount} members
                    </span>
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

        {/* Typing indicator - removed from header, now above input */}
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

      {/* Typing indicator - positioned above input */}
      {typingUsers.size > 0 && (
        <div className='flex-shrink-0 px-6 py-1.5 bg-white dark:bg-slate-900 border-t border-slate-100 dark:border-slate-800'>
          <div className='flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400'>
            <span className='flex gap-0.5'>
              <span
                className='w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce'
                style={{ animationDelay: '0ms' }}
              />
              <span
                className='w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce'
                style={{ animationDelay: '150ms' }}
              />
              <span
                className='w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce'
                style={{ animationDelay: '300ms' }}
              />
            </span>
            <span className='italic'>
              {(() => {
                const names = Array.from(typingUsers.values());
                if (names.length === 1) return `${names[0]} is typing...`;
                if (names.length === 2)
                  return `${names[0]} and ${names[1]} are typing...`;
                return 'Several people are typing...';
              })()}
            </span>
          </div>
        </div>
      )}

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
          onTypingStart={() => wsApi.sendTypingIndicator(true)}
          onTypingStop={() => wsApi.sendTypingIndicator(false)}
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
