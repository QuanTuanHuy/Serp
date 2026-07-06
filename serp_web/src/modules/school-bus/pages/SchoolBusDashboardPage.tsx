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
import {
  requestStatusLabel,
  routeReadinessStatusLabel,
  tripStudentStatusLabel,
  tripStatusLabel,
} from '../schoolBusLabels';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import { schoolBusUi } from '../theme';
import type { ChartItemDto } from '../types';

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
  READY: '#10B981',
  MISSING_BUS: '#EF4444',
  MISSING_DRIVER: '#F59E0B',
  MISSING_ATTENDANT: '#3B82F6',
};

const REQUEST_COLORS = {
  DRAFT: '#6B7280',
  SUBMITTED: '#F59E0B',
  APPROVED: '#10B981',
  REJECTED: '#EF4444',
  CANCELLED: '#EC4899',
};

const normalizeChartKey = (value?: string | null) =>
  (value || '')
    .trim()
    .replace(/([a-z])([A-Z])/g, '$1_$2')
    .replace(/[\s-]+/g, '_')
    .toUpperCase();

function localizeChartData(
  data: ChartItemDto[] | undefined,
  labelMap: Record<string, string>
) {
  return (data || []).map((item) => {
    const normalizedName = normalizeChartKey(item.name);
    const normalizedLabel = normalizeChartKey(item.label);
    const label =
      labelMap[normalizedName] ||
      labelMap[normalizedLabel] ||
      item.label ||
      item.name;

    return {
      ...item,
      name: normalizedName || normalizedLabel || item.name,
      label,
    };
  });
}

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
  const tripStatusChart = localizeChartData(
    operations?.tripStatusChart,
    tripStatusLabel
  );
  const attendanceChart = localizeChartData(
    operations?.attendanceChart,
    tripStudentStatusLabel
  );
  const routeReadinessChart = localizeChartData(
    operations?.routeReadinessChart,
    routeReadinessStatusLabel
  );
  const requestStatusChart = localizeChartData(
    operations?.requestStatusChart,
    requestStatusLabel
  );
  const pageTitle = access.isParentOnly
    ? 'Tổng quan đưa đón học sinh'
    : access.isDriver
      ? 'Bảng điều khiển tài xế'
      : access.isAttendant
        ? 'Bảng điều khiển phụ xe'
        : 'Tổng quan vận hành xe bus trường học';
  const pageDescription = access.isOperator
    ? 'Theo dõi theo thời gian thực về lập tuyến, trạng thái chuyến và điểm danh.'
    : 'Các chỉ số vận hành được giới hạn theo trường, học sinh, yêu cầu và chuyến xe thuộc phạm vi tài khoản của bạn.';

  return (
    <>
      <SchoolBusPageShell
        title={pageTitle}
        description={pageDescription}
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'Vận hành xe bus', href: '/school-bus/dashboard' },
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
              Bộ lọc
              {activeFilterCount > 0 && (
                <span className='ml-2 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-primary px-1.5 text-[10px] font-bold text-primary-foreground'>
                  {activeFilterCount}
                </span>
              )}
            </Button>

            {access.canAccessReports && (
              <Button asChild variant='outline' className='rounded-full'>
                <Link href='/school-bus/reports'>Mở báo cáo</Link>
              </Button>
            )}

            {access.canManageDispatching && (
              <Button
                asChild
                className={cn(schoolBusUi.primaryButton, 'rounded-full')}
              >
                <Link href='/school-bus/dispatch'>
                  Đi tới điều phối
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
              title='Không thể tải chỉ số tổng quan'
              description='Các biểu đồ vẫn hiển thị trong khi hệ thống thử tải lại dữ liệu tổng quan.'
              icon={Route}
              className='min-h-[154px]'
            />
          ) : (
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
              <SchoolBusMetricCard
                label='Trường đã thiết lập'
                value={summary.schoolCount}
                hint='Trường trong phạm vi dữ liệu hiện tại'
                icon={GraduationCap}
                tone='school'
              />
              <SchoolBusMetricCard
                label='Phụ huynh đã liên kết'
                value={summary.parentCount}
                hint='Phụ huynh trong phạm vi đã chọn'
                icon={Users}
                tone='default'
              />
              <SchoolBusMetricCard
                label='Học sinh đang quản lý'
                value={summary.studentCount}
                hint='Học sinh trong phạm vi đã chọn'
                icon={User}
                tone='student'
              />
              <SchoolBusMetricCard
                label='Yêu cầu chờ xử lý'
                value={summary.pendingRequestCount}
                hint='Yêu cầu chờ thao tác trong phạm vi truy cập'
                icon={FileText}
                tone='warning'
              />
            </div>
          )}

          <div className='grid gap-6 md:grid-cols-3'>
            <DashboardChartCard
              title='Phân bố trạng thái chuyến'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardDonutChart
                title='Phân bố trạng thái chuyến'
                data={tripStatusChart}
                colorMap={TRIP_STATUS_COLORS}
              />
            </DashboardChartCard>

            <DashboardChartCard
              title='Trạng thái điểm danh học sinh'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardDonutChart
                title='Trạng thái điểm danh học sinh'
                data={attendanceChart}
                colorMap={ATTENDANCE_COLORS}
              />
            </DashboardChartCard>

            <DashboardChartCard
              title='Trạng thái phân công tuyến'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardBarChart
                title='Trạng thái phân công tuyến'
                data={routeReadinessChart}
                colorMap={READINESS_COLORS}
              />
            </DashboardChartCard>
          </div>

          <div className='grid gap-6 md:grid-cols-3'>
            <DashboardChartCard
              title='Số chuyến theo thời gian'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
              className='md:col-span-2'
            >
              <DashboardLineChart
                title='Số chuyến theo thời gian'
                data={operations?.tripsByDate || []}
                color='#991B1B'
              />
            </DashboardChartCard>

            <DashboardChartCard
              title='Khối lượng yêu cầu'
              isLoading={operationsQuery.isLoading}
              isError={operationsQuery.isError}
            >
              <DashboardBarChart
                title='Khối lượng yêu cầu'
                data={requestStatusChart}
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
