'use client';

import Link from 'next/link';
import {
  ArrowRight,
  BusFront,
  ClipboardList,
  GraduationCap,
  Route,
  School,
  Users,
} from 'lucide-react';
import { Button } from '@/shared/components/ui';
import {
  useGetAttendanceQuery,
  useGetRoutesQuery,
  useGetSchoolBusReportQuery,
  useGetSchoolBusSummaryQuery,
  useGetTransportRequestsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime, getPageItems } from '../utils';

export function SchoolBusDashboardPage() {
  const { data: summaryData, isLoading: summaryLoading } =
    useGetSchoolBusSummaryQuery();
  const { data: reportData } = useGetSchoolBusReportQuery();
  const { data: requestData } = useGetTransportRequestsQuery({
    page: 0,
    size: 5,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });
  const { data: routeData } = useGetRoutesQuery({
    page: 0,
    size: 5,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const { data: attendanceData } = useGetAttendanceQuery({
    page: 0,
    size: 5,
    sortBy: 'recordedAt',
    sortDirection: 'DESC',
  });

  const summary = summaryData?.data;
  const report = reportData?.data;
  const requests = getPageItems(requestData?.data);
  const routes = getPageItems(routeData?.data);
  const attendance = getPageItems(attendanceData?.data);

  const pendingRequests = requests.filter((request) => request.status === 'SUBMITTED');
  const activeRoutes = routes.filter((route) =>
    ['ASSIGNED', 'IN_PROGRESS', 'PLANNED'].includes(route.status)
  );
  const latestAttendance = [...attendance]
    .sort((left, right) => right.recordedAt.localeCompare(left.recordedAt))
    .slice(0, 5);

  return (
    <SchoolBusPageShell
      title='School bus operations dashboard'
      description='Use the dashboard as the operational cockpit for approvals, dispatch readiness, and latest attendance activity.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dashboard' },
            { label: 'Dashboard', current: true },
          ]}
        />
      }
      actions={
        <>
          <Button asChild variant='outline' className='rounded-full'>
            <Link href='/school-bus/reports'>Open reports</Link>
          </Button>
          <Button asChild className='rounded-full'>
            <Link href='/school-bus/dispatch'>
              Go to dispatch
              <ArrowRight className='h-4 w-4' />
            </Link>
          </Button>
        </>
      }
    >
      {summaryLoading || !summary ? (
        <SchoolBusEmptyState
          title='Dashboard data is loading'
          description='Waiting for school_bus_service summary counters.'
          icon={Route}
        />
      ) : (
        <div className='space-y-6'>
          <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
            <SchoolBusMetricCard
              label='Schools onboarded'
              value={summary.schoolCount}
              hint='Tenant campuses currently active in the module'
              icon={School}
              tone='info'
            />
            <SchoolBusMetricCard
              label='Parents linked'
              value={summary.parentCount}
              hint='Operational parent profiles'
              icon={Users}
              tone='default'
            />
            <SchoolBusMetricCard
              label='Students managed'
              value={summary.studentCount}
              hint='Students currently available for requests and routing'
              icon={GraduationCap}
              tone='success'
            />
            <SchoolBusMetricCard
              label='Pending requests'
              value={summary.pendingRequestCount}
              hint='Approval workload requiring dispatcher action'
              icon={ClipboardList}
              tone='warning'
            />
          </div>

          <div className='grid gap-6 xl:grid-cols-[1.3fr_0.7fr]'>
            <SchoolBusSection
              title='Operational pulse'
              description='Core service counters sourced from dashboard and reporting endpoints.'
            >
              <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
                <SchoolBusMetricCard
                  label='Fleet registered'
                  value={summary.busCount}
                  hint='Vehicles available to be assigned'
                  icon={BusFront}
                  tone='default'
                />
                <SchoolBusMetricCard
                  label='Assigned routes'
                  value={summary.assignedRouteCount}
                  hint='Routes ready to move into execution'
                  icon={Route}
                  tone='info'
                />
                <SchoolBusMetricCard
                  label='Routes in progress'
                  value={summary.inProgressRouteCount}
                  hint='Trips currently under execution'
                  icon={Route}
                  tone='warning'
                />
                <SchoolBusMetricCard
                  label='Completed trips'
                  value={summary.completedTripCount}
                  hint={`${report?.completedRoutes ?? 0} completed routes in report summary`}
                  icon={Route}
                  tone='success'
                />
              </div>
            </SchoolBusSection>

            <SchoolBusSection
              title='Quick actions'
              description='Fastest navigation paths for an operator.'
            >
              <div className='space-y-3'>
                {[
                  {
                    href: '/school-bus/requests/new',
                    title: 'Create request',
                    description: 'Capture a new transport demand record.',
                  },
                  {
                    href: '/school-bus/dispatch/planning',
                    title: 'Plan routes',
                    description: 'Open the session-based route planning workspace.',
                  },
                  {
                    href: '/school-bus/attendance',
                    title: 'Open attendance',
                    description: 'Open the manifest and record check-in or check-out.',
                  },
                ].map((link) => (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`block ${schoolBusUi.interactiveCard}`}
                  >
                    <div className='flex items-start justify-between gap-4'>
                      <div>
                        <p className='font-medium'>{link.title}</p>
                        <p className='mt-1 text-sm leading-6 text-muted-foreground'>
                          {link.description}
                        </p>
                      </div>
                      <ArrowRight className='mt-1 h-4 w-4 text-rose-500' />
                    </div>
                  </Link>
                ))}
              </div>
            </SchoolBusSection>
          </div>

          <div className='grid gap-6 xl:grid-cols-2'>
            <SchoolBusSection
              title='Pending approval queue'
              description='Latest requests that still need a dispatcher decision.'
            >
              {pendingRequests.length === 0 ? (
                <SchoolBusEmptyState
                  title='No pending transport requests'
                  description='All current requests have already been processed.'
                  icon={ClipboardList}
                  className='min-h-[180px]'
                />
              ) : (
                <div className='space-y-3'>
                  {pendingRequests.slice(0, 5).map((request) => (
                    <div key={request.id} className={schoolBusUi.interactiveCard}>
                      <div className='flex items-start justify-between gap-4'>
                        <div className='space-y-1'>
                          <p className='font-medium'>{request.parentProfileName}</p>
                          <p className='text-sm text-muted-foreground'>
                            {request.schoolName} - {request.requestType} -{' '}
                            {formatDate(request.effectiveFrom)}
                          </p>
                        </div>
                        <SchoolBusStatusBadge status={request.status} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </SchoolBusSection>

            <SchoolBusSection
              title='Active route board'
              description='Routes that are planned, assigned, or currently in progress.'
            >
              {activeRoutes.length === 0 ? (
                <SchoolBusEmptyState
                  title='No active routes in the current window'
                  description='Create and assign routes from the dispatch board to populate this list.'
                  icon={Route}
                  className='min-h-[180px]'
                />
              ) : (
                <div className='space-y-3'>
                  {activeRoutes.slice(0, 5).map((route) => (
                    <div key={route.id} className={schoolBusUi.interactiveCard}>
                      <div className='flex items-start justify-between gap-4'>
                        <div className='space-y-1'>
                          <p className='font-medium'>
                            {route.routeCode} - {route.routeName}
                          </p>
                          <p className='text-sm text-muted-foreground'>
                            {route.schoolName} - {formatDate(route.serviceDate)} -{' '}
                            {route.schoolScheduleName}
                          </p>
                        </div>
                        <SchoolBusStatusBadge status={route.status} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </SchoolBusSection>
          </div>

          <SchoolBusSection
            title='Recent attendance activity'
            description='Latest boarding and attendance events captured from the operations layer.'
          >
            {latestAttendance.length === 0 ? (
              <SchoolBusEmptyState
                title='Attendance feed is still empty'
                description='Attendance appears here after route operations record events.'
                icon={ClipboardList}
                className='min-h-[180px]'
              />
            ) : (
              <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-5'>
                {latestAttendance.map((event) => (
                  <div key={event.id} className={schoolBusUi.interactiveCard}>
                    <p className='font-medium'>{event.studentName}</p>
                    <p className='mt-1 text-sm text-muted-foreground'>
                      {event.routeCode}
                    </p>
                    <p className='mt-2 text-xs text-muted-foreground'>
                      {formatDateTime(event.recordedAt)}
                    </p>
                    <div className='mt-3 flex items-center justify-between gap-3'>
                      <span className='text-xs text-muted-foreground'>
                        {event.attendanceType}
                      </span>
                      <SchoolBusStatusBadge status={event.status} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </SchoolBusSection>
        </div>
      )}
    </SchoolBusPageShell>
  );
}
