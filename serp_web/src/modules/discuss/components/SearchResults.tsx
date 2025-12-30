/*
Author: QuanTuanHuy
Description: Part of Serp Project - Search results component with grouped display
*/

'use client';

import { useCallback, useRef, useEffect } from 'react';
import {
  Hash,
  Users,
  FolderKanban,
  FileText,
  Image,
  Paperclip,
  ChevronRight,
  Loader2,
  SearchX,
} from 'lucide-react';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import type { GroupedSearchResults, ChannelType, MessageType } from '../types';

interface SearchResultsProps {
  results: GroupedSearchResults[];
  isLoading?: boolean;
  hasMore?: boolean;
  onLoadMore?: () => void;
  onResultClick?: (channelId: string, messageId: string) => void;
  searchQuery?: string;
  className?: string;
}

const CHANNEL_ICONS: Record<ChannelType, typeof Hash> = {
  DIRECT: Users,
  GROUP: Users,
  TOPIC: FolderKanban,
};

const MESSAGE_TYPE_ICONS: Record<MessageType, typeof FileText> = {
  TEXT: FileText,
  IMAGE: Image,
  FILE: Paperclip,
  SYSTEM: FileText,
};

export function SearchResults({
  results,
  isLoading = false,
  hasMore = false,
  onLoadMore,
  onResultClick,
  searchQuery = '',
  className = '',
}: SearchResultsProps) {
  const loadMoreRef = useRef<HTMLDivElement>(null);

  // Infinite scroll observer
  useEffect(() => {
    if (!loadMoreRef.current || !hasMore || isLoading) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          onLoadMore?.();
        }
      },
      { threshold: 0.1 }
    );

    observer.observe(loadMoreRef.current);

    return () => observer.disconnect();
  }, [hasMore, isLoading, onLoadMore]);

  const handleResultClick = useCallback(
    (channelId: string, messageId: string) => {
      onResultClick?.(channelId, messageId);
    },
    [onResultClick]
  );

  // Highlight search terms in text
  const highlightText = (text: string, query: string) => {
    if (!query.trim()) return text;

    const parts = text.split(new RegExp(`(${query})`, 'gi'));
    return parts.map((part, index) =>
      part.toLowerCase() === query.toLowerCase() ? (
        <mark
          key={index}
          className='bg-yellow-500/30 text-yellow-200 font-semibold rounded px-0.5'
        >
          {part}
        </mark>
      ) : (
        part
      )
    );
  };

  const formatDate = (dateString: string) => {
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
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  // Empty state
  if (!isLoading && results.length === 0) {
    return (
      <div className='flex flex-col items-center justify-center h-full py-16 px-4'>
        <div className='relative mb-6'>
          <div className='absolute inset-0 bg-gradient-to-r from-violet-600/20 to-fuchsia-600/20 blur-3xl rounded-full' />
          <div className='relative flex items-center justify-center w-24 h-24 rounded-full bg-white/5 border border-white/10'>
            <SearchX className='w-12 h-12 text-slate-400' />
          </div>
        </div>
        <h3 className='text-lg font-semibold mb-2'>No results found</h3>
        <p className='text-sm text-slate-400 text-center max-w-sm'>
          {searchQuery
            ? `No messages found matching "${searchQuery}". Try different keywords or adjust your filters.`
            : 'Start typing to search messages across all channels.'}
        </p>
      </div>
    );
  }

  return (
    <ScrollArea className={`h-full ${className}`}>
      <div className='space-y-6 p-4'>
        {/* Grouped results by channel */}
        {results.map((group) => {
          const ChannelIcon = CHANNEL_ICONS[group.channelType];

          return (
            <div key={group.channelId} className='space-y-2'>
              {/* Channel header */}
              <div className='flex items-center gap-2 px-2 py-1 sticky top-0 bg-slate-900/95 backdrop-blur-sm border-b border-white/10 z-10'>
                <ChannelIcon className='h-4 w-4 text-violet-400' />
                <h3 className='font-semibold text-sm'>{group.channelName}</h3>
                <Badge
                  variant='outline'
                  className='ml-auto text-xs bg-white/5 border-white/10'
                >
                  {group.results.length}{' '}
                  {group.results.length === 1 ? 'result' : 'results'}
                </Badge>
              </div>

              {/* Results for this channel */}
              <div className='space-y-1'>
                {group.results.map(({ message, highlights }) => {
                  const TypeIcon = MESSAGE_TYPE_ICONS[message.type];

                  return (
                    <button
                      key={message.id}
                      onClick={() =>
                        handleResultClick(group.channelId, message.id)
                      }
                      className='w-full group relative flex flex-col gap-2 p-3 rounded-lg border border-transparent hover:border-violet-500/50 hover:bg-white/5 transition-all duration-200'
                    >
                      {/* Message header */}
                      <div className='flex items-start gap-3'>
                        <Avatar className='h-8 w-8 flex-shrink-0'>
                          <AvatarImage
                            src={message.userAvatar}
                            alt={message.userName}
                          />
                          <AvatarFallback className='bg-gradient-to-br from-violet-600 to-fuchsia-600 text-white text-xs'>
                            {message.userName.charAt(0)}
                          </AvatarFallback>
                        </Avatar>

                        <div className='flex-1 min-w-0'>
                          <div className='flex items-center gap-2 mb-1'>
                            <span className='font-semibold text-sm'>
                              {message.userName}
                            </span>
                            <span className='text-xs text-slate-400'>
                              {formatDate(message.createdAt)}
                            </span>
                            <TypeIcon className='h-3.5 w-3.5 text-slate-400' />
                            {message.attachments.length > 0 && (
                              <Badge
                                variant='outline'
                                className='text-[10px] h-4 px-1 bg-white/5 border-white/10'
                              >
                                {message.attachments.length} file
                                {message.attachments.length > 1 ? 's' : ''}
                              </Badge>
                            )}
                          </div>

                          {/* Highlighted content */}
                          <p className='text-sm text-slate-300 line-clamp-2'>
                            {highlights.length > 0
                              ? highlightText(highlights[0], searchQuery)
                              : highlightText(message.content, searchQuery)}
                          </p>

                          {/* Reactions preview */}
                          {message.reactions.length > 0 && (
                            <div className='flex items-center gap-1 mt-2'>
                              {message.reactions
                                .slice(0, 3)
                                .map((reaction, idx) => (
                                  <div
                                    key={idx}
                                    className='flex items-center gap-0.5 px-1.5 py-0.5 rounded-full bg-white/5 border border-white/10'
                                  >
                                    <span className='text-xs'>
                                      {reaction.emoji}
                                    </span>
                                    <span className='text-[10px] text-slate-400'>
                                      {reaction.count}
                                    </span>
                                  </div>
                                ))}
                              {message.reactions.length > 3 && (
                                <span className='text-xs text-slate-400'>
                                  +{message.reactions.length - 3} more
                                </span>
                              )}
                            </div>
                          )}
                        </div>

                        {/* Navigate icon */}
                        <ChevronRight className='h-5 w-5 text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0' />
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}

        {/* Loading state */}
        {isLoading && (
          <div className='flex items-center justify-center py-8'>
            <Loader2 className='h-8 w-8 animate-spin text-violet-500' />
          </div>
        )}

        {/* Infinite scroll trigger */}
        {hasMore && !isLoading && (
          <div
            ref={loadMoreRef}
            className='h-20 flex items-center justify-center'
          >
            <Loader2 className='h-6 w-6 animate-spin text-violet-500' />
          </div>
        )}
      </div>
    </ScrollArea>
  );
}
