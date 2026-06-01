'use client';

import * as React from 'react';
import { useRouter } from 'next/navigation';
import {
  CheckCircle2,
  ClipboardCheck,
  PlayCircle,
  Route,
  Search,
  Warehouse,
  GraduationCap,
  BusFront,
  User,
  Users,
  AlertTriangle,
  XCircle,
  ExternalLink,
} from 'lucide-react';
import { useGetTripsQuery } from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { SchoolBusDatePicker } from '../components/ui/SchoolBusDatePicker';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { Button, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
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

const getFriendlyDirection = (dir?: string | null) => {
  if (dir === 'RETURN') return 'Return';
  if (dir === 'OUTBOUND') return 'Outbound';
  if (dir === 'ROUND_TRIP') return 'Round trip';
  return dir || '';
};

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

  // Metrics (calculated on overall loaded trips)
  const inProgressCount = trips.filter((t) => t.status === 'IN_PROGRESS').length;
  const completedCount = trips.filter((t) => t.status === 'COMPLETED').length;
  const cancelledCount = trips.filter((t) => t.status === 'CANCELLED').length;

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

  const attendanceColumns: SchoolBusTableColumn<any>[] = [
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
            <span className='font-bold text-slate-955 truncate'>{trip.tripCode}</span>
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
      key: 'direction',
      header: 'Direction',
      render: (trip) => (
        <span className='inline-flex items-center text-[10px] font-extrabold text-slate-600 bg-slate-100 rounded px-2 py-0.5 uppercase tracking-wider'>
          {getFriendlyDirection(trip.routeDirection)}
        </span>
      ),
    },
    {
      key: 'serviceDate',
      header: 'Service date',
      render: (trip) => <span className='font-semibold text-slate-700 text-xs'>{formatDate(trip.serviceDate)}</span>,
    },
    {
      key: 'busDriver',
      header: 'Bus / Driver',
      render: (trip) => {
        const hasBus = !!trip.busPlateNumber;
        const hasDriver = !!trip.driverName;
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
          </div>
        );
      },
    },
    {
      key: 'attendant',
      header: 'Attendant',
      render: (trip) => {
        const hasAttendant = !!trip.attendantName;
        return hasAttendant ? (
          <div className='flex items-center gap-1 text-xs text-slate-600'>
            <Users className='h-3.5 w-3.5 text-slate-400 shrink-0' />
            <span className='truncate max-w-[120px]' title={trip.attendantName}>{trip.attendantName}</span>
          </div>
        ) : (
          <span className='text-xs text-slate-400 italic'>—</span>
        );
      },
    },
    {
      key: 'status',
      header: 'Status',
      render: (trip) => renderTripStatus(trip.status),
    },
    {
      key: 'actions',
      header: 'Action',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (trip) => {
        const normalized = trip.status.toUpperCase();
        
        let label = 'Open operations';
        let icon = <ClipboardCheck className='mr-1.5 h-3.5 w-3.5' />;
        let btnVariant: 'default' | 'outline' = 'outline';
        let btnClass = '';
        const disabled = false;

        if (normalized === 'IN_PROGRESS') {
          label = 'Continue attendance';
          btnVariant = 'default';
          btnClass = 'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0 font-bold';
        } else if (normalized === 'COMPLETED') {
          label = 'Review attendance';
          icon = <CheckCircle2 className='mr-1.5 h-3.5 w-3.5 text-emerald-600' />;
        } else if (normalized === 'CANCELLED') {
          label = 'View details';
          icon = <XCircle className='mr-1.5 h-3.5 w-3.5 text-red-500' />;
        } else if (normalized === 'CREATED') {
          label = 'Open operations';
        }

        return (
          <div className='flex justify-end' onClick={(e) => e.stopPropagation()}>
            <Button
              variant={btnVariant}
              size='sm'
              className={cn('h-8 rounded-full text-xs font-semibold px-4 shrink-0', btnClass)}
              disabled={disabled}
              onClick={() => router.push(`/school-bus/attendance/${trip.id}`)}
            >
              {icon}
              {label}
            </Button>
          </div>
        );
      },
    },
  ];

  const attendanceToolbar = (
    <div className='flex flex-wrap items-center gap-3 justify-between w-full'>
      <div className='relative flex-1 min-w-[240px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Search trip or route...'
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
        <SchoolBusDatePicker
          value={filterDate}
          onChange={setFilterDate}
          placeholder='All dates'
          size='sm'
          className='w-40'
        />
      </div>
    </div>
  );

  return (
    <SchoolBusPageShell
      title='Trip Operations Board'
      description='Manage stop lifecycle, boarding, and drop-off attendance for active route execution records.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Trip Operations', current: true },
          ]}
        />
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Metrics Row */}
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
            value={inProgressCount}
            hint='Trips currently operating'
            icon={PlayCircle}
            tone='warning'
          />
          <SchoolBusMetricCard
            label='Completed'
            value={completedCount}
            hint='Closed trip executions'
            icon={CheckCircle2}
            tone='success'
          />
          <SchoolBusMetricCard
            label='Cancelled'
            value={cancelledCount}
            hint='Trips closed without completion'
            icon={XCircle}
            tone='default'
          />
        </div>

        {/* Data Table within Card */}
        <SchoolBusDataTable
          title='Trip operations list'
          description='Double click a row or click details to open the attendance and stop operation workspace for that trip.'
          toolbar={attendanceToolbar}
          data={filteredTrips}
          columns={attendanceColumns}
          isLoading={isLoading}
          pagination={{ page: data?.data, onPageChange: pagination.setPage }}
          stickyFirstColumn
          stickyActionColumn
          onRowDoubleClick={(row) => router.push(`/school-bus/attendance/${row.id}`)}
          emptyIcon={Route}
          emptyTitle={filteredTrips.length === 0 ? 'No trips found' : 'No trips match current filters'}
          emptyDescription={
            filteredTrips.length === 0
              ? 'Create and assign a route, then generate a trip from the Dispatch page.'
              : 'Try adjusting your search query or clear the active filters.'
          }
        />
      </div>
    </SchoolBusPageShell>
  );
}
