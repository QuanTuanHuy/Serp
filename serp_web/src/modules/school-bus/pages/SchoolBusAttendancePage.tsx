'use client';

import * as React from 'react';
import { BusFront, ClipboardCheck, MapPin } from 'lucide-react';
import { toast } from 'sonner';
import {
  useGetTripsQuery,
  useGetTripAttendanceManifestQuery,
  useGetTripAttendanceSummaryQuery,
  useGetTripAttendanceQuery,
  useBoardTripStudentMutation,
  useDropoffTripStudentMutation,
  useAbsentTripStudentMutation,
  useNoShowTripStudentMutation,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { Button } from '@/shared/components/ui';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime, getPageItems } from '../utils';
import type { TripAttendanceStudentItem } from '../types';

export function SchoolBusAttendancePage() {
  const [selectedTripId, setSelectedTripId] = React.useState<number | null>(null);

  const tripsQuery = useGetTripsQuery({
    page: 0,
    size: 30,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const allTrips = getPageItems(tripsQuery.data?.data);

  // Prioritize IN_PROGRESS trips at the top
  const sortedTrips = React.useMemo(() => {
    const active = allTrips.filter((t) => t.status === 'IN_PROGRESS');
    const rest = allTrips.filter((t) => t.status !== 'IN_PROGRESS');
    return [...active, ...rest];
  }, [allTrips]);

  // Auto-select first IN_PROGRESS trip on load
  React.useEffect(() => {
    if (!selectedTripId && sortedTrips.length > 0) {
      const inProgress = sortedTrips.find((t) => t.status === 'IN_PROGRESS');
      if (inProgress) setSelectedTripId(inProgress.id);
    }
  }, [sortedTrips, selectedTripId]);

  const selectedTrip = sortedTrips.find((t) => t.id === selectedTripId) ?? null;
  const isActive = selectedTrip?.status === 'IN_PROGRESS';

  const { data: manifestData, isLoading: manifestLoading } =
    useGetTripAttendanceManifestQuery(selectedTripId as number, {
      skip: !selectedTripId,
    });
  const { data: summaryData } = useGetTripAttendanceSummaryQuery(
    selectedTripId as number,
    { skip: !selectedTripId }
  );
  const { data: eventsData } = useGetTripAttendanceQuery(
    selectedTripId as number,
    { skip: !selectedTripId }
  );

  const manifest = manifestData?.data ?? null;
  const summary = summaryData?.data ?? manifest?.summary ?? null;
  const events = eventsData?.data ?? [];

  const [boardStudent, { isLoading: boarding }] = useBoardTripStudentMutation();
  const [dropoffStudent, { isLoading: droppingOff }] = useDropoffTripStudentMutation();
  const [absentStudent, { isLoading: markingAbsent }] = useAbsentTripStudentMutation();
  const [noShowStudent, { isLoading: markingNoShow }] = useNoShowTripStudentMutation();
  const isActing = boarding || droppingOff || markingAbsent || markingNoShow;

  const direction = manifest?.routeDirection ?? selectedTrip?.routeDirection ?? null;
  const isOutbound = direction === 'OUTBOUND';

  // Group students by their service stop, also handle terminal stops
  const studentsByStop = React.useMemo(() => {
    if (!manifest) return new Map<number, TripAttendanceStudentItem[]>();
    const map = new Map<number, TripAttendanceStudentItem[]>();

    // Build basic stop grouping by pickupStopId / dropoffStopId
    for (const student of manifest.students) {
      const stopId = isOutbound ? student.pickupStopId : student.dropoffStopId;
      if (stopId == null) continue;
      const list = map.get(stopId) ?? [];
      list.push(student);
      map.set(stopId, list);
    }

    // For terminal stops: OUTBOUND END_TERMINAL (school) shows all BOARDED students;
    // RETURN START_TERMINAL (school) shows all PLANNED students
    for (const stop of manifest.stops) {
      if (
        stop.stopPurpose === 'END_TERMINAL' &&
        stop.locationType === 'SCHOOL' &&
        isOutbound
      ) {
        const boardedStudents = manifest.students.filter(
          (s) => s.status === 'BOARDED' || s.status === 'DROPPED_OFF',
        );
        map.set(stop.routeStopId, boardedStudents);
      } else if (
        stop.stopPurpose === 'START_TERMINAL' &&
        stop.locationType === 'SCHOOL' &&
        !isOutbound
      ) {
        const plannedStudents = manifest.students.filter((s) => s.status === 'PLANNED');
        map.set(stop.routeStopId, plannedStudents);
      }
    }

    return map;
  }, [manifest, isOutbound]);

  const callAction = async (label: string, action: () => Promise<{ message?: string }>) => {
    try {
      const res = await action();
      toast.success(res.message ?? `${label} recorded`);
    } catch (err: unknown) {
      const apiError = err as { data?: { message?: string } };
      toast.error(apiError?.data?.message ?? `${label} failed`);
    }
  };

  const handleBoard = (student: TripAttendanceStudentItem, stopId?: number) => {
    const routeStopId = stopId ?? student.pickupStopId;
    if (!selectedTripId || !routeStopId) return;
    callAction('Board', () =>
      boardStudent({
        tripId: selectedTripId,
        body: { routeStopId: routeStopId, studentId: student.studentId },
      }).unwrap()
    );
  };

  const handleDropoff = (student: TripAttendanceStudentItem, stopId?: number) => {
    const routeStopId = stopId ?? student.dropoffStopId;
    if (!selectedTripId || !routeStopId) return;
    callAction('Drop-off', () =>
      dropoffStudent({
        tripId: selectedTripId,
        body: { routeStopId: routeStopId, studentId: student.studentId },
      }).unwrap()
    );
  };

  const handleAbsent = (student: TripAttendanceStudentItem, stopId: number) => {
    if (!selectedTripId) return;
    callAction('Absent', () =>
      absentStudent({
        tripId: selectedTripId,
        body: { routeStopId: stopId, studentId: student.studentId },
      }).unwrap()
    );
  };

  const handleNoShow = (student: TripAttendanceStudentItem, stopId: number) => {
    if (!selectedTripId) return;
    callAction('No-show', () =>
      noShowStudent({
        tripId: selectedTripId,
        body: { routeStopId: stopId, studentId: student.studentId },
      }).unwrap()
    );
  };

  return (
    <SchoolBusPageShell
      title='Attendance'
      description='Trip-based attendance management. Select an active trip to record boarding, drop-off, and absence events.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Attendance', current: true },
          ]}
        />
      }
    >
      {/* ── Trip selector ── */}
      <SchoolBusSection
        title='Select trip'
        description={
          sortedTrips.length === 0
            ? 'No trips found.'
            : `${sortedTrips.length} trips. IN_PROGRESS trips are shown first.`
        }
      >
        {sortedTrips.length === 0 ? (
          <SchoolBusEmptyState
            title='No trips found'
            description='Create and start a trip from the Dispatch or Trips page.'
            icon={BusFront}
          />
        ) : (
          <div className='flex flex-wrap gap-2'>
            {sortedTrips.map((trip) => (
              <button
                key={trip.id}
                onClick={() => setSelectedTripId(trip.id)}
                className={[
                  'flex items-center gap-2 rounded-full border px-4 py-1.5 text-sm font-medium transition',
                  selectedTripId === trip.id
                    ? 'border-rose-300 bg-rose-50 text-rose-700 shadow-sm'
                    : trip.status === 'IN_PROGRESS'
                    ? 'border-amber-200 bg-amber-50 text-amber-700 hover:border-amber-300 hover:bg-amber-100'
                    : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50',
                ].join(' ')}
              >
                <span
                  className={[
                    'h-2 w-2 rounded-full',
                    trip.status === 'IN_PROGRESS'
                      ? 'bg-amber-400'
                      : trip.status === 'COMPLETED'
                      ? 'bg-emerald-400'
                      : 'bg-slate-300',
                  ].join(' ')}
                />
                <span>{trip.tripCode}</span>
                <span className='text-xs opacity-60'>{trip.routeDirection}</span>
              </button>
            ))}
          </div>
        )}
      </SchoolBusSection>

      {/* ── No trip selected ── */}
      {!selectedTripId && (
        <SchoolBusEmptyState
          title='No trip selected'
          description='Select a trip above to view its attendance manifest and record events.'
          icon={ClipboardCheck}
        />
      )}

      {/* ── Trip detail ── */}
      {selectedTripId && (
        <div className='grid gap-6 xl:grid-cols-[1.4fr_0.6fr]'>
          {/* LEFT: trip header + summary + stop manifest */}
          <div className='space-y-6'>
            {/* Trip header */}
            {selectedTrip && (
              <div className={schoolBusUi.subtlePanel}>
                <div className='flex flex-wrap items-start justify-between gap-4'>
                  <div>
                    <p className={`${schoolBusUi.eyebrow} mb-1`}>
                      {selectedTrip.routeDirection} · {selectedTrip.routeCode}
                    </p>
                    <h3 className='text-lg font-semibold text-slate-900'>
                      {selectedTrip.tripCode}
                    </h3>
                    <p className='mt-1 text-sm text-slate-500'>
                      Service date: {formatDate(selectedTrip.serviceDate)}
                    </p>
                    {selectedTrip.driverName && (
                      <p className='mt-1 text-sm text-slate-600'>
                        Driver:{' '}
                        <span className='font-medium'>{selectedTrip.driverName}</span>
                        {selectedTrip.busPlateNumber && (
                          <span> · Bus: {selectedTrip.busPlateNumber}</span>
                        )}
                      </p>
                    )}
                  </div>
                  <div className='flex flex-col items-end gap-1'>
                    <SchoolBusStatusBadge status={selectedTrip.status} />
                    {!isActive && (
                      <span className='text-xs text-slate-400'>
                        Actions disabled — trip not IN_PROGRESS
                      </span>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Summary mini-cards */}
            {summary && (
              <div className='grid grid-cols-4 gap-3 sm:grid-cols-7'>
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
                  <div
                    key={item.label}
                    className='rounded-2xl border border-slate-200 bg-white p-3 text-center shadow-sm'
                  >
                    <p className={`text-2xl font-bold ${item.color}`}>{item.value}</p>
                    <p className='mt-1 text-xs text-slate-500'>{item.label}</p>
                  </div>
                ))}
              </div>
            )}

            {/* Stop manifest */}
            <SchoolBusSection
              title='Stop manifest'
              description={
                isOutbound
                  ? 'Outbound trip — record boarding at each pickup stop.'
                  : 'Return trip — record drop-off at each dropoff stop.'
              }
            >
              {manifestLoading ? (
                <SchoolBusEmptyState
                  title='Loading manifest…'
                  description=''
                  icon={ClipboardCheck}
                />
              ) : !manifest || manifest.stops.length === 0 ? (
                <SchoolBusEmptyState
                  title='No stop data'
                  description='The manifest has no stops yet. Start the trip to generate stop logs.'
                  icon={MapPin}
                />
              ) : (
                <div className='space-y-4'>
                  {manifest.stops.map((stop, idx) => {
                    const studentsAtStop =
                      studentsByStop.get(stop.routeStopId) ?? [];
                    const purpose = stop.stopPurpose;
                    const locType = stop.locationType;
                    const isDepotStop = locType === 'DEPOT';

                    // Badge variant for terminal stops
                    const purposeLabel =
                      purpose === 'START_TERMINAL'
                        ? locType === 'SCHOOL'
                          ? 'School (Start)'
                          : 'Depot (Start)'
                        : purpose === 'END_TERMINAL'
                          ? locType === 'SCHOOL'
                            ? 'School (End)'
                            : 'Depot (End)'
                          : undefined;

                    return (
                      <div
                        key={stop.routeStopId}
                        className='overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm'
                      >
                        {/* Stop header row */}
                        <div className='flex items-center justify-between border-b border-slate-100 bg-slate-50/60 px-4 py-3'>
                          <div className='flex items-center gap-3'>
                            <span className='flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-rose-100 text-xs font-bold text-rose-700'>
                              {idx + 1}
                            </span>
                            <div>
                              <div className='flex items-center gap-2'>
                                <p className='font-medium text-slate-900'>
                                  {stop.displayName ?? stop.stopName ?? `Stop ${stop.stopOrder}`}
                                </p>
                                {purposeLabel && (
                                  <span className='rounded-full bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700'>
                                    {purposeLabel}
                                  </span>
                                )}
                              </div>
                              <p className='text-xs text-slate-500'>
                                {purpose === 'PICKUP' || (purpose === 'START_TERMINAL' && locType === 'SCHOOL')
                                  ? `Planned board: ${stop.plannedBoardingCount} · Actual: ${stop.actualBoardedCount}`
                                  : `Planned drop: ${stop.plannedDropoffCount} · Actual: ${stop.actualDroppedCount}`}
                              </p>
                            </div>
                          </div>
                          <SchoolBusStatusBadge status={stop.stopStatus} />
                        </div>

                        {/* Student rows */}
                        {isDepotStop ? (
                          <p className='px-4 py-3 text-sm text-slate-400 italic'>
                            Terminal depot stop — no student attendance
                          </p>
                        ) : studentsAtStop.length === 0 ? (
                          <p className='px-4 py-3 text-sm text-slate-400'>
                            No students assigned to this stop
                          </p>
                        ) : (
                          <div className='divide-y divide-slate-100'>
                            {studentsAtStop.map((student) => {
                              const status = student.status;

                              // Determine the stopId to pass to absent/no-show actions
                              const actionStopId = stop.routeStopId;

                              // Action eligibility based on stop purpose + student status
                              const canBoard =
                                isActive && !isActing &&
                                status === 'PLANNED' && (
                                  (purpose === 'PICKUP') ||
                                  (purpose === 'START_TERMINAL' && locType === 'SCHOOL')
                                );

                              const canDropoff =
                                isActive && !isActing &&
                                status === 'BOARDED' && (
                                  (purpose === 'DROPOFF') ||
                                  (purpose === 'END_TERMINAL' && locType === 'SCHOOL')
                                );

                              const canAbsent =
                                isActive && !isActing && status === 'PLANNED' &&
                                (purpose === 'PICKUP' || (purpose === 'START_TERMINAL' && locType === 'SCHOOL'));

                              const canNoShow =
                                isActive && !isActing && status === 'PLANNED' &&
                                (purpose === 'PICKUP' || purpose === 'START_TERMINAL');

                              return (
                                <div
                                  key={student.tripStudentId}
                                  className='flex flex-wrap items-center justify-between gap-3 px-4 py-3'
                                >
                                  <div className='min-w-0'>
                                    <p className='font-medium text-slate-900'>
                                      {student.studentName ??
                                        `Student #${student.studentId}`}
                                    </p>
                                    {student.studentCode && (
                                      <p className='text-xs text-slate-500'>
                                        {student.studentCode}
                                      </p>
                                    )}
                                  </div>

                                  <div className='flex flex-wrap items-center gap-2'>
                                    <SchoolBusStatusBadge status={status} />

                                    {/* Board button */}
                                    {canBoard && (
                                      <Button
                                        size='sm'
                                        className='h-7 rounded-full bg-emerald-600 px-3 text-xs text-white hover:bg-emerald-700'
                                        onClick={() => handleBoard(student, actionStopId)}
                                        disabled={isActing}
                                      >
                                        Board
                                      </Button>
                                    )}

                                    {/* Drop-off button */}
                                    {canDropoff && (
                                      <Button
                                        size='sm'
                                        className='h-7 rounded-full bg-blue-600 px-3 text-xs text-white hover:bg-blue-700'
                                        onClick={() => handleDropoff(student, actionStopId)}
                                        disabled={isActing}
                                      >
                                        Drop-off
                                      </Button>
                                    )}

                                    {/* Absent button */}
                                    {canAbsent && (
                                      <Button
                                        size='sm'
                                        variant='outline'
                                        className='h-7 rounded-full border-amber-200 px-3 text-xs text-amber-700 hover:bg-amber-50'
                                        onClick={() => handleAbsent(student, actionStopId)}
                                        disabled={isActing}
                                      >
                                        Absent
                                      </Button>
                                    )}

                                    {/* No-show button */}
                                    {canNoShow && !canAbsent && (
                                      <Button
                                        size='sm'
                                        variant='outline'
                                        className='h-7 rounded-full border-rose-200 px-3 text-xs text-rose-700 hover:bg-rose-50'
                                        onClick={() => handleNoShow(student, actionStopId)}
                                        disabled={isActing}
                                      >
                                        No-show
                                      </Button>
                                    )}
                                    {canAbsent && (
                                      <Button
                                        size='sm'
                                        variant='outline'
                                        className='h-7 rounded-full border-rose-200 px-3 text-xs text-rose-700 hover:bg-rose-50'
                                        onClick={() => handleNoShow(student, actionStopId)}
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
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </SchoolBusSection>
          </div>

          {/* RIGHT: attendance event feed */}
          <SchoolBusSection
            title='Attendance events'
            description='Real-time events recorded for this trip.'
          >
            {events.length === 0 ? (
              <SchoolBusEmptyState
                title='No events yet'
                description='Events appear as students are boarded, dropped off, or marked absent.'
                icon={ClipboardCheck}
              />
            ) : (
              <div className='space-y-3'>
                {events.map((item) => (
                  <div key={item.id} className={schoolBusUi.interactiveCard}>
                    <div className='flex items-start justify-between gap-3'>
                      <div className='min-w-0'>
                        <p className='font-medium text-slate-900'>
                          {item.studentName}
                        </p>
                        <p className='mt-1 text-xs text-slate-500'>
                          {formatDateTime(item.recordedAt)}
                        </p>
                      </div>
                      <SchoolBusStatusBadge
                        status={item.eventType ?? item.attendanceType}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </SchoolBusSection>
        </div>
      )}
    </SchoolBusPageShell>
  );
}
