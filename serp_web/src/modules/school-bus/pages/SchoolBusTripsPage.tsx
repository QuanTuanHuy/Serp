'use client';

import * as React from 'react';
import Link from 'next/link';
import {
  CheckCircle2,
  MapPin,
  PlayCircle,
  Route,
  Search,
  Warehouse,
  GraduationCap,
  BusFront,
  User,
  Users,
  AlertTriangle,
  ArrowRightLeft,
  Calendar,
} from 'lucide-react';
import { toast } from 'sonner';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { Button, Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useArriveTripStopMutation,
  useCompleteTripMutation,
  useDepartTripStopMutation,
  useGetTripsQuery,
  useStartTripMutation,
} from '../api/schoolBusApi';
import { SchoolBusMetricCard } from '../components/SchoolBusMetricCard';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import { SchoolBusDataTable } from '../components/ui/SchoolBusDataTable';
import type { SchoolBusTableColumn } from '../components/ui/SchoolBusDataTable';
import { SchoolBusSelect } from '../components/ui/SchoolBusSelect';
import { useSchoolBusPagination } from '../hooks/useSchoolBusPagination';
import { formatDate, getPageItems, SCHOOL_BUS_PAGE_QUERY_OPTIONS } from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';
import { schoolBusUi } from '../theme';
import { directionLabel, tripStatusLabel } from '../schoolBusLabels';

const statusMap: Record<string, { label: string; className: string }> = {
  PLANNED: {
    label: tripStatusLabel.PLANNED,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  },
  ASSIGNED: {
    label: tripStatusLabel.ASSIGNED,
    className: 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-50',
  },
  IN_PROGRESS: {
    label: tripStatusLabel.IN_PROGRESS,
    className: 'border-blue-200 bg-blue-50 text-blue-700 hover:bg-blue-50',
  },
  COMPLETED: {
    label: tripStatusLabel.COMPLETED,
    className:
      'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-50',
  },
  CANCELLED: {
    label: tripStatusLabel.CANCELLED,
    className: 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50',
  },
};

const renderTripStatus = (status: string) => {
  const normalized = status.toUpperCase();
  const config = statusMap[normalized] || {
    label: normalized,
    className: 'border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-50',
  };
  return (
    <Badge
      className={cn(
        'rounded-full px-3 py-0.5 text-xs font-semibold shadow-none border',
        config.className
      )}
    >
      {config.label}
    </Badge>
  );
};

const formatLocationType = (type?: string | null) => {
  if (!type) return '';
  const t = type.toUpperCase();
  if (t === 'SCHOOL') return 'Trường học';
  if (t === 'DEPOT') return 'Bãi xe';
  if (t === 'PICKUP') return 'Điểm đón';
  if (t === 'DROPOFF') return 'Điểm trả';
  return type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
};

const getFriendlyDirection = (dir?: string | null) => {
  if (dir) return directionLabel[dir] || dir;
  return dir || '';
};

export function SchoolBusTripsPage() {
  const access = useSchoolBusAccess();
  const pagination = useSchoolBusPagination({
    page: 0,
    size: 10,
    sortBy: 'serviceDate',
    sortDirection: 'DESC',
  });
  const {
    data,
    isLoading,
    refetch: refetchTrips,
  } = useGetTripsQuery(pagination.params, SCHOOL_BUS_PAGE_QUERY_OPTIONS);
  const [startTrip] = useStartTripMutation();
  const [arriveTripStop] = useArriveTripStopMutation();
  const [departTripStop] = useDepartTripStopMutation();
  const [completeTrip] = useCompleteTripMutation();
  const trips = getPageItems(data?.data);

  const [lastUpdated, setLastUpdated] = React.useState<string>('');
  const [isTabVisible, setIsTabVisible] = React.useState(true);
  const [pollInterval, setPollInterval] = React.useState(20000); // 20s default

  // Track page visibility
  React.useEffect(() => {
    const handleVisibilityChange = () => {
      const visible = document.visibilityState === 'visible';
      setIsTabVisible(visible);
      if (visible) {
        refetchTrips().then((result) => {
          if (result.error) {
            const err = result.error as any;
            if (err?.status === 429) {
              setPollInterval(60000);
            }
          } else {
            setLastUpdated(new Date().toLocaleTimeString());
            setPollInterval(20000);
          }
        });
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [refetchTrips]);

  // Polling effect
  React.useEffect(() => {
    if (!isTabVisible) return;

    const intervalId = setInterval(() => {
      refetchTrips().then((result) => {
        if (result.error) {
          const err = result.error as any;
          if (err?.status === 429) {
            setPollInterval(60000);
          }
        } else {
          setLastUpdated(new Date().toLocaleTimeString());
          setPollInterval(20000);
        }
      });
    }, pollInterval);

    return () => clearInterval(intervalId);
  }, [refetchTrips, isTabVisible, pollInterval]);

  React.useEffect(() => {
    if (data) {
      setLastUpdated(new Date().toLocaleTimeString());
    }
  }, [data]);

  const [searchTerm, setSearchTerm] = React.useState('');
  const [debouncedSearch, setDebouncedSearch] = React.useState('');
  const [filterStatus, setFilterStatus] = React.useState('');
  const [filterDirection, setFilterDirection] = React.useState('');
  const [filterDate, setFilterDate] = React.useState('');

  React.useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(t);
  }, [searchTerm]);

  React.useEffect(() => {
    pagination.setKeyword(debouncedSearch || '');
  }, [debouncedSearch]);

  const filteredTrips = React.useMemo(() => {
    let result = trips;
    if (filterStatus) {
      result = result.filter((t) => t.status === filterStatus);
    }
    if (filterDirection) {
      result = result.filter((t) => t.routeDirection === filterDirection);
    }
    if (filterDate) {
      result = result.filter((t) => t.serviceDate === filterDate);
    }
    return result;
  }, [trips, filterStatus, filterDirection, filterDate]);

  const uniqueDates = React.useMemo(() => {
    const dates = new Set<string>();
    trips.forEach((t) => {
      if (t.serviceDate) dates.add(t.serviceDate);
    });
    return Array.from(dates).sort((a, b) => b.localeCompare(a));
  }, [trips]);

  const dateOptions = React.useMemo(() => {
    return [
      { label: 'Tất cả ngày phục vụ', value: '' },
      ...uniqueDates.map((date) => ({
        label: formatDate(date),
        value: date,
      })),
    ];
  }, [uniqueDates]);

  const statusOptions = [
    { label: 'Tất cả trạng thái', value: '' },
    { label: tripStatusLabel.PLANNED, value: 'PLANNED', color: 'slate' as const },
    { label: tripStatusLabel.ASSIGNED, value: 'ASSIGNED', color: 'orange' as const },
    { label: tripStatusLabel.IN_PROGRESS, value: 'IN_PROGRESS', color: 'blue' as const },
    { label: tripStatusLabel.COMPLETED, value: 'COMPLETED', color: 'green' as const },
    { label: tripStatusLabel.CANCELLED, value: 'CANCELLED', color: 'red' as const },
  ];

  const directionOptions = [
    { label: 'Tất cả chiều tuyến', value: '' },
    { label: directionLabel.OUTBOUND, value: 'OUTBOUND' },
    { label: directionLabel.RETURN, value: 'RETURN' },
  ];

  const call = async (label: string, action: () => Promise<any>) => {
    try {
      const response = await action();
      toast.success(response.message || `Đã hoàn tất: ${label}`);
    } catch (error: any) {
      toast.error(
        error?.status === 429
          ? 'Hệ thống đang bận. Vui lòng đợi vài giây và thử lại.'
          : (error?.data?.message || `Thao tác ${label} thất bại`)
      );
    }
  };

  const tripColumns: SchoolBusTableColumn<any>[] = [
    {
      key: 'trip',
      header: 'Chuyến xe',
      className: 'pl-6 font-semibold text-slate-900',
      headerClassName: 'pl-6',
      render: (trip) => (
        <div className='flex items-center gap-3 font-semibold text-slate-900'>
          <div className='flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700 border border-blue-100/50'>
            <Route className='h-4.5 w-4.5' />
          </div>
          <div className='flex flex-col min-w-0'>
            <span className='font-bold text-slate-950 truncate'>
              {trip.tripCode}
            </span>
            <span className='text-[10px] text-slate-400 font-normal mt-0.5'>
              {getFriendlyDirection(trip.routeDirection)} -{' '}
              {formatDate(trip.serviceDate)}
            </span>
          </div>
        </div>
      ),
    },
    {
      key: 'route',
      header: 'Tuyến',
      render: (trip) => (
        <div className='flex flex-col min-w-0'>
          <span className='font-bold text-slate-900 truncate'>
            {trip.routeCode}
          </span>
          <span className='text-xs text-slate-500 truncate mt-0.5'>
            {trip.routeName}
          </span>
        </div>
      ),
    },
    {
      key: 'service',
      header: 'Dịch vụ',
      render: (trip) => (
        <div className='flex flex-col text-slate-700 gap-0.5'>
          <span className='font-semibold text-xs'>
            {formatDate(trip.serviceDate)}
          </span>
          <span className='inline-flex items-center text-[9px] font-extrabold text-slate-500 bg-slate-100 rounded px-1.5 py-0.5 w-fit uppercase tracking-wider'>
            {getFriendlyDirection(trip.routeDirection)}
          </span>
        </div>
      ),
    },
    {
      key: 'startEnd',
      header: 'Điểm đầu -> Điểm cuối',
      render: (trip) => {
        const StartIcon =
          trip.startLocationType === 'SCHOOL'
            ? GraduationCap
            : trip.startLocationType === 'DEPOT'
              ? Warehouse
              : MapPin;
        const EndIcon =
          trip.endLocationType === 'SCHOOL'
            ? GraduationCap
            : trip.endLocationType === 'DEPOT'
              ? Warehouse
              : MapPin;
        const startColor =
          trip.startLocationType === 'SCHOOL'
            ? 'text-red-500 bg-red-50 border-red-100'
            : 'text-orange-500 bg-orange-50 border-orange-100';
        const endColor =
          trip.endLocationType === 'SCHOOL'
            ? 'text-red-500 bg-red-50 border-red-100'
            : 'text-orange-500 bg-orange-50 border-orange-100';

        return (
          <div className='flex flex-col gap-1 text-slate-700 max-w-[240px]'>
            <div className='flex items-center gap-1.5 text-xs font-semibold text-slate-900 truncate'>
              <div
                className={cn(
                  'flex h-5 w-5 shrink-0 items-center justify-center rounded border text-[10px]',
                  startColor
                )}
              >
                <StartIcon className='h-3 w-3' />
              </div>
              <span className='truncate' title={trip.startLocationName}>
                {trip.startLocationName || 'Chưa có'}
              </span>
              <span className='text-slate-400 font-normal shrink-0 mx-0.5'>
                {'->'}
              </span>
              <div
                className={cn(
                  'flex h-5 w-5 shrink-0 items-center justify-center rounded border text-[10px]',
                  endColor
                )}
              >
                <EndIcon className='h-3 w-3' />
              </div>
              <span className='truncate' title={trip.endLocationName}>
                {trip.endLocationName || 'Chưa có'}
              </span>
            </div>
            <span className='text-[10px] text-slate-500 font-medium'>
              {formatLocationType(trip.startLocationType)} {'->'}{' '}
              {formatLocationType(trip.endLocationType)}
            </span>
          </div>
        );
      },
    },
    {
      key: 'assignment',
      header: 'Phân công',
      render: (trip) => {
        const hasBus = !!trip.busPlateNumber;
        const hasDriver = !!trip.driverName;
        const hasAttendant = !!trip.attendantName;

        return (
          <div className='flex flex-col gap-1 text-xs'>
            {hasBus ? (
              <div className='flex items-center gap-1 font-medium text-slate-700'>
                <BusFront className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                <span className='font-bold bg-slate-50 border border-slate-200/60 rounded px-1.5 py-0.5 text-[10px] font-mono text-slate-800'>
                  {trip.busPlateNumber}
                </span>
              </div>
            ) : (
              <span className='inline-flex items-center gap-1 text-[9px] font-extrabold text-amber-600 bg-amber-50 border border-amber-100 rounded px-1.5 py-0.5 w-fit'>
                <AlertTriangle className='h-2.5 w-2.5' /> Thiếu xe
              </span>
            )}

            {hasDriver ? (
              <div className='flex items-center gap-1 text-slate-600'>
                <User className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                <span
                  className='truncate max-w-[120px]'
                  title={trip.driverName}
                >
                  {trip.driverName}
                </span>
              </div>
            ) : (
              <span className='inline-flex items-center gap-1 text-[9px] font-extrabold text-amber-600 bg-amber-50 border border-amber-100 rounded px-1.5 py-0.5 w-fit'>
                <AlertTriangle className='h-2.5 w-2.5' /> Thiếu tài xế
              </span>
            )}

            {hasAttendant ? (
              <div className='flex items-center gap-1 text-slate-500'>
                <Users className='h-3.5 w-3.5 text-slate-400 shrink-0' />
                <span
                  className='truncate max-w-[120px]'
                  title={trip.attendantName}
                >
                  {trip.attendantName}
                </span>
              </div>
            ) : null}

            {hasBus && hasDriver && (
              <span className='inline-flex items-center gap-1 text-[9px] font-extrabold uppercase bg-emerald-50 text-emerald-700 border border-emerald-100 rounded px-1.5 py-0.5 w-fit mt-0.5'>
                Đã phân công
              </span>
            )}
          </div>
        );
      },
    },
    {
      key: 'stopsCount',
      header: 'Tiến độ điểm dừng',
      render: (trip) => {
        const totalStops = trip.totalStops || 0;
        const completedStops =
          trip.status === 'COMPLETED' ? totalStops : trip.completedStops || 0;
        const percent =
          totalStops > 0 ? (completedStops / totalStops) * 100 : 0;

        return (
          <div className='flex flex-col gap-1 min-w-[120px] max-w-[160px] text-xs'>
            <div className='flex items-center justify-between font-semibold text-slate-700'>
              <span>
                {completedStops} / {totalStops} điểm dừng
              </span>
              {completedStops === totalStops && totalStops > 0 && (
                <span className='text-[9px] bg-emerald-50 text-emerald-700 px-1 py-0.2 rounded font-bold border border-emerald-100'>
                  Hoàn thành
                </span>
              )}
            </div>
            {totalStops > 0 ? (
              <div className='h-1.5 w-full bg-slate-100 rounded-full overflow-hidden'>
                <div
                  className={cn(
                    'h-full transition-all duration-300 rounded-full',
                    completedStops === totalStops
                      ? 'bg-emerald-500'
                      : 'bg-blue-500'
                  )}
                  style={{ width: `${percent}%` }}
                />
              </div>
            ) : (
              <span className='text-[10px] text-slate-400 italic'>
                Chưa có điểm dừng
              </span>
            )}
            {trip.nextStopName && (
              <span
                className='text-[10px] text-slate-500 truncate mt-0.5'
                title={trip.nextStopName}
              >
                Tiếp theo: {trip.nextStopName}
              </span>
            )}
          </div>
        );
      },
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (trip) => (
        <div className='space-y-1.5'>
          {renderTripStatus(trip.status)}
          {trip.status === 'CANCELLED' && trip.cancellationReason && (
            <p
              className='text-[10px] text-red-600 bg-red-50 border border-red-100 rounded px-1.5 py-0.5 max-w-[140px] truncate'
              title={trip.cancellationReason}
            >
              Lý do: {trip.cancellationReason}
            </p>
          )}
        </div>
      ),
    },
    {
      key: 'actions',
      header: access.isParentOnly ? 'Theo dõi' : 'Thao tác',
      className: 'pr-6 text-right',
      headerClassName: 'pr-6 text-right',
      render: (trip) => {
        const normalized = trip.status.toUpperCase();

        // Non-operators (Attendant, Parent) see view-only link
        if (!access.canOperateTrip || access.isParentOnly) {
          return (
            <div className='flex items-center justify-end gap-1.5'>
              <Button
                size='sm'
                variant='outline'
                className='rounded-full h-8 text-xs font-semibold px-3'
                asChild
              >
                <Link href={`/school-bus/trips/${trip.id}`}>
                  {access.isParentOnly ? 'Theo dõi chuyến' : 'Xem chi tiết'}
                </Link>
              </Button>
            </div>
          );
        }

        if (normalized === 'PLANNED' || normalized === 'ASSIGNED') {
          return (
            <div className='flex items-center justify-end gap-1.5'>
              <Button
                size='sm'
                className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                onClick={() =>
                  call('Bắt đầu chuyến', () => startTrip(trip.id).unwrap())
                }
              >
                <PlayCircle className='mr-1.5 h-3.5 w-3.5' />
                Bắt đầu chuyến
              </Button>
              <Button
                size='sm'
                variant='outline'
                className='rounded-full h-8 text-xs font-semibold px-3'
                asChild
              >
                <Link href={`/school-bus/trips/${trip.id}`}>
                  Mở vận hành
                </Link>
              </Button>
            </div>
          );
        }

        if (normalized === 'IN_PROGRESS') {
          const hasNextStop = !!trip.nextRouteStopId;

          if (hasNextStop) {
            const isArrivedOrBoarding = ['ARRIVED', 'BOARDING'].includes(
              trip.nextStopStatus
            );
            return (
              <div className='flex items-center justify-end gap-1.5'>
                {isArrivedOrBoarding ? (
                  <Button
                    size='sm'
                    className={cn(
                      schoolBusUi.primaryButton,
                      'rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                    )}
                    onClick={() =>
                      call('Rời điểm dừng', () =>
                        departTripStop({
                          tripId: trip.id,
                          routeStopId: trip.nextRouteStopId,
                        }).unwrap()
                      )
                    }
                  >
                    Rời điểm dừng
                  </Button>
                ) : (
                  <Button
                    size='sm'
                    className='bg-[#C81E3A] hover:bg-[#B31B34] text-white rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                    onClick={() =>
                      call('Đến điểm dừng', () =>
                        arriveTripStop({
                          tripId: trip.id,
                          routeStopId: trip.nextRouteStopId,
                        }).unwrap()
                      )
                    }
                  >
                    <MapPin className='mr-1.5 h-3.5 w-3.5' />
                    Đến điểm tiếp theo
                  </Button>
                )}
                <Button
                  size='sm'
                  variant='outline'
                  className='rounded-full h-8 text-xs font-semibold px-3'
                  asChild
                >
                  <Link href={`/school-bus/trips/${trip.id}`}>
                    Mở vận hành
                  </Link>
                </Button>
              </div>
            );
          } else {
            return (
              <div className='flex items-center justify-end gap-1.5'>
                <Button
                  size='sm'
                  className={cn(
                    schoolBusUi.primaryButton,
                    'rounded-full px-4.5 font-bold shadow-none h-8 border-0 text-xs shrink-0'
                  )}
                  onClick={() =>
                    call('Hoàn thành chuyến', () =>
                      completeTrip({ id: trip.id }).unwrap()
                    )
                  }
                >
                  Hoàn thành chuyến
                </Button>
                <Button
                  size='sm'
                  variant='outline'
                  className='rounded-full h-8 text-xs font-semibold px-3'
                  asChild
                >
                  <Link href={`/school-bus/trips/${trip.id}`}>
                    Mở vận hành
                  </Link>
                </Button>
              </div>
            );
          }
        }

        return (
          <div className='flex items-center justify-end gap-1.5'>
            <Button
              size='sm'
              variant='outline'
              className='rounded-full h-8 text-xs font-semibold px-3'
              asChild
            >
              <Link href={`/school-bus/trips/${trip.id}`}>Xem chi tiết</Link>
            </Button>
            <Button
              size='sm'
              variant='outline'
              className='rounded-full h-8 text-xs font-semibold px-3'
              disabled
            >
              Không có điểm chờ xử lý
            </Button>
          </div>
        );
      },
    },
  ];

  const tripToolbar = (
    <div className='flex flex-wrap items-center gap-3 justify-between w-full'>
      <div className='relative flex-1 min-w-[240px] max-w-xs'>
        <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
        <input
          type='text'
          placeholder='Tìm theo mã chuyến hoặc mã tuyến...'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className='w-full h-9 rounded-lg border border-slate-200 bg-white py-1.5 pl-9 pr-3 text-xs outline-none focus:border-slate-350 focus:ring-1 focus:ring-slate-200/50'
        />
      </div>

      <div className='flex flex-wrap items-center gap-2'>
        <SchoolBusSelect
          value={filterStatus}
          onChange={setFilterStatus}
          options={statusOptions}
          placeholder='Tất cả trạng thái'
          className='w-36'
        />
        <SchoolBusSelect
          value={filterDirection}
          onChange={setFilterDirection}
          options={directionOptions}
          placeholder='Tất cả chiều tuyến'
          className='w-36'
        />
        <SchoolBusSelect
          value={filterDate}
          onChange={setFilterDate}
          options={dateOptions}
          placeholder='Tất cả ngày phục vụ'
          className='w-40'
          disabled={uniqueDates.length === 0}
        />
      </div>
    </div>
  );

  return (
    <SchoolBusPageShell
      title={access.isParentOnly ? 'Theo dõi chuyến học sinh' : 'Vận hành chuyến'}
      description={
        access.isParentOnly
          ? 'Theo dõi các chuyến xe của học sinh theo thời gian gần thực.'
          : 'Quản lý các chuyến được tạo từ tuyến đã lập kế hoạch, bắt đầu chuyến và xử lý thứ tự đến/rời điểm.'
      }
      breadcrumb={
        <SchoolBusBreadcrumb
          items={
            access.isParentOnly
              ? [
                  { label: 'Xe bus trường học', href: '/school-bus/dashboard' },
                  { label: 'Theo dõi chuyến học sinh', current: true },
                ]
              : [
                  { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
                  { label: 'Vận hành chuyến', current: true },
                ]
          }
        />
      }
    >
      <div className='flex flex-col gap-6'>
        {/* Polling connection indicator */}
        {lastUpdated && (
          <div className='flex items-center justify-end -mb-3'>
            <span className='text-[10px] font-medium text-slate-400 px-2 py-0.5 rounded-full border border-slate-200 bg-slate-50 shrink-0'>
              <span className='inline-flex h-1.5 w-1.5 rounded-full bg-slate-400 mr-1.5 animate-pulse' />
              Tự làm mới mỗi 20 giây - cập nhật gần nhất: {lastUpdated}
            </span>
          </div>
        )}

        {/* Metrics row */}
        <div className='grid gap-4 md:grid-cols-3'>
          <SchoolBusMetricCard
            label={access.isParentOnly ? 'Tổng chuyến theo dõi' : 'Chuyến xe'}
            value={data?.data?.totalElements || 0}
            hint={
              access.isParentOnly
                ? 'Tất cả chuyến hiện tại và lịch sử của học sinh'
                : 'Bản ghi vận hành chuyến'
            }
            icon={Route}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Đang thực hiện'
            value={trips.filter((trip) => trip.status === 'IN_PROGRESS').length}
            hint={
              access.isParentOnly
                ? 'Chuyến đang chạy'
                : 'Chuyến đang vận hành'
            }
            icon={PlayCircle}
            tone='info'
          />
          <SchoolBusMetricCard
            label='Hoàn thành'
            value={trips.filter((trip) => trip.status === 'COMPLETED').length}
            hint={
              access.isParentOnly
                ? 'Chuyến đã kết thúc'
                : 'Chuyến đã đóng vận hành'
            }
            icon={CheckCircle2}
            tone='success'
          />
        </div>

        {/* Data Table within Card */}
        <SchoolBusDataTable
          title={
            access.isParentOnly ? 'Chuyến của học sinh' : 'Bảng vận hành chuyến'
          }
          description={
            access.isParentOnly
              ? 'Xem và theo dõi các chuyến hiện tại, lịch sử.'
              : 'Mỗi chuyến chỉ cho phép xử lý điểm dừng kế tiếp theo đúng thứ tự.'
          }
          toolbar={tripToolbar}
          data={filteredTrips}
          columns={tripColumns}
          isLoading={isLoading}
          pagination={{ page: data?.data, onPageChange: pagination.setPage }}
          stickyFirstColumn
          stickyActionColumn
          emptyIcon={Route}
          emptyTitle={
            trips.length === 0
              ? access.isParentOnly
                ? 'Không tìm thấy chuyến'
                : 'Chưa có chuyến'
              : 'Không có chuyến phù hợp với bộ lọc'
          }
          emptyDescription={
            trips.length === 0
              ? access.isParentOnly
                ? 'Hiện chưa có chuyến đang hoạt động hoặc đã lên lịch cho học sinh.'
                : 'Hãy tạo chuyến từ tuyến đã điều phối trước khi vận hành.'
              : 'Hãy thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.'
          }
        />
      </div>
    </SchoolBusPageShell>
  );
}
