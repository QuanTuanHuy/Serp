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
  useNotServedTripStudentMutation,
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

const tripStatusMap: Record<string, { label: string; className: string }> = {
  CREATED: { label: 'Created', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  PLANNED: { label: 'Planned', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  IN_PROGRESS: { label: 'In progress', className: 'border-blue-200 bg-blue-55 text-blue-700 hover:bg-blue-55' },
  COMPLETED: { label: 'Completed', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  CANCELLED: { label: 'Cancelled', className: 'border-red-200 bg-red-55 text-red-700 hover:bg-red-55' },
  PAUSED: { label: 'Paused', className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50' },
  // Stop statuses:
  PENDING: { label: 'Pending', className: 'border-slate-200 bg-slate-50 text-slate-500 hover:bg-slate-50' },
  ARRIVED: { label: 'Arrived', className: 'border-blue-200 bg-blue-50 text-blue-600 hover:bg-blue-50' },
  BOARDING: { label: 'Boarding', className: 'border-indigo-250 bg-indigo-50 text-indigo-700 hover:bg-indigo-50' },
  DEPARTED: { label: 'Departed', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  SKIPPED: { label: 'Skipped', className: 'border-slate-200 bg-slate-100 text-slate-400 hover:bg-slate-100' },
};

const studentStatusMap: Record<string, { label: string; className: string }> = {
  PLANNED: { label: 'Planned', className: 'border-amber-250 bg-amber-55 text-amber-700 hover:bg-amber-55' },
  BOARDED: { label: 'Boarded', className: 'border-blue-200 bg-blue-55 text-blue-700 hover:bg-blue-55' },
  DROPPED_OFF: { label: 'Dropped off', className: 'border-emerald-250 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  ABSENT: { label: 'Absent', className: 'border-red-200 bg-red-55 text-red-700 hover:bg-red-55' },
  NO_SHOW: { label: 'No-show', className: 'border-red-250 bg-red-50 text-red-650 hover:bg-red-50' },
  NOT_SERVED: { label: 'Not served', className: 'border-slate-200 bg-slate-50 text-slate-400 hover:bg-slate-50' },
  // Event types
  STUDENT_BOARDED: { label: 'Student Boarded', className: 'border-blue-150 bg-blue-50 text-blue-650' },
  STUDENT_ABSENT: { label: 'Student Absent', className: 'border-red-200 bg-red-50 text-red-700' },
  STUDENT_NO_SHOW: { label: 'Student No-show', className: 'border-red-250 bg-red-50 text-red-650' },
  STUDENT_DROPPED_OFF: { label: 'Student Dropped Off', className: 'border-emerald-200 bg-emerald-50 text-emerald-650' },
  STUDENT_NOT_SERVED: { label: 'Student Not Served', className: 'border-slate-200 bg-slate-50 text-slate-400' },
};

const renderTripBadge = (status: string) => {
  const normalized = (status || '').toUpperCase();
  const config = tripStatusMap[normalized] || {
    label: normalized,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge className={cn('rounded-full px-2 py-0.2 text-[10px] font-bold shadow-none border shrink-0 hover:bg-transparent', config.className)}>
      {config.label}
    </Badge>
  );
};

const renderStudentBadge = (status: string) => {
  const normalized = (status || '').toUpperCase();
  const config = studentStatusMap[normalized] || {
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
  const access = useSchoolBusAccess();
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
  const mergedEvents = React.useMemo<any[]>(() => {
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

  const sortedEvents = React.useMemo<any[]>(() => {
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
  const isStopActionable = tripIsActive && stopStatus === 'BOARDING';
  const isDepotStop = selectedStop?.locationType === 'DEPOT';

  // Action flags based on stop direction and purpose
  const isPickupActionStop = selectedStop?.stopPurpose === 'PICKUP';
  const isDropoffActionStop = selectedStop?.stopPurpose === 'DROPOFF';

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
  const [notServedStudent, { isLoading: markingNotServed }] = useNotServedTripStudentMutation();

  const isActing = boarding || droppingOff || markingAbsent || markingNoShow || markingNotServed;

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
  const handleNotServed = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Mark not served for ${s.studentName ?? ''}`, () =>
      notServedStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap()
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
            { label: 'Trip Operations', href: '/school-bus/trips' },
            { label: tripCode, current: true },
          ]}
        />
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Back link */}
        <div className='flex items-center justify-between'>
          <Button variant='outline' size='sm' className='rounded-full h-8 px-3 font-semibold' asChild>
            <Link href='/school-bus/trips'>
              <ArrowLeft className='h-3.5 w-3.5 mr-1.5' />
              Back to Trip Operations
            </Link>
          </Button>
          <div className='flex items-center gap-2'>
            <span className={cn(
              'text-[10px] font-bold px-2 py-0.5 rounded-full border shrink-0',
              wsState === 'Live' ? 'bg-emerald-50 border-emerald-250 text-emerald-700' : 'bg-slate-50 border-slate-200 text-slate-400'
            )}>
              {wsState} feed
            </span>
            {renderTripBadge(tripStatus || '')}
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
                size='sm'
                className='h-7 rounded-lg text-[10px] font-bold border-[#C81E3A]/30 text-[#C81E3A] hover:bg-rose-50 hover:text-[#B31B34]'
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
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm md:col-span-2 flex flex-col justify-between'>
              <div>
                <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-4'>Students Attendance Stats</p>
                <div className='grid grid-cols-3 sm:grid-cols-6 gap-4 text-center divide-x divide-slate-100'>
                  <div className='flex flex-col gap-1.5 min-w-0'>
                    <span className='text-2xl font-extrabold text-slate-800'>{summary.totalStudents}</span>
                    <span className='text-[10px] text-slate-400 font-bold truncate uppercase tracking-wide'>Total Students</span>
                  </div>
                  <div className='flex flex-col gap-1.5 min-w-0 pl-2'>
                    <span className='text-2xl font-extrabold text-amber-600'>{summary.planned}</span>
                    <span className='text-[10px] text-amber-600 font-bold truncate uppercase tracking-wide'>Planned</span>
                  </div>
                  <div className='flex flex-col gap-1.5 min-w-0 pl-2'>
                    <span className='text-2xl font-extrabold text-[#C81E3A]'>{summary.boarded}</span>
                    <span className='text-[10px] text-[#C81E3A] font-bold truncate uppercase tracking-wide'>Boarded</span>
                  </div>
                  <div className='flex flex-col gap-1.5 min-w-0 pl-2'>
                    <span className='text-2xl font-extrabold text-emerald-600'>{summary.droppedOff}</span>
                    <span className='text-[10px] text-emerald-600 font-bold truncate uppercase tracking-wide'>Dropped Off</span>
                  </div>
                  <div className='flex flex-col gap-1.5 min-w-0 pl-2'>
                    <span className='text-2xl font-extrabold text-rose-600'>{summary.absent + summary.noShow}</span>
                    <span className='text-[10px] text-rose-600 font-bold truncate uppercase tracking-wide'>Absent / NoShow</span>
                  </div>
                  <div className='flex flex-col gap-1.5 min-w-0 pl-2'>
                    <span className='text-2xl font-extrabold text-slate-500'>{summary.notServed}</span>
                    <span className='text-[10px] text-slate-500 font-bold truncate uppercase tracking-wide'>Not Served</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Active Stop Panel */}
          <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm flex flex-col justify-between gap-4'>
            <div>
              <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-2.5'>Active Stop Workspace</p>
              {manifest?.stops && manifest.stops.length > 0 ? (
                <div className='space-y-3.5'>
                  <SchoolBusSelect
                    value={selectedStopId ?? ''}
                    onChange={(val) => setSelectedStopId(val ? Number(val) : null)}
                    placeholder='Select stop...'
                    options={manifest.stops.map((stop) => {
                      const stopType = stop.locationType || '';
                      const purpose = stop.stopPurpose || '';
                      let roleText = '';
                      if (stopType === 'DEPOT') {
                        roleText = 'Depot - no attendance';
                      } else if (purpose === 'PICKUP') {
                        roleText = 'Pickup - boarding attendance';
                      } else if (stopType === 'SCHOOL') {
                        roleText = isOutbound ? 'School - drop-off attendance' : 'School - boarding attendance';
                      } else if (purpose === 'DROPOFF') {
                        roleText = 'Drop-off - drop-off attendance';
                      } else {
                        roleText = 'Stop';
                      }
                      return {
                        label: `[${roleText}] ${stop.stopOrder}. ${stop.displayName} (${stop.stopStatus})`,
                        value: stop.routeStopId,
                      };
                    })}
                  />
                  
                  {selectedStop && (
                    <div className='border border-slate-100 rounded-xl p-3 bg-slate-50/50 space-y-2 text-xs'>
                      <div className='flex items-center justify-between'>
                        <span className='text-slate-400 font-semibold'>Stop type</span>
                        <span className='font-bold text-slate-700'>{stopTypeLabel(selectedStop)}</span>
                      </div>
                      <div className='flex items-center justify-between'>
                        <span className='text-slate-400 font-semibold'>Status</span>
                        {renderTripBadge(selectedStop.stopStatus)}
                      </div>
                      <div className='flex items-center justify-between'>
                        <span className='text-slate-400 font-semibold'>Planned time</span>
                        <span className='font-bold text-slate-700'>
                          {selectedStop.plannedArrivalTime ? formatDateTime(selectedStop.plannedArrivalTime).split(' ')[1] : '—'}
                        </span>
                      </div>
                      {selectedStop.actualArrivalTime && (
                        <div className='flex items-center justify-between text-emerald-700'>
                          <span className='font-semibold'>Actual arrival</span>
                          <span className='font-bold'>{formatDateTime(selectedStop.actualArrivalTime).split(' ')[1]}</span>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <span className='text-xs text-slate-400'>No stops available</span>
              )}
            </div>
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

                {/* Status Warning Messages */}
                {tripStatus !== 'IN_PROGRESS' && tripStatus !== 'COMPLETED' && tripStatus !== 'CANCELLED' ? (
                  <div className='bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded-xl text-xs font-semibold'>
                    Start the trip before logging attendance.
                  </div>
                ) : tripStatus === 'COMPLETED' || tripStatus === 'CANCELLED' ? (
                  <div className='bg-slate-50 border border-slate-200 text-slate-650 px-4 py-3 rounded-xl text-xs font-semibold'>
                    This trip is completed or cancelled. Attendance records are locked.
                  </div>
                ) : selectedStop.stopStatus === 'PENDING' ? (
                  <div className='bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded-xl text-xs font-semibold'>
                    Arrive at this stop before logging attendance.
                  </div>
                ) : selectedStop.stopStatus === 'ARRIVED' ? (
                  <div className='bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded-xl text-xs font-semibold'>
                    Start boarding/dropoff at this stop before marking attendance.
                  </div>
                ) : selectedStop.stopStatus === 'DEPARTED' || selectedStop.stopStatus === 'SKIPPED' ? (
                  <div className='bg-slate-50 border border-slate-200 text-slate-650 px-4 py-3 rounded-xl text-xs font-semibold'>
                    This stop has been departed or skipped. Attendance records are locked.
                  </div>
                ) : isStopActionable && isPickupActionStop ? (
                  <div className='bg-emerald-50 border border-emerald-250 text-emerald-800 px-4 py-3 rounded-xl text-xs font-semibold'>
                    This stop is in boarding mode. Mark students as boarded, absent, or no-show.
                  </div>
                ) : isStopActionable && isDropoffActionStop ? (
                  <div className='bg-emerald-50 border border-emerald-250 text-emerald-800 px-4 py-3 rounded-xl text-xs font-semibold'>
                    This stop is in drop-off mode. Mark students as dropped-off or not served.
                  </div>
                ) : null}

                {isDepotStop ? (
                  <div className='py-12 text-center text-slate-450 text-xs font-semibold'>
                    Depot stop — student attendance logging is not supported here.
                  </div>
                ) : studentsAtStop.length === 0 ? (
                  <div className='py-12 text-center text-slate-400 text-xs font-medium'>
                    No students mapped to this stop direction.
                  </div>
                ) : (
                  <div className='space-y-3 max-h-[500px] overflow-y-auto pr-1'>
                    {studentsAtStop.map((student) => {
                      const stStatus = student.status || 'PLANNED';
                      const stNormalized = stStatus.toUpperCase();

                      // Checks
                      const isPlanned = stNormalized === 'PLANNED';
                      const isBoarded = stNormalized === 'BOARDED';

                      // Action flags based on stop direction and purpose
                      const canBoard = isPickupActionStop && isPlanned;
                      const canDrop = isDropoffActionStop && isBoarded;
                      const canAbsent = isPickupActionStop && isPlanned;
                      const canNotServed = isDropoffActionStop && isBoarded;

                      const lastEvent = mergedEvents.find(
                        (e: any) =>
                          (e.studentCode && e.studentCode === student.studentCode) ||
                          e.studentName === student.studentName
                      );

                      const lastEventText = (item: any) => {
                        const type = (item.eventType || item.attendanceType || '').toUpperCase();
                        const time = item.recordedAt ? formatDateTime(item.recordedAt).split(' ')[1] : '';
                        const action = type.includes('BOARD')
                          ? 'BOARDED'
                          : type.includes('DROP')
                          ? 'DROPPED OFF'
                          : type.includes('ABSENT')
                          ? 'ABSENT'
                          : type.includes('NO_SHOW')
                          ? 'NO SHOW'
                          : type.includes('NOT_SERVED')
                          ? 'NOT SERVED'
                          : type;
                        return `${action} at ${time}`;
                      };

                      return (
                        <div
                          key={student.tripStudentId}
                          className='bg-white border border-slate-150 rounded-xl p-4 shadow-2xs hover:shadow-xs transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4'
                        >
                          <div className='space-y-1.5 min-w-0'>
                            <div className='flex items-center gap-2 flex-wrap'>
                              <p className='font-bold text-slate-800 text-sm truncate'>{student.studentName}</p>
                              {renderStudentBadge(stStatus)}
                            </div>
                            <div className='flex flex-wrap items-center gap-x-3 gap-y-1 text-[11px] text-slate-400 font-semibold'>
                              <span>Code: {student.studentCode || 'N/A'}</span>
                              <span>•</span>
                              <span>Assigned Stop: {selectedStop.displayName}</span>
                            </div>
                            {lastEvent && (
                              <p className='text-[10px] text-slate-500 italic mt-1 flex items-center gap-1 bg-slate-50 w-fit px-2 py-0.5 rounded border border-slate-100'>
                                <Clock className='h-3 w-3 text-slate-400 shrink-0' />
                                Last event: {lastEventText(lastEvent)}
                              </p>
                            )}
                            {student.note && (
                              <p className='text-[10px] text-red-500 bg-red-50/50 border border-red-100/50 rounded px-2 py-0.5 mt-1.5 w-fit font-medium'>
                                Note: {student.note}
                              </p>
                            )}
                          </div>

                          {/* Attendance Action buttons — only for users who can mark attendance */}
                          {tripIsActive && isStopActionable && access.canMarkAttendance && (
                            <div className='flex flex-wrap items-center gap-2 shrink-0 self-end sm:self-center'>
                              {canBoard && (
                                <Button
                                  size='sm'
                                  className='h-8 bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-4 text-xs font-semibold shadow-none border-0'
                                  onClick={() => handleBoard(student)}
                                  disabled={isActing}
                                >
                                  Board
                                </Button>
                              )}
                              {canDrop && (
                                <Button
                                  size='sm'
                                  className='h-8 bg-emerald-600 hover:bg-emerald-700 text-white rounded-full px-4 text-xs font-semibold shadow-none border-0'
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
                                  className='h-8 rounded-full border-amber-250 px-4 text-xs text-amber-700 hover:bg-amber-50 font-semibold shadow-none'
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
                                  className='h-8 rounded-full border-red-250 px-4 text-xs text-red-650 hover:bg-red-50 font-semibold shadow-none'
                                  onClick={() => handleNoShow(student)}
                                  disabled={isActing}
                                >
                                  No-show
                                </Button>
                              )}
                              {canNotServed && (
                                <Button
                                  size='sm'
                                  variant='outline'
                                  className='h-8 rounded-full border-slate-300 px-4 text-xs text-slate-600 hover:bg-slate-50 font-semibold shadow-none'
                                  onClick={() => handleNotServed(student)}
                                  disabled={isActing}
                                >
                                  Not Served
                                </Button>
                              )}
                            </div>
                          )}
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
            <div className='flex items-center justify-between pb-2 border-b border-slate-100'>
              <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider'>Activity Log Feed</p>
              {wsState === 'Live' && (
                <span className='flex h-2 w-2 relative'>
                  <span className='animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75'></span>
                  <span className='relative inline-flex rounded-full h-2 w-2 bg-emerald-500'></span>
                </span>
              )}
            </div>

            {sortedEvents.length === 0 ? (
              <div className='py-8 text-center text-slate-450 text-xs font-semibold'>
                No attendance logs found for this trip.
              </div>
            ) : (
              <div className='relative pl-3.5 space-y-4 before:absolute before:left-[4px] before:top-2 before:bottom-2 before:w-[1.5px] before:bg-slate-100'>
                {sortedEvents.map((item: any) => {
                  const type = (item.eventType || item.attendanceType || '').toUpperCase();
                  const isBoard = type.includes('BOARD');
                  const isDrop = type.includes('DROP');
                  const isAbsent = type.includes('ABSENT') || type.includes('NO_SHOW');
                  const isNotServed = type.includes('NOT_SERVED');

                  const dotColor = isBoard
                    ? 'bg-blue-500'
                    : isDrop
                    ? 'bg-emerald-500'
                    : isAbsent
                    ? 'bg-red-500'
                    : isNotServed
                    ? 'bg-slate-400'
                    : 'bg-slate-300';

                  const actionLabel = isBoard
                    ? 'BOARDED'
                    : isDrop
                    ? 'DROPPED OFF'
                    : type.includes('ABSENT')
                    ? 'ABSENT'
                    : type.includes('NO_SHOW')
                    ? 'NO SHOW'
                    : 'NOT SERVED';

                  const timestamp = item.recordedAt ? formatDateTime(item.recordedAt).split(' ')[1] : '';

                  return (
                    <div key={item.id} className='relative text-xs'>
                      {/* Event dot */}
                      <span className={cn('absolute -left-[17px] top-1.5 h-2 w-2 rounded-full border border-white ring-2 ring-slate-50', dotColor)} />
                      
                      <div className='space-y-0.5 leading-relaxed'>
                        <span className='font-bold text-slate-500 mr-1.5'>{timestamp}</span>
                        <span className='text-slate-700 font-semibold'>
                          Student <span className='font-bold text-slate-900'>{item.studentName}</span> marked as <span className={cn('font-bold', isBoard ? 'text-blue-600' : isDrop ? 'text-emerald-600' : isAbsent ? 'text-red-600' : 'text-slate-500')}>{actionLabel}</span>.
                        </span>
                        {item.notes && (
                          <p className='text-[10px] text-slate-400 italic bg-slate-50 rounded px-1.5 py-0.2 mt-0.5 w-fit border border-slate-100/50'>
                            Note: {item.notes}
                          </p>
                        )}
                      </div>
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
