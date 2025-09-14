/**
 * PTM Notifications Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM Notifications and Alerts
 */

'use client';

import React, { useState } from 'react';
import {
  Bell,
  Check,
  X,
  Clock,
  AlertTriangle,
  Info,
  CheckCircle,
  Settings,
  Filter,
  MoreHorizontal,
} from 'lucide-react';
import { Button, Card } from '@/shared/components';

interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  time: string;
  read: boolean;
  actionable: boolean;
  category: 'task' | 'project' | 'calendar' | 'system';
}

const Notifications: React.FC = () => {
  const [filter, setFilter] = useState<'all' | 'unread' | 'actionable'>('all');

  const [notifications, setNotifications] = useState<Notification[]>([
    {
      id: 1,
      title: 'Task Deadline Approaching',
      message: 'Code review for authentication module is due in 2 hours',
      type: 'warning',
      time: '2 hours ago',
      read: false,
      actionable: true,
      category: 'task',
    },
    {
      id: 2,
      title: 'Meeting Reminder',
      message: 'Team standup meeting starts in 15 minutes',
      type: 'info',
      time: '15 minutes ago',
      read: false,
      actionable: true,
      category: 'calendar',
    },
    {
      id: 3,
      title: 'Task Completed',
      message: 'Database optimization research has been marked as complete',
      type: 'success',
      time: '1 hour ago',
      read: true,
      actionable: false,
      category: 'task',
    },
    {
      id: 4,
      title: 'Project Update',
      message: 'SERP Development project progress updated to 78%',
      type: 'info',
      time: '3 hours ago',
      read: true,
      actionable: false,
      category: 'project',
    },
    {
      id: 5,
      title: 'System Maintenance',
      message: 'Scheduled maintenance will occur tonight from 2-4 AM',
      type: 'warning',
      time: '1 day ago',
      read: false,
      actionable: false,
      category: 'system',
    },
    {
      id: 6,
      title: 'New Task Assignment',
      message: 'You have been assigned to review mobile app UI components',
      type: 'info',
      time: '2 days ago',
      read: true,
      actionable: true,
      category: 'task',
    },
  ]);

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'success':
        return <CheckCircle className='h-5 w-5 text-green-500' />;
      case 'warning':
        return <AlertTriangle className='h-5 w-5 text-yellow-500' />;
      case 'error':
        return <X className='h-5 w-5 text-red-500' />;
      default:
        return <Info className='h-5 w-5 text-blue-500' />;
    }
  };

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'task':
        return 'bg-blue-100 text-blue-700';
      case 'project':
        return 'bg-green-100 text-green-700';
      case 'calendar':
        return 'bg-purple-100 text-purple-700';
      case 'system':
        return 'bg-gray-100 text-gray-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const markAsRead = (id: number) => {
    setNotifications((prev) =>
      prev.map((notif) => (notif.id === id ? { ...notif, read: true } : notif))
    );
  };

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((notif) => ({ ...notif, read: true })));
  };

  const deleteNotification = (id: number) => {
    setNotifications((prev) => prev.filter((notif) => notif.id !== id));
  };

  const filteredNotifications = notifications.filter((notif) => {
    switch (filter) {
      case 'unread':
        return !notif.read;
      case 'actionable':
        return notif.actionable;
      default:
        return true;
    }
  });

  const unreadCount = notifications.filter((n) => !n.read).length;
  const actionableCount = notifications.filter((n) => n.actionable).length;

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-2'>
          <Bell className='h-6 w-6' />
          <h1 className='text-2xl font-bold'>Notifications</h1>
          {unreadCount > 0 && (
            <span className='rounded-full bg-red-500 px-2 py-1 text-xs font-medium text-white'>
              {unreadCount}
            </span>
          )}
        </div>
        <div className='flex gap-2'>
          <Button variant='outline' size='sm' onClick={markAllAsRead}>
            <Check className='mr-2 h-4 w-4' />
            Mark All Read
          </Button>
          <Button variant='outline' size='sm'>
            <Settings className='mr-2 h-4 w-4' />
            Settings
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className='grid grid-cols-1 gap-4 md:grid-cols-4'>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-blue-600'>
            {notifications.length}
          </div>
          <div className='text-sm text-muted-foreground'>Total</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-red-600'>{unreadCount}</div>
          <div className='text-sm text-muted-foreground'>Unread</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-yellow-600'>
            {actionableCount}
          </div>
          <div className='text-sm text-muted-foreground'>Actionable</div>
        </Card>
        <Card className='p-6 text-center'>
          <div className='text-2xl font-bold text-green-600'>
            {notifications.filter((n) => n.type === 'success').length}
          </div>
          <div className='text-sm text-muted-foreground'>Completed</div>
        </Card>
      </div>

      {/* Filter Tabs */}
      <div className='flex gap-2'>
        <Button
          variant={filter === 'all' ? 'default' : 'outline'}
          size='sm'
          onClick={() => setFilter('all')}
        >
          All ({notifications.length})
        </Button>
        <Button
          variant={filter === 'unread' ? 'default' : 'outline'}
          size='sm'
          onClick={() => setFilter('unread')}
        >
          Unread ({unreadCount})
        </Button>
        <Button
          variant={filter === 'actionable' ? 'default' : 'outline'}
          size='sm'
          onClick={() => setFilter('actionable')}
        >
          Actionable ({actionableCount})
        </Button>
        <Button variant='outline' size='sm'>
          <Filter className='mr-2 h-4 w-4' />
          More Filters
        </Button>
      </div>

      {/* Notifications List */}
      <Card className='divide-y'>
        {filteredNotifications.length === 0 ? (
          <div className='p-8 text-center'>
            <Bell className='mx-auto h-12 w-12 text-muted-foreground' />
            <h3 className='mt-4 text-lg font-medium'>No notifications</h3>
            <p className='mt-2 text-sm text-muted-foreground'>
              You're all caught up! No new notifications to show.
            </p>
          </div>
        ) : (
          filteredNotifications.map((notification) => (
            <div
              key={notification.id}
              className={`p-4 hover:bg-muted/50 transition-colors ${
                !notification.read ? 'bg-blue-50/50' : ''
              }`}
            >
              <div className='flex items-start gap-4'>
                {/* Icon */}
                <div className='mt-1'>
                  {getNotificationIcon(notification.type)}
                </div>

                {/* Content */}
                <div className='flex-1 min-w-0'>
                  <div className='flex items-start justify-between'>
                    <div className='flex-1'>
                      <h4
                        className={`text-sm font-medium ${
                          !notification.read ? 'font-semibold' : ''
                        }`}
                      >
                        {notification.title}
                      </h4>
                      <p className='mt-1 text-sm text-muted-foreground'>
                        {notification.message}
                      </p>
                      <div className='mt-2 flex items-center gap-2'>
                        <span
                          className={`rounded-full px-2 py-1 text-xs font-medium ${getCategoryColor(
                            notification.category
                          )}`}
                        >
                          {notification.category}
                        </span>
                        <span className='text-xs text-muted-foreground flex items-center gap-1'>
                          <Clock className='h-3 w-3' />
                          {notification.time}
                        </span>
                      </div>
                    </div>

                    {/* Actions */}
                    <div className='flex items-center gap-2 ml-4'>
                      {!notification.read && (
                        <Button
                          size='sm'
                          variant='ghost'
                          onClick={() => markAsRead(notification.id)}
                        >
                          <Check className='h-4 w-4' />
                        </Button>
                      )}
                      <Button
                        size='sm'
                        variant='ghost'
                        onClick={() => deleteNotification(notification.id)}
                      >
                        <X className='h-4 w-4' />
                      </Button>
                      <Button size='sm' variant='ghost'>
                        <MoreHorizontal className='h-4 w-4' />
                      </Button>
                    </div>
                  </div>

                  {/* Actionable buttons */}
                  {notification.actionable && (
                    <div className='mt-3 flex gap-2'>
                      <Button size='sm'>Take Action</Button>
                      <Button size='sm' variant='outline'>
                        View Details
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </Card>

      {/* Notification Settings */}
      <Card className='p-6'>
        <h3 className='mb-4 text-lg font-semibold'>Notification Preferences</h3>
        <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
          <div className='space-y-3'>
            <h4 className='font-medium'>Task Notifications</h4>
            <div className='space-y-2 text-sm'>
              <label className='flex items-center gap-2'>
                <input type='checkbox' defaultChecked />
                Deadline reminders
              </label>
              <label className='flex items-center gap-2'>
                <input type='checkbox' defaultChecked />
                Task assignments
              </label>
              <label className='flex items-center gap-2'>
                <input type='checkbox' defaultChecked />
                Task completions
              </label>
            </div>
          </div>
          <div className='space-y-3'>
            <h4 className='font-medium'>Project Notifications</h4>
            <div className='space-y-2 text-sm'>
              <label className='flex items-center gap-2'>
                <input type='checkbox' defaultChecked />
                Project updates
              </label>
              <label className='flex items-center gap-2'>
                <input type='checkbox' defaultChecked />
                Team activities
              </label>
              <label className='flex items-center gap-2'>
                <input type='checkbox' />
                Weekly summaries
              </label>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default Notifications;
