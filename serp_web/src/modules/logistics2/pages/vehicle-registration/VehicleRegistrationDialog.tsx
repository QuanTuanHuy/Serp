import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import { LoaderCircle } from 'lucide-react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import type { Vehicle, VehicleShipper } from '../../types';
import type { VehicleRegistrationDay } from './vehicleRegistration.types';
import { VehicleShipperTag } from './VehicleShipperTag';

interface VehicleRegistrationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  selectedDay: VehicleRegistrationDay | null;
  vehicles: Vehicle[];
  selectedVehicleId: string;
  isVehiclesLoading: boolean;
  isSubmitting: boolean;
  onSelectedVehicleIdChange: (value: string) => void;
  onSubmit: () => void;
}

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

const buildVehicleLabel = (vehicle: Vehicle) => {
  return `${formatVehicleType(vehicle.vehicleType)} - ${vehicle.licensePlate}`;
};

const sortRegistrations = (registrations: VehicleShipper[]) => {
  return [...registrations].sort((left, right) => {
    const leftTime = new Date(left.createdStamp).getTime();
    const rightTime = new Date(right.createdStamp).getTime();

    return leftTime - rightTime;
  });
};

export const VehicleRegistrationDialog: React.FC<
  VehicleRegistrationDialogProps
> = ({
  open,
  onOpenChange,
  selectedDay,
  vehicles,
  selectedVehicleId,
  isVehiclesLoading,
  isSubmitting,
  onSelectedVehicleIdChange,
  onSubmit,
}) => {
  const sortedRegistrations = selectedDay
    ? sortRegistrations(selectedDay.registrations)
    : [];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-xl'>
        <DialogHeader>
          <DialogTitle>Đăng ký xe</DialogTitle>
          <DialogDescription>
            {selectedDay
              ? `Chọn xe giao hàng cho ngày ${format(selectedDay.date, 'dd/MM/yyyy', { locale: vi })}.`
              : 'Chọn ngày trên lịch để đăng ký xe.'}
          </DialogDescription>
        </DialogHeader>

        <div className='space-y-4'>
          {selectedDay && sortedRegistrations.length > 0 && (
            <div className='space-y-2 rounded-lg border border-slate-200 bg-slate-50 p-3'>
              <p className='text-xs font-semibold uppercase tracking-wide text-slate-600'>
                Lịch sử đăng ký xe hôm nay
              </p>
              <div className='space-y-1'>
                {sortedRegistrations.map((registration) => (
                  <VehicleShipperTag
                    key={registration.id}
                    registration={registration}
                    isPastDay={false}
                  />
                ))}
              </div>
            </div>
          )}

          <div className='space-y-2'>
            <p className='text-sm font-medium text-slate-700'>
              Phương tiện giao hàng
            </p>
            <Select
              value={selectedVehicleId || undefined}
              onValueChange={onSelectedVehicleIdChange}
              disabled={isVehiclesLoading || isSubmitting}
            >
              <SelectTrigger className='w-full'>
                <SelectValue
                  placeholder={
                    isVehiclesLoading
                      ? 'Đang tải danh sách phương tiện...'
                      : 'Chọn phương tiện để đăng ký'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {vehicles.map((vehicle) => {
                  const isMaintenance = vehicle.status === 'MAINTENANCE';

                  return (
                    <SelectItem
                      key={vehicle.id}
                      value={vehicle.id}
                      disabled={isMaintenance}
                    >
                      {buildVehicleLabel(vehicle)}
                      {isMaintenance ? ' (Đang bảo trì)' : ''}
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>
        </div>

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
            disabled={isSubmitting}
          >
            Hủy
          </Button>
          <Button
            type='button'
            onClick={onSubmit}
            disabled={!selectedVehicleId || isSubmitting}
          >
            {isSubmitting && (
              <LoaderCircle className='mr-2 h-4 w-4 animate-spin' />
            )}
            Gửi đăng ký
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
