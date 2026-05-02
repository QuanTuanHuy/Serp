import { Card, CardContent } from '@/shared/components/ui';
import { VehicleShipper } from '../../types';
import { CheckCircle2, AlertCircle, CalendarClock } from 'lucide-react';
import { formatDateVN } from '@/shared/utils/format';

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

export const VehicleShipperCard: React.FC<{
  shipper: VehicleShipper;
}> = ({ shipper }) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const workDate = new Date(shipper.workingDate);
  workDate.setHours(0, 0, 0, 0);

  const timeDiff = workDate.getTime() - today.getTime();

  let statusConfig = {
    icon: CheckCircle2,
    text: 'Sẵn sàng cho ca giao hàng hôm nay',
    textColor: 'text-emerald-800',
    cardBg: 'border-emerald-200/80 bg-emerald-50/70',
    labelColor: 'text-emerald-700',
    valueColor: 'text-emerald-900',
  };

  if (timeDiff < 0) {
    statusConfig = {
      icon: AlertCircle,
      text: 'Đã hết hạn',
      textColor: 'text-slate-600',
      cardBg: 'border-slate-200/80 bg-slate-50/70',
      labelColor: 'text-slate-700',
      valueColor: 'text-slate-900',
    };
  } else if (timeDiff > 0) {
    statusConfig = {
      icon: CalendarClock,
      text: `Sẵn sàng cho ca giao hàng ngày ${formatDateVN(workDate)}`,
      textColor: 'text-blue-800',
      cardBg: 'border-blue-200/80 bg-blue-50/70',
      labelColor: 'text-blue-700',
      valueColor: 'text-blue-900',
    };
  }

  const Icon = statusConfig.icon;

  return (
    <Card className={`${statusConfig.cardBg} transition-colors`}>
      <CardContent className='flex flex-wrap items-center justify-between gap-3 p-4'>
        <div>
          <p
            className={`text-xs font-semibold uppercase tracking-wide ${statusConfig.labelColor}`}
          >
            Xe được phân công
          </p>
          <p className={`mt-1 text-sm font-medium ${statusConfig.valueColor}`}>
            {shipper.vehicle?.licensePlate || 'Chưa có biển số'}
            {' · '}
            {formatVehicleType(shipper.vehicle?.vehicleType)}
          </p>
        </div>

        <div
          className={`flex items-center gap-2 text-sm ${statusConfig.textColor}`}
        >
          <Icon className='h-4 w-4' />
          {statusConfig.text}
        </div>
      </CardContent>
    </Card>
  );
};
