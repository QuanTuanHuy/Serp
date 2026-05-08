'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { AlertCircle, ArrowLeft, Loader2, Play, XCircle } from 'lucide-react';
import { Badge, Button, Card, CardContent, Skeleton } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { TransportPlanDetail, TransportPlanStopDetail } from '../../types';
import {
  useGetMyTransportPlanDetailQuery,
  useStartRouteMutation,
  useArriveAtStopMutation,
  useCompleteStopMutation,
} from '../../api/ttcrsApi';
import { RouteInfoCard } from './components/RouteInfoCard';
import { StopsList } from './components/StopsList';
import { DriverRouteMap } from './components/DriverRouteMap';
import { ExecutingRouteView } from './components/executing/ExecutingRouteView';
import { EvidencePage } from './components/executing/EvidencePage';

// -------------------------------------------------------------------------
// OSRM total distance helper
// -------------------------------------------------------------------------

async function fetchOsrmTotalDistance(stops: TransportPlanStopDetail[]): Promise<number | null> {
  const coords = [...stops]
    .filter((s) => s.lat != null && s.lng != null)
    .sort((a, b) => a.sequence - b.sequence)
    .map((s) => `${s.lng},${s.lat}`)
    .join(';');
  if (!coords.includes(';')) return null; // need at least 2 points
  const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=false`;
  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(10000) });
    if (!res.ok) return null;
    const data = await res.json();
    if (data.code !== 'Ok' || !data.routes?.[0]) return null;
    return data.routes[0].distance / 1000; // metres → km
  } catch {
    return null;
  }
}

// -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

const STATUS_BADGE: Record<string, string> = {
  CREATED:   'bg-blue-100 text-blue-700 border-blue-200',
  EXECUTING: 'bg-orange-100 text-orange-700 border-orange-200',
  COMPLETED: 'bg-green-100 text-green-700 border-green-200',
  CANCELLED: 'bg-red-100 text-red-700 border-red-200',
};

/** Returns stops ordered by sequence */
function ordered(stops: TransportPlanStopDetail[]) {
  return [...stops].sort((a, b) => a.sequence - b.sequence);
}

/** The next stop to navigate to (first stop without actualArrivalTime after current) */
function nextUnvisitedStop(plan: TransportPlanDetail): TransportPlanStopDetail | null {
  const seq = ordered(plan.stops);
  return seq.find((s) => !s.actualArrivalTime) ?? null;
}

/** The stop that driver has arrived at but not completed yet */
function pendingEvidenceStop(plan: TransportPlanDetail): TransportPlanStopDetail | null {
  return plan.stops.find((s) => s.actualArrivalTime != null && !s.isCompleted) ?? null;
}

function isLastStop(plan: TransportPlanDetail, seq: number): boolean {
  const maxSeq = Math.max(...plan.stops.map((s) => s.sequence));
  return seq === maxSeq;
}

// -------------------------------------------------------------------------
// Component
// -------------------------------------------------------------------------

interface DriverRouteDetailPageProps {
  id: number;
}

export function DriverRouteDetailPage({ id }: DriverRouteDetailPageProps) {
  const router = useRouter();

  const { data, isLoading, isError, refetch } = useGetMyTransportPlanDetailQuery(id);
  const plan = data?.data ?? null;

  const [startRoute, { isLoading: isStarting }] = useStartRouteMutation();
  const [arriveAtStop, { isLoading: isArriving }] = useArriveAtStopMutation();
  const [completeStop, { isLoading: isCompleting }] = useCompleteStopMutation();

  // Pre-fetched OSRM total distance for the whole route (used on last stop completion).
  const [osrmTotalDistanceKm, setOsrmTotalDistanceKm] = useState<number | null>(null);
  const osrmFetchedRef = useRef(false);
  useEffect(() => {
    if (!plan || osrmFetchedRef.current) return;
    osrmFetchedRef.current = true;
    fetchOsrmTotalDistance(plan.stops).then(setOsrmTotalDistanceKm);
  }, [plan?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  // For EXECUTING: track local phase + which stop is being evidenced.
  // arrivedStop is set immediately when handleArrive succeeds (before refetch),
  // so the evidence page renders without waiting for server data to refresh.
  const [executingPhase, setExecutingPhase] = useState<'navigating' | 'evidence'>('navigating');
  const [arrivedStop, setArrivedStop] = useState<TransportPlanStopDetail | null>(null);

  // Sync phase on initial load / page refresh mid-route.
  useEffect(() => {
    if (!plan || plan.status !== 'EXECUTING') return;
    const serverEvidenceStop = pendingEvidenceStop(plan);
    if (serverEvidenceStop) {
      setArrivedStop(serverEvidenceStop);
      setExecutingPhase('evidence');
    } else {
      setExecutingPhase('navigating');
    }
  // Only re-run when the underlying server state changes, not on every render
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [plan?.status, plan?.currentStop]);

  // -------------------------------------------------------------------------
  // Action handlers
  // -------------------------------------------------------------------------

  const handleStart = async () => {
    try {
      await startRoute(id).unwrap();
      refetch();
    } catch (err: any) {
      alert(err?.data?.message ?? 'Failed to start route');
    }
  };

  const handleArrive = async (seq: number) => {
    const stop = plan?.stops.find((s) => s.sequence === seq) ?? null;
    if (!stop) return;
    // Optimistic UI: switch to evidence page IMMEDIATELY so the map disappears
    // before the API call completes. Revert on failure.
    const prevArrived = arrivedStop;
    const prevPhase = executingPhase;
    setArrivedStop(stop);
    setExecutingPhase('evidence');
    try {
      await arriveAtStop({ id, seq }).unwrap();
      refetch();
    } catch (err: any) {
      setArrivedStop(prevArrived);
      setExecutingPhase(prevPhase);
      alert(err?.data?.message ?? 'Failed to record arrival');
    }
  };

  const handleCompleteStop = async (seq: number, evidenceUrl: string | null, last: boolean) => {
    const totalDistance = last ? (osrmTotalDistanceKm ?? null) : null;
    // Optimistic: leave evidence page immediately. Revert if API fails.
    const prevArrived = arrivedStop;
    const prevPhase = executingPhase;
    setArrivedStop(null);
    setExecutingPhase('navigating');
    try {
      await completeStop({ id, seq, body: { evidenceUrl, totalDistance } }).unwrap();
      refetch();
    } catch (err: any) {
      setArrivedStop(prevArrived);
      setExecutingPhase(prevPhase);
      alert(err?.data?.message ?? 'Failed to complete stop');
    }
  };

  // -------------------------------------------------------------------------
  // Render states
  // -------------------------------------------------------------------------

  if (isLoading) {
    return (
      <div className="flex flex-col gap-4 px-12 py-8">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (isError || !plan) {
    return (
      <div className="flex flex-col items-center gap-4 py-20 text-destructive">
        <AlertCircle className="h-10 w-10" />
        <p>Failed to load route details.</p>
        <Button variant="outline" onClick={() => router.back()}>
          Go back
        </Button>
      </div>
    );
  }

  // -------------------------------------------------------------------------
  // EXECUTING — full screen view
  // -------------------------------------------------------------------------
  if (plan.status === 'EXECUTING') {
    const orderedStops = ordered(plan.stops);
    // Use arrivedStop from local state (set immediately on handleArrive).
    // Fall back to server-derived stop only on page-refresh recovery.
    const displayEvidenceStop = arrivedStop ?? pendingEvidenceStop(plan);
    const nextStop = nextUnvisitedStop(plan);

    const currentStopObj = plan.currentStop != null
      ? orderedStops.find((s) => s.sequence === plan.currentStop) ?? orderedStops[0]
      : orderedStops[0];

    if (executingPhase === 'evidence' && displayEvidenceStop) {
      return (
        <div className="min-h-screen bg-background">
          <div className="sticky top-0 z-10 flex items-center gap-3 border-b bg-background px-4 py-3">
            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => router.back()}>
              <ArrowLeft className="h-4 w-4" />
            </Button>
            <span className="font-semibold">Stop Evidence</span>
            <Badge className={cn('ml-auto border text-xs', STATUS_BADGE[plan.status])}>
              {plan.status}
            </Badge>
          </div>
          <EvidencePage
            stop={displayEvidenceStop}
            isLastStop={isLastStop(plan, displayEvidenceStop.sequence)}
            isCompleting={isCompleting}
            onComplete={(url) => handleCompleteStop(displayEvidenceStop.sequence, url, isLastStop(plan, displayEvidenceStop.sequence))}
          />
        </div>
      );
    }

    // navigating phase
    if (!nextStop) {
      return (
        <div className="flex flex-col items-center gap-4 py-20">
          <p className="text-muted-foreground">All stops visited. Waiting for server update…</p>
          <Button onClick={() => refetch()}>Refresh</Button>
        </div>
      );
    }

    return (
      <ExecutingRouteView
        fromStop={currentStopObj}
        toStop={nextStop}
        onArrived={() => handleArrive(nextStop.sequence)}
        isArrivePending={isArriving}
      />
    );
  }

  // -------------------------------------------------------------------------
  // CREATED / COMPLETED / CANCELLED — standard layout
  // -------------------------------------------------------------------------
  return (
    <div className="flex flex-col gap-6 px-4 py-6 sm:px-12">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => router.back()}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <h1 className="text-xl font-bold">Route #{plan.id}</h1>
            <Badge className={cn('border text-xs', STATUS_BADGE[plan.status])}>
              {plan.status}
            </Badge>
          </div>
          <p className="text-sm text-muted-foreground">Truck {plan.truckCode}</p>
        </div>

        {/* CREATED: Start Route button */}
        {plan.status === 'CREATED' && (
          <Button
            className="bg-green-600 hover:bg-green-700 text-white"
            onClick={handleStart}
            disabled={isStarting}
          >
            {isStarting ? (
              <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Starting…</>
            ) : (
              <><Play className="mr-2 h-4 w-4" /> Start Route</>
            )}
          </Button>
        )}
      </div>

      {/* Route info */}
      <RouteInfoCard plan={plan} />

      {/* CANCELLED: show reason */}
      {plan.status === 'CANCELLED' && plan.cancelReason && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="p-4">
            <div className="flex items-start gap-2">
              <XCircle className="h-5 w-5 text-red-500 mt-0.5 shrink-0" />
              <div>
                <p className="text-sm font-semibold text-red-700">Cancellation Reason</p>
                <p className="mt-1 text-sm text-red-600">{plan.cancelReason}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Stops list */}
      <div>
        <h2 className="mb-2 text-sm font-semibold text-muted-foreground uppercase tracking-wide">
          Stops
        </h2>
        <StopsList stops={plan.stops} />
      </div>

      {/* Map */}
      <div>
        <h2 className="mb-2 text-sm font-semibold text-muted-foreground uppercase tracking-wide">
          Route Map
        </h2>
        <div className="h-96 rounded-xl overflow-hidden border sm:h-[28rem]">
          <DriverRouteMap stops={plan.stops} />
        </div>
      </div>

    </div>
  );
}
