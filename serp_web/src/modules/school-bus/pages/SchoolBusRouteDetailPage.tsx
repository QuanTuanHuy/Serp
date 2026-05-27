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
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useAssignRouteMutation,
  useComputeRoutePathMutation,
  useCreateTripFromRouteMutation,
  useGetAttendantsQuery,
  useGetBusesQuery,
  useGetDriversQuery,
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
import { formatDate, formatDateTime, getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

interface SchoolBusRouteDetailPageProps {
  routeId: number;
}

export function SchoolBusRouteDetailPage({
  routeId,
}: SchoolBusRouteDetailPageProps) {
  const { data, isLoading } = useGetRouteByIdQuery(routeId);
  const { data: busesData } = useGetBusesQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'plateNumber',
  });
  const { data: driversData } = useGetDriversQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const { data: attendantsData } = useGetAttendantsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const [computeRoutePath, { isLoading: computingPath }] =
    useComputeRoutePathMutation();
  const [assignRoute, { isLoading: assigning }] = useAssignRouteMutation();
  const [createTripFromRoute, { isLoading: creatingTrip }] =
    useCreateTripFromRouteMutation();
  const [assignmentOpen, setAssignmentOpen] = React.useState(false);
  const { data: routePathData } = useGetRoutePathQuery(routeId);

  const detail = data?.data;


  const handleComputePath = async () => {
    try {
      const response = await computeRoutePath(routeId).unwrap();
      if (response.data?.estimated) {
        toast.warning(response.data.warning || 'Routing fallback path applied');
      } else {
        toast.success('Computed real route path');
      }
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to compute route path');
    }
  };

  const handleAssign = async (values: any) => {
    try {
      const response = await assignRoute({ id: routeId, body: values }).unwrap();
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
      <SchoolBusPageShell title='Route detail' description='Loading route detail...'>
        <SchoolBusEmptyState
          title='Loading route detail'
          description='Fetching route, stop, and assignment data.'
        />
      </SchoolBusPageShell>
    );
  }

  const route = detail.route;
  const missingStopCoordinates = detail.stops.filter(
    (stop) =>
      typeof stop.pickupPointLatitude !== 'number' ||
      typeof stop.pickupPointLongitude !== 'number'
  ).length;

  return (
    <>
      <SchoolBusPageShell
        title={`${route.routeCode} - ${route.routeName}`}
        description='Inspect route state, stop plan, and assignment details before and during execution.'
        breadcrumb={
          <div className='space-y-2'>
            <SchoolBusBreadcrumb
              items={[
                { label: 'School Bus Ops', href: '/school-bus/dispatch' },
                { label: 'Dispatch', href: '/school-bus/dispatch' },
                { label: `${route.routeCode} - ${route.routeName}`, current: true },
              ]}
            />
            {/* Section scroll sub-nav */}
            <div className='flex flex-wrap items-center gap-x-4 gap-y-1'>
              {([
                { id: 'section-summary', label: 'Route summary' },
                { id: 'section-assignment', label: 'Assignment' },
                { id: 'section-map', label: 'Map & stops' },
                { id: 'section-next', label: 'Next action' },
              ] as const).map((s) => (
                <button
                  key={s.id}
                  type='button'
                  onClick={() => {
                    const el = document.getElementById(s.id);
                    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
                  }}
                  className='text-[12px] text-slate-400 transition-colors duration-150 hover:text-rose-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-500'
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>
        }
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
              variant='outline'
              className='rounded-full'
              onClick={() => setAssignmentOpen(true)}
            >
              <UserCog className='h-4 w-4' />
              Assign
            </Button>
            <Button variant='outline' className='rounded-full' asChild>
              <Link href='/school-bus/dispatch/planning'>
                <Sparkles className='h-4 w-4' />
                Plan workspace
              </Link>
            </Button>
            <Button
              variant='outline'
              className='rounded-full'
              onClick={handleComputePath}
            >
              <MapPinned className='h-4 w-4' />
              {computingPath ? 'Computing...' : 'Compute real path'}
            </Button>
            {route.status === 'ASSIGNED' ? (
              <Button
                variant='outline'
                className='rounded-full'
                onClick={handleCreateTrip}
              >
                <BusFront className='h-4 w-4' />
                {creatingTrip ? 'Creating trip...' : 'Create trip'}
              </Button>
            ) : null}
          </>
        }
      >
        <div className='grid gap-6 xl:grid-cols-[0.9fr_1.1fr]'>
          <SchoolBusSection
            id='section-summary'
            title='Route summary'
            description='Core planning and execution metadata.'
          >
            <div className='grid gap-4 md:grid-cols-2'>
              <InfoCard label='School' value={route.schoolName} />
              <InfoCard label='Status' value={route.status} badge />
              <InfoCard
                label='Direction'
                value={route.routeDirection === 'RETURN' ? 'Chieu ve' : 'Chieu di'}
              />
              <InfoCard label='Service date' value={formatDate(route.serviceDate)} />
              <InfoCard label='School schedule' value={route.schoolScheduleName} />
              <InfoCard
                label='Start'
                value={`${route.startLocationName} (${route.startLocationType})`}
              />
              <InfoCard
                label='End'
                value={`${route.endLocationName} (${route.endLocationType})`}
              />
              <InfoCard
                label='Planned distance'
                value={`${route.plannedDistanceKm ?? 0} km`}
              />
              <InfoCard
                label='Planned duration'
                value={`${route.plannedDurationMin ?? 0} min`}
              />
              <InfoCard label='Started at' value={formatDateTime(route.startedAt)} />
              <InfoCard
                label='Completed at'
                value={formatDateTime(route.completedAt)}
              />
            </div>
          </SchoolBusSection>

          <SchoolBusSection
            id='section-assignment'
            title='Assignment'
            description='Current crew and vehicle attached to this route.'
          >
            {detail.assignment ? (
              <div className='grid gap-4 md:grid-cols-2'>
                <InfoCard label='Bus' value={detail.assignment.busPlateNumber} />
                <InfoCard label='Driver' value={detail.assignment.driverName} />
                <InfoCard
                  label='Attendant'
                  value={detail.assignment.attendantName || 'No attendant'}
                />
                <InfoCard
                  label='Assigned at'
                  value={formatDateTime(detail.assignment.assignedAt)}
                />
              </div>
            ) : (
              <SchoolBusEmptyState
                title='No assignment yet'
                description='Assign a bus and crew before route execution.'
                icon={UserCog}
              />
            )}
          </SchoolBusSection>
        </div>

        <div className='space-y-6'>
          <SchoolBusSection
            id='section-map'
            title='Route map and stop plan'
            description='Map-first workspace with fixed start/end and ordered stop list.'
          >
            <SchoolBusMapWorkspace
              defaultPreset='map-focus'
              map={
                <RouteMap
                  route={detail.route}
                  stops={detail.stops}
                  assignment={detail.assignment}
                  routePath={routePathData?.data}
                  className='h-full w-full'
                />
              }
              legend={<SchoolBusMapLegend />}
              panel={
                detail.stops.length === 0 ? (
                  <SchoolBusEmptyState
                    title='No stops generated yet'
                    description='Generate a route plan to materialize the stop sequence.'
                    icon={Route}
                  />
                ) : (
                  <div className='space-y-3'>
                    {missingStopCoordinates > 0 ? (
                      <p className='rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-xs font-medium text-amber-700'>
                        {missingStopCoordinates} stop(s) do not have coordinates.
                        Map keeps rendering plotted segments without crashing.
                      </p>
                    ) : null}
                    <SchoolBusScrollableTable>
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Order</TableHead>
                            <TableHead>
                              {route.routeDirection === 'RETURN'
                                ? 'Drop-off point'
                                : 'Pickup point'}
                            </TableHead>
                            <TableHead>Stop type</TableHead>
                            <TableHead>Students</TableHead>
                            <TableHead>Arrival</TableHead>
                            <TableHead>Departure</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {detail.stops.map((stop) => (
                            <TableRow key={stop.id}>
                              <TableCell>{stop.stopOrder}</TableCell>
                              <TableCell>{stop.pickupPointName}</TableCell>
                              <TableCell>
                                {stop.stopType === 'DROPOFF'
                                  ? 'Drop-off'
                                  : 'Pickup'}
                              </TableCell>
                              <TableCell>
                                {stop.estimatedStudentCount ?? 0}
                              </TableCell>
                              <TableCell>
                                {stop.plannedArrivalTime || 'N/A'}
                              </TableCell>
                              <TableCell>
                                {stop.plannedDepartureTime || 'N/A'}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </SchoolBusScrollableTable>
                  </div>
                )
              }
            />
          </SchoolBusSection>

          <SchoolBusSection
            id='section-next'
            title='Next action'
            description='Move from planning into execution and attendance.'
          >
            <div className='space-y-3'>
              <ActionCard
                icon={Route}
                title='Inspect the manifest'
                description='Use the attendance page to open this route manifest once resources are assigned.'
              />
              <ActionCard
                icon={ClipboardCheck}
                title='Run attendance'
                description='Check-in and check-out happen from the attendance module using the attendance manifest.'
              />
              <ActionCard
                icon={MapPinned}
                title='Adjust the route'
                description='Edit route metadata or regenerate the plan if the stop sequence needs to change.'
              />
            </div>
          </SchoolBusSection>
        </div>
      </SchoolBusPageShell>

      <RouteAssignmentDialog
        open={assignmentOpen}
        onOpenChange={setAssignmentOpen}
        initialData={detail.assignment}
        buses={getPageItems(busesData?.data)}
        drivers={getPageItems(driversData?.data)}
        attendants={getPageItems(attendantsData?.data)}
        onSubmit={handleAssign}
        isLoading={assigning}
      />
    </>
  );
}

function InfoCard({
  label,
  value,
  badge = false,
}: {
  label: string;
  value: string;
  badge?: boolean;
}) {
  return (
    <div className={schoolBusUi.interactiveCard}>
      <p className='text-sm text-slate-500'>{label}</p>
      <div className='mt-2'>
        {badge ? (
          <SchoolBusStatusBadge status={value} />
        ) : (
          <p className='font-medium text-slate-950'>{value}</p>
        )}
      </div>
    </div>
  );
}

function ActionCard({
  icon: Icon,
  title,
  description,
}: {
  icon: typeof Route;
  title: string;
  description: string;
}) {
  return (
    <div className={schoolBusUi.interactiveCard}>
      <div className='flex items-start gap-3'>
        <div className='rounded-full bg-rose-50 p-2 text-rose-600 ring-1 ring-rose-100'>
          <Icon className='h-4 w-4' />
        </div>
        <div>
          <p className='font-medium text-slate-950'>{title}</p>
          <p className='mt-1 text-sm leading-6 text-slate-500'>
            {description}
          </p>
        </div>
      </div>
    </div>
  );
}
