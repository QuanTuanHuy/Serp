'use client';

import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Badge,
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/shared/components/ui';
import {
  Package,
  Calendar,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Hash,
  ExternalLink,
  MoreVertical,
  Clock,
  Warehouse,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { formatDateVN } from '@/shared/utils/format';
import type { Facility, InventoryItem, Product } from '../../types';

const statusStyles = {
  VALID: {
    label: 'Còn hạn',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
    accent: '#10b981', // emerald-500
  },
  EXPIRED: {
    label: 'Đã hết hạn',
    bg: 'bg-rose-100 dark:bg-rose-900/30',
    text: 'text-rose-700 dark:text-rose-400',
    dot: 'bg-rose-500',
    icon: XCircle,
    accent: '#f43f5e', // rose-500
  },
  EXPIRING_SOON: {
    label: 'Sắp hết hạn',
    bg: 'bg-amber-100 dark:bg-amber-900/30',
    text: 'text-amber-700 dark:text-amber-400',
    dot: 'bg-amber-500',
    icon: AlertTriangle,
    accent: '#f59e0b', // amber-500
  },
  DAMAGED: {
    label: 'Hư hỏng',
    bg: 'bg-orange-100 dark:bg-orange-900/30',
    text: 'text-orange-700 dark:text-orange-400',
    dot: 'bg-orange-500',
    icon: AlertTriangle,
    accent: '#f97316', // orange-500
  },
} as const;

interface InventoryItemCardProps {
  item: InventoryItem;
  product: Product;
  facility: Facility;
  onClick?: () => void;
  viewMode?: 'grid' | 'list';
}

export const InventoryItemCard = ({
  item,
  product,
  facility,
  onClick,
  viewMode = 'grid',
}: InventoryItemCardProps) => {
  const isExpiringSoon =
    item.expirationDate &&
    new Date(item.expirationDate) <
      new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) &&
    new Date(item.expirationDate) >= new Date();

  const isExpired =
    item.expirationDate && new Date(item.expirationDate) < new Date();

  // Xác định trạng thái cuối cùng để render màu sắc
  let effectiveStatus =
    statusStyles[item.statusId as keyof typeof statusStyles] ||
    statusStyles.VALID;

  if (isExpired) {
    effectiveStatus = statusStyles.EXPIRED;
  } else if (isExpiringSoon && item.statusId === 'VALID') {
    effectiveStatus = statusStyles.EXPIRING_SOON;
  }

  const StatusIcon = effectiveStatus.icon;

  const handleOpenDetails = (e: React.MouseEvent) => {
    e.stopPropagation();
    onClick?.();
  };

  const qtyAvailable =
    (item.quantityOnHand || 0) -
    (item.quantityCommitted || 0) -
    (item.quantityReserved || 0);

  if (viewMode === 'list') {
    return (
      <Card
        className={cn(
          'group relative overflow-hidden cursor-pointer transition-colors',
          'hover:bg-muted/40 hover:border-primary/40',
          isExpiringSoon && 'border-amber-300 dark:border-amber-800',
          isExpired && 'border-red-300 dark:border-red-800'
        )}
        onClick={onClick}
      >
        <div className='overflow-x-auto'>
          <div className='grid min-w-[900px] grid-cols-[3fr_2.5fr_3fr_1.5fr_1.5fr] items-center gap-4 p-4'>
            <p className='text-sm font-semibold truncate'>
              {product?.name ?? `Sản phẩm chưa rõ`}
            </p>

            <p className='text-sm text-muted-foreground truncate'>
              {facility?.name || 'Không xác định'}
            </p>

            <p
              className={cn(
                'text-sm truncate',
                isExpiringSoon
                  ? 'text-amber-600 dark:text-amber-400 font-medium'
                  : isExpired
                    ? 'text-red-600 dark:text-red-400 font-medium'
                    : 'text-muted-foreground'
              )}
            >
              Hết hạn{' '}
              {item.expirationDate
                ? formatDateVN(item.expirationDate)
                : 'Không có thời hạn'}
            </p>

            <p
              className={
                'text-sm font-bold truncate text-emerald-600 dark:text-emerald-400'
              }
            >
              {qtyAvailable} Khả dụng
            </p>

            <p
              className={
                'text-sm font-bold truncate text-blue-600 dark:text-blue-400'
              }
            >
              {item.quantityOnHand} Tồn kho thực
            </p>
          </div>
        </div>
      </Card>
    );
  }

  return (
    <Card
      className={cn(
        'group relative overflow-hidden cursor-pointer transition-all duration-300',
        'hover:shadow-xl hover:scale-[1.02] hover:border-primary/50',
        'active:scale-[0.98]',
        isExpired &&
          'border-red-300 dark:border-red-800 shadow-red-100 dark:shadow-red-900/20'
      )}
      onClick={onClick}
    >
      {/* Top Accent Line */}
      <div
        className='absolute top-0 left-0 w-full h-1 transition-all duration-300 group-hover:h-1.5'
        style={{ backgroundColor: effectiveStatus.accent }}
      />

      <CardHeader className='pb-3 pt-5'>
        <div className='flex items-start justify-between gap-2'>
          <div className='flex items-start gap-3 flex-1 min-w-0'>
            {/* Vertical Accent Line */}
            <div
              className='w-1 h-12 rounded-full flex-shrink-0'
              style={{ backgroundColor: effectiveStatus.accent }}
            />

            <div className='grid flex-1'>
              <h3 className='font-semibold text-base leading-tight mb-1 truncate text-foreground'>
                {product?.name ?? `Sản phẩm chưa rõ`}
              </h3>
              <p className='text-xs text-muted-foreground truncate'>
                ID: {item.id ? `${item.id.slice(0, 10)}...` : 'N/A'}
              </p>
            </div>
          </div>

          {/* Quick Actions */}
          <div className='flex items-center gap-1 flex-shrink-0'>
            <Button
              variant='ghost'
              size='icon'
              className='h-8 w-8 text-muted-foreground hover:text-foreground'
              onClick={handleOpenDetails}
            >
              <ExternalLink className='h-4 w-4' />
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                <Button
                  variant='ghost'
                  size='icon'
                  className='h-8 w-8 text-muted-foreground hover:text-foreground'
                >
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

        {/* Status Badge */}
        <Badge
          variant='secondary'
          className={cn(
            'mt-3 w-fit gap-1 border-transparent',
            effectiveStatus.bg,
            effectiveStatus.text
          )}
        >
          <StatusIcon className='h-3.5 w-3.5' />
          <span
            className={cn('h-1.5 w-1.5 rounded-full', effectiveStatus.dot)}
          />
          {effectiveStatus.label}
        </Badge>
      </CardHeader>

      <CardContent className='space-y-5 pt-2'>
        {/* Main Info Grid */}
        <div className='flex items-center gap-4'>
          {/* Circular Icon (Matching OrderCard style) */}
          <div className='relative flex-shrink-0'>
            <div
              className='h-16 w-16 rounded-full border-4 flex items-center justify-center bg-background'
              style={{ borderColor: `${effectiveStatus.accent}40` }}
            >
              <Package
                className='h-7 w-7'
                style={{ color: effectiveStatus.accent }}
              />
            </div>
          </div>

          <div className='flex-1 grid grid-cols-2 gap-y-3 gap-x-2 text-xs'>
            <div className='space-y-1'>
              <div className='flex items-center gap-1.5 text-muted-foreground'>
                <Hash className='h-3.5 w-3.5' />
                <span>Số lô</span>
              </div>
              <p className='text-sm font-semibold truncate text-foreground'>
                {item.lotId || 'N/A'}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1.5 text-muted-foreground'>
                <Clock className='h-3.5 w-3.5' />
                <span>Ngày nhập kho</span>
              </div>
              <p className='text-sm font-semibold truncate text-foreground'>
                {formatDateVN(item.receivedDate)}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1.5 text-muted-foreground'>
                <Warehouse className='h-3.5 w-3.5' />
                <span>Kho hàng</span>
              </div>
              <p className='text-sm font-semibold truncate text-foreground'>
                {facility?.name || 'Không xác định'}
              </p>
            </div>

            <div className='space-y-1'>
              <div className='flex items-center gap-1.5 text-muted-foreground'>
                <Calendar className='h-3.5 w-3.5' />
                <span>Hạn sử dụng</span>
              </div>
              <p
                className={cn(
                  'text-sm font-semibold truncate',
                  isExpired
                    ? 'text-rose-600 dark:text-rose-400'
                    : isExpiringSoon
                      ? 'text-amber-600 dark:text-amber-400'
                      : 'text-foreground'
                )}
              >
                {item.expirationDate
                  ? formatDateVN(item.expirationDate)
                  : 'Không có thời hạn'}
              </p>
            </div>
          </div>
        </div>

        {/* Quantities Footer */}
        <div className='bg-muted/40 rounded-xl p-3 grid grid-cols-3 gap-2 text-center items-center border border-border/50'>
          <div className='flex flex-col space-y-0.5'>
            <span className='text-[10px] uppercase text-muted-foreground font-semibold tracking-wider'>
              Khả dụng
            </span>
            <span className='text-lg font-bold text-emerald-600 dark:text-emerald-400'>
              {qtyAvailable}
            </span>
          </div>

          <div className='flex flex-col space-y-0.5 border-x border-border/50'>
            <span className='text-[10px] uppercase text-muted-foreground font-semibold tracking-wider'>
              Tồn thực
            </span>
            <span className='text-lg font-bold text-blue-600 dark:text-blue-400'>
              {item.quantityOnHand || 0}
            </span>
          </div>

          <div className='flex flex-col space-y-0.5'>
            <span className='text-[10px] uppercase text-muted-foreground font-semibold tracking-wider'>
              Chưa xuất
            </span>
            <span className='text-lg font-bold text-amber-600 dark:text-amber-400'>
              {item.quantityCommitted || 0}
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
