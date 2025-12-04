/**
 * RecentActivity Component - Activity feed for CRM dashboard
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM Dashboard recent activities
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import {
  UserPlus,
  Target,
  DollarSign,
  Mail,
  Phone,
  Calendar,
  CheckCircle2,
  MessageSquare,
  type LucideIcon,
} from 'lucide-react';

export interface ActivityItem {
  id: string;
  type:
    | 'customer_added'
    | 'lead_created'
    | 'deal_won'
    | 'deal_lost'
    | 'email_sent'
    | 'call_made'
    | 'meeting_scheduled'
    | 'task_completed'
    | 'note_added';
  title: string;
  description?: string;
  timestamp: string;
  user?: {
    name: string;
    avatar?: string;
  };
  metadata?: Record<string, string | number>;
}

export interface RecentActivityProps {
  activities: ActivityItem[];
  title?: string;
  isLoading?: boolean;
  className?: string;
  maxItems?: number;
  onViewAll?: () => void;
  onActivityClick?: (activity: ActivityItem) => void;
}

const activityConfig: Record<
  ActivityItem['type'],
  {
    icon: LucideIcon;
    color: string;
    bgColor: string;
  }
> = {
  customer_added: {
    icon: UserPlus,
    color: 'text-blue-600 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
  },
  lead_created: {
    icon: Target,
    color: 'text-amber-600 dark:text-amber-400',
    bgColor: 'bg-amber-100 dark:bg-amber-900/30',
  },
  deal_won: {
    icon: DollarSign,
    color: 'text-emerald-600 dark:text-emerald-400',
    bgColor: 'bg-emerald-100 dark:bg-emerald-900/30',
  },
  deal_lost: {
    icon: DollarSign,
    color: 'text-rose-600 dark:text-rose-400',
    bgColor: 'bg-rose-100 dark:bg-rose-900/30',
  },
  email_sent: {
    icon: Mail,
    color: 'text-indigo-600 dark:text-indigo-400',
    bgColor: 'bg-indigo-100 dark:bg-indigo-900/30',
  },
  call_made: {
    icon: Phone,
    color: 'text-green-600 dark:text-green-400',
    bgColor: 'bg-green-100 dark:bg-green-900/30',
  },
  meeting_scheduled: {
    icon: Calendar,
    color: 'text-purple-600 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
  },
  task_completed: {
    icon: CheckCircle2,
    color: 'text-teal-600 dark:text-teal-400',
    bgColor: 'bg-teal-100 dark:bg-teal-900/30',
  },
  note_added: {
    icon: MessageSquare,
    color: 'text-slate-600 dark:text-slate-400',
    bgColor: 'bg-slate-100 dark:bg-slate-800/50',
  },
};

const formatTimestamp = (timestamp: string): string => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;

  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
};

export const RecentActivity: React.FC<RecentActivityProps> = ({
  activities,
  title = 'Recent Activity',
  isLoading = false,
  className,
  maxItems = 5,
  onViewAll,
  onActivityClick,
}) => {
  const displayedActivities = activities.slice(0, maxItems);

  if (isLoading) {
    return (
      <div
        className={cn('rounded-xl border bg-card p-6 animate-pulse', className)}
      >
        <div className='h-5 w-32 bg-muted rounded mb-6' />
        <div className='space-y-4'>
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className='flex items-start gap-3'>
              <div className='h-9 w-9 bg-muted rounded-full' />
              <div className='flex-1 space-y-2'>
                <div className='h-4 w-3/4 bg-muted rounded' />
                <div className='h-3 w-1/2 bg-muted rounded' />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className={cn('rounded-xl border bg-card p-6', className)}>
      {/* Header */}
      <div className='flex items-center justify-between mb-6'>
        <h3 className='text-lg font-semibold text-foreground'>{title}</h3>
        {onViewAll && (
          <button
            onClick={onViewAll}
            className='text-sm text-primary hover:text-primary/80 font-medium transition-colors'
          >
            View all
          </button>
        )}
      </div>

      {/* Activity List */}
      <div className='space-y-1'>
        {displayedActivities.length === 0 ? (
          <div className='text-center py-8 text-muted-foreground'>
            <MessageSquare className='h-12 w-12 mx-auto mb-3 opacity-20' />
            <p className='text-sm'>No recent activity</p>
          </div>
        ) : (
          displayedActivities.map((activity, index) => {
            const config = activityConfig[activity.type];
            const Icon = config.icon;
            const isLast = index === displayedActivities.length - 1;

            return (
              <div
                key={activity.id}
                className={cn(
                  'group relative flex items-start gap-3 p-3 -mx-3 rounded-lg transition-colors',
                  onActivityClick && 'cursor-pointer hover:bg-muted/50'
                )}
                onClick={() => onActivityClick?.(activity)}
              >
                {/* Timeline line */}
                {!isLast && (
                  <div className='absolute left-[26px] top-12 bottom-0 w-px bg-border' />
                )}

                {/* Icon */}
                <div
                  className={cn(
                    'relative flex h-9 w-9 shrink-0 items-center justify-center rounded-full',
                    config.bgColor
                  )}
                >
                  <Icon className={cn('h-4 w-4', config.color)} />
                </div>

                {/* Content */}
                <div className='flex-1 min-w-0'>
                  <p className='text-sm font-medium text-foreground leading-tight'>
                    {activity.title}
                  </p>
                  {activity.description && (
                    <p className='text-xs text-muted-foreground mt-0.5 line-clamp-1'>
                      {activity.description}
                    </p>
                  )}
                  <div className='flex items-center gap-2 mt-1'>
                    <span className='text-xs text-muted-foreground'>
                      {formatTimestamp(activity.timestamp)}
                    </span>
                    {activity.user && (
                      <>
                        <span className='text-muted-foreground'>•</span>
                        <span className='text-xs text-muted-foreground'>
                          by {activity.user.name}
                        </span>
                      </>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

// Default activities for demo
export const DEFAULT_ACTIVITIES: ActivityItem[] = [
  {
    id: '1',
    type: 'deal_won',
    title: 'Deal closed: Enterprise Plan',
    description: 'Acme Corp - $45,000 annual contract',
    timestamp: new Date(Date.now() - 15 * 60000).toISOString(),
    user: { name: 'John Doe' },
  },
  {
    id: '2',
    type: 'lead_created',
    title: 'New lead from website',
    description: 'TechStart Inc. - Software Development',
    timestamp: new Date(Date.now() - 45 * 60000).toISOString(),
    user: { name: 'System' },
  },
  {
    id: '3',
    type: 'meeting_scheduled',
    title: 'Demo meeting scheduled',
    description: 'With Global Solutions - Dec 5, 2pm',
    timestamp: new Date(Date.now() - 2 * 3600000).toISOString(),
    user: { name: 'Sarah Smith' },
  },
  {
    id: '4',
    type: 'email_sent',
    title: 'Proposal sent',
    description: 'Sent pricing proposal to DataFlow Inc.',
    timestamp: new Date(Date.now() - 5 * 3600000).toISOString(),
    user: { name: 'Mike Johnson' },
  },
  {
    id: '5',
    type: 'customer_added',
    title: 'New customer added',
    description: 'CloudServe Ltd. - Premium tier',
    timestamp: new Date(Date.now() - 24 * 3600000).toISOString(),
    user: { name: 'Emily Chen' },
  },
];
