'use client';

import { Clock, MapPin, Navigation, Truck, User } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { TransportPlanDetail } from '../../../types';

function fmt(dt: string | null) {
  if (!dt) return '—';
  return dt.replace('T', ' ').slice(0, 16);
}

function fmtMinutes(min: number | null) {
  if (min == null) return '—';
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
}

interface RouteInfoCardProps {
  plan: TransportPlanDetail;
}

export function RouteInfoCard({ plan }: RouteInfoCardProps) {
  const isCompleted = plan.status === 'COMPLETED';

  const startDisplay = isCompleted ? fmt(plan.actualStartTime) : fmt(plan.startTime);
  const endDisplay = isCompleted ? fmt(plan.actualEndTime) : fmt(plan.endTime);

  return (
    <Card>
      <CardContent className="p-4">
        <div className="grid grid-cols-2 gap-x-6 gap-y-3 sm:grid-cols-4">
          <InfoItem icon={<Truck className="h-4 w-4" />} label="Truck" value={plan.truckCode} />
          <InfoItem icon={<User className="h-4 w-4" />} label="Driver" value={plan.driverName ?? '—'} />
          <InfoItem
            icon={<Clock className="h-4 w-4" />}
            label={isCompleted ? 'Actual Start' : 'Planned Start'}
            value={startDisplay}
          />
          <InfoItem
            icon={<Clock className="h-4 w-4" />}
            label={isCompleted ? 'Actual End' : 'Planned End'}
            value={endDisplay}
          />
          <InfoItem
            icon={<Clock className="h-4 w-4" />}
            label="Created At"
            value={fmt(plan.createdStamp)}
          />
          <InfoItem
            icon={<MapPin className="h-4 w-4" />}
            label="Total Stops"
            value={String(plan.stops.length)}
          />
          <InfoItem
            icon={<Navigation className="h-4 w-4" />}
            label="Total Distance"
            value={plan.totalDistance != null ? `${plan.totalDistance.toFixed(1)} km` : '—'}
            highlight={isCompleted}
          />
          <InfoItem
            icon={<Clock className="h-4 w-4" />}
            label={isCompleted ? 'Actual Time' : 'Total Time'}
            value={fmtMinutes(plan.totalTime)}
            highlight={isCompleted}
          />
        </div>
      </CardContent>
    </Card>
  );
}

function InfoItem({
  icon,
  label,
  value,
  highlight,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  highlight?: boolean;
}) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="flex items-center gap-1 text-xs text-muted-foreground">
        {icon}
        {label}
      </span>
      <span className={cn('text-sm font-medium', highlight && 'text-primary font-semibold')}>
        {value}
      </span>
    </div>
  );
}
