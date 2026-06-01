'use client';

import { Badge } from '@/shared/components/ui';

interface SchoolBusStatusBadgeProps {
  status?: string | null;
}

export function SchoolBusStatusBadge({
  status,
}: SchoolBusStatusBadgeProps) {
  const normalizedStatus = status?.toUpperCase() || 'UNKNOWN';

  if (
    ['APPROVED', 'ASSIGNED', 'COMPLETED', 'READY', 'ACTIVE', 'PUBLISHED', 'GENERATED', 'BOARDED', 'DROPPED_OFF']
      .includes(normalizedStatus)
  ) {
    return (
      <Badge className='rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-emerald-700 hover:bg-emerald-50'>
        {normalizedStatus}
      </Badge>
    );
  }

  if (
    ['PENDING', 'SUBMITTED', 'PAUSED', 'WAITING', 'WARNING'].includes(
      normalizedStatus
    )
  ) {
    return (
      <Badge className='rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-amber-700 hover:bg-amber-50'>
        {normalizedStatus}
      </Badge>
    );
  }

  if (
    ['IN_PROGRESS', 'RUNNING', 'ON_BOARD', 'CHECKED_IN', 'BOARDING', 'DEPARTED', 'ARRIVED', 'SCHEDULED', 'PLANNED'].includes(
      normalizedStatus
    )
  ) {
    return (
      <Badge className='rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-blue-700 hover:bg-blue-50'>
        {normalizedStatus}
      </Badge>
    );
  }

  if (
    ['REJECTED', 'CANCELLED', 'ABSENT', 'INACTIVE', 'OFFLINE', 'STOPPED', 'EXPIRED', 'NO_SHOW', 'NOT_SERVED', 'SKIPPED'].includes(
      normalizedStatus
    )
  ) {
    return (
      <Badge className='rounded-full border border-red-200 bg-red-50 px-3 py-1 text-red-700 hover:bg-red-50'>
        {normalizedStatus}
      </Badge>
    );
  }

  return (
    <Badge
      variant='outline'
      className='rounded-full border-slate-200 bg-slate-50 px-3 py-1 text-slate-600'
    >
      {normalizedStatus}
    </Badge>
  );
}
