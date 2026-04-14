'use client';

import * as React from 'react';
import {
  CheckCircle2,
  ClipboardCheck,
  History,
  LogOut,
  Route,
  UserRoundCheck,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import {
  useCheckInStudentMutation,
  useCheckOutStudentMutation,
  useGetAttendanceQuery,
  useGetRouteAttendanceManifestQuery,
  useGetRouteByIdQuery,
  useGetRoutesQuery,
  useGetTripHistoryQuery,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime, getPageItems } from '../utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

export function SchoolBusAttendancePage() {
  const attendancePagination = useSchoolBusPagination({
    page: 0,
    size: 8,
    sortBy: 'recordedAt',
    sortDirection: 'DESC',
  });
  const historyPagination = useSchoolBusPagination({
    page: 0,
    size: 6,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const { data: attendanceData } = useGetAttendanceQuery(
    attendancePagination.params
  );
  const { data: historyData } = useGetTripHistoryQuery(historyPagination.params);
  const { data: routesData } = useGetRoutesQuery({
    page: 0,
    size: 100,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const [checkInStudent, { isLoading: checkingIn }] = useCheckInStudentMutation();
  const [checkOutStudent, { isLoading: checkingOut }] =
    useCheckOutStudentMutation();
  const activeRoutes = React.useMemo(
    () =>
      getPageItems(routesData?.data).filter((route) =>
        ['ASSIGNED', 'IN_PROGRESS', 'PLANNED'].includes(route.status)
      ),
    [routesData]
  );
  const [selectedRouteId, setSelectedRouteId] = React.useState<number | null>(
    null
  );

  React.useEffect(() => {
    if (!selectedRouteId && activeRoutes.length > 0) {
      setSelectedRouteId(activeRoutes[0].id);
    }
  }, [activeRoutes, selectedRouteId]);

  const { data: manifestData, isLoading: manifestLoading } =
    useGetRouteAttendanceManifestQuery(selectedRouteId as number, {
      skip: !selectedRouteId,
    });
  const { data: routeDetailData } = useGetRouteByIdQuery(selectedRouteId as number, {
    skip: !selectedRouteId,
  });
  const selectedRouteDetail = routeDetailData?.data;
  const selectedRouteMissingCoordinates =
    selectedRouteDetail?.stops.filter(
      (stop) =>
        typeof stop.pickupPointLatitude !== 'number' ||
        typeof stop.pickupPointLongitude !== 'number'
    ).length || 0;

  const attendance = getPageItems(attendanceData?.data);
  const history = getPageItems(historyData?.data);
  const manifest = manifestData?.data;

  const checkedInCount = attendance.filter(
    (item) => item.attendanceType === 'CHECKED_IN'
  ).length;
  const checkedOutCount = attendance.filter(
    (item) => item.attendanceType === 'CHECKED_OUT'
  ).length;
  const completedTrips = history.filter((trip) => trip.status === 'COMPLETED').length;

  const handleCheckIn = async (studentId: number) => {
    if (!selectedRouteId) {
      return;
    }

    try {
      const response = await checkInStudent({
        routeId: selectedRouteId,
        studentId,
      }).unwrap();
      toast.success(response.message || 'Check-in recorded');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to record check-in');
    }
  };

  const handleCheckOut = async (studentId: number) => {
    if (!selectedRouteId) {
      return;
    }

    try {
      const response = await checkOutStudent({
        routeId: selectedRouteId,
        studentId,
      }).unwrap();
      toast.success(response.message || 'Check-out recorded');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to record check-out');
    }
  };

  return (
    <SchoolBusPageShell
      title='Attendance and trip history'
      description='Attendance is now route-driven: open a route manifest, record check-in and check-out, and review the historical trail.'
    >
      <div className='grid gap-4 md:grid-cols-4'>
        <SchoolBusMetricCard
          label='Attendance events'
          value={attendance.length}
          hint='All attendance records captured in the tenant'
          icon={ClipboardCheck}
          tone='info'
        />
        <SchoolBusMetricCard
          label='Checked in'
          value={checkedInCount}
          hint='Students marked as boarded'
          icon={UserRoundCheck}
          tone='success'
        />
        <SchoolBusMetricCard
          label='Checked out'
          value={checkedOutCount}
          hint='Students marked as dropped off'
          icon={LogOut}
          tone='default'
        />
        <SchoolBusMetricCard
          label='Completed trips'
          value={completedTrips}
          hint='Trips fully closed in history'
          icon={History}
          tone='warning'
        />
      </div>

      <div className='grid gap-6 xl:grid-cols-[1.15fr_0.85fr]'>
        <SchoolBusSection
          title='Attendance manifest'
          description='Select an active route to open the student manifest and record attendance.'
        >
          {activeRoutes.length === 0 ? (
            <SchoolBusEmptyState
              title='No active routes available'
              description='Create and plan routes before attendance can be recorded.'
              icon={Route}
            />
          ) : (
            <div className='space-y-4'>
              <div className='flex flex-wrap gap-2'>
                {activeRoutes.map((route) => (
                  <Button
                    key={route.id}
                    variant={selectedRouteId === route.id ? 'default' : 'outline'}
                    className='rounded-full'
                    onClick={() => setSelectedRouteId(route.id)}
                  >
                    {route.routeCode}
                  </Button>
                ))}
              </div>

              {manifestLoading || !manifest ? (
                <p className='text-sm text-muted-foreground'>
                  Loading attendance manifest...
                </p>
              ) : (
                <>
                  {selectedRouteDetail ? (
                    <SchoolBusMapWorkspace
                      defaultPreset='map-focus'
                      mapHeightClassName='h-[400px]'
                      map={
                        <RouteMap
                          route={selectedRouteDetail.route}
                          stops={selectedRouteDetail.stops}
                          assignment={selectedRouteDetail.assignment}
                          className='h-full w-full'
                        />
                      }
                      legend={<SchoolBusMapLegend />}
                      panel={
                        <div className='space-y-3'>
                          <p className='text-sm font-semibold text-slate-950'>
                            Route context
                          </p>
                          <p className='text-xs text-slate-500'>
                            Start:{' '}
                            {selectedRouteDetail.route.startLocationName || 'Not set'}
                          </p>
                          <p className='text-xs text-slate-500'>
                            End:{' '}
                            {selectedRouteDetail.route.endLocationName || 'Not set'}
                          </p>
                          <p className='text-xs text-slate-500'>
                            Stops: {selectedRouteDetail.stops.length}
                          </p>
                          <p className='text-xs text-slate-500'>
                            Direction:{' '}
                            {selectedRouteDetail.route.routeDirection === 'RETURN'
                              ? 'Chieu ve'
                              : 'Chieu di'}
                          </p>
                          {selectedRouteMissingCoordinates > 0 ? (
                            <p className='rounded-lg border border-amber-200 bg-amber-50 px-2 py-1 text-xs font-medium text-amber-700'>
                              {selectedRouteMissingCoordinates} stop(s) missing coordinates.
                              Map renders available segments only.
                            </p>
                          ) : null}
                        </div>
                      }
                    />
                  ) : null}

                  <div className={schoolBusUi.interactiveCard}>
                    <div className='flex flex-wrap items-center justify-between gap-4'>
                      <div>
                        <p className='font-medium'>
                          {manifest.route.routeCode} - {manifest.route.routeName}
                        </p>
                        <p className='mt-1 text-sm text-muted-foreground'>
                          {manifest.route.schoolName} -{' '}
                          {formatDate(manifest.route.serviceDate)} -{' '}
                          {manifest.route.shiftType} -{' '}
                          {manifest.route.routeDirection === 'RETURN'
                            ? 'Chieu ve'
                            : 'Chieu di'}
                        </p>
                      </div>
                      <SchoolBusStatusBadge status={manifest.route.status} />
                    </div>
                    <div className='mt-3 text-sm text-muted-foreground'>
                      Assigned crew:{' '}
                      {manifest.assignment
                        ? `${manifest.assignment.driverName} / ${manifest.assignment.busPlateNumber}`
                        : 'No assignment yet'}
                    </div>
                  </div>

                  {manifest.students.length === 0 ? (
                    <SchoolBusEmptyState
                      title='No eligible students on this route'
                      description='Only students with valid approved requests appear in the manifest.'
                      icon={ClipboardCheck}
                    />
                  ) : (
                    <SchoolBusScrollableTable>
                      <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Student</TableHead>
                          <TableHead>
                            {manifest.route.routeDirection === 'RETURN'
                              ? 'Drop-off point'
                              : 'Pickup point'}
                          </TableHead>
                          <TableHead>Last event</TableHead>
                          <TableHead>Status</TableHead>
                          <TableHead className='text-right'>Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {manifest.students.map((student) => (
                          <TableRow key={student.studentId}>
                            <TableCell>{student.studentName}</TableCell>
                            <TableCell>
                              {student.pickupPointName || 'No pickup point'}
                            </TableCell>
                            <TableCell>
                              <div className='text-sm'>
                                <p>{student.latestAttendanceType || 'No events'}</p>
                                <p className='text-xs text-muted-foreground'>
                                  {formatDateTime(student.latestRecordedAt)}
                                </p>
                              </div>
                            </TableCell>
                            <TableCell>
                              <SchoolBusStatusBadge
                                status={student.latestAttendanceStatus || 'PENDING'}
                              />
                            </TableCell>
                            <TableCell className='text-right'>
                              <div className='flex justify-end gap-2'>
                                <Button
                                  size='sm'
                                  variant='outline'
                                  disabled={checkingIn}
                                  onClick={() => handleCheckIn(student.studentId)}
                                >
                                  {checkingIn ? 'Checking in...' : 'Check in'}
                                </Button>
                                <Button
                                  size='sm'
                                  disabled={checkingOut}
                                  onClick={() => handleCheckOut(student.studentId)}
                                >
                                  {checkingOut ? 'Checking out...' : 'Check out'}
                                </Button>
                              </div>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                      </Table>
                    </SchoolBusScrollableTable>
                  )}
                </>
              )}
            </div>
          )}
        </SchoolBusSection>

        <div className='space-y-6'>
          <SchoolBusSection
            title='Attendance feed'
            description='Most recent route attendance activity across the tenant.'
          >
            {attendance.length === 0 ? (
              <SchoolBusEmptyState
                title='No attendance activity yet'
                description='Attendance will appear after route operations start recording events.'
                icon={ClipboardCheck}
              />
            ) : (
              <div className='space-y-3'>
                {attendance.map((item) => (
                  <div key={item.id} className={schoolBusUi.interactiveCard}>
                    <div className='flex items-start justify-between gap-4'>
                      <div>
                        <p className='font-medium'>{item.studentName}</p>
                        <p className='mt-1 text-sm text-muted-foreground'>
                          {item.routeCode}
                        </p>
                        <p className='mt-2 text-xs text-muted-foreground'>
                          {formatDateTime(item.recordedAt)}
                        </p>
                      </div>
                      <SchoolBusStatusBadge status={item.attendanceType} />
                    </div>
                  </div>
                ))}
                <SchoolBusPaginationBar
                  page={attendanceData?.data}
                  onPageChange={attendancePagination.setPage}
                />
              </div>
            )}
          </SchoolBusSection>

          <SchoolBusSection
            title='Trip history'
            description='Closed routes with captured crew and time metadata.'
          >
            {history.length === 0 ? (
              <SchoolBusEmptyState
                title='No trip history yet'
                description='Completed routes will show up here after dispatch closes them.'
                icon={History}
              />
            ) : (
              <div className='space-y-3'>
                {history.map((trip) => (
                  <div key={trip.id} className={schoolBusUi.interactiveCard}>
                    <div className='flex items-start justify-between gap-4'>
                      <div>
                        <p className='font-medium'>{trip.routeCode}</p>
                        <p className='mt-1 text-sm text-muted-foreground'>
                          {formatDate(trip.serviceDate)}
                        </p>
                        <p className='mt-2 text-xs text-muted-foreground'>
                          {trip.driverName || 'No driver'} -{' '}
                          {trip.busPlateNumber || 'No bus'}
                        </p>
                      </div>
                      <SchoolBusStatusBadge status={trip.status} />
                    </div>
                  </div>
                ))}
                <SchoolBusPaginationBar
                  page={historyData?.data}
                  onPageChange={historyPagination.setPage}
                />
              </div>
            )}
          </SchoolBusSection>
        </div>
      </div>
    </SchoolBusPageShell>
  );
}
