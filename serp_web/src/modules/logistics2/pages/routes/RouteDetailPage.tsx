'use client';

import dynamic from 'next/dynamic';
import { useMemo, useState } from 'react';
import {
  AlertCircle,
  CalendarDays,
  CheckCircle2,
  Clock,
  Loader2,
  Map,
  MapPin,
  Package,
  PlayCircle,
  Truck,
  XCircle,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { useUser } from '@/modules/account';
import { useNotification } from '@/shared/hooks';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import {
  useAbortRouteStopMutation,
  useCancelRouteMutation,
  useCompleteRouteStopMutation,
  useGetRouteDetailQuery,
  useSelectRouteForDeliveryMutation,
} from '../../api/logistics2Api';
import { StatsCard } from '../../components/cards/StatsCard';
import { format } from 'date-fns';

interface RouteStopActionState {
  id: string;
  type: 'complete' | 'abort';
}

// IMPORT DYNAMIC BẢN ĐỒ KÈM SSR: FALSE
const RouteMap = dynamic(() => import('../common/RouteMap'), {
  ssr: false,
  loading: () => (
    <div className='h-[500px] w-full animate-pulse bg-slate-100 rounded-xl border flex items-center justify-center'>
      Đang tải bản đồ...
    </div>
  ),
});

const ROUTE_STATUS_CONFIG = {
  PENDING: {
    label: 'Chờ thực hiện',
    badgeClass: 'border-slate-200 bg-slate-100 text-slate-700',
    icon: Clock,
  },
  IN_PROGRESS: {
    label: 'Đang thực hiện',
    badgeClass: 'border-blue-200 bg-blue-100 text-blue-700',
    icon: PlayCircle,
  },
  COMPLETED: {
    label: 'Đã hoàn thành',
    badgeClass: 'border-emerald-200 bg-emerald-100 text-emerald-700',
    icon: CheckCircle2,
  },
  ABORTED: {
    label: 'Đã hủy',
    badgeClass: 'border-rose-200 bg-rose-100 text-rose-700',
    icon: AlertCircle,
  },
} as const;

const toDateKey = (date: Date) => format(date, 'yyyy-MM-dd');

const formatNumber = (value?: number | null, suffix?: string) => {
  const safeValue = value ?? 0;

  return `${new Intl.NumberFormat('vi-VN').format(safeValue)}${
    suffix ? ` ${suffix}` : ''
  }`;
};

const formatShortId = (value?: string | null) => {
  if (!value) {
    return '--';
  }

  return value.length > 12 ? `${value.slice(0, 10)}...` : value;
};

const formatVehicleType = (vehicleType?: string) => {
  if (!vehicleType) {
    return 'Chưa xác định';
  }

  switch (vehicleType.toUpperCase()) {
    case 'MOTORBIKE':
      return 'Xe máy';
    case 'VAN_500KG':
      return 'Xe tải van 500kg';
    case 'VAN_1000KG':
      return 'Xe tải van 1000kg';
    case 'LIGHT_TRUCK_1T':
      return 'Xe tải nhẹ 1 tấn';
    case 'LIGHT_TRUCK_2T':
      return 'Xe tải nhẹ 2 tấn';
    default:
      return vehicleType;
  }
};

const formatVehicleShipperStatus = (status?: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'Đang hoạt động';
    case 'INACTIVATE_REQUESTED':
      return 'Đang chờ hủy';
    case 'CANCELED':
      return 'Đã hủy';
    default:
      return 'Không xác định';
  }
};

export default function RouteDetailPage({ routeId }: { routeId: string }) {
  const notification = useNotification();
  const { user } = useUser();
  const [routeStopAction, setRouteStopAction] =
    useState<RouteStopActionState | null>(null);
  const todayKey = useMemo(() => toDateKey(new Date()), []);

  const {
    data: routeResponse,
    isLoading,
    error,
    isError,
    isFetching,
    refetch,
  } = useGetRouteDetailQuery(routeId);
  const [selectRouteForDelivery, { isLoading: isStartingRoute }] =
    useSelectRouteForDeliveryMutation();
  const [cancelRoute, { isLoading: isCancelingRoute }] =
    useCancelRouteMutation();
  const [completeRouteStop] = useCompleteRouteStopMutation();
  const [abortRouteStop] = useAbortRouteStopMutation();

  const route = routeResponse?.data;
  const routeErrorMessage = isError
    ? getErrorMessage(error)
    : 'Không thể tải chi tiết lộ trình. Vui lòng thử lại.';

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse space-y-3'>
          <div className='h-28 rounded-3xl bg-muted' />
          <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-5'>
            {Array.from({ length: 5 }).map((_, index) => (
              <div key={index} className='h-28 rounded-xl bg-muted' />
            ))}
          </div>
          <div className='h-[500px] rounded bg-muted' />
        </div>
      </div>
    );
  }

  if (isError || !route) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-2 p-6 text-center'>
          <AlertCircle className='mx-auto h-10 w-10 text-destructive' />
          <h3 className='text-lg font-semibold'>Không tìm thấy lộ trình</h3>
          <p className='text-sm text-muted-foreground'>{routeErrorMessage}</p>
        </CardContent>
      </Card>
    );
  }

  const statusConfig =
    ROUTE_STATUS_CONFIG[route.status as keyof typeof ROUTE_STATUS_CONFIG] ||
    ROUTE_STATUS_CONFIG.PENDING;
  const StatusIcon = statusConfig.icon;
  const vehicleShipper = route.vehicleShipper;
  const vehicle = vehicleShipper?.vehicle;
  const isRouteOwner = Boolean(
    user?.id && vehicleShipper?.shipperId === user.id
  );
  const isLogistics2Manager = Boolean(
    user?.roles?.includes('LOGISTICS2_MANAGER')
  );
  const isCancelableStatus =
    route.status === 'PENDING' || route.status === 'IN_PROGRESS';
  const canStartRoute =
    isRouteOwner &&
    route.status === 'PENDING' &&
    toDateKey(new Date(route.deliveryDate)) === todayKey;
  const canCancelRoute =
    isCancelableStatus && (isRouteOwner || isLogistics2Manager);
  const canOperateStops = isRouteOwner && route.status === 'IN_PROGRESS';
  const isSubmittingRouteAction = isStartingRoute || isCancelingRoute;

  const handleStartRoute = async () => {
    if (!canStartRoute) {
      return;
    }

    try {
      await selectRouteForDelivery(route.id).unwrap();
      notification.success('Đã bắt đầu lộ trình', {
        description: 'Lộ trình được chuyển sang trạng thái đang giao.',
      });
      await refetch();
    } catch (actionError) {
      notification.error('Không thể bắt đầu lộ trình', {
        description: getErrorMessage(actionError),
      });
    }
  };

  const handleCancelRoute = async () => {
    if (!canCancelRoute) {
      return;
    }

    if (
      !window.confirm('Bạn có chắc chắn muốn hủy chuyến giao hàng này không?')
    ) {
      return;
    }

    try {
      await cancelRoute(route.id).unwrap();
      notification.success('Đã hủy chuyến giao', {
        description: 'Tất cả đơn hàng trong chuyến giao cần được trả về kho.',
      });
      await refetch();
    } catch (actionError) {
      notification.error('Không thể hủy chuyến giao', {
        description: getErrorMessage(actionError),
      });
    }
  };

  const handleCompleteRouteStop = async (routeStopId: string) => {
    if (!canOperateStops || routeStopAction) {
      return;
    }

    setRouteStopAction({ id: routeStopId, type: 'complete' });

    try {
      await completeRouteStop(routeStopId).unwrap();
      notification.success('Đã giao thành công điểm dừng', {
        description: 'Trạng thái điểm giao đã được cập nhật.',
      });
      await refetch();
    } catch (actionError) {
      notification.error('Không thể cập nhật điểm giao', {
        description: getErrorMessage(actionError),
      });
    } finally {
      setRouteStopAction(null);
    }
  };

  const handleAbortRouteStop = async (routeStopId: string) => {
    if (!canOperateStops || routeStopAction) {
      return;
    }

    setRouteStopAction({ id: routeStopId, type: 'abort' });

    try {
      await abortRouteStop(routeStopId).unwrap();
      notification.success('Đã bỏ qua điểm dừng', {
        description: 'Hệ thống đã đánh dấu điểm giao là bỏ qua.',
      });
      await refetch();
    } catch (actionError) {
      notification.error('Không thể bỏ qua điểm dừng', {
        description: getErrorMessage(actionError),
      });
    } finally {
      setRouteStopAction(null);
    }
  };

  return (
    <div className='space-y-6'>
      <section className='relative overflow-hidden rounded-3xl border border-sky-200/70 bg-gradient-to-br from-sky-100 via-white to-cyan-100 p-6 shadow-lg shadow-sky-100/70'>
        <div className='pointer-events-none absolute -top-20 -right-10 h-56 w-56 rounded-full bg-cyan-300/30 blur-3xl' />
        <div className='pointer-events-none absolute -bottom-24 -left-16 h-56 w-56 rounded-full bg-sky-300/30 blur-3xl' />

        <div className='relative z-10 flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
          <div className='space-y-2'>
            <p className='text-xs font-semibold uppercase tracking-[0.2em] text-sky-700'>
              Chi tiết lộ trình
            </p>
            <h1 className='text-2xl font-bold tracking-tight text-slate-900'>
              {route.id}
            </h1>
            <p className='flex items-center gap-2 text-sm text-slate-700'>
              <CalendarDays className='h-4 w-4' />
              Ngày giao: {route.deliveryDate}
            </p>
            <p className='text-sm text-slate-600'>
              Kế hoạch: {route.deliveryPlanId}
            </p>
            {isFetching && (
              <p className='flex items-center gap-1.5 text-xs text-sky-700'>
                <Loader2 className='h-3.5 w-3.5 animate-spin' />
                Đang đồng bộ dữ liệu...
              </p>
            )}
          </div>

          <div className='flex flex-wrap items-center gap-2'>
            <Badge
              variant='outline'
              className={cn('gap-1.5 border', statusConfig.badgeClass)}
            >
              <StatusIcon className='h-3.5 w-3.5' />
              <span>{statusConfig.label}</span>
            </Badge>

            {canStartRoute && (
              <Button
                className='bg-emerald-600 text-white hover:bg-emerald-700'
                onClick={handleStartRoute}
                disabled={isSubmittingRouteAction || Boolean(routeStopAction)}
              >
                {isStartingRoute ? (
                  <Loader2 className='h-4 w-4 animate-spin' />
                ) : (
                  <PlayCircle className='h-4 w-4' />
                )}
                Bắt đầu giao
              </Button>
            )}

            {canCancelRoute && (
              <Button
                variant='destructive'
                onClick={handleCancelRoute}
                disabled={isSubmittingRouteAction || Boolean(routeStopAction)}
              >
                {isCancelingRoute ? (
                  <Loader2 className='h-4 w-4 animate-spin' />
                ) : (
                  <XCircle className='h-4 w-4' />
                )}
                Hủy chuyến
              </Button>
            )}
          </div>
        </div>
      </section>

      <div className='grid gap-4 sm:grid-cols-2 xl:grid-cols-4'>
        <StatsCard
          title='Tổng quãng đường'
          value={formatNumber(route.totalDistanceKm, 'km')}
          icon={Map}
          variant='primary'
        />
        <StatsCard
          title='Số điểm giao'
          value={formatNumber(route.routeStopCount, 'điểm')}
          icon={MapPin}
          variant='warning'
        />
        <StatsCard
          title='Khối lượng tải'
          value={formatNumber(route.totalWeightLoadedKg, 'kg')}
          icon={Truck}
          variant='success'
        />
        <StatsCard
          title='Tổng kích thước'
          value={formatNumber(route.totalVolumeLoadedCbm, 'cbm')}
          icon={Package}
          variant='default'
        />
      </div>

      <Card className='border-slate-200/80 bg-white/95 shadow-sm'>
        <CardHeader>
          <CardTitle className='text-base'>Chi tiết phương tiện giao</CardTitle>
        </CardHeader>
        <CardContent className='grid gap-3 sm:grid-cols-2 xl:grid-cols-4'>
          <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-3'>
            <p className='text-xs uppercase tracking-wide text-slate-500'>
              Biển kiểm soát
            </p>
            <p className='mt-1 text-sm font-semibold text-slate-900'>
              {vehicle?.licensePlate || 'Chưa xác định'}
            </p>
          </div>
          <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-3'>
            <p className='text-xs uppercase tracking-wide text-slate-500'>
              Loại xe
            </p>
            <p className='mt-1 text-sm font-semibold text-slate-900'>
              {formatVehicleType(vehicle?.vehicleType)}
            </p>
          </div>
          <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-3'>
            <p className='text-xs uppercase tracking-wide text-slate-500'>
              Sức chở tối đa
            </p>
            <p className='mt-1 text-sm font-semibold text-slate-900'>
              {formatNumber(vehicle?.maxWeightKg, 'kg')}
            </p>
          </div>
          <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-3'>
            <p className='text-xs uppercase tracking-wide text-slate-500'>
              Dung tích tối đa
            </p>
            <p className='mt-1 text-sm font-semibold text-slate-900'>
              {formatNumber(vehicle?.maxVolumeCbm, 'cbm')}
            </p>
          </div>
        </CardContent>
      </Card>

      <Card className='border-slate-200/80 bg-white/95 shadow-sm'>
        <CardHeader>
          <CardTitle>Bản đồ lộ trình di chuyển</CardTitle>
        </CardHeader>
        <CardContent className='p-0 sm:p-6 sm:pt-0'>
          <RouteMap
            route={route}
            canOperateStops={canOperateStops}
            onCompleteRouteStop={handleCompleteRouteStop}
            onAbortRouteStop={handleAbortRouteStop}
            routeStopAction={routeStopAction}
          />
        </CardContent>
      </Card>
    </div>
  );
}
