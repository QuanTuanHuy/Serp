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
  ArrowUpFromLine,
  Box,
  TruckIcon,
  Undo2Icon,
  WeightIcon,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import { DeliveryPlan, Facility } from '../../types';

const statusStyles = {
  DRAFT: {
    label: 'Nháp',
    bg: 'bg-slate-100 dark:bg-slate-900/30',
    text: 'text-slate-700 dark:text-slate-400',
    dot: 'bg-slate-500',
    icon: Clock,
    accent: '#64748B',
  },
  OPTIMIZING: {
    label: 'Đang tính toán',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
    icon: TruckIcon,
    accent: '#e3a149',
  },
  COMPLETED: {
    label: 'Đã tối ưu',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: ArrowUpFromLine,
    accent: '#8b5cf6',
  },
  FAILED: {
    label: 'Tối ưu thất bại',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-yellow-500',
    icon: Undo2Icon,
    accent: '#f43f5e',
  },
};

export const DeliveryPlanCard = ({
  plan,
  facility,
  onClick,
  viewMode = 'grid',
}: {
  plan: DeliveryPlan;
  facility: Facility;
  onClick?: () => void;
  viewMode?: 'grid' | 'list';
}) => {
  const status =
    statusStyles[plan.optimizationStatus as keyof typeof statusStyles] ||
    statusStyles.DRAFT;
  const StatusIcon = status.icon;

  const planTitle =
    plan.planCode || `Kế hoạch giao ID #${plan.id?.slice(0, 8) || 'N/A'}`;
  const shortId = plan.id ? `${plan.id.slice(0, 10)}...` : 'N/A';

  const facilityLabel = facility.name || 'N/A';

  const deliveryLabel = `Tạo ngày ${formatDateVN(plan.createdStamp)}`;

  const isOverdue = plan.optimizationStatus === 'DRAFT';

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
          <div className='grid min-w-[900px] grid-cols-[2.5fr_3fr_2fr_1fr_1fr_2fr] items-center gap-4 p-4'>
            <p className='text-sm font-semibold truncate'>{planTitle}</p>

            <Badge
              variant='secondary'
              className={cn('w-fit gap-1', status.bg, status.text)}
            >
              <StatusIcon className='h-3 w-3' />
              <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
              {status.label}
            </Badge>

            <p className='text-sm text-muted-foreground truncate'>
              {facilityLabel}
            </p>

            <p className='text-sm text-muted-foreground truncate'>
              {plan.totalSlips} đơn hàng
            </p>

            <p className='text-sm text-muted-foreground truncate'>
              {plan.totalVehicles} xe
            </p>

            <p
              className={cn(
                'text-sm truncate',
                isOverdue
                  ? 'text-red-600 dark:text-red-400 font-medium'
                  : 'text-muted-foreground'
              )}
            >
              Giao vào {formatDateVN(plan.deliveryDate)}
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
                {planTitle}
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
                <span>Giao ngày</span>
              </div>
              <p className='text-sm font-semibold truncate'>
                {formatDateVN(plan.deliveryDate)}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <Box className='h-3 w-3' />
                <span>Số đơn hàng</span>
              </div>
              <p className='text-sm font-semibold truncate'>
                {plan.totalSlips}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <WeightIcon className='h-3 w-3' />
                <span>Số xe</span>
              </div>
              <p className='text-sm font-semibold'>{plan.totalVehicles}</p>
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
