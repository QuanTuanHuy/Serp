'use client';

import Link from 'next/link';
import * as React from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  Calendar,
  Clock3,
  Eye,
  GraduationCap,
  Loader2,
  Map,
  MapPin,
  PlayCircle,
  Plus,
  Route,
  Sparkles,
  UserCog,
} from 'lucide-react';

import { cn } from '@/shared/utils';
import { Button, Badge } from '@/shared/components/ui';
import {
  useGetRouteMapQuery,
  useGetRoutesQuery,
} from '../api/schoolBusApi';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPaginationBar } from '../components/SchoolBusPaginationBar';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { schoolBusUi } from '../theme';
import {
  SCHOOL_BUS_PAGE_QUERY_OPTIONS,
  formatDate,
  formatDateTime,
  getPageItems,
} from '../utils';
import { routeStatusLabel } from '../schoolBusLabels';

const routeStatusMap: Record<string, { label: string; className: string }> = {
  DRAFT: {
    label: routeStatusLabel.DRAFT,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  },
  GENERATED: {
    label: routeStatusLabel.GENERATED,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  },
  REVIEWING: {
    label: routeStatusLabel.REVIEWING,
    className: 'border-indigo-200 bg-indigo-50 text-indigo-700 hover:bg-indigo-50',
  },
  PUBLISHED: {
    label: routeStatusLabel.PUBLISHED,
    className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50',
  },
  ASSIGNED: {
    label: routeStatusLabel.ASSIGNED,
    className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50',
  },
  TRIP_CREATED: {
    label: routeStatusLabel.TRIP_CREATED,
    className:
      'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-50',
  },
  CANCELLED: {
    label: routeStatusLabel.CANCELLED,
    className: 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50',
  },
};

function RouteStatusBadge({ status }: { status: string }) {
  const cfg = routeStatusMap[status.toUpperCase()] || {
    label: status,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge
      className={cn(
        'rounded-full border px-2.5 py-0.5 text-[11px] font-semibold shadow-none',
        cfg.className
      )}
    >
      {cfg.label}
    </Badge>
  );
}

export function SchoolBusDispatchPage() {
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'lastModifiedDate',
    sortDirection: 'DESC',
  });
  const { data, isLoading } = useGetRoutesQuery(
    pagination.params,
    SCHOOL_BUS_PAGE_QUERY_OPTIONS
  );
  const [selectedRouteId, setSelectedRouteId] = React.useState<number | null>(
    null
  );
  const routes = getPageItems(data?.data);

  // Route-level status - reflects planning/dispatch state, NOT execution state
  const plannedRoutes = routes.filter((route) =>
    ['DRAFT', 'GENERATED', 'REVIEWING', 'PUBLISHED', 'ASSIGNED'].includes(
      route.status
    )
  ).length;
  const dispatchedRoutes = routes.filter(
    (route) => route.status === 'TRIP_CREATED'
  ).length;

  const prioritizedRoutes = routes;
  const {
    data: selectedRouteMap,
    isLoading: isRouteMapLoading,
    isFetching: isRouteMapFetching,
  } = useGetRouteMapQuery(
    selectedRouteId as number,
    { skip: !selectedRouteId, refetchOnMountOrArgChange: true }
  );
  const selectedRouteData = selectedRouteMap?.data;
  const isSelectedRouteDetailCurrent =
    selectedRouteId != null && selectedRouteData?.route?.id === selectedRouteId;
  const routePreviewData = isSelectedRouteDetailCurrent
    ? selectedRouteData
    : null;
  const isRoutePreviewInitialLoading =
    Boolean(selectedRouteId) &&
    (isRouteMapLoading || !isSelectedRouteDetailCurrent);
  const isRoutePreviewRefreshing =
    Boolean(selectedRouteId) &&
    Boolean(routePreviewData) &&
    isRouteMapFetching;

  // Middle stops that lack coordinates (terminals use route-level coords instead)
  const selectedRouteMissingCoordinates =
    routePreviewData?.stops.filter(
      (stop: any) =>
        stop.stopPurpose !== 'START_TERMINAL' &&
        stop.stopPurpose !== 'END_TERMINAL' &&
        (typeof stop.pickupPointLatitude !== 'number' ||
          typeof stop.pickupPointLongitude !== 'number')
    ).length || 0;

  React.useEffect(() => {
    if (!selectedRouteId && prioritizedRoutes.length > 0) {
      setSelectedRouteId(prioritizedRoutes[0].id);
    }
  }, [prioritizedRoutes, selectedRouteId]);

  return (
    <SchoolBusPageShell
      title='Bảng điều phối'
      description='Lập kế hoạch, kiểm tra và chuyển tuyến qua các bước vận hành chính.'
      breadcrumb={
        <SchoolBusBreadcrumb
          items={[
            { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
            { label: 'Điều phối', current: true },
          ]}
        />
      }
      actions={
        <Button
          asChild
          className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white'
        >
          <Link href='/school-bus/dispatch/planning'>
            <Plus className='h-4 w-4' />
            Lập tuyến
          </Link>
        </Button>
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Metrics Row */}
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label='Tuyến khả dụng'
            value={routes.length}
            hint='Các tuyến hiện có trong đơn vị'
            icon={Route}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Sẵn sàng / đã phân công'
            value={plannedRoutes}
            hint='Tuyến chưa chạy nhưng đã gần đủ điều kiện vận hành'
            icon={Map}
            tone='warning'
          />
          <SchoolBusMetricCard
            label='Đã tạo chuyến'
            value={dispatchedRoutes}
            hint='Tuyến đã có chuyến vận hành được tạo'
            icon={PlayCircle}
            tone='success'
          />
        </div>

        {/* Dispatch Workspace */}
        <div className='grid gap-6 xl:grid-cols-[1.05fr_0.95fr] xl:items-start'>
          {/* Left Column: Execution Board */}
          <div
            className={cn(
              schoolBusUi.section,
              'flex flex-col gap-4 bg-white p-5 xl:h-[calc(100vh-140px)] xl:overflow-y-auto'
            )}
          >
            <div className='flex flex-col gap-1 pb-2 border-b border-slate-100'>
              <h2 className='text-lg font-bold text-slate-950 flex items-center gap-2'>
                <Route className='h-5 w-5 text-indigo-600' />
                Bảng vận hành
              </h2>
              <p className='text-xs text-slate-500'>
                Thao tác nhanh nằm ở danh sách, trang chi tiết dùng để phân
                công và kiểm tra tuyến sâu hơn.
              </p>
            </div>

            {isLoading ? (
              <p className='text-sm text-slate-500 animate-pulse py-4'>
                Đang tải danh sách tuyến...
              </p>
            ) : prioritizedRoutes.length === 0 ? (
              <SchoolBusEmptyState
                title='Chưa có tuyến khả dụng'
                description='Hãy tạo hoặc phát hành kế hoạch tuyến trước khi điều phối.'
                icon={Route}
              />
            ) : (
              <div className='space-y-4'>
                {prioritizedRoutes.map((route) => {
                  const hasBus = Boolean(route.busId);
                  const hasDriver = Boolean(route.driverId);
                  const hasAttendant = Boolean(route.attendantId);

                  return (
                    <div
                      key={route.id}
                      className={cn(
                        schoolBusUi.interactiveCard,
                        'p-5 cursor-pointer relative transition-all duration-200 flex flex-col gap-4',
                        selectedRouteId === route.id
                          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-300 border-r-slate-300 bg-slate-50/50 ring-1 ring-slate-100 shadow-sm'
                          : ''
                      )}
                      onClick={() => setSelectedRouteId(route.id)}
                    >
                    {/* Header: Code, Name and Status */}
                    <div className='flex flex-wrap items-center justify-between gap-3'>
                      <div className='flex items-center gap-2.5 min-w-0'>
                        <div className='flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-indigo-50 text-indigo-700 border border-indigo-100/50 font-bold text-xs'>
                          R
                        </div>
                        <div className='min-w-0'>
                          <span className='font-bold text-slate-950 text-sm mr-2 truncate block sm:inline-block'>
                            {route.routeCode}
                          </span>
                          <span className='text-xs font-medium text-slate-500 truncate block sm:inline-block'>
                            {route.routeName}
                          </span>
                        </div>
                      </div>
                      <RouteStatusBadge status={route.status} />
                    </div>

                    {/* Metadata: School, Date, Schedule, Direction */}
                    <div className='flex flex-wrap items-center gap-y-1.5 gap-x-3 text-xs text-slate-500'>
                      <div className='flex items-center gap-1'>
                        <GraduationCap className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                        <span className='font-medium text-slate-700'>
                          {route.schoolName}
                        </span>
                      </div>
                      <span className='text-slate-300 hidden sm:inline'>|</span>
                      <div className='flex items-center gap-1'>
                        <Calendar className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                        <span>{formatDate(route.serviceDate)}</span>
                      </div>

                      <span className='text-slate-300 hidden sm:inline'>|</span>
                      <div className='flex items-center gap-1.5'>
                        <span
                          className={cn(
                            'inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-semibold border',
                            route.routeDirection === 'RETURN'
                              ? 'bg-orange-50 text-orange-700 border-orange-100'
                              : 'bg-blue-50 text-blue-700 border-blue-100'
                          )}
                        >
                          {route.routeDirection === 'RETURN'
                            ? 'Về nhà'
                            : 'Đến trường'}
                        </span>
                      </div>
                    </div>

                    {/* Start/Điểm kết thúcs */}
                    <div className='grid grid-cols-1 md:grid-cols-2 gap-2 p-3 rounded-xl bg-slate-50 border border-slate-100 text-xs'>
                      <div className='flex items-start gap-2 min-w-0'>
                        <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-blue-100 text-blue-700 mt-0.5'>
                          <MapPin className='h-3 w-3' />
                        </div>
                        <div className='min-w-0'>
                          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
                            Điểm xuất phát
                          </p>
                          <p className='font-semibold text-slate-800 truncate mt-0.5'>
                            {route.startLocationName || 'Chưa thiết lập'}
                          </p>
                        </div>
                      </div>
                      <div className='flex items-start gap-2 min-w-0'>
                        <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-emerald-700 mt-0.5'>
                          <MapPin className='h-3 w-3' />
                        </div>
                        <div className='min-w-0'>
                          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
                            Điểm kết thúc
                          </p>
                          <p className='font-semibold text-slate-800 truncate mt-0.5'>
                            {route.endLocationName || 'Chưa thiết lập'}
                          </p>
                        </div>
                      </div>
                    </div>

                    {/* Resource Assignment Status Strip */}
                    <div className='flex flex-wrap gap-2 text-xs'>
                      <span
                        className={cn(
                          'inline-flex items-center gap-1 rounded-lg px-2 py-1 text-[11px] font-medium border shadow-none',
                          hasBus
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                            : 'bg-amber-50 text-amber-700 border-amber-100'
                        )}
                      >
                        {hasBus ? 'Đã phân công xe' : 'Thiếu xe'}
                      </span>
                      <span
                        className={cn(
                          'inline-flex items-center gap-1 rounded-lg px-2 py-1 text-[11px] font-medium border shadow-none',
                          hasDriver
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                            : 'bg-amber-50 text-amber-700 border-amber-100'
                        )}
                      >
                        {hasDriver
                          ? 'Đã phân công tài xế'
                          : 'Thiếu tài xế'}
                      </span>
                      <span
                        className={cn(
                          'inline-flex items-center gap-1 rounded-lg px-2 py-1 text-[11px] font-medium border shadow-none',
                          hasAttendant
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                            : 'bg-amber-50 text-amber-750 border-amber-100'
                        )}
                      >
                        {hasAttendant
                          ? 'Đã phân công phụ xe'
                          : 'Thiếu phụ xe'}
                      </span>
                    </div>

                    {/* Bottom Metadata & Actions */}
                    <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-t border-slate-100 pt-3'>
                      <div className='flex flex-wrap gap-4 text-xs text-slate-500'>
                        <span>
                          Khoảng cách:{' '}
                          <strong className='text-slate-700'>
                            {route.plannedDistanceKm || 0} km
                          </strong>
                        </span>
                        <span>
                          Thời lượng:{' '}
                          <strong className='text-slate-700'>
                            {route.plannedDurationMin || 0} phút
                          </strong>
                        </span>
                        {route.startedAt && (
                          <span>
                            Bắt đầu:{' '}
                            <strong className='text-slate-700'>
                              {formatDateTime(route.startedAt)}
                            </strong>
                          </span>
                        )}
                      </div>

                      <div
                        className='flex flex-wrap gap-1.5'
                        onClick={(e) => e.stopPropagation()}
                      >
                        {!(
                          route.status === 'ASSIGNED' ||
                          route.status === 'TRIP_CREATED' ||
                          route.status === 'IN_PROGRESS' ||
                          route.status === 'COMPLETED'
                        ) ? (
                          <Button
                            className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white hover:text-white h-8 text-xs gap-1 font-semibold'
                            asChild
                          >
                            <Link
                              href={`/school-bus/dispatch/${route.id}?assign=true`}
                            >
                              <UserCog className='h-3.5 w-3.5' />
                              Phân công
                            </Link>
                          </Button>
                        ) : (
                          <Button
                            variant='outline'
                            className='rounded-full border-slate-200 text-slate-700 h-8 text-xs gap-1 font-semibold'
                            asChild
                          >
                            <Link
                              href={`/school-bus/dispatch/${route.id}?assign=true`}
                            >
                              <UserCog className='h-3.5 w-3.5' />
                              Quản lý phân công
                            </Link>
                          </Button>
                        )}
                        <Button
                          variant='outline'
                          className='rounded-full border-slate-200 text-slate-700 h-8 text-xs gap-1 font-semibold'
                          asChild
                        >
                          <Link href={`/school-bus/dispatch/${route.id}`}>
                            <Eye className='h-3.5 w-3.5' />
                            Xem chi tiết
                          </Link>
                        </Button>
                        <Button
                          variant='outline'
                          className='rounded-full border-red-200 text-[#C81E3A] hover:bg-[#FDECEF]/50 h-8 text-xs gap-1 font-semibold'
                          asChild
                        >
                          <Link
                            href={`/school-bus/dispatch/planning?sessionId=${route.planningSessionId}&routeId=${route.id}`}
                          >
                            <Sparkles className='h-3.5 w-3.5' />
                            Mở lập kế hoạch
                          </Link>
                        </Button>
                      </div>
                    </div>
                    </div>
                  );
                })}

                <div className='pt-2'>
                  <SchoolBusPaginationBar
                    page={data?.data}
                    onPageChange={pagination.setPage}
                  />
                </div>
              </div>
            )}
          </div>

          {/* Right Column: Route Preview Map */}
          <div
            className={cn(
              schoolBusUi.section,
              'flex flex-col gap-4 bg-white p-5 xl:sticky xl:top-[88px] xl:h-[calc(100vh-140px)] xl:min-h-[560px] xl:max-h-[850px]'
            )}
          >
            <div className='flex flex-col gap-1 pb-2 border-b border-slate-100'>
              <h2 className='text-lg font-bold text-slate-950 flex items-center gap-2'>
                <Map className='h-5 w-5 text-emerald-600' />
                Bản đồ xem trước tuyến
              </h2>
              <p className='text-xs text-slate-500'>
                Tuyến xe đã chọn hiển thị với điểm đầu, điểm cuối, các điểm đón trả và đường đi theo thứ tự.
              </p>
            </div>

            {isRoutePreviewInitialLoading ? (
              <div className='flex flex-col items-center justify-center p-12 text-center flex-1 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200 min-h-[400px]'>
                <div className='mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-[#C81E3A] shadow-sm ring-1 ring-slate-200'>
                  <Loader2 className='h-6 w-6 animate-spin' />
                </div>
                <h3 className='text-sm font-semibold text-slate-800'>
                  Đang tải xem trước tuyến...
                </h3>
                <p className='text-xs text-slate-500 max-w-[260px] mt-1'>
                  Đang chuẩn bị điểm dừng và đường đi cho tuyến đang chọn.
                </p>
                <div className='mt-6 w-full max-w-sm space-y-2'>
                  <div className='h-3 rounded-full bg-slate-200/80 animate-pulse' />
                  <div className='h-3 w-3/4 rounded-full bg-slate-200/70 animate-pulse mx-auto' />
                  <div className='h-3 w-1/2 rounded-full bg-slate-200/60 animate-pulse mx-auto' />
                </div>
              </div>
            ) : routePreviewData ? (
              <div className='relative rounded-2xl border border-slate-200 overflow-hidden shadow-sm flex flex-col flex-1 min-h-0'>
                {isRoutePreviewRefreshing && (
                  <div className='absolute inset-0 z-20 flex items-center justify-center bg-white/70 backdrop-blur-[1px]'>
                    <div className='flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 shadow-sm'>
                      <Loader2 className='h-4 w-4 animate-spin text-[#C81E3A]' />
                      Đang cập nhật xem trước tuyến...
                    </div>
                  </div>
                )}
                <SchoolBusMapWorkspace
                  flat={true}
                  mapHeightClassName='h-[400px] xl:h-full'
                  map={
                    <RouteMap
                      route={routePreviewData.route}
                      stops={routePreviewData.stops}
                      assignment={routePreviewData.assignment}
                      routePath={routePreviewData.path}
                      className='h-full w-full'
                    />
                  }
                  legend={<SchoolBusMapLegend />}
                  panel={
                    <div className='space-y-4 p-4 min-w-[280px]'>
                      {/* Tóm tắt tuyến */}
                      <div>
                        <h3 className='text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5'>
                          Tóm tắt tuyến
                        </h3>
                        <div className='space-y-2 text-xs'>
                          <div className='flex items-start gap-2 min-w-0'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-600 mt-0.5'>
                              <MapPin className='h-3 w-3' />
                            </div>
                            <div className='min-w-0'>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Điểm bắt đầu
                              </p>
                              <p
                                className='font-medium text-slate-700 truncate mt-0.5'
                                title={
                                  routePreviewData.route.startLocationName
                                }
                              >
                                {routePreviewData.route.startLocationName ||
                                  'Chưa thiết lập'}
                              </p>
                            </div>
                          </div>

                          <div className='flex items-start gap-2 min-w-0'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 mt-0.5'>
                              <MapPin className='h-3 w-3' />
                            </div>
                            <div className='min-w-0'>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Điểm kết thúc
                              </p>
                              <p
                                className='font-medium text-slate-700 truncate mt-0.5'
                                title={
                                  routePreviewData.route.endLocationName
                                }
                              >
                                {routePreviewData.route.endLocationName ||
                                  'Chưa thiết lập'}
                              </p>
                            </div>
                          </div>

                          <div className='flex items-center gap-2'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-600'>
                              <Route className='h-3 w-3' />
                            </div>
                            <div>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Số điểm dừng
                              </p>
                              <p className='font-medium text-slate-700 mt-0.5'>
                                {routePreviewData.stops.length} điểm
                              </p>
                            </div>
                          </div>

                          <div className='flex items-center gap-2'>
                            <div className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-600'>
                              <Clock3 className='h-3 w-3' />
                            </div>
                            <div>
                              <p className='text-[10px] font-semibold text-slate-400 leading-none'>
                                Trạng thái
                              </p>
                              <div className='mt-0.5'>
                                <RouteStatusBadge
                                  status={routePreviewData.route.status}
                                />
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>

                      <hr className='border-slate-100' />

                      {/* Route Metrics */}
                      <div>
                        <h3 className='text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5'>
                          Thông số tuyến
                        </h3>
                        <div className='grid grid-cols-2 gap-2 text-xs'>
                          <div className='p-2 rounded-lg bg-slate-50 border border-slate-100'>
                            <p className='text-[9px] font-semibold text-slate-400 uppercase tracking-wide'>
                              Khoảng cách
                            </p>
                            <p className='font-bold text-slate-800 mt-0.5'>
                              {routePreviewData.route.plannedDistanceKm || 0}{' '}
                              km
                            </p>
                          </div>
                          <div className='p-2 rounded-lg bg-slate-50 border border-slate-100'>
                            <p className='text-[9px] font-semibold text-slate-400 uppercase tracking-wide'>
                              Thời lượng
                            </p>
                            <p className='font-bold text-slate-800 mt-0.5'>
                              {routePreviewData.route.plannedDurationMin || 0}{' '}
                              phút
                            </p>
                          </div>
                        </div>
                      </div>

                      <hr className='border-slate-100' />

                      {/* Missing coordinates warning */}
                      {selectedRouteMissingCoordinates > 0 && (
                        <div className='rounded-lg border border-amber-200 bg-amber-50/50 p-2.5 text-xs text-amber-700 leading-normal'>
                          <p className='font-semibold'>
                            Cảnh báo: {selectedRouteMissingCoordinates} điểm dừng thiếu tọa độ.
                          </p>
                          <p className='text-[10px] text-amber-600 mt-0.5'>
                            Đường đi của tuyến chỉ vẽ qua các đoạn có tọa độ.
                          </p>
                        </div>
                      )}
                    </div>
                  }
                />
              </div>
            ) : (
              <div className='flex flex-col items-center justify-center p-12 text-center flex-1 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200 min-h-[400px]'>
                <Map className='h-12 w-12 text-slate-300 stroke-[1.5] mb-3' />
                <h3 className='text-sm font-semibold text-slate-800'>
                  Chọn tuyến
                </h3>
                <p className='text-xs text-slate-500 max-w-[240px] mt-1'>
                  Chọn một tuyến từ bảng vận hành để xem trước đường đi.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </SchoolBusPageShell>
  );
}

