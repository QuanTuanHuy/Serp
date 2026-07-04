'use client';

import { CheckCircle2, Circle, MapPin } from 'lucide-react';
import { Badge, Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { StopAction, TransportPlanStopDetail } from '../../../types';

const ACTION_LABELS: Record<StopAction, string> = {
  DEPOT_START: 'Depot Start',
  DEPOT_END: 'Depot End',
  PICKUP_CONTAINER: 'Pick Up Container',
  DELIVERY_CONTAINER: 'Deliver Container',
  PICKUP_TRAILER: 'Pick Up Trailer',
  DROP_TRAILER: 'Drop Trailer',
};

const ACTION_COLORS: Record<StopAction, string> = {
  DEPOT_START: 'bg-gray-100 text-gray-700 border-gray-200',
  DEPOT_END: 'bg-gray-100 text-gray-700 border-gray-200',
  PICKUP_CONTAINER: 'bg-blue-100 text-blue-700 border-blue-200',
  DELIVERY_CONTAINER: 'bg-green-100 text-green-700 border-green-200',
  PICKUP_TRAILER: 'bg-purple-100 text-purple-700 border-purple-200',
  DROP_TRAILER: 'bg-orange-100 text-orange-700 border-orange-200',
};

function fmt(dt: string | null) {
  if (!dt) return '—';
  return dt.replace('T', ' ').slice(0, 16);
}

interface StopsListProps {
  stops: TransportPlanStopDetail[];
}

export function StopsList({ stops }: StopsListProps) {
  const ordered = [...stops].sort((a, b) => a.sequence - b.sequence);

  return (
    <Card>
      <CardContent className="p-0">
        <div className="divide-y divide-border">
          {ordered.map((stop, idx) => {
            const arrived = stop.actualArrivalTime != null;
            const completed = stop.isCompleted;

            return (
              <div key={stop.id} className={cn('flex items-start gap-3 p-3', completed && 'bg-muted/30')}>
                {/* Sequence number */}
                <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-bold">
                  {idx + 1}
                </div>

                {/* Main content */}
                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-mono text-sm font-semibold">{stop.locationCode}</span>
                    <Badge className={cn('border text-xs', ACTION_COLORS[stop.action])}>
                      {ACTION_LABELS[stop.action]}
                    </Badge>
                    {completed && (
                      <span className="flex items-center gap-1 text-xs text-green-600">
                        <CheckCircle2 className="h-3 w-3" /> Done
                      </span>
                    )}
                    {arrived && !completed && (
                      <span className="flex items-center gap-1 text-xs text-orange-600">
                        <Circle className="h-3 w-3" /> At stop
                      </span>
                    )}
                  </div>
                  <div className="mt-1 flex flex-wrap gap-4 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <MapPin className="h-3 w-3" /> Planned: {fmt(stop.plannedArrivalTime)}
                    </span>
                    {arrived && (
                      <span className="flex items-center gap-1 text-foreground">
                        Actual: {fmt(stop.actualArrivalTime)}
                      </span>
                    )}
                    {stop.requestId != null && (
                      <span>Request #{stop.requestId}</span>
                    )}
                    {stop.containerCode && (
                      <span className="font-mono">Container: {stop.containerCode}</span>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}
