'use client';

import { useRouter } from 'next/navigation';
import { ArrowRight, MapPin, Navigation, Truck } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { useGetMyTransportPlanDetailQuery } from '../../../api/ttcrsApi';
import type { TransportPlanListItem, TransportPlanStopDetail } from '../../../types';

interface ExecutingRouteBannerProps {
  plan: TransportPlanListItem;
}

function ordered(stops: TransportPlanStopDetail[]) {
  return [...stops].sort((a, b) => a.sequence - b.sequence);
}

/** Stop arrived at but evidence not yet submitted — driver is physically AT this location. */
function atStop(stops: TransportPlanStopDetail[]): TransportPlanStopDetail | null {
  return stops.find((s) => s.actualArrivalTime != null && !s.isCompleted) ?? null;
}

/** Next unvisited stop — driver is travelling toward this location. */
function travellingTo(stops: TransportPlanStopDetail[]): TransportPlanStopDetail | null {
  return ordered(stops).find((s) => !s.actualArrivalTime) ?? null;
}

export function ExecutingRouteBanner({ plan }: ExecutingRouteBannerProps) {
  const router = useRouter();
  const { data } = useGetMyTransportPlanDetailQuery(plan.id);
  const detail = data?.data ?? null;

  const currentAtStop = detail ? atStop(detail.stops) : null;
  const nextStop      = detail ? travellingTo(detail.stops) : null;

  // What to show in the stop line
  const stopLabel = currentAtStop
    ? { prefix: 'At stop', locationCode: currentAtStop.locationCode, action: currentAtStop.action }
    : nextStop
      ? { prefix: 'Travelling to', locationCode: nextStop.locationCode, action: nextStop.action }
      : null;

  return (
    <div
      className="relative overflow-hidden rounded-xl border-2 border-orange-400 bg-gradient-to-r from-orange-50 to-amber-50 dark:from-orange-950/40 dark:to-amber-950/40 p-4 cursor-pointer"
      onClick={() => router.push(`/ttcrs/driver/routes/${plan.id}`)}
    >
      {/* Animated left accent bar */}
      <div className="absolute left-0 top-0 h-full w-1 bg-orange-500" />

      <div className="flex items-center gap-4 pl-2">
        {/* Icon */}
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-orange-500 text-white shadow-md">
          <Navigation className="h-6 w-6" />
        </div>

        {/* Info */}
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2 flex-wrap">
            {/* LIVE pulse badge */}
            <span className="flex items-center gap-1.5 rounded-full bg-orange-500 px-2.5 py-0.5 text-xs font-bold text-white">
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-white opacity-75" />
                <span className="relative inline-flex h-2 w-2 rounded-full bg-white" />
              </span>
              LIVE
            </span>
            <span className="text-sm font-semibold text-orange-900 dark:text-orange-100">
              Route #{plan.id} is in progress
            </span>
          </div>

          {/* Truck · stops · start time */}
          <div className="mt-1 flex items-center gap-3 text-sm text-orange-700 dark:text-orange-300">
            <span className="flex items-center gap-1">
              <Truck className="h-3.5 w-3.5" />
              {plan.truckCode}
            </span>
            <span className="text-orange-400">·</span>
            <span>{plan.stopCount} stops</span>
            {plan.startTime && (
              <>
                <span className="text-orange-400">·</span>
                <span>Started {plan.startTime.replace('T', ' ').slice(0, 16)}</span>
              </>
            )}
          </div>

          {/* Current stop line */}
          {stopLabel && (
            <div className="mt-1.5 flex items-center gap-1.5 text-sm">
              <MapPin className="h-3.5 w-3.5 shrink-0 text-orange-500" />
              <span className="text-orange-600 dark:text-orange-400">{stopLabel.prefix}</span>
              <span className="font-semibold font-mono text-orange-900 dark:text-orange-100">
                {stopLabel.locationCode}
              </span>
              <span className="text-xs text-orange-500">
                ({stopLabel.action.replace(/_/g, ' ')})
              </span>
            </div>
          )}
        </div>

        {/* CTA */}
        <Button
          className="shrink-0 bg-orange-500 hover:bg-orange-600 text-white shadow-md"
          size="sm"
          onClick={(e) => {
            e.stopPropagation();
            router.push(`/ttcrs/driver/routes/${plan.id}`);
          }}
        >
          Continue
          <ArrowRight className="ml-1.5 h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
