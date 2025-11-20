/**
 * PTM v2 - Focus Time Badge Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Visual indicator for focus time slots on schedule
 */

import { Flame } from 'lucide-react';
import { Badge } from '@/shared/components/ui/badge';
import { cn } from '@/shared/utils';
import type { AvailabilitySlotType } from '../../types';

interface FocusTimeBadgeProps {
  slotType: AvailabilitySlotType;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

const SLOT_CONFIG: Record<
  AvailabilitySlotType,
  {
    label: string;
    icon?: any;
    className: string;
  }
> = {
  focus: {
    label: 'Focus',
    icon: Flame,
    className:
      'bg-purple-100 text-purple-700 border-purple-200 dark:bg-purple-950 dark:text-purple-300 dark:border-purple-800',
  },
  regular: {
    label: 'Regular',
    className:
      'bg-blue-100 text-blue-700 border-blue-200 dark:bg-blue-950 dark:text-blue-300 dark:border-blue-800',
  },
  flexible: {
    label: 'Flexible',
    className:
      'bg-green-100 text-green-700 border-green-200 dark:bg-green-950 dark:text-green-300 dark:border-green-800',
  },
};

export function FocusTimeBadge({
  slotType,
  className,
  size = 'sm',
}: FocusTimeBadgeProps) {
  const config = SLOT_CONFIG[slotType];
  const Icon = config.icon;

  // Only show badge for focus type (most important)
  if (slotType !== 'focus') {
    return null;
  }

  return (
    <Badge
      variant='outline'
      className={cn(
        'inline-flex items-center gap-1 font-medium',
        config.className,
        {
          'text-xs px-1.5 py-0.5': size === 'sm',
          'text-sm px-2 py-1': size === 'md',
          'text-base px-2.5 py-1.5': size === 'lg',
        },
        className
      )}
    >
      {Icon && <Icon className='h-3 w-3' />}
      {config.label}
    </Badge>
  );
}

// Visual time block indicator for calendar view
interface TimeBlockIndicatorProps {
  slotType: AvailabilitySlotType;
  className?: string;
}

export function TimeBlockIndicator({
  slotType,
  className,
}: TimeBlockIndicatorProps) {
  const config = SLOT_CONFIG[slotType];

  return (
    <div
      className={cn(
        'absolute left-0 top-0 bottom-0 w-1 rounded-l',
        {
          'bg-purple-500': slotType === 'focus',
          'bg-blue-400': slotType === 'regular',
          'bg-green-400': slotType === 'flexible',
        },
        className
      )}
      title={`${config.label} time slot`}
    />
  );
}
