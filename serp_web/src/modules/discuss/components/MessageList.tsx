/*
Author: QuanTuanHuy
Description: Part of Serp Project - Message list component with infinite scroll
*/

'use client';

import React, {
  useEffect,
  useLayoutEffect,
  useRef,
  useImperativeHandle,
  forwardRef,
  useState,
  useMemo,
  useCallback,
} from 'react';
import { cn } from '@/shared/utils';
import { ScrollArea } from '@/shared/components/ui';
import { Loader2, AlertCircle } from 'lucide-react';
import { MessageItem } from './MessageItem';
import type { Message } from '../types';

// =============================================================================
// Types
// =============================================================================

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
  lastReadMessageId?: string | null;
  onScrollPositionChange?: (isNearBottom: boolean) => void;
  className?: string;
}

export interface MessageListRef {
  scrollToMessage: (messageId: string) => void;
  scrollToBottom: () => void;
}

interface DateGroup {
  date: string;
  messages: Message[];
}

// =============================================================================
// Utility Functions (hoisted outside component for performance)
// =============================================================================

const formatDateHeader = (dateString: string): string => {
  const date = new Date(dateString);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const todayDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const yesterdayDate = new Date(yesterday.getFullYear(), yesterday.getMonth(), yesterday.getDate());

  if (messageDate.getTime() === todayDate.getTime()) {
    return 'Today';
  }
  if (messageDate.getTime() === yesterdayDate.getTime()) {
    return 'Yesterday';
  }
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: date.getFullYear() !== today.getFullYear() ? 'numeric' : undefined,
  });
};

const groupMessagesByDate = (messages: Message[]): DateGroup[] => {
  const groups: Record<string, Message[]> = {};

  for (const message of messages) {
    const dateKey = new Date(message.createdAt).toISOString().split('T')[0];
    if (!groups[dateKey]) {
      groups[dateKey] = [];
    }
    groups[dateKey].push(message);
  }

  return Object.entries(groups)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([date, msgs]) => ({
      date,
      messages: msgs.toSorted(
        (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      ),
    }));
};

const shouldGroupMessage = (current: Message, previous: Message | null): boolean => {
  if (!previous) return false;
  if (current.senderId !== previous.senderId) return false;
  const timeDiff = new Date(current.createdAt).getTime() - new Date(previous.createdAt).getTime();
  if (timeDiff > 2 * 60 * 1000) return false;
  if (previous.parentId) return false;
  return true;
};

// =============================================================================
// Constants
// =============================================================================

const SCROLL_THRESHOLD_TOP = 150; // pixels from top to trigger load more
const SCROLL_THRESHOLD_BOTTOM = 100; // pixels from bottom to consider "near bottom"
const LOAD_DEBOUNCE_MS = 300;

// =============================================================================
// Component
// =============================================================================

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
      lastReadMessageId,
      onScrollPositionChange,
      className,
    },
    ref
  ) => {
    // =========================================================================
    // Refs
    // =========================================================================
    const scrollAreaRef = useRef<HTMLDivElement>(null);
    const bottomRef = useRef<HTMLDivElement>(null);
    const messageRefs = useRef<Map<string, HTMLDivElement>>(new Map());
    const viewportRef = useRef<HTMLDivElement | null>(null);

    // State tracking refs (avoid re-renders)
    const lastLoadTimeRef = useRef(0);
    const isLoadingRef = useRef(false);
    const prevScrollHeightRef = useRef(0);
    const isFirstLoadRef = useRef(true);
    const prevMessagesLengthRef = useRef(0);

    // =========================================================================
    // State
    // =========================================================================
    const [isNearBottom, setIsNearBottom] = useState(true);

    // =========================================================================
    // Memoized Values
    // =========================================================================
    const dateGroups = useMemo(() => groupMessagesByDate(messages), [messages]);

    // =========================================================================
    // Get Viewport Element
    // =========================================================================
    const getViewport = useCallback((): HTMLDivElement | null => {
      if (viewportRef.current) return viewportRef.current;

      const scrollArea = scrollAreaRef.current;
      if (!scrollArea) return null;

      const viewport = scrollArea.querySelector(
        '[data-slot="scroll-area-viewport"]'
      ) as HTMLDivElement | null;

      if (viewport) {
        viewportRef.current = viewport;
      }
      return viewport;
    }, []);

    // =========================================================================
    // Load More Handler
    // =========================================================================
    const handleLoadMore = useCallback(() => {
      const now = Date.now();

      // Guards
      if (!hasMore || !onLoadMore) return;
      if (isLoading || isLoadingRef.current) return;
      if (now - lastLoadTimeRef.current < LOAD_DEBOUNCE_MS) return;

      console.log('[MessageList] Triggering load more');
      lastLoadTimeRef.current = now;
      isLoadingRef.current = true;

      // Store current scroll height before loading
      const viewport = getViewport();
      if (viewport) {
        prevScrollHeightRef.current = viewport.scrollHeight;
      }

      onLoadMore();
    }, [hasMore, isLoading, onLoadMore, getViewport]);

    // =========================================================================
    // Scroll Handler - Simple scroll position detection
    // =========================================================================
    const handleScroll = useCallback(() => {
      const viewport = getViewport();
      if (!viewport) return;

      const { scrollTop, scrollHeight, clientHeight } = viewport;

      // Check if near top - trigger load more
      if (scrollTop < SCROLL_THRESHOLD_TOP && !isFirstLoadRef.current) {
        handleLoadMore();
      }

      // Check if near bottom - for auto-scroll on new messages
      const distanceFromBottom = scrollHeight - scrollTop - clientHeight;
      const nearBottom = distanceFromBottom < SCROLL_THRESHOLD_BOTTOM;

      if (nearBottom !== isNearBottom) {
        setIsNearBottom(nearBottom);
      }
    }, [getViewport, handleLoadMore, isNearBottom]);

    // =========================================================================
    // Imperative Handle - Expose methods to parent
    // =========================================================================
    useImperativeHandle(
      ref,
      () => ({
        scrollToMessage: (messageId: string) => {
          const el = messageRefs.current.get(messageId);
          if (el) {
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            el.style.backgroundColor = 'rgba(139, 92, 246, 0.2)';
            setTimeout(() => {
              el.style.backgroundColor = '';
            }, 2000);
          }
        },
        scrollToBottom: () => {
          bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
        },
      }),
      []
    );

    // =========================================================================
    // Effect: Attach scroll listener
    // =========================================================================
    useEffect(() => {
      const viewport = getViewport();
      if (!viewport) return;

      viewport.addEventListener('scroll', handleScroll, { passive: true });
      return () => viewport.removeEventListener('scroll', handleScroll);
    }, [getViewport, handleScroll]);

    // =========================================================================
    // Effect: Notify parent of scroll position changes
    // =========================================================================
    useEffect(() => {
      onScrollPositionChange?.(isNearBottom);
    }, [isNearBottom, onScrollPositionChange]);

    // =========================================================================
    // Effect: Reset loading ref when loading state changes
    // =========================================================================
    useEffect(() => {
      if (!isLoading) {
        isLoadingRef.current = false;
      }
    }, [isLoading]);

    // =========================================================================
    // Effect: Reset state when channel changes (messages become empty)
    // =========================================================================
    useEffect(() => {
      if (messages.length === 0) {
        isFirstLoadRef.current = true;
        isLoadingRef.current = false;
        lastLoadTimeRef.current = 0;
        prevScrollHeightRef.current = 0;
        prevMessagesLengthRef.current = 0;
        viewportRef.current = null;
      }
    }, [messages.length]);

    // =========================================================================
    // Layout Effect: Auto-scroll to bottom on first load
    // =========================================================================
    useLayoutEffect(() => {
      const isFirstLoad = isFirstLoadRef.current && messages.length > 0;

      if (isFirstLoad && bottomRef.current && !isLoading) {
        bottomRef.current.scrollIntoView({ behavior: 'instant' });
        // Mark first load complete after a small delay
        setTimeout(() => {
          isFirstLoadRef.current = false;
          console.log('[MessageList] First load complete, infinite scroll enabled');
        }, 100);
      }
    }, [messages.length, isLoading]);

    // =========================================================================
    // Layout Effect: Auto-scroll on new messages (when near bottom)
    // =========================================================================
    useLayoutEffect(() => {
      const isNewMessage =
        messages.length > prevMessagesLengthRef.current && !isFirstLoadRef.current;

      if (isNewMessage && isNearBottom && bottomRef.current && !isLoading) {
        bottomRef.current.scrollIntoView({ behavior: 'smooth' });
      }

      prevMessagesLengthRef.current = messages.length;
    }, [messages.length, isLoading, isNearBottom]);

    // =========================================================================
    // Layout Effect: Preserve scroll position when prepending messages
    // =========================================================================
    useLayoutEffect(() => {
      const viewport = getViewport();
      if (!viewport) return;

      const prevHeight = prevScrollHeightRef.current;
      const newHeight = viewport.scrollHeight;
      const heightDiff = newHeight - prevHeight;

      // If height increased and we were loading (prepending older messages)
      if (heightDiff > 0 && prevHeight > 0 && !isFirstLoadRef.current) {
        viewport.scrollTop += heightDiff;
        console.log('[MessageList] Preserved scroll position:', { heightDiff });
      }

      prevScrollHeightRef.current = newHeight;
    }, [messages, getViewport]);

    // =========================================================================
    // Render: Loading state (initial)
    // =========================================================================
    if (isLoading && messages.length === 0) {
      return (
        <div className={cn('flex flex-col items-center justify-center h-full gap-3', className)}>
          <Loader2 className="h-8 w-8 text-violet-500 animate-spin" />
          <p className="text-sm text-slate-500 dark:text-slate-400">Loading messages...</p>
        </div>
      );
    }

    // =========================================================================
    // Render: Error state
    // =========================================================================
    if (isError) {
      return (
        <div
          className={cn(
            'flex flex-col items-center justify-center h-full gap-3 p-6',
            className
          )}
        >
          <div className="h-12 w-12 rounded-full bg-rose-100 dark:bg-rose-900/20 flex items-center justify-center">
            <AlertCircle className="h-6 w-6 text-rose-500" />
          </div>
          <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">
            Failed to load messages
          </p>
          <p className="text-xs text-slate-500 dark:text-slate-400 text-center">
            Please try again later
          </p>
        </div>
      );
    }

    // =========================================================================
    // Render: Empty state
    // =========================================================================
    if (messages.length === 0) {
      return (
        <div
          className={cn(
            'flex flex-col items-center justify-center h-full gap-3 p-6',
            className
          )}
        >
          <div className="h-16 w-16 rounded-full bg-gradient-to-br from-violet-500/10 to-fuchsia-500/10 flex items-center justify-center">
            <svg
              className="w-8 h-8 text-violet-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
              />
            </svg>
          </div>
          <h3 className="text-lg font-bold text-slate-900 dark:text-slate-100">No messages yet</h3>
          <p className="text-sm text-slate-500 dark:text-slate-400 text-center max-w-xs">
            Be the first to send a message in this channel
          </p>
        </div>
      );
    }

    // =========================================================================
    // Render: Message list
    // =========================================================================
    return (
      <ScrollArea ref={scrollAreaRef} className={cn('h-full', className)}>
        <div className="py-4">
          {/* Loading indicator at top */}
          {isLoading && hasMore && (
            <div className="flex justify-center py-4">
              <Loader2 className="h-5 w-5 text-violet-500 animate-spin" />
            </div>
          )}

          {/* No more messages indicator */}
          {!hasMore && messages.length > 0 && (
            <div className="flex justify-center py-4">
              <span className="text-xs text-slate-400 dark:text-slate-500">
                Beginning of conversation
              </span>
            </div>
          )}

          {/* Messages grouped by date */}
          {dateGroups.map((group) => (
            <div key={group.date} className="mb-6">
              {/* Date divider */}
              <div className="flex items-center justify-center my-6">
                <div className="flex-1 h-px bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent" />
                <div className="px-4">
                  <span className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 bg-white dark:bg-slate-900 px-3 py-1 rounded-full border border-slate-200 dark:border-slate-700">
                    {formatDateHeader(group.date)}
                  </span>
                </div>
                <div className="flex-1 h-px bg-gradient-to-r from-transparent via-slate-200 dark:via-slate-700 to-transparent" />
              </div>

              {/* Messages in this date group */}
              {group.messages.map((message, index) => {
                const previousMessage = index > 0 ? group.messages[index - 1] : null;
                const isOwn = message.isSentByMe === true || message.senderId === currentUserId;
                const isGrouped = shouldGroupMessage(message, previousMessage);
                const showNewMessageSeparator =
                  lastReadMessageId && message.id === lastReadMessageId;

                return (
                  <React.Fragment key={message.id}>
                    <div
                      ref={(el) => {
                        if (el) {
                          messageRefs.current.set(message.id, el);
                        } else {
                          messageRefs.current.delete(message.id);
                        }
                      }}
                      className="transition-colors duration-500"
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

                    {/* New messages separator */}
                    {showNewMessageSeparator && (
                      <div className="flex items-center gap-3 my-4 px-4">
                        <div className="flex-1 h-px bg-red-500/50" />
                        <span className="text-xs font-semibold text-red-500 uppercase tracking-wide">
                          New Messages
                        </span>
                        <div className="flex-1 h-px bg-red-500/50" />
                      </div>
                    )}
                  </React.Fragment>
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
