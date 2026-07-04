'use client';

import { useState } from 'react';
import { Badge, Button, Card, CardContent } from '@/shared/components/ui';
import { CheckCircle2, Circle, ImageIcon, MapPin } from 'lucide-react';
import { cn } from '@/shared/utils';
import type { StopAction, TransportPlanStopDetail } from '../../../types';
import { EvidenceViewDialog } from './EvidenceViewDialog';

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

type StopState = 'completed' | 'current-at' | 'current-travelling' | 'upcoming';

interface Props {
  stops: TransportPlanStopDetail[];
  /** plan.currentStop — null for COMPLETED routes */
  currentStopSeq?: number | null;
}

export function RouteManifestTimeline({ stops, currentStopSeq }: Props) {
  const [evidenceStop, setEvidenceStop] = useState<TransportPlanStopDetail | null>(null);

  const orderedStops = [...stops].sort((a, b) => a.sequence - b.sequence);

  // Stop driver arrived at but hasn't completed yet
  const atStop = stops.find((s) => s.actualArrivalTime != null && !s.isCompleted) ?? null;
  // First stop not yet arrived (next to navigate to)
  const nextUnvisited = orderedStops.find((s) => !s.actualArrivalTime) ?? null;

  function getState(stop: TransportPlanStopDetail): StopState {
    if (stop.isCompleted) return 'completed';
    if (atStop && stop.id === atStop.id) return 'current-at';
    if (!atStop && nextUnvisited && stop.id === nextUnvisited.id) return 'current-travelling';
    return 'upcoming';
  }

  return (
    <>
      <Card>
        <CardContent className="p-0">
          {orderedStops.map((stop, idx) => {
            const state = getState(stop);
            const isLast = idx === orderedStops.length - 1;

            return (
              <div key={stop.id} className="flex gap-4 px-4">
                {/* Timeline indicator column */}
                <div className="flex flex-col items-center">
                  <div
                    className={cn(
                      'mt-4 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2',
                      state === 'completed' &&
                        'border-green-500 bg-green-50',
                      (state === 'current-at' || state === 'current-travelling') &&
                        'border-orange-500 bg-orange-50 ring-2 ring-orange-200',
                      state === 'upcoming' && 'border-gray-300 bg-gray-50',
                    )}
                  >
                    {state === 'completed' ? (
                      <CheckCircle2 className="h-4 w-4 text-green-500" />
                    ) : state === 'current-at' || state === 'current-travelling' ? (
                      <div className="h-3 w-3 animate-pulse rounded-full bg-orange-500" />
                    ) : (
                      <Circle className="h-4 w-4 text-gray-400" />
                    )}
                  </div>
                  {!isLast && (
                    <div
                      className={cn(
                        'min-h-4 w-0.5 flex-1',
                        state === 'completed' ? 'bg-green-400' : 'bg-gray-200',
                      )}
                    />
                  )}
                </div>

                {/* Stop content */}
                <div
                  className={cn(
                    'flex-1 min-w-0 py-4',
                    !isLast && 'border-b border-border',
                  )}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1 min-w-0 space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span
                          className={cn(
                            'font-mono text-sm font-bold',
                            state === 'completed' && 'text-green-700',
                            (state === 'current-at' || state === 'current-travelling') &&
                              'text-orange-700',
                            state === 'upcoming' && 'text-foreground',
                          )}
                        >
                          {stop.locationCode}
                        </span>
                        <Badge
                          className={cn(
                            'border text-xs',
                            ACTION_COLORS[stop.action] ?? 'bg-gray-100 text-gray-700 border-gray-200',
                          )}
                        >
                          {ACTION_LABELS[stop.action] ?? stop.action}
                        </Badge>
                        {state === 'current-at' && (
                          <Badge className="border border-orange-200 bg-orange-100 text-xs text-orange-700">
                            At stop
                          </Badge>
                        )}
                        {state === 'current-travelling' && (
                          <Badge className="border border-blue-200 bg-blue-100 text-xs text-blue-700">
                            Travelling
                          </Badge>
                        )}
                      </div>

                      <div className="flex flex-wrap gap-4 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <MapPin className="h-3 w-3" />
                          Planned: {fmt(stop.plannedArrivalTime)}
                        </span>
                        {stop.actualArrivalTime && (
                          <span className="flex items-center gap-1 text-foreground">
                            Actual: {fmt(stop.actualArrivalTime)}
                          </span>
                        )}
                        {stop.requestId != null && <span>Request #{stop.requestId}</span>}
                        {stop.containerCode && (
                          <span className="font-mono">Container: {stop.containerCode}</span>
                        )}
                      </div>
                    </div>

                    {state === 'completed' && stop.evidenceUrl && (
                      <Button
                        variant="outline"
                        size="sm"
                        className="shrink-0 text-xs"
                        onClick={() => setEvidenceStop(stop)}
                      >
                        <ImageIcon className="mr-1 h-3 w-3" />
                        View Evidence
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </CardContent>
      </Card>

      <EvidenceViewDialog stop={evidenceStop} onClose={() => setEvidenceStop(null)} />
    </>
  );
}
