/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Reusable Notification Button for module headers
 */

'use client';

import { Bell } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Separator } from '@/shared/components/ui/separator';
import { Loader2, CheckCheck, Settings } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useCallback } from 'react';
import { useAppSelector, useAppDispatch } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import {
  selectNotifications,
  selectUnreadCount,
  selectHasUrgent,
  markAsRead,
} from '../store/notificationSlice';
import {
  useGetNotificationsQuery,
  useMarkNotificationAsReadMutation,
  useMarkAllNotificationsAsReadMutation,
} from '../services/notificationApi';
import { NotificationItem } from './NotificationItem';
import { NotificationResponse } from '../types/notification.types';

interface NotificationButtonProps {
  className?: string;
  maxItems?: number;
  settingsPath?: string;
  allNotificationsPath?: string;
}

export function NotificationButton({
  className,
  maxItems = 5,
  settingsPath = '/settings/notifications',
  allNotificationsPath = '/notifications',
}: NotificationButtonProps) {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const notifications = useAppSelector(selectNotifications);
  const unreadCount = useAppSelector(selectUnreadCount);
  const hasUrgent = useAppSelector(selectHasUrgent);

  const { data, isFetching } = useGetNotificationsQuery(
    { page: 0, pageSize: maxItems },
    { refetchOnMountOrArgChange: true }
  );

  const [markAsReadApi] = useMarkNotificationAsReadMutation();
  const [markAllAsReadApi] = useMarkAllNotificationsAsReadMutation();

  const displayNotifications =
    data?.notifications || notifications.slice(0, maxItems);

  const handleMarkAsRead = useCallback(
    async (id: number) => {
      dispatch(markAsRead(id));
      try {
        await markAsReadApi(id).unwrap();
      } catch (error) {
        console.error('Failed to mark notification as read:', error);
      }
    },
    [dispatch, markAsReadApi]
  );

  const handleMarkAllAsRead = useCallback(async () => {
    try {
      await markAllAsReadApi().unwrap();
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error);
    }
  }, [markAllAsReadApi]);

  const handleNotificationClick = useCallback(
    (notification: NotificationResponse) => {
      if (notification.actionUrl?.startsWith('/')) {
        router.push(notification.actionUrl);
      }
    },
    [router]
  );

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant='ghost' size='sm' className={cn('relative', className)}>
          <Bell
            className={cn(
              'h-5 w-5',
              hasUrgent && 'text-destructive animate-pulse'
            )}
          />
          {unreadCount > 0 && (
            <span
              className={cn(
                'absolute -top-1 -right-1 h-5 min-w-5 px-1 rounded-full text-xs font-medium flex items-center justify-center',
                hasUrgent
                  ? 'bg-destructive text-destructive-foreground'
                  : 'bg-red-500 text-white'
              )}
            >
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end' className='w-80'>
        {/* Header */}
        <div className='flex items-center justify-between p-3'>
          <h4 className='font-semibold'>Thông báo</h4>
          <div className='flex items-center gap-1'>
            {unreadCount > 0 && (
              <Button
                variant='ghost'
                size='sm'
                onClick={handleMarkAllAsRead}
                className='h-8 text-xs'
              >
                <CheckCheck className='h-4 w-4 mr-1' />
                Đọc tất cả
              </Button>
            )}
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8'
              onClick={() => router.push(settingsPath)}
            >
              <Settings className='h-4 w-4' />
            </Button>
          </div>
        </div>
        <Separator />

        {/* Notification List */}
        <ScrollArea className='h-[300px]'>
          {isFetching && displayNotifications.length === 0 ? (
            <div className='flex items-center justify-center h-full'>
              <Loader2 className='h-6 w-6 animate-spin text-muted-foreground' />
            </div>
          ) : displayNotifications.length === 0 ? (
            <div className='flex flex-col items-center justify-center h-full text-muted-foreground'>
              <Bell className='h-10 w-10 mb-2 opacity-50' />
              <p className='text-sm'>Không có thông báo</p>
            </div>
          ) : (
            <div className='divide-y'>
              {displayNotifications.map((notification) => (
                <NotificationItem
                  key={notification.id}
                  notification={notification}
                  onClick={handleNotificationClick}
                  onMarkAsRead={handleMarkAsRead}
                />
              ))}
            </div>
          )}
        </ScrollArea>

        <Separator />
        {/* Footer */}
        <div className='p-2'>
          <Button
            variant='ghost'
            className='w-full text-sm'
            onClick={() => router.push(allNotificationsPath)}
          >
            Xem tất cả thông báo
          </Button>
        </div>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

export default NotificationButton;
