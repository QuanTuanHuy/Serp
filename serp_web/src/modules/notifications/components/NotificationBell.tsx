/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification Bell with Badge
 */

'use client';

import { Bell } from 'lucide-react';
import { cn } from '@/shared/utils';
import { useAppSelector } from '@/shared/hooks';
import { selectUnreadCount, selectHasUrgent } from '../store/notificationSlice';

interface NotificationBellProps {
  className?: string;
  onClick?: () => void;
}

export function NotificationBell({
  className,
  onClick,
}: NotificationBellProps) {
  const unreadCount = useAppSelector(selectUnreadCount);
  const hasUrgent = useAppSelector(selectHasUrgent);

  return (
    <button
      className={cn('relative p-2 hover:bg-accent rounded-md', className)}
      onClick={onClick}
    >
      <Bell
        className={cn('h-5 w-5', hasUrgent && 'text-destructive animate-pulse')}
      />
      {unreadCount > 0 && (
        <span
          className={cn(
            'absolute -top-0.5 -right-0.5 h-5 min-w-5 px-1 rounded-full text-xs font-medium flex items-center justify-center',
            hasUrgent
              ? 'bg-destructive text-destructive-foreground animate-bounce'
              : 'bg-primary text-primary-foreground'
          )}
        >
          {unreadCount > 99 ? '99+' : unreadCount}
        </span>
      )}
    </button>
  );
}

export default NotificationBell;
