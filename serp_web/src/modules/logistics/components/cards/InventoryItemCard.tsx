'use client';

import { Card, CardContent, Badge } from '@/shared/components/ui';
import {
  Package,
  Calendar,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Hash,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import { useGetProductQuery } from '../../api/logisticsApi';
import type { InventoryItem } from '../../types';

const statusStyles = {
  VALID: {
    label: 'Còn hạn',
    bg: 'bg-green-100 dark:bg-green-900/30',
    text: 'text-green-700 dark:text-green-400',
    dot: 'bg-green-500',
    icon: CheckCircle2,
  },
  EXPIRED: {
    label: 'Hết hạn',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
  },
  DAMAGED: {
    label: 'Hư hỏng',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
    icon: AlertTriangle,
  },
};

export const InventoryItemCard = ({
  item,
  onClick,
}: {
  item: InventoryItem;
  onClick?: () => void;
}) => {
  const status =
    statusStyles[item.statusId as keyof typeof statusStyles] ||
    statusStyles.VALID;
  const StatusIcon = status.icon;

  const { data: productResponse } = useGetProductQuery(item.productId);
  const productName = productResponse?.data?.name;

  const isExpiringSoon =
    item.expirationDate &&
    new Date(item.expirationDate) <
      new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) &&
    new Date(item.expirationDate) >= new Date();

  const isExpired =
    item.expirationDate && new Date(item.expirationDate) < new Date();

  return (
    <Card
      className={cn(
        'group relative overflow-hidden',
        'hover:shadow-lg hover:border-primary/20 transition-all duration-200',
        onClick && 'cursor-pointer'
      )}
      onClick={onClick}
    >
      <div className='relative h-2 bg-gradient-to-r from-primary/60 via-primary/40 to-primary/20' />

      <CardContent className='p-5'>
        {/* Header */}
        <div className='flex items-start justify-between mb-4'>
          <div className='flex-1 min-w-0'>
            <div className='flex items-center gap-2 mb-2'>
              <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 text-primary'>
                <Package className='h-5 w-5' />
              </div>
              <div className='flex-1 min-w-0'>
                <h3 className='font-semibold text-foreground truncate'>
                  {productName ?? `SP: ${item.productId.slice(0, 12)}...`}
                </h3>
                <p className='text-xs text-muted-foreground'>
                  ID: {item.id?.slice(0, 10)}...
                </p>
              </div>
            </div>

            <div className='flex items-center gap-2 flex-wrap'>
              {!isExpired && !isExpiringSoon && (
                <Badge
                  variant='secondary'
                  className={cn('gap-1', status.bg, status.text)}
                >
                  <StatusIcon className='h-3 w-3' />
                  <span
                    className={cn('h-1.5 w-1.5 rounded-full', status.dot)}
                  />
                  {status.label}
                </Badge>
              )}
              {isExpiringSoon && (
                <Badge
                  variant='secondary'
                  className='gap-1 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400'
                >
                  <AlertTriangle className='h-3 w-3' />
                  Sắp hết hạn
                </Badge>
              )}
              {isExpired && (
                <Badge
                  variant='secondary'
                  className='gap-1 bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400'
                >
                  <XCircle className='h-3 w-3' />
                  Đã hết hạn
                </Badge>
              )}
            </div>
          </div>
        </div>

        {/* Item Info */}
        <div className='space-y-2 mb-4'>
          <div className='flex items-center gap-2 text-sm'>
            <Hash className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='text-muted-foreground truncate'>
              Lô: {item.lotId}
            </span>
          </div>
          {item.expirationDate && (
            <div className='flex items-center gap-2 text-sm'>
              <Calendar className='h-4 w-4 text-muted-foreground shrink-0' />
              <span className='text-muted-foreground'>
                HSD: {formatDateVN(item.expirationDate)}
              </span>
            </div>
          )}
          <div className='flex items-center gap-2 text-sm'>
            <Calendar className='h-4 w-4 text-muted-foreground shrink-0' />
            <span className='text-muted-foreground'>
              Nhập kho: {formatDateVN(item.receivedDate)}
            </span>
          </div>
        </div>

        {/* Footer - Quantities */}
        <div className='grid grid-cols-3 gap-2 pt-3 border-t text-center'>
          <div>
            <p className='text-xs text-muted-foreground'>Khả dụng</p>
            <p className='text-sm font-bold text-foreground'>
              {item.quantityOnHand -
                item.quantityCommitted -
                item.quantityReserved}
            </p>
          </div>
          <div>
            <p className='text-xs text-muted-foreground'>Tồn kho thực</p>
            <p className='text-sm font-bold text-foreground'>
              {item.quantityOnHand}
            </p>
          </div>
          <div>
            <p className='text-xs text-muted-foreground'>Chưa xuất kho</p>
            <p className='text-sm font-bold text-foreground'>
              {item.quantityReserved}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
