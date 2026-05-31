'use client';

import * as React from 'react';
import { CheckCircle2, MapPin, PlayCircle, Route, Search } from 'lucide-react';
import { toast } from 'sonner';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { Button } from '@/shared/components/ui';
import {
  useArriveTripStopMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useGetTripsQuery,
  useStartTripMutation,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { formatDate, getPageItems } from '../utils';

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
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || '');
  }, [debouncedSearch]);

  const tripColumns: SchoolBusTableColumn<any>[] = [
    {
      key: 'trip',
      header: 'Trip',
      className: 'pl-6',
      headerClassName: 'pl-6',
      render: (trip) => (
        <div className='flex items-center gap-3 font-semibold text-slate-900'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700 border border-blue-100/50'>
            <Route className='h-4.5 w-4.5' />
          </div>
          <span>{trip.tripCode}</span>
        </div>
      ),
    },
    {
      key: 'route',
      header: 'Route',
      render: (trip) => (
        <div>
          <p className='font-medium text-slate-700'>{trip.routeCode}</p>
          <p className='text-xs text-slate-500'>{trip.routeName}</p>
        </div>
      ),
    },
    {
      key: 'service',
      header: 'Service',
      render: (trip) => (
        <span className='text-slate-600'>
          {formatDate(trip.serviceDate)} - {trip.routeDirection}
        </span>
      ),
    },
    {
      key: 'startEnd',
      header: 'Start → End',
      render: (trip) => (
        <div className='text-sm text-slate-700'>
          <p>{trip.startLocationName || 'N/A'} → {trip.endLocationName || 'N/A'}</p>
          <p className='text-xs text-slate-500'>
            {trip.startLocationType || ''} → {trip.endLocationType || ''}
          </p>
        </div>
      ),
    },
    {
      key: 'assignment',
      header: 'Assignment',
      render: (trip) => (
        <span className='text-slate-600'>
          {trip.busPlateNumber || 'No bus'} / {trip.driverName || 'No driver'}
        </span>
      ),
    },
    {
      key: 'stopsCount',
      header: 'Stops',
      render: (trip) => <span className='text-slate-600'>{trip.stops.length}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (trip) => (
        <div className='space-y-1'>
          <SchoolBusStatusBadge status={trip.status} />
          {trip.status === 'CANCELLED' && trip.cancellationReason && (
            <p className='text-xs text-red-600' title={trip.cancellationReason}>
              {trip.cancellationReason.length > 40
                ? trip.cancellationReason.slice(0, 40) + '…'
                : trip.cancellationReason}
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
        const nextStop = trip.stops.find((stop: any) =>
          ['PENDING', 'ARRIVED', 'BOARDING'].includes(stop.status)
        );
        return (
          <div className='flex justify-end gap-2'>
            <Button
              size='sm'
              variant='outline'
              onClick={() =>
                call('Start trip', () => startTrip(trip.id).unwrap())
              }
            >
              Start
            </Button>
            <Button
              size='sm'
              variant='outline'
              disabled={!nextStop}
              onClick={() =>
                nextStop &&
                call('Arrive stop', () =>
                  arriveTripStop({
                    tripId: trip.id,
                    routeStopId: nextStop.routeStopId,
                  }).unwrap()
                )
              }
            >
              <MapPin className='mr-1 h-4 w-4' />
              Arrive
            </Button>
            <Button
              size='sm'
              variant='outline'
              disabled={!nextStop}
              onClick={() =>
                nextStop &&
                call('Depart stop', () =>
                  departTripStop({
                    tripId: trip.id,
                    routeStopId: nextStop.routeStopId,
                  }).unwrap()
                )
              }
            >
              Depart
            </Button>
            <Button
              size='sm'
              onClick={() =>
                call('Complete trip', () =>
                  completeTrip({ id: trip.id }).unwrap()
                )
              }
            >
              Complete
            </Button>
          </div>
        );
      },
    },
  ];

  const tripToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[200px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search trip or route code...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
    </div>
  );

  const call = async (label: string, action: () => Promise<any>) => {
    try {
      const response = await action();
      toast.success(response.message || `${label} completed`);
    } catch (error: any) {
      toast.error(error?.data?.message || `${label} failed`);
    }
  };

  return (
    <SchoolBusPageShell
      title='Trip execution'
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
          tone='success'
        />
        <SchoolBusMetricCard
          label='Completed'
          value={trips.filter((trip) => trip.status === 'COMPLETED').length}
          hint='Closed trip executions'
          icon={CheckCircle2}
          tone='default'
        />
      </div>

      <SchoolBusSection
        title='Trip control board'
        description='For each trip, only the next pending stop can be processed by the backend.'
      >
        <SchoolBusDataTable
          toolbar={tripToolbar}
          data={trips}
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
          className='border border-slate-200/80 shadow-sm'
        />
      </SchoolBusSection>
    </SchoolBusPageShell>
  );
}
