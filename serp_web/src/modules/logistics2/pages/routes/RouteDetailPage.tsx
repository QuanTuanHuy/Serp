'use client';

import dynamic from 'next/dynamic';
import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import {
  Map,
  Clock,
  CheckCircle2,
  AlertCircle,
  PlayCircle,
  Truck,
  MapPin,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { useGetRouteDetailQuery } from '../../api/logistics2Api';

// IMPORT DYNAMIC BẢN ĐỒ KÈM SSR: FALSE
const RouteMap = dynamic(() => import('../common/RouteMap'), {
  ssr: false,
  loading: () => (
    <div className='h-[500px] w-full animate-pulse bg-slate-100 rounded-xl border flex items-center justify-center'>
      Đang tải bản đồ...
    </div>
  ),
});

const ROUTE_STATUS_CONFIG = {
  PENDING: {
    label: 'Chờ thực hiện',
    color: 'text-slate-700',
    bgColor: 'bg-slate-100',
    icon: Clock,
  },
  IN_PROGRESS: {
    label: 'Đang thực hiện',
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    icon: PlayCircle,
  },
  COMPLETED: {
    label: 'Đã hoàn thành',
    color: 'text-emerald-700',
    bgColor: 'bg-emerald-100',
    icon: CheckCircle2,
  },
  ABORTED: {
    label: 'Đã hủy',
    color: 'text-rose-700',
    bgColor: 'bg-rose-100',
    icon: AlertCircle,
  },
};

export default function RouteDetailPage({ routeId }: { routeId: string }) {
  const {
    data: routeResponse,
    isLoading,
    error,
    isError,
  } = useGetRouteDetailQuery(routeId);

  const route = routeResponse?.data;

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse space-y-4'>
          <div className='h-8 w-1/3 rounded bg-muted' />
          <div className='h-[500px] rounded bg-muted' />
        </div>
      </div>
    );
  }

  if (isError || !route) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-2 p-6 text-center'>
          <AlertCircle className='mx-auto h-10 w-10 text-destructive' />
          <h3 className='text-lg font-semibold'>Không tìm thấy lộ trình</h3>
          <p className='text-sm text-muted-foreground'>
            {(error as { message?: string } | undefined)?.message ||
              'Không thể tải chi tiết lộ trình. Vui lòng thử lại.'}
          </p>
        </CardContent>
      </Card>
    );
  }

  const statusConfig =
    ROUTE_STATUS_CONFIG[route.status as keyof typeof ROUTE_STATUS_CONFIG] ||
    ROUTE_STATUS_CONFIG.PENDING;
  const StatusIcon = statusConfig.icon;

  return (
    <div className='space-y-6'>
      {/* Header Info */}
      <div className='flex flex-col gap-4 md:flex-row md:items-start md:justify-between'>
        <div>
          <div className='mb-2 flex items-center gap-3'>
            <h1 className='text-2xl font-bold tracking-tight'>
              Chi tiết lộ trình: {route.id.substring(0, 8)}...
            </h1>
            <Badge
              variant='secondary'
              className={cn(
                'gap-1.5',
                statusConfig.bgColor,
                statusConfig.color
              )}
            >
              <StatusIcon className='h-3 w-3' />
              <span>{statusConfig.label}</span>
            </Badge>
          </div>
          <p className='text-sm text-muted-foreground'>
            Ngày giao: {route.deliveryDate} • Kế hoạch: {route.deliveryPlanId}
          </p>
        </div>
      </div>

      {/* Thông số tổng quan */}
      <div className='grid gap-4 md:grid-cols-4'>
        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>
              Tổng quãng đường
            </CardTitle>
            <Map className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>{route.totalDistanceKm} km</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>Điểm giao</CardTitle>
            <MapPin className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>
              {route.routeStopCount} điểm
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
            <CardTitle className='text-sm font-medium'>
              Khối lượng tải
            </CardTitle>
            <Truck className='h-4 w-4 text-muted-foreground' />
          </CardHeader>
          <CardContent>
            <div className='text-2xl font-bold'>
              {route.totalWeightLoadedKg} kg
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Bản đồ Giao hàng */}
      <Card>
        <CardHeader>
          <CardTitle>Bản đồ lộ trình di chuyển</CardTitle>
        </CardHeader>
        <CardContent className='p-0 sm:p-6 sm:pt-0'>
          <RouteMap route={route} />
        </CardContent>
      </Card>
    </div>
  );
}
