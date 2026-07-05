'use client';

import { useState } from 'react';
import Link from 'next/link';
import {
  ArrowRight,
  FileText,
  Filter,
  GraduationCap,
  Route,
  User,
  Users,
} from 'lucide-react';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetDashboardOperationsQuery,
  useGetDashboardSchoolsQuery,
  type SchoolBusDashboardQueryParams,
} from '../api/schoolBusDashboardApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { DashboardBarChart } from '../components/dashboard/DashboardBarChart';
import { DashboardChartCard } from '../components/dashboard/DashboardChartCard';
import { DashboardDonutChart } from '../components/dashboard/DashboardDonutChart';
import {
  DashboardFilterSheet,
  EMPTY_DASHBOARD_FILTERS,
  type DashboardFilters,
} from '../components/dashboard/DashboardFilterSheet';
import { DashboardLineChart } from '../components/dashboard/DashboardLineChart';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import { schoolBusUi } from '../theme';

const TRIP_STATUS_COLORS = {
  PLANNED: '#6B7280',
  ASSIGNED: '#F59E0B',
  IN_PROGRESS: '#3B82F6',
  COMPLETED: '#10B981',
  CANCELLED: '#EF4444',
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

function countActiveFilters(filters: DashboardFilters) {
  return (
    Number(Boolean(filters.serviceDate)) +
    Number(filters.schoolId !== undefined) +
    Number(Boolean(filters.direction))
  );
}

export function SchoolBusDashboardPage() {
  const access = useSchoolBusAccess();
  const [filters, setFilters] = useState<DashboardFilters>(
    EMPTY_DASHBOARD_FILTERS
  );
  const [filterOpen, setFilterOpen] = useState(false);

  const queryArgs: SchoolBusDashboardQueryParams = {
    serviceDate: filters.serviceDate || undefined,
    schoolId: filters.schoolId,
    direction: filters.direction || undefined,
    userKey: access.userKey,
  };

  const operationsQuery = useGetDashboardOperationsQuery(queryArgs);
  const schoolsQuery = useGetDashboardSchoolsQuery({
    userKey: access.userKey,
  });

  const operations = operationsQuery.data?.data;
  const summary = operations?.summary;
  const activeFilterCount = countActiveFilters(filters);
  const pageTitle = access.isParentOnly
    ? 'Student transit dashboard'
    : access.isDriver
      ? 'Driver dashboard'
      : access.isAttendant
        ? 'Attendant dashboard'
        : 'School bus operations cockpit';
  const pageDescription = access.isOperator
    ? 'Modern operational control room offering real-time visibility into route planning, status metrics, and attendance tracking.'
    : 'Operational metrics are limited to the schools, students, requests, and trips available to your account.';

  return (
    <>
      <SchoolBusPageShell
        title={pageTitle}
        description={pageDescription}
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dashboard' },
              { label: 'Tổng quan', current: true },
            ]}
          />
        }
        actions={
          <>
            <Button
              type='button'
              variant='outline'
              className='relative rounded-full'
              onClick={() => setFilterOpen(true)}
            >
              <Filter className='mr-2 h-4 w-4' />
              Filters
              {activeFilterCount > 0 && (
                <span className='ml-2 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-primary px-1.5 text-[10px] font-bold text-primary-foreground'>
                  {activeFilterCount}
                </span>
              )}
            </Button>

            {access.canAccessReports && (
              <Button asChild variant='outline' className='rounded-full'>
                <Link href='/school-bus/reports'>Open reports</Link>
              </Button>
            )}

            {access.canManageDispatching && (
              <Button
                asChild
                className={cn(schoolBusUi.primaryButton, 'rounded-full')}
              >
                <Link href='/school-bus/dispatch'>
                  Go to dispatch
                  <ArrowRight className='ml-1 h-4 w-4' />
                </Link>
              </Button>
            )}
          </>
        }
      >
        <div className='space-y-6'>
          {operationsQuery.isLoading ? (
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
              {[0, 1, 2, 3].map((item) => (
                <div
                  key={item}
                  className='h-[154px] animate-pulse rounded-[28px] border border-border bg-muted/60'
                />
              ))}
            </div>
          ) : operationsQuery.isError || !summary ? (
            <SchoolBusEmptyState
              title='Summary metrics unavailable'
              description='The chart blocks remain available while the summary request is retried.'
              icon={Route}
              className='min-h-[154px]'
            />
          ) : (
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
              <SchoolBusMetricCard
                label='Schools onboarded'
                value={summary.schoolCount}
                hint='Schools inside your current data scope'
                icon={GraduationCap}
                tone='school'
              />
              <SchoolBusMetricCard
                label='Parents linked'
                value={summary.parentCount}
                hint='Parents represented in the selected scope'
                icon={Users}
                tone='default'
              />
              <SchoolBusMetricCard
                label='Students managed'
                value={summary.studentCount}
                hint='Students represented in the selected scope'
                icon={User}
                tone='student'
              />
              <SchoolBusMetricCard
                label='Pending requests'
                value={summary.pendingRequestCount}
                hint='Requests awaiting action in your accessible scope'
                icon={FileText}
                tone='warning'
              />
            </div>
          )}

          <div className='grid gap-6 md:grid-cols-3'>
            <DashboardChartCard
              title='Trip Status Distribution'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardDonutChart
                title='Trip Status Distribution'
                data={operations?.tripStatusChart || []}
                colorMap={TRIP_STATUS_COLORS}
              />
            </DashboardChartCard>

            <DashboardChartCard
              title='Student Attendance Status'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardDonutChart
                title='Student Attendance Status'
                data={operations?.attendanceChart || []}
                colorMap={ATTENDANCE_COLORS}
              />
            </DashboardChartCard>

            <DashboardChartCard
              title='Route Assignment Status'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardBarChart
                title='Route Assignment Status'
                data={operations?.routeReadinessChart || []}
                colorMap={READINESS_COLORS}
              />
            </DashboardChartCard>
          </div>

          <div className='grid gap-6 md:grid-cols-3'>
            <DashboardChartCard
              title='Trips Run Over Time'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
              className='md:col-span-2'
            >
              <DashboardLineChart
                title='Trips Run Over Time'
                data={operations?.tripsByDate || []}
                color='#991B1B'
              />
            </DashboardChartCard>

            <DashboardChartCard
              title='Request Workload'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardBarChart
                title='Request Workload'
                data={operations?.requestStatusChart || []}
                colorMap={REQUEST_COLORS}
              />
            </DashboardChartCard>
          </div>
        </div>
      </SchoolBusPageShell>

      <DashboardFilterSheet
        open={filterOpen}
        onOpenChange={setFilterOpen}
        filters={filters}
        schools={schoolsQuery.data?.data || []}
        onApply={setFilters}
        onReset={() => setFilters(EMPTY_DASHBOARD_FILTERS)}
      />
    </>
  );
}
