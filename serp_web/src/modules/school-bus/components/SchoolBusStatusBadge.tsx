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
    ['APPROVED', 'ASSIGNED', 'COMPLETED', 'ON_BOARD', 'CHECKED_IN', 'ACTIVE', 'PUBLISHED', 'GENERATED', 'REVIEWING', 'BOARDED', 'DROPPED_OFF']
      .includes(normalizedStatus)
  ) {
    return (
      <Badge className='rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-emerald-700 hover:bg-emerald-50'>
        {normalizedStatus}
      </Badge>
    );
  }

  if (
    ['PENDING', 'DRAFT', 'SUBMITTED', 'PLANNED', 'IN_PROGRESS', 'WAITING', 'SCHEDULED', 'PAUSED'].includes(
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
    ['REJECTED', 'CANCELLED', 'ABSENT', 'INACTIVE', 'OFFLINE', 'STOPPED', 'EXPIRED', 'NO_SHOW', 'NOT_SERVED'].includes(
      normalizedStatus
    )
  ) {
    return (
      <Badge className='rounded-full border border-rose-200 bg-rose-50 px-3 py-1 text-rose-700 hover:bg-rose-50'>
        {normalizedStatus}
      </Badge>
    );
  }

  return (
    <Badge
      variant='outline'
      className='rounded-full border-slate-200 px-3 py-1 text-slate-600'
    >
      {normalizedStatus}
    </Badge>
  );
}
