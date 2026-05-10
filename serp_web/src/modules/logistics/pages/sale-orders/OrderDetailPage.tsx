'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Label,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Phone,
  MapPin,
  Calendar,
  User,
  Package,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  FileText,
  TrendingUp,
  Truck,
  PanelsTopLeft,
  Mail,
  Warehouse,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetOutboundShipmentsQuery,
  useGetCustomerQuery,
  useGetFacilitiesQuery,
  useGetSaleOrderQuery,
} from '../../api/logisticsApi';
import { UserProfile, useGetUsersQuery } from '@/modules/admin';
import type {
  InventoryItemDetail,
  OrderItemEntity,
  OutboundShipment,
} from '../../types';
import { OutboundShipmentCard } from '../../components/cards/OutboundShipmentCard';

interface OrderDetailPageProps {
  orderId: string;
}

type InventoryDetailSegmentItem = {
  detail: InventoryItemDetail;
  productName: string;
  skuCode?: string;
  unit?: string;
  orderItemId: string;
  orderItemSeqId: number;
};

type InventoryDetailSegment = {
  facilityId: string;
  facilityName?: string;
  details: InventoryDetailSegmentItem[];
  totalQuantity: number;
  totalRemainingQuantity: number;
};

// Order status configuration
const STATUS_CONFIG = {
  CREATED: {
    label: 'Đã tạo',
    color: 'text-blue-700 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
    icon: Clock,
  },
  APPROVED: {
    label: 'Đã phê duyệt',
    color: 'text-emerald-700 dark:text-emerald-400',
    bgColor: 'bg-emerald-100 dark:bg-emerald-900/30',
    icon: CheckCircle2,
  },
  CANCELLED: {
    label: 'Đã hủy',
    color: 'text-rose-700 dark:text-rose-400',
    bgColor: 'bg-rose-100 dark:bg-rose-900/30',
    icon: XCircle,
  },
  FULLY_DELIVERED: {
    label: 'Đã nhận hàng',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: Package,
  },
};

const formatCurrency = (value?: number) => {
  if (!value) return '0 đ';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value);
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatNumber = (value?: number) => {
  return new Intl.NumberFormat('vi-VN').format(value ?? 0);
};

const formatFullname = (user: UserProfile | undefined) => {
  if (!user) return 'N/A';
  return `${user.firstName || ''} ${user.lastName || ''}`.trim();
};

export const OrderDetailPage: React.FC<OrderDetailPageProps> = ({
  orderId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch order data
  const {
    data: orderResponse,
    isLoading,
    isError,
  } = useGetSaleOrderQuery(orderId);
  const order = orderResponse?.data;

  // Keep hooks execution order stable across renders.
  const {
    data: shipmentsResponse,
    isLoading: isLoadingShipments,
    error: shipmentsError,
  } = useGetOutboundShipmentsQuery(
    {
      filters: { orderId: orderId },
      pagination: { page: 0, size: 100 },
    },
    {
      skip: !order,
    }
  );

  const shipments = shipmentsResponse?.data?.items || [];

  const customerId = order?.toCustomerId || '';
  const { data: customersResponse } = useGetCustomerQuery(customerId, {
    skip: !customerId,
  });
  const customer = customersResponse?.data;

  const { data: facilitiesResponse } = useGetFacilitiesQuery({
    filters: {},
    pagination: { page: 0, size: 100 },
  });

  // Collect unique user IDs from order
  const userIds = [
    order?.createdByUserId,
    order?.userApprovedId,
    order?.userCancelledId,
  ].filter((id): id is number => !!id);

  // Fetch users data - get all users and filter by IDs
  const { data: usersResponse } = useGetUsersQuery(
    {
      page: 0,
      pageSize: 100,
    },
    { skip: userIds.length === 0 }
  );

  // Create user map for quick lookup
  const userMap = new Map(
    usersResponse?.data?.items?.map((user) => [user.id, user]) || []
  );

  const facilityMap = useMemo(() => {
    const map = new Map<string, string>();

    facilitiesResponse?.data?.items?.forEach((facility) => {
      map.set(facility.id, facility.name);
    });

    return map;
  }, [facilitiesResponse]);

  const inventoryDetailSegments = useMemo<InventoryDetailSegment[]>(() => {
    if (!order?.items?.length) {
      return [];
    }

    const segmentMap = new Map<string, InventoryDetailSegment>();

    order.items.forEach((orderItem: OrderItemEntity) => {
      const productName = orderItem.product?.name || orderItem.productId;
      const skuCode = orderItem.product?.skuCode;
      const unit = orderItem.product?.unit || orderItem.unit;

      (orderItem.allocatedInventoryItems || []).forEach((detail) => {
        const facilityId = detail.facilityId || 'UNASSIGNED';
        const remainingQuantity = detail.notYetOutboundQuantity ?? 0;

        const segment =
          segmentMap.get(facilityId) ||
          ({
            facilityId,
            facilityName: facilityMap.get(facilityId),
            details: [],
            totalQuantity: 0,
            totalRemainingQuantity: 0,
          } as InventoryDetailSegment);

        segment.facilityName =
          facilityMap.get(facilityId) || segment.facilityName;

        segment.details.push({
          detail,
          productName,
          skuCode,
          unit,
          orderItemId: orderItem.id,
          orderItemSeqId: orderItem.orderItemSeqId,
        });

        segment.totalQuantity += detail.quantity;
        segment.totalRemainingQuantity += remainingQuantity;
        segmentMap.set(facilityId, segment);
      });
    });

    return Array.from(segmentMap.values()).sort((left, right) =>
      left.facilityId.localeCompare(right.facilityId)
    );
  }, [facilityMap, order?.items]);

  const inventoryDetailCount = inventoryDetailSegments.reduce(
    (count, segment) => count + segment.details.length,
    0
  );

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='animate-pulse'>
          <div className='h-8 bg-muted rounded w-1/3 mb-4' />
          <div className='h-64 bg-muted rounded' />
        </div>
      </div>
    );
  }

  if (isError || !order) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Không tìm thấy đơn hàng
          </h3>
          <p className='text-muted-foreground mb-4'>
            Đơn hàng không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.push('/logistics/sale-orders')}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại danh sách
          </Button>
        </CardContent>
      </Card>
    );
  }

  const statusConfig = order
    ? STATUS_CONFIG[order.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;
  const StatusIcon = statusConfig.icon;

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div className='flex items-center gap-4'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics/sale-orders')}
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <div className='flex items-center gap-3 mb-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {order.orderName || `Đơn hàng #${order.id?.slice(0, 8)}...`}
              </h1>
              <Badge
                variant='secondary'
                className={cn('gap-1.5', statusConfig.bgColor)}
              >
                <StatusIcon className={cn('h-3 w-3', statusConfig.color)} />
                <span className={statusConfig.color}>{statusConfig.label}</span>
              </Badge>
            </div>
            <p className='text-muted-foreground text-sm'>
              ID: {order.id} • Ngày đặt: {formatDate(order.orderDate)}
            </p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='items'>
            Tồn kho ({inventoryDetailCount})
          </TabsTrigger>
          <TabsTrigger value='shipments'>
            Phiếu kho ({shipments.length})
          </TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='space-y-6 mt-6'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            {/* Left Column - Order Details */}
            <div className='lg:col-span-2 space-y-6'>
              {/* Customer Information */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <User className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Khách hàng</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  {customer ? (
                    <>
                      <p className='font-medium'>{customer.name}</p>
                      {customer.email && (
                        <div className='flex items-center gap-2'>
                          <Mail className='h-4 w-4 text-muted-foreground' />
                          <span>{customer.email}</span>
                        </div>
                      )}
                      {customer.phone && (
                        <div className='flex items-center gap-2'>
                          <Phone className='h-4 w-4 text-muted-foreground' />
                          <span>{customer.phone}</span>
                        </div>
                      )}
                      {customer.address?.fullAddress && (
                        <div className='flex items-start gap-2'>
                          <MapPin className='h-4 w-4 text-muted-foreground mt-1' />
                          <span className='text-sm'>
                            {customer.address.fullAddress}
                          </span>
                        </div>
                      )}
                    </>
                  ) : (
                    <p className='text-muted-foreground'>
                      Không có thông tin khách hàng
                    </p>
                  )}
                </CardContent>
              </Card>

              {/* Delivery Information */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <Truck className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Thông tin giao nhận</h3>
                  </div>
                </CardHeader>
                <CardContent className='grid gap-4'>
                  <div className='grid grid-cols-2 gap-4'>
                    <div>
                      <Label className='text-muted-foreground'>
                        Giao sau ngày
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <Calendar className='h-4 w-4 text-muted-foreground' />
                        <span>{formatDate(order.deliveryAfterDate)}</span>
                      </div>
                    </div>
                    <div>
                      <Label className='text-muted-foreground'>
                        Giao trước ngày
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <Calendar className='h-4 w-4 text-muted-foreground' />
                        <span>{formatDate(order.deliveryBeforeDate)}</span>
                      </div>
                    </div>
                  </div>

                  {order.deliveryFullAddress && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Địa chỉ giao hàng
                      </Label>
                      <div className='flex items-start gap-2 mt-1'>
                        <MapPin className='h-4 w-4 text-muted-foreground mt-1' />
                        <span>{order.deliveryFullAddress}</span>
                      </div>
                    </div>
                  )}

                  {order.deliveryPhone && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Số điện thoại liên hệ
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <Phone className='h-4 w-4 text-muted-foreground' />
                        <span>{order.deliveryPhone}</span>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Additional Information */}
              {order.note && (
                <Card>
                  <CardHeader>
                    <div className='flex items-center gap-2'>
                      <FileText className='h-5 w-5 text-primary' />
                      <h3 className='font-semibold'>Ghi chú</h3>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className='text-muted-foreground whitespace-pre-wrap'>
                      {order.note}
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>

            {/* Right Column - Summary */}
            <div className='space-y-6'>
              {/* Order Summary */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <PanelsTopLeft className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Tổng quan</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>Mức ưu tiên</span>
                    <Badge variant='outline'>
                      <TrendingUp className='h-3 w-3 mr-1' />
                      {order.priority}
                    </Badge>
                  </div>
                  <div className='flex items-center justify-between'>
                    <span className='text-muted-foreground'>
                      Số lượng sản phẩm
                    </span>
                    <span className='font-medium'>
                      {order.items?.length || 0}
                    </span>
                  </div>

                  <div className='border-t pt-4'>
                    <div className='flex items-center justify-between text-lg font-semibold'>
                      <span>Tổng thành tiền</span>
                      <span className='text-primary'>
                        {formatCurrency(order.totalAmount)}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Order Metadata */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Thông tin chi tiết</h3>
                </CardHeader>
                <CardContent className='space-y-3 text-sm'>
                  <div>
                    <Label className='text-muted-foreground'>Ngày tạo</Label>
                    <p>{formatDate(String(order.createdStamp))}</p>
                  </div>

                  {order.lastUpdatedStamp && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Cập nhật lần cuối
                      </Label>
                      <p>{formatDate(String(order.lastUpdatedStamp))}</p>
                    </div>
                  )}

                  {order.createdByUserId && (
                    <div>
                      <Label className='text-muted-foreground'>Người tạo</Label>
                      <p className='font-bold'>
                        {formatFullname(userMap.get(order.createdByUserId))}
                      </p>
                    </div>
                  )}

                  {order.userApprovedId && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Người phê duyệt
                      </Label>
                      <p className='font-bold text-emerald-700 dark:text-emerald-400'>
                        {formatFullname(userMap.get(order.userApprovedId))}
                      </p>
                    </div>
                  )}

                  {order.userCancelledId && (
                    <div>
                      <Label className='text-muted-foreground'>Người hủy</Label>
                      <p className=' font-bold text-destructive'>
                        {formatFullname(userMap.get(order.userCancelledId))}
                      </p>
                    </div>
                  )}

                  {order.cancellationNote && (
                    <div>
                      <Label className='text-muted-foreground'>Lý do hủy</Label>
                      <p className='text-destructive'>
                        {order.cancellationNote}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        {/* Items Tab */}
        <TabsContent value='items' className='space-y-6 mt-6'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Chi tiết phân bổ tồn kho</h3>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {inventoryDetailSegments.length > 0 ? (
                <div className='space-y-4'>
                  {inventoryDetailSegments.map((segment) => (
                    <div
                      key={segment.facilityId}
                      className='overflow-hidden rounded-2xl border bg-gradient-to-br from-background to-muted/20 shadow-sm'
                    >
                      <div className='flex flex-col gap-4 border-b bg-muted/30 px-5 py-4 sm:flex-row sm:items-center sm:justify-between'>
                        <div className='flex items-start gap-3'>
                          <div className='flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10 text-primary'>
                            <Warehouse className='h-5 w-5' />
                          </div>
                          <div>
                            <p className='text-xs font-medium uppercase tracking-[0.2em] text-muted-foreground'>
                              Kho
                            </p>
                            <h4 className='text-lg font-semibold tracking-tight'>
                              {segment.facilityName || segment.facilityId}
                            </h4>
                          </div>
                        </div>
                      </div>

                      <div className='divide-y'>
                        {segment.details.map((entry) => {
                          const remainingQuantity =
                            entry.detail.notYetOutboundQuantity ?? 0;
                          const manufacturingDate =
                            entry.detail.manufacturingDate ||
                            entry.detail.inventoryItem?.manufacturingDate;
                          const expirationDate =
                            entry.detail.expirationDate ||
                            entry.detail.inventoryItem?.expirationDate;

                          return (
                            <div
                              key={entry.detail.id}
                              className='grid gap-4 px-5 py-4 xl:grid-cols-[minmax(0,1.9fr)_repeat(4,minmax(0,1fr))]'
                            >
                              <div className='space-y-2'>
                                <div className='flex flex-wrap items-center gap-2'>
                                  <p className='text-base font-semibold text-foreground'>
                                    {entry.productName}
                                  </p>

                                  {remainingQuantity > 0 ? (
                                    <Badge className='bg-emerald-100 text-emerald-700 hover:bg-emerald-100 dark:bg-emerald-900/30 dark:text-emerald-400'>
                                      Còn tồn
                                    </Badge>
                                  ) : (
                                    <Badge variant='secondary'>
                                      Đã xuất hết
                                    </Badge>
                                  )}
                                </div>
                                <div className='rounded-xl bg-muted/40 px-3 py-2 text-sm text-muted-foreground'>
                                  <span className='font-medium text-foreground'>
                                    Lô:
                                  </span>{' '}
                                  {entry.detail.lotId || 'N/A'}
                                </div>
                              </div>

                              <div className='rounded-xl border bg-background/80 px-4 py-3'>
                                <p className='text-xs text-muted-foreground'>
                                  Ngày sản xuất
                                </p>
                                <p className='mt-1 font-medium'>
                                  {formatDate(manufacturingDate)}
                                </p>
                              </div>

                              <div className='rounded-xl border bg-background/80 px-4 py-3'>
                                <p className='text-xs text-muted-foreground'>
                                  Hạn sử dụng
                                </p>
                                <p className='mt-1 font-medium'>
                                  {formatDate(expirationDate)}
                                </p>
                              </div>

                              <div className='rounded-xl border bg-background/80 px-4 py-3'>
                                <p className='text-xs text-muted-foreground'>
                                  Số lượng
                                </p>
                                <p className='mt-1 text-lg font-semibold'>
                                  {formatNumber(entry.detail.quantity)}{' '}
                                  {entry.unit || ''}
                                </p>
                              </div>

                              <div className='rounded-xl border bg-background/80 px-4 py-3'>
                                <p className='text-xs text-muted-foreground'>
                                  Chưa xuất kho
                                </p>
                                <p className='mt-1 text-lg font-semibold text-emerald-700 dark:text-emerald-400'>
                                  {formatNumber(remainingQuantity)}{' '}
                                  {entry.unit || ''}
                                </p>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='rounded-2xl border border-dashed border-muted-foreground/25 bg-muted/10 py-16 text-center text-muted-foreground'>
                  <Package className='mx-auto mb-4 h-12 w-12 opacity-40' />
                  <p className='font-medium text-foreground'>
                    Chưa có dữ liệu tồn kho được phân bổ
                  </p>
                  <p className='mt-2 text-sm'>
                    Danh sách này sẽ hiển thị khi các order item đã được gán
                    inventory detail theo từng kho.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Shipments Tab */}
        <TabsContent value='shipments' className='space-y-6 mt-6'>
          {/* Loading State */}
          {isLoadingShipments && (
            <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
              {Array.from({ length: 3 }).map((_, index) => (
                <Card key={index} className='animate-pulse'>
                  <CardContent className='p-5'>
                    <div className='flex items-center gap-3 mb-4'>
                      <div className='h-10 w-10 bg-muted rounded-lg' />
                      <div className='flex-1'>
                        <div className='h-4 bg-muted rounded w-3/4 mb-2' />
                        <div className='h-3 bg-muted rounded w-1/2' />
                      </div>
                    </div>
                    <div className='space-y-2'>
                      <div className='h-3 bg-muted rounded w-full' />
                      <div className='h-3 bg-muted rounded w-2/3' />
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {/* Error State */}
          {shipmentsError && (
            <Card className='border-destructive/50 bg-destructive/5'>
              <CardContent className='p-6 text-center'>
                <p className='text-destructive'>
                  Đã xảy ra lỗi khi tải phiếu xuất kho. Vui lòng thử lại sau.
                </p>
              </CardContent>
            </Card>
          )}

          {/* Shipments List */}
          {!isLoadingShipments && !shipmentsError && shipments.length > 0 && (
            <div className='space-y-4'>
              <div className='flex items-center justify-between'>
                <div>
                  <h3 className='text-lg font-semibold'>
                    Danh sách phiếu xuất kho
                  </h3>
                  <p className='text-sm text-muted-foreground'>
                    {shipments.length} phiếu xuất kho
                  </p>
                </div>
                <Button
                  onClick={() =>
                    router.push(
                      `/logistics/outbound-shipments/new?orderId=${orderId}`
                    )
                  }
                >
                  <Truck className='h-4 w-4 mr-2' />
                  Tạo phiếu xuất mới
                </Button>
              </div>

              <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
                {shipments.map((shipment: OutboundShipment) => (
                  <OutboundShipmentCard
                    key={shipment.id}
                    shipment={shipment}
                    order={order}
                    customer={customer}
                    onClick={() =>
                      router.push(
                        `/logistics/outbound-shipments/${shipment.id}`
                      )
                    }
                  />
                ))}
              </div>
            </div>
          )}

          {/* Empty State */}
          {!isLoadingShipments &&
            !shipmentsError &&
            shipments.length === 0 &&
            order.statusId === 'APPROVED' && (
              <Card>
                <CardContent className='py-16 text-center'>
                  <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                    <Truck className='w-10 h-10 text-muted-foreground' />
                  </div>
                  <h3 className='text-lg font-semibold mb-2'>
                    Chưa có phiếu xuất kho nào
                  </h3>
                  <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                    Tạo phiếu xuất kho đầu tiên ngay bây giờ.
                  </p>
                  <Button
                    onClick={() =>
                      router.push(
                        `/logistics/outbound-shipments/new?orderId=${orderId}`
                      )
                    }
                  >
                    <Truck className='h-4 w-4 mr-2' />
                    Tạo phiếu xuất kho đầu tiên
                  </Button>
                </CardContent>
              </Card>
            )}

          {!isLoadingShipments &&
            !shipmentsError &&
            shipments.length === 0 &&
            order.statusId !== 'APPROVED' && (
              <Card>
                <CardContent className='py-16 text-center'>
                  <div className='mx-auto w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
                    <Truck className='w-10 h-10 text-muted-foreground' />
                  </div>
                  <h3 className='text-lg font-semibold mb-2'>
                    Chưa có phiếu xuất kho nào
                  </h3>
                  <p className='text-muted-foreground mb-6 max-w-sm mx-auto'>
                    Cần phê duyệt đơn hàng trước khi tạo phiếu xuất kho.
                  </p>
                </CardContent>
              </Card>
            )}
        </TabsContent>
      </Tabs>
    </div>
  );
};
