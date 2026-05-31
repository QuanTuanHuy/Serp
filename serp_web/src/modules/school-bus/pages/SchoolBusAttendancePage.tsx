'use client';

import * as React from 'react';
import { useRouter } from 'next/navigation';
import {
  CheckCircle2,
  ClipboardCheck,
  PlayCircle,
  Route,
  Search,
  Users,
} from 'lucide-react';
import { useGetTripsQuery } from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { Button } from '@/shared/components/ui';
import { formatDate, getPageItems } from '../utils';

// ─── page ─────────────────────────────────────────────────────────────────────

export function SchoolBusAttendancePage() {
  const router = useRouter();
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 20,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });

  const { data, isLoading } = useGetTripsQuery(pagination.params);
  const trips = getPageItems(data?.data);

  const inProgress = trips.filter((t) => t.status === 'IN_PROGRESS').length;
  const completed = trips.filter((t) => t.status === 'COMPLETED').length;
  const cancelled = trips.filter((t) => t.status === 'CANCELLED').length;

  // Sorting: IN_PROGRESS first, then by serviceDate desc
  const sortedTrips = React.useMemo(
    () => [
      ...trips.filter((t) => t.status === 'IN_PROGRESS'),
      ...trips.filter((t) => t.status !== 'IN_PROGRESS'),
    ],
    [trips],
  );

  const [searchTerm, setSearchTerm] = React.useState('');
  const [debouncedSearch, setDebouncedSearch] = React.useState('');
  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || '');
  }, [debouncedSearch]);

  const attendanceColumns: SchoolBusTableColumn<any>[] = [
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
      key: 'direction',
      header: 'Direction',
      render: (trip) => <span className='text-slate-600'>{trip.routeDirection}</span>,
    },
    {
      key: 'serviceDate',
      header: 'Service date',
      render: (trip) => <span className='text-slate-600'>{formatDate(trip.serviceDate)}</span>,
    },
    {
      key: 'busDriver',
      header: 'Bus / Driver',
      render: (trip) => (
        <div className='text-sm text-slate-700'>
          <p className='font-medium'>{trip.busPlateNumber ?? <span className='text-slate-400'>No bus</span>}</p>
          <p className='text-xs text-slate-500'>{trip.driverName ?? 'No driver'}</p>
        </div>
      ),
    },
    {
      key: 'attendant',
      header: 'Attendant',
      render: (trip) => <span className='text-slate-600'>{trip.attendantName ?? <span className='text-slate-400'>—</span>}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (trip) => <SchoolBusStatusBadge status={trip.status} />,
    },
    {
      key: 'actions',
      header: 'Action',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (trip) => (
        <div className='flex justify-end' onClick={(e) => e.stopPropagation()}>
          <Button
            variant='outline'
            size='sm'
            className='h-8 rounded-full border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900'
            onClick={() => router.push(`/school-bus/attendance/${trip.id}`)}
          >
            <ClipboardCheck className='mr-1.5 h-3.5 w-3.5' />
            Attendance
          </Button>
        </div>
      ),
    },
  ];

  const attendanceToolbar = (
    <div className='flex flex-wrap items-center gap-3 flex-1 min-w-0'>
      <div className='relative flex-1 min-w-[200px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search trip or route...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-300 focus:ring-1 focus:ring-slate-200'
        />
      </div>
    </div>
  );

  return (
    <SchoolBusPageShell
      title='Trip Operations'
      description='Select a trip to manage stop lifecycle, boarding, and drop-off attendance.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Trip Operations', current: true },
          ]}
        />
      }
    >
      {/* ── Metric cards ── */}
      <div className='grid gap-4 md:grid-cols-4'>
        <SchoolBusMetricCard
          label='Total trips'
          value={data?.data?.totalElements ?? trips.length}
          hint='Trip execution records'
          icon={Route}
          tone='info'
        />
        <SchoolBusMetricCard
          label='In progress'
          value={inProgress}
          hint='Trips currently operating'
          icon={PlayCircle}
          tone='warning'
        />
        <SchoolBusMetricCard
          label='Completed'
          value={completed}
          hint='Closed trip executions'
          icon={CheckCircle2}
          tone='success'
        />
        <SchoolBusMetricCard
          label='Cancelled'
          value={cancelled}
          hint='Trips closed without completion'
          icon={Users}
          tone='default'
        />
      </div>

      {/* ── Trip table ── */}
      <SchoolBusSection
        title='Trip board'
        description='Click a row to open the attendance and stop operation workspace for that trip.'
      >
        <SchoolBusDataTable
          toolbar={attendanceToolbar}
          data={sortedTrips}
          columns={attendanceColumns}
          isLoading={isLoading}
          pagination={{ page: data?.data, onPageChange: pagination.setPage }}
          stickyFirstColumn
          stickyActionColumn
          onRowDoubleClick={(row) => router.push(`/school-bus/attendance/${row.id}`)}
          emptyIcon={Route}
          emptyTitle={sortedTrips.length === 0 ? 'No trips found' : 'No trips match current filters'}
          emptyDescription={
            sortedTrips.length === 0
              ? 'Create and assign a route, then generate a trip from the Dispatch page.'
              : 'Try adjusting your search query or clear the active filters.'
          }
          className='border border-slate-200/80 shadow-sm'
        />
      </SchoolBusSection>
    </SchoolBusPageShell>
  );
}

