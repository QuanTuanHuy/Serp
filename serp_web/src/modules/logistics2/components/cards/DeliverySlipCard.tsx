import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
} from '@/shared/components/ui';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui/dropdown-menu';
import {
  AlertCircle,
  Calendar,
  Clock,
  ExternalLink,
  MoreVertical,
  Truck,
  User,
  ArrowDownToLine,
  ArrowUpFromLine,
  Box,
  ReceiptText,
  TruckIcon,
  NotepadText,
  Undo2Icon,
  WeightIcon,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import { Customer, DeliverySlip, Order, OutboundShipment } from '../../types';

const statusStyles = {
  PENDING: {
    label: 'Chờ lên kế hoạch',
    bg: 'bg-slate-100 dark:bg-slate-900/30',
    text: 'text-slate-700 dark:text-slate-400',
    dot: 'bg-slate-500',
    icon: Clock,
    accent: '#64748B',
  },
  ASSIGNED: {
    label: 'Đã lên kế hoạch giao',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: NotepadText,
    accent: '#3b82f6',
  },
  DELIVERING: {
    label: 'Đang giao',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
    icon: TruckIcon,
    accent: '#e3a149',
  },
  DELIVERED: {
    label: 'Đã giao',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: ArrowUpFromLine,
    accent: '#8b5cf6',
  },
  RECALLING: {
    label: 'Đang thu hồi',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-yellow-500',
    icon: Undo2Icon,
    accent: '#f43f5e',
  },
};

export const DeliverySlipCard = ({
  slip,
  onClick,
  viewMode = 'grid',
}: {
  slip: DeliverySlip;
  onClick?: () => void;
  viewMode?: 'grid' | 'list';
}) => {
  const status =
    statusStyles[slip.status as keyof typeof statusStyles] ||
    statusStyles.PENDING;
  const StatusIcon = status.icon;

  const slipTitle =
    slip.code || `Phiếu giao ID #${slip.id?.slice(0, 8) || 'N/A'}`;
  const shortId = slip.id ? `${slip.id.slice(0, 10)}...` : 'N/A';

  const partyLabel = slip.customerName || 'N/A';

  const facilityLabel = slip.facility?.name || 'N/A';

  const deliveryLabel = `Tạo ngày ${formatDateVN(slip.createdStamp)}`;

  const isOverdue = slip.status === 'PENDING';

  const handleOpenDetails = (e: React.MouseEvent) => {
    e.stopPropagation();
    onClick?.();
  };

  if (viewMode === 'list') {
    return (
      <Card
        className={cn(
          'group relative overflow-hidden cursor-pointer transition-colors',
          'hover:bg-muted/40 hover:border-primary/40',
          isOverdue && 'border-red-300 dark:border-red-800'
        )}
        onClick={onClick}
      >
        <div className='overflow-x-auto'>
          <div className='grid min-w-[900px] grid-cols-[2.5fr_3fr_0.5fr_0.5fr_3fr_2fr_2fr] items-center gap-4 p-4'>
            <p className='text-sm font-semibold truncate'>{slipTitle}</p>

            <Badge
              variant='secondary'
              className={cn('w-fit gap-1', status.bg, status.text)}
            >
              <StatusIcon className='h-3 w-3' />
              <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
              {status.label}
            </Badge>

            <p className='text-sm text-muted-foreground truncate'>
              {slip.totalVolumeCbm}m³
            </p>

            <p className='text-sm text-muted-foreground truncate'>
              {slip.totalWeightKg}kg
            </p>

            <p className='text-sm text-muted-foreground truncate'>
              {partyLabel}
            </p>

            <p className='text-sm text-muted-foreground truncate'>
              {facilityLabel}
            </p>

            <p
              className={cn(
                'text-sm truncate',
                isOverdue
                  ? 'text-red-600 dark:text-red-400 font-medium'
                  : 'text-muted-foreground'
              )}
            >
              {deliveryLabel}
            </p>
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'group relative overflow-hidden cursor-pointer transition-all',
        'hover:shadow-xl hover:scale-[1.02] hover:border-primary/50',
        'active:scale-[0.98]',
        isOverdue &&
          'border-red-300 dark:border-red-800 shadow-red-100 dark:shadow-red-900/20'
      )}
      onClick={onClick}
    >
      <div
        className='absolute top-0 left-0 w-full h-1 transition-all group-hover:h-1.5'
        style={{ backgroundColor: status.accent }}
      />

      <CardHeader className='pb-3'>
        <div className='flex items-start justify-between gap-2'>
          <div className='flex items-start gap-3 flex-1 min-w-0'>
            <div
              className='w-1 h-12 rounded-full flex-shrink-0'
              style={{ backgroundColor: status.accent }}
            />

            <div className='flex-1 min-w-0'>
              <h3 className='font-semibold text-base leading-tight mb-1 truncate'>
                {slipTitle}
              </h3>
              <p className='text-xs text-muted-foreground truncate'>
                ID: {shortId}
              </p>
            </div>
          </div>

          <div className='flex items-center gap-1 flex-shrink-0'>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8'
              onClick={handleOpenDetails}
            >
              <ExternalLink className='h-4 w-4' />
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                <Button variant='ghost' size='icon' className='h-8 w-8'>
                  <MoreVertical className='h-4 w-4' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end'>
                <DropdownMenuItem onClick={handleOpenDetails}>
                  <ExternalLink className='mr-2 h-4 w-4' />
                  Xem chi tiết
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <Badge
          variant='secondary'
          className={cn('w-fit gap-1', status.bg, status.text)}
        >
          <StatusIcon className='h-3 w-3' />
          <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
          {status.label}
        </Badge>
      </CardHeader>

      <CardContent className='space-y-4 pt-6'>
        <div className='flex items-center gap-4'>
          <div className='relative flex-shrink-0'>
            <div
              className='h-16 w-16 rounded-full border-4 flex items-center justify-center'
              style={{ borderColor: `${status.accent}55` }}
            >
              <Truck className='h-7 w-7' style={{ color: status.accent }} />
            </div>
          </div>

          <div className='flex-1 grid grid-cols-2 gap-2 text-xs'>
            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <Box className='h-3 w-3' />
                <span>Kho giao</span>
              </div>
              <p className='text-sm font-semibold truncate'>{facilityLabel}</p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <User className='h-3 w-3' />
                <span>Khách hàng</span>
              </div>
              <p className='text-sm font-semibold truncate'>{partyLabel}</p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <Box className='h-3 w-3' />
                <span>Thể tích</span>
              </div>
              <p className='text-sm font-semibold truncate'>
                {slip.totalVolumeCbm} m³
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <WeightIcon className='h-3 w-3' />
                <span>Khối lượng</span>
              </div>
              <p className='text-sm font-semibold'>{slip.totalWeightKg} kg</p>
            </div>
          </div>
        </div>

        <div className='flex items-center justify-between gap-2'>
          <div
            className={cn(
              'flex items-center gap-1.5 text-xs',
              isOverdue
                ? 'text-red-600 dark:text-red-400 font-medium'
                : 'text-muted-foreground'
            )}
          >
            {isOverdue ? (
              <AlertCircle className='h-3.5 w-3.5' />
            ) : (
              <Calendar className='h-3.5 w-3.5' />
            )}
            <span>{deliveryLabel}</span>
          </div>

          <Button
            variant='ghost'
            size='sm'
            className='opacity-0 group-hover:opacity-100 transition-opacity'
            onClick={handleOpenDetails}
          >
            Xem chi tiết
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
