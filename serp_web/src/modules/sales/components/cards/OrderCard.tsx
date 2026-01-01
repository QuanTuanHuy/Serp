'use client';

import { Button, Card, CardContent, Badge } from '@/shared/components/ui';
import {
  ShoppingCart,
  Package,
  Clock,
  CheckCircle2,
  XCircle,
  Calendar,
  User,
  TrendingUp,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import type { Customer, Order } from '../../types';

const statusStyles = {
  CREATED: {
    label: 'Đã tạo',
    bg: 'bg-blue-100 dark:bg-blue-900/30',
    text: 'text-blue-700 dark:text-blue-400',
    dot: 'bg-blue-500',
    icon: Clock,
  },
  APPROVED: {
    label: 'Đã duyệt',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
  },
  CANCELLED: {
    label: 'Đã hủy',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
  },
  FULLY_DELIVERED: {
    label: 'Đã giao hàng',
    bg: 'bg-purple-100 dark:bg-purple-900/30',
    text: 'text-purple-700 dark:text-purple-400',
    dot: 'bg-purple-500',
    icon: Package,
  },
};

const formatCurrency = (value?: number) => {
  if (!value) return 'đ0';
  if (value >= 1000000) return `đ${(value / 1000000).toFixed(1)}M`;
  if (value >= 1000) return `đ${(value / 1000).toFixed(1)}K`;
  if (value >= 1000000000) return `đ${(value / 1000000000).toFixed(1)}B`;
  return `đ${value.toLocaleString()}`;
};

export const OrderCard = ({
  order,
  onClick,
  customer,
}: {
  order: Order;
  onClick?: () => void;
  customer?: Customer;
}) => {
  const status =
    statusStyles[order.statusId as keyof typeof statusStyles] ||
    statusStyles.CREATED;
  const StatusIcon = status.icon;

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200 cursor-pointer'
      )}
      onClick={onClick}
    >
      {/* Header with gradient accent */}
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      <CardContent className='p-5'>
        {/* Header */}
        <div className='flex items-start justify-between mb-4'>
          <div className='flex-1 min-w-0'>
            <div className='flex items-center gap-2 mb-2'>
              <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 text-primary'>
                <ShoppingCart className='h-5 w-5' />
              </div>
              <div className='flex-1 min-w-0'>
                <h3 className='font-semibold text-foreground truncate'>
                  {order.orderName || `Đơn hàng #${order.id?.slice(0, 8)}...`}
                </h3>
                <p className='text-xs text-muted-foreground'>
                  ID: {order.id?.slice(0, 10) || 'N/A'}...
                </p>
              </div>
            </div>

            <Badge
              variant='secondary'
              className={cn('gap-1', status.bg, status.text)}
            >
              <StatusIcon className='h-3 w-3' />
              <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
              {status.label}
            </Badge>
          </div>
        </div>

        {/* Order Info */}
        <div className='space-y-2 mb-4'>
          {order.deliveryBeforeDate && (
            <div className='flex items-center gap-2 text-sm'>
              <Calendar className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                Giao trước ngày{' '}
                {new Date(order.deliveryBeforeDate).toLocaleDateString()}
              </span>
            </div>
          )}
          {order.toCustomerId && (
            <div className='flex items-center gap-2 text-sm'>
              <User className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                {customer?.name || `KH: ${order.toCustomerId.slice(0, 8)}...`}
              </span>
            </div>
          )}
          {order.priority && (
            <div className='flex items-center gap-2 text-sm'>
              <TrendingUp className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='truncate text-muted-foreground'>
                Mức ưu tiên: {order.priority || `N/A`}
              </span>
            </div>
          )}
        </div>

        {/* Footer - Total */}
        <div className='flex items-center justify-between pt-3 border-t'>
          <div>
            <p className='text-xs text-muted-foreground'>Tổng thành tiền</p>
            <p className='text-lg font-bold text-foreground'>
              {formatCurrency(order.totalAmount)}
            </p>
          </div>

          <Button
            variant='ghost'
            size='sm'
            className='opacity-0 group-hover:opacity-100 transition-opacity'
            onClick={(e) => {
              e.stopPropagation();
              onClick?.();
            }}
          >
            Xem chi tiết →
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
