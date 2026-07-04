'use client';

import { useEffect, useMemo, useState } from 'react';
import {
  addMonths,
  addDays,
  endOfMonth,
  endOfWeek,
  format,
  isAfter,
  isBefore,
  isSameMonth,
  parseISO,
  startOfDay,
  startOfMonth,
  startOfWeek,
  isSameDay,
} from 'date-fns';
import { vi } from 'date-fns/locale';
import { ChevronLeft, ChevronRight, RefreshCcw } from 'lucide-react';
import { getErrorMessage } from '@/lib/store/api';
import { useAppDispatch } from '@/lib/store';
import {
  Alert,
  AlertDescription,
  Badge,
  Button,
  Card,
  CardContent,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import {
  useAssignVehicleShipperMutation,
  useGetVehiclesForUsageQuery,
  useGetVehicleShippersQuery,
} from '../../api/logistics2Api';
import { setActiveModule } from '../../store';
import type { VehicleShipper } from '../../types';
import { VehicleRegistrationCalendar } from './VehicleRegistrationCalendar';
import { VehicleRegistrationDialog } from './VehicleRegistrationDialog';
import type { VehicleRegistrationDay } from './vehicleRegistration.types';
import { useUser } from '@/modules/account/hooks/useUser';

const DATE_KEY_FORMAT = 'yyyy-MM-dd';

const parseWorkingDate = (value: string) => {
  const parsed = parseISO(value);

  if (!Number.isNaN(parsed.getTime())) {
    return startOfDay(parsed);
  }

  const fallback = new Date(value);

  if (Number.isNaN(fallback.getTime())) {
    return null;
  }

  return startOfDay(fallback);
};

const toTimeValue = (stamp: string) => {
  const parsed = new Date(stamp).getTime();

  return Number.isNaN(parsed) ? 0 : parsed;
};

const buildCalendarDays = (
  month: Date,
  registrationsByDate: Map<string, VehicleShipper[]>
): VehicleRegistrationDay[] => {
  const monthStart = startOfMonth(month);
  const monthEnd = endOfMonth(month);
  const gridStart = startOfWeek(monthStart, { weekStartsOn: 1 });
  const gridEnd = endOfWeek(monthEnd, { weekStartsOn: 1 });
  const today = startOfDay(new Date());
  const days: VehicleRegistrationDay[] = [];

  for (let cursor = gridStart; cursor <= gridEnd; cursor = addDays(cursor, 1)) {
    const dateKey = format(cursor, DATE_KEY_FORMAT);
    const registrations = [...(registrationsByDate.get(dateKey) || [])].sort(
      (left, right) =>
        toTimeValue(left.createdStamp) - toTimeValue(right.createdStamp)
    );
    const activeRegistrations = registrations.filter(
      (registration) => registration.status !== 'CANCELED'
    );

    days.push({
      date: cursor,
      dateKey,
      isCurrentMonth: isSameMonth(cursor, month),
      isPast: isBefore(cursor, today),
      canRegister:
        activeRegistrations.length === 0 &&
        (isSameDay(cursor, today) || isAfter(cursor, today)),
      registrations,
    });
  }

  return days;
};

export const VehicleRegistrationPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const { user } = useUser();

  const [displayMonth, setDisplayMonth] = useState(() =>
    startOfMonth(new Date())
  );
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedDay, setSelectedDay] = useState<VehicleRegistrationDay | null>(
    null
  );
  const [selectedVehicleId, setSelectedVehicleId] = useState('');

  useEffect(() => {
    dispatch(setActiveModule('vehicle-registration'));
  }, [dispatch]);

  const {
    data: vehiclesData,
    isLoading: isVehiclesLoading,
    error: vehiclesError,
    refetch: refetchVehiclesForUsage,
  } = useGetVehiclesForUsageQuery(
    {
      filters: {
        workingDate: selectedDay?.dateKey,
      },
      pagination: {
        page: 0,
        size: 200,
        sortBy: 'createdStamp',
        sortDirection: 'desc',
      },
    },
    {
      skip: !selectedDay,
    }
  );

  const {
    data: vehicleShippersData,
    isLoading: isVehicleShippersLoading,
    error: vehicleShippersError,
    refetch: refetchVehicleShippers,
  } = useGetVehicleShippersQuery({
    filters: {
      shipperId: user?.id || 0,
    },
    pagination: {
      page: 0,
      size: 500,
      sortBy: 'workingDate',
      sortDirection: 'asc',
    },
  });

  const [assignVehicleShipper, { isLoading: isSubmitting }] =
    useAssignVehicleShipperMutation();

  const vehicles = vehiclesData?.data?.items || [];
  const vehicleShippers = vehicleShippersData?.data?.items || [];

  const registrationsByDate = useMemo(() => {
    const mapped = new Map<string, VehicleShipper[]>();

    vehicleShippers.forEach((registration) => {
      const parsedDate = parseWorkingDate(registration.workingDate);

      if (!parsedDate) return;

      const dateKey = format(parsedDate, DATE_KEY_FORMAT);
      const existing = mapped.get(dateKey) || [];
      existing.push(registration);
      mapped.set(dateKey, existing);
    });

    return mapped;
  }, [vehicleShippers]);

  const calendarDays = useMemo(
    () => buildCalendarDays(displayMonth, registrationsByDate),
    [displayMonth, registrationsByDate]
  );

  const monthRegistrations = useMemo(() => {
    const monthKey = format(displayMonth, 'yyyy-MM');

    return Array.from(registrationsByDate.entries())
      .filter(([dateKey]) => dateKey.startsWith(monthKey))
      .flatMap(([, registrations]) => registrations);
  }, [displayMonth, registrationsByDate]);

  const monthlyStats = useMemo(
    () => ({
      total: monthRegistrations.length,
      active: monthRegistrations.filter((item) => item.status === 'ACTIVE')
        .length,
      requestCancel: monthRegistrations.filter(
        (item) => item.status === 'INACTIVATE_REQUESTED'
      ).length,
      canceled: monthRegistrations.filter((item) => item.status === 'CANCELED')
        .length,
    }),
    [monthRegistrations]
  );

  const mergedErrorMessage = useMemo(() => {
    if (vehicleShippersError) {
      return getErrorMessage(vehicleShippersError);
    }

    if (vehiclesError) {
      return getErrorMessage(vehiclesError);
    }

    return '';
  }, [vehicleShippersError, vehiclesError]);

  const handleSelectDay = (day: VehicleRegistrationDay) => {
    if (!day.canRegister) return;

    setSelectedDay(day);
    setSelectedVehicleId('');
    setDialogOpen(true);
  };

  const handleSubmitRegistration = async () => {
    if (!selectedDay || !selectedVehicleId) return;

    try {
      await assignVehicleShipper({
        vehicleId: selectedVehicleId,
        workingDay: selectedDay.dateKey,
      }).unwrap();

      notification.success('Đăng ký xe thành công', {
        description: `Ngày ${format(selectedDay.date, 'dd/MM/yyyy', { locale: vi })} đã được cập nhật.`,
      });

      setDialogOpen(false);
      setSelectedVehicleId('');
      setSelectedDay(null);
      refetchVehicleShippers();
    } catch (error) {
      notification.error('Không thể đăng ký xe', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleDialogOpenChange = (open: boolean) => {
    setDialogOpen(open);

    if (!open) {
      setSelectedVehicleId('');
      setSelectedDay(null);
    }
  };

  const monthLabel = format(displayMonth, "'Tháng' M yyyy", { locale: vi });

  return (
    <div className='space-y-6'>
      <section className='relative overflow-hidden rounded-3xl border border-sky-200/70 bg-gradient-to-br from-sky-100 via-cyan-50 to-emerald-100 p-6 shadow-lg shadow-cyan-100/60'>
        <div className='pointer-events-none absolute -top-24 -right-20 h-56 w-56 rounded-full bg-cyan-300/30 blur-3xl' />
        <div className='pointer-events-none absolute -bottom-24 -left-16 h-56 w-56 rounded-full bg-emerald-300/30 blur-3xl' />

        <div className='relative z-10 space-y-5'>
          <div className='flex flex-wrap items-center justify-between gap-3'>
            <div>
              <h1 className='text-2xl font-bold tracking-tight text-slate-800'>
                Lịch đăng ký xe giao hàng
              </h1>
              <p className='mt-1 max-w-3xl text-sm text-slate-700'>
                Vui lòng đăng ký xe theo ngày công
              </p>
            </div>

            <Button
              variant='outline'
              className='border-cyan-300 bg-white/90 text-slate-700 hover:bg-white'
              onClick={() => {
                refetchVehicleShippers();

                if (selectedDay) {
                  refetchVehiclesForUsage();
                }
              }}
            >
              <RefreshCcw className='mr-2 h-4 w-4' />
              Làm mới dữ liệu
            </Button>
          </div>

          <div className='grid gap-3 sm:grid-cols-2 xl:grid-cols-3'>
            <Card className='border-white/60 bg-white/70 shadow-sm backdrop-blur'>
              <CardContent className='p-4'>
                <p className='text-xs font-medium uppercase tracking-wide text-slate-500'>
                  Số xe đăng ký trong tháng
                </p>
                <p className='mt-1 text-2xl font-semibold text-slate-800'>
                  {monthlyStats.total}
                </p>
              </CardContent>
            </Card>

            <Card className='border-amber-200 bg-amber-50/90 shadow-sm'>
              <CardContent className='p-4'>
                <p className='text-xs font-medium uppercase tracking-wide text-amber-700'>
                  Số xe đang chờ hủy
                </p>
                <p className='mt-1 text-2xl font-semibold text-amber-800'>
                  {monthlyStats.requestCancel}
                </p>
              </CardContent>
            </Card>

            <Card className='border-red-200 bg-red-50/90 shadow-sm'>
              <CardContent className='p-4'>
                <p className='text-xs font-medium uppercase tracking-wide text-red-700'>
                  Số xe đã hủy
                </p>
                <p className='mt-1 text-2xl font-semibold text-red-800'>
                  {monthlyStats.canceled}
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {mergedErrorMessage && (
        <Alert variant='destructive'>
          <AlertDescription>
            Không thể tải dữ liệu lịch đăng ký xe: {mergedErrorMessage}
          </AlertDescription>
        </Alert>
      )}

      <Card className='border-slate-200/80 shadow-lg shadow-slate-200/60'>
        <CardContent className='flex flex-wrap items-center justify-between gap-3 p-4'>
          <Button
            type='button'
            variant='outline'
            onClick={() =>
              setDisplayMonth((current) => startOfMonth(addMonths(current, -1)))
            }
          >
            <ChevronLeft className='mr-2 h-4 w-4' />
            Tháng trước
          </Button>

          <p className='text-sm font-semibold uppercase tracking-wide text-slate-700'>
            {monthLabel}
          </p>

          <Button
            type='button'
            variant='outline'
            onClick={() =>
              setDisplayMonth((current) => startOfMonth(addMonths(current, 1)))
            }
          >
            Tháng sau
            <ChevronRight className='ml-2 h-4 w-4' />
          </Button>
        </CardContent>
      </Card>

      <VehicleRegistrationCalendar
        days={calendarDays}
        isLoading={isVehicleShippersLoading}
        onSelectDay={handleSelectDay}
      />

      <VehicleRegistrationDialog
        open={dialogOpen}
        onOpenChange={handleDialogOpenChange}
        selectedDay={selectedDay}
        vehicles={vehicles}
        selectedVehicleId={selectedVehicleId}
        isVehiclesLoading={isVehiclesLoading}
        isSubmitting={isSubmitting}
        onSelectedVehicleIdChange={setSelectedVehicleId}
        onSubmit={handleSubmitRegistration}
      />
    </div>
  );
};
