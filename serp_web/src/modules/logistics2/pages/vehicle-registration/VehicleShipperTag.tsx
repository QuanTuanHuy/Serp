/*
Author: QuanTuanHuy
Description: Part of Serp Project - Vehicle registration status card
*/

import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { VehicleDetailTooltip } from './VehicleDetailTooltip';
import type { VehicleShipper, VehicleShipperStatus } from '../../types';

interface VehicleShipperTagProps {
  registration: VehicleShipper;
  isPastDay: boolean;
}

interface StatusMeta {
  label: string;
  containerClassName: string;
  badgeClassName: string;
}

const STATUS_META: Record<VehicleShipperStatus, StatusMeta> = {
  ACTIVE: {
    label: 'Đang hoạt động',
    containerClassName: 'border-emerald-300 bg-emerald-50 text-emerald-900',
    badgeClassName: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  },
  INACTIVATE_REQUESTED: {
    label: 'Đã yêu cầu hủy',
    containerClassName: 'border-amber-300 bg-amber-50 text-amber-900',
    badgeClassName: 'bg-amber-100 text-amber-800 border-amber-200',
  },
  CANCELED: {
    label: 'Đã hủy',
    containerClassName: 'border-red-300 bg-red-50 text-red-900',
    badgeClassName: 'bg-red-100 text-red-800 border-red-200',
  },
};

const PAST_META: StatusMeta = {
  label: 'Hết hạn',
  containerClassName: 'border-slate-300 bg-slate-100 text-slate-700',
  badgeClassName: 'bg-slate-200 text-slate-700 border-slate-300',
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

export const VehicleShipperTag: React.FC<VehicleShipperTagProps> = ({
  registration,
  isPastDay,
}) => {
  const meta = isPastDay ? PAST_META : STATUS_META[registration.status];
  const vehicleLabel =
    registration.vehicle?.licensePlate || registration.vehicleId;

  return (
    <VehicleDetailTooltip registration={registration}>
      <article
        className={cn(
          'rounded-lg border px-2 py-1.5 text-[11px] shadow-sm transition-colors',
          meta.containerClassName
        )}
      >
        <div className='flex items-start justify-between gap-2'>
          <div className='min-w-0'>
            <p className='truncate font-semibold'>{vehicleLabel}</p>
            <p className='truncate text-[10px] uppercase tracking-wide opacity-80'>
              {formatVehicleType(registration.vehicle?.vehicleType)}
            </p>
          </div>
          <Badge
            variant='outline'
            className={cn(
              'h-5 shrink-0 border px-1.5 text-[9px] font-semibold',
              meta.badgeClassName
            )}
          >
            {meta.label}
          </Badge>
        </div>
      </article>
    </VehicleDetailTooltip>
  );
};
