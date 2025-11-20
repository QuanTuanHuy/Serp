/**
 * PTM v2 - Recurring Task Badge Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Display recurring task pattern
 */

'use client';

import { Repeat } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import type { RepeatConfig } from '../../types';

interface RecurringBadgeProps {
  repeatConfig: RepeatConfig;
  className?: string;
}

const DAYS_SHORT = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export function RecurringBadge({
  repeatConfig,
  className,
}: RecurringBadgeProps) {
  const getRecurrenceText = (): string => {
    const { frequency, interval, daysOfWeek } = repeatConfig;

    if (frequency === 'daily') {
      return interval === 1 ? 'Daily' : `Every ${interval}d`;
    }

    if (frequency === 'weekly') {
      if (daysOfWeek && daysOfWeek.length > 0 && daysOfWeek.length < 7) {
        const dayNames = daysOfWeek.map((d) => DAYS_SHORT[d]).join(', ');
        return interval === 1 ? dayNames : `${dayNames} (${interval}w)`;
      }
      return interval === 1 ? 'Weekly' : `Every ${interval}w`;
    }

    if (frequency === 'monthly') {
      return interval === 1 ? 'Monthly' : `Every ${interval}m`;
    }

    return 'Recurring';
  };

  return (
    <Badge
      variant='outline'
      className={`text-xs bg-blue-50 dark:bg-blue-950/30 border-blue-200 dark:border-blue-800 text-blue-700 dark:text-blue-400 ${className}`}
    >
      <Repeat className='h-3 w-3 mr-1' />
      {getRecurrenceText()}
    </Badge>
  );
}
