/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification Toast Provider
 */

'use client';

import { useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Info, CheckCircle, AlertTriangle, AlertCircle, X } from 'lucide-react';
import { toast, Toaster, Toast } from 'react-hot-toast';
import { useAppSelector } from '@/shared/hooks';
import { selectNotifications } from '../store/notificationSlice';
import {
  NotificationResponse,
  NotificationType,
} from '../types/notification.types';
import {
  notifyUser,
  showBrowserNotification,
  requestNotificationPermission,
} from '../utils/notificationSound';

const typeIcons: Record<NotificationType, React.ReactNode> = {
  INFO: <Info className='h-5 w-5 text-blue-500' />,
  SUCCESS: <CheckCircle className='h-5 w-5 text-green-500' />,
  WARNING: <AlertTriangle className='h-5 w-5 text-yellow-500' />,
  ERROR: <AlertCircle className='h-5 w-5 text-red-500' />,
};

interface NotificationToastProps {
  notification: NotificationResponse;
  t: Toast;
  onClose: () => void;
  onClick?: () => void;
}

function NotificationToastContent({
  notification,
  t,
  onClose,
  onClick,
}: NotificationToastProps) {
  return (
    <div
      className={`${
        t.visible ? 'animate-enter' : 'animate-leave'
      } max-w-sm w-full bg-background shadow-lg rounded-lg pointer-events-auto flex ring-1 ring-black ring-opacity-5 border`}
    >
      <div className='flex-1 w-0 p-4 cursor-pointer' onClick={onClick}>
        <div className='flex items-start'>
          <div className='flex-shrink-0'>{typeIcons[notification.type]}</div>
          <div className='ml-3 flex-1'>
            <p className='text-sm font-medium text-foreground'>
              {notification.title}
            </p>
            <p className='mt-1 text-sm text-muted-foreground line-clamp-2'>
              {notification.message}
            </p>
          </div>
        </div>
      </div>
      <div className='flex border-l border-border'>
        <button
          onClick={onClose}
          className='w-full border border-transparent rounded-none rounded-r-lg p-4 flex items-center justify-center text-sm font-medium text-muted-foreground hover:text-foreground focus:outline-none'
        >
          <X className='h-5 w-5' />
        </button>
      </div>
    </div>
  );
}

export function NotificationToastProvider() {
  const router = useRouter();
  const notifications = useAppSelector(selectNotifications);
  const prevNotificationsRef = useRef<NotificationResponse[]>([]);
  const hasRequestedPermission = useRef(false);

  // Request browser notification permission on mount
  useEffect(() => {
    if (!hasRequestedPermission.current) {
      hasRequestedPermission.current = true;
      requestNotificationPermission();
    }
  }, []);

  // Watch for new notifications
  useEffect(() => {
    const prevNotifications = prevNotificationsRef.current;

    // Find new notifications
    const newNotifications = notifications.filter(
      (n) => !prevNotifications.some((prev) => prev.id === n.id)
    );

    // Show toast for each new notification
    newNotifications.forEach((notification) => {
      const isUrgent = notification.priority === 'URGENT';

      // Play sound and vibrate
      notifyUser({
        sound: true,
        vibrate: true,
        urgent: isUrgent,
      });

      // Show browser notification if tab is not focused
      if (document.hidden) {
        showBrowserNotification(notification.title, {
          body: notification.message,
          tag: `notification-${notification.id}`,
        });
      }

      // Show toast
      toast.custom(
        (t) => (
          <NotificationToastContent
            notification={notification}
            t={t}
            onClose={() => toast.dismiss(t.id)}
            onClick={() => {
              toast.dismiss(t.id);
              if (notification.actionUrl) {
                if (notification.actionUrl.startsWith('/')) {
                  router.push(notification.actionUrl);
                } else {
                  window.open(notification.actionUrl, '_blank');
                }
              }
            }}
          />
        ),
        {
          duration: isUrgent ? 10000 : 5000,
          position: 'top-right',
        }
      );
    });

    // Update ref
    prevNotificationsRef.current = notifications;
  }, [notifications, router]);

  return (
    <Toaster
      position='top-right'
      toastOptions={{
        duration: 5000,
      }}
    />
  );
}

export default NotificationToastProvider;
