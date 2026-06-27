'use client';

import Link from 'next/link';
import * as React from 'react';
import { useMemo } from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  Calendar,
  Clock3,
  Eye,
  GraduationCap,
  Map,
  MapPin,
  Pencil,
  PlayCircle,
  Plus,
  Route,
  Sparkles,
  UserCog,
} from 'lucide-react';

import { cn } from '@/shared/utils';
import { Button, Badge } from '@/shared/components/ui';
import {
  useGetRoutePathQuery,
  useGetRouteByIdQuery,
  useGetRoutesQuery,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import {
  SCHOOL_BUS_PAGE_QUERY_OPTIONS,
  formatDate,
  formatDateTime,
  getPageItems,
} from '../utils';

const routeStatusMap: Record<string, { label: string; className: string }> = {
  PUBLISHED: {
    label: 'Published',
    className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50',
  },
  TRIP_CREATED: {
    label: 'Trip created',
    className:
      'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-50',
  },
  PLANNED: {
    label: 'Planned',
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  },
  ASSIGNED: {
    label: 'Assigned',
    className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50',
  },
  IN_PROGRESS: {
    label: 'In progress',
    className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50',
  },
  COMPLETED: {
    label: 'Completed',
    className:
      'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-50',
  },
  CANCELLED: {
    label: 'Cancelled',
    className: 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50',
  },
};

function RouteStatusBadge({ status }: { status: string }) {
  const cfg = routeStatusMap[status.toUpperCase()] || {
    label: status,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge
      className={cn(
        'rounded-full border px-2.5 py-0.5 text-[11px] font-semibold shadow-none',
        cfg.className
      )}
    >
      {cfg.label}
    </Badge>
  );
}

export function SchoolBusDispatchPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'lastModifiedDate',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetRoutesQuery(
    pagination.params,
    SCHOOL_BUS_PAGE_QUERY_OPTIONS
  );
  const [selectedRouteId, setSelectedRouteId] = React.useState<number | null>(
    null
  );
  const routes = getPageItems(data?.data);

  // Route-level status - reflects planning/dispatch state, NOT execution state
  const plannedRoutes = routes.filter((route) =>
    ['PLANNED', 'ASSIGNED'].includes(route.status)
  ).length;
  const dispatchedRoutes = routes.filter(
    (route) => route.status === 'TRIP_CREATED'
  ).length;

  const prioritizedRoutes = routes;
  const { data: selectedRouteDetail } = useGetRouteByIdQuery(
    selectedRouteId as number,
    { skip: !selectedRouteId }
  );
  const { data: selectedRoutePath } = useGetRoutePathQuery(
    selectedRouteId as number,
    { skip: !selectedRouteId }
  );

  // Middle stops that lack coordinates (terminals use route-level coords instead)
  const selectedRouteMissingCoordinates =
    selectedRouteDetail?.data.stops.filter(
      (stop: any) =>
        stop.stopPurpose !== 'START_TERMINAL' &&
        stop.stopPurpose !== 'END_TERMINAL' &&
        (typeof stop.pickupPointLatitude !== 'number' ||
          typeof stop.pickupPointLongitude !== 'number')
    ).length || 0;

  React.useEffect(() => {
    if (!selectedRouteId && prioritizedRoutes.length > 0) {
      setSelectedRouteId(prioritizedRoutes[0].id);
    }
  }, [prioritizedRoutes, selectedRouteId]);

  return (
    <SchoolBusPageShell
      title='Dispatch board'
      description='Plan, inspect, and move routes through the core execution lifecycle.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Dispatch', current: true },
          ]}
        />
      }
      actions={
        <Button
          asChild
          className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white'
        >
          <Link href='/school-bus/dispatch/planning'>
            <Plus className='h-4 w-4' />
            Plan routes
          </Link>
        </Button>
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Metrics Row */}
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Routes available'
            value={routes.length}
            hint='Current route plans in the tenant'
            icon={Route}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Ready / assigned'
            value={plannedRoutes}
            hint='Routes not yet started but close to execution'
            icon={Map}
            tone='warning'
          />
          <SchoolBusMetricCard
            label='Trip created'
            value={dispatchedRoutes}
            hint='Routes with a trip snapshot locked for execution'
            icon={PlayCircle}
            tone='success'
          />
        </div>

        {/* Dispatch Workspace */}
        <div className='grid gap-6 xl:grid-cols-[1.05fr_0.95fr] xl:items-start'>
          {/* Left Column: Execution Board */}
          <div
            className={cn(
              schoolBusUi.section,
              'flex flex-col gap-4 bg-white p-5 xl:h-[calc(100vh-140px)] xl:overflow-y-auto'
            )}
          >
            <div className='flex flex-col gap-1 pb-2 border-b border-slate-100'>
              <h2 className='text-lg font-bold text-slate-950 flex items-center gap-2'>
                <Route className='h-5 w-5 text-indigo-600' />
                Execution board
              </h2>
              <p className='text-xs text-slate-500'>
                Quick actions remain on the list page, while detail pages cover
                assignment and deeper route inspection.
              </p>
            </div>

            {isLoading ? (
              <p className='text-sm text-slate-500 animate-pulse py-4'>
                Loading routes...
              </p>
            ) : prioritizedRoutes.length === 0 ? (
              <SchoolBusEmptyState
                title='No routes available'
                description='Create or publish route plans before dispatch.'
                icon={Route}
              />
            ) : (
              <div className='space-y-4'>
                {prioritizedRoutes.map((route) => {
                  const hasBus = Boolean(route.busId);
                  const hasDriver = Boolean(route.driverId);
                  const hasAttendant = Boolean(route.attendantId);

                  return (
                    <div
                      key={route.id}
                      className={cn(
                        schoolBusUi.interactiveCard,
                        'p-5 cursor-pointer relative transition-all duration-200 flex flex-col gap-4',
                        selectedRouteId === route.id
                          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-300 border-r-slate-300 bg-slate-50/50 ring-1 ring-slate-100 shadow-sm'
                          : ''
                      )}
                      onClick={() => setSelectedRouteId(route.id)}
                    >
                    {/* Header: Code, Name and Status */}
                    <div className='flex flex-wrap items-center justify-between gap-3'>
                      <div className='flex items-center gap-2.5 min-w-0'>
                        <div className='flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-indigo-50 text-indigo-700 border border-indigo-100/50 font-bold text-xs'>
                          R
                        </div>
                        <div className='min-w-0'>
                          <span className='font-bold text-slate-950 text-sm mr-2 truncate block sm:inline-block'>
                            {route.routeCode}
                          </span>
                          <span className='text-xs font-medium text-slate-500 truncate block sm:inline-block'>
                            {route.routeName}
                          </span>
                        </div>
                      </div>
                      <RouteStatusBadge status={route.status} />
                    </div>

                    {/* Metadata: School, Date, Schedule, Direction */}
                    <div className='flex flex-wrap items-center gap-y-1.5 gap-x-3 text-xs text-slate-500'>
                      <div className='flex items-center gap-1'>
                        <GraduationCap className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                        <span className='font-medium text-slate-700'>
                          {route.schoolName}
                        </span>
                      </div>
                      <span className='text-slate-300 hidden sm:inline'>|</span>
                      <div className='flex items-center gap-1'>
                        <Calendar className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                        <span>{formatDate(route.serviceDate)}</span>
                      </div>

                      <span className='text-slate-300 hidden sm:inline'>|</span>
                      <div className='flex items-center gap-1.5'>
                        <span
                          className={cn(
                            'inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-semibold border',
                            route.routeDirection === 'RETURN'
                              ? 'bg-orange-50 text-orange-700 border-orange-100'
                              : 'bg-blue-50 text-blue-700 border-blue-100'
                          )}
                        >
                          {route.routeDirection === 'RETURN'
                            ? 'Return'
                            : 'Outbound'}
                        </span>
                      </div>
                    </div>

                    {/* Start/End Locations */}
                    <div className='grid grid-cols-1 md:grid-cols-2 gap-2 p-3 rounded-xl bg-slate-50 border border-slate-100 text-xs'>
                      <div className='flex items-start gap-2 min-w-0'>
                        <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-blue-100 text-blue-700 mt-0.5'>
                          <MapPin className='h-3 w-3' />
                        </div>
                        <div className='min-w-0'>
                          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
                            Start Point
                          </p>
                          <p className='font-semibold text-slate-800 truncate mt-0.5'>
                            {route.startLocationName || 'Not set'}
                          </p>
                        </div>
                      </div>
                      <div className='flex items-start gap-2 min-w-0'>
                        <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-emerald-700 mt-0.5'>
                          <MapPin className='h-3 w-3' />
                        </div>
                        <div className='min-w-0'>
                          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
                            End Point
                          </p>
                          <p className='font-semibold text-slate-800 truncate mt-0.5'>
                            {route.endLocationName || 'Not set'}
                          </p>
                        </div>
                      </div>
                    </div>

                    {/* Resource Assignment Status Strip */}
                    <div className='flex flex-wrap gap-2 text-xs'>
                      <span
                        className={cn(
                          'inline-flex items-center gap-1 rounded-lg px-2 py-1 text-[11px] font-medium border shadow-none',
                          hasBus
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                            : 'bg-amber-50 text-amber-700 border-amber-100'
                        )}
                      >
                        {hasBus ? 'OK Bus assigned' : 'Warning: Missing bus'}
                      </span>
                      <span
                        className={cn(
                          'inline-flex items-center gap-1 rounded-lg px-2 py-1 text-[11px] font-medium border shadow-none',
                          hasDriver
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                            : 'bg-amber-50 text-amber-700 border-amber-100'
                        )}
                      >
                        {hasDriver
                          ? 'OK Driver assigned'
                          : 'Warning: Missing driver'}
                      </span>
                      <span
                        className={cn(
                          'inline-flex items-center gap-1 rounded-lg px-2 py-1 text-[11px] font-medium border shadow-none',
                          hasAttendant
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                            : 'bg-amber-50 text-amber-750 border-amber-100'
                        )}
                      >
                        {hasAttendant
                          ? 'OK Attendant assigned'
                          : 'Warning: Missing attendant'}
                      </span>
                    </div>

                    {/* Bottom Metadata & Actions */}
                    <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-t border-slate-100 pt-3'>
                      <div className='flex flex-wrap gap-4 text-xs text-slate-500'>
                        <span>
                          Distance:{' '}
                          <strong className='text-slate-700'>
                            {route.plannedDistanceKm || 0} km
                          </strong>
                        </span>
                        <span>
                          Duration:{' '}
                          <strong className='text-slate-700'>
                            {route.plannedDurationMin || 0} mins
                          </strong>
                        </span>
                        {route.startedAt && (
                          <span>
                            Started:{' '}
                            <strong className='text-slate-700'>
                              {formatDateTime(route.startedAt)}
                            </strong>
                          </span>
                        )}
                      </div>

                      <div
                        className='flex flex-wrap gap-1.5'
                        onClick={(e) => e.stopPropagation()}
                      >
                        {!(
                          route.status === 'ASSIGNED' ||
                          route.status === 'TRIP_CREATED' ||
                          route.status === 'IN_PROGRESS' ||
                          route.status === 'COMPLETED'
                        ) ? (
                          <Button
                            className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white hover:text-white h-8 text-xs gap-1 font-semibold'
                            asChild
                          >
                            <Link
                              href={`/school-bus/dispatch/${route.id}?assign=true`}
                            >
                              <UserCog className='h-3.5 w-3.5' />
                              Assign
                            </Link>
                          </Button>
                        ) : (
                          <Button
                            variant='outline'
                            className='rounded-full border-slate-200 text-slate-700 h-8 text-xs gap-1 font-semibold'
                            asChild
                          >
                            <Link
                              href={`/school-bus/dispatch/${route.id}?assign=true`}
                            >
                              <UserCog className='h-3.5 w-3.5' />
                              Manage assignment
                            </Link>
                          </Button>
                        )}
                        <Button
                          variant='outline'
                          className='rounded-full border-slate-200 text-slate-700 h-8 text-xs gap-1 font-semibold'
                          asChild
                        >
                          <Link href={`/school-bus/dispatch/${route.id}`}>
                            <Eye className='h-3.5 w-3.5' />
                            View details
                          </Link>
                        </Button>
                        <Button
                          variant='outline'
                          className='rounded-full border-red-200 text-[#C81E3A] hover:bg-[#FDECEF]/50 h-8 text-xs gap-1 font-semibold'
                          asChild
                        >
                          <Link
                            href={`/school-bus/dispatch/planning?sessionId=${route.planningSessionId}&routeId=${route.id}`}
                          >
                            <Sparkles className='h-3.5 w-3.5' />
                            Open planning
                          </Link>
                        </Button>
                      </div>
                    </div>
                    </div>
                  );
                })}

                <div className='pt-2'>
                  <SchoolBusPaginationBar
                    page={data?.data}
                    onPageChange={pagination.setPage}
                  />
                </div>
              </div>
            )}
          </div>

          {/* Right Column: Route Preview Map */}
          <div
            className={cn(
              schoolBusUi.section,
              'flex flex-col gap-4 bg-white p-5 xl:sticky xl:top-[88px] xl:h-[calc(100vh-140px)] xl:min-h-[560px] xl:max-h-[850px]'
            )}
          >
            <div className='flex flex-col gap-1 pb-2 border-b border-slate-100'>
              <h2 className='text-lg font-bold text-slate-950 flex items-center gap-2'>
                <Map className='h-5 w-5 text-emerald-600' />
                Route preview map
              </h2>
              <p className='text-xs text-slate-500'>
                Selected route rendered with fixed start, stops, fixed end, and
                stop-order polyline.
              </p>
            </div>

            {selectedRouteDetail?.data ? (
              <div className='rounded-2xl border border-slate-200 overflow-hidden shadow-sm flex flex-col flex-1 min-h-0'>
                <SchoolBusMapWorkspace
                  flat={true}
                  mapHeightClassName='h-[400px] xl:h-full'
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
                    <div className='space-y-4 p-4 min-w-[280px]'>
                      {/* Route Summary */}
                      <div>
                        <h3 className='text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5'>
                          Route Summary
                        </h3>
                        <div className='space-y-2 text-xs'>
                          <div className='flex items-start gap-2 min-w-0'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-600 mt-0.5'>
                              <MapPin className='h-3 w-3' />
                            </div>
                            <div className='min-w-0'>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Start Location
                              </p>
                              <p
                                className='font-medium text-slate-700 truncate mt-0.5'
                                title={
                                  selectedRouteDetail.data.route
                                    .startLocationName
                                }
                              >
                                {selectedRouteDetail.data.route
                                  .startLocationName || 'Not set'}
                              </p>
                            </div>
                          </div>

                          <div className='flex items-start gap-2 min-w-0'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 mt-0.5'>
                              <MapPin className='h-3 w-3' />
                            </div>
                            <div className='min-w-0'>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                End Location
                              </p>
                              <p
                                className='font-medium text-slate-700 truncate mt-0.5'
                                title={
                                  selectedRouteDetail.data.route.endLocationName
                                }
                              >
                                {selectedRouteDetail.data.route
                                  .endLocationName || 'Not set'}
                              </p>
                            </div>
                          </div>

                          <div className='flex items-center gap-2'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-600'>
                              <Route className='h-3 w-3' />
                            </div>
                            <div>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Stops Count
                              </p>
                              <p className='font-medium text-slate-700 mt-0.5'>
                                {selectedRouteDetail.data.stops.length} stop(s)
                              </p>
                            </div>
                          </div>

                          <div className='flex items-center gap-2'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-600'>
                              <Clock3 className='h-3 w-3' />
                            </div>
                            <div>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Status
                              </p>
                              <div className='mt-0.5'>
                                <RouteStatusBadge
                                  status={selectedRouteDetail.data.route.status}
                                />
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>

                      <hr className='border-slate-100' />

                      {/* Route Metrics */}
                      <div>
                        <h3 className='text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5'>
                          Route Metrics
                        </h3>
                        <div className='grid grid-cols-2 gap-2 text-xs'>
                          <div className='p-2 rounded-lg bg-slate-50 border border-slate-100'>
                            <p className='text-[9px] font-semibold text-slate-400 uppercase tracking-wide'>
                              Distance
                            </p>
                            <p className='font-bold text-slate-800 mt-0.5'>
                              {selectedRouteDetail.data.route
                                .plannedDistanceKm || 0}{' '}
                              km
                            </p>
                          </div>
                          <div className='p-2 rounded-lg bg-slate-50 border border-slate-100'>
                            <p className='text-[9px] font-semibold text-slate-400 uppercase tracking-wide'>
                              Duration
                            </p>
                            <p className='font-bold text-slate-800 mt-0.5'>
                              {selectedRouteDetail.data.route
                                .plannedDurationMin || 0}{' '}
                              mins
                            </p>
                          </div>
                        </div>
                      </div>

                      <hr className='border-slate-100' />

                      {/* Missing coordinates warning */}
                      {selectedRouteMissingCoordinates > 0 && (
                        <div className='rounded-lg border border-amber-200 bg-amber-50/50 p-2.5 text-xs text-amber-700 leading-normal'>
                          <p className='font-semibold'>
                            Warning: {selectedRouteMissingCoordinates} stop(s) missing
                            coordinates.
                          </p>
                          <p className='text-[10px] text-amber-600 mt-0.5'>
                            Route path renders only plotted segments.
                          </p>
                        </div>
                      )}
                    </div>
                  }
                />
              </div>
            ) : (
              <div className='flex flex-col items-center justify-center p-12 text-center flex-1 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200 min-h-[400px]'>
                <Map className='h-12 w-12 text-slate-300 stroke-[1.5] mb-3' />
                <h3 className='text-sm font-semibold text-slate-800'>
                  Select a route
                </h3>
                <p className='text-xs text-slate-500 max-w-[240px] mt-1'>
                  Choose a route from the execution board to preview its path.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </SchoolBusPageShell>
  );
}

