/**
 * Outbound Shipment Detail Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Outbound shipment detail (read-only)
 */

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  Label,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Clock3,
  Grid3X3,
  List,
  Package,
  PanelsTopLeft,
  Plus,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetDeliverySlipsQuery,
  useGetOrderDetailQuery,
  useGetOutboundShipmentDetailQuery,
} from '../../api/logistics2Api';
import type { DeliverySlip } from '../../types';
import { useGetUsersQuery } from '@/modules/admin/services/users/usersApi';
import { DeliverySlipCard } from '../../components/cards/DeliverySlipCard';

interface OutboundShipmentDetailPageProps {
  shipmentId: string;
  initialTab?: DetailTab;
}

type DetailTab = 'overview' | 'items' | 'delivery-slips';

const STATUS_CONFIG = {
  CREATED: {
    label: 'Nháp',
    color: 'text-blue-700 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
    icon: Clock3,
  },
  READY_TO_EXPORT: {
    label: 'Sẵn sàng xuất kho',
    color: 'text-amber-700 dark:text-amber-400',
    bgColor: 'bg-amber-100 dark:bg-amber-900/30',
    icon: Truck,
  },
  DELIVERED: {
    label: 'Đã xuất kho',
    color: 'text-emerald-700 dark:text-emerald-400',
    bgColor: 'bg-emerald-100 dark:bg-emerald-900/30',
    icon: CheckCircle2,
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatFullname = (
  user: { firstName?: string; lastName?: string } | undefined
) => {
  if (!user) return 'N/A';
  return `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'N/A';
};

const formatNumber = (value?: number) => {
  if (!value) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

export const OutboundShipmentDetailPage: React.FC<
  OutboundShipmentDetailPageProps
> = ({ shipmentId, initialTab = 'overview' }) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<DetailTab>(initialTab);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  const {
    data: shipmentResponse,
    isLoading,
    isError,
  } = useGetOutboundShipmentDetailQuery(shipmentId);

  const shipment = shipmentResponse?.data;

  const { data: orderResponse } = useGetOrderDetailQuery(
    shipment?.orderId || '',
    {
      skip: !shipment?.orderId,
    }
  );

  const {
    data: deliverySlipsResponse,
    isLoading: isLoadingDeliverySlips,
    error: deliverySlipsError,
  } = useGetDeliverySlipsQuery(
    {
      filters: {
        outboundShipmentId: shipmentId,
      },
      pagination: {
        page: 0,
        size: 100,
      },
    },
    {
      skip: !shipment,
    }
  );

  const deliverySlips = deliverySlipsResponse?.data?.items || [];

  const userIds = shipment?.createdByUserId ? [shipment.createdByUserId] : [];
  const { data: usersResponse } = useGetUsersQuery(
    {
      page: 0,
      pageSize: 100,
    },
    {
      skip: userIds.length === 0,
    }
  );

  const userMap = useMemo(
    () => new Map(usersResponse?.data?.items?.map((user) => [user.id, user])),
    [usersResponse]
  );

  const order = orderResponse?.data;

  const statusConfig = shipment
    ? STATUS_CONFIG[shipment.status as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;

  const StatusIcon = statusConfig.icon;

  const deliveredSlips = deliverySlips.filter(
    (slip) => slip.status === 'DELIVERED'
  ).length;

  const canCreateDeliverySlip = shipment?.status === 'READY_TO_EXPORT';

  const totalItemQuantity = (shipment?.items || []).reduce(
    (sum, item) => sum + item.quantity,
    0
  );

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse space-y-4'>
          <div className='h-8 w-1/3 rounded bg-muted' />
          <div className='h-64 rounded bg-muted' />
        </div>
      </div>
    );
  }

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='space-y-4 p-8 text-center'>
          <AlertCircle className='mx-auto h-12 w-12 text-destructive' />
          <h3 className='text-lg font-semibold'>Không tìm thấy phiếu xuất</h3>
          <p className='text-muted-foreground'>
            Phiếu xuất không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics2/outbound-shipments')}>
            <ArrowLeft className='mr-2 h-4 w-4' />
            Quay lai danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 md:flex-row md:items-center md:justify-between'>
        <div className='flex items-start gap-3'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics2/outbound-shipments')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div>
            <div className='mb-2 flex flex-wrap items-center gap-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {shipment.name}
              </h1>
              <Badge
                variant='secondary'
                className={cn('gap-1.5', statusConfig.bgColor)}
              >
                <StatusIcon className={cn('h-3 w-3', statusConfig.color)} />
                <span className={statusConfig.color}>{statusConfig.label}</span>
              </Badge>
            </div>
            <p className='text-sm text-muted-foreground'>
              ID: {shipment.id} • Tạo ngày: {formatDate(shipment.createdStamp)}
            </p>
          </div>
        </div>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={(value) => setActiveTab(value as DetailTab)}
      >
        <TabsList>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='items'>
            Mặt hàng ({shipment.items?.length || 0})
          </TabsTrigger>
          <TabsTrigger value='delivery-slips'>
            Đơn giao hàng ({deliverySlips.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='mt-6 space-y-6'>
          <div className='grid gap-6 lg:grid-cols-3'>
            <div className='space-y-6 lg:col-span-2'>
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Thông tin tổng quan</h3>
                </CardHeader>
                <CardContent className='grid gap-4 sm:grid-cols-2'>
                  <div>
                    <Label className='text-muted-foreground'>Đơn hàng</Label>
                    <p className='font-medium'>
                      {order?.orderName || shipment.orderId}
                    </p>
                  </div>
                  <div>
                    <Label className='text-muted-foreground'>Kho xuất</Label>
                    <p className='font-medium'>
                      {shipment.facility?.name || shipment.facilityId}
                    </p>
                  </div>
                  <div>
                    <Label className='text-muted-foreground'>Người tạo</Label>
                    <p>
                      {formatFullname(
                        shipment.createdByUserId
                          ? userMap.get(shipment.createdByUserId)
                          : undefined
                      )}
                    </p>
                  </div>
                  <div>
                    <Label className='text-muted-foreground'>
                      Cập nhật lần cuối
                    </Label>
                    <p>{formatDate(shipment.lastUpdatedStamp)}</p>
                  </div>
                </CardContent>
              </Card>
            </div>

            <div className='space-y-6'>
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <PanelsTopLeft className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Thống kê phiếu xuất</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-3 text-sm'>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Số mặt hàng</span>
                    <span className='font-medium'>
                      {shipment.items?.length || 0}
                    </span>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Tổng số lượng</span>
                    <span className='font-medium'>
                      {formatNumber(totalItemQuantity)}
                    </span>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Đơn giao hàng</span>
                    <span className='font-medium'>{deliverySlips.length}</span>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Đã giao xong</span>
                    <span className='font-medium text-emerald-700 dark:text-emerald-300'>
                      {deliveredSlips}
                    </span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        <TabsContent value='items' className='mt-6 space-y-6'>
          <Card>
            <CardHeader>
              <div className='flex items-center gap-2'>
                <Package className='h-5 w-5 text-primary' />
                <h3 className='font-semibold'>Danh sách mặt hàng</h3>
              </div>
            </CardHeader>
            <CardContent>
              {(shipment.items || []).length > 0 ? (
                <div className='space-y-3'>
                  {(shipment.items || []).map((item) => (
                    <div
                      key={item.id}
                      className='rounded-lg border p-4 transition-colors hover:bg-muted/40'
                    >
                      <div className='flex flex-col gap-4 md:flex-row md:items-center'>
                        <div className='md:w-1/4 md:min-w-[200px]'>
                          <p className='font-medium'>
                            {item.product?.name || item.productId}
                          </p>
                        </div>
                        <div className='grid flex-1 grid-cols-2 gap-4 text-sm md:grid-cols-5'>
                          <div>
                            <Label className='text-muted-foreground'>
                              Số lượng xuất
                            </Label>
                            <p className='font-medium'>
                              {formatNumber(item.quantity)}
                            </p>
                          </div>
                          <div>
                            <Label className='text-muted-foreground'>
                              Chưa tạo phiếu giao
                            </Label>
                            <p className='font-medium'>
                              {formatNumber(item.quantityRemaining || 0)}
                            </p>
                          </div>
                          <div>
                            <Label className='text-muted-foreground'>
                              Lô hàng
                            </Label>
                            <p className='font-medium'>
                              {item.inventoryItem?.lotId || 'N/A'}
                            </p>
                          </div>
                          <div>
                            <Label className='text-muted-foreground'>
                              Ngày sản xuất
                            </Label>
                            <p className='font-medium'>
                              {formatDate(
                                item.inventoryItem?.manufacturingDate
                              )}
                            </p>
                          </div>
                          <div>
                            <Label className='text-muted-foreground'>
                              Hạn sử dụng
                            </Label>
                            <p className='font-medium'>
                              {formatDate(item.inventoryItem?.expirationDate)}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='rounded-lg border border-dashed py-12 text-center text-muted-foreground'>
                  Chưa có mặt hàng trong phiếu xuất
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='delivery-slips' className='mt-6 space-y-6'>
          <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
            <div>
              <h3 className='text-lg font-semibold'>Danh sách đơn giao hàng</h3>
              <p className='text-sm text-muted-foreground'>
                {canCreateDeliverySlip
                  ? `${deliverySlips.length} đơn giao hàng thuộc phiếu xuất này`
                  : 'Chỉ có thể tạo đơn giao hàng khi phiếu xuất ở trạng thái READY_TO_EXPORT.'}
              </p>
            </div>

            <div className='flex items-center gap-2'>
              <Button
                onClick={() =>
                  router.push(
                    `/logistics2/outbound-shipments/${shipment.id}/delivery-slips/create`
                  )
                }
                disabled={!canCreateDeliverySlip}
              >
                <Plus className='mr-2 h-4 w-4' />
                Tạo đơn giao hàng
              </Button>

              {deliverySlips.length > 0 && (
                <div className='flex rounded-lg border bg-muted p-1'>
                  <button
                    type='button'
                    onClick={() => setViewMode('grid')}
                    className={cn(
                      'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
                      viewMode === 'grid'
                        ? 'bg-background shadow-sm'
                        : 'hover:bg-background/50'
                    )}
                  >
                    <Grid3X3 className='h-4 w-4' />
                  </button>
                  <button
                    type='button'
                    onClick={() => setViewMode('list')}
                    className={cn(
                      'flex items-center justify-center h-8 w-8 rounded-md transition-colors',
                      viewMode === 'list'
                        ? 'bg-background shadow-sm'
                        : 'hover:bg-background/50'
                    )}
                  >
                    <List className='h-4 w-4' />
                  </button>
                </div>
              )}
            </div>
          </div>

          {isLoadingDeliverySlips && (
            <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
              {Array.from({ length: 4 }).map((_, index) => (
                <Card key={index} className='animate-pulse'>
                  <CardContent className='p-5'>
                    <div className='mb-3 h-4 w-1/2 rounded bg-muted' />
                    <div className='space-y-2'>
                      <div className='h-3 w-full rounded bg-muted' />
                      <div className='h-3 w-2/3 rounded bg-muted' />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {deliverySlipsError && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardContent className='p-6 text-center text-destructive'>
                Đã xảy ra lỗi khi tải danh sách đơn giao hàng.
              </CardContent>
            </Card>
          )}

          {!isLoadingDeliverySlips &&
            !deliverySlipsError &&
            deliverySlips.length > 0 && (
              <div className='space-y-4'>
                <div
                  className={cn(
                    'gap-4',
                    viewMode === 'grid'
                      ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3'
                      : 'flex flex-col'
                  )}
                >
                  {deliverySlips.map((slip: DeliverySlip) => (
                    <DeliverySlipCard
                      key={slip.id}
                      slip={slip}
                      viewMode={viewMode}
                      onClick={() =>
                        router.push(`/logistics2/delivery-slips/${slip.id}`)
                      }
                    />
                  ))}
                </div>
              </div>
            )}

          {!isLoadingDeliverySlips &&
            !deliverySlipsError &&
            deliverySlips.length === 0 && (
              <Card>
                <CardContent className='py-16 text-center'>
                  <div className='mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full bg-muted'>
                    <Truck className='h-10 w-10 text-muted-foreground' />
                  </div>
                  <h3 className='mb-2 text-lg font-semibold'>
                    Chưa có đơn giao hàng nào
                  </h3>
                  <p className='mx-auto max-w-sm text-muted-foreground'>
                    Phiếu xuất này chưa được tách thành đơn giao hàng.
                  </p>
                </CardContent>
              </Card>
            )}
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default OutboundShipmentDetailPage;
