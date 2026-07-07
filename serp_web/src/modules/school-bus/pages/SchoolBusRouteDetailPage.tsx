'use client';

import Link from 'next/link';
import * as React from 'react';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import {
  ClipboardCheck,
  BusFront,
  MapPinned,
  Pencil,
  PlayCircle,
  Route,
  Sparkles,
  UserCog,
  GraduationCap,
  Calendar,
  Clock3,
  Loader2,
  MapPin,
  Users,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useAssignRouteMutation,
  useCreateTripFromRouteMutation,
  useGetAttendantDropdownOptionsQuery,
  useGetBusesQuery,
  useGetDriverDropdownOptionsQuery,
  useGetRouteMapQuery,
} from '../api/schoolBusApi';
import { RouteAssignmentDialog } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusSection } from '../components/SchoolBusSection';
import { SchoolBusScrollableTable } from '../components/SchoolBusScrollableTable';
import { SchoolBusStatusBadge } from '../components/SchoolBusStatusBadge';
import { RouteMap } from '../components/map/RouteMap';
import { SchoolBusMapLegend } from '../components/map/SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from '../components/map/SchoolBusMapWorkspace';
import { schoolBusUi } from '../theme';
import {
  formatDate,
  formatDateTime,
  getPageItems,
  SCHOOL_BUS_OPTION_QUERY,
} from '../utils';
import { directionLabel, routeStatusLabel } from '../schoolBusLabels';

interface SchoolBusRouteDetailPageProps {
  routeId: number;
}

export function SchoolBusRouteDetailPage({
  routeId,
}: SchoolBusRouteDetailPageProps) {
  const {
    data,
    isLoading,
    isFetching,
  } = useGetRouteMapQuery(routeId, { refetchOnMountOrArgChange: true });
  const { data: driversData } = useGetDriverDropdownOptionsQuery();

  const { data: attendantsData } = useGetAttendantDropdownOptionsQuery();
  const { data: busesData } = useGetBusesQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'plateNumber',
  });

  const [assignRoute, { isLoading: assigning }] = useAssignRouteMutation();
  const [createTripFromRoute, { isLoading: creatingTrip }] =
    useCreateTripFromRouteMutation();
  const [assignmentOpen, setAssignmentOpen] = React.useState(false);
  const [fitKey, setFitKey] = React.useState(0);

  const detail = data?.data;

  React.useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      if (params.get('assign') === 'true') {
        setAssignmentOpen(true);
      }
    }
  }, []);

  const handleAssign = async (values: any) => {
    try {
      const response = await assignRoute({
        id: routeId,
        body: values,
      }).unwrap();
      toast.success(response.message || 'Đã phân công nguồn lực');
      setAssignmentOpen(false);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể phân công tuyến');
    }
  };

  const handleCreateTrip = async () => {
    try {
      const response = await createTripFromRoute(routeId).unwrap();
      toast.success(response.message || 'Đã tạo chuyến từ tuyến');
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể tạo chuyến');
    }
  };

  if (isLoading || !detail) {
    return (
      <SchoolBusPageShell
        title='Chi tiết tuyến'
        description='Đang tải chi tiết tuyến...'
      >
        <SchoolBusEmptyState
          title='Đang tải chi tiết tuyến'
          description='Đang tải dữ liệu tuyến, điểm dừng và phân công.'
        />
      </SchoolBusPageShell>
    );
  }

  const route = detail.route;
  const selectedBusPlate =
    detail.assignment?.busPlateNumber || route.busPlateNumber;
  const selectedBusCapacity =
    detail.assignment?.busCapacity ||
    route.busCapacity;
  const plannedStudentCount =
    route.plannedStudentCount || 0;
  const stopsCount = route.stopsCount || detail.stops.length;
  const driverName = detail.assignment?.driverName || route.driverName;
  const attendantName = detail.assignment?.attendantName || route.attendantName;
  const hasBusReservation = Boolean(detail.assignment?.busId || route.busId);
  const hasStaffAssignment = Boolean(driverName);
  const isAssigned = hasBusReservation && hasStaffAssignment;
  const missingResourceCount = [
    !selectedBusPlate,
    !driverName,
    !attendantName,
  ].filter(Boolean).length;
  // Middle stops that lack coordinates (terminals use route-level coords instead)
  const missingStopCoordinates = detail.stops.filter(
    (stop) =>
      stop.stopPurpose !== 'START_TERMINAL' &&
      stop.stopPurpose !== 'END_TERMINAL' &&
      (typeof stop.pickupPointLatitude !== 'number' ||
        typeof stop.pickupPointLongitude !== 'number')
  ).length;

  const friendlyStatusLabel = (status: string) =>
    routeStatusLabel[status.toUpperCase()] || status;

  return (
    <>
      <SchoolBusPageShell
        title={`${route.routeCode} - ${route.routeName}`}
        description='Kiểm tra trạng thái tuyến, lộ trình điểm dừng và chi tiết phân công trước và trong khi vận hành.'
        actions={
          <>
            {['DRAFT', 'GENERATED', 'REVIEWING', 'PUBLISHED', 'ASSIGNED'].includes(route.status) ? (
              <Button variant='outline' className='rounded-full' asChild>
                <Link href={`/school-bus/dispatch/${route.id}/edit`}>
                  <Pencil className='h-4 w-4' />
                  Chỉnh sửa
                </Link>
              </Button>
            ) : null}
            <Button
              variant={isAssigned ? 'outline' : 'default'}
              className={cn(
                'rounded-full font-semibold',
                !isAssigned &&
                  'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
              )}
              onClick={() => setAssignmentOpen(true)}
            >
              <UserCog className='h-4 w-4' />
              {hasBusReservation ? 'Quản lý nguồn lực' : 'Phân công nguồn lực'}
            </Button>
            <Button variant='outline' className='rounded-full' asChild>
              <Link
                href={`/school-bus/dispatch/planning?sessionId=${route.planningSessionId || ''}&routeId=${route.id || ''}`}
              >
                <Sparkles className='h-4 w-4' />
                Mở phiên lập tuyến
              </Link>
            </Button>
            {route.status === 'ASSIGNED' ? (
              <Button
                className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white font-semibold border-0'
                onClick={handleCreateTrip}
                disabled={creatingTrip}
              >
                <BusFront className='h-4 w-4 mr-1' />
                {creatingTrip ? 'Đang tạo...' : 'Tạo chuyến'}
              </Button>
            ) : null}
          </>
        }
      >
        {/* Status Strip */}
        <div className='grid grid-cols-2 md:grid-cols-5 gap-4 mb-6'>
          <StatusCard
            label='Lập kế hoạch'
            value={route.status === 'DRAFT' ? 'Nháp' : 'Hoàn tất'}
            status={route.status === 'DRAFT' ? 'warning' : 'success'}
          />
          <StatusCard
            label='Phân công'
            value={isAssigned ? 'Sẵn sàng' : hasBusReservation ? 'Đã chọn xe' : 'Còn thiếu'}
            status={isAssigned ? 'success' : 'warning'}
          />
          <StatusCard
            label='Chuyến vận hành'
            value={
              ['TRIP_CREATED'].includes(
                route.status
              )
                ? 'Đã khóa'
                : 'Chưa tạo'
            }
            status={
              ['TRIP_CREATED'].includes(
                route.status
              )
                ? 'success'
                : 'muted'
            }
          />
          <StatusCard
            label='Điểm danh'
            value={
              ['TRIP_CREATED'].includes(
                route.status
              )
                ? 'Sẵn sàng'
                : 'Chưa mở'
            }
            status={
              ['TRIP_CREATED'].includes(
                route.status
              )
                ? 'success'
                : 'muted'
            }
          />
        </div>

        {/* Main 2-column workspace */}
        <div className='grid gap-6 xl:grid-cols-[0.95fr_1.05fr] items-start'>
          {/* Left Column */}
          <div className='flex flex-col gap-6'>
            {/* Thông tin tuyến compact card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Route className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>
                    Thông tin tuyến
                  </h3>
                </div>
                <span className='rounded-full px-2.5 py-0.5 text-[10px] font-extrabold border shadow-none bg-slate-50 border-slate-200 text-slate-600 uppercase tracking-wider'>
                  {friendlyStatusLabel(route.status)}
                </span>
              </div>

              <div className='grid grid-cols-2 gap-y-4 gap-x-6 text-xs'>
                <SummaryItem
                  label='Trường học'
                  value={route.schoolName}
                  icon={GraduationCap}
                />
                <SummaryItem
                  label='Chiều tuyến'
                  value={
                    directionLabel[route.routeDirection] || route.routeDirection
                  }
                  icon={MapPinned}
                  isBadge
                  badgeColor={
                    route.routeDirection === 'RETURN'
                      ? 'bg-orange-50 text-orange-700 border-orange-100'
                      : 'bg-blue-50 text-blue-700 border-blue-100'
                  }
                />
                <SummaryItem
                  label='Ngày phục vụ'
                  value={formatDate(route.serviceDate)}
                  icon={Calendar}
                />

                <SummaryItem
                  label='Điểm đầu tuyến'
                  value={`${route.startLocationName} (${route.startLocationType})`}
                  icon={MapPin}
                />
                <SummaryItem
                  label='Điểm cuối tuyến'
                  value={`${route.endLocationName} (${route.endLocationType})`}
                  icon={MapPin}
                />
                <SummaryItem
                  label='Bãi xe'
                  value={
                    route.startDepotName ||
                    (route.startLocationType === 'DEPOT'
                      ? route.startLocationName
                      : route.endLocationName)
                  }
                  icon={MapPin}
                />
                <SummaryItem
                  label='Quãng đường dự kiến'
                  value={
                    route.plannedDistanceKm != null
                      ? `${route.plannedDistanceKm} km`
                      : 'N/A'
                  }
                  icon={Route}
                />
                <SummaryItem
                  label='Thời gian dự kiến'
                  value={
                    route.plannedDurationMin != null
                      ? `${route.plannedDurationMin} phút`
                      : 'N/A'
                  }
                  icon={Clock3}
                />
                <SummaryItem
                  label='Xe'
                  value={selectedBusPlate || 'Chưa chọn xe'}
                  icon={BusFront}
                />
                <SummaryItem
                  label='Sức chứa'
                  value={
                    selectedBusCapacity != null
                      ? `${selectedBusCapacity} chỗ`
                      : 'N/A'
                  }
                  icon={Users}
                />
                <SummaryItem
                  label='Học sinh'
                  value={
                    selectedBusCapacity != null
                      ? `${plannedStudentCount}/${selectedBusCapacity}`
                      : String(plannedStudentCount)
                  }
                  icon={Users}
                />
                <SummaryItem
                  label='Điểm dừng'
                  value={String(stopsCount)}
                  icon={Route}
                />
                <SummaryItem
                  label='Tài xế'
                  value={driverName || 'Chưa có tài xế'}
                  icon={UserCog}
                />
                <SummaryItem
                  label='Phụ xe'
                  value={attendantName || 'Chưa có phụ xe'}
                  icon={UserCog}
                />
                <SummaryItem
                  label='Bắt đầu lúc'
                  value={formatDateTime(route.startedAt) || 'N/A'}
                  icon={PlayCircle}
                />
                <SummaryItem
                  label='Hoàn thành lúc'
                  value={formatDateTime(route.completedAt) || 'N/A'}
                  icon={ClipboardCheck}
                />
              </div>
            </div>

            {/* Assignment Details card */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <UserCog className='h-4.5 w-4.5 text-emerald-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>
                    Chi tiết phân công
                  </h3>
                </div>
                <span
                  className={cn(
                    'rounded-full px-2.5 py-0.5 text-[10px] font-bold border shadow-none uppercase tracking-wider',
                    isAssigned
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-250'
                      : 'bg-amber-50 text-amber-700 border-amber-250'
                  )}
                >
                  {isAssigned
                    ? 'Sẵn sàng vận hành'
                    : `Thiếu ${missingResourceCount} nguồn lực`}
                </span>
              </div>

              <div className='grid gap-3'>
                <AssignmentRow
                  label='Xe'
                  value={selectedBusPlate}
                  type='bus'
                />
                <AssignmentRow
                  label='Tài xế'
                  value={driverName}
                  type='driver'
                />
                <AssignmentRow
                  label='Phụ xe'
                  value={attendantName}
                  type='attendant'
                />
              </div>

              <div className='pt-2'>
                <Button
                  variant={isAssigned ? 'outline' : 'default'}
                  className={cn(
                    'w-full rounded-full text-xs font-semibold h-9',
                    !isAssigned &&
                      'bg-[#C81E3A] hover:bg-[#B31B34] text-white border-0'
                  )}
                  onClick={() => setAssignmentOpen(true)}
                >
                  <UserCog className='h-3.5 w-3.5 mr-1.5' />
                  {isAssigned ? 'Quản lý phân công' : 'Phân công nguồn lực'}
                </Button>
              </div>
            </div>

            {/* Next Recommended Actions (Workflow Stepper) */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center gap-2 pb-2.5 border-b border-slate-100'>
                <ClipboardCheck className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                <h3 className='font-bold text-slate-900 text-sm'>
                  Quy trình tuyến
                </h3>
              </div>

              <div className='relative pl-3.5 space-y-6 before:absolute before:left-[23px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100'>
                {/* Step 1: Plan Route */}
                <WorkflowStep
                  stepNumber={1}
                  title='Lập tuyến'
                  description='Xác định trường, lịch phục vụ, chiều tuyến và các điểm dừng.'
                  status='completed'
                />

                {/* Step 2: Assign Resources */}
                <WorkflowStep
                  stepNumber={2}
                  title='Phân công nguồn lực'
                  description='Phân công tài xế và phụ xe để vận hành tuyến.'
                  status={isAssigned ? 'completed' : 'current'}
                  action={
                    !isAssigned ? (
                      <Button
                        size='sm'
                        variant='outline'
                        className='h-7 text-[10px] rounded-full border-slate-200 mt-2 font-semibold shadow-none'
                        onClick={() => setAssignmentOpen(true)}
                      >
                        Phân công nguồn lực
                      </Button>
                    ) : null
                  }
                />

                {/* Step 3: Create Trip Snapshot */}
                <WorkflowStep
                  stepNumber={3}
                  title='Tạo chuyến vận hành'
                  description='Chốt cấu trúc tuyến thành danh sách vận hành.'
                  status={
                    ['TRIP_CREATED'].includes(
                      route.status
                    )
                      ? 'completed'
                      : isAssigned
                        ? 'available'
                        : 'locked'
                  }
                  action={
                    route.status === 'ASSIGNED' ? (
                      <Button
                        size='sm'
                        className='h-7 text-[10px] rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white mt-2 font-semibold shadow-none'
                        onClick={handleCreateTrip}
                        disabled={creatingTrip}
                      >
                        {creatingTrip ? 'Đang tạo...' : 'Tạo chuyến'}
                      </Button>
                    ) : null
                  }
                />

                {/* Step 4: Run Attendance */}
                <WorkflowStep
                  stepNumber={4}
                  title='Điểm danh'
                  description='Ghi nhận học sinh lên và xuống xe theo danh sách chuyến.'
                  status={
                    ['TRIP_CREATED'].includes(route.status)
                        ? 'available'
                        : 'locked'
                  }
                  action={
                    ['TRIP_CREATED'].includes(route.status) ? (
                      <Button
                        size='sm'
                        variant='outline'
                        className='h-7 text-[10px] rounded-full border-slate-200 mt-2 font-semibold shadow-none'
                        asChild
                      >
                        <Link href='/school-bus/trips'>
                          Mở vận hành chuyến
                        </Link>
                      </Button>
                    ) : null
                  }
                />

                {/* Step 5: Complete Journey */}
                <WorkflowStep
                  stepNumber={5}
                  title='Hoàn thành chuyến'
                  description='Đánh dấu chuyến xe đã hoàn tất.'
                  status={
                    route.status === 'TRIP_CREATED' ? 'available' : 'locked'
                  }
                />
              </div>
            </div>
          </div>

          {/* Right Column */}
          <div className='flex flex-col gap-6 lg:sticky lg:top-6 lg:self-start'>
            {/* Bản đồ tuyến đường Card with Workspace Wrapper */}
            <div className='bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm flex flex-col'>
              <div className='p-4 border-b border-slate-100 flex items-center justify-between'>
                <h3 className='font-bold text-slate-900 text-sm flex items-center gap-2'>
                  <MapPinned className='h-4.5 w-4.5 text-indigo-600' />
                  Route Map
                </h3>
                {missingStopCoordinates > 0 && (
                  <span className='rounded bg-amber-50 border border-amber-100 text-amber-700 font-bold text-[9px] px-1.5 py-0.5 animate-pulse'>
                    Cảnh báo: {missingStopCoordinates} điểm dừng thiếu tọa độ
                  </span>
                )}
              </div>
              <div className='w-full bg-slate-50'>
                <div className='relative'>
                  {isFetching && (
                    <div className='absolute inset-0 z-20 flex items-center justify-center bg-white/70 backdrop-blur-[1px]'>
                      <div className='flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 shadow-sm'>
                        <Loader2 className='h-4 w-4 animate-spin text-[#C81E3A]' />
                        Đang cập nhật bản đồ...
                      </div>
                    </div>
                  )}
                  <SchoolBusMapWorkspace
                    flat
                    mapHeightClassName='h-[420px]'
                    map={
                      <RouteMap
                        route={detail.route}
                        stops={detail.stops}
                        assignment={detail.assignment}
                        routePath={detail.path}
                        className='h-full w-full'
                        fitKey={fitKey}
                      />
                    }
                    legend={<SchoolBusMapLegend />}
                    onFitAll={() => setFitKey((k) => k + 1)}
                    canFitAll={true}
                  />
                </div>
              </div>
            </div>

            {/* Stop sequence timeline */}
            <div className='bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4'>
              <div className='flex items-center justify-between pb-2.5 border-b border-slate-100'>
                <div className='flex items-center gap-2'>
                  <Route className='h-4.5 w-4.5 text-indigo-600 shrink-0' />
                  <h3 className='font-bold text-slate-900 text-sm'>
                    Kế hoạch lộ trình điểm dừng
                  </h3>
                </div>
                <span className='text-[10px] text-slate-500 font-bold bg-slate-50 border border-slate-150 px-2 py-0.5 rounded-full'>
                  {detail.stops.length} điểm dừng
                </span>
              </div>

              {detail.stops.length === 0 ? (
                <SchoolBusEmptyState
                  title='Chưa tạo điểm dừng'
                  description='Tạo kế hoạch tuyến để xác lập lộ trình điểm dừng.'
                  icon={Route}
                />
              ) : (
                <div className='relative pl-3 space-y-4 before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-100'>
                  {detail.stops.map((stop) => {
                    const isStart = stop.stopPurpose === 'START_TERMINAL';
                    const isEnd = stop.stopPurpose === 'END_TERMINAL';
                    const isDropoff = stop.stopPurpose === 'DROPOFF';

                    return (
                      <div
                        key={stop.id}
                        className='relative flex items-start gap-4 text-xs'
                      >
                        {/* Node */}
                        <span
                          className={cn(
                            'absolute -left-[20px] top-1 flex h-[22px] w-[22px] items-center justify-center rounded-full text-[9px] font-extrabold ring-4 ring-white border shadow-sm',
                            isStart
                              ? 'bg-orange-500 border-orange-655 text-white'
                              : isEnd
                                ? 'bg-red-500 border-red-655 text-white'
                                : isDropoff
                                  ? 'bg-emerald-500 border-emerald-655 text-white'
                                  : 'bg-blue-500 border-blue-655 text-white'
                          )}
                        >
                          {isStart ? 'S' : isEnd ? 'E' : stop.stopOrder}
                        </span>

                        <div className='min-w-0 flex-1 p-3 rounded-xl bg-slate-50/50 border border-slate-100 flex items-center justify-between gap-3'>
                          <div className='min-w-0'>
                            <p className='font-bold text-slate-800 truncate'>
                              {stop.displayName ||
                                stop.pickupPointName ||
                                `Stop #${stop.id}`}
                            </p>
                            <p className='text-[10px] text-slate-450 mt-1 flex items-center gap-1.5'>
                              <span
                                className={cn(
                                  'inline-flex items-center rounded px-1.5 py-0.5 text-[8px] font-extrabold border uppercase tracking-wider',
                                  isStart
                                    ? 'bg-orange-50 text-orange-700 border-orange-100'
                                    : isEnd
                                      ? 'bg-red-50 text-red-700 border-red-100'
                                      : isDropoff
                                        ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
                                        : 'bg-blue-50 text-blue-700 border-blue-100'
                                )}
                              >
                                {isStart
                                  ? 'Bãi xe'
                                  : isEnd
                                    ? 'Trường học'
                                    : isDropoff
                                      ? 'Điểm trả'
                                      : 'Điểm đón'}
                              </span>
                              <span>-</span>
                              <span>
                                {stop.estimatedStudentCount || 0} học sinh
                              </span>
                            </p>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      </SchoolBusPageShell>

      <RouteAssignmentDialog
        open={assignmentOpen}
        onOpenChange={setAssignmentOpen}
        initialData={detail.assignment}
        buses={getPageItems(busesData?.data).map((bus) => ({
          id: bus.id,
          label: `${bus.plateNumber} - ${bus.capacity} chỗ`,
          value: String(bus.id),
        }))}
        drivers={driversData?.data || []}
        attendants={attendantsData?.data || []}
        onSubmit={handleAssign}
        isLoading={assigning}
      />
    </>
  );
}

function StatusCard({
  label,
  value,
  status,
}: {
  label: string;
  value: string;
  status: 'success' | 'warning' | 'muted' | 'danger';
}) {
  return (
    <div className='bg-white border border-slate-205 rounded-2xl p-4 shadow-sm flex flex-col justify-between h-[80px]'>
      <span className='text-[10px] font-bold text-slate-400 uppercase tracking-wider'>
        {label}
      </span>
      <span
        className={cn(
          'text-sm font-extrabold mt-1 inline-flex items-center gap-1.5',
          status === 'success'
            ? 'text-emerald-700'
            : status === 'warning'
              ? 'text-amber-700'
              : status === 'danger'
                ? 'text-rose-700'
                : 'text-slate-400'
        )}
      >
        {status === 'success'
          ? 'Đạt '
          : status === 'warning'
            ? 'Cảnh báo: '
            : status === 'danger'
              ? 'Lỗi '
              : ' '}
        {value}
      </span>
    </div>
  );
}

function SummaryItem({
  label,
  value,
  icon: Icon,
  isBadge = false,
  badgeColor = '',
}: {
  label: string;
  value: string | null | undefined;
  icon: any;
  isBadge?: boolean;
  badgeColor?: string;
}) {
  const displayVal = value || 'N/A';
  const isNa = displayVal === 'N/A';
  return (
    <div className='flex items-start gap-2.5 min-w-0'>
      <div className='flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-slate-50 text-slate-400 mt-0.5 border border-slate-100'>
        <Icon className='h-3.5 w-3.5' />
      </div>
      <div className='min-w-0 flex-1'>
        <p className='text-[10px] font-semibold text-slate-400 leading-none'>
          {label}
        </p>
        {isBadge && !isNa ? (
          <span
            className={cn(
              'inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-bold border mt-1',
              badgeColor
            )}
          >
            {displayVal}
          </span>
        ) : (
          <p
            className={cn(
              'font-bold text-slate-705 truncate mt-1 text-[11px]',
              isNa && 'text-slate-400 italic font-medium'
            )}
          >
            {displayVal}
          </p>
        )}
      </div>
    </div>
  );
}

function AssignmentRow({
  label,
  value,
  type,
}: {
  label: string;
  value: string | null | undefined;
  type: 'bus' | 'driver' | 'attendant';
}) {
  const Icon = type === 'bus' ? BusFront : type === 'driver' ? UserCog : Users;
  const isMissing = !value;
  return (
    <div
      className={cn(
        'flex items-center justify-between p-3.5 rounded-xl border transition-all duration-155',
        isMissing
          ? 'bg-amber-50/20 border-amber-100 text-amber-800'
          : 'bg-slate-50/50 border-slate-100 text-slate-800'
      )}
    >
      <div className='flex items-center gap-3.5 min-w-0'>
        <div
          className={cn(
            'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border shadow-sm',
            isMissing
              ? 'bg-amber-50 text-amber-500 border-amber-100'
              : 'bg-indigo-50 text-indigo-650 border-indigo-100'
          )}
        >
          <Icon className='h-4 w-4' />
        </div>
        <div className='min-w-0'>
          <p className='text-[10px] font-semibold text-slate-405 leading-none'>
            {label}
          </p>
          <p
            className={cn(
              'font-bold text-slate-800 mt-1 truncate text-xs',
              isMissing && 'text-amber-700 italic font-medium'
            )}
          >
            {value || `Thiếu ${type === 'bus' ? 'xe' : type === 'driver' ? 'tài xế' : 'phụ xe'}`}
          </p>
        </div>
      </div>
      <span
        className={cn(
          'rounded-full px-2.5 py-0.5 text-[9px] font-extrabold border shadow-none shrink-0 uppercase tracking-wider',
          isMissing
            ? 'bg-amber-50 text-amber-750 border-amber-200'
            : 'bg-emerald-50 text-emerald-700 border-emerald-200'
        )}
      >
        {isMissing ? 'Còn thiếu' : 'Đã phân công'}
      </span>
    </div>
  );
}

function WorkflowStep({
  stepNumber,
  title,
  description,
  status,
  action,
}: {
  stepNumber: number;
  title: string;
  description: string;
  status: 'completed' | 'current' | 'available' | 'locked';
  action?: React.ReactNode;
}) {
  return (
    <div className='relative flex items-start gap-4'>
      <span
        className={cn(
          'z-10 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ring-4 ring-white border shadow-sm',
          status === 'completed'
            ? 'bg-emerald-500 border-emerald-600 text-white'
            : status === 'current'
              ? 'bg-[#C81E3A] border-[#C81E3A] text-white animate-pulse'
              : status === 'available'
                ? 'bg-indigo-50 border-indigo-400 text-indigo-755'
                : 'bg-slate-105 border-slate-200 text-slate-400'
        )}
      >
        {status === 'completed' ? 'Đạt' : stepNumber}
      </span>

      <div className='min-w-0 flex-1 bg-slate-50/30 border border-slate-100/60 rounded-xl p-3.5 shadow-sm'>
        <p
          className={cn(
            'text-xs font-bold leading-none',
            status === 'completed'
              ? 'text-slate-800'
              : status === 'current'
                ? 'text-[#C81E3A]'
                : status === 'available'
                  ? 'text-indigo-950 font-bold'
                  : 'text-slate-400'
          )}
        >
          {title}
        </p>
        <p className='text-[10px] text-slate-500 mt-1.5 leading-relaxed'>
          {description}
        </p>
        {action}
      </div>
    </div>
  );
}
