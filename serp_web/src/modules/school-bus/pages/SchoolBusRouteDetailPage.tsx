'use client';

import Link from 'next/link';
import * as React from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  ClipboardCheck,
  BusFront,
  MapPinned,
  Pencil,
  PlayCircle,
  Route,
  Sparkles,
  UserCog,
  GraduationCap,
  Calendar,
  Clock3,
  MapPin,
  Users,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useAssignRouteMutation,
  useCreateTripFromRouteMutation,
  useGetAttendantDropdownOptionsQuery,
  useGetBusesQuery,
  useGetDriverDropdownOptionsQuery,
  useGetRoutePathQuery,
  useGetRouteByIdQuery,
} from '../api/schoolBusApi';
import { RouteAssignmentDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { schoolBusUi } from '../theme';
import {
  formatDate,
  formatDateTime,
  getPageItems,
  SCHOOL_BUS_OPTION_QUERY,
} from '../utils';

interface SchoolBusRouteDetailPageProps {
  routeId: number;
}

export function SchoolBusRouteDetailPage({
  routeId,
}: SchoolBusRouteDetailPageProps) {
  const {
    data,
    isLoading,
    refetch: refetchRoute,
  } = useGetRouteByIdQuery(routeId);
  const { data: driversData } = useGetDriverDropdownOptionsQuery();

  const { data: attendantsData } = useGetAttendantDropdownOptionsQuery();
  const { data: busesData } = useGetBusesQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'plateNumber',
  });

  const [assignRoute, { isLoading: assigning }] = useAssignRouteMutation();
  const [createTripFromRoute, { isLoading: creatingTrip }] =
    useCreateTripFromRouteMutation();
  const [assignmentOpen, setAssignmentOpen] = React.useState(false);
  const [fitKey, setFitKey] = React.useState(0);
  const { data: routePathData, refetch: refetchPath } =
    useGetRoutePathQuery(routeId);

  const detail = data?.data;

  React.useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      if (params.get('assign') === 'true') {
        setAssignmentOpen(true);
      }
    }
  }, []);

  const handleAssign = async (values: any) => {
    try {
      const response = await assignRoute({
        id: routeId,
        body: values,
      }).unwrap();
      toast.success(response.message || 'Route assigned');
      setAssignmentOpen(false);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to assign route');
    }
  };

  const handleCreateTrip = async () => {
    try {
      const response = await createTripFromRoute(routeId).unwrap();
      toast.success(response.message || 'Trip created from route');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to create trip');
    }
  };

  if (isLoading || !detail) {
    return (
      <SchoolBusPageShell
        title='Route detail'
        description='Loading route detail...'
      >
        <SchoolBusEmptyState
          title='Loading route detail'
          description='Fetching route, stop, and assignment data.'
        />
      </SchoolBusPageShell>
    );
  }

  const route = detail.route;
  const selectedBusPlate =
    detail.assignment?.busPlateNumber || route.busPlateNumber;
  const selectedBusCapacity =
    detail.assignment?.busCapacity ||
    route.busCapacity;
  const plannedStudentCount =
    route.plannedStudentCount || detail.students.length || 0;
  const stopsCount = route.stopsCount || detail.stops.length;
  const driverName = detail.assignment?.driverName || route.driverName;
  const attendantName = detail.assignment?.attendantName || route.attendantName;
  const hasBusReservation = Boolean(detail.assignment?.busId || route.busId);
  const hasStaffAssignment = Boolean(driverName);
  const isAssigned = hasBusReservation && hasStaffAssignment;
  const missingResourceCount = [
    !selectedBusPlate,
    !driverName,
    !attendantName,
  ].filter(Boolean).length;
  // Middle stops that lack coordinates (terminals use route-level coords instead)
  const missingStopCoordinates = detail.stops.filter(
    (stop) =>
      stop.stopPurpose !== 'START_TERMINAL' &&
      stop.stopPurpose !== 'END_TERMINAL' &&
      (typeof stop.pickupPointLatitude !== 'number' ||
        typeof stop.pickupPointLongitude !== 'number')
  ).length;

  const friendlyStatusLabel = (status: string) => {
    const s = status.toUpperCase();
    if (s === 'TRIP_CREATED') return 'Trip created';
    if (s === 'PUBLISHED') return 'Published';
    if (s === 'PLANNED') return 'Planned';
    if (s === 'ASSIGNED') return 'Assigned';
    if (s === 'IN_PROGRESS') return 'In progress';
    if (s === 'COMPLETED') return 'Completed';
    if (s === 'CANCELLED') return 'Cancelled';
    return status;
  };

  return (
    <>
      <SchoolBusPageShell
        title={`${route.routeCode} - ${route.routeName}`}
        description='Inspect route state, stop plan, and assignment details before and during execution.'
        actions={
          <>
            {['DRAFT', 'PLANNED', 'ASSIGNED'].includes(route.status) ? (
              <Button variant='outline' className='rounded-full' asChild>
                <Link href={`/school-bus/dispatch/${route.id}/edit`}>
                  <Pencil className='h-4 w-4' />
                  Edit
                </Link>
              </Button>
            ) : null}
            <Button
              variant={isAssigned ? 'outline' : 'default'}
              className={cn(
                'rounded-full font-semibold',
                !isAssigned &&
                  'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
              )}
              onClick={() => setAssignmentOpen(true)}
            >
              <UserCog className='h-4 w-4' />
              {hasBusReservation ? 'Manage resources' : 'Assign resources'}
            </Button>
            <Button variant='outline' className='rounded-full' asChild>
              <Link
                href={`/school-bus/dispatch/planning?sessionId=${route.planningSessionId || ''}&routeId=${route.id || ''}`}
              >
                <Sparkles className='h-4 w-4' />
                Plan workspace
              </Link>
            </Button>
            {route.status === 'ASSIGNED' ? (
              <Button
                className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-semibold border-0'
                onClick={handleCreateTrip}
                disabled={creatingTrip}
              >
                <BusFront className='h-4 w-4 mr-1' />
                {creatingTrip ? 'Creating...' : 'Create trip'}
              </Button>
            ) : null}
          </>
        }
      >
        {/* Status Strip */}
        <div className='grid grid-cols-2 md:grid-cols-5 gap-4 mb-6'>
          <StatusCard
            label='Planning'
            value={route.status === 'DRAFT' ? 'Draft' : 'Complete'}
            status={route.status === 'DRAFT' ? 'warning' : 'success'}
          />
          <StatusCard
            label='Assignment'
            value={isAssigned ? 'Ready' : hasBusReservation ? 'Bus selected' : 'Missing'}
            status={isAssigned ? 'success' : 'warning'}
          />
          <StatusCard
            label='Trip Snapshot'
            value={
              ['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(
                route.status
              )
                ? 'Locked'
                : 'Not created'
            }
            status={
              ['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(
                route.status
              )
                ? 'success'
                : 'muted'
            }
          />
          <StatusCard
            label='Attendance'
            value={
              ['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(
                route.status
              )
                ? 'Available'
                : 'Locked'
            }
            status={
              ['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(
                route.status
              )
                ? 'success'
                : 'muted'
            }
          />
        </div>

        {/* Main 2-column workspace */}
        <div className='grid gap-6 xl:grid-cols-[0.95fr_1.05fr] items-start'>
          {/* Left Column */}
          <div className='flex flex-col gap-6'>
            {/* Route Summary compact card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Route className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>
                    Route Summary
                  </h3>
                </div>
                <span className='rounded-full px-2.5 py-0.5 text-[10px] font-extrabold border shadow-none bg-slate-50 border-slate-200 text-slate-600 uppercase tracking-wider'>
                  {friendlyStatusLabel(route.status)}
                </span>
              </div>

              <div className='grid grid-cols-2 gap-y-4 gap-x-6 text-xs'>
                <SummaryItem
                  label='School'
                  value={route.schoolName}
                  icon={GraduationCap}
                />
                <SummaryItem
                  label='Direction'
                  value={
                    route.routeDirection === 'RETURN' ? 'Return' : 'Outbound'
                  }
                  icon={MapPinned}
                  isBadge
                  badgeColor={
                    route.routeDirection === 'RETURN'
                      ? 'bg-orange-50 text-orange-700 border-orange-100'
                      : 'bg-blue-50 text-blue-700 border-blue-100'
                  }
                />
                <SummaryItem
                  label='Service Date'
                  value={formatDate(route.serviceDate)}
                  icon={Calendar}
                />

                <SummaryItem
                  label='Start Terminal'
                  value={`${route.startLocationName} (${route.startLocationType})`}
                  icon={MapPin}
                />
                <SummaryItem
                  label='End Terminal'
                  value={`${route.endLocationName} (${route.endLocationType})`}
                  icon={MapPin}
                />
                <SummaryItem
                  label='Depot'
                  value={
                    route.startDepotName ||
                    (route.startLocationType === 'DEPOT'
                      ? route.startLocationName
                      : route.endLocationName)
                  }
                  icon={MapPin}
                />
                <SummaryItem
                  label='Planned Distance'
                  value={
                    route.plannedDistanceKm != null
                      ? `${route.plannedDistanceKm} km`
                      : 'N/A'
                  }
                  icon={Route}
                />
                <SummaryItem
                  label='Planned Duration'
                  value={
                    route.plannedDurationMin != null
                      ? `${route.plannedDurationMin} mins`
                      : 'N/A'
                  }
                  icon={Clock3}
                />
                <SummaryItem
                  label='Bus'
                  value={selectedBusPlate || 'No bus selected'}
                  icon={BusFront}
                />
                <SummaryItem
                  label='Capacity'
                  value={
                    selectedBusCapacity != null
                      ? `${selectedBusCapacity} seats`
                      : 'N/A'
                  }
                  icon={Users}
                />
                <SummaryItem
                  label='Students'
                  value={
                    selectedBusCapacity != null
                      ? `${plannedStudentCount}/${selectedBusCapacity}`
                      : String(plannedStudentCount)
                  }
                  icon={Users}
                />
                <SummaryItem
                  label='Stops'
                  value={String(stopsCount)}
                  icon={Route}
                />
                <SummaryItem
                  label='Driver'
                  value={driverName || 'No driver'}
                  icon={UserCog}
                />
                <SummaryItem
                  label='Attendant'
                  value={attendantName || 'No attendant'}
                  icon={UserCog}
                />
                <SummaryItem
                  label='Started At'
                  value={formatDateTime(route.startedAt) || 'N/A'}
                  icon={PlayCircle}
                />
                <SummaryItem
                  label='Completed At'
                  value={formatDateTime(route.completedAt) || 'N/A'}
                  icon={ClipboardCheck}
                />
              </div>
            </div>

            {/* Assignment Details card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <UserCog className='h-4.5 w-4.5 text-emerald-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>
                    Assignment Details
                  </h3>
                </div>
                <span
                  className={cn(
                    'rounded-full px-2.5 py-0.5 text-[10px] font-bold border shadow-none uppercase tracking-wider',
                    isAssigned
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-250'
                      : 'bg-amber-50 text-amber-700 border-amber-250'
                  )}
                >
                  {isAssigned
                    ? 'Ready for execution'
                    : `Missing ${missingResourceCount} resource(s)`}
                </span>
              </div>

              <div className='grid gap-3'>
                <AssignmentRow
                  label='Bus Vehicle'
                  value={selectedBusPlate}
                  type='bus'
                />
                <AssignmentRow
                  label='Driver'
                  value={driverName}
                  type='driver'
                />
                <AssignmentRow
                  label='Attendant'
                  value={attendantName}
                  type='attendant'
                />
              </div>

              <div className='pt-2'>
                <Button
                  variant={isAssigned ? 'outline' : 'default'}
                  className={cn(
                    'w-full rounded-full text-xs font-semibold h-9',
                    !isAssigned &&
                      'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
                  )}
                  onClick={() => setAssignmentOpen(true)}
                >
                  <UserCog className='h-3.5 w-3.5 mr-1.5' />
                  {isAssigned ? 'Manage assignment' : 'Assign resources'}
                </Button>
              </div>
            </div>

            {/* Next Recommended Actions (Workflow Stepper) */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center gap-2 pb-2.5 border-b border-slate-100'>
                <ClipboardCheck className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                <h3 className='font-bold text-slate-900 text-sm'>
                  Route Workflow
                </h3>
              </div>

              <div className='relative pl-3.5 space-y-6 before:absolute before:left-[23px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100'>
                {/* Step 1: Plan Route */}
                <WorkflowStep
                  stepNumber={1}
                  title='Plan Route'
                  description='Define school, schedule, direction, and draft stops.'
                  status='completed'
                />

                {/* Step 2: Assign Resources */}
                <WorkflowStep
                  stepNumber={2}
                  title='Assign Resources'
                  description='Assign driver and attendant to execute this route.'
                  status={isAssigned ? 'completed' : 'current'}
                  action={
                    !isAssigned ? (
                      <Button
                        size='sm'
                        variant='outline'
                        className='h-7 text-[10px] rounded-full border-slate-200 mt-2 font-semibold shadow-none'
                        onClick={() => setAssignmentOpen(true)}
                      >
                        Assign resources
                      </Button>
                    ) : null
                  }
                />

                {/* Step 3: Create Trip Snapshot */}
                <WorkflowStep
                  stepNumber={3}
                  title='Create Trip Snapshot'
                  description='Lock route structure into an active trip manifest.'
                  status={
                    ['TRIP_CREATED', 'IN_PROGRESS', 'COMPLETED'].includes(
                      route.status
                    )
                      ? 'completed'
                      : isAssigned
                        ? 'available'
                        : 'locked'
                  }
                  action={
                    route.status === 'ASSIGNED' ? (
                      <Button
                        size='sm'
                        className='h-7 text-[10px] rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white mt-2 font-semibold shadow-none'
                        onClick={handleCreateTrip}
                        disabled={creatingTrip}
                      >
                        {creatingTrip ? 'Creating...' : 'Create trip'}
                      </Button>
                    ) : null
                  }
                />

                {/* Step 4: Run Attendance */}
                <WorkflowStep
                  stepNumber={4}
                  title='Run Attendance'
                  description='Check-in and check-out students using manifest lists.'
                  status={
                    ['IN_PROGRESS', 'COMPLETED'].includes(route.status)
                      ? 'completed'
                      : ['TRIP_CREATED'].includes(route.status)
                        ? 'available'
                        : 'locked'
                  }
                  action={
                    ['TRIP_CREATED', 'IN_PROGRESS'].includes(route.status) ? (
                      <Button
                        size='sm'
                        variant='outline'
                        className='h-7 text-[10px] rounded-full border-slate-200 mt-2 font-semibold shadow-none'
                        asChild
                      >
                        <Link href='/school-bus/trips'>
                          Open Trip Operations
                        </Link>
                      </Button>
                    ) : null
                  }
                />

                {/* Step 5: Complete Journey */}
                <WorkflowStep
                  stepNumber={5}
                  title='Complete Trip'
                  description='Mark trip execution as finished.'
                  status={
                    route.status === 'COMPLETED'
                      ? 'completed'
                      : route.status === 'IN_PROGRESS'
                        ? 'current'
                        : 'locked'
                  }
                />
              </div>
            </div>
          </div>

          {/* Right Column */}
          <div className='flex flex-col gap-6 lg:sticky lg:top-6 lg:self-start'>
            {/* Route Map Card with Workspace Wrapper */}
            <div className='bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm flex flex-col'>
              <div className='p-4 border-b border-slate-100 flex items-center justify-between'>
                <h3 className='font-bold text-slate-900 text-sm flex items-center gap-2'>
                  <MapPinned className='h-4.5 w-4.5 text-indigo-600' />
                  Route Map
                </h3>
                {missingStopCoordinates > 0 && (
                  <span className='rounded bg-amber-50 border border-amber-100 text-amber-700 font-bold text-[9px] px-1.5 py-0.5 animate-pulse'>
                    Warning: {missingStopCoordinates} stop(s) missing coords
                  </span>
                )}
              </div>
              <div className='w-full bg-slate-50'>
                <SchoolBusMapWorkspace
                  flat
                  mapHeightClassName='h-[420px]'
                  map={
                    <RouteMap
                      route={detail.route}
                      stops={detail.stops}
                      assignment={detail.assignment}
                      routePath={routePathData?.data}
                      className='h-full w-full'
                      fitKey={fitKey}
                    />
                  }
                  legend={<SchoolBusMapLegend />}
                  onFitAll={() => setFitKey((k) => k + 1)}
                  canFitAll={true}
                />
              </div>
            </div>

            {/* Stop sequence timeline */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Route className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>
                    Stop Sequence Plan
                  </h3>
                </div>
                <span className='text-[10px] text-slate-500 font-bold bg-slate-50 border border-slate-150 px-2 py-0.5 rounded-full'>
                  {detail.stops.length} location(s)
                </span>
              </div>

              {detail.stops.length === 0 ? (
                <SchoolBusEmptyState
                  title='No stops generated yet'
                  description='Generate a route plan to materialize the stop sequence.'
                  icon={Route}
                />
              ) : (
                <div className='relative pl-3 space-y-4 before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100'>
                  {detail.stops.map((stop) => {
                    const isStart = stop.stopPurpose === 'START_TERMINAL';
                    const isEnd = stop.stopPurpose === 'END_TERMINAL';
                    const isDropoff = stop.stopPurpose === 'DROPOFF';

                    return (
                      <div
                        key={stop.id}
                        className='relative flex items-start gap-4 text-xs'
                      >
                        {/* Node */}
                        <span
                          className={cn(
                            'absolute -left-[20px] top-1 flex h-[22px] w-[22px] items-center justify-center rounded-full text-[9px] font-extrabold ring-4 ring-white border shadow-sm',
                            isStart
                              ? 'bg-orange-500 border-orange-655 text-white'
                              : isEnd
                                ? 'bg-red-500 border-red-655 text-white'
                                : isDropoff
                                  ? 'bg-emerald-500 border-emerald-655 text-white'
                                  : 'bg-blue-500 border-blue-655 text-white'
                          )}
                        >
                          {isStart ? 'S' : isEnd ? 'E' : stop.stopOrder}
                        </span>

                        <div className='min-w-0 flex-1 p-3 rounded-xl bg-slate-50/50 border border-slate-100 flex items-center justify-between gap-3'>
                          <div className='min-w-0'>
                            <p className='font-bold text-slate-800 truncate'>
                              {stop.displayName ||
                                stop.pickupPointName ||
                                `Stop #${stop.id}`}
                            </p>
                            <p className='text-[10px] text-slate-450 mt-1 flex items-center gap-1.5'>
                              <span
                                className={cn(
                                  'inline-flex items-center rounded px-1.5 py-0.5 text-[8px] font-extrabold border uppercase tracking-wider',
                                  isStart
                                    ? 'bg-orange-50 text-orange-700 border-orange-100'
                                    : isEnd
                                      ? 'bg-red-50 text-red-700 border-red-100'
                                      : isDropoff
                                        ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                                        : 'bg-blue-50 text-blue-700 border-blue-100'
                                )}
                              >
                                {isStart
                                  ? 'Depot'
                                  : isEnd
                                    ? 'School'
                                    : isDropoff
                                      ? 'Drop-off'
                                      : 'Pickup'}
                              </span>
                              <span>-</span>
                              <span>
                                {stop.estimatedStudentCount || 0} student(s)
                              </span>
                            </p>
                          </div>
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

      <RouteAssignmentDialog
        open={assignmentOpen}
        onOpenChange={setAssignmentOpen}
        initialData={detail.assignment}
        buses={getPageItems(busesData?.data).map((bus) => ({
          id: bus.id,
          label: `${bus.plateNumber} - ${bus.capacity} seats`,
          value: String(bus.id),
        }))}
        drivers={driversData?.data || []}
        attendants={attendantsData?.data || []}
        onSubmit={handleAssign}
        isLoading={assigning}
      />
    </>
  );
}

function StatusCard({
  label,
  value,
  status,
}: {
  label: string;
  value: string;
  status: 'success' | 'warning' | 'muted' | 'danger';
}) {
  return (
    <div className='bg-white border border-slate-205 rounded-2xl p-4 shadow-sm flex flex-col justify-between h-[80px]'>
      <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>
        {label}
      </span>
      <span
        className={cn(
          'text-sm font-extrabold mt-1 inline-flex items-center gap-1.5',
          status === 'success'
            ? 'text-emerald-700'
            : status === 'warning'
              ? 'text-amber-700'
              : status === 'danger'
                ? 'text-rose-700'
                : 'text-slate-400'
        )}
      >
        {status === 'success'
          ? 'OK '
          : status === 'warning'
            ? 'Warning: '
            : status === 'danger'
              ? 'X '
              : ' '}
        {value}
      </span>
    </div>
  );
}

function SummaryItem({
  label,
  value,
  icon: Icon,
  isBadge = false,
  badgeColor = '',
}: {
  label: string;
  value: string | null | undefined;
  icon: any;
  isBadge?: boolean;
  badgeColor?: string;
}) {
  const displayVal = value || 'N/A';
  const isNa = displayVal === 'N/A';
  return (
    <div className='flex items-start gap-2.5 min-w-0'>
      <div className='flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-400 mt-0.5 border border-slate-100'>
        <Icon className='h-3.5 w-3.5' />
      </div>
      <div className='min-w-0 flex-1'>
        <p className='text-[10px] font-semibold text-slate-400 leading-none'>
          {label}
        </p>
        {isBadge && !isNa ? (
          <span
            className={cn(
              'inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-bold border mt-1',
              badgeColor
            )}
          >
            {displayVal}
          </span>
        ) : (
          <p
            className={cn(
              'font-bold text-slate-705 truncate mt-1 text-[11px]',
              isNa && 'text-slate-400 italic font-medium'
            )}
          >
            {displayVal}
          </p>
        )}
      </div>
    </div>
  );
}

function AssignmentRow({
  label,
  value,
  type,
}: {
  label: string;
  value: string | null | undefined;
  type: 'bus' | 'driver' | 'attendant';
}) {
  const Icon = type === 'bus' ? BusFront : type === 'driver' ? UserCog : Users;
  const isMissing = !value;
  return (
    <div
      className={cn(
        'flex items-center justify-between p-3.5 rounded-xl border transition-all duration-155',
        isMissing
          ? 'bg-amber-50/20 border-amber-100 text-amber-800'
          : 'bg-slate-50/50 border-slate-100 text-slate-800'
      )}
    >
      <div className='flex items-center gap-3.5 min-w-0'>
        <div
          className={cn(
            'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border shadow-sm',
            isMissing
              ? 'bg-amber-50 text-amber-500 border-amber-100'
              : 'bg-indigo-50 text-indigo-650 border-indigo-100'
          )}
        >
          <Icon className='h-4 w-4' />
        </div>
        <div className='min-w-0'>
          <p className='text-[10px] font-semibold text-slate-405 leading-none'>
            {label}
          </p>
          <p
            className={cn(
              'font-bold text-slate-800 mt-1 truncate text-xs',
              isMissing && 'text-amber-700 italic font-medium'
            )}
          >
            {value || `Missing ${type}`}
          </p>
        </div>
      </div>
      <span
        className={cn(
          'rounded-full px-2.5 py-0.5 text-[9px] font-extrabold border shadow-none shrink-0 uppercase tracking-wider',
          isMissing
            ? 'bg-amber-50 text-amber-750 border-amber-200'
            : 'bg-emerald-50 text-emerald-700 border-emerald-200'
        )}
      >
        {isMissing ? 'Missing' : 'Assigned'}
      </span>
    </div>
  );
}

function WorkflowStep({
  stepNumber,
  title,
  description,
  status,
  action,
}: {
  stepNumber: number;
  title: string;
  description: string;
  status: 'completed' | 'current' | 'available' | 'locked';
  action?: React.ReactNode;
}) {
  return (
    <div className='relative flex items-start gap-4'>
      <span
        className={cn(
          'z-10 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ring-4 ring-white border shadow-sm',
          status === 'completed'
            ? 'bg-emerald-500 border-emerald-600 text-white'
            : status === 'current'
              ? 'bg-[#C81E3A] border-[#C81E3A] text-white animate-pulse'
              : status === 'available'
                ? 'bg-indigo-50 border-indigo-400 text-indigo-755'
                : 'bg-slate-105 border-slate-200 text-slate-400'
        )}
      >
        {status === 'completed' ? 'OK' : stepNumber}
      </span>

      <div className='min-w-0 flex-1 bg-slate-50/30 border border-slate-100/60 rounded-xl p-3.5 shadow-sm'>
        <p
          className={cn(
            'text-xs font-bold leading-none',
            status === 'completed'
              ? 'text-slate-800'
              : status === 'current'
                ? 'text-[#C81E3A]'
                : status === 'available'
                  ? 'text-indigo-950 font-bold'
                  : 'text-slate-400'
          )}
        >
          {title}
        </p>
        <p className='text-[10px] text-slate-500 mt-1.5 leading-relaxed'>
          {description}
        </p>
        {action}
      </div>
    </div>
  );
}
