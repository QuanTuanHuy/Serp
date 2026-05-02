/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 route card component
*/

import type { KeyboardEvent } from 'react';
import {
  ArrowRight,
  MapPinned,
  MoreVertical,
  PlayCircle,
  XCircle,
} from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { Route, RouteStatus } from '../../types';

const ROUTE_STATUS_STYLES: Record<
  RouteStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: 'Chưa bắt đầu',
    className: 'bg-slate-100 text-slate-700 border-slate-200',
  },
  IN_PROGRESS: {
    label: 'Đang giao',
    className: 'bg-sky-100 text-sky-800 border-sky-200',
  },
  COMPLETED: {
    label: 'Đã hoàn tất',
    className: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  },
  ABORTED: {
    label: 'Đã hủy',
    className: 'bg-rose-100 text-rose-800 border-rose-200',
  },
};

interface RouteCardProps {
  route: Route;
  onOpenDetail: (routeId: string) => void;
  showStartButton?: boolean;
  onStartRoute?: (routeId: string) => void;
  isStartLoading?: boolean;
  showCancelButton?: boolean;
  onCancelRoute?: (routeId: string) => void;
  isCancelLoading?: boolean;
}

const formatShortId = (value?: string | null) => {
  if (!value) {
    return '--';
  }

  return value.length > 12 ? `${value.slice(0, 10)}...` : value;
};

const formatNumber = (value?: number | null, suffix?: string) => {
  const safeValue = value ?? 0;

  return `${new Intl.NumberFormat('vi-VN').format(safeValue)}${
    suffix ? ` ${suffix}` : ''
  }`;
};

const StatBlock = ({ label, value }: { label: string; value: string }) => (
  <div className='rounded-lg border border-slate-200 bg-slate-50/70 px-3 py-2'>
    <p className='text-[10px] uppercase tracking-wider text-slate-500'>
      {label}
    </p>
    <p className='mt-0.5 text-sm font-semibold text-slate-900'>{value}</p>
  </div>
);

export const RouteCard = ({
  route,
  onOpenDetail,
  showStartButton = false,
  onStartRoute,
  isStartLoading = false,
  showCancelButton = false,
  onCancelRoute,
  isCancelLoading = false,
}: RouteCardProps) => {
  const statusStyle = ROUTE_STATUS_STYLES[route.status];
  const isInProgress = route.status === 'IN_PROGRESS';

  const handleOpenDetail = () => {
    onOpenDetail(route.id);
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      handleOpenDetail();
    }
  };

  return (
    <Card
      role='button'
      tabIndex={0}
      onClick={handleOpenDetail}
      onKeyDown={handleKeyDown}
      className={cn(
        'cursor-pointer border-slate-200/90 bg-white/95 shadow-sm transition hover:shadow-md focus-visible:ring-2 focus-visible:ring-sky-400',
        isInProgress &&
          'border-sky-200 bg-gradient-to-r from-sky-50/80 via-white to-cyan-50/80'
      )}
    >
      <CardContent className='p-4 sm:p-5'>
        <div className='flex flex-col gap-4 lg:grid lg:grid-cols-12 lg:items-center'>
          <div className='space-y-2 lg:col-span-4 xl:col-span-3'>
            <div className='flex flex-wrap items-center gap-2'>
              <p className='text-xs font-semibold uppercase tracking-[0.14em] text-slate-500'>
                Chuyến giao
              </p>
              <Badge variant='outline' className={statusStyle.className}>
                {statusStyle.label}
              </Badge>
            </div>

            <h3 className='text-lg font-semibold text-slate-900'>
              {formatShortId(route.id)}
            </h3>

            <p className='text-sm text-slate-600'>
              Ngày giao {route.deliveryDate}
            </p>
          </div>

          <div className='grid grid-cols-2 gap-2 sm:grid-cols-4 lg:col-span-5 xl:col-span-6'>
            <StatBlock
              label='Điểm giao'
              value={formatNumber(route.routeStopCount, 'điểm')}
            />
            <StatBlock
              label='Tổng quãng đường'
              value={formatNumber(route.totalDistanceKm, 'km')}
            />
            <StatBlock
              label='Tổng khối lượng'
              value={formatNumber(route.totalWeightLoadedKg, 'kg')}
            />
            <StatBlock
              label='Tổng kích thước'
              value={formatNumber(route.totalVolumeLoadedCbm, 'cbm')}
            />
          </div>

          <div className='flex items-center gap-2 lg:col-span-3 lg:justify-end xl:col-span-3'>
            <Button
              variant='outline'
              size='sm'
              className='h-9 border-slate-200 text-slate-700 hover:bg-slate-50'
              onClick={(event) => {
                event.stopPropagation();
                handleOpenDetail();
              }}
            >
              <MapPinned className='mr-2 h-4 w-4' />
              Chi tiết
            </Button>

            {/* Menu hành động bổ sung (Chỉ hiện nếu có ít nhất 1 nút điều khiển) */}
            {(showStartButton || showCancelButton) && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant='ghost'
                    size='icon'
                    className='h-9 w-9 rounded-full hover:bg-slate-100'
                    onClick={(e) => e.stopPropagation()} // Ngăn chặn sự kiện click vào card
                  >
                    <MoreVertical className='h-5 w-5 text-slate-500' />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align='end' className='w-48'>
                  {showStartButton && onStartRoute && (
                    <DropdownMenuItem
                      className='flex items-center gap-2 py-2.5 text-emerald-600 focus:bg-emerald-50 focus:text-emerald-700'
                      disabled={isStartLoading}
                      onClick={(e) => {
                        e.stopPropagation();
                        onStartRoute(route.id);
                      }}
                    >
                      <PlayCircle className='h-4 w-4' />
                      <span className='font-medium'>Bắt đầu giao</span>
                    </DropdownMenuItem>
                  )}

                  {showCancelButton && onCancelRoute && (
                    <DropdownMenuItem
                      className='flex items-center gap-2 py-2.5 text-rose-600 focus:bg-rose-50 focus:text-rose-700'
                      disabled={isCancelLoading}
                      onClick={(e) => {
                        e.stopPropagation();
                        onCancelRoute(route.id);
                      }}
                    >
                      <XCircle className='h-4 w-4' />
                      <span className='font-medium'>Hủy chuyến</span>
                    </DropdownMenuItem>
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
