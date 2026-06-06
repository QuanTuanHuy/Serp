'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CalendarDays,
  CheckCircle2,
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
  Search,
} from 'lucide-react';
import { toast } from 'sonner';
import { Client } from '@stomp/stompjs';
import {
  useAbsentTripStudentMutation,
  useBoardTripStudentMutation,
  useDropoffTripStudentMutation,
  useGetTripAttendanceManifestQuery,
  useGetTripAttendanceQuery,
  useGetTripAttendanceSummaryQuery,
  useGetTripsQuery,
  useNoShowTripStudentMutation,
} from '../api/schoolBusApi';
import { connectSchoolBusSocket, subscribeTripEvents } from '../api/schoolBusSocket';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { Button, Input, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate, formatDateTime, getPageItems } from '../utils';
import type { TripAttendanceStopItem, TripAttendanceStudentItem } from '../types';

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
  // Student statuses:
  PLANNED: { label: 'Planned', className: 'border-amber-250 bg-amber-55 text-amber-700 hover:bg-amber-55' },
  BOARDED: { label: 'Boarded', className: 'border-blue-200 bg-blue-55 text-blue-700 hover:bg-blue-55' },
  DROPPED_OFF: { label: 'Dropped off', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  ABSENT: { label: 'Absent', className: 'border-red-200 bg-red-55 text-red-700 hover:bg-red-55' },
  NO_SHOW: { label: 'No-show', className: 'border-red-250 bg-red-50 text-red-650 hover:bg-red-50' },
  NOT_SERVED: { label: 'Not served', className: 'border-slate-200 bg-slate-50 text-slate-400 hover:bg-slate-50' },
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

const STUDENT_STATUS_DONE = new Set(['ABSENT', 'NO_SHOW', 'DROPPED_OFF', 'NOT_SERVED']);

interface SchoolBusAttendanceDetailPageProps {
  tripId: number;
}

export function SchoolBusAttendanceDetailPage({ tripId }: SchoolBusAttendanceDetailPageProps) {
  // ── State ──────────────────────────────────────────────────────────────────
  const [selectedStopId, setSelectedStopId] = React.useState<number | null>(null);
  const [searchQuery, setSearchQuery] = React.useState('');
  const [wsEvents, setWsEvents] = React.useState<any[]>([]);
  const [wsState, setWsState] = React.useState<'Live' | 'Offline'>('Offline');
  const clientRef = React.useRef<Client | null>(null);

  // ── Queries ────────────────────────────────────────────────────────────────
  const tripsQuery = useGetTripsQuery({ page: 0, size: 50, sortBy: 'serviceDate', sortDirection: 'DESC' });
  const { data: manifestData, isLoading: manifestLoading, refetch: refetchManifest } =
    useGetTripAttendanceManifestQuery(tripId);
  const { data: summaryData, refetch: refetchSummary } = useGetTripAttendanceSummaryQuery(tripId);
  const { data: eventsData, refetch: refetchEvents } = useGetTripAttendanceQuery(tripId);

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

  // ── Auto-advance Stop Selection ──────────────────────────────────────────
  React.useEffect(() => {
    if (!manifest?.stops?.length) return;
    const selected = manifest.stops.find((s) => s.routeStopId === selectedStopId);
    if (!selected) {
      // Find first non-done stop to select automatically
      const next = manifest.stops.find(
        (s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED'
      );
      setSelectedStopId(
        next?.routeStopId ??
          manifest.stops[manifest.stops.length - 1]?.routeStopId ??
          null
      );
    }
  }, [manifest, selectedStopId]);

  // ── Selected Stop Details ─────────────────────────────────────────────────
  const selectedStop = manifest?.stops?.find((s) => s.routeStopId === selectedStopId) ?? null;
  const stopStatus = selectedStop?.stopStatus ?? null;
  const isStopActionable = tripIsActive && (stopStatus === 'ARRIVED' || stopStatus === 'BOARDING' || stopStatus === 'PENDING');
  const isDepotStop = selectedStop?.locationType === 'DEPOT';

  // ── Students List filtered by Stop & Search ────────────────────────────────
  const studentsAtStop = React.useMemo<TripAttendanceStudentItem[]>(() => {
    if (!manifest || !selectedStop) return [];
    const { stopPurpose, locationType, routeStopId } = selectedStop;
    if (locationType === 'DEPOT') return [];

    let filtered: TripAttendanceStudentItem[] = [];

    if (stopPurpose === 'PICKUP') {
      filtered = manifest.students.filter((s) => s.pickupStopId === routeStopId);
    } else if (stopPurpose === 'DROPOFF') {
      filtered = manifest.students.filter((s) => s.dropoffStopId === routeStopId);
    } else if (stopPurpose === 'END_TERMINAL' && locationType === 'SCHOOL' && isOutbound) {
      filtered = manifest.students.filter(
        (s) => s.status === 'BOARDED' || s.status === 'DROPPED_OFF'
      );
    } else if (stopPurpose === 'START_TERMINAL' && locationType === 'SCHOOL' && !isOutbound) {
      filtered = manifest.students.filter((s) => s.status === 'PLANNED');
    }

    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (s) =>
          (s.studentName || '').toLowerCase().includes(q) ||
          (s.studentCode || '').toLowerCase().includes(q)
      );
    }

    return filtered;
  }, [manifest, selectedStop, isOutbound, searchQuery]);

  // ── Mutations ──────────────────────────────────────────────────────────────
  const [boardStudent, { isLoading: boarding }] = useBoardTripStudentMutation();
  const [dropoffStudent, { isLoading: droppingOff }] = useDropoffTripStudentMutation();
  const [absentStudent, { isLoading: markingAbsent }] = useAbsentTripStudentMutation();
  const [noShowStudent, { isLoading: markingNoShow }] = useNoShowTripStudentMutation();

  const isActing = boarding || droppingOff || markingAbsent || markingNoShow;

  const act = async (label: string, fn: () => Promise<unknown>) => {
    try {
      await fn();
      toast.success(`${label} recorded`);
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? `${label} failed`);
    }
  };

  // ── Attendance handlers ───────────────────────────────────────────────────
  const handleBoard = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Board ${s.studentName ?? ''}`, () =>
      boardStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap()
    );
  };
  const handleDropoff = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Drop-off ${s.studentName ?? ''}`, () =>
      dropoffStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap()
    );
  };
  const handleAbsent = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Absent ${s.studentName ?? ''}`, () =>
      absentStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap()
    );
  };
  const handleNoShow = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`No-show ${s.studentName ?? ''}`, () =>
      noShowStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap()
    );
  };

  return (
    <SchoolBusPageShell
      title='Student Boarding & Attendance Workspace'
      description='Log student boarding, absences, and drop-off records for active route stops.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Attendance', href: '/school-bus/attendance' },
            { label: tripCode, current: true },
          ]}
        />
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Back link */}
        <div className='flex items-center justify-between'>
          <Button variant='outline' size='sm' className='rounded-full h-8 px-3 font-semibold' asChild>
            <Link href='/school-bus/attendance'>
              <ArrowLeft className='h-3.5 w-3.5 mr-1.5' />
              Back to Attendance list
            </Link>
          </Button>
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

        {/* Basic Trip Info Summary */}
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
              <Button
                variant='outline'
                size='xs'
                className='h-7 rounded-lg text-[10px] font-bold border-blue-200 text-blue-650 hover:bg-blue-50'
                asChild
              >
                <Link href={`/school-bus/trips/${tripId}`}>
                  View Operation Dashboard
                </Link>
              </Button>
            </div>

            <div className='grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs'>
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
                <BusFront className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                <div className='flex flex-col min-w-0'>
                  <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Bus Vehicle</span>
                  <span className='font-mono font-bold text-slate-800 mt-0.5'>{trip.busPlateNumber || '—'}</span>
                </div>
              </div>

              <div className='flex items-start gap-2 min-w-0'>
                <Users className='h-4.5 w-4.5 text-slate-400 shrink-0 mt-0.5' />
                <div className='flex flex-col min-w-0'>
                  <span className='text-slate-400 text-[10px] font-semibold uppercase tracking-wider'>Attendant / Driver</span>
                  <span className='font-bold text-slate-800 truncate mt-0.5'>{trip.attendantName || trip.driverName || '—'}</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Stats and Stop Selector */}
        <div className='grid gap-4 md:grid-cols-3'>
          {/* Stats Summary Panel */}
          {summary && (
            <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm md:col-span-2'>
              <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-3'>Students Attendance Stats</p>
              <div className='grid grid-cols-6 gap-2 text-center divide-x divide-slate-100'>
                <div className='flex flex-col gap-1 min-w-0'>
                  <span className='text-xl font-extrabold text-slate-850'>{summary.totalStudents}</span>
                  <span className='text-[10px] text-slate-400 font-semibold truncate'>Total Students</span>
                </div>
                <div className='flex flex-col gap-1 min-w-0'>
                  <span className='text-xl font-extrabold text-amber-600'>{summary.planned}</span>
                  <span className='text-[10px] text-slate-400 font-semibold truncate'>Planned</span>
                </div>
                <div className='flex flex-col gap-1 min-w-0'>
                  <span className='text-xl font-extrabold text-blue-600'>{summary.boarded}</span>
                  <span className='text-[10px] text-slate-400 font-semibold truncate'>Boarded</span>
                </div>
                <div className='flex flex-col gap-1 min-w-0'>
                  <span className='text-xl font-extrabold text-emerald-600'>{summary.droppedOff}</span>
                  <span className='text-[10px] text-slate-400 font-semibold truncate'>Dropped Off</span>
                </div>
                <div className='flex flex-col gap-1 min-w-0'>
                  <span className='text-xl font-extrabold text-red-500'>{summary.absent + summary.noShow}</span>
                  <span className='text-[10px] text-slate-400 font-semibold truncate'>Absent / No-show</span>
                </div>
                <div className='flex flex-col gap-1 min-w-0'>
                  <span className='text-xl font-extrabold text-slate-405'>{summary.notServed}</span>
                  <span className='text-[10px] text-slate-400 font-semibold truncate'>Not Served</span>
                </div>
              </div>
            </div>
          )}

          {/* Stop Selector Panel */}
          <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-col justify-center'>
            <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2.5'>Active Stop Select</p>
            {manifest?.stops && manifest.stops.length > 0 ? (
              <SchoolBusSelect
                value={selectedStopId ?? ''}
                onChange={(val) => setSelectedStopId(val ? Number(val) : null)}
                placeholder='Select stop...'
                options={manifest.stops
                  .filter((s) => s.locationType !== 'DEPOT')
                  .map((stop) => ({
                    label: `${stop.stopOrder}. ${stop.displayName} (${stop.stopStatus})`,
                    value: stop.routeStopId,
                  }))}
              />
            ) : (
              <span className='text-xs text-slate-400'>No stops available</span>
            )}
          </div>
        </div>

        {/* Student List & Event feed split */}
        <div className='grid gap-5 xl:grid-cols-[1fr_360px] items-start'>
          {/* Student attendance logging list */}
          <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4 min-h-[400px]'>
            {selectedStop ? (
              <>
                <div className='flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-slate-100'>
                  <div>
                    <h3 className='font-bold text-slate-800 text-sm'>
                      Students at {selectedStop.displayName}
                    </h3>
                    <p className='text-[10px] text-slate-400 font-semibold mt-0.5'>
                      Type: {stopTypeLabel(selectedStop)} • Stop Status: {selectedStop.stopStatus}
                    </p>
                  </div>

                  {/* Search box */}
                  <div className='relative w-48 shrink-0'>
                    <Search className='absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400' />
                    <Input
                      type='text'
                      placeholder='Search student...'
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className='h-7 pl-8 text-xs rounded-lg'
                    />
                  </div>
                </div>

                {isDepotStop ? (
                  <div className='py-12 text-center text-slate-450 text-xs font-semibold'>
                    Depot stop — student attendance logging is not supported here.
                  </div>
                ) : studentsAtStop.length === 0 ? (
                  <div className='py-12 text-center text-slate-400 text-xs font-medium'>
                    No students mapped to this stop direction.
                  </div>
                ) : (
                  <div className='divide-y divide-slate-100 max-h-[500px] overflow-y-auto pr-1'>
                    {studentsAtStop.map((student) => {
                      const stStatus = student.status || 'PLANNED';
                      const stNormalized = stStatus.toUpperCase();

                      // Checks
                      const isPlanned = stNormalized === 'PLANNED';
                      const isBoarded = stNormalized === 'BOARDED';
                      const isDone = STUDENT_STATUS_DONE.has(stNormalized);

                      // Action flags based on stop purpose
                      const isPickupStop = selectedStop.stopPurpose === 'PICKUP' || selectedStop.stopPurpose === 'START_TERMINAL';
                      const isDropoffStop = selectedStop.stopPurpose === 'DROPOFF' || selectedStop.stopPurpose === 'END_TERMINAL';

                      const canBoard = isStopActionable && isPickupStop && isPlanned;
                      const canDrop = isStopActionable && isDropoffStop && isBoarded;
                      const canAbsent = isStopActionable && isPickupStop && isPlanned;

                      return (
                        <div
                          key={student.tripStudentId}
                          className='flex items-center justify-between gap-4 py-3 first:pt-0 last:pb-0'
                        >
                          <div className='min-w-0'>
                            <p className='font-bold text-slate-800 text-xs truncate'>{student.studentName}</p>
                            <p className='text-[10px] text-slate-400 mt-0.5 truncate font-semibold'>Code: {student.studentCode || 'N/A'}</p>
                            {student.note && (
                              <p className='text-[9px] text-red-500 bg-red-50/50 border border-red-100/50 rounded px-1.5 py-0.2 mt-1 w-fit'>
                                Note: {student.note}
                              </p>
                            )}
                          </div>

                          <div className='flex items-center gap-2 shrink-0'>
                            {renderFriendlyBadge(stStatus)}

                            {/* Attendance Action buttons */}
                            {tripIsActive && (canBoard || canDrop || canAbsent) && (
                              <div className='flex items-center gap-1.5'>
                                {canBoard && (
                                  <Button
                                    size='sm'
                                    className='h-7 bg-blue-600 hover:bg-blue-700 text-white rounded-full px-3 text-[10px] font-bold shadow-none'
                                    onClick={() => handleBoard(student)}
                                    disabled={isActing}
                                  >
                                    Board
                                  </Button>
                                )}
                                {canDrop && (
                                  <Button
                                    size='sm'
                                    className='h-7 bg-emerald-605 hover:bg-emerald-700 text-white rounded-full px-3 text-[10px] font-bold shadow-none'
                                    onClick={() => handleDropoff(student)}
                                    disabled={isActing}
                                  >
                                    Drop-off
                                  </Button>
                                )}
                                {canAbsent && (
                                  <Button
                                    size='sm'
                                    variant='outline'
                                    className='h-7 rounded-full border-slate-200 px-3 text-[10px] text-slate-600 hover:bg-slate-50 font-bold shadow-none'
                                    onClick={() => handleAbsent(student)}
                                    disabled={isActing}
                                  >
                                    Absent
                                  </Button>
                                )}
                                {canBoard && (
                                  <Button
                                    size='sm'
                                    variant='outline'
                                    className='h-7 rounded-full border-red-250 px-3 text-[10px] text-red-650 hover:bg-red-50 font-bold shadow-none'
                                    onClick={() => handleNoShow(student)}
                                    disabled={isActing}
                                  >
                                    No-show
                                  </Button>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </>
            ) : (
              <SchoolBusEmptyState
                title='No active stop selected'
                description='Please select a stop to manage student attendance logs.'
                icon={MapPin}
              />
            )}
          </div>

          {/* Activity Log Feed */}
          <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4 max-h-[500px] overflow-y-auto'>
            <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider'>Activity Log Feed</p>
            {sortedEvents.length === 0 ? (
              <SchoolBusEmptyState
                title='No events yet'
                description='Attendance events will appear here as they are logged.'
                icon={Bell}
              />
            ) : (
              <div className='space-y-2.5'>
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
                      className={cn('rounded-xl border p-3 shadow-2xs space-y-1.5 transition-all hover:shadow-xs', cardBorder)}
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
      </div>
    </SchoolBusPageShell>
  );
}
