/*
Author: QuanTuanHuy
Description: Part of Serp Project - Channel group header for discuss module
*/

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import { ChevronDown, Hash, Users, MessageSquare } from 'lucide-react';
import type { ChannelType } from '../types';

interface ChannelGroupHeaderProps {
  type: ChannelType;
  count: number;
  isExpanded: boolean;
  onToggle: () => void;
}

const GROUP_CONFIG: Record<
  ChannelType,
  { label: string; icon: React.ReactNode; color: string }
> = {
  DIRECT: {
    label: 'Direct Messages',
    icon: <MessageSquare className='h-4 w-4' />,
    color: 'text-blue-500',
  },
  GROUP: {
    label: 'Groups',
    icon: <Users className='h-4 w-4' />,
    color: 'text-violet-500',
  },
  TOPIC: {
    label: 'Topics',
    icon: <Hash className='h-4 w-4' />,
    color: 'text-emerald-500',
  },
};

export const ChannelGroupHeader: React.FC<ChannelGroupHeaderProps> = ({
  type,
  count,
  isExpanded,
  onToggle,
}) => {
  const config = GROUP_CONFIG[type];

  return (
    <button
      onClick={onToggle}
      className={cn(
        'group w-full flex items-center gap-2 px-3 py-2',
        'text-left rounded-lg transition-all duration-200',
        'hover:bg-slate-100/50 dark:hover:bg-slate-800/50',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500 focus-visible:ring-offset-2'
      )}
    >
      {/* Expand/Collapse Icon */}
      <ChevronDown
        className={cn(
          'h-3.5 w-3.5 text-slate-400 transition-transform duration-200',
          isExpanded ? 'rotate-0' : '-rotate-90'
        )}
      />

      {/* Type Icon */}
      <div className={cn('transition-colors', config.color)}>{config.icon}</div>

      {/* Label */}
      <span className='text-xs font-bold uppercase tracking-wider text-slate-600 dark:text-slate-300 flex-1'>
        {config.label}
      </span>

      {/* Count Badge */}
      <span
        className={cn(
          'text-xs font-semibold px-2 py-0.5 rounded-full',
          'bg-slate-200 dark:bg-slate-700',
          'text-slate-600 dark:text-slate-300',
          'transition-colors group-hover:bg-slate-300 dark:group-hover:bg-slate-600'
        )}
      >
        {count}
      </span>
    </button>
  );
};
