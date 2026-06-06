'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CheckCircle2,
  MapPin,
  PlayCircle,
  Route,
  Search,
  Warehouse,
  GraduationCap,
  BusFront,
  User,
  Users,
  AlertTriangle,
  ArrowRightLeft,
  Calendar,
} from 'lucide-react';
import { toast } from 'sonner';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { Button, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useArriveTripStopMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useGetTripsQuery,
  useStartTripMutation,
} from '../api/schoolBusApi';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { formatDate, getPageItems } from '../utils';

const statusMap: Record<string, { label: string; className: string }> = {
  CREATED: { label: 'Created', className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50' },
  IN_PROGRESS: { label: 'In progress', className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50' },
  COMPLETED: { label: 'Completed', className: 'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-50' },
  CANCELLED: { label: 'Cancelled', className: 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50' },
  PAUSED: { label: 'Paused', className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50' },
};

const renderTripStatus = (status: string) => {
  const normalized = status.toUpperCase();
  const config = statusMap[normalized] || {
    label: normalized,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge className={cn('rounded-full px-3 py-0.5 text-xs font-semibold shadow-none border', config.className)}>
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

export function SchoolBusTripsPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetTripsQuery(pagination.params);
  const [startTrip] = useStartTripMutation();
  const [arriveTripStop] = useArriveTripStopMutation();
  const [departTripStop] = useDepartTripStopMutation();
  const [completeTrip] = useCompleteTripMutation();
  const trips = getPageItems(data?.data);

  const [searchTerm, setSearchTerm] = React.useState('');
  const [debouncedSearch, setDebouncedSearch] = React.useState('');
  const [filterStatus, setFilterStatus] = React.useState('');
  const [filterDirection, setFilterDirection] = React.useState('');
  const [filterDate, setFilterDate] = React.useState('');

  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || '');
  }, [debouncedSearch]);

  const filteredTrips = React.useMemo(() => {
    let result = trips;
    if (filterStatus) {
      result = result.filter((t) => t.status === filterStatus);
    }
    if (filterDirection) {
      result = result.filter((t) => t.routeDirection === filterDirection);
    }
    if (filterDate) {
      result = result.filter((t) => t.serviceDate === filterDate);
    }
    return result;
  }, [trips, filterStatus, filterDirection, filterDate]);

  const uniqueDates = React.useMemo(() => {
    const dates = new Set<string>();
    trips.forEach((t) => {
      if (t.serviceDate) dates.add(t.serviceDate);
    });
    return Array.from(dates).sort((a, b) => b.localeCompare(a));
  }, [trips]);

  const dateOptions = React.useMemo(() => {
    return [
      { label: 'All service dates', value: '' },
      ...uniqueDates.map((date) => ({
        label: formatDate(date),
        value: date,
      })),
    ];
  }, [uniqueDates]);

  const statusOptions = [
    { label: 'All statuses', value: '' },
    { label: 'Created', value: 'CREATED', color: 'slate' as const },
    { label: 'In progress', value: 'IN_PROGRESS', color: 'blue' as const },
    { label: 'Completed', value: 'COMPLETED', color: 'green' as const },
    { label: 'Cancelled', value: 'CANCELLED', color: 'red' as const },
  ];

  const directionOptions = [
    { label: 'All directions', value: '' },
    { label: 'Outbound', value: 'OUTBOUND' },
    { label: 'Return', value: 'RETURN' },
  ];

  const call = async (label: string, action: () => Promise<any>) => {
    try {
      const response = await action();
      toast.success(response.message || `${label} completed`);
    } catch (error: any) {
      toast.error(error?.data?.message || `${label} failed`);
    }
  };

  const tripColumns: SchoolBusTableColumn<any>[] = [
    {
      key: 'trip',
      header: 'Trip',
      className: 'pl-6 font-semibold text-slate-900',
      headerClassName: 'pl-6',
      render: (trip) => (
        <div className='flex items-center gap-3 font-semibold text-slate-900'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700 border border-blue-100/50'>
            <Route className='h-4.5 w-4.5' />
          </div>
          <div className='flex flex-col min-w-0'>
            <span className='font-bold text-slate-950 truncate'>{trip.tripCode}</span>
            <span className='text-[10px] text-slate-400 font-normal mt-0.5'>
              {getFriendlyDirection(trip.routeDirection)} · {formatDate(trip.serviceDate)}
            </span>
          </div>
        </div>
      ),
    },
    {
      key: 'route',
      header: 'Route',
      render: (trip) => (
        <div className='flex flex-col min-w-0'>
          <span className='font-bold text-slate-900 truncate'>{trip.routeCode}</span>
          <span className='text-xs text-slate-500 truncate mt-0.5'>{trip.routeName}</span>
        </div>
      ),
    },
    {
      key: 'service',
      header: 'Service',
      render: (trip) => (
        <div className='flex flex-col text-slate-700 gap-0.5'>
          <span className='font-semibold text-xs'>{formatDate(trip.serviceDate)}</span>
          <span className='inline-flex items-center text-[9px] font-extrabold text-slate-500 bg-slate-100 rounded px-1.5 py-0.5 w-fit uppercase tracking-wider'>
            {getFriendlyDirection(trip.routeDirection)}
          </span>
        </div>
      ),
    },
    {
      key: 'startEnd',
      header: 'Start → End',
      render: (trip) => {
        const StartIcon = trip.startLocationType === 'SCHOOL' ? GraduationCap : trip.startLocationType === 'DEPOT' ? Warehouse : MapPin;
        const EndIcon = trip.endLocationType === 'SCHOOL' ? GraduationCap : trip.endLocationType === 'DEPOT' ? Warehouse : MapPin;
        const startColor = trip.startLocationType === 'SCHOOL' ? 'text-red-500 bg-red-50 border-red-100' : 'text-orange-500 bg-orange-50 border-orange-100';
        const endColor = trip.endLocationType === 'SCHOOL' ? 'text-red-500 bg-red-50 border-red-100' : 'text-orange-500 bg-orange-50 border-orange-100';

        return (
          <div className='flex flex-col gap-1 text-slate-700 max-w-[240px]'>
            <div className='flex items-center gap-1.5 text-xs font-semibold text-slate-900 truncate'>
              <div className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded border text-[10px]', startColor)}>
                <StartIcon className='h-3 w-3' />
              </div>
              <span className='truncate' title={trip.startLocationName}>{trip.startLocationName || 'N/A'}</span>
              <span className='text-slate-400 font-normal shrink-0 mx-0.5'>→</span>
              <div className={cn('flex h-5 w-5 shrink-0 items-center justify-center rounded border text-[10px]', endColor)}>
                <EndIcon className='h-3 w-3' />
              </div>
              <span className='truncate' title={trip.endLocationName}>{trip.endLocationName || 'N/A'}</span>
            </div>
            <span className='text-[10px] text-slate-500 font-medium'>
              {formatLocationType(trip.startLocationType)} → {formatLocationType(trip.endLocationType)}
            </span>
          </div>
        );
      },
    },
    {
      key: 'assignment',
      header: 'Assignment',
      render: (trip) => {
        const hasBus = !!trip.busPlateNumber;
        const hasDriver = !!trip.driverName;
        const hasAttendant = !!trip.attendantName;

        return (
          <div className='flex flex-col gap-1 text-xs'>
            {hasBus ? (
              <div className='flex items-center gap-1 font-medium text-slate-700'>
                <BusFront className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                <span className='font-bold bg-slate-50 border border-slate-200/60 rounded px-1.5 py-0.5 text-[10px] font-mono text-slate-800'>{trip.busPlateNumber}</span>
              </div>
            ) : (
              <span className='inline-flex items-center gap-1 text-[9px] font-extrabold text-amber-600 bg-amber-50 border border-amber-100 rounded px-1.5 py-0.5 w-fit'>
                <AlertTriangle className='h-2.5 w-2.5' /> Missing Bus
              </span>
            )}

            {hasDriver ? (
              <div className='flex items-center gap-1 text-slate-600'>
                <User className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                <span className='truncate max-w-[120px]' title={trip.driverName}>{trip.driverName}</span>
              </div>
            ) : (
              <span className='inline-flex items-center gap-1 text-[9px] font-extrabold text-amber-600 bg-amber-50 border border-amber-100 rounded px-1.5 py-0.5 w-fit'>
                <AlertTriangle className='h-2.5 w-2.5' /> Missing Driver
              </span>
            )}

            {hasAttendant ? (
              <div className='flex items-center gap-1 text-slate-500'>
                <Users className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                <span className='truncate max-w-[120px]' title={trip.attendantName}>{trip.attendantName}</span>
              </div>
            ) : null}

            {hasBus && hasDriver && (
              <span className='inline-flex items-center gap-1 text-[9px] font-extrabold uppercase bg-emerald-50 text-emerald-700 border border-emerald-100 rounded px-1.5 py-0.5 w-fit mt-0.5'>
                Assigned
              </span>
            )}
          </div>
        );
      },
    },
    {
      key: 'stopsCount',
      header: 'Stop Progress',
      render: (trip) => {
        const totalStops = trip.stops?.length ?? 0;
        const completedStops = trip.stops?.filter((s: any) => s.status === 'DEPARTED' || s.status === 'SKIPPED').length ?? 0;
        const nextStop = trip.stops?.find((s: any) =>
          ['PENDING', 'ARRIVED', 'BOARDING'].includes(s.status)
        );
        const percent = totalStops > 0 ? (completedStops / totalStops) * 100 : 0;

        return (
          <div className='flex flex-col gap-1 min-w-[120px] max-w-[160px] text-xs'>
            <div className='flex items-center justify-between font-semibold text-slate-700'>
              <span>{completedStops} / {totalStops} stops</span>
              {completedStops === totalStops && totalStops > 0 && (
                <span className='text-[9px] bg-emerald-50 text-emerald-700 px-1 py-0.2 rounded font-bold border border-emerald-100'>Done</span>
              )}
            </div>
            {totalStops > 0 ? (
              <div className='h-1.5 w-full bg-slate-100 rounded-full overflow-hidden'>
                <div
                  className={cn(
                    'h-full transition-all duration-300 rounded-full',
                    completedStops === totalStops ? 'bg-emerald-500' : 'bg-blue-500'
                  )}
                  style={{ width: `${percent}%` }}
                />
              </div>
            ) : (
              <span className='text-[10px] text-slate-400 italic'>No stops</span>
            )}
            {nextStop && (
              <span className='text-[10px] text-slate-500 truncate mt-0.5' title={nextStop.stopName}>
                Next: {nextStop.stopName}
              </span>
            )}
          </div>
        );
      },
    },
    {
      key: 'status',
      header: 'Status',
      render: (trip) => (
        <div className='space-y-1.5'>
          {renderTripStatus(trip.status)}
          {trip.status === 'CANCELLED' && trip.cancellationReason && (
            <p className='text-[10px] text-red-600 bg-red-50 border border-red-100 rounded px-1.5 py-0.5 max-w-[140px] truncate' title={trip.cancellationReason}>
              Reason: {trip.cancellationReason}
            </p>
          )}
        </div>
      ),
    },
    {
      key: 'actions',
      header: 'Operate',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (trip) => {
        const normalized = trip.status.toUpperCase();

        if (normalized === 'CREATED') {
          return (
            <div className='flex items-center justify-end gap-1.5'>
              <Button
                size='sm'
                className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                onClick={() =>
                  call('Start trip', () => startTrip(trip.id).unwrap())
                }
              >
                <PlayCircle className='mr-1.5 h-3.5 w-3.5' />
                Start trip
              </Button>
              <Button
                size='sm'
                variant='outline'
                className='rounded-full h-8 text-xs font-semibold px-3'
                asChild
              >
                <Link href={`/school-bus/trips/${trip.id}`}>
                  Open operations
                </Link>
              </Button>
            </div>
          );
        }

        if (normalized === 'IN_PROGRESS') {
          const nextStop = trip.stops?.find((s: any) =>
            ['PENDING', 'ARRIVED', 'BOARDING'].includes(s.status)
          );

          if (nextStop) {
            const isArrivedOrBoarding = ['ARRIVED', 'BOARDING'].includes(nextStop.status);
            return (
              <div className='flex items-center justify-end gap-1.5'>
                {isArrivedOrBoarding ? (
                  <Button
                    size='sm'
                    className='bg-blue-600 hover:bg-blue-700 text-white rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                    onClick={() =>
                      call('Depart stop', () =>
                        departTripStop({
                          tripId: trip.id,
                          routeStopId: nextStop.routeStopId,
                        }).unwrap()
                      )
                    }
                  >
                    Depart stop
                  </Button>
                ) : (
                  <Button
                    size='sm'
                    className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                    onClick={() =>
                      call('Arrive stop', () =>
                        arriveTripStop({
                          tripId: trip.id,
                          routeStopId: nextStop.routeStopId,
                        }).unwrap()
                      )
                    }
                  >
                    <MapPin className='mr-1.5 h-3.5 w-3.5' />
                    Arrive next stop
                  </Button>
                )}
                <Button
                  size='sm'
                  variant='outline'
                  className='rounded-full h-8 text-xs font-semibold px-3'
                  asChild
                >
                  <Link href={`/school-bus/trips/${trip.id}`}>
                    Open operations
                  </Link>
                </Button>
              </div>
            );
          } else {
            return (
              <div className='flex items-center justify-end gap-1.5'>
                <Button
                  size='sm'
                  className='bg-emerald-600 hover:bg-emerald-700 text-white rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                  onClick={() =>
                    call('Complete trip', () =>
                      completeTrip({ id: trip.id }).unwrap()
                    )
                  }
                >
                  Complete trip
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  className='rounded-full h-8 text-xs font-semibold px-3'
                  asChild
                >
                  <Link href={`/school-bus/trips/${trip.id}`}>
                    Open operations
                  </Link>
                </Button>
              </div>
            );
          }
        }

        return (
          <div className='flex items-center justify-end gap-1.5'>
            <Button
              size='sm'
              variant='outline'
              className='rounded-full h-8 text-xs font-semibold px-3'
              asChild
            >
              <Link href={`/school-bus/trips/${trip.id}`}>
                Open details
              </Link>
            </Button>
            <Button
              size='sm'
              variant='outline'
              className='rounded-full h-8 text-xs font-semibold px-3'
              disabled
            >
              No pending stop
            </Button>
          </div>
        );
      },
    },
  ];

  const tripToolbar = (
    <div className='flex flex-wrap items-center gap-3 justify-between w-full'>
      <div className='relative flex-1 min-w-[240px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search trip or route code...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-350 focus:ring-1 focus:ring-slate-200/50'
        />
      </div>

      <div className='flex flex-wrap items-center gap-2'>
        <SchoolBusSelect
          value={filterStatus}
          onChange={setFilterStatus}
          options={statusOptions}
          placeholder='All statuses'
          className='w-36'
        />
        <SchoolBusSelect
          value={filterDirection}
          onChange={setFilterDirection}
          options={directionOptions}
          placeholder='All directions'
          className='w-36'
        />
        <SchoolBusSelect
          value={filterDate}
          onChange={setFilterDate}
          options={dateOptions}
          placeholder='All service dates'
          className='w-40'
          disabled={uniqueDates.length === 0}
        />
      </div>
    </div>
  );

  return (
    <SchoolBusPageShell
      title='Trip operations'
      description='Trips are operational snapshots created from planned routes. Use this page to start trips and process stop arrival/departure order.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Trips', current: true },
          ]}
        />
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Metrics row */}
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Trips'
            value={data?.data?.totalElements ?? 0}
            hint='Route execution records'
            icon={Route}
            tone='info'
          />
          <SchoolBusMetricCard
            label='In progress'
            value={trips.filter((trip) => trip.status === 'IN_PROGRESS').length}
            hint='Trips currently operating'
            icon={PlayCircle}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Completed'
            value={trips.filter((trip) => trip.status === 'COMPLETED').length}
            hint='Closed trip executions'
            icon={CheckCircle2}
            tone='success'
          />
        </div>

        {/* Data Table within Card */}
        <SchoolBusDataTable
          title='Trip Operations Board'
          description='For each trip, only the next pending stop can be processed by the backend.'
          toolbar={tripToolbar}
          data={filteredTrips}
          columns={tripColumns}
          isLoading={isLoading}
          pagination={{ page: data?.data, onPageChange: pagination.setPage }}
          stickyFirstColumn
          stickyActionColumn
          emptyIcon={Route}
          emptyTitle={trips.length === 0 ? 'No trips yet' : 'No trips match current filters'}
          emptyDescription={
            trips.length === 0
              ? 'Create a trip from a dispatched route before execution can start.'
              : 'Try adjusting your search query or clear the active filters.'
          }
        />
      </div>
    </SchoolBusPageShell>
  );
}
