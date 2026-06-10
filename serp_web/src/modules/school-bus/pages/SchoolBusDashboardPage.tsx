'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import {
  ArrowRight,
  Bus,
  FileText,
  GraduationCap,
  Route,
  User,
  Users,
  Calendar,
  Filter,
} from 'lucide-react';
import { Button } from '@/shared/components/ui';
import {
  useGetOperationsDashboardQuery,
  useGetSchoolsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { schoolBusUi } from '../theme';
import { formatDate, formatDateTime } from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';

// Import Recharts chart wrappers
import { DashboardDonutChart } from '../components/dashboard/DashboardDonutChart';
import { DashboardBarChart } from '../components/dashboard/DashboardBarChart';
import { DashboardLineChart } from '../components/dashboard/DashboardLineChart';

// Color Maps for Consistent Chart Theme
const TRIP_STATUS_COLORS = {
  PLANNED: '#6B7280',    // Gray
  ASSIGNED: '#F59E0B',   // Amber
  IN_PROGRESS: '#3B82F6',// Blue
  COMPLETED: '#10B981',  // Green
  CANCELLED: '#EF4444',  // Red
};

const ATTENDANCE_COLORS = {
  PLANNED: '#6B7280',
  BOARDED: '#3B82F6',
  DROPPED_OFF: '#10B981',
  ABSENT: '#EF4444',
  NO_SHOW: '#F59E0B',
  NOT_SERVED: '#EC4899',
};

const READINESS_COLORS = {
  Ready: '#10B981',
  'Missing Bus': '#EF4444',
  'Missing Driver': '#F59E0B',
  'Missing Attendant': '#3B82F6',
};

const REQUEST_COLORS = {
  DRAFT: '#6B7280',
  SUBMITTED: '#F59E0B',
  APPROVED: '#10B981',
  REJECTED: '#EF4444',
  CANCELLED: '#EC4899',
};

export function SchoolBusDashboardPage() {
  const access = useSchoolBusAccess();
  // Filters State
  const [serviceDate, setServiceDate] = useState<string>('');
  const [schoolId, setSchoolId] = useState<number | undefined>(undefined);
  const [direction, setDirection] = useState<string>('');

  // Fetch Operations Dashboard Data
  const { data: operationsData, isLoading: dashboardLoading } =
    useGetOperationsDashboardQuery({
      serviceDate: serviceDate || undefined,
      schoolId,
      direction: direction || undefined,
    });

  // Fetch schools list for filter dropdown
  const { data: schoolsData } = useGetSchoolsQuery({ size: 100 });

  const schools = schoolsData?.data?.items || [];
  const dashboard = operationsData?.data;
  const summary = dashboard?.summary;

  // Derived collections
  const activeRoutes = dashboard?.activeRoutes || [];
  const pendingRequests = dashboard?.pendingApprovalQueue || [];
  const recentAttendance = dashboard?.recentAttendanceActivity || [];

  if (access.isParent) {
    return (
      <SchoolBusPageShell
        title="Student Transit Dashboard"
        description="View real-time status, upcoming trips, and attendance history of your students."
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dashboard' },
              { label: 'Parent Dashboard', current: true },
            ]}
          />
        }
        actions={
          <>
            {access.canWriteRequest && (
              <Button asChild className="rounded-full bg-red-800 hover:bg-red-900 text-white">
                <Link href="/school-bus/requests/new">Create request</Link>
              </Button>
            )}
          </>
        }
      >
        {dashboardLoading || !dashboard || !summary ? (
          <SchoolBusEmptyState
            title="Dashboard is loading"
            description="Fetching your students' transit information..."
            icon={Route}
          />
        ) : (
          <div className="space-y-6">
            {/* Metric Cards for Parent */}
            <div className="grid gap-4 md:grid-cols-3">
              <SchoolBusMetricCard
                label="Student"
                value={summary.studentCount}
                hint="Registered student profiles"
                icon={User}
                tone="student"
              />
              <SchoolBusMetricCard
                label="Active Subscriptions"
                value={summary.assignedRouteCount}
                hint="Active subscriptions currently routing"
                icon={Calendar}
                tone="success"
              />
              <SchoolBusMetricCard
                label="Open Requests"
                value={summary.pendingRequestCount}
                hint="Requests awaiting dispatcher approval"
                icon={FileText}
                tone="warning"
              />
            </div>

            {/* Attendance Chart (scoped to their children) */}
            <div className="grid gap-6 md:grid-cols-2">
              <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
                <DashboardDonutChart
                  title="Student Attendance Status"
                  data={dashboard.attendanceChart}
                  colorMap={ATTENDANCE_COLORS}
                />
              </div>
              <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
                <DashboardDonutChart
                  title="Student Trip Status"
                  data={dashboard.tripStatusChart}
                  colorMap={TRIP_STATUS_COLORS}
                />
              </div>
            </div>

            {/* Trips & Attendance List */}
            <div className="grid gap-6 xl:grid-cols-2">
              <SchoolBusSection
                title="Upcoming Trips of Students"
                description="Status of routes assigned to your students today."
              >
                {activeRoutes.length === 0 ? (
                  <SchoolBusEmptyState
                    title="No upcoming trips today"
                    description="No trips are scheduled for your students today."
                    icon={Route}
                    className="min-h-[180px]"
                  />
                ) : (
                  <div className="space-y-3">
                    {activeRoutes.map((route) => (
                      <div
                        key={route.id}
                        className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div className="space-y-1">
                            <p className="font-semibold text-zinc-900 dark:text-zinc-100">
                              {route.routeCode} - {route.routeName}
                            </p>
                            <p className="text-sm text-zinc-500 dark:text-zinc-400">
                              {route.schoolName} - {formatDate(route.serviceDate)}
                            </p>
                          </div>
                          <SchoolBusStatusBadge status={route.status} />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </SchoolBusSection>

              <SchoolBusSection
                title="Latest Requests Status"
                description="Your submitted transport request records."
              >
                {pendingRequests.length === 0 ? (
                  <SchoolBusEmptyState
                    title="No pending requests"
                    description="You have no requests awaiting dispatcher approval."
                    icon={FileText}
                    className="min-h-[180px]"
                  />
                ) : (
                  <div className="space-y-3">
                    {pendingRequests.map((request) => (
                      <div
                        key={request.id}
                        className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div className="space-y-1">
                            <p className="font-semibold text-zinc-900 dark:text-zinc-100">{request.parentProfileName}</p>
                            <p className="text-sm text-zinc-500 dark:text-zinc-400">
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
            </div>

            <SchoolBusSection
              title="Latest Attendance of Students"
              description="History of check-in and check-out events of your students."
            >
              {recentAttendance.length === 0 ? (
                <SchoolBusEmptyState
                  title="No attendance events recorded"
                  description="No attendance details found for your students yet."
                  icon={FileText}
                  className="min-h-[180px]"
                />
              ) : (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                  {recentAttendance.map((event) => (
                    <div
                      key={event.id}
                      className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                    >
                      <p className="font-semibold text-zinc-900 dark:text-zinc-100">{event.studentName}</p>
                      <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                        {event.routeCode}
                      </p>
                      <p className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
                        {formatDateTime(event.recordedAt)}
                      </p>
                      <div className="mt-3 flex items-center justify-between gap-3 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                        <span className="text-xs text-zinc-500 dark:text-zinc-400">
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

  if (access.isDriver || access.isAttendant) {
    return (
      <SchoolBusPageShell
        title={access.isDriver ? "Driver Dashboard" : "Attendant Dashboard"}
        description={access.isDriver ? "Manage your assigned trips and execute routes." : "Manage student attendance manifest on assigned trips."}
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dashboard' },
              { label: access.isDriver ? 'Driver Dashboard' : 'Attendant Dashboard', current: true },
            ]}
          />
        }
        actions={
          <>
            {access.isDriver && (
              <Button asChild className="rounded-full bg-blue-700 hover:bg-blue-800 text-white">
                <Link href="/school-bus/trips">
                  Trips
                  <ArrowRight className="h-4 w-4 ml-1" />
                </Link>
              </Button>
            )}
            {access.isAttendant && (
              <Button asChild className="rounded-full bg-blue-700 hover:bg-blue-800 text-white">
                <Link href="/school-bus/attendance">
                  Attendance
                  <ArrowRight className="h-4 w-4 ml-1" />
                </Link>
              </Button>
            )}
          </>
        }
      >
        {dashboardLoading || !dashboard || !summary ? (
          <SchoolBusEmptyState
            title="Dashboard is loading"
            description="Fetching your assigned trip data..."
            icon={Route}
          />
        ) : (
          <div className="space-y-6">
            {/* Metric Cards for Driver/Attendant */}
            <div className="grid gap-4 md:grid-cols-2">
              <SchoolBusMetricCard
                label="Active Routes"
                value={summary.inProgressRouteCount}
                hint="Routes currently in execution"
                icon={Route}
                tone="warning"
              />
              <SchoolBusMetricCard
                label="Completed trips today"
                value={summary.completedTripCount}
                hint="Finished trips in history today"
                icon={Route}
                tone="success"
              />
            </div>

            {/* Attendance Chart (scoped to their trips) */}
            <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
              <DashboardDonutChart
                title="Student Attendance Status on Trips"
                data={dashboard.attendanceChart}
                colorMap={ATTENDANCE_COLORS}
              />
            </div>

            {/* List of active routes and attendance logs */}
            <div className="grid gap-6 xl:grid-cols-2">
              <SchoolBusSection
                title="Active Routes"
                description="List of routes assigned to you today."
              >
                {activeRoutes.length === 0 ? (
                  <SchoolBusEmptyState
                    title="No active routes assigned"
                    description="You have no active routes scheduled for today."
                    icon={Route}
                    className="min-h-[180px]"
                  />
                ) : (
                  <div className="space-y-3">
                    {activeRoutes.map((route) => (
                      <div
                        key={route.id}
                        className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div className="space-y-1">
                            <p className="font-semibold text-zinc-900 dark:text-zinc-100">
                              {route.routeCode} - {route.routeName}
                            </p>
                            <p className="text-sm text-zinc-500 dark:text-zinc-400">
                              {route.schoolName} - {formatDate(route.serviceDate)}
                            </p>
                          </div>
                          <SchoolBusStatusBadge status={route.status} />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </SchoolBusSection>

              <SchoolBusSection
                title="Recent Attendance Activity"
                description="Latest check-in/out logs on your routes."
              >
                {recentAttendance.length === 0 ? (
                  <SchoolBusEmptyState
                    title="No attendance events"
                    description="No attendance activity logged on your routes today."
                    icon={FileText}
                    className="min-h-[180px]"
                  />
                ) : (
                  <div className="space-y-3">
                    {recentAttendance.map((event) => (
                      <div
                        key={event.id}
                        className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div className="space-y-1">
                            <p className="font-semibold text-zinc-900 dark:text-zinc-100">{event.studentName}</p>
                            <p className="text-sm text-zinc-500 dark:text-zinc-400">
                              {event.routeCode} - {formatDateTime(event.recordedAt)}
                            </p>
                          </div>
                          <SchoolBusStatusBadge status={event.status} />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </SchoolBusSection>
            </div>
          </div>
        )}
      </SchoolBusPageShell>
    );
  }

  return (
    <SchoolBusPageShell
      title="School bus operations cockpit"
      description="Modern operational control room offering real-time visibility into route planning, status metrics, and attendance tracking."
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'School Bus Ops', href: '/school-bus/dashboard' },
            { label: 'Dashboard Cockpit', current: true },
          ]}
        />
      }
      actions={
        <>
          {access.canAccessReports && (
            <Button asChild variant="outline" className="rounded-full border-zinc-300 dark:border-zinc-700">
              <Link href="/school-bus/reports">Open reports</Link>
            </Button>
          )}
          {access.canManageDispatching && (
            <Button asChild className="rounded-full bg-red-800 hover:bg-red-900 text-white">
              <Link href="/school-bus/dispatch">
                Go to dispatch
                <ArrowRight className="h-4 w-4 ml-1" />
              </Link>
            </Button>
          )}
          {access.isDriver && (
            <Button asChild className="rounded-full bg-blue-700 hover:bg-blue-800 text-white">
              <Link href="/school-bus/trips">
                Trips
                <ArrowRight className="h-4 w-4 ml-1" />
              </Link>
            </Button>
          )}
          {access.isAttendant && (
            <Button asChild className="rounded-full bg-blue-700 hover:bg-blue-800 text-white">
              <Link href="/school-bus/attendance">
                Attendance
                <ArrowRight className="h-4 w-4 ml-1" />
              </Link>
            </Button>
          )}
        </>
      }
    >
      {/* Dynamic Filter Panel */}
      <div className="mb-6 p-4 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-2xl shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-zinc-800 dark:text-zinc-200">
            <Filter className="h-4 w-4 text-red-800" />
            <span className="font-semibold text-sm">Dashboard Filters</span>
          </div>
          <div className="flex flex-wrap gap-3 items-center">
            {/* Service Date Filter */}
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-zinc-400" />
              <input
                type="date"
                value={serviceDate}
                onChange={(e) => setServiceDate(e.target.value)}
                className="text-sm border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950 text-zinc-800 dark:text-zinc-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-1 focus:ring-red-800"
              />
            </div>

            {/* School Filter */}
            <select
              value={schoolId || ''}
              onChange={(e) => setSchoolId(e.target.value ? Number(e.target.value) : undefined)}
              className="text-sm border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950 text-zinc-800 dark:text-zinc-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-1 focus:ring-red-800"
            >
              <option value="">All Schools</option>
              {schools.map((school) => (
                <option key={school.id} value={school.id}>
                  {school.name}
                </option>
              ))}
            </select>

            {/* Direction Filter */}
            <select
              value={direction}
              onChange={(e) => setDirection(e.target.value)}
              className="text-sm border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-950 text-zinc-800 dark:text-zinc-200 rounded-lg px-2.5 py-1.5 focus:outline-none focus:ring-1 focus:ring-red-800"
            >
              <option value="">All Directions</option>
              <option value="OUTBOUND">Outbound</option>
              <option value="RETURN">Return</option>
            </select>

            {/* Reset Filter Button */}
            {(serviceDate || schoolId !== undefined || direction) && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setServiceDate('');
                  setSchoolId(undefined);
                  setDirection('');
                }}
                className="text-xs text-red-800 hover:text-red-950 p-1"
              >
                Reset Filters
              </Button>
            )}
          </div>
        </div>
      </div>

      {dashboardLoading || !dashboard || !summary ? (
        <SchoolBusEmptyState
          title="Dashboard is loading"
          description="Fetching operations metrics and chart data from the service aggregation endpoint..."
          icon={Route}
        />
      ) : (
        <div className="space-y-6">
          {/* Main Summary Metric Cards */}
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <SchoolBusMetricCard
              label="Schools onboarded"
              value={summary.schoolCount}
              hint="Tenant campuses currently active in the module"
              icon={GraduationCap}
              tone="school"
            />
            <SchoolBusMetricCard
              label="Parents linked"
              value={summary.parentCount}
              hint="Operational parent profiles"
              icon={Users}
              tone="default"
            />
            <SchoolBusMetricCard
              label="Students managed"
              value={summary.studentCount}
              hint="Students currently available for requests and routing"
              icon={User}
              tone="student"
            />
            <SchoolBusMetricCard
              label="Pending requests"
              value={summary.pendingRequestCount}
              hint="Approval workload requiring dispatcher action"
              icon={FileText}
              tone="warning"
            />
          </div>

          {/* Grid Layout for Analytical Charts */}
          <div className="grid gap-6 md:grid-cols-3">
            {/* Trip status distribution donut */}
            <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
              <DashboardDonutChart
                title="Trip Status Distribution"
                data={dashboard.tripStatusChart}
                colorMap={TRIP_STATUS_COLORS}
              />
            </div>

            {/* Attendance donut */}
            <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
              <DashboardDonutChart
                title="Student Attendance Status"
                data={dashboard.attendanceChart}
                colorMap={ATTENDANCE_COLORS}
              />
            </div>

            {/* Route Readiness bar chart */}
            <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
              <DashboardBarChart
                title="Route Readiness"
                data={dashboard.routeReadinessChart}
                colorMap={READINESS_COLORS}
              />
            </div>
          </div>

          <div className="grid gap-6 md:grid-cols-3">
            {/* Trips by Date Trend Chart (takes 2 cols) */}
            <div className="md:col-span-2 bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
              <DashboardLineChart
                title="Trips Run Over Time"
                data={dashboard.tripsByDate}
                color="#991B1B"
              />
            </div>

            {/* Request status bar chart */}
            <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 p-5 rounded-2xl shadow-sm">
              <DashboardBarChart
                title="Request Workload"
                data={dashboard.requestStatusChart}
                colorMap={REQUEST_COLORS}
              />
            </div>
          </div>

          {/* Operational Pulse & Quick Navigation */}
          <div className="grid gap-6 xl:grid-cols-[1.3fr_0.7fr]">
            <SchoolBusSection
              title="Operational Pulse"
              description="Core service counters sourced from dashboard and reporting endpoints."
            >
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                <SchoolBusMetricCard
                  label="Fleet registered"
                  value={summary.busCount}
                  hint="Vehicles available to be assigned"
                  icon={Bus}
                  tone="linked"
                />
                <SchoolBusMetricCard
                  label="Assigned routes"
                  value={summary.assignedRouteCount}
                  hint="Routes ready to move into execution"
                  icon={Route}
                  tone="info"
                />
                <SchoolBusMetricCard
                  label="Routes in progress"
                  value={summary.inProgressRouteCount}
                  hint="Trips currently under execution"
                  icon={Route}
                  tone="warning"
                />
                <SchoolBusMetricCard
                  label="Completed trips"
                  value={summary.completedTripCount}
                  hint="Finished route plans in history database"
                  icon={Route}
                  tone="success"
                />
              </div>
            </SchoolBusSection>

            <SchoolBusSection
              title="Quick Actions"
              description="Fastest navigation paths for an operator."
            >
              <div className="space-y-3">
                {[
                  // Operator-only quick actions
                  ...(access.canWriteRequest ? [{
                    href: '/school-bus/requests/new',
                    title: 'Create request',
                    description: 'Capture a new transport demand record.',
                  }] : []),
                  ...(access.canManageDispatching ? [{
                    href: '/school-bus/dispatch/planning',
                    title: 'Plan routes',
                    description: 'Open the session-based route planning workspace.',
                  }] : []),
                  // Attendant quick action
                  ...(access.canMarkAttendance ? [{
                    href: '/school-bus/attendance',
                    title: 'Open attendance',
                    description: 'Open the manifest and record check-in or check-out.',
                  }] : []),
                  // Driver quick action
                  ...(access.isDriver ? [{
                    href: '/school-bus/trips',
                    title: 'Trips Today',
                    description: 'View and operate trips assigned to you.',
                  }] : []),
                  // Parent quick action
                  ...(access.isParent ? [{
                    href: '/school-bus/students',
                    title: 'Students',
                    description: 'View student profiles and status.',
                  }] : []),
                ].map((link) => (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`block ${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <p className="font-semibold text-zinc-900 dark:text-zinc-100">{link.title}</p>
                        <p className="mt-1 text-sm leading-6 text-zinc-500 dark:text-zinc-400">
                          {link.description}
                        </p>
                      </div>
                      <ArrowRight className="mt-1 h-4 w-4 text-zinc-400" />
                    </div>
                  </Link>
                ))}
              </div>
            </SchoolBusSection>
          </div>

          {/* Pending approval queue & Active Route board */}
          <div className="grid gap-6 xl:grid-cols-2">
            <SchoolBusSection
              title="Pending Approval Queue"
              description="Latest student registration requests that still need a dispatcher decision."
            >
              {pendingRequests.length === 0 ? (
                <SchoolBusEmptyState
                  title="No pending requests"
                  description="All current requests have already been processed."
                  icon={FileText}
                  className="min-h-[180px]"
                />
              ) : (
                <div className="space-y-3">
                  {pendingRequests.map((request) => (
                    <div
                      key={request.id}
                      className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div className="space-y-1">
                          <p className="font-semibold text-zinc-900 dark:text-zinc-100">{request.parentProfileName}</p>
                          <p className="text-sm text-zinc-500 dark:text-zinc-400">
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
              title="Active Route Board"
              description="Routes that are currently active in planning, assignment, or execution."
            >
              {activeRoutes.length === 0 ? (
                <SchoolBusEmptyState
                  title="No active routes for today"
                  description="Create and assign routes from the planning workspace to populate this list."
                  icon={Route}
                  className="min-h-[180px]"
                />
              ) : (
                <div className="space-y-3">
                  {activeRoutes.map((route) => (
                    <div
                      key={route.id}
                      className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div className="space-y-1">
                          <p className="font-semibold text-zinc-900 dark:text-zinc-100">
                            {route.routeCode} - {route.routeName}
                          </p>
                          <p className="text-sm text-zinc-500 dark:text-zinc-400">
                            {route.schoolName} - {formatDate(route.serviceDate)}
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

          {/* Recent Attendance Activity Feed */}
          <SchoolBusSection
            title="Recent Attendance Activity"
            description="Real-time check-in and check-out events logged from transit operations."
          >
            {recentAttendance.length === 0 ? (
              <SchoolBusEmptyState
                title="Attendance feed is empty"
                description="Attendance logs will appear here as soon as student check-ins occur during trips."
                icon={FileText}
                className="min-h-[180px]"
              />
            ) : (
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                {recentAttendance.map((event) => (
                  <div
                    key={event.id}
                    className={`${schoolBusUi.interactiveCard} border border-zinc-200 dark:border-zinc-800 rounded-xl hover:border-red-800 dark:hover:border-red-800 transition-all`}
                  >
                    <p className="font-semibold text-zinc-900 dark:text-zinc-100">{event.studentName}</p>
                    <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
                      {event.routeCode}
                    </p>
                    <p className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
                      {formatDateTime(event.recordedAt)}
                    </p>
                    <div className="mt-3 flex items-center justify-between gap-3 pt-2 border-t border-zinc-100 dark:border-zinc-800">
                      <span className="text-xs text-zinc-500 dark:text-zinc-400">
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
