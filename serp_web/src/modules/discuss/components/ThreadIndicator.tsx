/*
Author: QuanTuanHuy
Description: Part of Serp Project - Thread Replies Component
*/

'use client';

import React from 'react';
import { MessageSquare, ChevronRight } from 'lucide-react';
import { cn } from '@/shared/utils';
import { Button } from '@/shared/components/ui/button';

interface ThreadIndicatorProps {
  threadCount: number;
  onClick?: () => void;
  className?: string;
}

export const ThreadIndicator: React.FC<ThreadIndicatorProps> = ({
  threadCount,
  onClick,
  className,
}) => {
  if (threadCount === 0) return null;

  return (
    <Button
      variant='ghost'
      size='sm'
      onClick={onClick}
      className={cn(
        'h-7 px-2 gap-1.5 text-xs font-semibold',
        'text-violet-600 dark:text-violet-400',
        'hover:bg-violet-100 dark:hover:bg-violet-900/30',
        'transition-all duration-200',
        'border border-violet-200 dark:border-violet-800',
        'rounded-full',
        className
      )}
    >
      <MessageSquare className='h-3.5 w-3.5' />
      <span>
        {threadCount} {threadCount === 1 ? 'reply' : 'replies'}
      </span>
      <ChevronRight className='h-3 w-3' />
    </Button>
  );
};
