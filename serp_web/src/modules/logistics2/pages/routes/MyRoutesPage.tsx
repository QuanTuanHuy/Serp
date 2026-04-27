/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 my routes page
*/

'use client';

import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import { addDays, format, isValid, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import {
  ArrowRight,
  CalendarClock,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Navigation,
  RefreshCcw,
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
  Input,
} from '@/shared/components/ui';
import { Calendar } from '@/shared/components/ui/calendar';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import {
  useCancelRouteMutation,
  useGetRoutesQuery,
  useGetVehicleShippersQuery,
  useSelectRouteForDeliveryMutation,
} from '../../api/logistics2Api';
import { RouteCard } from '../../components/cards/RouteCard';
import { setActiveModule } from '../../store';
import type { RouteStatus, VehicleShipperStatus } from '../../types';
import { VehicleShipperCard } from '../../components/cards/VehicleShipperCard';

const ROUTE_STATUS_PRIORITY: Record<RouteStatus, number> = {
  IN_PROGRESS: 0,
  PENDING: 1,
  COMPLETED: 2,
  ABORTED: 3,
};

const toDateKey = (date: Date) => format(date, 'yyyy-MM-dd');

const parseDateKey = (dateKey: string) => {
  const parsed = parseISO(dateKey);

  return isValid(parsed) ? parsed : new Date();
};

export const MyRoutesPage = () => {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const { user, isLoading: isUserLoading } = useUser();
  const [selectRouteForDelivery] = useSelectRouteForDeliveryMutation();
  const [cancelRoute] = useCancelRouteMutation();

  const todayKey = useMemo(() => toDateKey(new Date()), []);
  const [selectedDateKey, setSelectedDateKey] = useState(todayKey);
  const [isDatePickerOpen, setIsDatePickerOpen] = useState(false);
  const [startingRouteId, setStartingRouteId] = useState<string | null>(null);
  const [cancelingRouteId, setCancelingRouteId] = useState<string | null>(null);

  const selectedDate = useMemo(
    () => parseDateKey(selectedDateKey),
    [selectedDateKey]
  );
  const selectedDateLabel = useMemo(
    () => format(selectedDate, 'EEEE, dd/MM/yyyy', { locale: vi }),
    [selectedDate]
  );
  const isViewingToday = selectedDateKey === todayKey;

  useEffect(() => {
    dispatch(setActiveModule('routes'));
  }, [dispatch]);

  const handleShiftDate = (offsetDays: number) => {
    setSelectedDateKey((previousDateKey) =>
      toDateKey(addDays(parseDateKey(previousDateKey), offsetDays))
    );
  };

  const handleSelectDate = (date?: Date) => {
    if (!date) {
      return;
    }

    setSelectedDateKey(toDateKey(date));
    setIsDatePickerOpen(false);
  };

  const {
    data: vehicleShippersResponse,
    isLoading: isVehicleShippersLoading,
    isFetching: isVehicleShippersFetching,
    error: vehicleShippersError,
    refetch: refetchVehicleShippers,
  } = useGetVehicleShippersQuery(
    user?.id
      ? {
          filters: {
            shipperId: user.id,
            workingDate: selectedDateKey,
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

  const vehicleShippers = vehicleShippersResponse?.data?.items ?? [];

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
    data: routesResponse,
    isLoading: isRoutesLoading,
    isFetching: isRoutesFetching,
    error: routesError,
    refetch: refetchRoutes,
  } = useGetRoutesQuery(
    currentVehicleShipper?.id
      ? {
          filters: {
            vehicleShipperId: currentVehicleShipper.id,
            deliveryDate: selectedDateKey,
          },
          pagination: {
            page: 0,
            size: 50,
            sortBy: 'createdStamp',
            sortDirection: 'desc',
          },
        }
      : undefined,
    {
      skip: !currentVehicleShipper?.id,
    }
  );

  const isFetchingData = isVehicleShippersFetching || isRoutesFetching;

  const routes = currentVehicleShipper
    ? (routesResponse?.data?.items ?? [])
    : [];

  const orderedRoutes = useMemo(
    () =>
      [...routes].sort(
        (left, right) =>
          ROUTE_STATUS_PRIORITY[left.status] -
          ROUTE_STATUS_PRIORITY[right.status]
      ),
    [routes]
  );

  const inProgressRoutes = useMemo(
    () => orderedRoutes.filter((item) => item.status === 'IN_PROGRESS'),
    [orderedRoutes]
  );

  const otherRoutes = useMemo(
    () => orderedRoutes.filter((item) => item.status !== 'IN_PROGRESS'),
    [orderedRoutes]
  );

  const hasInProgressRoute = inProgressRoutes.length > 0;

  const routeStats = useMemo(
    () => ({
      total: orderedRoutes.length,
      inProgress: orderedRoutes.filter((item) => item.status === 'IN_PROGRESS')
        .length,
      pending: orderedRoutes.filter((item) => item.status === 'PENDING').length,
      completed: orderedRoutes.filter((item) => item.status === 'COMPLETED')
        .length,
    }),
    [orderedRoutes]
  );

  const vehicleShipperErrorMessage = vehicleShippersError
    ? getErrorMessage(vehicleShippersError)
    : '';
  const routesErrorMessage = routesError ? getErrorMessage(routesError) : '';

  const handleRefreshData = () => {
    refetchVehicleShippers();

    if (currentVehicleShipper?.id) {
      refetchRoutes();
    }
  };

  const handleOpenRouteDetail = (routeId: string) => {
    router.push(`/logistics2/routes/${routeId}`);
  };

  const handleStartRoute = async (routeId: string) => {
    setStartingRouteId(routeId);

    try {
      await selectRouteForDelivery(routeId).unwrap();
      notification.success('Đã bắt đầu lộ trình', {
        description: 'Lộ trình được chuyển sang trạng thái đang giao.',
      });
      refetchRoutes();
    } catch (error) {
      notification.error('Không thể bắt đầu lộ trình', {
        description: getErrorMessage(error),
      });
    } finally {
      setStartingRouteId(null);
    }
  };

  const handleCancelRoute = async (routeId: string) => {
    if (
      !window.confirm('Bạn có chắc chắn muốn hủy chuyến giao hàng này không?')
    ) {
      return;
    }

    setCancelingRouteId(routeId);

    try {
      await cancelRoute(routeId).unwrap();
      notification.success('Đã hủy chuyến giao', {
        description: 'Tất cả đơn hàng trong chuyến giao cần được trả về kho.',
      });
      refetchRoutes();
    } catch (error) {
      notification.error('Không thể hủy chuyến giao', {
        description: getErrorMessage(error),
      });
    } finally {
      setCancelingRouteId(null);
    }
  };

  return (
    <div className='space-y-6'>
      <section className='relative overflow-hidden rounded-3xl border border-emerald-200/70 bg-gradient-to-br from-emerald-100 via-teal-50 to-cyan-100 p-6 shadow-lg shadow-emerald-100/70'>
        <div className='pointer-events-none absolute -top-20 -right-12 h-56 w-56 rounded-full bg-cyan-300/30 blur-3xl' />
        <div className='pointer-events-none absolute -bottom-20 -left-16 h-56 w-56 rounded-full bg-emerald-300/35 blur-3xl' />

        <div className='relative z-10 flex flex-wrap items-center justify-between gap-4'>
          <div>
            <p className='text-xs font-semibold uppercase tracking-[0.2em] text-emerald-700'>
              Lộ trình của tôi
            </p>
            <h1 className='mt-1 text-2xl font-bold tracking-tight text-slate-900'>
              Danh sách lộ trình theo ngày phân công
            </h1>
            <p className='mt-2 flex items-center gap-2 text-sm text-slate-700'>
              <CalendarDays className='h-4 w-4' />
              {selectedDateLabel}
            </p>
          </div>

          <div className='flex flex-wrap items-center gap-2'>
            <Button asChild>
              <Link href='/logistics2/next-route'>
                Theo dõi điểm giao tiếp theo
                <ArrowRight className='h-4 w-4' />
              </Link>
            </Button>

            <Button
              variant='outline'
              className='border-emerald-300 bg-white/90 text-slate-700 hover:bg-white'
              onClick={handleRefreshData}
              disabled={isFetchingData}
            >
              <RefreshCcw
                className={`mr-2 h-4 w-4 ${isFetchingData ? 'animate-spin text-emerald-600' : ''}`}
              />
              {isFetchingData ? 'Đang tải...' : 'Làm mới'}
            </Button>
          </div>
        </div>
      </section>

      <Card className='border-teal-200/70 bg-gradient-to-r from-teal-50/90 via-white to-cyan-50/90'>
        <CardContent className='space-y-3 p-4'>
          <div className='flex items-center gap-2 text-sm font-medium text-teal-900'>
            <CalendarClock className='h-4 w-4' />
            Điều hướng ngày làm việc
          </div>

          <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
            <div className='flex flex-wrap items-center gap-2'>
              <Button
                variant='outline'
                size='sm'
                className='border-teal-200 bg-white'
                onClick={() => handleShiftDate(-1)}
              >
                <ChevronLeft className='h-4 w-4' />
                Ngày trước
              </Button>

              <Button
                variant='outline'
                size='sm'
                className='border-teal-200 bg-white'
                onClick={() => handleShiftDate(1)}
              >
                Ngày sau
                <ChevronRight className='h-4 w-4' />
              </Button>

              <Button
                size='sm'
                variant={isViewingToday ? 'default' : 'secondary'}
                onClick={() => setSelectedDateKey(todayKey)}
              >
                Hôm nay
              </Button>
            </div>

            <div className='flex flex-wrap items-center gap-2'>
              <Popover
                open={isDatePickerOpen}
                onOpenChange={setIsDatePickerOpen}
              >
                <PopoverTrigger asChild>
                  <Button
                    variant='outline'
                    size='sm'
                    className='min-w-[220px] justify-start border-teal-200 bg-white text-left'
                  >
                    <CalendarDays className='h-4 w-4' />
                    {selectedDateLabel}
                  </Button>
                </PopoverTrigger>
                <PopoverContent align='end' className='w-auto p-0'>
                  <Calendar
                    mode='single'
                    selected={selectedDate}
                    onSelect={handleSelectDate}
                    locale={vi}
                  />
                </PopoverContent>
              </Popover>
            </div>
          </div>
        </CardContent>
      </Card>

      {(isUserLoading || isVehicleShippersFetching) && (
        <Card className='border-slate-200'>
          <CardContent className='space-y-3 p-6'>
            <div className='h-7 w-72 animate-pulse rounded bg-slate-100' />
            <div className='h-5 w-1/2 animate-pulse rounded bg-slate-100' />
            <div className='h-32 animate-pulse rounded-xl bg-slate-100' />
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

      {!isVehicleShippersFetching && vehicleShipperErrorMessage && (
        <Alert variant='destructive'>
          <AlertTitle>Không thể tải phân công xe</AlertTitle>
          <AlertDescription>{vehicleShipperErrorMessage}</AlertDescription>
        </Alert>
      )}

      {!isVehicleShippersFetching &&
        !vehicleShipperErrorMessage &&
        !currentVehicleShipper && (
          <Card className='border-amber-200 bg-amber-50/80'>
            <CardContent className='space-y-4 p-6'>
              <div className='flex items-center gap-2 text-amber-900'>
                <Truck className='h-5 w-5' />
                <h2 className='text-lg font-semibold'>
                  Chưa có phân công xe cho ngày đã chọn
                </h2>
              </div>

              <p className='text-sm text-amber-800'>
                Bạn chưa có phân công xe ngày {selectedDateKey}. Hãy đăng ký xe
                làm việc để hệ thống phân bổ lộ trình.
              </p>

              <Button asChild>
                <Link href='/logistics2/vehicle-registration'>
                  Đăng ký xe ngay
                  <ArrowRight className='h-4 w-4' />
                </Link>
              </Button>
            </CardContent>
          </Card>
        )}

      {!isRoutesFetching && routesErrorMessage && (
        <Alert variant='destructive'>
          <AlertTitle>Không thể tải danh sách lộ trình</AlertTitle>
          <AlertDescription>{routesErrorMessage}</AlertDescription>
        </Alert>
      )}

      {isRoutesFetching &&
        currentVehicleShipper &&
        !routesErrorMessage &&
        !isVehicleShippersFetching && (
          <Card className='border-slate-200'>
            <CardContent className='space-y-3 p-6'>
              <div className='h-7 w-72 animate-pulse rounded bg-slate-100' />
              <div className='h-5 w-1/2 animate-pulse rounded bg-slate-100' />
              <div className='h-32 animate-pulse rounded-xl bg-slate-100' />
            </CardContent>
          </Card>
        )}

      {!isRoutesFetching &&
        !routesErrorMessage &&
        currentVehicleShipper &&
        orderedRoutes.length === 0 && (
          <Card className='border-sky-200 bg-sky-50/80'>
            <CardContent className='space-y-4 p-6'>
              <div className='flex items-center gap-2 text-sky-900'>
                <Navigation className='h-5 w-5' />
                <h2 className='text-lg font-semibold'>
                  Chưa có chuyến hàng nào được giao
                </h2>
              </div>
              <p className='text-sm text-sky-800'>
                Hệ thống chưa phân bổ chuyến hàng nào cho xe của bạn vào ngày đã
                chọn. Hãy thử đổi ngày hoặc quay lại sau.
              </p>
            </CardContent>
          </Card>
        )}

      {!isRoutesFetching &&
        !routesErrorMessage &&
        currentVehicleShipper &&
        orderedRoutes.length > 0 && (
          <>
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
              <Card className='border-slate-200/80 bg-white/90'>
                <CardContent className='p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-slate-500'>
                    Tổng chuyến hàng
                  </p>
                  <p className='mt-1 text-2xl font-semibold text-slate-900'>
                    {routeStats.total}
                  </p>
                </CardContent>
              </Card>

              <Card className='border-sky-200 bg-sky-50/80'>
                <CardContent className='p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-sky-700'>
                    Đang giao
                  </p>
                  <p className='mt-1 text-2xl font-semibold text-sky-800'>
                    {routeStats.inProgress}
                  </p>
                </CardContent>
              </Card>

              <Card className='border-amber-200 bg-amber-50/80'>
                <CardContent className='p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-amber-700'>
                    Chưa khởi hàng
                  </p>
                  <p className='mt-1 text-2xl font-semibold text-amber-800'>
                    {routeStats.pending}
                  </p>
                </CardContent>
              </Card>

              <Card className='border-emerald-200 bg-emerald-50/80'>
                <CardContent className='p-4'>
                  <p className='text-xs font-medium uppercase tracking-wide text-emerald-700'>
                    Hoàn tất
                  </p>
                  <p className='mt-1 text-2xl font-semibold text-emerald-800'>
                    {routeStats.completed}
                  </p>
                </CardContent>
              </Card>
            </div>

            <div className='space-y-5'>
              <section className='space-y-3'>
                <div className='flex items-center justify-between rounded-xl border border-sky-200/70 bg-sky-50/80 px-4 py-3'>
                  <div>
                    <p className='text-xs font-semibold uppercase tracking-[0.14em] text-sky-700'>
                      Lộ trình đang giao
                    </p>
                  </div>
                  <Badge
                    variant='outline'
                    className='border-sky-200 text-sky-700'
                  >
                    {inProgressRoutes.length} lộ trình
                  </Badge>
                </div>

                {inProgressRoutes.length > 0 && (
                  <div className='space-y-3'>
                    {inProgressRoutes.map((route) => (
                      <RouteCard
                        key={route.id}
                        route={route}
                        onOpenDetail={handleOpenRouteDetail}
                        showCancelButton={route.status === 'IN_PROGRESS'}
                        onCancelRoute={handleCancelRoute}
                        isCancelLoading={cancelingRouteId === route.id}
                      />
                    ))}
                  </div>
                )}
              </section>

              <section className='space-y-3'>
                <div className='flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/80 px-4 py-3'>
                  <div>
                    <p className='text-xs font-semibold uppercase tracking-[0.14em] text-slate-500'>
                      Danh sách chuyến hàng còn lại
                    </p>
                  </div>
                  <Badge
                    variant='outline'
                    className='border-slate-300 text-slate-700'
                  >
                    {otherRoutes.length} lộ trình
                  </Badge>
                </div>

                {otherRoutes.length > 0 ? (
                  <div className='space-y-3'>
                    {otherRoutes.map((route) => (
                      <RouteCard
                        key={route.id}
                        route={route}
                        onOpenDetail={handleOpenRouteDetail}
                        showStartButton={
                          !hasInProgressRoute && route.status === 'PENDING'
                        }
                        onStartRoute={handleStartRoute}
                        isStartLoading={startingRouteId === route.id}
                        showCancelButton={route.status === 'PENDING'}
                        onCancelRoute={handleCancelRoute}
                        isCancelLoading={cancelingRouteId === route.id}
                      />
                    ))}
                  </div>
                ) : (
                  <Card className='border-dashed border-slate-200 bg-slate-50/40'>
                    <CardContent className='p-4 text-sm text-slate-600'>
                      Không còn lộ trình nào ngoài các lộ trình đang giao.
                    </CardContent>
                  </Card>
                )}
              </section>
            </div>
          </>
        )}

      {!isVehicleShippersFetching && currentVehicleShipper && (
        <VehicleShipperCard shipper={currentVehicleShipper} />
      )}
    </div>
  );
};
