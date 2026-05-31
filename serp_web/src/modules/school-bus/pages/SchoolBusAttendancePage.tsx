'use client';

import * as React from 'react';
import { useRouter } from 'next/navigation';
import {
  BusFront,
  CheckCircle2,
  ClipboardCheck,
  PlayCircle,
  Users,
} from 'lucide-react';
import {
  useGetTripsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
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
          icon={BusFront}
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
        {isLoading ? (
          <SchoolBusEmptyState
            title='Loading trips…'
            description=''
            icon={ClipboardCheck}
          />
        ) : sortedTrips.length === 0 ? (
          <SchoolBusEmptyState
            title='No trips found'
            description='Create and assign a route, then generate a trip from the Dispatch page.'
            icon={BusFront}
          />
        ) : (
          <SchoolBusScrollableTable
            footer={
              <SchoolBusPaginationBar
                page={data?.data}
                onPageChange={pagination.setPage}
              />
            }
          >
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Trip</TableHead>
                  <TableHead>Route</TableHead>
                  <TableHead>Direction</TableHead>
                  <TableHead>Service date</TableHead>
                  <TableHead>Bus / Driver</TableHead>
                  <TableHead>Attendant</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className='w-[140px] text-right'>Action</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sortedTrips.map((trip) => (
                  <TableRow
                    key={trip.id}
                    className='cursor-pointer transition-colors hover:bg-slate-50'
                    onClick={() => router.push(`/school-bus/attendance/${trip.id}`)}
                  >
                    <TableCell>
                      <span className='font-semibold text-slate-900'>
                        {trip.tripCode}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div>
                        <p className='font-medium text-slate-900'>{trip.routeCode}</p>
                        <p className='text-xs text-slate-400'>{trip.routeName}</p>
                      </div>
                    </TableCell>
                    <TableCell className='text-sm text-slate-600'>
                      {trip.routeDirection}
                    </TableCell>
                    <TableCell className='text-sm text-slate-600'>
                      {formatDate(trip.serviceDate)}
                    </TableCell>
                    <TableCell>
                      <div className='text-sm'>
                        <p className='font-medium text-slate-900'>
                          {trip.busPlateNumber ?? <span className='text-slate-400'>No bus</span>}
                        </p>
                        <p className='text-xs text-slate-400'>
                          {trip.driverName ?? 'No driver'}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell className='text-sm text-slate-600'>
                      {trip.attendantName ?? (
                        <span className='text-slate-400'>—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <SchoolBusStatusBadge status={trip.status} />
                    </TableCell>
                    <TableCell className='text-right'>
                      <Button
                        variant='outline'
                        size='sm'
                        className='h-8 rounded-full border-slate-200 text-slate-700 hover:bg-slate-50 hover:text-slate-900'
                        onClick={(e) => {
                          e.stopPropagation();
                          router.push(`/school-bus/attendance/${trip.id}`);
                        }}
                      >
                        <ClipboardCheck className='mr-1.5 h-3.5 w-3.5' />
                        Attendance
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SchoolBusScrollableTable>
        )}
      </SchoolBusSection>
    </SchoolBusPageShell>
  );
}

