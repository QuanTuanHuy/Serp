'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CalendarDays,
  CheckCircle2,
  PlayCircle,
  XCircle,
  Warehouse,
  GraduationCap,
  BusFront,
  Users,
  MapPin,
  Route,
  Clock,
  ArrowLeft,
  Bell,
  Play,
  SkipForward,
  User,
} from 'lucide-react';
import { toast } from 'sonner';
import { Client } from '@stomp/stompjs';
import {
  useArriveTripStopMutation,
  useCancelTripMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useGetTripAttendanceManifestQuery,
  useGetTripAttendanceQuery,
  useGetTripAttendanceSummaryQuery,
  useGetTripsQuery,
  useSkipTripStopMutation,
  useStartTripMutation,
  useStartBoardingTripStopMutation,
  useGetRoutePathQuery,
} from '../api/schoolBusApi';
import { connectSchoolBusSocket, subscribeTripEvents } from '../api/schoolBusSocket';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { Button, Input, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate, formatDateTime, getPageItems } from '../utils';
import type { TripAttendanceStopItem } from '../types';
import { TripMap } from '../components/map/TripMap';
import { MapMarkerVisibilityProvider } from '../components/map/MapMarkerVisibilityContext';
import { useSchoolBusAccess } from '../security/schoolBusAccess';

// ── Helpers ──────────────────────────────────────────────────────────────────

function stopTypeLabel(stop: TripAttendanceStopItem): string {
  const { stopPurpose, locationType } = stop;
  if (stopPurpose === 'START_TERMINAL') {
    return locationType === 'SCHOOL' ? 'School – Start terminal' : 'Depot – Start terminal';
  }
  if (stopPurpose === 'END_TERMINAL') {
    return locationType === 'SCHOOL' ? 'School – End terminal' : 'Depot – End terminal';
  }
  if (stopPurpose === 'PICKUP') return 'Pickup stop';
  if (stopPurpose === 'DROPOFF') return 'Drop-off stop';
  return locationType ?? 'Stop';
}

const statusMap: Record<string, { label: string; className: string }> = {
  CREATED: { label: 'Created', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  PLANNED: { label: 'Planned', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  IN_PROGRESS: { label: 'In progress', className: 'border-blue-200 bg-blue-55 text-blue-700 hover:bg-blue-55' },
  COMPLETED: { label: 'Completed', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  CANCELLED: { label: 'Cancelled', className: 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50' },
  PAUSED: { label: 'Paused', className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50' },
  // Stop statuses:
  PENDING: { label: 'Pending', className: 'border-slate-200 bg-slate-50 text-slate-500 hover:bg-slate-50' },
  ARRIVED: { label: 'Arrived', className: 'border-blue-200 bg-blue-50 text-blue-600 hover:bg-blue-50' },
  BOARDING: { label: 'Boarding', className: 'border-indigo-250 bg-indigo-50 text-indigo-700 hover:bg-indigo-50' },
  DEPARTED: { label: 'Departed', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  SKIPPED: { label: 'Skipped', className: 'border-slate-200 bg-slate-100 text-slate-400 hover:bg-slate-100' },
  // Event types
  TRIP_STARTED: { label: 'Trip Started', className: 'border-blue-250 bg-blue-50 text-blue-700' },
  TRIP_COMPLETED: { label: 'Trip Completed', className: 'border-emerald-250 bg-emerald-50 text-emerald-700' },
  TRIP_CANCELLED: { label: 'Trip Cancelled', className: 'border-red-250 bg-red-50 text-red-700' },
  STOP_ARRIVED: { label: 'Stop Arrived', className: 'border-blue-200 bg-blue-50 text-blue-600' },
  STOP_BOARDING_STARTED: { label: 'Boarding Started', className: 'border-indigo-200 bg-indigo-50 text-indigo-700' },
  STOP_DEPARTED: { label: 'Stop Departed', className: 'border-emerald-200 bg-emerald-55 text-emerald-700' },
  STOP_SKIPPED: { label: 'Stop Skipped', className: 'border-slate-200 bg-slate-100 text-slate-450' },
  STUDENT_BOARDED: { label: 'Student Boarded', className: 'border-blue-150 bg-blue-50 text-blue-650' },
  STUDENT_ABSENT: { label: 'Student Absent', className: 'border-red-200 bg-red-50 text-red-700' },
  STUDENT_NO_SHOW: { label: 'Student No-show', className: 'border-red-250 bg-red-50 text-red-650' },
  STUDENT_DROPPED_OFF: { label: 'Student Dropped Off', className: 'border-emerald-200 bg-emerald-50 text-emerald-650' },
  STUDENT_NOT_SERVED: { label: 'Student Not Served', className: 'border-slate-200 bg-slate-50 text-slate-400' },
};

const renderFriendlyBadge = (status: string) => {
  const normalized = (status || '').toUpperCase();
  const config = statusMap[normalized] || {
    label: normalized,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge className={cn('rounded-full px-2 py-0.2 text-[10px] font-bold shadow-none border shrink-0 hover:bg-transparent', config.className)}>
      {config.label}
    </Badge>
  );
};

const getFriendlyDirection = (dir?: string | null) => {
  if (dir === 'RETURN') return 'Return';
  if (dir === 'OUTBOUND') return 'Outbound';
  if (dir === 'ROUND_TRIP') return 'Round trip';
  return dir || '';
};

interface SchoolBusTripOperationDetailPageProps {
  tripId: number;
}

export function SchoolBusTripOperationDetailPage({ tripId }: SchoolBusTripOperationDetailPageProps) {
  const access = useSchoolBusAccess();
  // ── State ──────────────────────────────────────────────────────────────────
  const [selectedStopId, setSelectedStopId] = React.useState<number | null>(null);
  const [showSkipForm, setShowSkipForm] = React.useState(false);
  const [skipReason, setSkipReason] = React.useState('');
  const [showCancelForm, setShowCancelForm] = React.useState(false);
  const [cancelReason, setCancelReason] = React.useState('');
  const [wsEvents, setWsEvents] = React.useState<any[]>([]);
  const [wsState, setWsState] = React.useState<'Live' | 'Offline'>('Offline');
  const clientRef = React.useRef<Client | null>(null);

  // ── Queries ────────────────────────────────────────────────────────────────
  const tripsQuery = useGetTripsQuery({ page: 0, size: 50, sortBy: 'serviceDate', sortDirection: 'DESC' });
  const { data: manifestData, isLoading: manifestLoading, refetch: refetchManifest } =
    useGetTripAttendanceManifestQuery(tripId);
  const { data: summaryData, refetch: refetchSummary } = useGetTripAttendanceSummaryQuery(tripId);
  const { data: eventsData, refetch: refetchEvents } = useGetTripAttendanceQuery(tripId);
  const { data: routePathData } = useGetRoutePathQuery(
    manifestData?.data?.routeId as number,
    { skip: !manifestData?.data?.routeId }
  );

  // ── Derived ────────────────────────────────────────────────────────────────
  const allTrips = getPageItems(tripsQuery.data?.data);
  const trip = allTrips.find((t) => t.id === tripId) ?? null;

  const manifest = manifestData?.data ?? null;
  const summary = summaryData?.data ?? manifest?.summary ?? null;
  const restEvents = eventsData?.data ?? [];

  const tripStatus = manifest?.tripStatus ?? trip?.status ?? null;
  const tripIsActive = tripStatus === 'IN_PROGRESS';
  const tripIsCompleted = tripStatus === 'COMPLETED';
  const tripIsCancelled = tripStatus === 'CANCELLED';
  const isOutbound = (manifest?.routeDirection ?? trip?.routeDirection) === 'OUTBOUND';
  const tripCode = manifest?.tripCode ?? trip?.tripCode ?? `Trip #${tripId}`;
  const routeCode = manifest?.routeCode ?? trip?.routeCode ?? '';
  const routeName = (manifest as any)?.routeName ?? trip?.routeName ?? '';

  // ── WebSocket Real-time ───────────────────────────────────────────────────
  React.useEffect(() => {
    const client = connectSchoolBusSocket();
    clientRef.current = client;

    const originalOnConnect = client.onConnect;
    client.onConnect = (frame) => {
      originalOnConnect?.(frame);
      setWsState('Live');
    };

    const originalOnWebSocketClose = client.onWebSocketClose;
    client.onWebSocketClose = (evt) => {
      originalOnWebSocketClose?.(evt);
      setWsState('Offline');
    };

    subscribeTripEvents(client, tripId, (msg) => {
      setWsEvents((prev) => [msg, ...prev]);
      // Refetch API queries to sync state instantly
      refetchManifest();
      refetchSummary();
      refetchEvents();
    });

    return () => {
      if (client.active) {
        client.deactivate();
      }
      clientRef.current = null;
      setWsState('Offline');
    };
  }, [tripId, refetchManifest, refetchSummary, refetchEvents]);

  // ── Merge & Sort Events ───────────────────────────────────────────────────
  const mergedEvents = React.useMemo(() => {
    if (wsEvents.length === 0) return restEvents;
    const wsConverted = wsEvents.map((e, idx) => ({
      id: -idx - 1,
      studentName: e.studentName || 'System',
      studentCode: e.studentCode || '',
      eventType: e.eventType || e.action,
      recordedAt: e.timestamp || new Date().toISOString(),
      notes: e.reason || e.notes || '',
      attendanceType: e.attendanceType || e.eventType || '',
    }));
    // Deduplicate
    const eventKey = (e: any) =>
      `${e.studentName}|${e.eventType}|${e.recordedAt}`;
    const restKeys = new Set(restEvents.map(eventKey));
    const newWs = wsConverted.filter((e) => !restKeys.has(eventKey(e)));
    return [...newWs, ...restEvents];
  }, [wsEvents, restEvents]);

  const sortedEvents = React.useMemo(() => {
    return [...mergedEvents].sort(
      (a: any, b: any) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
    );
  }, [mergedEvents]);

  // ── Auto-advance to current (first non-done) stop ─────────────────────────
  React.useEffect(() => {
    if (!manifest?.stops?.length) return;
    const selected = manifest.stops.find((s) => s.routeStopId === selectedStopId);
    if (
      !selected ||
      selected.stopStatus === 'DEPARTED' ||
      selected.stopStatus === 'SKIPPED'
    ) {
      const next = manifest.stops.find(
        (s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED',
      );
      setSelectedStopId(
        next?.routeStopId ??
          manifest.stops[manifest.stops.length - 1]?.routeStopId ??
          null,
      );
    }
  }, [manifest, selectedStopId]);

  // ── Current & Next Stops Inferred ──────────────────────────────────────────
  const opSummary = React.useMemo(() => {
    if (!manifest?.stops?.length) return null;
    const stops = manifest.stops;
    const total = stops.length;
    const done = stops.filter(
      (s) => s.stopStatus === 'DEPARTED' || s.stopStatus === 'SKIPPED',
    ).length;

    let current = null;
    let next = null;

    if (tripStatus === 'COMPLETED') {
      current = stops[stops.length - 1];
    } else if (tripStatus === 'CREATED' || tripStatus === 'PLANNED') {
      current = stops[0];
      if (stops.length > 1) next = stops[1];
    } else if (tripStatus === 'IN_PROGRESS') {
      const active = stops.find((s) => s.stopStatus === 'ARRIVED' || s.stopStatus === 'BOARDING');
      if (active) {
        current = active;
      } else {
        const firstNonDone = stops.find((s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED');
        current = firstNonDone ?? stops[stops.length - 1];
      }
      next = stops.find((s) => s.stopStatus === 'PENDING');
    }

    return { total, done, current, next };
  }, [manifest, tripStatus]);

  // ── Derived logic for complete trip and timeline sequence ──────────────────
  const sortedStops = React.useMemo(() => {
    if (!manifest?.stops) return [];
    return [...manifest.stops].sort((a, b) => a.stopOrder - b.stopOrder);
  }, [manifest?.stops]);

  const firstUnfinishedStop = React.useMemo(() => {
    return sortedStops.find((s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED') ?? null;
  }, [sortedStops]);

  const allStopsFinished = React.useMemo(() => {
    return sortedStops.length > 0 && sortedStops.every(
      (s) => s.stopStatus === 'DEPARTED' || s.stopStatus === 'SKIPPED'
    );
  }, [sortedStops]);

  const hasPlannedStudents = React.useMemo(() => {
    return manifest?.students?.some((st) => st.status === 'PLANNED') ?? false;
  }, [manifest?.students]);

  const hasBoardedStudents = React.useMemo(() => {
    return manifest?.students?.some((st) => st.status === 'BOARDED') ?? false;
  }, [manifest?.students]);

  const canCompleteTrip =
    tripStatus === 'IN_PROGRESS' &&
    allStopsFinished &&
    !hasPlannedStudents &&
    !hasBoardedStudents;

  // ── Mutations ──────────────────────────────────────────────────────────────
  const [startTrip, { isLoading: starting }] = useStartTripMutation();
  const [completeTrip, { isLoading: completing }] = useCompleteTripMutation();
  const [cancelTrip, { isLoading: cancelling }] = useCancelTripMutation();
  const [arriveStop, { isLoading: arriving }] = useArriveTripStopMutation();
  const [departStop, { isLoading: departing }] = useDepartTripStopMutation();
  const [skipStop, { isLoading: skipping }] = useSkipTripStopMutation();
  const [startBoardingStop, { isLoading: boarding }] = useStartBoardingTripStopMutation();

  const isActing = starting || completing || cancelling || arriving || departing || skipping || boarding;

  const act = async (label: string, fn: () => Promise<unknown>) => {
    try {
      await fn();
      toast.success(`${label} completed`);
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? `${label} failed`);
    }
  };

  // ── Actions ────────────────────────────────────────────────────────────────
  const handleStart = () => act('Start trip', () => startTrip(tripId).unwrap());
  const handleComplete = () => act('Complete trip', () => completeTrip({ id: tripId }).unwrap());
  const handleStartBoarding = (stopId: number) => {
    act('Start boarding', () => startBoardingStop({ tripId, routeStopId: stopId }).unwrap());
  };
  const handleCancel = () => {
    if (!cancelReason.trim()) return;
    act('Cancel trip', () =>
      cancelTrip({ id: tripId, body: { reason: cancelReason } }).unwrap(),
    );
    setShowCancelForm(false);
    setCancelReason('');
  };

  const handleArrive = (stopId: number) => {
    act('Arrive stop', () => arriveStop({ tripId, routeStopId: stopId }).unwrap());
  };
  const handleDepart = (stopId: number) => {
    act('Depart stop', () => departStop({ tripId, routeStopId: stopId }).unwrap());
  };
  const handleSkip = (stopId: number) => {
    if (!skipReason.trim()) return;
    act('Skip stop', () =>
      skipStop({ tripId, routeStopId: stopId, reason: skipReason }).unwrap(),
    );
    setShowSkipForm(false);
    setSkipReason('');
  };

  return (
    <MapMarkerVisibilityProvider>
      <SchoolBusPageShell
        title={tripCode}
        description='Real-time trip dispatch cockpit. Track stop execution lifecycle and review route operation logs.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Trips', href: '/school-bus/trips' },
              { label: tripCode, current: true },
            ]}
          />
        }
      >
        <div className='flex flex-col gap-6'>
          {/* Back navigation & banners */}
          <div className='flex flex-col gap-3'>
            <div className='flex items-center justify-between'>
              <Button variant='outline' size='sm' className='rounded-full h-8 px-3 font-semibold' asChild>
                <Link href='/school-bus/trips'>
                  <ArrowLeft className='h-3.5 w-3.5 mr-1.5' />
                  Back to Trips list
                </Link>
              </Button>
            </div>

            {tripIsCompleted && (
              <div className='flex items-center gap-2.5 bg-emerald-50 border border-emerald-100 text-emerald-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
                <CheckCircle2 className='h-4.5 w-4.5 text-emerald-600 shrink-0' />
                <span>Trip completed — operations are locked.</span>
              </div>
            )}

            {tripIsCancelled && (
              <div className='flex items-center gap-2.5 bg-red-50 border border-red-100 text-red-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
                <XCircle className='h-4.5 w-4.5 text-red-600 shrink-0' />
                <span>Trip cancelled. Reason: {(manifest as any)?.cancellationReason || trip?.cancellationReason || 'N/A'}</span>
              </div>
            )}
          </div>

          {/* Trip Summary Card */}
          {trip && (
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-3 border-b border-slate-100'>
                <div className='flex items-center gap-2.5 min-w-0'>
                  <div className='flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700 border border-blue-100/50'>
                    <Route className='h-5 w-5' />
                  </div>
                  <div className='min-w-0'>
                    <h3 className='font-bold text-slate-900 text-sm truncate'>{tripCode}</h3>
                    <p className='text-xs text-slate-400 truncate mt-0.5'>{routeCode} — {routeName}</p>
                  </div>
                </div>
                <div className='flex items-center gap-2'>
                  <span className={cn(
                    'text-[10px] font-bold px-2 py-0.5 rounded-full border shrink-0',
                    wsState === 'Live' ? 'bg-emerald-50 border-emerald-250 text-emerald-700' : 'bg-slate-50 border-slate-200 text-slate-400'
                  )}>
                    {wsState} feed
                  </span>
                  {renderFriendlyBadge(tripStatus || '')}
                </div>
              </div>

              <div className='grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-8 gap-y-4 gap-x-6 text-xs'>
                <div className='flex items-start gap-2 min-w-0'>
                  <CalendarDays className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Service Date</span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>{formatDate(trip.serviceDate || '')}</span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Route className='h-4.5 w-4.5 text-indigo-500 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Direction</span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>{getFriendlyDirection(trip.routeDirection)}</span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Route className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Route Length</span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {manifest?.distanceKm != null ? `${manifest.distanceKm} km` : (trip?.plannedDistanceKm != null ? `${trip.plannedDistanceKm} km` : '—')}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Clock className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Est. Duration</span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>
                      {manifest?.durationMin != null ? `${manifest.durationMin} mins` : (trip?.plannedDurationMin != null ? `${trip.plannedDurationMin} mins` : '—')}
                    </span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <BusFront className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Bus Vehicle</span>
                    {trip.busPlateNumber ? (
                      <span className='font-mono font-bold text-slate-800 bg-slate-50 border border-slate-200/80 rounded px-1.5 py-0.2 mt-0.5 w-fit'>{trip.busPlateNumber}</span>
                    ) : (
                      <span className='font-bold text-amber-600 mt-0.5'>Missing bus</span>
                    )}
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <User className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Driver</span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>{trip.driverName || 'No driver assigned'}</span>
                  </div>
                </div>

                <div className='flex items-start gap-2 min-w-0'>
                  <Users className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                  <div className='flex flex-col min-w-0'>
                    <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Attendant</span>
                    <span className='font-bold text-slate-800 truncate mt-0.5'>{trip.attendantName || '—'}</span>
                  </div>
                </div>

                {/* Main operational control actions — only for users who can operate trips */}
                <div className='flex flex-col justify-center sm:col-span-2 lg:col-span-1 gap-2'>
                  {access.canOperateTrip && !tripIsCompleted && !tripIsCancelled && (
                    <>
                      {tripStatus === 'CREATED' || tripStatus === 'PLANNED' || tripStatus === 'ASSIGNED' || tripStatus === 'READY' ? (
                        <Button
                          size='sm'
                          className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full font-bold shadow-none h-8 px-4 border-0 text-xs shrink-0 w-full'
                          onClick={handleStart}
                          disabled={isActing}
                        >
                          <PlayCircle className='mr-1.5 h-4 w-4' />
                          Start Trip
                        </Button>
                      ) : null}

                      {tripStatus === 'IN_PROGRESS' && (
                        <div className='w-full' title={!canCompleteTrip ? 'Complete all stops and resolve all student statuses before completing this trip.' : undefined}>
                          <Button
                            size='sm'
                            className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full font-bold shadow-none h-8 px-4 border-0 text-xs shrink-0 w-full disabled:opacity-50 disabled:cursor-not-allowed'
                            onClick={handleComplete}
                            disabled={isActing || !canCompleteTrip}
                          >
                            <CheckCircle2 className='mr-1.5 h-4 w-4' />
                            Complete Trip
                          </Button>
                          {!canCompleteTrip && (
                            <p className='text-[9px] text-red-500 mt-1 font-semibold text-center leading-tight'>
                              Complete all stops and resolve all student statuses before completing this trip.
                            </p>
                          )}
                        </div>
                      )}

                      {showCancelForm ? (
                        <div className='flex flex-col gap-1.5'>
                          <Input
                            value={cancelReason}
                            onChange={(e) => setCancelReason(e.target.value)}
                            placeholder='Cancellation reason…'
                            className='h-7 text-xs rounded-lg px-2 bg-slate-50'
                          />
                          <div className='flex gap-1 justify-end'>
                            <Button
                              size='sm'
                              variant='ghost'
                              className='h-6 text-[10px] px-2 rounded-lg'
                              onClick={() => setShowCancelForm(false)}
                            >
                              Cancel
                            </Button>
                            <Button
                              size='sm'
                              className='h-6 text-[10px] rounded-lg bg-red-650 hover:bg-red-700 text-white font-bold px-2'
                              onClick={handleCancel}
                              disabled={!cancelReason.trim() || isActing}
                            >
                              Confirm
                            </Button>
                          </div>
                        </div>
                      ) : (
                        <Button
                          size='sm'
                          variant='outline'
                          className='h-8 rounded-full border-red-250 text-red-650 hover:bg-red-50 text-xs font-semibold px-3 w-full'
                          onClick={() => setShowCancelForm(true)}
                          disabled={isActing}
                        >
                          <XCircle size={13} className='mr-1.5 shrink-0' />
                          Cancel Trip
                        </Button>
                      )}
                    </>
                  )}
                  {/* Readonly badge for non-operators */}
                  {!access.canOperateTrip && !tripIsCompleted && !tripIsCancelled && (
                    <span className='inline-flex items-center justify-center rounded-full bg-slate-50 border border-slate-200 px-3 py-1 text-[10px] font-semibold text-slate-400'>
                      View only
                    </span>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Stats & Summaries Row */}
          {(summary || opSummary) && (
            <div className='grid gap-4 md:grid-cols-3'>
              {/* Trip progress */}
              {opSummary && (
                <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-between'>
                  <div>
                    <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2'>Trip Progress</p>
                    <div className='flex items-end justify-between mb-2'>
                      <span className='text-2xl font-extrabold text-slate-800'>{opSummary.done}/{opSummary.total}</span>
                      <span className='text-xs font-semibold text-slate-400'>Stops Visited</span>
                    </div>
                  </div>
                  <div className='w-full bg-slate-100 rounded-full h-2.5 overflow-hidden border border-slate-200/50'>
                    <div
                      className='bg-blue-600 h-full rounded-full transition-all duration-300'
                      style={{ width: `${(opSummary.done / opSummary.total) * 100}%` }}
                    />
                  </div>
                </div>
              )}

              {/* Stop tracking details */}
              {opSummary && (
                <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-between text-xs font-medium text-slate-500'>
                  <div>
                    <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2'>Route Status Inferred</p>
                    <div className='space-y-1.5'>
                      <div className='flex items-center justify-between'>
                        <span>Current location:</span>
                        <span className='font-bold text-slate-800 truncate max-w-[150px]'>
                          {opSummary.current ? opSummary.current.displayName : '—'}
                        </span>
                      </div>
                      <div className='flex items-center justify-between'>
                        <span>Next terminal:</span>
                        <span className='font-bold text-slate-800 truncate max-w-[150px]'>
                          {opSummary.next ? opSummary.next.displayName : '—'}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Students Summary & Open Attendance */}
              {summary && (
                <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-between'>
                  <div>
                    <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-3'>Students Attendance</p>
                    <div className='grid grid-cols-6 gap-1 text-center divide-x divide-slate-100 mb-3'>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-slate-800'>{summary.totalStudents}</span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>Total</span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-amber-600'>{summary.planned}</span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>Planned</span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-blue-600'>{summary.boarded}</span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>Boarded</span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-emerald-600'>{summary.droppedOff}</span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>Drop</span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-red-500'>{summary.absent + summary.noShow}</span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>Absent</span>
                      </div>
                      <div className='flex flex-col gap-1 min-w-0'>
                        <span className='text-base font-extrabold text-slate-400'>{summary.notServed}</span>
                        <span className='text-[9px] text-slate-400 font-semibold truncate'>Not Srv</span>
                      </div>
                    </div>
                  </div>
                  <Button
                    size='sm'
                    className='w-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-bold rounded-xl h-8 text-xs border-0 shadow-none'
                    asChild
                  >
                    <Link href={`/school-bus/attendance/${tripId}`}>
                      Open Student Attendance Board
                    </Link>
                  </Button>
                </div>
              )}
            </div>
          )}

          {/* Map and Timeline */}
          <div className='grid gap-5 xl:grid-cols-[1fr_390px] items-start'>
            {/* Map container */}
            <div className='relative h-[480px] overflow-hidden rounded-2xl border border-slate-200 shadow-xs bg-slate-50'>
              <TripMap
                stops={manifest?.stops || []}
                tripStatus={tripStatus || ''}
                isOutbound={isOutbound}
                routeGeometry={manifest?.routeGeometry}
                routePath={routePathData?.data}
                className='h-full w-full'
              />
            </div>

            {/* Timeline stop operations */}
            <div className='flex flex-col gap-4 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm max-h-[480px] overflow-y-auto'>
              <div>
                <p className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                  Stop Operation Timeline
                </p>
                <p className='text-[10px] text-slate-450 mt-1 font-semibold leading-relaxed'>
                  Execute arrivals, boarding periods, and departures sequentially along the path.
                </p>
              </div>

              {manifest?.stops && manifest.stops.length > 0 ? (
                <div className='relative pl-3 space-y-5 before:absolute before:left-[15px] before:top-2 before:bottom-2 before:w-[1.5px] before:bg-slate-100/70'>
                  {sortedStops.map((stop) => {
                      const isCurrent = opSummary?.current && stop.routeStopId === opSummary.current.routeStopId;
                      const isNext = opSummary?.next && stop.routeStopId === opSummary.next.routeStopId;

                      const isPending = stop.stopStatus === 'PENDING';
                      const isArrived = stop.stopStatus === 'ARRIVED';
                      const isBoarding = stop.stopStatus === 'BOARDING';
                      const isDeparted = stop.stopStatus === 'DEPARTED';
                      const isSkipped = stop.stopStatus === 'SKIPPED';
                      const isFinished = isDeparted || isSkipped;

                      const stopType = stop.locationType || '';
                      const StopIcon = stopType === 'SCHOOL'
                        ? GraduationCap
                        : stopType === 'DEPOT'
                        ? Warehouse
                        : MapPin;

                      // Control eligibility (sequential check)
                      const isNextActionableStop = firstUnfinishedStop && stop.routeStopId === firstUnfinishedStop.routeStopId;

                      const showArrive = isPending;
                      const canBoard =
                        (isOutbound && stop.stopPurpose === 'PICKUP') ||
                        (!isOutbound && stop.stopPurpose === 'START_TERMINAL' && stop.locationType === 'SCHOOL');
                      const showStartBoarding = isArrived && canBoard;
                      const showDepart = isArrived || isBoarding;
                      const canSkip = stop.stopPurpose !== 'START_TERMINAL' && stop.stopPurpose !== 'END_TERMINAL';
                      const showSkip = (isPending || isArrived) && canSkip;

                      return (
                        <div
                           key={stop.routeStopId}
                           className={cn(
                             'relative flex gap-4 text-xs font-semibold group transition-all',
                             isCurrent && 'text-blue-700',
                             isDeparted && 'text-slate-400 opacity-70',
                             isSkipped && 'text-slate-350 opacity-60'
                           )}
                        >
                           {/* Timeline dot */}
                           <div className={cn(
                             'relative z-10 flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white border shadow-2xs transition-all',
                             isCurrent
                               ? 'border-blue-600 ring-4 ring-blue-500/10'
                               : isDeparted
                               ? 'border-emerald-300 bg-emerald-50'
                               : isSkipped
                               ? 'border-slate-200 bg-slate-50'
                               : 'border-slate-250'
                           )}>
                             <span className={cn(
                               'h-2 w-2 rounded-full',
                               isCurrent
                                 ? 'bg-blue-600 animate-pulse'
                                 : isDeparted
                                 ? 'bg-emerald-500'
                                 : isSkipped
                                 ? 'bg-slate-300'
                                 : 'bg-slate-350'
                             )} />
                           </div>

                           {/* Stop Details */}
                           <div className='flex-1 min-w-0 space-y-2'>
                             <div className='flex items-start justify-between gap-1.5'>
                               <div className='min-w-0 flex-1'>
                                 <div className='flex items-center gap-1.5 min-w-0'>
                                   <div className={cn(
                                     'flex h-4.5 w-4.5 items-center justify-center rounded shrink-0 border border-slate-100',
                                     stopType === 'SCHOOL' ? 'bg-red-50 text-red-500' : stopType === 'DEPOT' ? 'bg-orange-50 text-orange-500' : 'bg-slate-50 text-slate-500'
                                   )}>
                                     <StopIcon className='h-3 w-3' />
                                   </div>
                                   <p className='truncate text-[11px] font-bold text-slate-800'>
                                     {stop.stopOrder}. {stop.displayName || `Stop #${stop.routeStopId}`}
                                   </p>
                                 </div>
                                 <p className='text-[9px] text-slate-400 font-semibold mt-0.5 pl-6'>
                                   {stopTypeLabel(stop)}
                                 </p>
                               </div>
                               <div className='flex flex-col items-end gap-1 shrink-0'>
                                 {renderFriendlyBadge(stop.stopStatus)}
                                 {stop.studentCount !== undefined && stop.studentCount !== null && stop.studentCount > 0 && (
                                   <span className='text-[8px] font-extrabold uppercase bg-slate-100 border border-slate-200 text-slate-650 px-1 py-0.2 rounded'>
                                     Students: {stop.studentCount}
                                   </span>
                                 )}
                               </div>
                             </div>

                             {/* Detailed timing info */}
                             <div className='text-[10px] text-slate-500 pl-6 space-y-0.5 font-medium'>
                               <div className='flex items-center gap-1.5'>
                                 <span className='text-slate-400'>Planned:</span>
                                 <span className='text-slate-700 font-semibold'>
                                   {stop.plannedArrivalTime || '—'}
                                   {stop.plannedDepartureTime ? ` - ${stop.plannedDepartureTime}` : ''}
                                 </span>
                               </div>
                               {(stop.actualArrivalTime || stop.actualDepartureTime) && (
                                 <div className='flex items-center gap-1.5 text-blue-600'>
                                   <span>Actual:</span>
                                   <span className='font-semibold'>
                                     {stop.actualArrivalTime ? stop.actualArrivalTime.split('T')[1]?.substring(0, 5) || stop.actualArrivalTime : '—'}
                                     {stop.actualDepartureTime ? ` - ${stop.actualDepartureTime.split('T')[1]?.substring(0, 5) || stop.actualDepartureTime}` : ''}
                                   </span>
                                 </div>
                               )}
                             </div>

                             {/* Actual visits info */}
                             {(stop.actualBoardedCount > 0 || stop.actualDroppedCount > 0) && (
                               <p className='text-[10px] text-slate-500 pl-6 font-medium'>
                                 Actual boarded: <span className='font-bold text-slate-700'>{stop.actualBoardedCount}</span> • Actual dropped: <span className='font-bold text-slate-700'>{stop.actualDroppedCount}</span>
                               </p>
                             )}

                             {/* Action Buttons — only for users who can operate trips */}
                             {tripIsActive && !isFinished && access.canOperateTrip && (
                               <div
                                 className='flex flex-wrap items-center gap-1.5 pl-6 pt-1'
                                 title={!isNextActionableStop ? 'Process previous stops first.' : undefined}
                               >
                                 {showArrive && (
                                   <Button
                                     size='sm'
                                     className='h-7 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-lg px-2.5 text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed border-0'
                                     onClick={() => handleArrive(stop.routeStopId)}
                                     disabled={isActing || !isNextActionableStop}
                                   >
                                     Arrive Stop
                                   </Button>
                                 )}
                                 {showStartBoarding && (
                                   <Button
                                     size='sm'
                                     className='h-7 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-lg px-2.5 text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed border-0'
                                     onClick={() => handleStartBoarding(stop.routeStopId)}
                                     disabled={isActing || !isNextActionableStop}
                                   >
                                     Start Boarding
                                   </Button>
                                 )}
                                 {showDepart && (
                                   <Button
                                     size='sm'
                                     variant={showStartBoarding ? 'outline' : 'default'}
                                     className={cn(
                                       'h-7 rounded-lg text-[10px] font-bold shadow-none disabled:opacity-50 disabled:cursor-not-allowed',
                                       showStartBoarding
                                         ? 'border-slate-200 text-slate-600 hover:bg-slate-50'
                                         : 'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
                                     )}
                                     onClick={() => handleDepart(stop.routeStopId)}
                                     disabled={isActing || !isNextActionableStop}
                                   >
                                     Depart Stop
                                   </Button>
                                 )}
                                 {showSkip && (
                                   <>
                                     {showSkipForm && selectedStopId === stop.routeStopId ? (
                                       <div className='flex items-center gap-1'>
                                         <Input
                                           value={skipReason}
                                           onChange={(e) => setSkipReason(e.target.value)}
                                           placeholder='Skip reason…'
                                           className='h-6 text-[10px] rounded-lg px-1.5 w-24 bg-slate-50'
                                         />
                                         <Button
                                           size='sm'
                                           className='h-6 text-[9px] rounded-lg bg-red-650 hover:bg-red-700 text-white font-bold px-1.5'
                                           onClick={() => handleSkip(stop.routeStopId)}
                                           disabled={!skipReason.trim() || isActing}
                                         >
                                           OK
                                         </Button>
                                         <Button
                                           size='sm'
                                           variant='ghost'
                                           className='h-6 text-[9px] px-1 rounded-lg'
                                           onClick={() => setShowSkipForm(false)}
                                         >
                                           Back
                                         </Button>
                                       </div>
                                     ) : (
                                       <Button
                                         size='sm'
                                         variant='outline'
                                         className='h-7 rounded-lg border-slate-200 text-slate-500 hover:bg-slate-50 text-[10px] font-semibold px-2.5 disabled:opacity-50 disabled:cursor-not-allowed'
                                         onClick={() => {
                                           setSelectedStopId(stop.routeStopId);
                                           setShowSkipForm(true);
                                         }}
                                         disabled={isActing || !isNextActionableStop}
                                       >
                                         <SkipForward size={11} className='mr-1 shrink-0' />
                                         Skip Stop
                                       </Button>
                                     )}
                                   </>
                                 )}
                               </div>
                             )}
                           </div>
                        </div>
                      );
                    })}
                </div>
              ) : (
                <SchoolBusEmptyState
                  title='No stops mapped'
                  description='Execution stops sequence is missing.'
                  icon={MapPin}
                />
              )}
            </div>
          </div>

          {/* Activity Log Feed */}
          <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
            <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider'>Activity Log Feed</p>
            {sortedEvents.length === 0 ? (
              <SchoolBusEmptyState
                title='No events logged yet'
                description='Attendance and lifecycle action logs will appear here as they are processed.'
                icon={Bell}
              />
            ) : (
              <div className='grid gap-3 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 max-h-[300px] overflow-y-auto pr-1'>
                {sortedEvents.map((item) => {
                  const isBoard = ['BOARDED', 'BOARD'].includes((item.eventType || item.attendanceType || '').toUpperCase());
                  const isDrop = ['DROPPED_OFF', 'DROPOFF'].includes((item.eventType || item.attendanceType || '').toUpperCase());
                  const isAbsent = ['ABSENT', 'NO_SHOW'].includes((item.eventType || item.attendanceType || '').toUpperCase());

                  const cardBorder = isBoard
                    ? 'border-blue-100 bg-blue-50/10'
                    : isDrop
                    ? 'border-emerald-100 bg-emerald-50/10'
                    : isAbsent
                    ? 'border-red-100 bg-red-50/10'
                    : 'border-slate-150 bg-white';

                  return (
                    <div
                      key={item.id}
                      className={cn('rounded-xl border p-3.5 shadow-2xs space-y-2.5 transition-all hover:shadow-xs', cardBorder)}
                    >
                      <div className='flex items-start justify-between gap-2.5 min-w-0'>
                        <div className='min-w-0 flex-1'>
                          <p className='truncate text-xs font-bold text-slate-900'>
                            {item.studentName}
                          </p>
                          <p className='text-[10px] text-slate-450 mt-1 font-semibold flex items-center gap-1'>
                            <Clock className='h-3 w-3 text-slate-350' />
                            {formatDateTime(item.recordedAt)}
                          </p>
                        </div>
                        {renderFriendlyBadge(item.eventType ?? item.attendanceType)}
                      </div>
                      {item.notes && (
                        <p className='text-[10px] text-slate-500 bg-slate-50 border border-slate-100 rounded px-1.5 py-0.5 mt-1 truncate' title={item.notes}>
                          Note: {item.notes}
                        </p>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </SchoolBusPageShell>
    </MapMarkerVisibilityProvider>
  );
}
