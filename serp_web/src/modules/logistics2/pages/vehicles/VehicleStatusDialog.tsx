'use client';

import { Loader2, PauseCircle, PlayCircle } from 'lucide-react';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import type { Vehicle, VehicleStatus, VehicleType } from '../../types';

export type VehicleStatusAction = 'activate' | 'deactivate';

interface VehicleStatusDialogProps {
  open: boolean;
  action: VehicleStatusAction;
  vehicle: Vehicle | null;
  isSubmitting: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
}

const STATUS_LABELS: Record<VehicleStatus, string> = {
  IN_USE: 'Dang hoat dong',
  MAINTENANCE: 'Bao tri',
};

const STATUS_BADGE_STYLES: Record<VehicleStatus, string> = {
  IN_USE: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  MAINTENANCE: 'border-amber-200 bg-amber-50 text-amber-700',
};

const VEHICLE_TYPE_LABELS: Record<VehicleType, string> = {
  MOTORBIKE: 'Xe máy',
  VAN_500KG: 'Xe van 500kg',
  VAN_1000KG: 'Xe van 1000kg',
  LIGHT_TRUCK_1T: 'Xe tải 1 tấn',
  LIGHT_TRUCK_2T: 'Xe tải 2 tấn',
};

const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '--';
  return new Intl.NumberFormat('vi-VN').format(value);
};

const formatVehicleType = (vehicleType?: VehicleType) => {
  if (!vehicleType) return 'Chưa xác định';
  return VEHICLE_TYPE_LABELS[vehicleType];
};

export const VehicleStatusDialog: React.FC<VehicleStatusDialogProps> = ({
  open,
  action,
  vehicle,
  isSubmitting,
  onOpenChange,
  onConfirm,
}) => {
  const isDeactivate = action === 'deactivate';
  const Icon = isDeactivate ? PauseCircle : PlayCircle;
  const headerClass = isDeactivate
    ? 'bg-rose-100 text-rose-600'
    : 'bg-emerald-100 text-emerald-600';

  const statusLabel = vehicle ? STATUS_LABELS[vehicle.status] : '--';
  const statusBadgeClass = vehicle
    ? STATUS_BADGE_STYLES[vehicle.status]
    : 'border-slate-200 bg-slate-50 text-slate-600';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader>
          <div className='flex items-start gap-3'>
            <div
              className={cn(
                'flex h-11 w-11 items-center justify-center rounded-2xl shadow-sm',
                headerClass
              )}
            >
              <Icon className='h-5 w-5' />
            </div>
            <div>
              <DialogTitle>
                {isDeactivate
                  ? 'Tạm ngưng phương tiện'
                  : 'Kích hoạt phương tiện'}
              </DialogTitle>
              <DialogDescription className='mt-1'>
                {isDeactivate
                  ? 'Phương tiện sẽ tạm ngưng nhận kế hoạch giao mới.'
                  : 'Phương tiện sẽ sẵn sàng nhận kế hoạch giao hàng.'}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className='rounded-xl border border-slate-200 bg-slate-50 p-4'>
          <div className='flex flex-wrap items-start justify-between gap-3'>
            <div>
              <p className='text-xs font-semibold uppercase tracking-wide text-slate-500'>
                Phương tiện
              </p>
              <p className='text-sm font-semibold text-slate-900'>
                {vehicle?.licensePlate || '--'}
              </p>
              <p className='text-xs text-slate-500'>
                {formatVehicleType(vehicle?.vehicleType)}
              </p>
            </div>

            <Badge variant='outline' className={statusBadgeClass}>
              {statusLabel}
            </Badge>
          </div>

          <Separator className='my-3' />

          <div className='grid gap-2 text-xs text-slate-600 sm:grid-cols-2'>
            <div>
              <p className='font-medium text-slate-700'>Tải trọng tối đa</p>
              <p>{formatNumber(vehicle?.maxWeightKg)} kg</p>
            </div>
            <div>
              <p className='font-medium text-slate-700'>Thể tích tối đa</p>
              <p>{formatNumber(vehicle?.maxVolumeCbm)} cbm</p>
            </div>
            <div>
              <p className='font-medium text-slate-700'>Cập nhật lần cuối</p>
              <p>
                {vehicle?.lastUpdatedStamp
                  ? formatDateVN(vehicle.lastUpdatedStamp)
                  : '--'}
              </p>
            </div>
            <div>
              <p className='font-medium text-slate-700'>Mã phương tiện</p>
              <p className='truncate '>{vehicle?.id || '--'}</p>
            </div>
          </div>
        </div>

        <DialogFooter className='gap-2 sm:gap-0'>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isSubmitting}
          >
            Huy
          </Button>
          <Button
            type='button'
            variant={isDeactivate ? 'destructive' : 'default'}
            onClick={onConfirm}
            disabled={!vehicle || isSubmitting}
          >
            {isSubmitting && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
            {isDeactivate ? 'Tam ngung' : 'Kich hoat'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
