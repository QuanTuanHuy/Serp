/**
 * PTM v2 - Priority Badge Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task priority badge
 */

import { ArrowUp, ArrowRight, ArrowDown } from 'lucide-react';
import { cn } from '@/shared/utils';
import { PTM_COLORS } from '../../constants/colors';
import type { TaskPriority } from '../../types';

interface PriorityBadgeProps {
  priority: TaskPriority;
  showLabel?: boolean;
  className?: string;
}

export function PriorityBadge({
  priority,
  showLabel = true,
  className,
}: PriorityBadgeProps) {
  const priorityConfig = {
    LOW: {
      label: 'Low',
      icon: ArrowDown,
      ...PTM_COLORS.priority.low,
    },
    MEDIUM: {
      label: 'Medium',
      icon: ArrowRight,
      ...PTM_COLORS.priority.medium,
    },
    HIGH: {
      label: 'High',
      icon: ArrowUp,
      ...PTM_COLORS.priority.high,
    },
  };

  const config = priorityConfig[priority];
  const Icon = config.icon;

  return (
    <div
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium',
        config.bg,
        config.text,
        config.border,
        'border',
        className
      )}
    >
      <Icon className='h-3 w-3' />
      {showLabel && config.label}
    </div>
  );
}
