'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CalendarDays,
  CheckCircle2,
  ClipboardCheck,
  Play,
  SkipForward,
  User,
  XCircle,
  Warehouse,
  GraduationCap,
  BusFront,
  Users,
  MapPin,
  Route,
  Info,
  Clock,
  ArrowLeft,
  Bell,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  useAbsentTripStudentMutation,
  useArriveTripStopMutation,
  useBoardTripStudentMutation,
  useCancelTripMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useDropoffTripStudentMutation,
  useGetTripAttendanceManifestQuery,
  useGetTripAttendanceQuery,
  useGetTripAttendanceSummaryQuery,
  useGetTripsQuery,
  useNoShowTripStudentMutation,
  useSkipTripStopMutation,
  useStartTripMutation,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { Button, Input, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate, formatDateTime, getPageItems } from '../utils';
import type { TripAttendanceStopItem, TripAttendanceStudentItem } from '../types';

// ─── helpers ──────────────────────────────────────────────────────────────────

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

function stopActionHint(stop: TripAttendanceStopItem, isOutbound: boolean): string {
  const { stopPurpose, locationType } = stop;
  if (locationType === 'DEPOT') return 'Depot — no attendance actions';
  if (stopPurpose === 'PICKUP' && isOutbound) return 'Board students at this stop';
  if (stopPurpose === 'START_TERMINAL' && locationType === 'SCHOOL' && !isOutbound)
    return 'Board students (school start)';
  if (stopPurpose === 'END_TERMINAL' && locationType === 'SCHOOL' && isOutbound)
    return 'Drop off boarded students (school end)';
  if (stopPurpose === 'DROPOFF' && !isOutbound) return 'Drop off students at this stop';
  return '';
}

const statusMap: Record<string, { label: string; className: string }> = {
  CREATED: { label: 'Created', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  IN_PROGRESS: { label: 'In progress', className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50' },
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

const formatLocationType = (type?: string | null) => {
  if (!type) return '';
  const t = type.toUpperCase();
  if (t === 'SCHOOL') return 'School';
  if (t === 'DEPOT') return 'Depot';
  if (t === 'PICKUP') return 'Pickup';
  if (t === 'DROPOFF') return 'Dropoff';
  return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
};

const getFriendlyDirection = (dir?: string | null) => {
  if (dir === 'RETURN') return 'Return';
  if (dir === 'OUTBOUND') return 'Outbound';
  if (dir === 'ROUND_TRIP') return 'Round trip';
  return dir || '';
};

const STUDENT_STATUS_DONE = new Set(['ABSENT', 'NO_SHOW', 'DROPPED_OFF', 'NOT_SERVED']);

// ─── page ─────────────────────────────────────────────────────────────────────

interface SchoolBusTripOperationsPageProps {
  tripId: number;
}

export function SchoolBusTripOperationsPage({ tripId }: SchoolBusTripOperationsPageProps) {
  // ── state ──────────────────────────────────────────────────────────────────
  const [selectedStopId, setSelectedStopId] = React.useState<number | null>(null);
  const [showSkipForm, setShowSkipForm] = React.useState(false);
  const [skipReason, setSkipReason] = React.useState('');
  const [showCancelForm, setShowCancelForm] = React.useState(false);
  const [cancelReason, setCancelReason] = React.useState('');

  // ── queries ────────────────────────────────────────────────────────────────
  const tripsQuery = useGetTripsQuery({ page: 0, size: 50, sortBy: 'serviceDate', sortDirection: 'DESC' });
  const { data: manifestData, isLoading: manifestLoading } =
    useGetTripAttendanceManifestQuery(tripId);
  const { data: summaryData } = useGetTripAttendanceSummaryQuery(tripId);
  const { data: eventsData } = useGetTripAttendanceQuery(tripId);

  // ── derived ────────────────────────────────────────────────────────────────
  const allTrips = getPageItems(tripsQuery.data?.data);
  const trip = allTrips.find((t) => t.id === tripId) ?? null;

  const manifest = manifestData?.data ?? null;
  const summary = summaryData?.data ?? manifest?.summary ?? null;
  const events = eventsData?.data ?? [];

  const tripStatus = manifest?.tripStatus ?? trip?.status ?? null;
  const tripIsActive = tripStatus === 'IN_PROGRESS';
  const tripIsCompleted = tripStatus === 'COMPLETED';
  const tripIsCancelled = tripStatus === 'CANCELLED';
  const isOutbound = (manifest?.routeDirection ?? trip?.routeDirection) === 'OUTBOUND';
  const tripCode = manifest?.tripCode ?? trip?.tripCode ?? `Trip #${tripId}`;
  const routeCode = manifest?.routeCode ?? trip?.routeCode ?? '';
  const routeName = (manifest as any)?.routeName ?? trip?.routeName ?? '';

  // ── auto-advance to current (first non-done) stop ─────────────────────────
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [manifest]);

  // ── current stop ──────────────────────────────────────────────────────────
  const currentStop = manifest?.stops?.find((s) => s.routeStopId === selectedStopId) ?? null;
  const stopStatus = currentStop?.stopStatus ?? null;
  const isStopActionable = tripIsActive && (stopStatus === 'ARRIVED' || stopStatus === 'BOARDING');
  const isDepotStop = currentStop?.locationType === 'DEPOT';

  // ── students at current stop ───────────────────────────────────────────────
  const studentsAtStop = React.useMemo<TripAttendanceStudentItem[]>(() => {
    if (!manifest || !currentStop) return [];
    const { stopPurpose, locationType, routeStopId } = currentStop;
    if (locationType === 'DEPOT') return [];
    if (stopPurpose === 'PICKUP')
      return manifest.students.filter((s) => s.pickupStopId === routeStopId);
    if (stopPurpose === 'DROPOFF')
      return manifest.students.filter((s) => s.dropoffStopId === routeStopId);
    if (stopPurpose === 'END_TERMINAL' && locationType === 'SCHOOL' && isOutbound)
      return manifest.students.filter(
        (s) => s.status === 'BOARDED' || s.status === 'DROPPED_OFF',
      );
    if (stopPurpose === 'START_TERMINAL' && locationType === 'SCHOOL' && !isOutbound)
      return manifest.students.filter((s) => s.status === 'PLANNED');
    return [];
  }, [manifest, currentStop, isOutbound]);

  // ── operation summary ─────────────────────────────────────────────────────
  const opSummary = React.useMemo(() => {
    if (!manifest?.stops?.length) return null;
    const stops = manifest.stops;
    const total = stops.length;
    const done = stops.filter(
      (s) => s.stopStatus === 'DEPARTED' || s.stopStatus === 'SKIPPED',
    ).length;
    const currentSt =
      stops.find((s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED') ?? null;
    const remaining = Math.max(0, total - done - (currentSt ? 1 : 0));
    return { total, done, current: currentSt, remaining };
  }, [manifest]);

  // ── sorted events ─────────────────────────────────────────────────────────
  const sortedEvents = React.useMemo(() => {
    return [...events].sort(
      (a: any, b: any) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
    );
  }, [events]);

  // ── mutations ──────────────────────────────────────────────────────────────
  const [startTrip, { isLoading: starting }] = useStartTripMutation();
  const [completeTrip, { isLoading: completing }] = useCompleteTripMutation();
  const [cancelTrip, { isLoading: cancelling }] = useCancelTripMutation();
  const [arriveStop, { isLoading: arriving }] = useArriveTripStopMutation();
  const [departStop, { isLoading: departing }] = useDepartTripStopMutation();
  const [skipStop, { isLoading: skipping }] = useSkipTripStopMutation();
  const [boardStudent, { isLoading: boarding }] = useBoardTripStudentMutation();
  const [dropoffStudent, { isLoading: droppingOff }] = useDropoffTripStudentMutation();
  const [absentStudent, { isLoading: markingAbsent }] = useAbsentTripStudentMutation();
  const [noShowStudent, { isLoading: markingNoShow }] = useNoShowTripStudentMutation();

  const isActing =
    starting || completing || cancelling || arriving || departing ||
    skipping || boarding || droppingOff || markingAbsent || markingNoShow;

  const act = async (label: string, fn: () => Promise<unknown>) => {
    try {
      await fn();
      toast.success(`${label} completed`);
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message ?? `${label} failed`);
    }
  };

  // ── trip lifecycle ─────────────────────────────────────────────────────────
  const handleStart = () => act('Start trip', () => startTrip(tripId).unwrap());
  const handleComplete = () => act('Complete trip', () => completeTrip({ id: tripId }).unwrap());
  const handleCancel = () => {
    if (!cancelReason.trim()) return;
    act('Cancel trip', () =>
      cancelTrip({ id: tripId, body: { reason: cancelReason } }).unwrap(),
    );
    setShowCancelForm(false);
    setCancelReason('');
  };

  // ── stop operations ────────────────────────────────────────────────────────
  const handleArrive = () => {
    if (!selectedStopId) return;
    act('Arrive stop', () => arriveStop({ tripId, routeStopId: selectedStopId }).unwrap());
  };
  const handleDepart = () => {
    if (!selectedStopId) return;
    act('Depart stop', () => departStop({ tripId, routeStopId: selectedStopId }).unwrap());
  };
  const handleSkip = () => {
    if (!selectedStopId || !skipReason.trim()) return;
    act('Skip stop', () =>
      skipStop({ tripId, routeStopId: selectedStopId, reason: skipReason }).unwrap(),
    );
    setShowSkipForm(false);
    setSkipReason('');
  };

  // ── attendance actions ─────────────────────────────────────────────────────
  const handleBoard = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Board ${s.studentName ?? ''}`, () =>
      boardStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap(),
    );
  };
  const handleDropoff = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Drop-off ${s.studentName ?? ''}`, () =>
      dropoffStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap(),
    );
  };
  const handleAbsent = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`Absent ${s.studentName ?? ''}`, () =>
      absentStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap(),
    );
  };
  const handleNoShow = (s: TripAttendanceStudentItem) => {
    if (!selectedStopId) return;
    act(`No-show ${s.studentName ?? ''}`, () =>
      noShowStudent({ tripId, body: { routeStopId: selectedStopId, studentId: s.studentId } }).unwrap(),
    );
  };

  const firstNonDoneIndex = React.useMemo(() => {
    if (!manifest?.stops) return -1;
    return manifest.stops.findIndex(
      (s: any) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED'
    );
  }, [manifest]);

  return (
    <SchoolBusPageShell
      title={tripCode}
      description='Manage student attendance and execute real-time terminal and stop sequence operations.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Trip Operations', href: '/school-bus/attendance' },
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
              <Link href='/school-bus/attendance'>
                <ArrowLeft className='h-3.5 w-3.5 mr-1.5' />
                Back to Operations list
              </Link>
            </Button>
          </div>

          {tripIsCompleted && (
            <div className='flex items-center gap-2.5 bg-emerald-50 border border-emerald-100 text-emerald-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
              <CheckCircle2 className='h-4.5 w-4.5 text-emerald-600 shrink-0' />
              <span>Trip completed — operations and attendance logs are locked in read-only mode.</span>
            </div>
          )}

          {tripIsCancelled && (
            <div className='flex items-center gap-2.5 bg-red-50 border border-red-100 text-red-800 px-4 py-3 rounded-2xl text-xs font-semibold shadow-xs'>
              <XCircle className='h-4.5 w-4.5 text-red-600 shrink-0' />
              <span>Trip cancelled — operations are disabled. Reason: {(manifest as any)?.cancellationReason || trip?.cancellationReason || 'N/A'}</span>
            </div>
          )}
        </div>

        {/* B: Trip Summary Card */}
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
              {renderFriendlyBadge(tripStatus || '')}
            </div>

            <div className='grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-y-4 gap-x-6 text-xs'>
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

              {/* Trip Cancel operations */}
              {!tripIsCompleted && !tripIsCancelled && (
                <div className='flex flex-col justify-center sm:col-span-2 lg:col-span-1'>
                  {showCancelForm ? (
                    <div className='flex items-center gap-1.5'>
                      <Input
                        value={cancelReason}
                        onChange={(e) => setCancelReason(e.target.value)}
                        placeholder='Reason…'
                        className='h-7 text-xs rounded-lg px-2 w-28 bg-slate-50'
                      />
                      <Button
                        size='sm'
                        className='h-7 text-[10px] rounded-lg bg-red-650 hover:bg-red-700 text-white font-bold px-2'
                        onClick={handleCancel}
                        disabled={!cancelReason.trim() || isActing}
                      >
                        Confirm
                      </Button>
                      <Button
                        size='sm'
                        variant='ghost'
                        className='h-7 text-[10px] px-1 rounded-lg'
                        onClick={() => setShowCancelForm(false)}
                      >
                        Back
                      </Button>
                    </div>
                  ) : (
                    <Button
                      size='sm'
                      variant='outline'
                      className='h-8 rounded-full border-red-200 text-red-600 hover:bg-red-50 text-xs font-semibold px-3 w-fit'
                      onClick={() => setShowCancelForm(true)}
                      disabled={isActing}
                    >
                      <XCircle size={13} className='mr-1.5 shrink-0' />
                      Cancel Trip
                    </Button>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {/* C: Stats cards rows */}
        {(summary || opSummary) && (
          <div className='grid gap-4 sm:grid-cols-2'>
            {summary && (
              <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm'>
                <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-3'>Students Attendance Stats</p>
                <div className='grid grid-cols-7 gap-1 text-center divide-x divide-slate-100'>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-slate-800'>{summary.totalStudents}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Total</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-amber-600'>{summary.planned}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Planned</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-blue-600'>{summary.boarded}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Boarded</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-emerald-600'>{summary.droppedOff}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Dropped</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-red-500'>{summary.absent}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Absent</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-red-650'>{summary.noShow}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>No-show</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-slate-400'>{summary.notServed}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Not served</span>
                  </div>
                </div>
              </div>
            )}
            {opSummary && (
              <div className='bg-white border border-slate-200 rounded-2xl p-4 shadow-sm'>
                <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider mb-3'>Stops Operations Progress</p>
                <div className='grid grid-cols-4 gap-1 text-center divide-x divide-slate-100'>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-slate-800'>{opSummary.total}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Total</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-emerald-600'>{opSummary.done}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Done</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-amber-600'>
                      {opSummary.current ? `#${(opSummary.current.stopOrder ?? 0) + 1}` : '—'}
                    </span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Current</span>
                  </div>
                  <div className='flex flex-col gap-1 min-w-0'>
                    <span className='text-lg font-extrabold text-slate-500'>{opSummary.remaining}</span>
                    <span className='text-[10px] text-slate-400 font-medium truncate'>Remaining</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* D+E+G: Main workspace */}
        {manifestLoading ? (
          <SchoolBusEmptyState title='Loading operations workspace...' description='Fetching manifest, stops, and student logs.' icon={ClipboardCheck} />
        ) : !manifest || manifest.stops.length === 0 ? (
          <SchoolBusEmptyState
            title='No stop data'
            description={
              tripIsActive
                ? 'Stop logs not yet generated — try refreshing.'
                : 'Start the trip to generate stop logs.'
            }
            icon={ClipboardCheck}
          />
        ) : (
          <div className='grid gap-5 xl:grid-cols-[270px_1fr_310px] items-start'>
            {/* ── D: Left Stop timeline ── */}
            <div className='flex flex-col gap-2.5'>
              <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider px-1'>Stop Timeline</p>
              <div className='relative pl-4 space-y-4 before:absolute before:left-[19px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100 xl:max-h-[calc(100vh-320px)] xl:overflow-y-auto xl:pr-2'>
                {manifest.stops.map((stop, idx) => {
                  const isSelected = stop.routeStopId === selectedStopId;
                  const isDone = stop.stopStatus === 'DEPARTED' || stop.stopStatus === 'SKIPPED';
                  const locked = (tripStatus !== 'COMPLETED' && tripStatus !== 'CANCELLED') && idx > firstNonDoneIndex;

                  const StopIcon = stop.locationType === 'SCHOOL'
                    ? GraduationCap
                    : stop.locationType === 'DEPOT'
                    ? Warehouse
                    : MapPin;

                  const iconColor = stop.locationType === 'SCHOOL'
                    ? 'text-red-500 bg-red-55 border-red-100'
                    : stop.locationType === 'DEPOT'
                    ? 'text-orange-500 bg-orange-55 border-orange-100'
                    : stop.stopPurpose === 'PICKUP'
                    ? 'text-blue-500 bg-blue-55 border-blue-100'
                    : 'text-emerald-500 bg-emerald-55 border-emerald-100';

                  return (
                    <div key={stop.routeStopId} className='relative flex items-start gap-3 group'>
                      {/* Timeline Node Icon/Circle */}
                      <div className='relative z-10 flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-white border border-slate-200 shadow-sm'>
                        <span className={cn(
                          'flex h-7 w-7 items-center justify-center rounded-full text-xs font-extrabold transition-all',
                          isSelected
                            ? 'bg-[#C81E3A] text-white shadow-sm ring-4 ring-red-500/10'
                            : isDone
                            ? 'bg-emerald-50 text-emerald-700'
                            : locked
                            ? 'bg-slate-100 text-slate-400'
                            : 'bg-blue-50 text-blue-600'
                        )}>
                          {idx + 1}
                        </span>
                      </div>

                      {/* Timeline Stop Card */}
                      <button
                        onClick={() => {
                          setSelectedStopId(stop.routeStopId);
                          setShowSkipForm(false);
                        }}
                        disabled={locked}
                        className={cn(
                          'flex-1 text-left rounded-xl border p-3 transition-all outline-none',
                          isSelected
                            ? 'border-blue-300 bg-blue-50/20 shadow-sm ring-1 ring-blue-300/30'
                            : locked
                            ? 'border-transparent bg-slate-50/40 opacity-50 cursor-not-allowed'
                            : 'border-slate-200 bg-white hover:bg-slate-50 hover:border-slate-350 shadow-xs',
                          isDone && 'opacity-65'
                        )}
                      >
                        <div className='flex items-start justify-between gap-1.5 min-w-0'>
                          <div className='min-w-0 flex-1'>
                            <div className='flex items-center gap-1.5'>
                              <div className={cn('flex h-4.5 w-4.5 items-center justify-center rounded border shrink-0', iconColor)}>
                                <StopIcon className='h-3 w-3' />
                              </div>
                              <p className='truncate text-xs font-bold text-slate-900'>
                                {stop.displayName ?? stop.stopName ?? `Stop ${stop.stopOrder}`}
                              </p>
                            </div>
                            <p className='truncate text-[10px] text-slate-400 mt-1.5 font-medium'>
                              {stopTypeLabel(stop)}
                            </p>
                          </div>
                          {locked ? (
                            <span className='rounded px-1.5 py-0.2 text-[9px] font-bold border bg-slate-50 border-slate-200 text-slate-400 uppercase tracking-wider shrink-0'>
                              Locked
                            </span>
                          ) : (
                            renderFriendlyBadge(stop.stopStatus || '')
                          )}
                        </div>

                        {(stop.plannedBoardingCount > 0 || stop.plannedDropoffCount > 0) && !locked && (
                          <div className='mt-2.5 flex flex-wrap gap-2 text-[10px] text-slate-500 font-semibold border-t border-slate-100/50 pt-2'>
                            {stop.plannedBoardingCount > 0 && (
                              <span className='flex items-center gap-0.5 text-blue-600 bg-blue-50/50 border border-blue-100/30 rounded px-1.5 py-0.2'>
                                ↑ {stop.actualBoardedCount}/{stop.plannedBoardingCount} Board
                              </span>
                            )}
                            {stop.plannedDropoffCount > 0 && (
                              <span className='flex items-center gap-0.5 text-emerald-600 bg-emerald-50/50 border border-emerald-100/30 rounded px-1.5 py-0.2'>
                                ↓ {stop.actualDroppedCount}/{stop.plannedDropoffCount} Drop
                              </span>
                            )}
                          </div>
                        )}
                      </button>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* ── E+F: Center Active Stop Workbench ── */}
            <div className='space-y-4 min-w-0'>
              {!currentStop ? (
                <SchoolBusEmptyState
                  title='Select a stop'
                  description='Click a stop in the timeline to operate on it.'
                  icon={ClipboardCheck}
                />
              ) : (
                <>
                  {/* Stop header + operations */}
                  <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
                    <div className='flex flex-wrap items-start justify-between gap-3'>
                      <div className='flex items-center gap-2.5 min-w-0'>
                        <span className={cn(
                          'flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-extrabold',
                          isDepotStop
                            ? 'bg-slate-100 text-slate-650'
                            : currentStop.locationType === 'SCHOOL'
                            ? 'bg-indigo-50 text-indigo-700 border border-indigo-100'
                            : 'bg-blue-50 text-blue-700 border border-blue-100',
                        )}>
                          {(currentStop.stopOrder ?? 0) + 1}
                        </span>
                        <div className='min-w-0'>
                          <p className='font-bold text-slate-900 text-sm truncate'>
                            {currentStop.displayName ?? currentStop.stopName ?? `Stop ${currentStop.stopOrder}`}
                          </p>
                          <p className='text-xs text-slate-400 truncate mt-0.5'>{stopTypeLabel(currentStop)}</p>
                        </div>
                      </div>
                      {renderFriendlyBadge(stopStatus || '')}
                    </div>

                    {stopActionHint(currentStop, isOutbound) && (
                      <p className='text-xs font-semibold text-slate-600 bg-slate-50 border border-slate-100 rounded-xl px-3.5 py-2.5'>
                        {stopActionHint(currentStop, isOutbound)}
                      </p>
                    )}

                    {/* Next backend operation controls area inside center panel */}
                    <div className='bg-slate-50/70 border border-slate-200/80 rounded-2xl p-4 space-y-3'>
                      <div className='flex items-center justify-between text-[10px] font-extrabold text-slate-400 uppercase tracking-wider'>
                        <span>Next Backend Operation</span>
                        <span>{tripIsActive ? 'Active' : 'Locked'}</span>
                      </div>

                      <div className='flex flex-wrap gap-2 items-center justify-between bg-white border border-slate-150 rounded-xl p-3.5'>
                        {/* Status descriptions */}
                        <div className='flex items-start gap-2.5 flex-1 min-w-[200px]'>
                          <Info className='h-4.5 w-4.5 text-blue-500 shrink-0 mt-0.5' />
                          <div className='flex flex-col text-xs gap-0.5'>
                            {tripIsCompleted || tripIsCancelled ? (
                              <>
                                <span className='font-bold text-slate-700'>Operations are disabled</span>
                                <span className='text-slate-400'>Trip status is {tripStatus?.toLowerCase()}. No further actions are allowed.</span>
                              </>
                            ) : !tripIsActive ? (
                              <>
                                <span className='font-bold text-slate-700'>Trip not started</span>
                                <span className='text-slate-400'>Start the trip to begin terminal and stop execution operations.</span>
                              </>
                            ) : currentStop.routeStopId !== selectedStopId ? (
                              <>
                                <span className='font-bold text-slate-700'>Inactive stop selected</span>
                                <span className='text-slate-400'>Only the next pending stop can be processed. Select the active stop to operate.</span>
                              </>
                            ) : stopStatus === 'PENDING' ? (
                              <>
                                <span className='font-bold text-slate-700'>Mark arrival</span>
                                <span className='text-slate-400'>Arrive at this stop to verify student boarding and dropoff lists.</span>
                              </>
                            ) : stopStatus === 'ARRIVED' || stopStatus === 'BOARDING' ? (
                              <>
                                <span className='font-bold text-slate-700'>Mark departure</span>
                                <span className='text-slate-400'>Depart this stop to lock attendance logs and advance the bus timeline.</span>
                              </>
                            ) : (
                              <>
                                <span className='font-bold text-slate-700'>Stop completed</span>
                                <span className='text-slate-400'>All operations for this stop are closed.</span>
                              </>
                            )}
                          </div>
                        </div>

                        {/* Interactive Buttons */}
                        <div className='flex items-center gap-2 shrink-0'>
                          {/* Start Trip (IfCREATED) */}
                          {!tripIsActive && !tripIsCompleted && !tripIsCancelled && (
                            <Button
                              size='sm'
                              className='rounded-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold h-8 px-4 border-0'
                              onClick={handleStart}
                              disabled={isActing}
                            >
                              <Play size={13} className='mr-1.5' />
                              Start Trip
                            </Button>
                          )}

                          {/* Stop Arrive / Depart / Skip */}
                          {tripIsActive && currentStop.routeStopId === selectedStopId && (
                            <>
                              {stopStatus === 'PENDING' && (
                                <Button
                                  size='sm'
                                  className='rounded-full bg-amber-500 hover:bg-amber-600 text-white font-bold h-8 px-4 border-0'
                                  onClick={handleArrive}
                                  disabled={isActing}
                                >
                                  Arrive Stop
                                </Button>
                              )}

                              {(stopStatus === 'ARRIVED' || stopStatus === 'BOARDING') && (
                                <Button
                                  size='sm'
                                  className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-bold h-8 px-4 border-0'
                                  onClick={handleDepart}
                                  disabled={isActing}
                                >
                                  Depart Stop
                                </Button>
                              )}

                              {currentStop.stopPurpose !== 'START_TERMINAL' &&
                                currentStop.stopPurpose !== 'END_TERMINAL' &&
                                stopStatus !== 'DEPARTED' &&
                                stopStatus !== 'SKIPPED' && (
                                  <>
                                    {showSkipForm ? (
                                      <div className='flex items-center gap-1.5 bg-slate-50 border border-slate-100 rounded-lg p-1.5'>
                                        <Input
                                          value={skipReason}
                                          onChange={(e) => setSkipReason(e.target.value)}
                                          placeholder='Skip reason…'
                                          className='h-7 text-xs rounded px-2 w-28'
                                        />
                                        <Button
                                          size='sm'
                                          className='h-7 text-[10px] rounded bg-slate-600 text-white font-bold hover:bg-slate-750 px-2'
                                          onClick={handleSkip}
                                          disabled={!skipReason.trim() || isActing}
                                        >
                                          Confirm
                                        </Button>
                                        <Button
                                          size='sm'
                                          variant='ghost'
                                          className='h-7 text-[10px] px-1 rounded'
                                          onClick={() => setShowSkipForm(false)}
                                        >
                                          Cancel
                                        </Button>
                                      </div>
                                    ) : (
                                      <Button
                                        size='sm'
                                        variant='outline'
                                        className='rounded-full border-slate-200 text-slate-600 hover:bg-slate-50 h-8 px-3'
                                        onClick={() => setShowSkipForm(true)}
                                        disabled={isActing}
                                      >
                                        <SkipForward size={13} className='mr-1.5 shrink-0' />
                                        Skip Stop
                                      </Button>
                                    )}
                                  </>
                                )}
                            </>
                          )}

                          {/* Complete Trip (If all stops departed) */}
                          {tripIsActive && opSummary && opSummary.remaining === 0 && (
                            <Button
                              size='sm'
                              className='rounded-full bg-blue-600 hover:bg-blue-700 text-white font-bold h-8 px-4 border-0'
                              onClick={handleComplete}
                              disabled={isActing}
                            >
                              <CheckCircle2 size={13} className='mr-1.5' />
                              Complete Trip
                            </Button>
                          )}
                        </div>
                      </div>

                      {/* Warnings / Tips */}
                      {tripIsActive && stopStatus === 'PENDING' && currentStop.routeStopId === selectedStopId && (
                        <p className='text-[10px] text-amber-600 font-semibold bg-amber-50 border border-amber-100 rounded-lg p-2.5'>
                          ⚠️ Bus must arrive at this stop first before attendance actions can be recorded.
                        </p>
                      )}
                    </div>
                  </div>

                  {/* ── F: Student manifest list ── */}
                  {isDepotStop ? (
                    <div className='bg-slate-50/50 border border-slate-200 rounded-2xl p-6 text-center text-slate-500 flex flex-col items-center gap-2 shadow-xs'>
                      <Info className='h-8 w-8 text-slate-400' />
                      <span className='font-semibold text-slate-700 text-sm'>Depot Stop</span>
                      <span className='text-xs text-slate-400 max-w-sm'>
                        Depot stop — terminal point without attendance actions.
                      </span>
                    </div>
                  ) : studentsAtStop.length === 0 ? (
                    <div className='bg-slate-50/50 border border-slate-200 rounded-2xl p-6 text-center text-slate-500 flex flex-col items-center gap-2 shadow-xs'>
                      <Users className='h-8 w-8 text-slate-400' />
                      <span className='font-semibold text-slate-700 text-sm'>No Students Mapped</span>
                      <span className='text-xs text-slate-400 max-w-sm'>
                        No students are planned to board or drop off at this stop.
                      </span>
                    </div>
                  ) : (
                    <div className='overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm'>
                      <div className='border-b border-slate-100 bg-slate-50/70 px-4 py-3 flex items-center justify-between'>
                        <p className='text-xs font-extrabold text-slate-650 uppercase tracking-wider'>
                          Student Manifest{' '}
                          <span className='font-bold text-slate-400 ml-1'>
                            ({studentsAtStop.length})
                          </span>
                        </p>
                        <span className='text-[10px] text-slate-400 font-medium'>Actions correspond to stop purpose</span>
                      </div>
                      <div className='divide-y divide-slate-100'>
                        {studentsAtStop.map((student) => {
                          const status = student.status;
                          const isDoneStatus = STUDENT_STATUS_DONE.has(status);
                          const { stopPurpose, locationType } = currentStop;

                          const canBoard =
                            isStopActionable &&
                            status === 'PLANNED' &&
                            (stopPurpose === 'PICKUP' ||
                              (stopPurpose === 'START_TERMINAL' && locationType === 'SCHOOL'));

                          const canDropoff =
                            isStopActionable &&
                            status === 'BOARDED' &&
                            (stopPurpose === 'DROPOFF' ||
                              (stopPurpose === 'END_TERMINAL' && locationType === 'SCHOOL'));

                          return (
                            <div
                              key={student.tripStudentId}
                              className={cn(
                                'flex flex-wrap items-center justify-between gap-3 px-4 py-3.5 transition-all',
                                isDoneStatus && 'bg-slate-50/30 opacity-70'
                              )}
                            >
                              <div className='flex items-center gap-3 min-w-0'>
                                <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-violet-50 text-[#7C3AED] border border-violet-100'>
                                  <User className='h-4.5 w-4.5' />
                                </div>
                                <div className='min-w-0'>
                                  <p className='font-bold text-slate-900 text-xs truncate'>
                                    {student.studentName ?? `Student #${student.studentId}`}
                                  </p>
                                  {student.studentCode && (
                                    <p className='text-[10px] text-slate-400 mt-0.5 font-semibold'>{student.studentCode}</p>
                                  )}
                                </div>
                              </div>

                              <div className='flex flex-wrap items-center gap-2'>
                                {/* Planned action badge */}
                                {status === 'PLANNED' && (
                                  <span className='inline-flex items-center text-[9px] font-extrabold uppercase bg-blue-50 text-blue-700 border border-blue-100 rounded px-1.5 py-0.2'>
                                    Plan: Board
                                  </span>
                                )}
                                {status === 'BOARDED' && (
                                  <span className='inline-flex items-center text-[9px] font-extrabold uppercase bg-emerald-50 text-emerald-700 border border-emerald-100 rounded px-1.5 py-0.2'>
                                    Plan: Drop
                                  </span>
                                )}

                                {renderFriendlyBadge(status)}

                                {canBoard && (
                                  <Button
                                    size='sm'
                                    className='h-7 rounded-full bg-emerald-600 px-3 text-xs text-white hover:bg-emerald-700 font-bold border-0 shadow-none'
                                    onClick={() => handleBoard(student)}
                                    disabled={isActing}
                                  >
                                    Board
                                  </Button>
                                )}
                                {canDropoff && (
                                  <Button
                                    size='sm'
                                    className='h-7 rounded-full bg-blue-600 px-3 text-xs text-white hover:bg-blue-700 font-bold border-0 shadow-none'
                                    onClick={() => handleDropoff(student)}
                                    disabled={isActing}
                                  >
                                    Drop-off
                                  </Button>
                                )}
                                {canBoard && (
                                  <Button
                                    size='sm'
                                    variant='outline'
                                    className='h-7 rounded-full border-amber-250 px-3 text-xs text-amber-700 hover:bg-amber-50 font-bold shadow-none'
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
                                    className='h-7 rounded-full border-red-250 px-3 text-xs text-red-650 hover:bg-red-50 font-bold shadow-none'
                                    onClick={() => handleNoShow(student)}
                                    disabled={isActing}
                                  >
                                    No-show
                                  </Button>
                                )}
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>

            {/* ── G: Right Event feed ── */}
            <div className='flex flex-col gap-2.5 xl:w-[310px] shrink-0'>
              <p className='text-[10px] font-extrabold text-slate-400 uppercase tracking-wider px-1'>Activity Log Feed</p>
              {sortedEvents.length === 0 ? (
                <SchoolBusEmptyState
                  title='No events yet'
                  description='Attendance events will appear here as they are logged.'
                  icon={Bell}
                />
              ) : (
                <div className='space-y-2 xl:max-h-[calc(100vh-320px)] xl:overflow-y-auto xl:pr-1'>
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
                      : 'border-slate-100 bg-white';

                    return (
                      <div
                        key={item.id}
                        className={cn('rounded-xl border p-3 shadow-xs space-y-1.5 transition-all hover:shadow-sm', cardBorder)}
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
        )}
      </div>
    </SchoolBusPageShell>
  );
}
