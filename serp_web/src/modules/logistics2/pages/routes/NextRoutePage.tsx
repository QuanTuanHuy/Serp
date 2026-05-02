/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 next route tracking page
*/

'use client';

import Link from 'next/link';
import { useEffect, useMemo } from 'react';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import {
  ArrowRight,
  CalendarDays,
  CheckCircle2,
  CircleOff,
  Loader2,
  MapPin,
  Navigation,
  Phone,
  RefreshCcw,
  Route as RouteIcon,
  SkipForward,
  Truck,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { useAppDispatch } from '@/lib/store';
import { useUser } from '@/modules/account';
import { useNotification } from '@/shared/hooks';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
} from '@/shared/components/ui';
import {
  useAbortRouteStopMutation,
  useCompleteRouteStopMutation,
  useGetNextRouteStopQuery,
  useGetVehicleShippersQuery,
} from '../../api/logistics2Api';
import { setActiveModule } from '../../store';
import type {
  RouteStop,
  RouteStopStatus,
  VehicleShipperStatus,
} from '../../types';
import { VehicleShipperCard } from '../../components/cards/VehicleShipperCard';

const ROUTE_STOP_STATUS_STYLES: Record<
  RouteStopStatus,
  { label: string; className: string }
> = {
  WAITING: {
    label: 'Đang chờ giao',
    className: 'bg-amber-100 text-amber-800 border-amber-200',
  },
  ARRIVED: {
    label: 'Đã giao thành công',
    className: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  },
  FAILED: {
    label: 'Giao thất bại',
    className: 'bg-rose-100 text-rose-800 border-rose-200',
  },
};

const VEHICLE_SHIPPER_STATUS_STYLES: Record<
  VehicleShipperStatus,
  { label: string; className: string }
> = {
  ACTIVE: {
    label: 'Đang hoạt động',
    className: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  },
  INACTIVATE_REQUESTED: {
    label: 'Đang chờ hủy',
    className: 'bg-amber-100 text-amber-800 border-amber-200',
  },
  CANCELED: {
    label: 'Đã hủy',
    className: 'bg-slate-100 text-slate-700 border-slate-200',
  },
};

const formatShortId = (value?: string | null) => {
  if (!value) {
    return '--';
  }

  return value.length > 12 ? `${value.slice(0, 10)}...` : value;
};

export const NextRoutePage = () => {
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const { user, isLoading: isUserLoading } = useUser();

  const todayKey = useMemo(() => format(new Date(), 'yyyy-MM-dd'), []);
  const todayLabel = useMemo(
    () => format(new Date(), 'EEEE, dd/MM/yyyy', { locale: vi }),
    []
  );

  useEffect(() => {
    dispatch(setActiveModule('routes'));
  }, [dispatch]);

  const {
    data: vehicleShippersResponse,
    isLoading: isVehicleShippersLoading,
    error: vehicleShippersError,
    refetch: refetchVehicleShippers,
  } = useGetVehicleShippersQuery(
    user?.id
      ? {
          filters: {
            shipperId: user.id,
            workingDate: todayKey,
          },
          pagination: {
            page: 0,
            size: 20,
            sortBy: 'createdStamp',
            sortDirection: 'desc',
          },
        }
      : undefined,
    {
      skip: !user?.id,
    }
  );

  const vehicleShippers = vehicleShippersResponse?.data?.items || [];

  const currentVehicleShipper = useMemo(() => {
    const availableVehicleShippers = vehicleShippers.filter(
      (item) => item.status !== 'CANCELED'
    );

    if (!availableVehicleShippers.length) {
      return null;
    }

    const activeVehicleShipper = availableVehicleShippers.find(
      (item) => item.status === 'ACTIVE'
    );

    if (activeVehicleShipper) {
      return activeVehicleShipper;
    }

    const inactivateRequestedVehicleShipper = availableVehicleShippers.find(
      (item) => item.status === 'INACTIVATE_REQUESTED'
    );

    if (inactivateRequestedVehicleShipper) {
      return inactivateRequestedVehicleShipper;
    }

    return availableVehicleShippers[0] || null;
  }, [vehicleShippers]);

  const {
    data: nextRouteStopResponse,
    isLoading: isNextRouteStopLoading,
    error: nextRouteStopError,
    refetch: refetchNextRouteStop,
  } = useGetNextRouteStopQuery(currentVehicleShipper?.id || '', {
    skip: !currentVehicleShipper?.id,
  });

  const [completeRouteStop, { isLoading: isCompletingRouteStop }] =
    useCompleteRouteStopMutation();
  const [abortRouteStop, { isLoading: isAbortingRouteStop }] =
    useAbortRouteStopMutation();

  const isSubmitting = isCompletingRouteStop || isAbortingRouteStop;

  const nextStop = (nextRouteStopResponse?.data ?? null) as RouteStop | null;

  const routeStopStatusStyle =
    nextStop && ROUTE_STOP_STATUS_STYLES[nextStop.status]
      ? ROUTE_STOP_STATUS_STYLES[nextStop.status]
      : ROUTE_STOP_STATUS_STYLES.WAITING;

  const vehicleShipperStatusStyle = currentVehicleShipper
    ? VEHICLE_SHIPPER_STATUS_STYLES[currentVehicleShipper.status]
    : VEHICLE_SHIPPER_STATUS_STYLES.ACTIVE;

  const vehicleShipperErrorMessage = vehicleShippersError
    ? getErrorMessage(vehicleShippersError)
    : '';
  const nextRouteStopErrorMessage = nextRouteStopError
    ? getErrorMessage(nextRouteStopError)
    : '';

  const handleRefreshData = () => {
    refetchVehicleShippers();

    if (currentVehicleShipper?.id) {
      refetchNextRouteStop();
    }
  };

  const handleCompleteRouteStop = async () => {
    if (!nextStop?.id) {
      return;
    }

    try {
      await completeRouteStop(nextStop.id).unwrap();
      notification.success('Đã hoàn thành điểm giao', {
        description:
          'Hệ thống đã cập nhật trạng thái và chuyển sang điểm giao kế tiếp.',
      });
      refetchNextRouteStop();
    } catch (error) {
      notification.error('Không thể hoàn thành điểm giao', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleAbortRouteStop = async () => {
    if (!nextStop?.id) {
      return;
    }

    try {
      await abortRouteStop(nextStop.id).unwrap();
      notification.success('Đã bỏ qua điểm giao', {
        description:
          'Điểm giao hiện tại đã được bỏ qua. Bạn có thể tiếp tục lộ trình.',
      });
      refetchNextRouteStop();
    } catch (error) {
      notification.error('Không thể bỏ qua điểm giao', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <section className='relative overflow-hidden rounded-3xl border border-cyan-200/70 bg-gradient-to-br from-cyan-100 via-sky-50 to-emerald-100 p-6 shadow-lg shadow-sky-100/70'>
        <div className='pointer-events-none absolute -top-24 -right-10 h-56 w-56 rounded-full bg-cyan-300/30 blur-3xl' />
        <div className='pointer-events-none absolute -bottom-20 -left-16 h-56 w-56 rounded-full bg-emerald-300/30 blur-3xl' />

        <div className='relative z-10 flex flex-wrap items-center justify-between gap-4'>
          <div>
            <p className='text-xs font-semibold uppercase tracking-[0.2em] text-cyan-700'>
              Theo dõi lộ trình
            </p>
            <h1 className='mt-1 text-2xl font-bold tracking-tight text-slate-900'>
              Điểm giao tiếp theo trong ngày
            </h1>
            <p className='mt-2 flex items-center gap-2 text-sm text-slate-700'>
              <CalendarDays className='h-4 w-4' />
              {todayLabel}
            </p>
          </div>

          <div className='flex flex-wrap items-center gap-2'>
            {currentVehicleShipper && (
              <Badge
                variant='outline'
                className={vehicleShipperStatusStyle.className}
              >
                <Truck className='mr-1 h-3.5 w-3.5' />
                {vehicleShipperStatusStyle.label}
              </Badge>
            )}

            <Button
              variant='outline'
              className='border-cyan-300 bg-white/90 text-slate-700 hover:bg-white'
              onClick={handleRefreshData}
            >
              <RefreshCcw className='mr-2 h-4 w-4' />
              Làm mới
            </Button>
          </div>
        </div>
      </section>

      {(isUserLoading || isVehicleShippersLoading) && (
        <Card className='border-slate-200'>
          <CardContent className='space-y-3 p-6'>
            <div className='h-7 w-72 animate-pulse rounded bg-slate-100' />
            <div className='h-5 w-1/2 animate-pulse rounded bg-slate-100' />
            <div className='h-40 animate-pulse rounded-xl bg-slate-100' />
          </CardContent>
        </Card>
      )}

      {!isUserLoading && !user?.id && (
        <Alert variant='destructive'>
          <AlertTitle>Không tìm thấy thông tin người dùng</AlertTitle>
          <AlertDescription>
            Không thể xác định tài xế hiện tại để truy vấn lộ trình.
          </AlertDescription>
        </Alert>
      )}

      {!isVehicleShippersLoading && vehicleShipperErrorMessage && (
        <Alert variant='destructive'>
          <AlertTitle>Không thể tải dữ liệu phân công xe</AlertTitle>
          <AlertDescription>{vehicleShipperErrorMessage}</AlertDescription>
        </Alert>
      )}

      {!isVehicleShippersLoading &&
        !vehicleShipperErrorMessage &&
        !currentVehicleShipper && (
          <Card className='border-amber-200 bg-amber-50/80'>
            <CardContent className='space-y-4 p-6'>
              <div className='flex items-center gap-2 text-amber-900'>
                <CircleOff className='h-5 w-5' />
                <h2 className='text-lg font-semibold'>
                  Hôm nay bạn chưa đăng ký phân công xe
                </h2>
              </div>
              <p className='text-sm text-amber-800'>
                Vui lòng đăng ký xe cho ngày làm việc hiện tại trước khi theo
                dõi điểm giao tiếp theo.
              </p>
              <div className='flex flex-wrap gap-3'>
                <Button asChild>
                  <Link href='/logistics2/vehicle-registration'>
                    Đăng ký xe hôm nay
                    <ArrowRight className='h-4 w-4' />
                  </Link>
                </Button>
                <Button asChild variant='outline'>
                  <Link href='/logistics2/my-routes'>Đến lộ trình của tôi</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

      {!isVehicleShippersLoading &&
        !vehicleShipperErrorMessage &&
        currentVehicleShipper &&
        isNextRouteStopLoading && (
          <Card className='border-slate-200'>
            <CardContent className='space-y-3 p-6'>
              <div className='h-7 w-72 animate-pulse rounded bg-slate-100' />
              <div className='h-5 w-1/2 animate-pulse rounded bg-slate-100' />
              <div className='h-40 animate-pulse rounded-xl bg-slate-100' />
            </CardContent>
          </Card>
        )}

      {!isNextRouteStopLoading && nextRouteStopErrorMessage && (
        <Alert variant='destructive'>
          <AlertTitle>Không thể tải điểm giao tiếp theo</AlertTitle>
          <AlertDescription>{nextRouteStopErrorMessage}</AlertDescription>
        </Alert>
      )}

      {!isNextRouteStopLoading &&
        !nextRouteStopErrorMessage &&
        currentVehicleShipper &&
        !nextStop && (
          <Card className='border-sky-200 bg-sky-50/80'>
            <CardContent className='space-y-4 p-6'>
              <div className='flex items-center gap-2 text-sky-900'>
                <Navigation className='h-5 w-5' />
                <h2 className='text-lg font-semibold'>
                  Chưa bắt đầu chuyến giao hàng nào
                </h2>
              </div>

              <p className='text-sm text-sky-800'>
                Hiện chưa có điểm giao tiếp theo. Bạn có thể mở danh sách chuyến
                hàng được giao để chọn bắt đầu một chuyến giao mới.
              </p>

              <Button asChild>
                <Link href='/logistics2/my-routes'>
                  Đi đến Lộ trình của tôi
                  <ArrowRight className='h-4 w-4' />
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}

      {!isNextRouteStopLoading &&
        !nextRouteStopErrorMessage &&
        currentVehicleShipper &&
        nextStop && (
          <Card className='border-cyan-200/80 bg-white/95 shadow-lg shadow-cyan-100/70'>
            <CardContent className='space-y-6 p-6'>
              <div className='flex flex-wrap items-start justify-between gap-4'>
                <div className='space-y-1'>
                  <p className='text-xs font-semibold uppercase tracking-[0.18em] text-cyan-700'>
                    Điểm giao kế tiếp
                  </p>
                  <h2 className='text-2xl font-bold tracking-tight text-slate-900'>
                    Lộ trình {formatShortId(nextStop.routeId)}
                  </h2>
                  <p className='text-sm text-slate-600'>
                    Điểm dừng thứ #{nextStop.sequence}
                  </p>
                </div>

                <Badge
                  variant='outline'
                  className={routeStopStatusStyle.className}
                >
                  {routeStopStatusStyle.label}
                </Badge>
              </div>

              <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
                <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-slate-500'>
                    Mã phiếu giao
                  </p>
                  <p className='mt-1 text-base font-semibold text-slate-900'>
                    {nextStop.deliverySlip?.code || '--'}
                  </p>
                </div>

                <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-slate-500'>
                    Tổng khối lượng
                  </p>
                  <p className='mt-1 text-base font-semibold text-slate-900'>
                    {nextStop.deliverySlip?.totalWeightKg || 0} kg
                  </p>
                </div>

                <div className='rounded-xl border border-slate-200 bg-slate-50/80 p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-slate-500'>
                    Tổng kích thước
                  </p>
                  <p className='mt-1 text-base font-semibold text-slate-900'>
                    {nextStop.deliverySlip?.totalVolumeCbm || 0} cbm
                  </p>
                </div>
              </div>

              <div className='grid gap-4 lg:grid-cols-2'>
                <div className='rounded-2xl border border-cyan-200 bg-cyan-50/80 p-4'>
                  <div className='mb-2 flex items-center gap-2 text-cyan-900'>
                    <MapPin className='h-4 w-4' />
                    <p className='text-sm font-semibold'>Điểm giao hàng</p>
                  </div>

                  <p className='text-sm font-medium text-slate-900'>
                    {nextStop.deliverySlip?.customerName ||
                      'Khách hàng chưa xác định'}
                  </p>
                  <p className='mt-1 text-sm text-slate-700'>
                    {nextStop.deliverySlip?.customerAddress?.fullAddress ||
                      'Chưa có địa chỉ giao hàng'}
                  </p>
                  <p className='mt-2 flex items-center gap-2 text-sm text-slate-600'>
                    <Phone className='h-3.5 w-3.5' />
                    {nextStop.deliverySlip?.customerPhone ||
                      'Chưa có số điện thoại'}
                  </p>
                </div>

                <div className='rounded-2xl border border-slate-200 bg-white p-4'>
                  <div className='mb-2 flex items-center gap-2 text-slate-700'>
                    <Truck className='h-4 w-4' />
                    <p className='text-sm font-semibold'>Thông tin phân công</p>
                  </div>

                  <div className='space-y-1.5 text-sm text-slate-700'>
                    <p>
                      Mã phân công:{' '}
                      <span className='font-medium text-slate-900'>
                        {formatShortId(currentVehicleShipper.id)}
                      </span>
                    </p>
                    <p>
                      Xe:{' '}
                      <span className='font-medium text-slate-900'>
                        {currentVehicleShipper.vehicle?.licensePlate ||
                          'Chưa có biển số'}
                      </span>
                    </p>
                    <p>
                      Ngày làm việc:{' '}
                      <span className='font-medium text-slate-900'>
                        {currentVehicleShipper.workingDate || todayKey}
                      </span>
                    </p>
                  </div>
                </div>
              </div>

              <div className='flex flex-wrap gap-3'>
                <Button asChild variant='outline'>
                  <Link href={`/logistics2/routes/${nextStop.routeId}`}>
                    <RouteIcon className='h-4 w-4' />
                    Mở chi tiết lộ trình
                  </Link>
                </Button>

                <Button
                  onClick={handleCompleteRouteStop}
                  disabled={isSubmitting}
                  className='bg-emerald-600 text-white hover:bg-emerald-700'
                >
                  {isCompletingRouteStop && (
                    <Loader2 className='h-4 w-4 animate-spin' />
                  )}
                  {!isCompletingRouteStop && (
                    <CheckCircle2 className='h-4 w-4' />
                  )}
                  Hoàn thành giao hàng
                </Button>

                <Button
                  variant='outline'
                  onClick={handleAbortRouteStop}
                  disabled={isSubmitting}
                  className='border-rose-300 text-rose-700 hover:bg-rose-50'
                >
                  {isAbortingRouteStop && (
                    <Loader2 className='h-4 w-4 animate-spin' />
                  )}
                  {!isAbortingRouteStop && <SkipForward className='h-4 w-4' />}
                  Bỏ qua điểm này
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      {currentVehicleShipper && (
        <VehicleShipperCard shipper={currentVehicleShipper} />
      )}
    </div>
  );
};
