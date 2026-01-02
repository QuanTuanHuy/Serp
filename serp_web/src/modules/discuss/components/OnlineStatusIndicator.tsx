/*
Author: QuanTuanHuy
Description: Part of Serp Project - Online Status Indicator Component
*/

'use client';

import React from 'react';
import { cn } from '@/shared/utils';

export type OnlineStatus = 'online' | 'away' | 'busy' | 'offline';

interface OnlineStatusIndicatorProps {
  status: OnlineStatus;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  showPulse?: boolean;
}

const statusConfig = {
  online: {
    color: 'bg-emerald-500',
    ring: 'ring-emerald-400/30',
    label: 'Online',
  },
  away: {
    color: 'bg-amber-500',
    ring: 'ring-amber-400/30',
    label: 'Away',
  },
  busy: {
    color: 'bg-rose-500',
    ring: 'ring-rose-400/30',
    label: 'Busy',
  },
  offline: {
    color: 'bg-slate-400',
    ring: 'ring-slate-300/30',
    label: 'Offline',
  },
};

const sizeConfig = {
  sm: 'h-2 w-2',
  md: 'h-2.5 w-2.5',
  lg: 'h-3 w-3',
};

export const OnlineStatusIndicator: React.FC<OnlineStatusIndicatorProps> = ({
  status,
  size = 'md',
  className,
  showPulse = true,
}) => {
  const config = statusConfig[status];
  const sizeClass = sizeConfig[size];
  const shouldAnimate = showPulse && status === 'online';

  return (
    <div className={cn('relative inline-flex', className)} title={config.label}>
      {/* Pulse animation ring (only for online status) */}
      {shouldAnimate && (
        <span
          className={cn(
            'absolute inline-flex h-full w-full rounded-full opacity-75 animate-ping',
            config.color
          )}
        />
      )}

      {/* Status dot */}
      <span
        className={cn(
          'relative inline-flex rounded-full',
          'ring-2 ring-white dark:ring-slate-900',
          config.color,
          sizeClass
        )}
      />
    </div>
  );
};

interface OnlineStatusBadgeProps {
  status: OnlineStatus;
  showLabel?: boolean;
  className?: string;
}

export const OnlineStatusBadge: React.FC<OnlineStatusBadgeProps> = ({
  status,
  showLabel = true,
  className,
}) => {
  const config = statusConfig[status];

  return (
    <div
      className={cn(
        'inline-flex items-center gap-1.5 px-2 py-1 rounded-full',
        'bg-white dark:bg-slate-800 border',
        'border-slate-200 dark:border-slate-700',
        'shadow-sm',
        className
      )}
    >
      <OnlineStatusIndicator status={status} size='sm' />
      {showLabel && (
        <span className='text-xs font-medium text-slate-700 dark:text-slate-300'>
          {config.label}
        </span>
      )}
    </div>
  );
};
