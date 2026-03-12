/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel list component for discuss module
*/

'use client';

import React, { useState, useMemo, useCallback } from 'react';
import Link from 'next/link';
import { cn } from '@/shared/utils';
import { Input, ScrollArea, Button } from '@/shared/components/ui';
import { useDebounce } from '@/shared/hooks';
import {
  Search,
  Plus,
  Loader2,
  AlertCircle,
  ArrowLeft,
  MessageSquare,
  X,
} from 'lucide-react';
import { ChannelItem } from './ChannelItem';
import { ChannelGroupHeader } from './ChannelGroupHeader';
import { CreateChannelDialog } from './CreateChannelDialog';
import { useGetChannelsQuery } from '../api/discussApi';
import type { Channel, ChannelType } from '../types';

interface ChannelListProps {
  onChannelSelect: (channel: Channel) => void;
  selectedChannelId?: string;
  className?: string;
}

type ExpandedState = Record<ChannelType, boolean>;

const CHANNEL_TYPES: ChannelType[] = ['DIRECT', 'GROUP', 'TOPIC'];

const EMPTY_GROUPS: Record<ChannelType, Channel[]> = {
  DIRECT: [],
  GROUP: [],
  TOPIC: [],
};

export const ChannelList: React.FC<ChannelListProps> = ({
  onChannelSelect,
  selectedChannelId,
  className,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [isHeaderHovered, setIsHeaderHovered] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [expandedGroups, setExpandedGroups] = useState<ExpandedState>({
    DIRECT: true,
    GROUP: true,
    TOPIC: true,
  });

  const debouncedSearch = useDebounce(searchQuery, 300);
  const isSearchStale = debouncedSearch !== searchQuery;

  const {
    data: channelsResponse,
    isLoading,
    isFetching,
    isError,
    error,
  } = useGetChannelsQuery({
    filters: {
      search: debouncedSearch || undefined,
    },
    pagination: { page: 1, limit: 100 },
  });

  // Group channels by type, sort by last message time (js-combine-iterations)
  const groupedChannels = useMemo(() => {
    const channels = channelsResponse?.data?.items;
    if (!channels || channels.length === 0) return EMPTY_GROUPS;

    const groups: Record<ChannelType, Channel[]> = {
      DIRECT: [],
      GROUP: [],
      TOPIC: [],
    };

    for (let i = 0; i < channels.length; i++) {
      const channel = channels[i];
      if (!channel.isArchived) {
        groups[channel.type].push(channel);
      }
    }

    // Sort each group by last message time (most recent first)
    for (const type of CHANNEL_TYPES) {
      groups[type].sort((a, b) => {
        const timeA = a.lastMessageAt
          ? new Date(a.lastMessageAt).getTime()
          : 0;
        const timeB = b.lastMessageAt
          ? new Date(b.lastMessageAt).getTime()
          : 0;
        return timeB - timeA;
      });
    }

    return groups;
  }, [channelsResponse]);

  // Stable callbacks (rerender-functional-setstate)
  const toggleGroup = useCallback((type: ChannelType) => {
    setExpandedGroups((prev) => ({
      ...prev,
      [type]: !prev[type],
    }));
  }, []);

  const handleSearchChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      setSearchQuery(e.target.value);
    },
    []
  );

  const handleClearSearch = useCallback(() => {
    setSearchQuery('');
  }, []);

  const handleOpenCreateDialog = useCallback(() => {
    setCreateDialogOpen(true);
  }, []);

  const handleCreateSuccess = useCallback(() => {
    // Channels will auto-refresh via RTK Query cache invalidation
  }, []);

  const totalUnread = useMemo(() => {
    const channels = channelsResponse?.data?.items;
    if (!channels || channels.length === 0) return 0;

    let total = 0;
    for (let i = 0; i < channels.length; i++) {
      total += channels[i].unreadCount;
    }
    return total;
  }, [channelsResponse]);

  const isSearching = !!debouncedSearch;

  // Loading state (initial load only)
  if (isLoading) {
    return (
      <div
        className={cn(
          'flex flex-col items-center justify-center h-full gap-3',
          className
        )}
      >
        <Loader2 className='h-8 w-8 text-violet-500 animate-spin' />
        <p className='text-sm text-slate-500 dark:text-slate-400'>
          Loading channels...
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
          Failed to load channels
        </p>
        <p className='text-xs text-slate-500 dark:text-slate-400 text-center'>
          {error && 'data' in error ? String(error.data) : 'An error occurred'}
        </p>
      </div>
    );
  }

  return (
    <div className={cn('flex flex-col h-full', className)}>
      {/* Header */}
      <div className='flex-shrink-0 px-4 py-3 border-b border-slate-200 dark:border-slate-700'>
        <div className='flex items-center justify-between mb-3'>
          <Link
            href='/home'
            className='flex items-center gap-2 group'
            onMouseEnter={() => setIsHeaderHovered(true)}
            onMouseLeave={() => setIsHeaderHovered(false)}
          >
            <div
              className={cn(
                'flex h-8 w-8 items-center justify-center rounded-md transition-all duration-200',
                isHeaderHovered
                  ? 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300'
                  : 'bg-violet-50 dark:bg-violet-900/20 text-violet-600 dark:text-violet-400'
              )}
            >
              {isHeaderHovered ? (
                <ArrowLeft className='h-5 w-5' />
              ) : (
                <MessageSquare className='h-5 w-5' />
              )}
            </div>
            <h2
              className={cn(
                'text-lg font-bold transition-colors',
                !isHeaderHovered &&
                  'bg-gradient-to-r from-violet-600 to-fuchsia-600 bg-clip-text text-transparent',
                isHeaderHovered && 'text-slate-700 dark:text-slate-200'
              )}
            >
              Discuss
            </h2>
          </Link>
          {totalUnread > 0 ? (
            <span className='px-2 py-0.5 text-xs font-bold rounded-full bg-gradient-to-br from-rose-500 to-pink-500 text-white shadow-sm'>
              {totalUnread > 99 ? '99+' : totalUnread}
            </span>
          ) : null}
        </div>

        {/* Search */}
        <div className='relative'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400' />
          <Input
            type='text'
            placeholder='Search channels...'
            value={searchQuery}
            onChange={handleSearchChange}
            className={cn(
              'pl-9 pr-8 h-9',
              'bg-slate-100 dark:bg-slate-800',
              'border-transparent',
              'focus-visible:ring-violet-500',
              'placeholder:text-slate-500'
            )}
          />
          {searchQuery ? (
            <button
              type='button'
              onClick={handleClearSearch}
              className='absolute right-2 top-1/2 -translate-y-1/2 p-0.5 rounded-sm text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 transition-colors'
              aria-label='Clear search'
            >
              <X className='h-3.5 w-3.5' />
            </button>
          ) : null}
        </div>
      </div>

      {/* Channel List */}
      <ScrollArea className='flex-1'>
        <div
          className={cn(
            'px-2 py-3 space-y-1 transition-opacity duration-150',
            (isSearchStale || isFetching) && 'opacity-70'
          )}
        >
          {CHANNEL_TYPES.map((type) => {
            const channels = groupedChannels[type];
            const hasChannels = channels.length > 0;

            if (!hasChannels && isSearching) return null;

            return (
              <div key={type} className='space-y-1'>
                <ChannelGroupHeader
                  type={type}
                  count={channels.length}
                  isExpanded={expandedGroups[type]}
                  onToggle={() => toggleGroup(type)}
                />

                {expandedGroups[type] ? (
                  <div className='pl-2 space-y-0.5'>
                    {hasChannels ? (
                      channels.map((channel) => (
                        <ChannelItem
                          key={channel.id}
                          channel={channel}
                          isActive={channel.id === selectedChannelId}
                          onClick={onChannelSelect}
                        />
                      ))
                    ) : (
                      <p className='px-3 py-2 text-xs text-slate-500 dark:text-slate-400 italic'>
                        No {type.toLowerCase()} channels
                      </p>
                    )}
                  </div>
                ) : null}
              </div>
            );
          })}

          {/* No results feedback for search */}
          {isSearching &&
          !isFetching &&
          CHANNEL_TYPES.every(
            (type) => groupedChannels[type].length === 0
          ) ? (
            <div className='flex flex-col items-center justify-center py-8 gap-2'>
              <Search className='h-8 w-8 text-slate-300 dark:text-slate-600' />
              <p className='text-sm text-slate-500 dark:text-slate-400'>
                No channels found
              </p>
              <p className='text-xs text-slate-400 dark:text-slate-500'>
                Try a different search term
              </p>
            </div>
          ) : null}
        </div>
      </ScrollArea>

      {/* Footer - New Channel Button */}
      <div className='flex-shrink-0 p-3 border-t border-slate-200 dark:border-slate-700'>
        <Button
          variant='outline'
          size='sm'
          onClick={handleOpenCreateDialog}
          className={cn(
            'w-full',
            'bg-gradient-to-r from-violet-500/10 to-fuchsia-500/10',
            'hover:from-violet-500/20 hover:to-fuchsia-500/20',
            'border-violet-200 dark:border-violet-800',
            'text-violet-700 dark:text-violet-300',
            'font-semibold',
            'transition-all duration-200'
          )}
        >
          <Plus className='h-4 w-4 mr-2' />
          New Channel
        </Button>
      </div>

      {/* Create Channel Dialog */}
      <CreateChannelDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
        onSuccess={handleCreateSuccess}
      />
    </div>
  );
};
