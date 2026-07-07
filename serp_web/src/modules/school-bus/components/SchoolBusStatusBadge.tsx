'use client';

import { Badge } from '@/shared/components/ui';
import { getLabel, statusLabel } from '../schoolBusLabels';

interface SchoolBusStatusBadgeProps {
  status?: string | null;
  labelMap?: Record<string, string>;
}

export function SchoolBusStatusBadge({
  status,
  labelMap = statusLabel,
}: SchoolBusStatusBadgeProps) {
  const normalizedStatus = status?.toUpperCase() || 'UNKNOWN';
  const displayLabel = getLabel(labelMap, normalizedStatus);

  if (
    [
      'APPROVED',
      'ASSIGNED',
      'COMPLETED',
      'READY',
      'ACTIVE',
      'PUBLISHED',
      'GENERATED',
      'BOARDED',
      'DROPPED_OFF',
    ].includes(normalizedStatus)
  ) {
    return (
      <Badge className='rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-300 dark:hover:bg-emerald-950/60'>
        {displayLabel}
      </Badge>
    );
  }

  if (
    ['PENDING', 'SUBMITTED', 'PAUSED', 'WARNING'].includes(
      normalizedStatus
    )
  ) {
    return (
      <Badge className='rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-amber-700 hover:bg-amber-50 dark:border-amber-800 dark:bg-amber-950/50 dark:text-amber-300 dark:hover:bg-amber-950/60'>
        {displayLabel}
      </Badge>
    );
  }

  if (
    [
      'IN_PROGRESS',
      'RUNNING',
      'ON_BOARD',
      'CHECKED_IN',
      'BOARDING',
      'DEPARTED',
      'ARRIVED',
      'PLANNED',
    ].includes(normalizedStatus)
  ) {
    return (
      <Badge className='rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-blue-700 hover:bg-blue-50 dark:border-blue-800 dark:bg-blue-950/50 dark:text-blue-300 dark:hover:bg-blue-950/60'>
        {displayLabel}
      </Badge>
    );
  }

  if (
    [
      'REJECTED',
      'CANCELLED',
      'ABSENT',
      'INACTIVE',
      'OFFLINE',
      'STOPPED',
      'EXPIRED',
      'NO_SHOW',
      'NOT_SERVED',
      'SKIPPED',
    ].includes(normalizedStatus)
  ) {
    return (
      <Badge className='rounded-full border border-red-200 bg-red-50 px-3 py-1 text-red-700 hover:bg-red-50 dark:border-red-800 dark:bg-red-950/50 dark:text-red-300 dark:hover:bg-red-950/60'>
        {displayLabel}
      </Badge>
    );
  }

  return (
    <Badge
      variant='outline'
      className='rounded-full border-border bg-muted px-3 py-1 text-muted-foreground'
    >
      {displayLabel}
    </Badge>
  );
}
