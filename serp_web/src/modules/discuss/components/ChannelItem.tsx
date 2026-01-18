/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel item component for discuss module
*/

'use client';

import React from 'react';
import { cn, getAvatarColor } from '@/shared/utils';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Badge,
} from '@/shared/components/ui';
import { Hash, Users, Lock } from 'lucide-react';
import type { Channel, ChannelType } from '../types';

interface ChannelItemProps {
  channel: Channel;
  isActive?: boolean;
  onClick: (channel: Channel) => void;
}

const getChannelIcon = (type: ChannelType) => {
  switch (type) {
    case 'DIRECT':
      return null; // Will show avatar
    case 'GROUP':
      return <Users className='h-4 w-4 text-violet-500' />;
    case 'TOPIC':
      return <Hash className='h-4 w-4 text-emerald-500' />;
    default:
      return <Hash className='h-4 w-4' />;
  }
};

const getChannelInitials = (name: string) => {
  return name
    .split(' ')
    .map((word) => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

const formatTimestamp = (timestamp?: string) => {
  if (!timestamp) return '';

  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return 'now';
  if (minutes < 60) return `${minutes}m`;
  if (hours < 24) return `${hours}h`;
  if (days < 7) return `${days}d`;

  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

export const ChannelItem: React.FC<ChannelItemProps> = ({
  channel,
  isActive = false,
  onClick,
}) => {
  const showAvatar = channel.type === 'DIRECT' || channel.avatarUrl;
  const icon = showAvatar ? null : getChannelIcon(channel.type);

  return (
    <button
      onClick={() => onClick(channel)}
      className={cn(
        'group relative w-full flex items-start gap-3 px-3 py-3',
        'rounded-lg transition-all duration-200',
        'hover:bg-gradient-to-r',
        isActive
          ? 'bg-gradient-to-r from-violet-500/10 to-fuchsia-500/10 shadow-sm border border-violet-500/20'
          : 'hover:from-slate-100 hover:to-slate-50 dark:hover:from-slate-800/50 dark:hover:to-slate-800/30',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2'
      )}
    >
      {/* Avatar/Icon */}
      <div className='relative flex-shrink-0'>
        {showAvatar ? (
          <Avatar className='h-10 w-10 ring-2 ring-white dark:ring-slate-900'>
            {channel.avatarUrl && <AvatarImage src={channel.avatarUrl} alt={channel.name} />}
            <AvatarFallback className={cn(
              'text-xs font-semibold text-white bg-gradient-to-br',
              getAvatarColor(channel.name)
            )}>
              {getChannelInitials(channel.name)}
            </AvatarFallback>
          </Avatar>
        ) : (
          <div className='h-10 w-10 rounded-full bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-800 dark:to-slate-700 flex items-center justify-center ring-2 ring-white dark:ring-slate-900'>
            {icon}
          </div>
        )}

        {/* Unread indicator dot */}
        {channel.unreadCount > 0 && (
          <div className='absolute -top-0.5 -right-0.5 h-3 w-3 bg-gradient-to-br from-rose-500 to-pink-500 rounded-full ring-2 ring-white dark:ring-slate-900 animate-pulse' />
        )}
      </div>

      {/* Channel Info */}
      <div className='flex-1 min-w-0 text-left'>
        <div className='flex items-start justify-between gap-2 mb-1'>
          <h4
            className={cn(
              'text-sm font-semibold transition-colors flex-1 break-words',
              isActive
                ? 'text-violet-700 dark:text-violet-300'
                : 'text-slate-900 dark:text-slate-100',
              channel.unreadCount > 0 &&
                !isActive &&
                'text-slate-900 dark:text-white'
            )}
          >
            {channel.name}
            {channel.isArchived && (
              <Lock className='inline ml-1 h-3 w-3 text-slate-400' />
            )}
          </h4>

          {channel.lastMessageAt && (
            <span className='text-xs text-slate-500 dark:text-slate-400 flex-shrink-0'>
              {formatTimestamp(channel.lastMessageAt)}
            </span>
          )}
        </div>

        {/* Last Message Preview */}
        {channel.lastMessage && (
          <p
            className={cn(
              'text-xs transition-colors line-clamp-2 break-words',
              isActive
                ? 'text-violet-600/80 dark:text-violet-400/80'
                : channel.unreadCount > 0
                  ? 'text-slate-700 dark:text-slate-300 font-medium'
                  : 'text-slate-500 dark:text-slate-400'
            )}
          >
            {channel.lastMessage}
          </p>
        )}
      </div>

      {/* Unread Badge */}
      {channel.unreadCount > 0 && (
        <Badge
          variant='default'
          className={cn(
            'ml-auto flex-shrink-0 h-5 min-w-[1.25rem] px-1.5',
            'bg-gradient-to-br from-rose-500 to-pink-500',
            'text-white text-xs font-bold',
            'shadow-sm'
          )}
        >
          {channel.unreadCount > 99 ? '99+' : channel.unreadCount}
        </Badge>
      )}

      {/* Hover effect */}
      <div
        className={cn(
          'absolute inset-0 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none',
          'bg-gradient-to-r from-violet-500/5 to-fuchsia-500/5'
        )}
      />
    </button>
  );
};
