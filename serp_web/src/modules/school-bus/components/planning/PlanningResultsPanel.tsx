'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import {
  ChevronUp,
  ChevronDown,
  Trash2,
  AlertTriangle,
  Info,
  Ban,
  MapPin,
  Users,
  Plus,
  Route,
  Clock,
  Calendar,
  AlertCircle,
  Loader2,
  Navigation,
  Sparkles,
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDate } from '../../utils';
import {
  SchoolBusConfirmDialog,
  useSchoolBusConfirm,
} from '../SchoolBusConfirmDialog';
import { schoolBusUi } from '../../theme';
import { ManualDemandAssignPanel } from './ManualDemandAssignPanel';
import { SchoolBusSelect } from '../ui/SchoolBusSelect';
import {
  useGetRouteByIdQuery,
  useReorderRouteStopsMutation,
  useRemoveRouteStopMutation,
  useRemoveRouteStudentMutation,
  useGetDepotsQuery,
  useGetBusesQuery,
  useGetSchoolByIdQuery,
  useDeleteRouteInSessionMutation,
  useGreedyFillRouteMutation,
} from '../../api/schoolBusApi';
import {
  busStatusLabel,
  directionLabel,
  usageTypeLabel,
} from '../../schoolBusLabels';
import type {
  SchoolBusPlanningPreview,
  SchoolBusPlanningSession,
  SchoolBusRoute,
  SchoolBusEligibleStudent,
  CreateRouteInSessionRequest,
} from '../../types';

/* -- Create Route Dialog ------------------------------------------------ */

type LocationType = 'SCHOOL' | 'DEPOT';
type DirectionType = 'OUTBOUND' | 'RETURN';

interface CreateRouteFormState {
  routeName: string;
  routeDirection: DirectionType;
  startLocationType: LocationType;
  startDepotId: number | '';
  endLocationType: LocationType;
  endDepotId: number | '';
  busId: number | '';
  planningNotes: string;
}

function CreateRoutePanel({
  session,
  onSubmit,
  submitting,
  open,
  onOpenChange,
}: {
  session: SchoolBusPlanningSession;
  onSubmit: (req: CreateRouteInSessionRequest) => Promise<boolean> | boolean;
  submitting?: boolean;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const initDir = (session.routeDirection as DirectionType) || 'OUTBOUND';
  const [form, setForm] = useState<CreateRouteFormState>({
    routeName: '',
    routeDirection: initDir,
    startLocationType: initDir === 'OUTBOUND' ? 'DEPOT' : 'SCHOOL',
    startDepotId: '',
    endLocationType: initDir === 'OUTBOUND' ? 'SCHOOL' : 'DEPOT',
    endDepotId: '',
    busId: '',
    planningNotes: '',
  });

  useEffect(() => {
    if (initDir === 'OUTBOUND') {
      setForm((p) => ({
        ...p,
        routeDirection: initDir,
        startLocationType: 'DEPOT',
        endLocationType: 'SCHOOL',
        endDepotId: '',
      }));
    } else {
      setForm((p) => ({
        ...p,
        routeDirection: initDir,
        startLocationType: 'SCHOOL',
        endLocationType: 'DEPOT',
        startDepotId: '',
      }));
    }
  }, [initDir]);

  // Fetch depots for selectors
  const { data: depotsData } = useGetDepotsQuery({ page: 0, size: 100 });
  const depots = depotsData?.data?.items || [];
  const hasDepots = depots.length > 0;
  const selectedDepotId =
    initDir === 'OUTBOUND' ? form.startDepotId : form.endDepotId;
  const { data: busesData, isFetching: loadingBuses } = useGetBusesQuery(
    selectedDepotId
      ? {
          page: 0,
          size: 100,
          depotId: Number(selectedDepotId),
          sortBy: 'plateNumber',
        }
      : undefined,
    { skip: !selectedDepotId }
  );
  const buses = busesData?.data?.items || [];
  const selectedBus = buses.find((b) => Number(b.id) === Number(form.busId));

  const needsStartDepot = form.startLocationType === 'DEPOT';
  const needsEndDepot = form.endLocationType === 'DEPOT';

  const canSubmit =
    form.routeName.trim() !== '' &&
    (!needsStartDepot || form.startDepotId !== '') &&
    (!needsEndDepot || form.endDepotId !== '') &&
    form.busId !== '';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canSubmit) return;
    const req: CreateRouteInSessionRequest = {
      schoolId: session.schoolId,
      routeDirection: form.routeDirection,
      startLocationType: form.startLocationType,
      endLocationType: form.endLocationType,
      routeName: form.routeName,
      serviceDate: session.serviceDate,
      busId: Number(form.busId),
      planningNotes: form.planningNotes || undefined,
    };
    if (form.startLocationType === 'SCHOOL')
      req.startSchoolId = session.schoolId;
    else req.startDepotId = Number(form.startDepotId);
    if (form.endLocationType === 'SCHOOL') req.endSchoolId = session.schoolId;
    else req.endDepotId = Number(form.endDepotId);
    const created = await onSubmit(req);
    if (created) {
      onOpenChange(false);
      setForm((p) => ({
        ...p,
        routeName: '',
        startDepotId: '',
        endDepotId: '',
        busId: '',
        planningNotes: '',
      }));
    }
  };

  const inputCls =
    'w-full rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs text-slate-900 shadow-sm focus:outline-none focus:ring-2 focus:ring-slate-900/10 focus:border-slate-900 transition-all';
  const labelCls =
    'block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1';

  return (
    <div className='mb-4'>
      <Button
        size='sm'
        variant='outline'
        onClick={() => onOpenChange(!open)}
        className='w-full rounded-full border-red-250 text-[#C81E3A] hover:bg-[#FDECEF]/50 gap-1.5 text-xs font-semibold'
      >
        <Plus className='h-3.5 w-3.5' />
        {open ? 'Đóng bộ tạo tuyến' : 'Mở bộ tạo tuyến'}
      </Button>
      {open && (
        <form
          onSubmit={handleSubmit}
          className='mt-3 rounded-2xl border border-slate-200 bg-slate-50/50 p-4 space-y-4 shadow-sm'
        >
          <div className='border-b border-slate-100 pb-1.5'>
            <h4 className='text-xs font-bold text-slate-700 uppercase tracking-wide'>
              Bộ tạo tuyến
            </h4>
          </div>

          {/* Group 1: Route identity */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
              1. Thông tin tuyến
            </p>
            <div>
              <label className={labelCls}>Tên tuyến *</label>
              <input
                className={inputCls}
                value={form.routeName}
                onChange={(e) =>
                  setForm((p) => ({ ...p, routeName: e.target.value }))
                }
                placeholder={
                  initDir === 'OUTBOUND'
                    ? 'VD: Tuyến sáng đến trường A'
                    : 'VD: Tuyến chiều về nhà A'
                }
                required
              />
            </div>
            <div>
              <label className={labelCls}>Chiều tuyến</label>
              <div className='w-full rounded-xl border border-slate-150 bg-slate-50/50 px-3 py-2 text-xs font-semibold text-slate-700 shadow-none'>
                {initDir === 'OUTBOUND' ? (
                  <span>
                    Đến trường - Bãi xe {'->'} Trường{' '}
                    <span className='text-[10px] font-medium text-slate-400 ml-1'>
                      (Lấy từ ngữ cảnh phiên)
                    </span>
                  </span>
                ) : (
                  <span>
                    Về nhà - Trường {'->'} Bãi xe{' '}
                    <span className='text-[10px] font-medium text-slate-400 ml-1'>
                      (Lấy từ ngữ cảnh phiên)
                    </span>
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Group 2: Start & End Terminals */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
              2. Start & End Terminals
            </p>

            {/* Start location */}
            <div className='space-y-2 pt-1.5'>
              <label className={labelCls}>
                Điểm bắt đầu ({initDir === 'OUTBOUND' ? 'DEPOT' : 'SCHOOL'})
              </label>
              {initDir === 'OUTBOUND' ? (
                hasDepots ? (
                  <div className='mt-1'>
                    <SchoolBusSelect
                      fullWidth
                      value={form.startDepotId}
                      onChange={(val) =>
                        setForm((p) => ({
                          ...p,
                          startDepotId: val === '' ? '' : Number(val),
                          busId: '',
                        }))
                      }
                      placeholder='- Chọn bãi xe -'
                      options={depots.map((d) => ({
                        label: d.name,
                        value: d.id,
                      }))}
                    />
                  </div>
                ) : (
                  <p className='text-[11px] text-amber-600 font-medium'>
                    Cảnh báo: chưa có bãi xe khả dụng. Vui lòng tạo bãi xe trước.
                  </p>
                )
              ) : (
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 font-medium'>
                  Starts at school:{' '}
                  <span className='font-semibold text-slate-700'>
                    {session.schoolName}
                  </span>{' '}
                  (ID: {session.schoolId})
                </p>
              )}
            </div>

            {/* End location */}
            <div className='space-y-2 pt-3 border-t border-slate-100'>
              <label className={labelCls}>
                Điểm kết thúc ({initDir === 'OUTBOUND' ? 'SCHOOL' : 'DEPOT'})
              </label>
              {initDir === 'RETURN' ? (
                hasDepots ? (
                  <div className='mt-1'>
                    <SchoolBusSelect
                      fullWidth
                      value={form.endDepotId}
                      onChange={(val) =>
                        setForm((p) => ({
                          ...p,
                          endDepotId: val === '' ? '' : Number(val),
                          busId: '',
                        }))
                      }
                      placeholder='- Chọn bãi xe -'
                      options={depots.map((d) => ({
                        label: d.name,
                        value: d.id,
                      }))}
                    />
                  </div>
                ) : (
                  <p className='text-[11px] text-amber-600 font-medium'>
                    Cảnh báo: chưa có bãi xe khả dụng. Vui lòng tạo bãi xe trước.
                  </p>
                )
              ) : (
                <p className='text-[11px] text-slate-500 bg-slate-50 p-2 rounded-lg border border-slate-100 font-medium'>
                  Kết thúc tại trường:{' '}
                  <span className='font-semibold text-slate-700'>
                    {session.schoolName}
                  </span>{' '}
                  (ID: {session.schoolId})
                </p>
              )}
            </div>
          </div>

          {/* Group 3: Bus */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
              3. Xe
            </p>
            <div>
              <label className={labelCls}>Xe *</label>
              <SchoolBusSelect
                fullWidth
                value={form.busId}
                onChange={(val) =>
                  setForm((p) => ({
                    ...p,
                    busId: val === '' ? '' : Number(val),
                  }))
                }
                disabled={
                  !selectedDepotId || loadingBuses || buses.length === 0
                }
                placeholder={
                  !selectedDepotId
                    ? 'Chọn bãi xe trước'
                    : loadingBuses
                      ? 'Đang tải xe...'
                      : buses.length === 0
                        ? 'Chưa có xe trong bãi này'
                        : 'Chọn xe'
                }
                options={buses.map((bus) => ({
                  label: `${bus.plateNumber} - ${bus.capacity} chỗ - ${bus.busType || 'Xe'} - ${busStatusLabel[bus.status || ''] || bus.status || 'Không rõ'}`,
                  value: bus.id,
                }))}
              />
            </div>
            {selectedBus ? (
              <div className='grid grid-cols-2 gap-2 rounded-xl border border-slate-100 bg-slate-50/70 p-2 text-[10px]'>
                <div className='col-span-2'>
                  <span className='block text-slate-400 font-bold uppercase'>
                    Xe
                  </span>
                  <span className='font-bold text-slate-800'>
                    {selectedBus.plateNumber}
                  </span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>
                    Sức chứa
                  </span>
                  <span className='font-bold text-slate-800'>
                    {selectedBus.capacity} học sinh
                  </span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>
                    Đã phân công
                  </span>
                  <span className='font-bold text-slate-800'>
                    0/{selectedBus.capacity}
                  </span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>
                    Còn trống
                  </span>
                  <span className='font-bold text-slate-800'>
                    {selectedBus.capacity}
                  </span>
                </div>
                <div>
                  <span className='block text-slate-400 font-bold uppercase'>
                    Trạng thái
                  </span>
                  <span className='font-bold text-slate-800'>
                    {busStatusLabel[selectedBus.status || ''] || selectedBus.status || '-'}
                  </span>
                </div>
              </div>
            ) : (
              <p className='text-[11px] text-slate-450 font-medium'>
                {!selectedDepotId
                  ? 'Chọn bãi xe trước khi chọn xe.'
                  : 'Chọn xe để xác định sức chứa tuyến.'}
              </p>
            )}
          </div>
          {/* Group 4: Notes & validation warnings */}
          <div className='space-y-3 p-3 bg-white rounded-xl border border-slate-100 shadow-sm'>
            <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider leading-none'>
              4. Thông tin bổ sung
            </p>
            <div>
              <label className={labelCls}>Ghi chú</label>
              <input
                className={inputCls}
                value={form.planningNotes}
                onChange={(e) =>
                  setForm((p) => ({ ...p, planningNotes: e.target.value }))
                }
                placeholder='Ghi chú không bắt buộc'
              />
            </div>
            {((needsStartDepot && form.startDepotId === '') ||
              (needsEndDepot && form.endDepotId === '')) && (
              <p className='text-[11px] text-amber-600 font-semibold'>
                Cảnh báo: cần chọn bãi xe để tạo tuyến.
              </p>
            )}
          </div>

          <div className='flex justify-end gap-2 pt-2'>
            <Button
              type='button'
              size='sm'
              variant='ghost'
              onClick={() => onOpenChange(false)}
              className='text-slate-500 hover:text-slate-900'
            >
              Hủy
            </Button>
            <Button
              type='submit'
              size='sm'
              disabled={submitting || !canSubmit}
              className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white'
            >
              {submitting ? 'Đang tạo...' : 'Tạo tuyến'}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}

/* -- Session Route Card ---------------------------------------------------- */

function SessionRouteCard({
  route,
  onSelect,
  isSelected,
  onDelete,
  deleting,
  onGreedyFill,
  greedyFilling,
  onAddStudents,
}: {
  route: SchoolBusRoute;
  onSelect: (id: number) => void;
  isSelected: boolean;
  onDelete?: () => void;
  deleting?: boolean;
  onGreedyFill?: () => void;
  greedyFilling?: boolean;
  onAddStudents?: (id: number) => void;
}) {
  const isAssigned = [
    'ASSIGNED',
    'TRIP_CREATED',
    'IN_PROGRESS',
    'COMPLETED',
  ].includes(route.status);
  const studentCount = route.plannedStudentCount || 0;
  const capacity = route.busCapacity || null;
  const depotName =
    route.startDepotName ||
    (route.startLocationType === 'DEPOT'
      ? route.startLocationName
      : route.endLocationName);

  // Deletable if not published/tripped AND has 0 students
  const isDeletable =
    route.status !== 'PUBLISHED' &&
    route.status !== 'TRIP_CREATED' &&
    studentCount === 0;
  const canGreedyFill =
    ['DRAFT', 'GENERATED', 'REVIEWING'].includes(route.status) &&
    Boolean(route.busId) &&
    capacity != null &&
    studentCount < capacity;
  const handleAddStudents = () => (onAddStudents || onSelect)(route.id);

  return (
    <div
      id={`route-card-${route.id}`}
      onClick={() => onSelect(route.id)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-355 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}
    >
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>
            {route.routeName}
          </p>
          <p className='text-[10px] font-bold text-slate-500 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>
              {route.routeCode}
            </span>
            <span>
              {directionLabel[route.routeDirection] || route.routeDirection}
            </span>
          </p>
        </div>
        <div className='flex flex-col items-end gap-1.5 shrink-0'>
          <span
            className={cn(
              'rounded-full px-2.5 py-0.5 text-[10px] font-semibold border shadow-none text-center',
              route.status === 'PUBLISHED'
                ? 'bg-emerald-50 text-emerald-700 border-emerald-255'
                : route.status === 'CANCELLED'
                  ? 'bg-slate-50 text-slate-500 border-slate-200'
                  : 'bg-amber-50 text-amber-700 border-amber-250'
            )}
          >
            {route.status === 'PUBLISHED'
              ? 'Đã phát hành'
              : route.status === 'CANCELLED'
                ? 'Đã hủy'
                : 'Nháp'}
          </span>
          <span
            className={cn(
              'rounded-full px-2 py-0.5 text-[9px] font-bold border shadow-none text-center',
              isAssigned
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'
            )}
          >
            {isAssigned ? 'Sẵn sàng tạo chuyến' : 'Cần phân công'}
          </span>
        </div>
      </div>

      <div className='grid grid-cols-2 gap-2 text-[11px] text-slate-650 bg-slate-50/60 p-2.5 rounded-xl border border-slate-100/60'>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Xe:</span>
          <span className='font-bold text-slate-700 truncate'>
            {route.busPlateNumber || 'Chưa chọn'}
          </span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Học sinh:</span>
          <span className='font-bold text-slate-700'>
            {capacity != null ? `${studentCount}/${capacity}` : studentCount}
          </span>
        </div>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Bãi xe:</span>
          <span className='font-bold text-slate-700 truncate'>
            {depotName || 'N/A'}
          </span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Điểm dừng:</span>
          <span className='font-bold text-slate-700'>
            {route.stopsCount || 0}
          </span>
        </div>
        <div className='col-span-2 flex items-center gap-1.5 min-w-0 border-t border-slate-200/40 pt-1.5 mt-0.5'>
          <span className='text-slate-400 font-medium'>Staff:</span>
          <span className='font-semibold text-slate-700 truncate'>
            {route.driverName && route.attendantName ? (
              `Driver: ${route.driverName} - Attendant: ${route.attendantName}`
            ) : route.driverName ? (
              `Driver: ${route.driverName} (Chưa có phụ xe)`
            ) : route.attendantName ? (
              `Attendant: ${route.attendantName} (Chưa có tài xế)`
            ) : (
              <span className='text-slate-400 italic font-medium'>
                Not assigned
              </span>
            )}
          </span>
        </div>
      </div>

      <div className='flex items-center justify-between border-t border-slate-100/50 pt-2 text-[10px] text-slate-450'>
        <span>{route.serviceDate}</span>
        {isSelected && (
          <span className='px-2 py-0.5 bg-red-50 text-[#C81E3A] border border-red-100 text-[9px] font-extrabold rounded-full flex items-center gap-1 shadow-sm'>
            Currently Selected
          </span>
        )}
      </div>

      {isSelected && (
        <div
          className='flex flex-wrap gap-2 border-t border-slate-100/50 pt-2.5 mt-1'
          onClick={(e) => e.stopPropagation()}
        >
          {canGreedyFill && onGreedyFill && (
            <Button
              size='sm'
              variant='outline'
              disabled={greedyFilling}
              className='rounded-full h-8 text-xs font-semibold border-indigo-200 text-indigo-700 bg-indigo-50/40 hover:bg-indigo-50 gap-1.5'
              onClick={onGreedyFill}
            >
              {greedyFilling ? (
                <Loader2 className='h-3.5 w-3.5 animate-spin' />
              ) : (
                <Sparkles className='h-3.5 w-3.5' />
              )}
              {greedyFilling ? 'Đang chạy...' : 'Gán tự động'}
            </Button>
          )}
          {(() => {
            const hasNoOrUnderCapacityStudents =
              capacity === null || capacity === 0 || studentCount < capacity;
            const hasStudentsButNoStaff =
              studentCount > 0 && !route.driverName && !route.attendantName;
            const hasStaff = !!route.driverName || !!route.attendantName;

            if (hasStaff) {
              return (
                <>
                  <Button
                    size='sm'
                    className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}`}>
                      Xem chi tiết
                    </Link>
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    onClick={handleAddStudents}
                  >
                    Thêm học sinh
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}?assign=true`}>
                      Sửa nhân sự
                    </Link>
                  </Button>
                </>
              );
            }

            if (hasStudentsButNoStaff) {
              return (
                <>
                  <Button
                    size='sm'
                    className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}?assign=true`}>
                      Phân công nhân sự
                    </Link>
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    onClick={handleAddStudents}
                  >
                    Thêm học sinh
                  </Button>
                  <Button
                    size='sm'
                    variant='outline'
                    className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                    asChild
                  >
                    <Link href={`/school-bus/dispatch/${route.id}`}>
                      Xem chi tiết
                    </Link>
                  </Button>
                </>
              );
            }

            // Case: hasNoOrUnderCapacityStudents is true (or fallback default)
            return (
              <>
                <Button
                  size='sm'
                  className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
                  onClick={handleAddStudents}
                >
                  Thêm học sinh
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
                  asChild
                >
                  <Link href={`/school-bus/dispatch/${route.id}`}>
                    Xem chi tiết
                  </Link>
                </Button>
              </>
            );
          })()}

          {isDeletable && onDelete && (
            <Button
              size='sm'
              variant='ghost'
              disabled={deleting}
              className='rounded-full h-8 text-xs font-semibold text-red-600 hover:text-red-700 hover:bg-red-50/50 gap-1 ml-auto'
              onClick={onDelete}
            >
              {deleting ? (
                <Loader2 className='h-3 w-3 animate-spin' />
              ) : (
                <Trash2 className='h-3.5 w-3.5' />
              )}
              Xóa
            </Button>
          )}
        </div>
      )}
    </div>
  );
}

/* -- Route Card ----------------------------------------------------------- */

function RouteCard({
  route,
  onSelect,
  isSelected,
}: {
  route: any;
  onSelect: (id: number) => void;
  isSelected: boolean;
}) {
  const isAssigned = [
    'ASSIGNED',
    'TRIP_CREATED',
    'IN_PROGRESS',
    'COMPLETED',
  ].includes(route.status);

  return (
    <div
      id={`route-card-${route.routeId}`}
      onClick={() => onSelect(route.routeId)}
      className={cn(
        'cursor-pointer rounded-2xl border p-4 transition-all duration-200 flex flex-col gap-2.5',
        isSelected
          ? 'border-l-4 border-l-[#C81E3A] border-y-slate-350 border-r-slate-355 bg-slate-50/50 shadow-sm'
          : schoolBusUi.interactiveCard
      )}
    >
      <div className='flex items-start justify-between gap-3'>
        <div className='min-w-0'>
          <p className='text-xs font-bold text-slate-900 truncate'>
            {route.routeName}
          </p>
          <p className='text-[10px] font-bold text-slate-555 mt-0.5 flex items-center gap-1.5'>
            <span className='px-1.5 py-0.5 bg-indigo-50 border border-indigo-100 rounded text-indigo-700 text-[9px]'>
              {route.routeCode}
            </span>
          </p>
        </div>
        <div className='flex items-center gap-2'>
          <div className='flex flex-col items-end gap-1 shrink-0'>
            <span
              className={cn(
                'rounded-full px-2 py-0.5 text-[9px] font-bold border shadow-none text-center',
                isAssigned
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                  : 'bg-amber-50 text-amber-700 border-amber-200'
              )}
            >
              {isAssigned ? 'Sẵn sàng tạo chuyến' : 'Cần phân công'}
            </span>
          </div>
        </div>
      </div>

      <div className='grid grid-cols-2 gap-2 text-[11px] text-slate-650 bg-slate-50/60 p-2.5 rounded-xl border border-slate-100/60'>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Sức chứa:</span>
          <span className='font-bold text-slate-700'>
            {route.requiredCapacity || '-'}
          </span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Học sinh:</span>
          <span className='font-bold text-slate-700'>{route.studentCount}</span>
        </div>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Khoảng cách:</span>
          <span className='font-bold text-slate-700'>
            {route.totalDistanceKm != null
              ? route.totalDistanceKm.toFixed(1) + ' km'
              : '-'}
          </span>
        </div>
        <div className='flex items-center gap-1.5 justify-end'>
          <span className='text-slate-400 font-medium'>Thời lượng:</span>
          <span className='font-bold text-slate-700'>
            {route.totalDurationMin != null
              ? route.totalDurationMin + ' min'
              : '-'}
          </span>
        </div>
        <div className='flex items-center gap-1.5 min-w-0'>
          <span className='text-slate-400 font-medium'>Điểm dừng:</span>
          <span className='font-bold text-slate-700'>
            {route.stopCount || 0}
          </span>
        </div>
      </div>

      <div className='flex items-center justify-between border-t border-slate-100/50 pt-2 text-[10px] text-slate-450'>
        <span>Bản nháp xem trước</span>
        {isSelected && (
          <span className='px-2 py-0.5 bg-red-50 text-[#C81E3A] border border-red-100 text-[9px] font-extrabold rounded-full flex items-center gap-1 shadow-sm'>
            Đang chọn
          </span>
        )}
      </div>

      {isSelected && (
        <div
          className='flex flex-wrap gap-2 border-t border-slate-100/50 pt-2.5 mt-1'
          onClick={(e) => e.stopPropagation()}
        >
          <Button
            size='sm'
            className='rounded-full bg-[#C81E3A] hover:bg-[#B31B34] text-white shadow-sm h-8 text-xs font-semibold px-4'
            onClick={() => onSelect(route.routeId)}
          >
            Thêm học sinh
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.routeId}`}>
              Xem chi tiết
            </Link>
          </Button>
          <Button
            size='sm'
            variant='outline'
            className='rounded-full h-8 text-xs font-semibold border-slate-200 text-slate-700 bg-white hover:bg-slate-50 shadow-sm'
            asChild
          >
            <Link href={`/school-bus/dispatch/${route.routeId}?assign=true`}>
              Phân công nhân sự
            </Link>
          </Button>
        </div>
      )}
    </div>
  );
}

/* -- Route Detail Panel --------------------------------------------------- */

function RouteDetailPanel({
  routeId,
  sessionId,
}: {
  routeId: number;
  sessionId: number;
}) {
  const { data: routeDetail, isLoading } = useGetRouteByIdQuery(routeId, {
    refetchOnMountOrArgChange: true,
  });
  const [reorderStops, { isLoading: reorderingStops }] =
    useReorderRouteStopsMutation();
  const [removeStop, { isLoading: removingStop }] =
    useRemoveRouteStopMutation();
  const [removeStudent, { isLoading: removingStudent }] =
    useRemoveRouteStudentMutation();
  const detail = routeDetail?.data;

  // -- Confirm dialogs --------------------------------------------------
  const stopConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const stopId = stopConfirm.confirmState.payload as number;
      try {
        await removeStop({ routeId, stopId, sessionId }).unwrap();
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message || 'Không thể xóa điểm dừng');
      }
    },
    isLoading: removingStop,
  });

  const studentConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const { studentId, subscriptionId } = studentConfirm.confirmState
        .payload as {
        studentId: number;
        subscriptionId: number;
      };
      try {
        await removeStudent({
          routeId,
          studentId,
          subscriptionId,
          sessionId,
        }).unwrap();
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message || 'Không thể xóa học sinh');
      }
    },
    isLoading: removingStudent,
  });

  if (isLoading || !detail)
    return (
      <div className='py-8 text-center text-sm text-slate-400'>
        Đang tải chi tiết tuyến...
      </div>
    );

  const stops = detail.stops || [];
  const students = detail.students || [];
  const editable = ![
    'COMPLETED',
    'IN_PROGRESS',
    'TRIP_CREATED',
    'CANCELLED',
  ].includes(detail.route.status);

  const swap = async (i: number, j: number) => {
    // Only swap middle stops - find their indices in the full stops array
    const middleStops = stops.filter(
      (s) =>
        s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL'
    );
    const ids = middleStops.map((s) => s.id);
    [ids[i], ids[j]] = [ids[j], ids[i]];
    try {
      await reorderStops({
        id: routeId,
        orderedStopIds: ids,
        sessionId,
      }).unwrap();
    } catch (e: unknown) {
      const err = e as { data?: { message?: string } };
      toast.error(err?.data?.message || 'Không thể sắp xếp lại điểm dừng');
    }
  };

  const delStop = (stopId: number) => {
    stopConfirm.requestConfirm({
      title: 'Xóa điểm dừng?',
      description:
        'Điểm dừng này và tất cả học sinh được gán vào đó sẽ bị xóa khỏi tuyến.',
      confirmLabel: 'Xóa điểm dừng',
      variant: 'danger',
      payload: stopId,
    });
  };

  const delStudent = (studentId: number, subscriptionId: number) => {
    studentConfirm.requestConfirm({
      title: 'Xóa học sinh?',
      description:
        'Học sinh này sẽ được gỡ khỏi tuyến và quay lại danh sách chưa gán.',
      confirmLabel: 'Xóa',
      variant: 'danger',
      payload: { studentId, subscriptionId },
    });
  };

  const middleStops = stops.filter(
    (s) =>
      s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL'
  );

  const anyLoading = reorderingStops || removingStop || removingStudent;

  return (
    <>
      <div className='p-5 space-y-4 border-t border-slate-200 bg-slate-50/20'>
        <div className='border-b border-slate-100 pb-2'>
          <h3 className='text-sm font-bold text-slate-955 flex items-center gap-2'>
            <Route className='h-4 w-4 text-emerald-600 shrink-0' />
            {detail.route.routeName} - Detail
          </h3>
        </div>

        <div>
          {/* Terminal route banner */}
          {(() => {
            const startTerminal = stops.find(
              (s) => s.stopPurpose === 'START_TERMINAL'
            );
            const endTerminal = stops.find(
              (s) => s.stopPurpose === 'END_TERMINAL'
            );
            if (!startTerminal && !endTerminal) return null;
            return (
              <div className='mb-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-3 text-xs text-slate-600'>
                <div className='flex items-center gap-2 font-bold text-slate-700 mb-2'>
                  <Navigation className='h-3.5 w-3.5 text-blue-600' />
                  <span className='text-[10px] font-extrabold uppercase tracking-wider text-slate-400'>
                    Terminal Stops
                  </span>
                </div>
                <div className='flex items-center gap-2 pl-5 text-[11px] font-semibold text-slate-700'>
                  <span className='bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded border border-blue-100'>
                    {startTerminal?.depotName ||
                      startTerminal?.schoolName ||
                      'Start Depot/School'}
                  </span>
                  <span className='text-slate-400'>{'->'}</span>
                  <span className='bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded border border-blue-100'>
                    {endTerminal?.depotName ||
                      endTerminal?.schoolName ||
                      'End Depot/School'}
                  </span>
                </div>
              </div>
            );
          })()}

          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2'>
            {detail.route.routeDirection === 'OUTBOUND'
              ? 'Điểm đón'
              : 'Điểm trả'}{' '}
            ({middleStops.length})
          </p>
          {middleStops.length === 0 ? (
            <p className='text-xs text-slate-400 italic bg-white border border-dashed border-slate-200 rounded-2xl p-4 text-center'>
              {detail.route.routeDirection === 'OUTBOUND'
                ? 'Chưa tạo điểm đón. Hãy gán học sinh để tự động tạo điểm dừng.'
                : 'Chưa tạo điểm trả. Hãy gán học sinh để tự động tạo điểm dừng.'}
            </p>
          ) : (
            <div className='space-y-1.5'>
              {middleStops.map((stop, middleIndex) => {
                const isFirstMiddle = middleIndex === 0;
                const isLastMiddle = middleIndex === middleStops.length - 1;
                return (
                  <div
                    key={stop.id}
                    className='flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs text-slate-700'
                  >
                    <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-slate-100 text-[10px] font-bold text-slate-650'>
                      {middleIndex + 1}
                    </span>
                    <span className='flex-1 font-semibold truncate'>
                      {stop.pickupPointName || `Stop #${stop.id}`}
                    </span>
                    <span className='text-[10px] text-slate-450'>
                      {stop.estimatedStudentCount || 0} students
                    </span>
                    {editable && (
                      <div
                        className='flex gap-0.5'
                        onClick={(e) => e.stopPropagation()}
                      >
                        <Button
                          variant='ghost'
                          size='icon'
                          className='h-6 w-6'
                          onClick={() => swap(middleIndex, middleIndex - 1)}
                          disabled={isFirstMiddle || anyLoading}
                        >
                          <ChevronUp className='h-3 w-3' />
                        </Button>
                        <Button
                          variant='ghost'
                          size='icon'
                          className='h-6 w-6'
                          onClick={() => swap(middleIndex, middleIndex + 1)}
                          disabled={isLastMiddle || anyLoading}
                        >
                          <ChevronDown className='h-3 w-3' />
                        </Button>
                        <Button
                          variant='ghost'
                          size='icon'
                          className='h-6 w-6 text-red-500 hover:text-red-700'
                          onClick={() => delStop(stop.id)}
                          disabled={anyLoading}
                        >
                          <Trash2 className='h-3 w-3' />
                        </Button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div>
          {(() => {
            return (
              <>
                <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2 mt-4'>
                  Học sinh ({students.length})
                </p>
                {students.length === 0 ? (
                  <p className='text-xs text-slate-300'>
                    Chưa gán học sinh vào điểm dừng
                  </p>
                ) : (
                  <div className='space-y-1.5'>
                    {students.map((ps) => (
                      <div
                        key={`${ps.studentId}-${ps.subscriptionId}`}
                        className='flex flex-col gap-1.5 rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-xs text-slate-700 shadow-sm hover:shadow transition-shadow'
                      >
                        <div className='flex items-center gap-2'>
                          <span className='flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-indigo-50 text-indigo-650'>
                            <Users className='h-3 w-3' />
                          </span>
                          <span className='flex-1 font-bold text-slate-800 truncate'>
                            {ps.studentName || `Student #${ps.studentId}`}
                          </span>
                          {editable && (
                            <Button
                              variant='ghost'
                              size='icon'
                              className='h-6 w-6 text-red-500 hover:text-red-700'
                              onClick={() =>
                                delStudent(ps.studentId, ps.subscriptionId || 0)
                              }
                              disabled={anyLoading}
                            >
                              <Trash2 className='h-3 w-3' />
                            </Button>
                          )}
                        </div>
                        <div className='pl-7 text-[10px] text-slate-450 space-y-1.5 border-t border-slate-100/60 pt-2 mt-1'>
                          {ps.pickupPointName && (
                            <div className='flex items-center gap-1.5'>
                              <span className='px-1 py-0.2 rounded-[4px] font-extrabold text-[8px] uppercase tracking-wider bg-emerald-50 text-emerald-700 border border-emerald-100'>
                                PICKUP
                              </span>
                              <span className='text-slate-600 font-semibold truncate'>
                                {ps.pickupPointName}
                              </span>
                            </div>
                          )}
                          {ps.dropoffPointName && (
                            <div className='flex items-center gap-1.5'>
                              <span className='px-1 py-0.2 rounded-[4px] font-extrabold text-[8px] uppercase tracking-wider bg-blue-50 text-blue-700 border border-blue-100'>
                                DROPOFF
                              </span>
                              <span className='text-slate-600 font-semibold truncate'>
                                {ps.dropoffPointName}
                              </span>
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            );
          })()}
        </div>

        {/* Staff Section */}
        <div>
          <p className='text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2 mt-4'>
            Staff
          </p>
          <div className='rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-xs text-slate-700 shadow-sm'>
            {detail.route.driverName || detail.route.attendantName ? (
              <div className='space-y-2'>
                {detail.route.driverName && (
                  <div className='flex items-center justify-between'>
                    <div className='flex items-center gap-2'>
                      <span className='w-16 text-slate-400 font-semibold'>
                        Driver:
                      </span>
                      <span className='font-bold text-slate-800'>
                        {detail.route.driverName}
                      </span>
                    </div>
                  </div>
                )}
                {detail.route.attendantName && (
                  <div className='flex items-center justify-between border-t border-slate-100/65 pt-2 mt-2'>
                    <div className='flex items-center gap-2'>
                      <span className='w-16 text-slate-400 font-semibold'>
                        Attendant:
                      </span>
                      <span className='font-bold text-slate-800'>
                        {detail.route.attendantName}
                      </span>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <p className='text-xs text-slate-450 italic py-1'>
                Tuyến này chưa được phân công nhân sự.
              </p>
            )}
          </div>
        </div>

        <hr className='border-slate-150 mt-4' />
      </div>

      {/* Confirm dialogs */}
      <SchoolBusConfirmDialog {...stopConfirm.dialogProps} />
      <SchoolBusConfirmDialog {...studentConfirm.dialogProps} />
    </>
  );
}

/* -- Main Results Panel --------------------------------------------------- */

interface PlanningResultsPanelProps {
  preview: SchoolBusPlanningPreview | null;
  /** Routes loaded from backend and preserved across refreshes. */
  sessionRoutes: SchoolBusRoute[];
  activeSession: SchoolBusPlanningSession | null;
  eligibleStudents: SchoolBusEligibleStudent[];
  loadingEligible?: boolean;
  selectedRouteId: number | null;
  onSelectRoute: (id: number | null) => void;
  onCreateManualRoute: (req: CreateRouteInSessionRequest) => Promise<boolean> | boolean;
  creatingRoute?: boolean;
  refreshingRoutes?: boolean;
  form?: any;
  rightPanelTab: 'demand-preview' | 'route-builder';
  onTabChange: (tab: 'demand-preview' | 'route-builder') => void;
  onPreviewDemandClick?: () => void;
  previewing?: boolean;
  onOpenSession?: (id: number) => void;
}

export function PlanningResultsPanel({
  preview,
  sessionRoutes,
  activeSession,
  eligibleStudents,
  loadingEligible,
  selectedRouteId,
  onSelectRoute,
  onCreateManualRoute,
  creatingRoute,
  refreshingRoutes,
  form,
  rightPanelTab,
  onTabChange,
  onPreviewDemandClick,
  previewing,
  onOpenSession,
}: PlanningResultsPanelProps) {
  const [previewTab, setPreviewTab] = useState<
    'demands' | 'points' | 'context'
  >('demands');
  const [pendingAssignScrollRouteId, setPendingAssignScrollRouteId] = useState<
    number | null
  >(null);
  const [createRouteBuilderOpen, setCreateRouteBuilderOpen] = useState(false);
  const manualAssignPanelRef = React.useRef<HTMLDivElement>(null);

  const [deleteRoute, { isLoading: deletingRoute }] =
    useDeleteRouteInSessionMutation();
  const [greedyFillRoute, { isLoading: greedyFilling }] =
    useGreedyFillRouteMutation();

  const handleAddStudents = (routeId: number) => {
    setPendingAssignScrollRouteId(routeId);
    if (selectedRouteId !== routeId) {
      onSelectRoute(routeId);
    }
  };

  useEffect(() => {
    if (
      rightPanelTab !== 'route-builder' ||
      pendingAssignScrollRouteId === null ||
      selectedRouteId !== pendingAssignScrollRouteId
    ) {
      return;
    }

    const animationFrame = window.requestAnimationFrame(() => {
      manualAssignPanelRef.current?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
      setPendingAssignScrollRouteId(null);
    });

    return () => window.cancelAnimationFrame(animationFrame);
  }, [pendingAssignScrollRouteId, rightPanelTab, selectedRouteId]);

  const deleteConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const routeId = deleteConfirm.confirmState.payload as number;
      if (!activeSession) return;
      try {
        await deleteRoute({ sessionId: activeSession.id, routeId }).unwrap();
        toast.success('Đã xóa tuyến thành công');
        if (selectedRouteId === routeId) {
          onSelectRoute(null);
        }
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message || 'Không thể xóa tuyến');
      }
    },
    isLoading: deletingRoute,
  });

  const greedyConfirm = useSchoolBusConfirm({
    onConfirm: async () => {
      const routeId = greedyConfirm.confirmState.payload as number;
      if (!activeSession) return;
      try {
        const response = await greedyFillRoute({
          sessionId: activeSession.id,
          routeId,
          body: { preserveExistingAssignments: true },
        }).unwrap();
        toast.success(
          `Đã thêm ${response.data.addedStudents} học sinh qua ${response.data.addedStops} điểm dừng. Sức chứa còn lại: ${response.data.remainingCapacity}.`
        );
      } catch (e: unknown) {
        const err = e as { data?: { message?: string } };
        toast.error(err?.data?.message || 'Gán tự động thất bại');
        throw e;
      }
    },
    isLoading: greedyFilling,
  });

  const schoolId = Number(preview?.schoolId || form?.schoolId) || 0;
  const { data: schoolData } = useGetSchoolByIdQuery(schoolId, {
    skip: !schoolId,
  });
  const currentSchool = schoolData?.data || null;

  const scheduleId = 0; // schedule removed (Phase 3)
  // scheduleData query removed (Phase 3)

  const isPreviewStale = preview
    ? Number(preview.schoolId) !== Number(form?.schoolId) ||
      preview.serviceDate !== form?.serviceDate ||
      preview.routeDirection !== form?.routeDirection
    : false;

  return (
    <div className='flex h-full flex-col bg-white overflow-hidden'>
      {/* Premium Tab Selector */}
      <div className='shrink-0 flex border-b border-slate-200 bg-slate-50/50 p-1.5 gap-1.5'>
        <button
          onClick={() => onTabChange('demand-preview')}
          className={cn(
            'flex-1 flex flex-col items-center justify-center py-2 px-3 rounded-xl border transition-all text-center',
            rightPanelTab === 'demand-preview'
              ? 'bg-white border-slate-200 shadow-sm text-[#C81E3A] font-bold'
              : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-100/50 hover:text-slate-700'
          )}
        >
          <span className='text-xs font-bold'>Nhu cầu học sinh</span>
          <span className='text-[10px] text-slate-400 mt-0.5 font-medium'>
            {preview && !isPreviewStale
              ? `Tổng ${preview.summary?.totalSubscriptions || preview.totalEligibleStudents} || Đủ điều kiện ${preview.summary?.eligibleStudents || preview.totalEligibleStudents} || Điểm ${preview.summary?.pointCount || preview.totalEligiblePickupPoints}`
              : isPreviewStale
                ? 'Đã cũ'
                : 'Chưa có xem trước'}
          </span>
        </button>
        <button
          onClick={() => onTabChange('route-builder')}
          className={cn(
            'flex-1 flex flex-col items-center justify-center py-2 px-3 rounded-xl border transition-all text-center',
            rightPanelTab === 'route-builder'
              ? 'bg-white border-slate-200 shadow-sm text-[#C81E3A] font-bold'
              : 'bg-transparent border-transparent text-slate-500 hover:bg-slate-100/50 hover:text-slate-700'
          )}
        >
          <span className='text-xs font-bold'>Bộ tạo tuyến</span>
          <span className='text-[10px] text-slate-400 mt-0.5 font-medium'>
            {activeSession ? `Routes (${sessionRoutes.length})` : 'Chưa có phiên'}
          </span>
        </button>
      </div>

      <div className='flex-1 overflow-y-auto divide-y divide-slate-150'>
        {/* Tab 1: Demand Preview */}
        {rightPanelTab === 'demand-preview' && (
          <>
            {/* Case A: Stale Preview */}
            {isPreviewStale && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-amber-50 border border-amber-100 text-amber-500 mb-3 shadow-sm'>
                  <AlertTriangle className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>
                  Bản xem trước đã cũ
                </p>
                <p className='mt-1 text-xs text-slate-455 max-w-[240px] leading-relaxed'>
                  Ngữ cảnh lập tuyến đã thay đổi. Bấm Xem trước để tải lại.
                </p>
                {onPreviewDemandClick && (
                  <Button
                    size='sm'
                    onClick={onPreviewDemandClick}
                    disabled={previewing}
                    className='mt-4 rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm'
                  >
                    {previewing ? 'Đang xem trước...' : 'Xem trước nhu cầu'}
                  </Button>
                )}
              </div>
            )}

            {/* Case B: No Preview at all */}
            {!preview && !isPreviewStale && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 mb-3 shadow-sm'>
                  <Users className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>
                  {activeSession
                    ? 'Xem trước nhu cầu của phiên này'
                    : 'Chưa có xem trước nhu cầu'}
                </p>
                <p className='mt-1 text-xs text-slate-455 max-w-[240px] leading-relaxed'>
                  {activeSession
                    ? 'Xem trước nhu cầu của phiên này để kiểm tra học sinh đủ điều kiện và bị chặn.'
                    : 'Hoàn tất ngữ cảnh lập tuyến rồi bấm Xem trước để kiểm tra học sinh đủ điều kiện.'}
                </p>
                {onPreviewDemandClick && (
                  <Button
                    size='sm'
                    onClick={onPreviewDemandClick}
                    disabled={previewing}
                    className='mt-4 rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm'
                  >
                    {previewing ? 'Đang xem trước...' : 'Xem trước nhu cầu'}
                  </Button>
                )}
              </div>
            )}

            {/* Case C: Active Preview Data */}
            {preview &&
              !isPreviewStale &&
              (() => {
                const summary = preview?.summary;
                const eligibleDemands = preview?.eligibleDemands || [];
                const points = preview?.points || [];

                if (preview.existingSessionId) {
                  return (
                    <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px] space-y-4'>
                      <div className='flex h-12 w-12 items-center justify-center rounded-full bg-amber-50 border border-amber-100 text-amber-500 shadow-sm'>
                        <AlertTriangle className='h-6 w-6' />
                      </div>
                      <p className='text-sm font-bold text-slate-800 leading-snug'>
                        Phiên lập kế hoạch cho trường học, ngày và chiều này đã tồn tại.
                        Vui lòng mở phiên đã có để tiếp tục.

                      </p>
                      {onOpenSession && (
                        <Button
                          size='sm'
                          onClick={() =>
                            onOpenSession(preview.existingSessionId!)
                          }
                          className='rounded-full bg-[#C81E3A] text-white hover:bg-[#A91931] px-5 text-xs font-semibold shadow-sm gap-1.5'
                        >
                          Mở phiên lập kế hoạch hiện tại
                        </Button>
                      )}
                    </div>
                  );
                }

                if (eligibleDemands.length === 0) {
                  return (
                    <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px] space-y-3'>
                      <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 shadow-sm'>
                        <Ban className='h-6 w-6' />
                      </div>
                      <p className='text-sm font-bold text-slate-700'>
                        Không có học sinh đủ điều kiện cho ngày và chiều này.
                      </p>
                      <p className='text-xs text-slate-450 max-w-[240px] leading-relaxed'>
                        Make sure students have active subscriptions for this
                        school and route direction.
                      </p>
                    </div>
                  );
                }

                // Derived variables for display
                const totalSubs =
                  summary?.totalSubscriptions || preview.totalEligibleStudents;
                const eligibleCount =
                  summary?.eligibleStudents || preview.totalEligibleStudents;
                const pointsCount =
                  summary?.pointCount || preview.totalEligiblePickupPoints;

                const currentDirection =
                  preview?.routeDirection || form?.routeDirection;

                return (
                  <div className='p-5 space-y-4 bg-white rounded-2xl'>
                    <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
                      <h3 className='text-sm font-bold text-slate-950 flex items-center gap-2'>
                        <Users className='h-4 w-4 text-[#C81E3A] shrink-0' />
                        Xem trước nhu cầu lập tuyến
                      </h3>
                      <span className='px-2 py-0.5 bg-red-50 border border-red-100 text-[#C81E3A] text-[10px] font-bold rounded-full'>
                        {currentDirection === 'RETURN' ? 'Về nhà' : 'Đến trường'}
                      </span>
                    </div>

                    {/* KPI Summary Cards */}
                    <div className='grid grid-cols-3 gap-2 text-xs'>
                      <div className='p-2.5 rounded-2xl bg-slate-50 border border-slate-150 shadow-sm flex flex-col justify-between'>
                        <span className='text-[9px] font-extrabold text-slate-450 uppercase tracking-wider leading-none'>
                          Tổng
                        </span>
                        <span className='text-sm font-black text-slate-850 mt-1.5'>
                          {totalSubs}
                        </span>
                      </div>
                      <div className='p-2.5 rounded-2xl bg-emerald-50/50 border border-emerald-100 shadow-sm flex flex-col justify-between'>
                        <span className='text-[9px] font-extrabold text-emerald-650 uppercase tracking-wider leading-none'>
                          Đủ điều kiện
                        </span>
                        <span className='text-sm font-black text-emerald-700 mt-1.5'>
                          {eligibleCount}
                        </span>
                      </div>
                      <div className='p-2.5 rounded-2xl bg-blue-50/50 border border-blue-100 shadow-sm flex flex-col justify-between'>
                        <span className='text-[9px] font-extrabold text-blue-650 uppercase tracking-wider leading-none'>
                          Điểm
                        </span>
                        <span className='text-sm font-black text-blue-700 mt-1.5'>
                          {pointsCount}
                        </span>
                      </div>
                    </div>

                    {/* Tab Navigation */}
                    <div className='flex border-b border-slate-150 text-xs font-bold'>
                      <button
                        onClick={() => setPreviewTab('demands')}
                        className={cn(
                          'px-3 py-2 border-b-2 transition-all -mb-px',
                          previewTab === 'demands'
                            ? 'border-[#C81E3A] text-slate-900'
                            : 'border-transparent text-slate-400 hover:text-slate-600'
                        )}
                      >
                        Học sinh ({preview ? eligibleDemands.length : 0})
                      </button>
                      <button
                        onClick={() => setPreviewTab('points')}
                        className={cn(
                          'px-3 py-2 border-b-2 transition-all -mb-px',
                          previewTab === 'points'
                            ? 'border-[#C81E3A] text-slate-900'
                            : 'border-transparent text-slate-400 hover:text-slate-600'
                        )}
                      >
                        Điểm ({preview ? points.length : 0})
                      </button>
                      <button
                        onClick={() => setPreviewTab('context')}
                        className={cn(
                          'px-3 py-2 border-b-2 transition-all -mb-px',
                          previewTab === 'context'
                            ? 'border-[#C81E3A] text-slate-900'
                            : 'border-transparent text-slate-400 hover:text-slate-600'
                        )}
                      >
                        Ngữ cảnh
                      </button>
                    </div>

                    {/* Tab: Students List */}
                    {previewTab === 'demands' &&
                      (() => {
                        if (!preview) {
                          return (
                            <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                              <Users className='h-8 w-8 text-slate-350 mb-2' />
                              <p className='text-xs font-bold text-slate-600'>
                                Chưa có dữ liệu xem trước
                              </p>
                              <p className='text-[10px] text-slate-455 mt-1 max-w-[200px]'>
                                Điền ngữ cảnh bên trái và bấm "Xem trước" để
                                tải danh sách học sinh.
                              </p>
                            </div>
                          );
                        }
                        return (
                          <div className='space-y-3 pt-1'>
                            {eligibleDemands.length === 0 ? (
                              <div className='text-center py-8 text-slate-400 text-xs'>
                                Không tìm thấy học sinh.
                              </div>
                            ) : (
                              <div className='space-y-2 max-h-[380px] overflow-y-auto pr-1'>
                                {eligibleDemands.map((d) => (
                                  <div
                                    key={d.subscriptionId}
                                    className='rounded-xl border border-slate-150 p-3 flex flex-col gap-1.5 shadow-sm bg-white'
                                  >
                                    <div className='min-w-0'>
                                      <p className='text-xs font-bold text-slate-800 truncate'>
                                        {d.studentName}
                                      </p>
                                      <p className='text-[10px] text-slate-400 mt-0.5 flex items-center gap-1.5'>
                                        <span className='font-bold'>
                                          {d.studentCode}
                                        </span>
                                        <span>-</span>
                                        <span>
                                          {usageTypeLabel[d.tripOption] || d.tripOption}
                                        </span>
                                      </p>
                                    </div>

                                    {d.pointName && (
                                      <div className='flex items-center gap-1.5 text-[10px] text-slate-500 bg-slate-50 px-2 py-1 rounded-lg border border-slate-100 mt-0.5'>
                                        <MapPin className='h-3 w-3 text-slate-400 shrink-0' />
                                        <span className='font-bold truncate'>
                                          {d.pointName}
                                        </span>
                                      </div>
                                    )}
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        );
                      })()}

                    {/* Tab: Points List */}
                    {previewTab === 'points' &&
                      (() => {
                        if (!preview) {
                          return (
                            <div className='flex flex-col items-center justify-center py-12 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 shadow-sm w-full'>
                              <MapPin className='h-8 w-8 text-slate-350 mb-2' />
                              <p className='text-xs font-bold text-slate-600'>
                                Chưa có dữ liệu xem trước
                              </p>
                              <p className='text-[10px] text-slate-455 mt-1 max-w-[200px]'>
                                Điền ngữ cảnh bên trái và bấm "Xem trước" để
                                tải danh sách điểm.
                              </p>
                            </div>
                          );
                        }
                        return (
                          <div className='space-y-3 pt-1'>
                            {points.length === 0 ? (
                              <div className='text-center py-8 text-slate-400 text-xs'>
                                Chưa có điểm trên bản đồ.
                              </div>
                            ) : (
                              <div className='grid gap-2 max-h-[380px] overflow-y-auto pr-1 sm:grid-cols-2'>
                                {points.map((pp) => (
                                  <div
                                    key={pp.pointId}
                                    className='rounded-xl border border-slate-150 p-3 flex flex-col justify-between gap-1 shadow-sm bg-white transition-all'
                                  >
                                    <div className='min-w-0'>
                                      <p className='text-xs font-bold text-slate-800 flex items-center gap-1.5 truncate'>
                                        <MapPin className='h-3.5 w-3.5 shrink-0 text-[#C81E3A]' />
                                        {pp.pointName}
                                      </p>
                                      {pp.pointCode && (
                                        <p className='text-[9px] font-bold text-slate-400 mt-0.5'>
                                          {pp.pointCode}
                                        </p>
                                      )}
                                    </div>

                                    <div className='flex items-center justify-between mt-2 pt-2 border-t border-slate-100/50 text-[10px] text-slate-500'>
                                      <span className='font-bold'>
                                        {pp.studentCount} student(s)
                                      </span>
                                      {(!pp.latitude || !pp.longitude) && (
                                        <span className='text-[9px] font-bold text-amber-600'>
                                          Chưa có tọa độ
                                        </span>
                                      )}
                                    </div>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        );
                      })()}

                    {/* Tab 4: Context */}
                    {previewTab === 'context' &&
                      (() => {
                        // 1. School Info
                        const schName =
                          preview?.schoolName || currentSchool?.name || '-';
                        const schCode =
                          preview?.schoolCode || currentSchool?.code || '-';
                        const schAddr =
                          preview?.schoolAddress ||
                          currentSchool?.address ||
                          '-';

                        // 3. Active Days
                        const pActiveDays = preview?.activeDays;
                        const rawActiveDays = pActiveDays || [];
                        const activeDaysSet = new Set(
                          rawActiveDays.map((d: string) => d.toUpperCase())
                        );

                        const daysOfWeek = [
                          'MONDAY',
                          'TUESDAY',
                          'WEDNESDAY',
                          'THURSDAY',
                          'FRIDAY',
                          'SATURDAY',
                          'SUNDAY',
                        ];
                        const dayLabels: Record<string, string> = {
                          MONDAY: 'Mon',
                          TUESDAY: 'Tue',
                          WEDNESDAY: 'Wed',
                          THURSDAY: 'Thu',
                          FRIDAY: 'Fri',
                          SATURDAY: 'Sat',
                          SUNDAY: 'Sun',
                        };

                        // Check if service date matches schedule days
                        const sDate = preview?.serviceDate || form?.serviceDate;
                        let dateMatchText = '';
                        let dateMatchColor = '';
                        if (sDate) {
                          const dayNames = [
                            'SUNDAY',
                            'MONDAY',
                            'TUESDAY',
                            'WEDNESDAY',
                            'THURSDAY',
                            'FRIDAY',
                            'SATURDAY',
                          ];
                          const dateDayName =
                            dayNames[new Date(sDate).getDay()];
                          const isMatch = activeDaysSet.has(dateDayName);
                          if (isMatch) {
                            dateMatchText = 'Ngày phục vụ khớp với lịch hoạt động';
                            dateMatchColor =
                              'text-emerald-700 bg-emerald-55/10 border-emerald-100';
                          } else {
                            dateMatchText =
                              'Ngày phục vụ không nằm trong lịch hoạt động';
                            dateMatchColor =
                              'text-red-700 bg-red-55/10 border-red-100';
                          }
                        }

                        // 4. Planning Context
                        const serviceDateFormatted = sDate
                          ? formatDate(sDate)
                          : '-';
                        const dirVal =
                          preview?.direction || form?.routeDirection;
                        const dirLabel =
                          dirVal === 'OUTBOUND'
                            ? 'Nhà -> Trường'
                            : dirVal === 'RETURN'
                              ? 'Trường -> Nhà'
                              : (dirVal || '-');
                        const hasContext = !!(
                          form?.schoolId || preview?.schoolId
                        );
                        if (!hasContext) {
                          return (
                            <div className='flex flex-col items-center justify-center py-10 px-4 text-center bg-slate-50/40 rounded-2xl border border-dashed border-slate-200 mt-2 w-full'>
                              <Calendar className='h-8 w-8 text-slate-355 mb-2' />
                              <p className='text-xs font-bold text-slate-650'>
                                Chọn ngữ cảnh
                              </p>
                              <p className='text-[10px] text-slate-455 mt-1 max-w-[200px]'>
                                Chọn ngữ cảnh lập kế hoạch và xem trước nhu cầu.
                              </p>
                            </div>
                          );
                        }

                        return (
                          <div className='space-y-3 pt-1 text-xs text-slate-700 w-full'>
                            {/* School Section */}
                            <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-1.5 shadow-sm'>
                              <p className='text-[9px] font-extrabold text-slate-455 uppercase tracking-wider'>
                                Trường học
                              </p>
                              <div>
                                <p className='font-bold text-slate-800 text-sm leading-snug'>
                                  {schName}
                                </p>
                                <p className='text-[10px] text-slate-400 font-semibold mt-0.5'>
                                  {schCode}
                                </p>
                                {schAddr && schAddr !== '-' && (
                                  <p className='text-[10px] text-slate-500 mt-1 truncate'>
                                    {schAddr}
                                  </p>
                                )}
                              </div>
                            </div>

                            {/* Active Days Section */}
                            <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2.5 shadow-sm'>
                              <p className='text-[9px] font-extrabold text-slate-455 uppercase tracking-wider'>
                                Ngày hoạt động
                              </p>
                              <div className='flex flex-wrap gap-1.5'>
                                {daysOfWeek.map((day) => {
                                  const isActive = activeDaysSet.has(day);
                                  return (
                                    <span
                                      key={day}
                                      className={cn(
                                        'px-2 py-0.5 rounded-lg border text-[10px] font-bold transition-all',
                                        isActive
                                          ? 'bg-red-50 text-[#C81E3A] border-red-100'
                                          : 'bg-slate-50 text-slate-400 border-slate-100'
                                      )}
                                    >
                                      {dayLabels[day]}
                                    </span>
                                  );
                                })}
                              </div>
                              {dateMatchText && (
                                <div
                                  className={cn(
                                    'flex items-center gap-1.5 rounded-xl border px-2.5 py-1.5 text-[10px] font-bold shadow-sm',
                                    dateMatchColor
                                  )}
                                >
                                  <span>
                                    {dateMatchText ===
                                    'Ngày phục vụ khớp với lịch hoạt động'
                                      ? 'Đạt'
                                      : 'Cảnh báo:'}
                                  </span>
                                  <span>{dateMatchText}</span>
                                </div>
                              )}
                            </div>

                            {/* Planning Section */}
                            <div className='rounded-2xl border border-slate-150 bg-white p-3 space-y-2 shadow-sm'>
                              <p className='text-[9px] font-extrabold text-slate-455 uppercase tracking-wider'>
                                Planning
                              </p>
                              <div className='grid grid-cols-2 gap-x-3 gap-y-2 pt-1 text-[11px]'>
                                <div className='flex flex-col'>
                                  <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>
                                    Ngày phục vụ
                                  </span>
                                  <span className='font-bold text-slate-800 mt-0.5'>
                                    {serviceDateFormatted}
                                  </span>
                                </div>
                                <div className='flex flex-col'>
                                  <span className='text-[9px] font-bold text-slate-400 uppercase tracking-wider'>
                                    Chiều tuyến
                                  </span>
                                  <span className='font-bold text-slate-800 mt-0.5'>
                                    {dirLabel}
                                  </span>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })()}
                  </div>
                );
              })()}
          </>
        )}

        {/* Tab 2: Route Builder */}
        {rightPanelTab === 'route-builder' && (
          <>
            {/* Case A: No active session */}
            {!activeSession && (
              <div className='flex flex-col items-center justify-center p-8 text-center bg-white h-full min-h-[350px]'>
                <div className='flex h-12 w-12 items-center justify-center rounded-full bg-slate-50 border border-slate-150 text-slate-400 mb-3 shadow-sm'>
                  <Route className='h-6 w-6' />
                </div>
                <p className='text-sm font-bold text-slate-700'>
                  Chưa có phiên lập tuyến
                </p>
                <p className='mt-1 text-xs text-slate-455 max-w-[240px] leading-relaxed'>
                  Hãy tạo phiên trước khi xây dựng tuyến.
                </p>
              </div>
            )}

            {/* Case B: Active Session exists */}
            {activeSession && (
              <>
                <div className='p-5 space-y-4'>
                  <div className='border-b border-slate-100 pb-2.5 flex items-center justify-between'>
                    <h3 className='text-sm font-bold text-slate-955 flex items-center gap-2'>
                      <Route className='h-4 w-4 text-blue-600 shrink-0' />
                      Tuyến ({sessionRoutes.length})
                    </h3>
                    {refreshingRoutes && !creatingRoute && (
                      <span className='inline-flex items-center gap-1 text-[10px] font-semibold text-slate-400'>
                        <Loader2 className='h-3 w-3 animate-spin' />
                        Đang làm mới
                      </span>
                    )}
                  </div>
                  <CreateRoutePanel
                    session={activeSession}
                    onSubmit={onCreateManualRoute}
                    submitting={creatingRoute}
                    open={createRouteBuilderOpen}
                    onOpenChange={setCreateRouteBuilderOpen}
                  />
                  {creatingRoute ? (
                    <div className='flex flex-col items-center justify-center py-10 px-4 text-center bg-slate-50/50 border border-slate-200 rounded-2xl shadow-sm space-y-3 min-h-[160px] animate-pulse'>
                      <Loader2 className='h-7 w-7 text-blue-600 animate-spin' />
                      <div className='space-y-1'>
                        <p className='text-xs font-bold text-slate-700'>
                          Đang tạo tuyến...
                        </p>
                        <p className='text-[10px] text-slate-455 font-semibold'>
                          Đang tính lộ trình và thời gian dự kiến...
                        </p>
                      </div>
                    </div>
                  ) : sessionRoutes.length === 0 ? (
                    <div className='flex flex-col items-center justify-center p-8 border border-dashed border-slate-200 rounded-2xl bg-slate-50/50 text-center'>
                      <Route className='h-8 w-8 text-slate-300 stroke-[1.5] mb-2' />
                      <p className='text-xs font-bold text-slate-500'>
                        Chưa có tuyến
                      </p>
                      <p className='text-[10px] text-slate-400 mt-0.5'>
                        Tạo tuyến đầu tiên ở phía trên để bắt đầu.
                      </p>
                    </div>
                  ) : (
                    <div className='space-y-3'>
                      {sessionRoutes.map((r) => (
                        <SessionRouteCard
                          key={r.id}
                          route={r}
                          onSelect={(id) =>
                            onSelectRoute(selectedRouteId === id ? null : id)
                          }
                          isSelected={selectedRouteId === r.id}
                          onAddStudents={handleAddStudents}
                          onDelete={() => {
                            deleteConfirm.requestConfirm({
                              title: 'Xóa tuyến trống?',
                              description:
                                'Bạn có chắc chắn muốn xóa tuyến này không? Thao tác này không thể hoàn tác.',
                              confirmLabel: 'Xóa',
                              variant: 'danger',
                              payload: r.id,
                            });
                          }}
                          deleting={
                            deletingRoute &&
                            deleteConfirm.confirmState.payload === r.id
                          }
                          onGreedyFill={() => {
                            greedyConfirm.requestConfirm({
                              title: 'Gán tự động cho tuyến này?',
                              description:
                                'Tự động gán học sinh đủ điều kiện vào tuyến dựa trên khoảng cách và sức chứa còn lại của xe?',
                              confirmLabel: 'Gán tự động',
                              variant: 'primary',
                              payload: r.id,
                            });
                          }}
                          greedyFilling={
                            greedyFilling &&
                            greedyConfirm.confirmState.payload === r.id
                          }
                        />
                      ))}
                    </div>
                  )}
                </div>

                {/* Demand assignment panel - shown when a route is selected */}
                <div
                  ref={manualAssignPanelRef}
                  id='manual-demand-assign-panel'
                  className='scroll-mt-4'
                >
                  <ManualDemandAssignPanel
                    selectedRoute={
                      sessionRoutes.find((r) => r.id === selectedRouteId) ||
                      null
                    }
                    eligibleStudents={eligibleStudents}
                    sessionId={activeSession.id}
                    loadingEligible={loadingEligible}
                  />
                </div>
              </>
            )}
          </>
        )}

        {selectedRouteId && rightPanelTab === 'route-builder' && (
          <RouteDetailPanel
            routeId={selectedRouteId}
            sessionId={activeSession?.id || 0}
          />
        )}
      </div>
      <SchoolBusConfirmDialog {...deleteConfirm.dialogProps} />
      <SchoolBusConfirmDialog {...greedyConfirm.dialogProps} />
    </div>
  );
}
