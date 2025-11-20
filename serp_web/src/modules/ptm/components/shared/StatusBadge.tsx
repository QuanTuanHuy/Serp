/**
 * PTM v2 - Status Badge Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Task status badge
 */

import { cn } from '@/shared/utils';
import { PTM_COLORS } from '../../constants/colors';
import type { TaskStatus } from '../../types';

interface StatusBadgeProps {
  status: TaskStatus;
  className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const statusConfig: Record<
    TaskStatus,
    {
      label: string;
      bg: string;
      text: string;
      border: string;
      dot: string;
    }
  > = {
    TODO: {
      label: 'To Do',
      ...PTM_COLORS.status.todo,
    },
    IN_PROGRESS: {
      label: 'In Progress',
      ...PTM_COLORS.status.in_progress,
    },
    DONE: {
      label: 'Done',
      ...PTM_COLORS.status.done,
    },
    CANCELLED: {
      label: 'Cancelled',
      ...PTM_COLORS.status.cancelled,
    },
  };

  const config = statusConfig[status];

  if (!config) {
    return null;
  }

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
      <div className={cn('w-1.5 h-1.5 rounded-full', config.dot)} />
      {config.label}
    </div>
  );
}
