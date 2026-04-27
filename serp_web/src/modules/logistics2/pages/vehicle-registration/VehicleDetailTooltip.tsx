'use client';

/*
Author: QuanTuanHuy
Description: Part of Serp Project - Vehicle detail tooltip
*/

import { useEffect, useState } from 'react';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components/ui';
import type { VehicleShipper, VehicleStatus } from '../../types';

interface VehicleDetailTooltipProps {
  registration: VehicleShipper;
  children: React.ReactElement;
}

const VEHICLE_STATUS_LABELS: Record<VehicleStatus, string> = {
  IN_USE: 'Đang sử dụng',
  MAINTENANCE: 'Bảo trì',
};

const formatVehicleType = (vehicleType?: string) => {
  if (!vehicleType) return 'Xe không xác định';

  switch (vehicleType.toUpperCase()) {
    case 'MOTORBIKE':
      return 'Xe máy';
    case 'VAN_500KG':
      return 'Xe ben 500kg';
    case 'VAN_1000KG':
      return 'Xe ben 1 tấn';
    case 'LIGHT_TRUCK_1T':
      return 'Xe tải 1 tấn';
    case 'LIGHT_TRUCK_2T':
      return 'Xe tải 2 tấn';
    default:
      return 'Xe không xác định';
  }
};

const formatDate = (dateValue?: string) => {
  if (!dateValue) return 'Không rõ';

  const parsed = parseISO(dateValue);

  if (!Number.isNaN(parsed.getTime())) {
    return format(parsed, 'dd/MM/yyyy', { locale: vi });
  }

  const fallback = new Date(dateValue);

  if (!Number.isNaN(fallback.getTime())) {
    return format(fallback, 'dd/MM/yyyy', { locale: vi });
  }

  return dateValue;
};

const formatNumber = (value?: number, suffix?: string) => {
  if (value == null) return 'Không rõ';

  const formatted = new Intl.NumberFormat('vi-VN').format(value);

  return suffix ? `${formatted} ${suffix}` : formatted;
};

export const VehicleDetailTooltip: React.FC<VehicleDetailTooltipProps> = ({
  registration,
  children,
}) => {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const mediaQuery = window.matchMedia('(max-width: 767px)');

    const updateIsMobile = () => {
      setIsMobile(mediaQuery.matches);
    };

    updateIsMobile();

    if (typeof mediaQuery.addEventListener === 'function') {
      mediaQuery.addEventListener('change', updateIsMobile);

      return () => {
        mediaQuery.removeEventListener('change', updateIsMobile);
      };
    }

    mediaQuery.addListener(updateIsMobile);

    return () => {
      mediaQuery.removeListener(updateIsMobile);
    };
  }, []);

  const vehicle = registration.vehicle;
  const vehicleLabel = vehicle?.licensePlate || registration.vehicleId;
  const vehicleStatus = vehicle
    ? VEHICLE_STATUS_LABELS[vehicle.status]
    : 'Không rõ';

  const detailContent = (
    <div className='max-w-full space-y-2'>
      <div>
        <p className='truncate text-sm font-semibold text-slate-900'>
          {vehicleLabel}
        </p>
        <p className='truncate text-xs text-slate-500'>
          {formatVehicleType(vehicle?.vehicleType)}
        </p>
      </div>

      <div className='grid grid-cols-[110px_minmax(0,1fr)] gap-y-1 text-xs'>
        <span className='text-slate-500'>Mã phương tiện</span>
        <span className='block min-w-0 truncate font-medium text-slate-800'>
          {registration.vehicleId}
        </span>

        <span className='text-slate-500'>Trạng thái xe</span>
        <span className='block min-w-0 truncate font-medium text-slate-800'>
          {vehicleStatus}
        </span>

        <span className='text-slate-500'>Ngày sử dụng</span>
        <span className='block min-w-0 truncate font-medium text-slate-800'>
          {formatDate(registration.workingDate)}
        </span>

        <span className='text-slate-500'>Tải trọng tối đa</span>
        <span className='block min-w-0 truncate font-medium text-slate-800'>
          {formatNumber(vehicle?.maxWeightKg, 'kg')}
        </span>

        <span className='text-slate-500'>Thể tích tối đa</span>
        <span className='block min-w-0 truncate font-medium text-slate-800'>
          {formatNumber(vehicle?.maxVolumeCbm, 'cbm')}
        </span>
      </div>
    </div>
  );

  const triggerElement = (
    <span
      className='block'
      onClick={(event) => {
        event.stopPropagation();
      }}
      onPointerDown={(event) => {
        event.stopPropagation();
      }}
      onKeyDown={(event) => {
        event.stopPropagation();
      }}
    >
      {children}
    </span>
  );

  if (isMobile) {
    return (
      <Popover>
        <PopoverTrigger asChild>{triggerElement}</PopoverTrigger>
        <PopoverContent
          side='top'
          align='start'
          sideOffset={8}
          className='w-72 rounded-xl border border-slate-200 bg-white p-3 text-slate-700 shadow-xl'
        >
          {detailContent}
        </PopoverContent>
      </Popover>
    );
  }

  return (
    <Tooltip>
      <TooltipTrigger asChild>{triggerElement}</TooltipTrigger>

      <TooltipContent
        side='top'
        align='start'
        sideOffset={8}
        className='w-72 rounded-xl border border-slate-200 bg-white p-3 text-slate-700 shadow-xl'
      >
        {detailContent}
      </TooltipContent>
    </Tooltip>
  );
};
