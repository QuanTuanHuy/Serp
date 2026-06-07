'use client';

import {
  Button,
  Card,
  CardContent,
  Badge,
  CardHeader,
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/shared/components/ui';
import {
  ShoppingCart,
  Package,
  Clock,
  CheckCircle2,
  XCircle,
  Calendar,
  User,
  TrendingUp,
  ExternalLink,
  MoreVertical,
  TriangleAlert,
  AlertCircle,
} from 'lucide-react';
import { cn, formatCurrency } from '@/shared/utils';
import type { Customer, Order } from '../../types';
import { formatDateVN } from '@/shared/utils/format';

const statusStyles = {
  CREATED: {
    label: 'Chờ duyệt',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: Clock,
    accent: '#3b82f6',
  },
  APPROVED: {
    label: 'Đã duyệt',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
    accent: '#10b981',
  },
  CANCELLED: {
    label: 'Đã hủy',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
    accent: '#f43f5e',
  },
  FULLY_DELIVERED: {
    label: 'Đã giao hàng',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: Package,
    accent: '#a855f7',
  },
} as const;

interface OrderCardProps {
  order: Order;
  onClick?: () => void;
  customer?: Customer;
  viewMode?: 'grid' | 'list';
}

const getPriorityMeta = (priority?: number) => {
  if (priority === undefined || priority === null) {
    return {
      label: 'Không ưu tiên',
      color: 'text-muted-foreground',
    };
  }

  if (priority >= 8) {
    return {
      label: `Cao (${priority})`,
      color: 'text-red-600 dark:text-red-400',
    };
  }

  if (priority >= 5) {
    return {
      label: `Trung bình (${priority})`,
      color: 'text-amber-600 dark:text-amber-400',
    };
  }

  return {
    label: `Thấp (${priority})`,
    color: 'text-green-600 dark:text-green-400',
  };
};

export const OrderCard = ({
  order,
  onClick,
  customer,
  viewMode = 'grid',
}: OrderCardProps) => {
  const status =
    statusStyles[order.statusId as keyof typeof statusStyles] ||
    statusStyles.CREATED;
  const StatusIcon = status.icon;
  const priorityMeta = getPriorityMeta(order.priority);

  const orderTitle =
    order.orderName || `Đơn hàng #${order.id?.slice(0, 8) || 'N/A'}`;
  const shortId = order.id ? `${order.id.slice(0, 10)}...` : 'N/A';
  const customerLabel =
    customer?.name ||
    (order.toCustomerId ? `${order.toCustomerId.slice(0, 8)}...` : 'N/A');
  const deliveryLabel = order.deliveryBeforeDate
    ? `Giao trước ngày ${formatDateVN(order.deliveryBeforeDate)}`
    : 'Không có hạn giao';
  const priorityValue =
    order.priority === undefined || order.priority === null
      ? 'Không ưu tiên'
      : `Mức ưu tiên ${order.priority}`;

  const deadlineMs = order.deliveryBeforeDate
    ? new Date(order.deliveryBeforeDate).getTime()
    : null;

  const isOverdue =
    !!deadlineMs &&
    deadlineMs < Date.now() &&
    order.statusId !== 'FULLY_DELIVERED' &&
    order.statusId !== 'CANCELLED';

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
          <div className='grid min-w-[900px] grid-cols-[3fr_3fr_2.5fr_1.5fr] items-center gap-4 p-4'>
            <p className='text-sm font-semibold truncate'>{orderTitle}</p>

            <p className='text-sm text-muted-foreground truncate'>
              {customerLabel}
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

            <p
              className={cn('text-sm font-medium truncate', priorityMeta.color)}
            >
              {priorityValue}
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

            <div className='grid flex-1'>
              <h3 className='font-semibold text-base leading-tight mb-1 truncate'>
                {orderTitle}
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
          className={cn('mt-2 w-fit gap-1', status.bg, status.text)}
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
              <ShoppingCart
                className='h-7 w-7'
                style={{ color: status.accent }}
              />
            </div>
          </div>

          <div className='flex-1 grid grid-cols-2 gap-2 text-xs'>
            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <Calendar className='h-3 w-3' />
                <span>Ngày đặt</span>
              </div>
              <p className='text-sm font-semibold'>
                {formatDateVN(order.orderDate)}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <User className='h-3 w-3' />
                <span>Khách hàng</span>
              </div>
              <p className='text-sm font-semibold truncate'>{customerLabel}</p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <TriangleAlert className='h-3 w-3' />
                <span>Ưu tiên</span>
              </div>
              <p className={cn('font-semibold', priorityMeta.color)}>
                {priorityMeta.label}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1 text-muted-foreground'>
                <TrendingUp className='h-3 w-3' />
                <span>Tổng tiền</span>
              </div>
              <p className='text-sm font-semibold truncate'>
                {formatCurrency(order.totalAmount)}
              </p>
            </div>
          </div>
        </div>

        <div className='flex items-center justify-between gap-2'>
          {order.deliveryBeforeDate ? (
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
              <span>Giao trước: {formatDateVN(order.deliveryBeforeDate)}</span>
            </div>
          ) : (
            <div className='text-xs text-muted-foreground'>
              Không có hạn giao
            </div>
          )}

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
