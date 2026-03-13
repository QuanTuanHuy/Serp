/*
Author: QuanTuanHuy
Description: Part of Serp Project - Search results component with flat message display
*/

'use client';

import React, { useCallback, useRef, useEffect, useMemo } from 'react';
import {
  FileText,
  Image,
  Paperclip,
  ChevronRight,
  Loader2,
  SearchX,
  MessageSquare,
} from 'lucide-react';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import type { Message, MessageType } from '../types';
import { cn, getAvatarColor } from '@/shared/utils';

// =============================================================================
// Types
// =============================================================================

interface SearchResultsProps {
  results: Message[];
  isLoading?: boolean;
  isFetchingMore?: boolean;
  hasMore?: boolean;
  onLoadMore?: () => void;
  onResultClick?: (channelId: string, messageId: string) => void;
  searchQuery?: string;
  className?: string;
}

// =============================================================================
// Constants (hoisted outside component - js-hoist-regexp, rendering-hoist-jsx)
// =============================================================================

const MESSAGE_TYPE_ICONS: Record<MessageType, typeof FileText> = {
  TEXT: FileText,
  IMAGE: Image,
  FILE: Paperclip,
  SYSTEM: FileText,
};

const escapeRegExp = (str: string): string =>
  str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const formatRelativeDate = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
};

// Empty state JSX (rendering-hoist-jsx)
const InitialState = (
  <div className='flex flex-col items-center justify-center h-full py-16 px-4'>
    <div className='relative mb-6'>
      <div className='relative flex items-center justify-center w-20 h-20 rounded-full bg-muted'>
        <MessageSquare className='w-10 h-10 text-muted-foreground' />
      </div>
    </div>
    <h3 className='text-base font-semibold text-foreground mb-1'>
      Search messages
    </h3>
    <p className='text-sm text-muted-foreground text-center max-w-xs'>
      Type a keyword to search through messages in this channel.
    </p>
  </div>
);

// =============================================================================
// Sub-components (extracted for re-render optimization - rerender-memo)
// =============================================================================

const HighlightedText = React.memo(function HighlightedText({
  text,
  query,
}: {
  text: string;
  query: string;
}) {
  if (!query.trim()) return <>{text}</>;

  const escaped = escapeRegExp(query);
  const parts = text.split(new RegExp(`(${escaped})`, 'gi'));

  return (
    <>
      {parts.map((part, index) =>
        part.toLowerCase() === query.toLowerCase() ? (
          <mark
            key={index}
            className='bg-yellow-200 dark:bg-yellow-500/30 text-yellow-900 dark:text-yellow-200 font-semibold rounded px-0.5'
          >
            {part}
          </mark>
        ) : (
          <React.Fragment key={index}>{part}</React.Fragment>
        )
      )}
    </>
  );
});

const SearchResultItem = React.memo(function SearchResultItem({
  message,
  searchQuery,
  onClick,
}: {
  message: Message;
  searchQuery: string;
  onClick: (channelId: string, messageId: string) => void;
}) {
  const TypeIcon = MESSAGE_TYPE_ICONS[message.type] ?? FileText;
  const senderName = message.sender?.name || 'Unknown User';
  const senderInitial = senderName.charAt(0) || '?';

  const handleClick = useCallback(() => {
    onClick(message.channelId, message.id);
  }, [onClick, message.channelId, message.id]);

  return (
    <button
      onClick={handleClick}
      className='w-full group relative flex items-start gap-3 p-3 rounded-lg border border-transparent hover:border-primary/30 hover:bg-accent/50 transition-colors duration-150 text-left'
    >
      {/* Avatar */}
      <Avatar className='h-8 w-8 flex-shrink-0 mt-0.5'>
        {message.sender?.avatarUrl && (
          <AvatarImage src={message.sender.avatarUrl} alt={senderName} />
        )}
        <AvatarFallback
          className={cn(
            'text-xs text-white bg-gradient-to-br',
            getAvatarColor(senderName)
          )}
        >
          {senderInitial}
        </AvatarFallback>
      </Avatar>

      {/* Content */}
      <div className='flex-1 min-w-0'>
        <div className='flex items-center gap-2 mb-0.5'>
          <span className='font-semibold text-sm text-foreground truncate'>
            {senderName}
          </span>
          <span className='text-xs text-muted-foreground flex-shrink-0'>
            {formatRelativeDate(message.createdAt)}
          </span>
          <TypeIcon className='h-3.5 w-3.5 text-muted-foreground flex-shrink-0' />
          {message.attachments.length > 0 && (
            <Badge
              variant='outline'
              className='text-[10px] h-4 px-1.5 flex-shrink-0'
            >
              <Paperclip className='h-2.5 w-2.5 mr-0.5' />
              {message.attachments.length}
            </Badge>
          )}
        </div>

        {/* Highlighted message content */}
        <p className='text-sm text-muted-foreground line-clamp-2'>
          <HighlightedText text={message.content} query={searchQuery} />
        </p>

        {/* Reactions preview */}
        {message.reactions.length > 0 && (
          <div className='flex items-center gap-1 mt-1.5'>
            {message.reactions.slice(0, 3).map((reaction, idx) => (
              <div
                key={idx}
                className='flex items-center gap-0.5 px-1.5 py-0.5 rounded-full bg-muted border text-xs'
              >
                <span>{reaction.emoji}</span>
                <span className='text-[10px] text-muted-foreground'>
                  {reaction.count}
                </span>
              </div>
            ))}
            {message.reactions.length > 3 && (
              <span className='text-xs text-muted-foreground'>
                +{message.reactions.length - 3}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Navigate icon */}
      <ChevronRight className='h-4 w-4 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0 mt-1' />
    </button>
  );
});

// =============================================================================
// Loading skeleton (rendering-hoist-jsx)
// =============================================================================

function SearchResultSkeleton() {
  return (
    <div className='flex items-start gap-3 p-3 animate-pulse'>
      <div className='h-8 w-8 rounded-full bg-muted flex-shrink-0' />
      <div className='flex-1 space-y-2'>
        <div className='flex items-center gap-2'>
          <div className='h-3.5 w-24 bg-muted rounded' />
          <div className='h-3 w-12 bg-muted rounded' />
        </div>
        <div className='h-3.5 w-full bg-muted rounded' />
        <div className='h-3.5 w-3/4 bg-muted rounded' />
      </div>
    </div>
  );
}

// =============================================================================
// Main Component
// =============================================================================

export function SearchResults({
  results,
  isLoading = false,
  isFetchingMore = false,
  hasMore = false,
  onLoadMore,
  onResultClick,
  searchQuery = '',
  className,
}: SearchResultsProps) {
  const loadMoreRef = useRef<HTMLDivElement>(null);

  // Stable callback (rerender-functional-setstate pattern)
  const handleResultClick = useCallback(
    (channelId: string, messageId: string) => {
      onResultClick?.(channelId, messageId);
    },
    [onResultClick]
  );

  // Infinite scroll via IntersectionObserver
  useEffect(() => {
    const el = loadMoreRef.current;
    if (!el || !hasMore || isFetchingMore) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          onLoadMore?.();
        }
      },
      { threshold: 0.1 }
    );

    observer.observe(el);
    return () => observer.disconnect();
  }, [hasMore, isFetchingMore, onLoadMore]);

  // Initial loading skeleton
  if (isLoading) {
    return (
      <div className={cn('p-4 space-y-1', className)}>
        {Array.from({ length: 5 }, (_, i) => (
          <SearchResultSkeleton key={i} />
        ))}
      </div>
    );
  }

  // No query entered yet
  if (!searchQuery.trim()) {
    return InitialState;
  }

  // No results found
  if (results.length === 0) {
    return (
      <div className='flex flex-col items-center justify-center h-full py-16 px-4'>
        <div className='relative mb-6'>
          <div className='relative flex items-center justify-center w-20 h-20 rounded-full bg-muted'>
            <SearchX className='w-10 h-10 text-muted-foreground' />
          </div>
        </div>
        <h3 className='text-base font-semibold text-foreground mb-1'>
          No results found
        </h3>
        <p className='text-sm text-muted-foreground text-center max-w-xs'>
          No messages found matching &quot;{searchQuery}&quot;. Try different
          keywords or adjust your filters.
        </p>
      </div>
    );
  }

  // Results list
  return (
    <ScrollArea className={cn('h-full', className)}>
      <div className='p-4 space-y-0.5'>
        {results.map((message) => (
          <SearchResultItem
            key={message.id}
            message={message}
            searchQuery={searchQuery}
            onClick={handleResultClick}
          />
        ))}

        {/* Infinite scroll sentinel */}
        {hasMore && (
          <div
            ref={loadMoreRef}
            className='flex items-center justify-center py-4'
          >
            <Loader2 className='h-5 w-5 animate-spin text-primary' />
          </div>
        )}

        {/* Fetching more indicator */}
        {isFetchingMore && !hasMore && (
          <div className='flex items-center justify-center py-4'>
            <Loader2 className='h-5 w-5 animate-spin text-primary' />
          </div>
        )}
      </div>
    </ScrollArea>
  );
}
