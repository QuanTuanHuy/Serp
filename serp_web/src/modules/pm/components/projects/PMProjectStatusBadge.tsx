/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project status badge
 */

import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { PMProjectStatus } from '../../types/project-list.types';

interface PMProjectStatusBadgeProps {
  status: PMProjectStatus;
}

const STATUS_STYLES: Record<PMProjectStatus, string> = {
  ACTIVE:
    'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-900 dark:bg-emerald-950/70 dark:text-emerald-300',
  COMPLETED:
    'border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-900 dark:bg-sky-950/70 dark:text-sky-300',
  ARCHIVED:
    'border-zinc-200 bg-zinc-100 text-zinc-700 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-300',
};

export function PMProjectStatusBadge({ status }: PMProjectStatusBadgeProps) {
  return (
    <Badge
      variant='outline'
      className={cn(
        'rounded-md px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide',
        STATUS_STYLES[status]
      )}
    >
      {status}
    </Badge>
  );
}
