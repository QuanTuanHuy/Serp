'use client';

import Link from 'next/link';
import * as React from 'react';
import { useMemo } from 'react';
import {
  Clock3,
  Eye,
  Map,
  Pencil,
  PlayCircle,
  Plus,
  Route,
  Sparkles,
} from 'lucide-react';

import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useCompleteRouteMutation,
  useComputeRoutePathMutation,
  useGetRoutePathQuery,
  useGetRouteByIdQuery,
  useGetRoutesQuery,
  useStartRouteMutation,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime, getPageItems } from '../utils';

export function SchoolBusDispatchPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetRoutesQuery(pagination.params);
  const [startRoute, { isLoading: starting }] = useStartRouteMutation();
  const [completeRoute, { isLoading: completing }] = useCompleteRouteMutation();
  const [computeRoutePath, { isLoading: computingPath }] =
    useComputeRoutePathMutation();
  const [selectedRouteId, setSelectedRouteId] = React.useState<number | null>(
    null
  );
  const routes = getPageItems(data?.data);
  const plannedRoutes = routes.filter((route) =>
    ['PLANNED', 'ASSIGNED'].includes(route.status)
  ).length;
  const inProgressRoutes = routes.filter(
    (route) => route.status === 'IN_PROGRESS'
  ).length;
  const completedRoutes = routes.filter(
    (route) => route.status === 'COMPLETED'
  ).length;

  const prioritizedRoutes = useMemo(
    () =>
      [...routes].sort((left, right) =>
        `${left.serviceDate}${left.routeCode}`.localeCompare(
          `${right.serviceDate}${right.routeCode}`
        )
      ),
    [routes]
  );
  const { data: selectedRouteDetail } = useGetRouteByIdQuery(
    selectedRouteId as number,
    { skip: !selectedRouteId }
  );
  const { data: selectedRoutePath } = useGetRoutePathQuery(
    selectedRouteId as number,
    { skip: !selectedRouteId }
  );
  const selectedRouteMissingCoordinates =
    selectedRouteDetail?.data.stops.filter(
      (stop) =>
        typeof stop.pickupPointLatitude !== 'number' ||
        typeof stop.pickupPointLongitude !== 'number'
    ).length || 0;

  React.useEffect(() => {
    if (!selectedRouteId && prioritizedRoutes.length > 0) {
      setSelectedRouteId(prioritizedRoutes[0].id);
    }
  }, [prioritizedRoutes, selectedRouteId]);

  const handleComputePath = async () => {
    if (!selectedRouteId) {
      return;
    }
    try {
      const response = await computeRoutePath(selectedRouteId).unwrap();
      if (response.data?.estimated) {
        toast.warning(response.data.warning || 'Routing fallback path applied');
      } else {
        toast.success('Computed real route path');
      }
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to compute route path');
    }
  };

  const handleStartRoute = async (routeId: number) => {
    try {
      const response = await startRoute(routeId).unwrap();
      toast.success(response.message || 'Route started');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to start route');
    }
  };

  const handleCompleteRoute = async (routeId: number) => {
    try {
      const response = await completeRoute(routeId).unwrap();
      toast.success(response.message || 'Route completed');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to complete route');
    }
  };

  return (
    <SchoolBusPageShell
      title='Dispatch board'
      description='Plan, inspect, and move routes through the core execution lifecycle.'
      actions={
        <Button asChild className='rounded-full'>
          <Link href='/school-bus/dispatch/planning'>
            <Plus className='h-4 w-4' />
            Plan routes
          </Link>
        </Button>
      }
    >
      <div className='grid gap-4 md:grid-cols-4'>
        <SchoolBusMetricCard
          label='Routes available'
          value={routes.length}
          hint='Current route plans in the tenant'
          icon={Route}
          tone='info'
        />
        <SchoolBusMetricCard
          label='Planned or assigned'
          value={plannedRoutes}
          hint='Routes not yet started but close to execution'
          icon={Map}
          tone='warning'
        />
        <SchoolBusMetricCard
          label='In progress'
          value={inProgressRoutes}
          hint='Trips currently under active execution'
          icon={PlayCircle}
          tone='success'
        />
        <SchoolBusMetricCard
          label='Completed'
          value={completedRoutes}
          hint='Routes already closed and reported'
          icon={Clock3}
          tone='default'
        />
      </div>

      <div className='grid gap-6 xl:grid-cols-[1.05fr_0.95fr]'>
        <SchoolBusSection
          title='Execution board'
          description='Quick actions remain on the list page, while detail pages cover assignment and deeper route inspection.'
        >
          {isLoading ? (
            <p className='text-sm text-muted-foreground'>Loading routes...</p>
          ) : prioritizedRoutes.length === 0 ? (
            <SchoolBusEmptyState
              title='No routes available'
              description='Create the first route to unlock dispatch operations.'
              icon={Route}
            />
          ) : (
            <div className='space-y-4'>
              {prioritizedRoutes.map((route) => (
                <div
                  key={route.id}
                  className={`${schoolBusUi.interactiveCard} p-5 ${
                    selectedRouteId === route.id
                      ? 'border-rose-300 bg-rose-50/70 shadow-[0_18px_48px_rgba(225,29,72,0.14)]'
                      : ''
                  }`}
                  onClick={() => setSelectedRouteId(route.id)}
                >
                <div className='flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between'>
                  <div className='space-y-2'>
                    <div className='flex flex-wrap items-center gap-3'>
                      <p className='text-lg font-semibold'>
                        {route.routeCode} - {route.routeName}
                      </p>
                      <SchoolBusStatusBadge status={route.status} />
                    </div>
                    <p className='text-sm text-muted-foreground'>
                      {route.schoolName} - {formatDate(route.serviceDate)} -{' '}
                      {route.schoolScheduleName || '-'} -{' '}
                      {route.routeDirection === 'RETURN' ? 'Chieu ve' : 'Chieu di'}
                    </p>
                    <div className='flex flex-wrap gap-4 text-xs text-muted-foreground'>
                      <span>
                        Start: {route.startLocationName || 'Not set'}
                      </span>
                      <span>End: {route.endLocationName || 'Not set'}</span>
                      <span>
                        Planned distance: {route.plannedDistanceKm ?? 0} km
                      </span>
                      <span>
                        Planned duration: {route.plannedDurationMin ?? 0} min
                      </span>
                      <span>Started: {formatDateTime(route.startedAt)}</span>
                    </div>
                  </div>

                  <div className='flex flex-wrap gap-2'>
                    <Button variant='outline' className='rounded-full' asChild>
                      <Link href={`/school-bus/dispatch/${route.id}`}>
                        <Eye className='h-4 w-4' />
                        Details
                      </Link>
                    </Button>
                    {['DRAFT', 'PLANNED', 'ASSIGNED'].includes(route.status) ? (
                      <Button variant='outline' className='rounded-full' asChild>
                        <Link href={`/school-bus/dispatch/${route.id}/edit`}>
                          <Pencil className='h-4 w-4' />
                          Edit
                        </Link>
                      </Button>
                    ) : null}
                    <Button variant='outline' className='rounded-full' asChild>
                      <Link href='/school-bus/dispatch/planning'>
                        <Sparkles className='h-4 w-4' />
                        Plan workspace
                      </Link>
                    </Button>
                    {['PLANNED', 'ASSIGNED'].includes(route.status) ? (
                      <Button
                        className='rounded-full'
                        disabled={starting}
                        onClick={() => handleStartRoute(route.id)}
                      >
                        {starting ? 'Starting...' : 'Start route'}
                      </Button>
                    ) : null}
                    {route.status === 'IN_PROGRESS' ? (
                      <Button
                        variant='secondary'
                        className='rounded-full'
                        disabled={completing}
                        onClick={() => handleCompleteRoute(route.id)}
                      >
                        {completing ? 'Completing...' : 'Complete route'}
                      </Button>
                    ) : null}
                  </div>
                </div>
                </div>
              ))}
              <SchoolBusPaginationBar
                page={data?.data}
                onPageChange={pagination.setPage}
              />
            </div>
          )}
        </SchoolBusSection>

        <SchoolBusSection
          title='Route preview map'
          description='Selected route rendered with fixed start, stops, fixed end, and stop-order polyline.'
        >
          {selectedRouteDetail?.data ? (
            <SchoolBusMapWorkspace
              defaultPreset='map-focus'
              map={
                <RouteMap
                  route={selectedRouteDetail.data.route}
                  stops={selectedRouteDetail.data.stops}
                  assignment={selectedRouteDetail.data.assignment}
                  routePath={selectedRoutePath?.data}
                  className='h-full w-full'
                />
              }
              legend={<SchoolBusMapLegend />}
              panel={
                <div className='space-y-3'>
                  <p className='text-sm font-semibold text-slate-950'>
                    Preview context
                  </p>
                  <p className='text-xs text-slate-500'>
                    Start: {selectedRouteDetail.data.route.startLocationName}
                  </p>
                  <p className='text-xs text-slate-500'>
                    End: {selectedRouteDetail.data.route.endLocationName}
                  </p>
                  <p className='text-xs text-slate-500'>
                    Stops: {selectedRouteDetail.data.stops.length}
                  </p>
                  {selectedRouteMissingCoordinates > 0 ? (
                    <p className='rounded-lg border border-amber-200 bg-amber-50 px-2 py-1 text-xs font-medium text-amber-700'>
                      {selectedRouteMissingCoordinates} stop(s) missing coordinates.
                      Route line renders only plotted segments.
                    </p>
                  ) : null}
                  <Button
                    size='sm'
                    variant='outline'
                    className='w-full rounded-full'
                    disabled={!selectedRouteId || computingPath}
                    onClick={handleComputePath}
                  >
                    {computingPath ? 'Computing path...' : 'Compute real path'}
                  </Button>
                  <p className='text-xs text-slate-500'>
                    Status: {selectedRouteDetail.data.route.status}
                  </p>
                </div>
              }
            />
          ) : (
            <SchoolBusEmptyState
              title='Select a route to preview'
              description='The map preview updates when you focus a route from the execution board.'
              icon={Map}
            />
          )}
        </SchoolBusSection>
      </div>
    </SchoolBusPageShell>
  );
}
