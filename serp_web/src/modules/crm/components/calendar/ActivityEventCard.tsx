// ActivityEventCard Component (authors: QuanTuanHuy, Description: Part of Serp Project)

'use client';

import React from 'react';
import { Phone, Mail, Users, CheckCircle2 } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { Activity, ActivityType } from '../../types';

const ACTIVITY_ICON_MAP: Record<
  string,
  { icon: React.ElementType; bg: string; text: string }
> = {
  CALL: { icon: Phone, bg: 'bg-blue-100', text: 'text-blue-600' },
  EMAIL: { icon: Mail, bg: 'bg-purple-100', text: 'text-purple-600' },
  MEETING: { icon: Users, bg: 'bg-green-100', text: 'text-green-600' },
  TASK: {
    icon: CheckCircle2,
    bg: 'bg-orange-100',
    text: 'text-orange-600',
  },
};

interface ActivityEventCardProps {
  activity: Activity;
  isMonthView?: boolean;
}

export const ActivityEventCard: React.FC<ActivityEventCardProps> = ({
  activity,
  isMonthView = false,
}) => {
  const config = ACTIVITY_ICON_MAP[activity.type] || ACTIVITY_ICON_MAP.TASK;
  const Icon = config.icon;
  const isInactive =
    activity.status === 'COMPLETED' || activity.status === 'CANCELLED';

  if (isMonthView) {
    return (
      <div
        className={cn(
          'flex items-center gap-1 px-1 py-0.5 text-xs truncate',
          isInactive && 'opacity-50'
        )}
      >
        <Icon className={cn('h-3 w-3 flex-shrink-0', config.text)} />
        <span className={cn('truncate', isInactive && 'line-through')}>
          {activity.subject}
        </span>
      </div>
    );
  }

  // Week/Day view — more detailed
  return (
    <div
      className={cn(
        'flex flex-col gap-0.5 px-2 py-1 h-full text-xs',
        isInactive && 'opacity-50'
      )}
    >
      <div className='flex items-center gap-1'>
        <Icon className={cn('h-3 w-3 flex-shrink-0', config.text)} />
        <span
          className={cn('font-medium truncate', isInactive && 'line-through')}
        >
          {activity.subject}
        </span>
      </div>
      {activity.assignedToName && (
        <span className='text-[10px] opacity-70 truncate'>
          {activity.assignedToName}
        </span>
      )}
    </div>
  );
};
