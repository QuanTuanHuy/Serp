'use client';

import { useEffect, useState } from 'react';
import { AlertCircle, CheckCircle2, Clock, Navigation, TrendingUp } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { TransportPlanDetail, TransportPlanStopDetail } from '../../../types';

async function fetchOsrmRoute(
  waypoints: [number, number][],
): Promise<{ distanceKm: number; durationSec: number } | null> {
  if (waypoints.length < 2) return null;
  const coords = waypoints.map(([lat, lng]) => `${lng},${lat}`).join(';');
  const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=false`;
  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(8000) });
    if (!res.ok) return null;
    const data = await res.json();
    if (data.code !== 'Ok' || !data.routes?.[0]) return null;
    const r = data.routes[0];
    return { distanceKm: r.distance / 1000, durationSec: r.duration };
  } catch {
    return null;
  }
}

function ordered(stops: TransportPlanStopDetail[]) {
  return [...stops].sort((a, b) => a.sequence - b.sequence);
}

function fmtTime(d: Date) {
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function fmtDuration(sec: number) {
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
}

interface Props {
  plan: TransportPlanDetail;
}

export function RouteProgressOverview({ plan }: Props) {
  const orderedStops = ordered(plan.stops);
  const completedStops = plan.stops.filter((s) => s.isCompleted).length;
  const totalStops = plan.stops.length;
  const percentage = totalStops > 0 ? Math.round((completedStops / totalStops) * 100) : 0;

  // Driver is AT a stop: arrived but not yet completed
  const atStop = plan.stops.find((s) => s.actualArrivalTime != null && !s.isCompleted) ?? null;
  // Next stop to navigate to
  const nextStop = orderedStops.find((s) => !s.actualArrivalTime) ?? null;
  // Current position: last stop driver arrived at (for OSRM from-point)
  const currentStopObj =
    orderedStops.find((s) => s.sequence === plan.currentStop) ?? orderedStops[0] ?? null;

  const [etaInfo, setEtaInfo] = useState<{ distanceKm: number; durationSec: number } | null>(
    null,
  );
  const [remainingInfo, setRemainingInfo] = useState<{
    distanceKm: number;
    durationSec: number;
  } | null>(null);

  // ETA for current travelling leg (only when not at a stop)
  useEffect(() => {
    if (atStop || !nextStop || !currentStopObj) {
      setEtaInfo(null);
      return;
    }
    if (!currentStopObj.lat || !currentStopObj.lng || !nextStop.lat || !nextStop.lng) {
      setEtaInfo(null);
      return;
    }
    fetchOsrmRoute([
      [currentStopObj.lat, currentStopObj.lng],
      [nextStop.lat, nextStop.lng],
    ]).then(setEtaInfo);
  }, [atStop?.id, nextStop?.id, currentStopObj?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  // Remaining route distance & time.
  // - AT stop Xn      → OSRM(Xn → Xn+1 → … → end)   driver is at Xn, start from there
  // - Travelling Xn-1→Xn → OSRM(Xn-1 → Xn → … → end)  prepend last completed as origin
  useEffect(() => {
    const unvisited = orderedStops.filter(
      (s) => !s.isCompleted && s.lat != null && s.lng != null,
    );

    let waypoints: TransportPlanStopDetail[];

    if (atStop) {
      // Driver is physically at atStop — start remaining from atStop
      waypoints = [atStop, ...unvisited.filter((s) => s.id !== atStop.id)];
    } else {
      // Driver is travelling — prepend last completed stop as approximate origin
      const lastCompleted = [...orderedStops]
        .reverse()
        .find((s) => s.isCompleted && s.lat != null && s.lng != null) ?? null;
      waypoints = lastCompleted ? [lastCompleted, ...unvisited] : unvisited;
    }

    if (waypoints.length < 2) {
      setRemainingInfo(null);
      return;
    }
    fetchOsrmRoute(waypoints.map((s) => [s.lat!, s.lng!])).then(setRemainingInfo);
  }, [completedStops, atStop?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const computedEta = etaInfo ? new Date(Date.now() + etaInfo.durationSec * 1000) : null;
  const isLate =
    computedEta && nextStop?.plannedArrivalTime
      ? computedEta > new Date(nextStop.plannedArrivalTime)
      : false;

  return (
    <div className="grid grid-cols-3 gap-4">
      {/* Overall Progress */}
      <Card>
        <CardContent className="p-4 space-y-3">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-blue-500" />
            <span className="text-sm font-semibold">Overall Progress</span>
          </div>
          <div className="space-y-1.5">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">
                {completedStops}/{totalStops} stops
              </span>
              <span className="font-bold text-blue-600">{percentage}%</span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-muted">
              <div
                className="h-full rounded-full bg-blue-500 transition-all duration-500"
                style={{ width: `${percentage}%` }}
              />
            </div>
          </div>
          <div className="text-xs text-muted-foreground">
            {atStop ? (
              <span className={cn('flex items-center gap-1 text-orange-600')}>
                <CheckCircle2 className="h-3 w-3" />
                At stop:{' '}
                <span className="font-mono font-medium">{atStop.locationCode}</span>
              </span>
            ) : nextStop ? (
              <span className="flex items-center gap-1 text-blue-600">
                <Navigation className="h-3 w-3" />
                Travelling to:{' '}
                <span className="font-mono font-medium">{nextStop.locationCode}</span>
              </span>
            ) : (
              <span className="text-green-600">All stops visited</span>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Current Stop ETA */}
      <Card>
        <CardContent className="p-4 space-y-3">
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-orange-500" />
            <span className="text-sm font-semibold">Current Stop ETA</span>
          </div>
          {atStop ? (
            <div className="space-y-0.5">
              <p className="text-xs text-muted-foreground">Now at stop</p>
              <p className="text-lg font-bold font-mono">{atStop.locationCode}</p>
              <p className="text-xs text-muted-foreground">
                {atStop.action.replace(/_/g, ' ')}
              </p>
              {atStop.containerCode && (
                <p className="text-xs font-mono font-medium">
                  Container: {atStop.containerCode}
                </p>
              )}
            </div>
          ) : nextStop ? (
            <div className="space-y-1">
              <p className="text-xs text-muted-foreground">Next stop</p>
              <p className="font-mono font-bold">{nextStop.locationCode}</p>
              {nextStop.containerCode && (
                <p className="text-xs font-mono font-medium">
                  Container: {nextStop.containerCode}
                </p>
              )}
              <div className="flex items-center gap-2">
                <span className="text-lg font-bold">
                  {computedEta ? fmtTime(computedEta) : '—'}
                </span>
                {isLate ? (
                  <span className="flex items-center gap-1 text-xs font-medium text-red-600">
                    <AlertCircle className="h-3 w-3" /> Late
                  </span>
                ) : computedEta ? (
                  <span className="text-xs font-medium text-green-600">On time</span>
                ) : null}
              </div>
              {nextStop.plannedArrivalTime && (
                <p className="text-xs text-muted-foreground">
                  Planned: {nextStop.plannedArrivalTime.replace('T', ' ').slice(11, 16)}
                </p>
              )}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">Route complete</p>
          )}
        </CardContent>
      </Card>

      {/* Remaining */}
      <Card>
        <CardContent className="p-4 space-y-3">
          <div className="flex items-center gap-2">
            <Navigation className="h-4 w-4 text-green-500" />
            <span className="text-sm font-semibold">Remaining</span>
          </div>
          <div className="space-y-2">
            <div>
              <p className="text-xs text-muted-foreground">Distance</p>
              <p className="text-lg font-bold">
                {remainingInfo ? `${remainingInfo.distanceKm.toFixed(1)} km` : '—'}
              </p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Drive time</p>
              <p className="text-sm font-semibold">
                {remainingInfo ? fmtDuration(remainingInfo.durationSec) : '—'}
              </p>
            </div>
            <p className="text-xs text-muted-foreground">
              {totalStops - completedStops} stop
              {totalStops - completedStops !== 1 ? 's' : ''} remaining
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
