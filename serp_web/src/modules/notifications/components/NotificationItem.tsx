/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification Item Component
 */

'use client';

import { formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';
import {
  Info,
  CheckCircle,
  AlertTriangle,
  AlertCircle,
  ExternalLink,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  NotificationResponse,
  NotificationType,
  NotificationPriority,
} from '../types/notification.types';

interface NotificationItemProps {
  notification: NotificationResponse;
  onClick?: (notification: NotificationResponse) => void;
  onMarkAsRead?: (id: number) => void;
}

const typeIcons: Record<NotificationType, React.ReactNode> = {
  INFO: <Info className='h-5 w-5 text-blue-500' />,
  SUCCESS: <CheckCircle className='h-5 w-5 text-green-500' />,
  WARNING: <AlertTriangle className='h-5 w-5 text-yellow-500' />,
  ERROR: <AlertCircle className='h-5 w-5 text-red-500' />,
};

const priorityColors: Record<NotificationPriority, string> = {
  LOW: 'border-l-gray-300',
  MEDIUM: 'border-l-blue-400',
  HIGH: 'border-l-orange-500',
  URGENT: 'border-l-red-600',
};

export function NotificationItem({
  notification,
  onClick,
  onMarkAsRead,
}: NotificationItemProps) {
  const handleClick = () => {
    if (!notification.isRead && onMarkAsRead) {
      onMarkAsRead(notification.id);
    }
    if (onClick) {
      onClick(notification);
    }
    if (notification.actionUrl) {
      window.open(notification.actionUrl, '_blank');
    }
  };

  const timeAgo = formatDistanceToNow(new Date(notification.createdAt), {
    addSuffix: true,
    locale: vi,
  });

  return (
    <div
      className={cn(
        'flex items-start gap-3 p-3 cursor-pointer transition-colors border-l-4',
        'hover:bg-accent/50',
        !notification.isRead && 'bg-accent/30',
        priorityColors[notification.priority]
      )}
      onClick={handleClick}
    >
      {/* Icon */}
      <div className='flex-shrink-0 mt-0.5'>{typeIcons[notification.type]}</div>

      {/* Content */}
      <div className='flex-1 min-w-0'>
        <div className='flex items-start justify-between gap-2'>
          <p
            className={cn(
              'text-sm font-medium truncate',
              !notification.isRead && 'text-foreground',
              notification.isRead && 'text-muted-foreground'
            )}
          >
            {notification.title}
          </p>
          {!notification.isRead && (
            <span className='flex-shrink-0 h-2 w-2 rounded-full bg-primary' />
          )}
        </div>

        <p
          className={cn(
            'text-xs mt-1 line-clamp-2',
            !notification.isRead && 'text-muted-foreground',
            notification.isRead && 'text-muted-foreground/70'
          )}
        >
          {notification.message}
        </p>

        <div className='flex items-center gap-2 mt-2'>
          <span className='text-xs text-muted-foreground'>{timeAgo}</span>
          {notification.category && (
            <>
              <span className='text-muted-foreground'>•</span>
              <span className='text-xs text-muted-foreground capitalize'>
                {notification.category.toLowerCase()}
              </span>
            </>
          )}
          {notification.actionUrl && (
            <ExternalLink className='h-3 w-3 text-muted-foreground ml-auto' />
          )}
        </div>
      </div>
    </div>
  );
}

export default NotificationItem;
