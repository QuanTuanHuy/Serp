'use client';

import { Activity, BarChart3, Download, FileText } from 'lucide-react';
import { Button } from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import {
  useGetSchoolBusReportAttendanceQuery,
  useGetSchoolBusReportCapacityQuery,
  useGetSchoolBusReportQuery,
  useGetSchoolBusReportTripsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusTableTabs } from '../components/SchoolBusTableTabs';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { formatDate, formatDateTime, getPageItems } from '../utils';

export function SchoolBusReportsPage() {
  const tripPagination = useSchoolBusPagination({
    page: 0,
    size: 8,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const attendancePagination = useSchoolBusPagination({
    page: 0,
    size: 8,
    sortBy: 'recordedAt',
    sortDirection: 'DESC',
  });
  const capacityPagination = useSchoolBusPagination({
    page: 0,
    size: 8,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const { data: summaryData, isLoading } = useGetSchoolBusReportQuery();
  const { data: tripsData } = useGetSchoolBusReportTripsQuery(tripPagination.params);
  const { data: attendanceData } = useGetSchoolBusReportAttendanceQuery(
    attendancePagination.params
  );
  const { data: capacityData } = useGetSchoolBusReportCapacityQuery(
    capacityPagination.params
  );
  const report = summaryData?.data;
  const trips = getPageItems(tripsData?.data);
  const attendance = getPageItems(attendanceData?.data);
  const capacity = getPageItems(capacityData?.data);

  const handleExport = () => {
    window.open(
      `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/school-bus/api/v1/reports/operations-summary/export`,
      '_blank'
    );
  };

  return (
    <SchoolBusPageShell
      title='Operational reporting'
      description='Filtered reports for trips, attendance, and capacity utilization. CSV export is available for the operations summary.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dispatch' },
            { label: 'Reports', current: true },
          ]}
        />
      }
      actions={
        <Button onClick={handleExport}>
          <Download className='mr-2 h-4 w-4' />
          Export CSV
        </Button>
      }
    >
      {isLoading || !report ? (
        <SchoolBusEmptyState
          title='Report summary is loading'
          description='Operations reporting will appear once the backend responds.'
          icon={BarChart3}
        />
      ) : (
        <div className='space-y-6'>
          <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
            <SchoolBusMetricCard
              label='Total requests'
              value={report.totalRequests}
              hint='Transport demand records in scope'
              icon={FileText}
              tone='info'
            />
            <SchoolBusMetricCard
              label='Approved requests'
              value={report.approvedRequests}
              hint='Released into subscriptions'
              icon={Activity}
              tone='success'
            />
            <SchoolBusMetricCard
              label='Completed routes'
              value={report.completedRoutes}
              hint='Route plans closed'
              icon={BarChart3}
              tone='warning'
            />
            <SchoolBusMetricCard
              label='Attendance events'
              value={report.attendanceEvents}
              hint='Boarding/dropoff/absence events'
              icon={Activity}
              tone='default'
            />
          </div>

          <SchoolBusTableTabs
            defaultValue='trips'
            flatContent
            tabs={[
              {
                value: 'trips',
                label: 'Trips',
                count: tripsData?.data?.totalElements || 0,
                content: (
                  <SchoolBusSection
                    title='Trip report'
                    description='Execution records with route, service date, assignment, and status.'
                  >
                    <SchoolBusScrollableTable
                      footer={
                        <SchoolBusPaginationBar
                          page={tripsData?.data}
                          onPageChange={tripPagination.setPage}
                        />
                      }
                    >
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Trip</TableHead>
                            <TableHead>Route</TableHead>
                            <TableHead>Service date</TableHead>
                            <TableHead>Direction</TableHead>
                            <TableHead>Assignment</TableHead>
                            <TableHead>Status</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {trips.map((trip) => (
                            <TableRow key={trip.id}>
                              <TableCell className='font-semibold text-slate-900'>
                                {trip.tripCode}
                              </TableCell>
                              <TableCell>{trip.routeCode}</TableCell>
                              <TableCell>{formatDate(trip.serviceDate)}</TableCell>
                              <TableCell>{trip.routeDirection}</TableCell>
                              <TableCell>
                                {trip.busPlateNumber || 'No bus'} /{' '}
                                {trip.driverName || 'No driver'}
                              </TableCell>
                              <TableCell>
                                <SchoolBusStatusBadge status={trip.status} />
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </SchoolBusScrollableTable>
                  </SchoolBusSection>
                ),
              },
              {
                value: 'attendance',
                label: 'Attendance',
                count: attendanceData?.data?.totalElements || 0,
                content: (
                  <SchoolBusSection
                    title='Attendance report'
                    description='Student-level boarding, dropoff, absent and no-show events.'
                  >
                    <SchoolBusScrollableTable
                      footer={
                        <SchoolBusPaginationBar
                          page={attendanceData?.data}
                          onPageChange={attendancePagination.setPage}
                        />
                      }
                    >
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Student</TableHead>
                            <TableHead>Route</TableHead>
                            <TableHead>Trip</TableHead>
                            <TableHead>Event</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Recorded</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {attendance.map((item) => (
                            <TableRow key={item.id}>
                              <TableCell>{item.studentName}</TableCell>
                              <TableCell>{item.routeCode}</TableCell>
                              <TableCell>{item.tripId || '-'}</TableCell>
                              <TableCell>{item.eventType || item.attendanceType}</TableCell>
                              <TableCell>
                                <SchoolBusStatusBadge status={item.status} />
                              </TableCell>
                              <TableCell>{formatDateTime(item.recordedAt)}</TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </SchoolBusScrollableTable>
                  </SchoolBusSection>
                ),
              },
              {
                value: 'capacity',
                label: 'Capacity',
                count: capacityData?.data?.totalElements || 0,
                content: (
                  <SchoolBusSection
                    title='Capacity utilization'
                    description='Compare planned student load with assigned bus capacity.'
                  >
                    <SchoolBusScrollableTable
                      footer={
                        <SchoolBusPaginationBar
                          page={capacityData?.data}
                          onPageChange={capacityPagination.setPage}
                        />
                      }
                    >
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Trip</TableHead>
                            <TableHead>Route</TableHead>
                            <TableHead>Planned students</TableHead>
                            <TableHead>Bus capacity</TableHead>
                            <TableHead>Utilization</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {capacity.map((row) => (
                            <TableRow key={row.tripId}>
                              <TableCell>{row.tripCode}</TableCell>
                              <TableCell>{row.routeCode}</TableCell>
                              <TableCell>{row.plannedStudents}</TableCell>
                              <TableCell>{row.busCapacity}</TableCell>
                              <TableCell>
                                {row.utilizationPercent.toFixed(1)}%
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </SchoolBusScrollableTable>
                  </SchoolBusSection>
                ),
              },
            ]}
          />
        </div>
      )}
    </SchoolBusPageShell>
  );
}
