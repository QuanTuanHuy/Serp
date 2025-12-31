/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel list component for discuss module
*/

'use client';

import React, { useState, useMemo } from 'react';
import Link from 'next/link';
import { cn } from '@/shared/utils';
import { Input, ScrollArea, Button } from '@/shared/components/ui';
import {
  Search,
  Plus,
  Loader2,
  AlertCircle,
  ArrowLeft,
  MessageSquare,
} from 'lucide-react';
import { ChannelItem } from './ChannelItem';
import { ChannelGroupHeader } from './ChannelGroupHeader';
import { useGetChannelsQuery } from '../api/discussApi';
import type { Channel, ChannelType } from '../types';

interface ChannelListProps {
  onChannelSelect: (channel: Channel) => void;
  selectedChannelId?: string;
  className?: string;
}

type ExpandedState = Record<ChannelType, boolean>;

export const ChannelList: React.FC<ChannelListProps> = ({
  onChannelSelect,
  selectedChannelId,
  className,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [isHeaderHovered, setIsHeaderHovered] = useState(false);
  const [expandedGroups, setExpandedGroups] = useState<ExpandedState>({
    DIRECT: true,
    GROUP: true,
    TOPIC: true,
  });

  // Fetch channels from RTK Query
  const {
    data: channelsResponse,
    isLoading,
    isError,
    error,
  } = useGetChannelsQuery({
    filters: {},
    pagination: { page: 1, limit: 100 },
  });

  // Group and filter channels
  const groupedChannels = useMemo(() => {
    const channels = channelsResponse?.data?.data || [];

    // Filter by search query
    const filtered = channels.filter((channel: Channel) =>
      channel.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Group by type
    const groups: Record<ChannelType, Channel[]> = {
      DIRECT: [],
      GROUP: [],
      TOPIC: [],
    };

    filtered.forEach((channel: Channel) => {
      if (!channel.isArchived) {
        groups[channel.type].push(channel);
      }
    });

    // Sort by last message time (most recent first)
    Object.keys(groups).forEach((type) => {
      groups[type as ChannelType].sort((a, b) => {
        const timeA = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0;
        const timeB = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0;
        return timeB - timeA;
      });
    });

    return groups;
  }, [channelsResponse, searchQuery]);

  const toggleGroup = (type: ChannelType) => {
    setExpandedGroups((prev) => ({
      ...prev,
      [type]: !prev[type],
    }));
  };

  const totalUnread = useMemo(() => {
    const channels = channelsResponse?.data?.data || [];
    return channels.reduce(
      (sum: number, channel: Channel) => sum + channel.unreadCount,
      0
    );
  }, [channelsResponse]);

  // Loading state
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
          {totalUnread > 0 && (
            <span className='px-2 py-0.5 text-xs font-bold rounded-full bg-gradient-to-br from-rose-500 to-pink-500 text-white shadow-sm'>
              {totalUnread > 99 ? '99+' : totalUnread}
            </span>
          )}
        </div>

        {/* Search */}
        <div className='relative'>
          <Search className='absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400' />
          <Input
            type='text'
            placeholder='Search channels...'
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className={cn(
              'pl-9 h-9',
              'bg-slate-100 dark:bg-slate-800',
              'border-transparent',
              'focus-visible:ring-violet-500',
              'placeholder:text-slate-500'
            )}
          />
        </div>
      </div>

      {/* Channel List */}
      <ScrollArea className='flex-1'>
        <div className='px-2 py-3 space-y-1'>
          {(['DIRECT', 'GROUP', 'TOPIC'] as ChannelType[]).map((type) => {
            const channels = groupedChannels[type];
            const hasChannels = channels.length > 0;

            if (!hasChannels && searchQuery) return null;

            return (
              <div key={type} className='space-y-1'>
                <ChannelGroupHeader
                  type={type}
                  count={channels.length}
                  isExpanded={expandedGroups[type]}
                  onToggle={() => toggleGroup(type)}
                />

                {expandedGroups[type] && (
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
                )}
              </div>
            );
          })}
        </div>
      </ScrollArea>

      {/* Footer - New Channel Button */}
      <div className='flex-shrink-0 p-3 border-t border-slate-200 dark:border-slate-700'>
        <Button
          variant='outline'
          size='sm'
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
    </div>
  );
};
