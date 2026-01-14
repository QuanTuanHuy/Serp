/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message list component with infinite scroll
*/

'use client';

import React, {
  useEffect,
  useRef,
  useImperativeHandle,
  forwardRef,
} from 'react';
import { cn } from '@/shared/utils';
import { ScrollArea } from '@/shared/components/ui';
import { Loader2, AlertCircle } from 'lucide-react';
import { MessageItem } from './MessageItem';
import type { Message } from '../types';

interface MessageListProps {
  messages: Message[];
  currentUserId: string;
  isLoading?: boolean;
  isError?: boolean;
  hasMore?: boolean;
  onLoadMore?: () => void;
  onEditMessage?: (message: Message) => void;
  onDeleteMessage?: (message: Message) => void;
  onReplyMessage?: (message: Message) => void;
  onReaction?: (messageId: string, emoji: string) => void;
  onRemoveReaction?: (messageId: string, emoji: string) => void;
  className?: string;
}

export interface MessageListRef {
  scrollToMessage: (messageId: string) => void;
}

interface DateGroup {
  date: string;
  messages: Message[];
}

const formatDateHeader = (dateString: string) => {
  const date = new Date(dateString);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  // Reset time parts for comparison
  const messageDate = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );
  const todayDate = new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate()
  );
  const yesterdayDate = new Date(
    yesterday.getFullYear(),
    yesterday.getMonth(),
    yesterday.getDate()
  );

  if (messageDate.getTime() === todayDate.getTime()) {
    return 'Today';
  } else if (messageDate.getTime() === yesterdayDate.getTime()) {
    return 'Yesterday';
  } else {
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: date.getFullYear() !== today.getFullYear() ? 'numeric' : undefined,
    });
  }
};

const groupMessagesByDate = (messages: Message[]): DateGroup[] => {
  const groups: Record<string, Message[]> = {};

  messages.forEach((message) => {
    const date = new Date(message.createdAt);
    const dateKey = date.toISOString().split('T')[0]; // YYYY-MM-DD

    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(message);
  });

  return Object.entries(groups)
    .sort(([dateA], [dateB]) => dateA.localeCompare(dateB)) // Sort dates ascending (oldest first)
    .map(([date, messages]) => ({
      date,
      messages: messages.sort((a, b) => {
        // Sort messages within each date group ascending (oldest first, newest at bottom)
        return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      }),
    }));
};

const shouldGroupMessage = (
  current: Message,
  previous: Message | null,
  currentUserId: string
): boolean => {
  if (!previous) return false;

  // Don't group if different senders
  if (current.senderId !== previous.senderId) return false;

  // Don't group own messages
  if (current.senderId === currentUserId) return false;

  // Don't group if time gap > 2 minutes
  const timeDiff =
    new Date(current.createdAt).getTime() -
    new Date(previous.createdAt).getTime();
  if (timeDiff > 2 * 60 * 1000) return false;

  // Don't group if previous message has reply
  if (previous.parentId) return false;

  return true;
};

export const MessageList = forwardRef<MessageListRef, MessageListProps>(
  (
    {
      messages,
      currentUserId,
      isLoading = false,
      isError = false,
      hasMore = false,
      onLoadMore,
      onEditMessage,
      onDeleteMessage,
      onReplyMessage,
      onReaction,
      onRemoveReaction,
      className,
    },
    ref
  ) => {
    const scrollAreaRef = useRef<HTMLDivElement>(null);
    const bottomRef = useRef<HTMLDivElement>(null);
    const observerRef = useRef<IntersectionObserver | null>(null);
    const topSentinelRef = useRef<HTMLDivElement>(null);
    const messageRefs = useRef<Map<string, HTMLDivElement>>(new Map());

    // Expose scrollToMessage method to parent
    useImperativeHandle(
      ref,
      () => ({
        scrollToMessage: (messageId: string) => {
          const messageElement = messageRefs.current.get(messageId);
          if (messageElement) {
            messageElement.scrollIntoView({
              behavior: 'smooth',
              block: 'center',
            });
            // Highlight message briefly
            messageElement.style.backgroundColor = 'rgba(139, 92, 246, 0.2)';
            setTimeout(() => {
              messageElement.style.backgroundColor = '';
            }, 2000);
          }
        },
      }),
      []
    );

    // Auto-scroll to bottom on new messages
    useEffect(() => {
      if (bottomRef.current && messages.length > 0) {
        bottomRef.current.scrollIntoView({ behavior: 'smooth' });
      }
    }, [messages.length]);

    // Infinite scroll - load more on scroll to top
    useEffect(() => {
      if (!hasMore || !onLoadMore) return;

      const sentinel = topSentinelRef.current;
      if (!sentinel) return;

      observerRef.current = new IntersectionObserver(
        (entries) => {
          if (entries[0].isIntersecting && !isLoading) {
            onLoadMore();
          }
        },
        { threshold: 0.1 }
      );

      observerRef.current.observe(sentinel);

      return () => {
        if (observerRef.current) {
          observerRef.current.disconnect();
        }
      };
    }, [hasMore, onLoadMore, isLoading]);

    const dateGroups = groupMessagesByDate(messages);

    // Loading state
    if (isLoading && messages.length === 0) {
      return (
        <div
          className={cn(
            'flex flex-col items-center justify-center h-full gap-3',
            className
          )}
        >
          <Loader2 className='h-8 w-8 text-violet-500 animate-spin' />
          <p className='text-sm text-slate-500 dark:text-slate-400'>
            Loading messages...
          </p>
        </div>
      );
    }

    // Error state
    if (isError) {
      return (
        <div
          className={cn(
            'flex flex-col items-center justify-center h-full gap-3 p-6',
            className
          )}
        >
          <div className='h-12 w-12 rounded-full bg-rose-100 dark:bg-rose-900/20 flex items-center justify-center'>
            <AlertCircle className='h-6 w-6 text-rose-500' />
          </div>
          <p className='text-sm font-semibold text-slate-900 dark:text-slate-100'>
            Failed to load messages
          </p>
          <p className='text-xs text-slate-500 dark:text-slate-400 text-center'>
            Please try again later
          </p>
        </div>
      );
    }

    // Empty state
    if (messages.length === 0) {
      return (
        <div
          className={cn(
            'flex flex-col items-center justify-center h-full gap-3 p-6',
            className
          )}
        >
          <div className='h-16 w-16 rounded-full bg-gradient-to-br from-violet-500/10 to-fuchsia-500/10 flex items-center justify-center'>
            <svg
              className='w-8 h-8 text-violet-500'
              fill='none'
              stroke='currentColor'
              viewBox='0 0 24 24'
            >
              <path
                strokeLinecap='round'
                strokeLinejoin='round'
                strokeWidth={2}
                d='M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z'
              />
            </svg>
          </div>
          <h3 className='text-lg font-bold text-slate-900 dark:text-slate-100'>
            No messages yet
          </h3>
          <p className='text-sm text-slate-500 dark:text-slate-400 text-center max-w-xs'>
            Be the first to send a message in this channel
          </p>
        </div>
      );
    }

    return (
      <ScrollArea ref={scrollAreaRef} className={cn('h-full', className)}>
        <div className='py-4'>
          {/* Top sentinel for infinite scroll */}
          {hasMore && <div ref={topSentinelRef} className='h-1' />}

          {/* Loading indicator at top */}
          {isLoading && hasMore && (
            <div className='flex justify-center py-4'>
              <Loader2 className='h-5 w-5 text-violet-500 animate-spin' />
            </div>
          )}

          {/* Messages grouped by date */}
          {dateGroups.map((group) => (
            <div key={group.date} className='mb-6'>
              {/* Date divider */}
              <div className='flex items-center justify-center my-6'>
                <div className='flex-1 h-px bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent' />
                <div className='px-4'>
                  <span className='text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 bg-white dark:bg-slate-900 px-3 py-1 rounded-full border border-slate-200 dark:border-slate-700'>
                    {formatDateHeader(group.date)}
                  </span>
                </div>
                <div className='flex-1 h-px bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent' />
              </div>

              {/* Messages in this date group */}
              {group.messages.map((message, index) => {
                const previousMessage =
                  index > 0 ? group.messages[index - 1] : null;
                const isOwn = message.isSentByMe === true || message.senderId === currentUserId;
                const isGrouped = shouldGroupMessage(
                  message,
                  previousMessage,
                  currentUserId
                );

                return (
                  <div
                    key={message.id}
                    ref={(el) => {
                      if (el) {
                        messageRefs.current.set(message.id, el);
                      } else {
                        messageRefs.current.delete(message.id);
                      }
                    }}
                    className='transition-colors duration-500'
                  >
                    <MessageItem
                      message={message}
                      isOwn={isOwn}
                      isGrouped={isGrouped}
                      showAvatar={!isGrouped || isOwn}
                      onEdit={onEditMessage}
                      onDelete={onDeleteMessage}
                      onReply={onReplyMessage}
                      onReaction={onReaction}
                      onRemoveReaction={onRemoveReaction}
                    />
                  </div>
                );
              })}
            </div>
          ))}

          {/* Bottom anchor for auto-scroll */}
          <div ref={bottomRef} />
        </div>
      </ScrollArea>
    );
  }
);

MessageList.displayName = 'MessageList';
