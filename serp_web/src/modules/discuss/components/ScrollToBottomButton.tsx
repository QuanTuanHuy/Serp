/*
Author: QuanTuanHuy
Description: Part of Serp Project - Scroll to bottom floating button with unread count
*/

'use client';

import React from 'react';
import { ArrowDown } from 'lucide-react';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';

interface ScrollToBottomButtonProps {
  visible: boolean;
  unreadCount?: number;
  onClick: () => void;
  className?: string;
}

export const ScrollToBottomButton: React.FC<ScrollToBottomButtonProps> = ({
  visible,
  unreadCount = 0,
  onClick,
  className,
}) => {
  if (!visible) return null;

  return (
    <div
      className={cn(
        'absolute bottom-24 right-6 z-10',
        'transition-all duration-200 ease-out',
        visible
          ? 'opacity-100 translate-y-0'
          : 'opacity-0 translate-y-2 pointer-events-none',
        className
      )}
    >
      <div className='relative'>
        <Button
          size='icon'
          variant='secondary'
          onClick={onClick}
          className={cn(
            'h-12 w-12 rounded-full shadow-lg hover:shadow-xl',
            'transition-all duration-200',
            'bg-white dark:bg-slate-800',
            'border-2 border-slate-200 dark:border-slate-700',
            'hover:border-violet-300 dark:hover:border-violet-600',
            'group'
          )}
          aria-label={
            unreadCount > 0
              ? `Scroll to bottom (${unreadCount} new message${unreadCount > 1 ? 's' : ''})`
              : 'Scroll to bottom'
          }
        >
          <ArrowDown className='h-5 w-5 text-slate-600 dark:text-slate-300 group-hover:text-violet-600 dark:group-hover:text-violet-400 transition-colors' />

          {unreadCount > 0 && (
            <Badge
              variant='destructive'
              className={cn(
                'absolute -top-1 -right-1',
                'h-5 min-w-5 px-1',
                'flex items-center justify-center',
                'text-xs font-bold',
                'animate-in zoom-in-50 duration-200'
              )}
            >
              {unreadCount > 99 ? '99+' : unreadCount}
            </Badge>
          )}
        </Button>

        {unreadCount > 0 && (
          <div
            className={cn(
              'absolute bottom-full mb-2 right-0',
              'whitespace-nowrap',
              'bg-slate-900 dark:bg-slate-700 text-white',
              'text-xs py-1.5 px-2.5 rounded-md shadow-lg',
              'pointer-events-none',
              'animate-in fade-in-50 slide-in-from-bottom-1 duration-200'
            )}
          >
            {unreadCount} new message{unreadCount > 1 ? 's' : ''}
            <div className='absolute top-full right-4 -mt-px'>
              <div className='border-4 border-transparent border-t-slate-900 dark:border-t-slate-700' />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
