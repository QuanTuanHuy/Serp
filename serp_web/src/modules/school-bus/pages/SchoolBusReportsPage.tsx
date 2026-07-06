'use client';

import * as React from 'react';
import { useState, useEffect, useMemo } from 'react';
import {
  Activity,
  BarChart3,
  FileText,
  Milestone,
  Route,
  Fingerprint,
  GraduationCap,
  Bus,
  CheckCircle2,
  Users,
  Search,
  X,
  Filter,
  ArrowRight,
  RotateCcw,
} from 'lucide-react';
import {
  Button,
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useGetSchoolBusReportAttendanceQuery,
  useGetSchoolBusReportCapacityQuery,
  useGetSchoolBusReportQuery,
  useGetSchoolBusReportTripsQuery,
  useGetSchoolDropdownOptionsQuery,
} from '../api/schoolBusApi';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import { SchoolBusDatePicker } from '../components/ui/SchoolBusDatePicker';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { formatDate, formatDateTime, getPageItems } from '../utils';
import {
  attendanceStatusLabel,
  attendanceTypeLabel,
  operationEventLabel,
  tripStatusLabel,
} from '../schoolBusLabels';

export function SchoolBusReportsPage() {
  // --- Filter States ---------------------------------------------------------
  const [filters, setFilters] = useState({
    fromDate: '',
    toDate: '',
    schoolId: '' as string | number,
    status: '',
    direction: '',
  });

  const [tempFilters, setTempFilters] = useState({
    fromDate: '',
    toDate: '',
    schoolId: '' as string | number,
    status: '',
    direction: '',
  });

  const [isFilterOpen, setIsFilterOpen] = useState(false);

  const activeFiltersCount = useMemo(() => {
    let count = 0;
    if (filters.fromDate) count++;
    if (filters.toDate) count++;
    if (filters.schoolId) count++;
    if (filters.status) count++;
    if (filters.direction) count++;
    return count;
  }, [filters]);

  // --- Keyword / Search States -----------------------------------------------
  const [keyword, setKeyword] = useState('');
  const [debouncedKeyword, setDebouncedKeyword] = useState('');

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedKeyword(keyword);
    }, 300);
    return () => clearTimeout(handler);
  }, [keyword]);

  // --- Tab Selection State ---------------------------------------------------
  const [activeTab, setActiveTab] = useState('trips');

  // --- Pagination Hooks ------------------------------------------------------
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

  // Reset page when switching tabs
  const handleTabChange = (key: string) => {
    setActiveTab(key);
    setKeyword('');
  };

  // --- Query Parameters ------------------------------------------------------
  const summaryParams = useMemo(
    () =>
      ({
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        schoolId: filters.schoolId || undefined,
        status: filters.status || undefined,
        direction: filters.direction || undefined,
      }) as any,
    [filters]
  );

  const tripsParams = useMemo(
    () =>
      ({
        ...tripPagination.params,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        schoolId: filters.schoolId || undefined,
        status: filters.status || undefined,
        direction: filters.direction || undefined,
        keyword: debouncedKeyword || undefined,
      }) as any,
    [tripPagination.params, filters, debouncedKeyword]
  );

  const attendanceParams = useMemo(
    () =>
      ({
        ...attendancePagination.params,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        schoolId: filters.schoolId || undefined,
        status: filters.status || undefined,
        direction: filters.direction || undefined,
        keyword: debouncedKeyword || undefined,
      }) as any,
    [attendancePagination.params, filters, debouncedKeyword]
  );

  const capacityParams = useMemo(
    () =>
      ({
        ...capacityPagination.params,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        schoolId: filters.schoolId || undefined,
        status: filters.status || undefined,
        direction: filters.direction || undefined,
        keyword: debouncedKeyword || undefined,
      }) as any,
    [capacityPagination.params, filters, debouncedKeyword]
  );

  // --- Queries ---------------------------------------------------------------
  const { data: summaryData, isLoading: loadingSummary } =
    useGetSchoolBusReportQuery(summaryParams);
  const { data: tripsData, isLoading: loadingTrips } =
    useGetSchoolBusReportTripsQuery(tripsParams);
  const { data: attendanceData, isLoading: loadingAttendance } =
    useGetSchoolBusReportAttendanceQuery(attendanceParams);
  const { data: capacityData, isLoading: loadingCapacity } =
    useGetSchoolBusReportCapacityQuery(capacityParams);

  // School list query for filters dropdown
  const { data: schoolsData } = useGetSchoolDropdownOptionsQuery();

  const report = summaryData?.data;
  const trips = getPageItems(tripsData?.data);
  const attendance = getPageItems(attendanceData?.data);
  const capacity = getPageItems(capacityData?.data);
  const schoolsList = schoolsData?.data || [];

  // --- Dropdown Options ------------------------------------------------------
  const schoolOptions = useMemo(() => {
    return [
      { label: 'All Schools', value: '' },
      ...schoolsList.map((school) => ({
        label: school.label,
        value: school.id,
      })),
    ];
  }, [schoolsList]);

  const statusOptions = [
    { label: 'All Statuses', value: '' },
    { label: 'Đang hoạt động', value: 'ACTIVE' },
    { label: 'Đang chờ', value: 'PENDING' },
    { label: 'In Progress', value: 'IN_PROGRESS' },
    { label: 'Hoàn thành', value: 'COMPLETED' },
    { label: 'Đã hủy', value: 'CANCELLED' },
  ];

  const directionOptions = [
    { label: 'All Directions', value: '' },
    { label: 'Outbound', value: 'OUTBOUND' },
    { label: 'Return', value: 'RETURN' },
  ];

  // --- Filter Actions --------------------------------------------------------
  const handleApplyFilters = () => {
    setFilters(tempFilters);
    tripPagination.setPage(0);
    attendancePagination.setPage(0);
    capacityPagination.setPage(0);
    setIsFilterOpen(false);
  };

  const handleResetFilters = () => {
    const cleared = {
      fromDate: '',
      toDate: '',
      schoolId: '',
      status: '',
      direction: '',
    };
    setTempFilters(cleared);
    setFilters(cleared);
    setKeyword('');
    tripPagination.setPage(0);
    attendancePagination.setPage(0);
    capacityPagination.setPage(0);
    setIsFilterOpen(false);
  };

  // --- Attendance KPI strip calculations -------------------------------------
  const boardedCount = useMemo(
    () =>
      attendance.filter((a) => {
        const type = (a.eventType || a.attendanceType || '').toUpperCase();
        return type === 'BOARDED' || type === 'BOARDING';
      }).length,
    [attendance]
  );

  const droppedOffCount = useMemo(
    () =>
      attendance.filter((a) => {
        const type = (a.eventType || a.attendanceType || '').toUpperCase();
        return (
          type === 'DROPOFF' ||
          type === 'DROPOFF_ONLY' ||
          type === 'DROPPED_OFF'
        );
      }).length,
    [attendance]
  );

  const absentCount = useMemo(
    () =>
      attendance.filter((a) => {
        const type = (a.eventType || a.attendanceType || '').toUpperCase();
        return type === 'ABSENT';
      }).length,
    [attendance]
  );

  const noShowCount = useMemo(
    () =>
      attendance.filter((a) => {
        const type = (a.eventType || a.attendanceType || '').toUpperCase();
        return type === 'NO_SHOW';
      }).length,
    [attendance]
  );

  const notServedCount = useMemo(
    () =>
      attendance.filter((a) => {
        const type = (a.eventType || a.attendanceType || '').toUpperCase();
        return type === 'NOT_SERVED';
      }).length,
    [attendance]
  );

  // --- Table Columns Definitions ---------------------------------------------
  const tripColumns = useMemo(
    () => [
      {
        key: 'trip',
        header: 'Chuyến xe',
        render: (row: any) => (
          <div className='flex items-center gap-2.5'>
            <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-red-50 text-[#C81E3A] border border-red-100/70'>
              <Milestone className='h-4 w-4' />
            </div>
            <span className='font-semibold text-slate-900'>{row.tripCode}</span>
          </div>
        ),
      },
      {
        key: 'route',
        header: 'Tuyến',
        render: (row: any) => (
          <div className='flex items-center gap-2'>
            <Route className='h-4 w-4 text-slate-400 shrink-0' />
            <span className='font-medium text-slate-700'>{row.routeCode}</span>
          </div>
        ),
      },
      {
        key: 'serviceDate',
        header: 'Service date',
        render: (row: any) => (
          <span className='text-slate-600 font-medium'>
            {formatDate(row.serviceDate)}
          </span>
        ),
      },
      {
        key: 'direction',
        header: 'Direction',
        render: (row: any) => {
          const isReturn = row.routeDirection === 'RETURN';
          return (
            <span
              className={cn(
                'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border',
                isReturn
                  ? 'bg-indigo-50/50 text-indigo-700 border-indigo-200/50'
                  : 'bg-orange-50/50 text-orange-700 border-orange-200/50'
              )}
            >
              {isReturn ? 'Return' : 'Outbound'}
            </span>
          );
        },
      },
      {
        key: 'assignment',
        header: 'Phân công',
        render: (row: any) => (
          <div className='flex flex-col gap-0.5'>
            <span className='font-semibold text-slate-700 text-xs'>
              {row.busPlateNumber || 'Chưa có xe'}
            </span>
            <span className='text-slate-400 text-[11px]'>
              {row.driverName || 'Chưa có tài xế'}
            </span>
          </div>
        ),
      },
      {
        key: 'status',
        header: 'Trạng thái',
        render: (row: any) => (
          <SchoolBusStatusBadge status={row.status} labelMap={tripStatusLabel} />
        ),
      },
    ],
    []
  );

  const attendanceColumns = useMemo(
    () => [
      {
        key: 'student',
        header: 'Học sinh',
        render: (row: any) => (
          <div className='flex items-center gap-2.5'>
            <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-violet-50 text-violet-700 border border-violet-100/70'>
              <GraduationCap className='h-4 w-4' />
            </div>
            <span className='font-semibold text-slate-900'>
              {row.studentName}
            </span>
          </div>
        ),
      },
      {
        key: 'route',
        header: 'Tuyến',
        render: (row: any) => (
          <div className='flex items-center gap-2'>
            <Route className='h-4 w-4 text-slate-400 shrink-0' />
            <span className='font-medium text-slate-700'>{row.routeCode}</span>
          </div>
        ),
      },
      {
        key: 'trip',
        header: 'Chuyến xe',
        render: (row: any) => (
          <span className='font-medium text-slate-600'>
            {row.tripId || '-'}
          </span>
        ),
      },
      {
        key: 'event',
        header: 'Sự kiện',
        render: (row: any) => {
          const evType = (
            row.eventType ||
            row.attendanceType ||
            'UNKNOWN'
          ).toUpperCase();
          let colorClass = 'bg-slate-50 text-slate-600 border-slate-200';
          if (['BOARDED', 'BOARDING'].includes(evType)) {
            colorClass = 'bg-blue-50/70 text-blue-700 border-blue-200/50';
          } else if (
            ['DROPPED_OFF', 'DROPOFF', 'DROPOFF_ONLY'].includes(evType)
          ) {
            colorClass =
              'bg-emerald-50/70 text-emerald-700 border-emerald-200/50';
          } else if (['ABSENT', 'NO_SHOW', 'NOT_SERVED'].includes(evType)) {
            colorClass = 'bg-rose-50/70 text-rose-700 border-rose-200/50';
          }
          return (
            <span
              className={cn(
                'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border capitalize',
                colorClass
              )}
            >
              {operationEventLabel[evType] ||
                attendanceTypeLabel[evType] ||
                evType.toLowerCase().replace('_', ' ')}
            </span>
          );
        },
      },
      {
        key: 'status',
        header: 'Trạng thái',
        render: (row: any) => (
          <SchoolBusStatusBadge
            status={row.status}
            labelMap={attendanceStatusLabel}
          />
        ),
      },
      {
        key: 'recorded',
        header: 'Ghi nhận lúc',
        render: (row: any) => (
          <span className='text-slate-500 font-medium text-xs'>
            {formatDateTime(row.recordedAt)}
          </span>
        ),
      },
    ],
    []
  );

  const capacityColumns = useMemo(
    () => [
      {
        key: 'trip',
        header: 'Chuyến xe',
        render: (row: any) => (
          <div className='flex items-center gap-2.5'>
            <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-slate-50 text-slate-600 border border-slate-200/70'>
              <Milestone className='h-4 w-4' />
            </div>
            <span className='font-semibold text-slate-900'>{row.tripCode}</span>
          </div>
        ),
      },
      {
        key: 'route',
        header: 'Tuyến',
        render: (row: any) => (
          <div className='flex items-center gap-2'>
            <Route className='h-4 w-4 text-slate-400 shrink-0' />
            <span className='font-medium text-slate-700'>{row.routeCode}</span>
          </div>
        ),
      },
      {
        key: 'plannedStudents',
        header: 'Học sinh dự kiến',
        render: (row: any) => (
          <div className='flex items-center gap-1.5 font-medium text-slate-700'>
            <Users className='h-3.5 w-3.5 text-slate-400 shrink-0' />
            <span>{row.plannedStudents} học sinh</span>
          </div>
        ),
      },
      {
        key: 'busCapacity',
        header: 'Sức chứa xe',
        render: (row: any) => (
          <div className='flex items-center gap-1.5 font-medium text-slate-700'>
            <Bus className='h-3.5 w-3.5 text-slate-400 shrink-0' />
            <span>{row.busCapacity} chỗ</span>
          </div>
        ),
      },
      {
        key: 'utilization',
        header: 'Utilization',
        render: (row: any) => {
          const pct = row.utilizationPercent || 0;
          const displayPct = Math.min(pct, 100);
          const barColor =
            pct > 100
              ? 'bg-rose-500'
              : pct === 100
                ? 'bg-emerald-500'
                : 'bg-blue-500';
          return (
            <div className='flex items-center gap-3 min-w-[130px]'>
              <div className='w-24 bg-slate-100 h-2 rounded-full overflow-hidden shrink-0 border border-slate-200/20'>
                <div
                  className={cn(
                    'h-full rounded-full transition-all duration-300',
                    barColor
                  )}
                  style={{ width: `${displayPct}%` }}
                />
              </div>
              <span className='text-xs font-bold text-slate-700 whitespace-nowrap'>
                {pct.toFixed(1)}%
              </span>
            </div>
          );
        },
      },
      {
        key: 'status',
        header: 'Trạng thái',
        render: (row: any) => {
          const pct = row.utilizationPercent || 0;
          if (pct > 100) {
            return (
              <span className='inline-flex items-center rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 border border-rose-200/50'>
                Over Capacity
              </span>
            );
          }
          if (pct === 100) {
            return (
              <span className='inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200/50'>
                Optimal (Full)
              </span>
            );
          }
          return (
            <span className='inline-flex items-center rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700 border border-blue-200/50'>
              Under Capacity
            </span>
          );
        },
      },
    ],
    []
  );

  // --- Toolbar Content -------------------------------------------------------
  const toolbar = (
    <div className='flex flex-col gap-4 w-full'>
      <div className='flex flex-wrap items-center justify-between gap-4'>
        <div className='flex items-center gap-2 flex-1 min-w-[240px] max-w-xs'>
          <div className='relative w-full'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
            <input
              type='text'
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder={
                activeTab === 'trips'
                  ? 'Search trips, students, routes...'
                  : activeTab === 'attendance'
                    ? 'Search students, routes...'
                    : 'Search trips, students, routes...'
              }
              className='w-full h-9 pl-9 pr-3 text-xs bg-slate-50 border border-slate-200 rounded-lg outline-none focus:bg-white focus:border-slate-300 transition-all font-medium text-slate-700'
            />
          </div>
          <Button
            variant='outline'
            size='sm'
            onClick={() => {
              setTempFilters(filters);
              setIsFilterOpen(true);
            }}
            className='h-9 rounded-lg px-3 flex items-center gap-2 border-slate-200 font-semibold text-xs text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
          >
            <Filter className='h-3.5 w-3.5 text-slate-500' />
            <span>Filter</span>
            {activeFiltersCount > 0 && (
              <span className='flex h-5 min-w-5 items-center justify-center rounded-full bg-[#C81E3A] px-1 text-[10px] font-bold text-white leading-none'>
                {activeFiltersCount}
              </span>
            )}
          </Button>
        </div>
        <div className='text-xs text-slate-400 font-semibold uppercase tracking-wider'>
          Bản ghi trên trang
        </div>
      </div>

      {activeTab === 'attendance' && attendance.length > 0 && (
        <div className='grid grid-cols-2 sm:grid-cols-5 gap-3 border-t border-slate-100 pt-3.5'>
          <div className='bg-blue-50/50 border border-blue-100/50 rounded-xl p-2.5 text-center'>
            <p className='text-[10px] font-bold text-blue-500 uppercase tracking-wider'>
              Đã lên xe
            </p>
            <p className='text-lg font-bold text-blue-700 mt-1'>
              {boardedCount}
            </p>
          </div>
          <div className='bg-emerald-50/50 border border-emerald-100/50 rounded-xl p-2.5 text-center'>
            <p className='text-[10px] font-bold text-emerald-500 uppercase tracking-wider'>
              Đã xuống xe
            </p>
            <p className='text-lg font-bold text-emerald-700 mt-1'>
              {droppedOffCount}
            </p>
          </div>
          <div className='bg-amber-50/50 border border-amber-100/50 rounded-xl p-2.5 text-center'>
            <p className='text-[10px] font-bold text-amber-500 uppercase tracking-wider'>
              Vắng mặt
            </p>
            <p className='text-lg font-bold text-amber-700 mt-1'>
              {absentCount}
            </p>
          </div>
          <div className='bg-rose-50/50 border border-rose-100/50 rounded-xl p-2.5 text-center'>
            <p className='text-[10px] font-bold text-rose-500 uppercase tracking-wider'>
              Không có mặt
            </p>
            <p className='text-lg font-bold text-rose-700 mt-1'>
              {noShowCount}
            </p>
          </div>
          <div className='bg-slate-50 border border-slate-200/50 rounded-xl p-2.5 text-center col-span-2 sm:col-span-1'>
            <p className='text-[10px] font-bold text-slate-500 uppercase tracking-wider'>
              Chưa phục vụ
            </p>
            <p className='text-lg font-bold text-slate-700 mt-1'>
              {notServedCount}
            </p>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <>
      <SchoolBusPageShell
        title='Báo cáo'
        description='Review trip execution, attendance activity, and capacity utilization.'
        breadcrumb={
          <SchoolBusBreadcrumb
            items={[
              { label: 'School Bus Ops', href: '/school-bus/dispatch' },
              { label: 'Báo cáo', current: true },
            ]}
          />
        }
      >
        <div className='space-y-6'>
          {/* --- 1. Operations Summary Metrics ---------------------------------- */}
          {loadingSummary || !report ? (
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
              {[1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  className='bg-slate-50/50 border border-slate-100 rounded-[20px] h-28 animate-pulse'
                />
              ))}
            </div>
          ) : (
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
              <SchoolBusMetricCard
                label='Yêu cầu đưa đón'
                value={report.totalRequests}
                hint='Yêu cầu trong phạm vi dữ liệu'
                icon={FileText}
                tone='info'
              />
              <SchoolBusMetricCard
                label='Yêu cầu đã duyệt'
                value={report.approvedRequests}
                hint='Đã chuyển thành đăng ký'
                icon={CheckCircle2}
                tone='success'
              />
              <SchoolBusMetricCard
                label='Chuyến đã hoàn thành'
                value={report.completedRoutes}
                hint='Tuyến đã hoàn tất vận hành'
                icon={Milestone}
                tone='success'
              />
              <SchoolBusMetricCard
                label='Sự kiện điểm danh'
                value={report.attendanceEvents}
                hint='Lên xe, xuống xe và vắng mặt'
                icon={Fingerprint}
                tone='info'
              />
            </div>
          )}

          {/* --- 2. Report Card -------------------------------------------------- */}
          <SchoolBusDataTable
            tabs={[
              {
                key: 'trips',
                label: 'Chuyến xe',
                count: tripsData?.data?.totalElements || 0,
              },
              {
                key: 'attendance',
                label: 'Attendance',
                count: attendanceData?.data?.totalElements || 0,
              },
              {
                key: 'capacity',
                label: 'Capacity',
                count: capacityData?.data?.totalElements || 0,
              },
            ]}
            activeTab={activeTab}
            onTabChange={handleTabChange}
            toolbar={toolbar}
            data={
              activeTab === 'trips'
                ? trips
                : activeTab === 'attendance'
                  ? attendance
                  : capacity
            }
            columns={
              activeTab === 'trips'
                ? tripColumns
                : activeTab === 'attendance'
                  ? attendanceColumns
                  : capacityColumns
            }
            isLoading={
              activeTab === 'trips'
                ? loadingTrips
                : activeTab === 'attendance'
                  ? loadingAttendance
                  : loadingCapacity
            }
            pagination={{
              page:
                activeTab === 'trips'
                  ? tripsData?.data
                  : activeTab === 'attendance'
                    ? attendanceData?.data
                    : capacityData?.data,
              onPageChange:
                activeTab === 'trips'
                  ? tripPagination.setPage
                  : activeTab === 'attendance'
                    ? attendancePagination.setPage
                    : capacityPagination.setPage,
            }}
            emptyIcon={
              activeTab === 'trips'
                ? Milestone
                : activeTab === 'attendance'
                  ? Fingerprint
                  : BarChart3
            }
            emptyTitle={
              activeTab === 'trips'
                ? 'Không tìm thấy bản ghi chuyến'
                : activeTab === 'attendance'
                  ? 'Không tìm thấy sự kiện điểm danh'
                  : 'Không tìm thấy bản ghi sức chứa'
            }
            emptyDescription='Adjust filters or select another reporting period.'
          />
        </div>
      </SchoolBusPageShell>

      <Sheet open={isFilterOpen} onOpenChange={setIsFilterOpen}>
        <SheetContent
          side='right'
          className='school-bus-shell flex h-full w-[100vw] flex-col justify-between overflow-y-auto bg-background p-6 text-foreground sm:max-w-[400px]'
        >
          <div className='flex flex-col gap-6'>
            <SheetHeader className='border-b border-slate-100 pb-4'>
              <SheetTitle className='text-base font-extrabold text-slate-800'>
                Filters
              </SheetTitle>
            </SheetHeader>

            <div className='space-y-4'>
              <SchoolBusDatePicker
                label='From date'
                value={tempFilters.fromDate}
                onChange={(val) =>
                  setTempFilters({ ...tempFilters, fromDate: val })
                }
                placeholder='Chọn ngày bắt đầu'
                size='sm'
                fullWidth
              />
              <SchoolBusDatePicker
                label='To date'
                value={tempFilters.toDate}
                onChange={(val) =>
                  setTempFilters({ ...tempFilters, toDate: val })
                }
                placeholder='Chọn ngày kết thúc'
                size='sm'
                fullWidth
              />
              <div className='flex flex-col gap-1.5 w-full'>
                <label className='text-xs font-semibold text-slate-700'>
                  School
                </label>
                <SchoolBusSelect
                  value={tempFilters.schoolId}
                  onChange={(val) =>
                    setTempFilters({ ...tempFilters, schoolId: val })
                  }
                  options={schoolOptions}
                  placeholder='Chọn trường'
                  size='sm'
                  searchable
                  fullWidth
                />
              </div>
              <div className='flex flex-col gap-1.5 w-full'>
                <label className='text-xs font-semibold text-slate-700'>
                  Status
                </label>
                <SchoolBusSelect
                  value={tempFilters.status}
                  onChange={(val) =>
                    setTempFilters({ ...tempFilters, status: val })
                  }
                  options={statusOptions}
                  placeholder='Chọn trạng thái'
                  size='sm'
                  fullWidth
                />
              </div>
              <div className='flex flex-col gap-1.5 w-full'>
                <label className='text-xs font-semibold text-slate-700'>
                  Direction
                </label>
                <SchoolBusSelect
                  value={tempFilters.direction}
                  onChange={(val) =>
                    setTempFilters({ ...tempFilters, direction: val })
                  }
                  options={directionOptions}
                  placeholder='Chọn chiều tuyến'
                  size='sm'
                  fullWidth
                />
              </div>
            </div>
          </div>

          <div className='flex items-center justify-between gap-3 border-t border-slate-100 pt-4 mt-6'>
            <Button
              variant='ghost'
              size='sm'
              onClick={handleResetFilters}
              className='text-slate-500 hover:bg-slate-50 rounded-lg text-xs font-semibold px-4 py-2'
            >
              <RotateCcw className='mr-1.5 h-3.5 w-3.5' /> Clear Filters
            </Button>
            <Button
              size='sm'
              onClick={handleApplyFilters}
              className='bg-[#C81E3A] text-white hover:bg-[#b01730] active:bg-[#961427] rounded-lg text-xs font-semibold shadow-sm px-4 py-2'
            >
              Apply Filters
            </Button>
          </div>
        </SheetContent>
      </Sheet>
    </>
  );
}

