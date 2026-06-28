/*
Author: QuanTuanHuy
Description: Part of Serp Project - User Profile Card and Status Switcher Popover
*/

'use client';

import React, { useState } from 'react';
import { useAuth } from '@/modules/account';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
  Button,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui';
import { cn, getAvatarColor } from '@/shared/utils';
import {
  useGetMyPresenceQuery,
  useUpdateMyPresenceMutation,
} from '../api/discussApi';
import { OnlineStatusIndicator } from './OnlineStatusIndicator';
import { Loader2 } from 'lucide-react';
import type { UserStatus } from '../api/presence.api';

export const UserProfileCard: React.FC = () => {
  const { user } = useAuth();
  const { data: myPresenceData, isLoading } = useGetMyPresenceQuery();
  const [updateMyPresence, { isLoading: isUpdating }] =
    useUpdateMyPresenceMutation();

  const [statusMessage, setStatusMessage] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  if (isLoading || !user) {
    return (
      <div className='flex items-center gap-3 px-3 py-2 animate-pulse'>
        <div className='h-9 w-9 rounded-full bg-slate-200 dark:bg-slate-700' />
        <div className='flex-1 space-y-2 py-1'>
          <div className='h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4' />
          <div className='h-3 bg-slate-200 dark:bg-slate-700 rounded w-1/2' />
        </div>
      </div>
    );
  }

  const currentPresence = myPresenceData?.data;
  const currentStatus = currentPresence?.status || 'OFFLINE';
  const currentMessage = currentPresence?.statusMessage || '';

  const mapStatusToOnlineStatus = (status: UserStatus) => {
    switch (status) {
      case 'ONLINE':
        return 'online';
      case 'BUSY':
        return 'busy';
      default:
        return 'offline';
    }
  };

  const handleOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (open) {
      setStatusMessage(currentMessage);
    }
  };

  const handleStatusSelect = async (status: UserStatus) => {
    try {
      await updateMyPresence({ status, statusMessage: currentMessage }).unwrap();
    } catch (err) {
      console.error('Failed to update status:', err);
    }
  };

  const handleSaveMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateMyPresence({
        status: currentStatus,
        statusMessage: statusMessage.trim() || undefined,
      }).unwrap();
      setIsOpen(false);
    } catch (err) {
      console.error('Failed to update status message:', err);
    }
  };

  const handleClearMessage = async () => {
    try {
      await updateMyPresence({
        status: currentStatus,
        statusMessage: undefined,
      }).unwrap();
      setStatusMessage('');
      setIsOpen(false);
    } catch (err) {
      console.error('Failed to clear status message:', err);
    }
  };

  const initials = user.fullName
    ? user.fullName
        .split(' ')
        .map((w) => w[0])
        .join('')
        .slice(0, 2)
        .toUpperCase()
    : 'U';

  return (
    <Popover open={isOpen} onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <button
          className={cn(
            'w-full flex items-center gap-3 px-3 py-2 rounded-lg text-left transition-all duration-200',
            'hover:bg-slate-100 dark:hover:bg-slate-800/60',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-500'
          )}
        >
          <div className='relative flex-shrink-0'>
            <Avatar className='h-9 w-9 border border-slate-200 dark:border-slate-700'>
              {user.avatarUrl && (
                <AvatarImage src={user.avatarUrl} alt={user.fullName} />
              )}
              <AvatarFallback
                className={cn(
                  'text-xs font-semibold text-white bg-gradient-to-br',
                  getAvatarColor(user.fullName || '')
                )}
              >
                {initials}
              </AvatarFallback>
            </Avatar>
            <div className='absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 bg-white dark:bg-slate-900 rounded-full flex items-center justify-center border border-slate-200 dark:border-slate-800'>
              <OnlineStatusIndicator
                status={mapStatusToOnlineStatus(currentStatus)}
                size='sm'
                showPulse={false}
              />
            </div>
          </div>
          <div className='flex-1 min-w-0'>
            <p className='text-sm font-semibold text-slate-900 dark:text-slate-100 truncate'>
              {user.fullName}
            </p>
            {currentMessage ? (
              <p className='text-xs text-slate-500 dark:text-slate-400 truncate italic'>
                {currentMessage}
              </p>
            ) : (
              <p className='text-xs text-slate-400 dark:text-slate-500 truncate'>
                Set status...
              </p>
            )}
          </div>
        </button>
      </PopoverTrigger>
      <PopoverContent className='w-72 p-3' align='start' side='top' sideOffset={8}>
        <div className='space-y-3'>
          <div className='flex items-center gap-2 pb-2 border-b border-slate-100 dark:border-slate-800'>
            <span className='text-xs font-semibold text-slate-500 uppercase tracking-wide'>
              Set presence status
            </span>
          </div>

          {/* Quick status selector */}
          <div className='grid grid-cols-3 gap-1.5'>
            {(['ONLINE', 'BUSY', 'OFFLINE'] as UserStatus[]).map((statusOption) => {
              const mapped = mapStatusToOnlineStatus(statusOption);
              const isActive = currentStatus === statusOption;
              const label =
                statusOption === 'ONLINE'
                  ? 'Online'
                  : statusOption === 'BUSY'
                    ? 'DND'
                    : 'Offline';

              return (
                <Button
                  key={statusOption}
                  variant={isActive ? 'default' : 'outline'}
                  size='sm'
                  onClick={() => handleStatusSelect(statusOption)}
                  disabled={isUpdating}
                  className={cn(
                    'h-8 gap-1.5 text-xs',
                    isActive &&
                      statusOption === 'ONLINE' &&
                      'bg-emerald-600 hover:bg-emerald-700',
                    isActive &&
                      statusOption === 'BUSY' &&
                      'bg-rose-600 hover:bg-rose-700'
                  )}
                >
                  <OnlineStatusIndicator status={mapped} size='sm' showPulse={false} />
                  {label}
                </Button>
              );
            })}
          </div>

          {/* Status Message Form */}
          <form onSubmit={handleSaveMessage} className='space-y-2 pt-2 border-t border-slate-100 dark:border-slate-800'>
            <div className='space-y-1.5'>
              <label className='text-xs font-medium text-slate-500'>
                Status message
              </label>
              <Input
                placeholder='What is on your mind?'
                value={statusMessage}
                onChange={(e) => setStatusMessage(e.target.value)}
                maxLength={255}
                disabled={isUpdating}
                className='h-8 text-xs focus-visible:ring-violet-500'
              />
            </div>
            <div className='flex items-center justify-between gap-2 pt-1'>
              {currentMessage ? (
                <Button
                  type='button'
                  variant='ghost'
                  size='sm'
                  onClick={handleClearMessage}
                  disabled={isUpdating}
                  className='h-7 text-xs px-2 text-rose-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/20'
                >
                  Clear Status
                </Button>
              ) : (
                <div />
              )}
              <Button
                type='submit'
                size='sm'
                disabled={isUpdating}
                className='h-7 text-xs px-3 bg-violet-600 hover:bg-violet-700'
              >
                {isUpdating && <Loader2 className='h-3 w-3 animate-spin mr-1' />}
                Save
              </Button>
            </div>
          </form>
        </div>
      </PopoverContent>
    </Popover>
  );
};
