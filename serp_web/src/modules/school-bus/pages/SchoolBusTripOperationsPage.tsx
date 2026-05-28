'use client';

import * as React from 'react';
import {
  CalendarDays,
  CheckCircle2,
  ClipboardCheck,
  Play,
  SkipForward,
  User,
  XCircle,
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
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { Button, Input } from '@/shared/components/ui';
import { schoolBusUi } from '../theme';
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
  const isOutbound = (manifest?.routeDirection ?? trip?.routeDirection) === 'OUTBOUND';
  const tripCode = manifest?.tripCode ?? trip?.tripCode ?? `Trip #${tripId}`;
  const routeCode = manifest?.routeCode ?? trip?.routeCode ?? '';

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
    // intentionally excludes selectedStopId — auto-advance only when manifest changes
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
      toast.success(`${label} done`);
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

  // ── render ─────────────────────────────────────────────────────────────────
  return (
    <SchoolBusPageShell
      title={tripCode}
      description='Manage stop lifecycle, boarding, and drop-off attendance for this trip.'
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
      {/* ── B: Trip header ── */}
      {trip && (
        <div className={schoolBusUi.section}>
          <div className='flex flex-wrap items-start justify-between gap-4'>
            {/* Info */}
            <div>
              <div className='flex flex-wrap items-center gap-2'>
                <p className={schoolBusUi.eyebrow}>
                  {isOutbound ? 'OUTBOUND' : 'RETURN'} · {routeCode}
                </p>
                <SchoolBusStatusBadge status={tripStatus} />
              </div>
              <h2 className='mt-1 text-xl font-bold text-slate-900'>{tripCode}</h2>
              <div className='mt-2 flex flex-wrap gap-x-5 gap-y-1 text-sm text-slate-600'>
                <span className='flex items-center gap-1.5'>
                  <CalendarDays size={13} className='text-slate-400' />
                  {formatDate(trip.serviceDate)}
                </span>
                {trip.busPlateNumber && (
                  <span className='flex items-center gap-1.5 font-medium'>
                    🚌 {trip.busPlateNumber}
                  </span>
                )}
                {trip.driverName && (
                  <span className='flex items-center gap-1.5'>
                    <User size={13} className='text-slate-400' />
                    {trip.driverName}
                  </span>
                )}
                {trip.attendantName && (
                  <span className='flex items-center gap-1.5 text-slate-500'>
                    <User size={13} className='text-slate-400' />
                    {trip.attendantName}{' '}
                    <span className='text-xs text-slate-400'>(attendant)</span>
                  </span>
                )}
              </div>
            </div>

            {/* Trip lifecycle actions */}
            <div className='flex flex-col items-end gap-2'>
              {!['IN_PROGRESS', 'COMPLETED', 'CANCELLED'].includes(tripStatus ?? '') && (
                <Button
                  size='sm'
                  className='rounded-full bg-emerald-600 px-5 text-white hover:bg-emerald-700'
                  onClick={handleStart}
                  disabled={isActing}
                >
                  <Play size={13} className='mr-1.5' />
                  Start Trip
                </Button>
              )}
              {tripIsActive && (
                <Button
                  size='sm'
                  className='rounded-full bg-blue-600 px-5 text-white hover:bg-blue-700'
                  onClick={handleComplete}
                  disabled={isActing}
                >
                  <CheckCircle2 size={13} className='mr-1.5' />
                  Complete Trip
                </Button>
              )}
              {!['COMPLETED', 'CANCELLED'].includes(tripStatus ?? '') &&
                (showCancelForm ? (
                  <div className='flex items-center gap-2'>
                    <Input
                      value={cancelReason}
                      onChange={(e) => setCancelReason(e.target.value)}
                      placeholder='Cancel reason…'
                      className='h-8 rounded-full px-3 text-sm'
                    />
                    <Button
                      size='sm'
                      className='rounded-full bg-rose-600 px-3 text-white hover:bg-rose-700'
                      onClick={handleCancel}
                      disabled={!cancelReason.trim() || isActing}
                    >
                      Confirm
                    </Button>
                    <Button size='sm' variant='ghost' onClick={() => setShowCancelForm(false)}>
                      Back
                    </Button>
                  </div>
                ) : (
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full border-rose-200 px-4 text-rose-600 hover:bg-rose-50'
                    onClick={() => setShowCancelForm(true)}
                    disabled={isActing}
                  >
                    <XCircle size={13} className='mr-1.5' />
                    Cancel Trip
                  </Button>
                ))}
            </div>
          </div>
        </div>
      )}

      {/* ── C: Summary cards ── */}
      {(summary || opSummary) && (
        <div className='grid gap-4 sm:grid-cols-2'>
          {summary && (
            <div className={schoolBusUi.section}>
              <p className={`${schoolBusUi.eyebrow} mb-3`}>Students</p>
              <div className='grid grid-cols-4 gap-2 sm:grid-cols-7'>
                {(
                  [
                    { label: 'Total', value: summary.totalStudents, color: 'text-slate-700' },
                    { label: 'Planned', value: summary.planned, color: 'text-amber-600' },
                    { label: 'Boarded', value: summary.boarded, color: 'text-emerald-600' },
                    { label: 'Dropped', value: summary.droppedOff, color: 'text-emerald-700' },
                    { label: 'Absent', value: summary.absent, color: 'text-rose-600' },
                    { label: 'No-show', value: summary.noShow, color: 'text-rose-500' },
                    { label: 'Not served', value: summary.notServed, color: 'text-slate-400' },
                  ] as const
                ).map((item) => (
                  <div key={item.label} className='text-center'>
                    <p className={`text-xl font-bold ${item.color}`}>{item.value}</p>
                    <p className='mt-0.5 text-[10px] text-slate-400'>{item.label}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
          {opSummary && (
            <div className={schoolBusUi.section}>
              <p className={`${schoolBusUi.eyebrow} mb-3`}>Stops</p>
              <div className='grid grid-cols-4 gap-2'>
                {(
                  [
                    { label: 'Total', value: opSummary.total, color: 'text-slate-700' },
                    { label: 'Done', value: opSummary.done, color: 'text-emerald-600' },
                    {
                      label: 'Current',
                      value: opSummary.current ? `#${(opSummary.current.stopOrder ?? 0) + 1}` : '—',
                      color: 'text-amber-600',
                    },
                    { label: 'Remaining', value: opSummary.remaining, color: 'text-slate-500' },
                  ] as const
                ).map((item) => (
                  <div key={item.label} className='text-center'>
                    <p className={`text-xl font-bold ${item.color}`}>{item.value}</p>
                    <p className='mt-0.5 text-[10px] text-slate-400'>{item.label}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ── D+E+G: Main workspace ── */}
      {manifestLoading ? (
        <SchoolBusEmptyState title='Loading…' description='' icon={ClipboardCheck} />
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
        <div className='grid gap-5 xl:grid-cols-[240px_1fr_288px]'>
          {/* ── D: Stop timeline ── */}
          <div>
            <p className={`${schoolBusUi.eyebrow} mb-2 px-1`}>Stop timeline</p>
            <div className='space-y-1 xl:max-h-[calc(100vh-260px)] xl:overflow-y-auto xl:pr-1'>
              {manifest.stops.map((stop, idx) => {
                const isCurrent = stop.routeStopId === selectedStopId;
                const isDone = stop.stopStatus === 'DEPARTED' || stop.stopStatus === 'SKIPPED';
                return (
                  <button
                    key={stop.routeStopId}
                    onClick={() => {
                      setSelectedStopId(stop.routeStopId);
                      setShowSkipForm(false);
                    }}
                    className={[
                      'w-full rounded-xl border px-3 py-2.5 text-left transition',
                      isCurrent
                        ? 'border-rose-200 bg-rose-50 shadow-sm'
                        : 'border-transparent hover:bg-slate-50',
                      isDone ? 'opacity-50' : '',
                    ].join(' ')}
                  >
                    <div className='flex items-center gap-2'>
                      <span
                        className={[
                          'flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold',
                          isCurrent
                            ? 'bg-rose-600 text-white'
                            : isDone
                              ? 'bg-emerald-100 text-emerald-600'
                              : 'bg-slate-100 text-slate-500',
                        ].join(' ')}
                      >
                        {idx + 1}
                      </span>
                      <div className='min-w-0 flex-1'>
                        <p className='truncate text-sm font-medium text-slate-900'>
                          {stop.displayName ?? stop.stopName ?? `Stop ${stop.stopOrder}`}
                        </p>
                        <p className='truncate text-[11px] text-slate-400'>
                          {stopTypeLabel(stop)}
                        </p>
                      </div>
                      <SchoolBusStatusBadge status={stop.stopStatus} />
                    </div>
                    {(stop.plannedBoardingCount > 0 || stop.plannedDropoffCount > 0) && (
                      <div className='mt-1 flex gap-3 pl-8 text-[10px] text-slate-400'>
                        {stop.plannedBoardingCount > 0 && (
                          <span>↑ {stop.actualBoardedCount}/{stop.plannedBoardingCount}</span>
                        )}
                        {stop.plannedDropoffCount > 0 && (
                          <span>↓ {stop.actualDroppedCount}/{stop.plannedDropoffCount}</span>
                        )}
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* ── E+F: Current Stop Workbench ── */}
          <div className='space-y-4'>
            {!currentStop ? (
              <SchoolBusEmptyState
                title='Select a stop'
                description='Click a stop in the timeline to operate on it.'
                icon={ClipboardCheck}
              />
            ) : (
              <>
                {/* Stop header + operations */}
                <div className={schoolBusUi.section}>
                  <div className='flex flex-wrap items-start justify-between gap-3'>
                    <div>
                      <div className='flex items-center gap-2'>
                        <span
                          className={[
                            'flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-bold',
                            isDepotStop
                              ? 'bg-slate-100 text-slate-500'
                              : currentStop.locationType === 'SCHOOL'
                                ? 'bg-indigo-100 text-indigo-700'
                                : 'bg-rose-100 text-rose-700',
                          ].join(' ')}
                        >
                          {(currentStop.stopOrder ?? 0) + 1}
                        </span>
                        <div>
                          <p className='font-semibold text-slate-900'>
                            {currentStop.displayName ?? currentStop.stopName ?? `Stop ${currentStop.stopOrder}`}
                          </p>
                          <p className='text-xs text-slate-500'>{stopTypeLabel(currentStop)}</p>
                        </div>
                      </div>
                      {stopActionHint(currentStop, isOutbound) && (
                        <p className='mt-2 text-sm text-slate-600'>
                          {stopActionHint(currentStop, isOutbound)}
                        </p>
                      )}
                    </div>
                    <SchoolBusStatusBadge status={stopStatus} />
                  </div>

                  {/* Stop lifecycle controls */}
                  {tripIsActive && stopStatus !== 'DEPARTED' && stopStatus !== 'SKIPPED' && (
                    <div className='mt-4 space-y-3 border-t border-slate-100 pt-4'>
                      {/* Action flow indicator */}
                      <div className='flex flex-wrap items-center gap-1 text-xs text-slate-400'>
                        <span className={stopStatus === 'PENDING' ? 'font-semibold text-amber-600' : ''}>
                          Arrive
                        </span>
                        <span>→</span>
                        <span
                          className={
                            stopStatus === 'ARRIVED' || stopStatus === 'BOARDING'
                              ? 'font-semibold text-emerald-600'
                              : ''
                          }
                        >
                          Attendance
                        </span>
                        <span>→</span>
                        <span>Depart</span>
                      </div>

                      <div className='flex flex-wrap gap-2'>
                        {stopStatus === 'PENDING' && (
                          <Button
                            size='sm'
                            className='rounded-full bg-amber-500 px-4 text-white hover:bg-amber-600'
                            onClick={handleArrive}
                            disabled={isActing}
                          >
                            Arrive Stop
                          </Button>
                        )}
                        {(stopStatus === 'ARRIVED' || stopStatus === 'BOARDING') && (
                          <Button
                            size='sm'
                            className='rounded-full bg-emerald-600 px-4 text-white hover:bg-emerald-700'
                            onClick={handleDepart}
                            disabled={isActing}
                          >
                            Depart Stop
                          </Button>
                        )}
                        {currentStop.stopPurpose !== 'START_TERMINAL' &&
                          currentStop.stopPurpose !== 'END_TERMINAL' &&
                          (showSkipForm ? (
                            <div className='flex items-center gap-2'>
                              <Input
                                value={skipReason}
                                onChange={(e) => setSkipReason(e.target.value)}
                                placeholder='Skip reason…'
                                className='h-8 rounded-full px-3 text-sm'
                              />
                              <Button
                                size='sm'
                                className='rounded-full bg-slate-600 px-3 text-white hover:bg-slate-700'
                                onClick={handleSkip}
                                disabled={!skipReason.trim() || isActing}
                              >
                                Confirm skip
                              </Button>
                              <Button size='sm' variant='ghost' onClick={() => setShowSkipForm(false)}>
                                Cancel
                              </Button>
                            </div>
                          ) : (
                            <Button
                              size='sm'
                              variant='outline'
                              className='rounded-full border-slate-200 px-4 text-slate-600 hover:bg-slate-50'
                              onClick={() => setShowSkipForm(true)}
                              disabled={isActing}
                            >
                              <SkipForward size={13} className='mr-1.5' />
                              Skip Stop
                            </Button>
                          ))}
                      </div>

                      {stopStatus === 'PENDING' && (
                        <p className='text-xs text-amber-600'>
                          Arrive this stop first to enable attendance actions.
                        </p>
                      )}
                    </div>
                  )}
                  {(stopStatus === 'DEPARTED' || stopStatus === 'SKIPPED') && (
                    <p className='mt-3 text-xs italic text-slate-400'>
                      Stop {stopStatus?.toLowerCase()} — read-only.
                    </p>
                  )}
                  {!tripIsActive && (
                    <p className='mt-3 text-xs italic text-slate-400'>
                      Trip is {tripStatus?.toLowerCase()} — operations disabled.
                    </p>
                  )}
                </div>

                {/* ── F: Student list ── */}
                {isDepotStop ? (
                  <div className={`${schoolBusUi.section} text-sm italic text-slate-400`}>
                    Depot terminal stop — no student attendance actions.
                  </div>
                ) : studentsAtStop.length === 0 ? (
                  <div className={`${schoolBusUi.section} text-sm text-slate-400`}>
                    No students assigned to this stop.
                  </div>
                ) : (
                  <div className='overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm'>
                    <div className='border-b border-slate-100 bg-slate-50/70 px-4 py-2.5'>
                      <p className='text-sm font-semibold text-slate-700'>
                        Students{' '}
                        <span className='font-normal text-slate-400'>
                          ({studentsAtStop.length})
                        </span>
                      </p>
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
                            className={[
                              'flex flex-wrap items-center justify-between gap-3 px-4 py-3',
                              isDoneStatus ? 'opacity-60' : '',
                            ].join(' ')}
                          >
                            <div className='min-w-0'>
                              <p className='font-medium text-slate-900'>
                                {student.studentName ?? `Student #${student.studentId}`}
                              </p>
                              {student.studentCode && (
                                <p className='text-xs text-slate-400'>{student.studentCode}</p>
                              )}
                            </div>

                            <div className='flex flex-wrap items-center gap-2'>
                              <SchoolBusStatusBadge status={status} />
                              {canBoard && (
                                <Button
                                  size='sm'
                                  className='h-7 rounded-full bg-emerald-600 px-3 text-xs text-white hover:bg-emerald-700'
                                  onClick={() => handleBoard(student)}
                                  disabled={isActing}
                                >
                                  Board
                                </Button>
                              )}
                              {canDropoff && (
                                <Button
                                  size='sm'
                                  className='h-7 rounded-full bg-blue-600 px-3 text-xs text-white hover:bg-blue-700'
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
                                  className='h-7 rounded-full border-amber-200 px-3 text-xs text-amber-700 hover:bg-amber-50'
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
                                  className='h-7 rounded-full border-rose-200 px-3 text-xs text-rose-700 hover:bg-rose-50'
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

          {/* ── G: Event feed ── */}
          <div>
            <p className={`${schoolBusUi.eyebrow} mb-2`}>Event feed</p>
            {events.length === 0 ? (
              <SchoolBusEmptyState
                title='No events yet'
                description='Events appear as students board, drop off, or are marked absent.'
                icon={ClipboardCheck}
              />
            ) : (
              <div className='space-y-2'>
                {events.map((item) => (
                  <div
                    key={item.id}
                    className='rounded-xl border border-slate-100 bg-white px-3 py-2.5 shadow-sm'
                  >
                    <div className='flex items-start justify-between gap-2'>
                      <div className='min-w-0'>
                        <p className='truncate text-sm font-medium text-slate-900'>
                          {item.studentName}
                        </p>
                        <p className='text-[11px] text-slate-400'>
                          {formatDateTime(item.recordedAt)}
                        </p>
                      </div>
                      <SchoolBusStatusBadge status={item.eventType ?? item.attendanceType} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </SchoolBusPageShell>
  );
}
