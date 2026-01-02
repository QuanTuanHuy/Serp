/**
 * Shipment Detail Page - Purchase Module (View Only)
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment detail view (read-only)
 */

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
  AlertCircle,
  FileText,
  Building2,
  Truck,
  ArrowDownToLine,
  ArrowUpFromLine,
  ReceiptText,
  Mail,
  Warehouse,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetShipmentQuery,
  useGetSupplierQuery,
  useGetProductsQuery,
} from '../../../api/purchaseApi';
import type { Shipment, InventoryItemDetail } from '../../../types';
import { useGetUsersQuery } from '@/modules/admin';

interface ShipmentDetailPageProps {
  shipmentId: string;
}

const STATUS_CONFIG = {
  CREATED: {
    label: 'Nháp',
    color: 'text-blue-700 dark:text-blue-400',
    bgColor: 'bg-blue-100 dark:bg-blue-900/30',
    icon: Clock,
  },
  IMPORTED: {
    label: 'Đã nhập kho',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: ArrowDownToLine,
  },
  EXPORTED: {
    label: 'Đã xuất kho',
    color: 'text-purple-700 dark:text-purple-400',
    bgColor: 'bg-purple-100 dark:bg-purple-900/30',
    icon: ArrowUpFromLine,
  },
};

const TYPE_CONFIG = {
  INBOUND: {
    label: 'Phiếu nhập',
    color: 'text-green-700 dark:text-green-400',
    bgColor: 'bg-green-100 dark:bg-green-900/30',
    icon: ReceiptText,
  },
  OUTBOUND: {
    label: 'Phiếu xuất',
    color: 'text-orange-700 dark:text-orange-400',
    bgColor: 'bg-orange-100 dark:bg-orange-900/30',
    icon: ReceiptText,
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

export const ShipmentDetailPage: React.FC<ShipmentDetailPageProps> = ({
  shipmentId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');

  const {
    data: shipmentResponse,
    isLoading,
    isError,
  } = useGetShipmentQuery(shipmentId);

  const shipment = shipmentResponse?.data;

  // Fetch supplier data
  const { data: supplierResponse } = useGetSupplierQuery(
    shipment?.fromSupplierId || '',
    { skip: !shipment?.fromSupplierId }
  );

  const supplier = supplierResponse?.data;

  // Collect unique user IDs from shipment
  const userIds = [shipment?.createdByUserId, shipment?.handledByUserId].filter(
    (id): id is number => !!id
  );

  // Fetch users data
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

  // Collect unique product IDs
  const productIds = useMemo(() => {
    return (
      shipment?.items
        ?.map((item) => item.productId)
        .filter((id): id is string => !!id) || []
    );
  }, [shipment]);

  // Fetch products data
  const { data: productsResponse } = useGetProductsQuery(
    {
      filters: {},
      pagination: { page: 0, size: 100 },
    },
    { skip: productIds.length === 0 }
  );

  // Create product map for quick lookup
  const productMap = useMemo(() => {
    const map = new Map();
    productsResponse?.data?.items?.forEach((product) => {
      map.set(product.id, product);
    });
    return map;
  }, [productsResponse]);

  const statusConfig = shipment
    ? STATUS_CONFIG[shipment.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.CREATED
    : STATUS_CONFIG.CREATED;

  const typeConfig = shipment
    ? TYPE_CONFIG[shipment.shipmentTypeId as keyof typeof TYPE_CONFIG] ||
      TYPE_CONFIG.INBOUND
    : TYPE_CONFIG.INBOUND;

  const StatusIcon = statusConfig.icon;
  const TypeIcon = typeConfig.icon;

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

  if (isError || !shipment) {
    return (
      <Card className='border-destructive/50 bg-destructive/5'>
        <CardContent className='p-8 text-center'>
          <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
          <h3 className='text-lg font-semibold mb-2'>
            Không tìm thấy phiếu kho
          </h3>
          <p className='text-muted-foreground mb-4'>
            Phiếu kho không tồn tại hoặc đã bị xóa.
          </p>
          <Button onClick={() => router.back()}>
            <ArrowLeft className='h-4 w-4 mr-2' />
            Quay lại
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      {/* Header Section */}
      <div className='flex items-center justify-between'>
        <div className='flex items-center gap-4'>
          <Button variant='ghost' size='icon' onClick={() => router.back()}>
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <div className='flex items-center gap-3 mb-2'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {shipment.shipmentName ||
                  `Phiếu #${shipment.id?.slice(0, 8)}...`}
              </h1>
              <Badge
                variant='outline'
                className={cn('gap-1.5', typeConfig.bgColor)}
              >
                <TypeIcon className={cn('h-3 w-3', typeConfig.color)} />
                <span className={typeConfig.color}>{typeConfig.label}</span>
              </Badge>
              <Badge
                variant='secondary'
                className={cn('gap-1.5', statusConfig.bgColor)}
              >
                <StatusIcon className={cn('h-3 w-3', statusConfig.color)} />
                <span className={statusConfig.color}>{statusConfig.label}</span>
              </Badge>
            </div>
            <p className='text-muted-foreground text-sm'>
              ID: {shipment.id} • Tạo lúc: {formatDate(shipment.createdStamp)}
            </p>
          </div>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
          <TabsTrigger value='items'>
            Sản phẩm ({shipment.items?.length || 0})
          </TabsTrigger>
        </TabsList>

        <TabsContent value='overview' className='space-y-6 mt-6'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            <div className='lg:col-span-2 space-y-6'>
              {/* General Information */}
              <Card>
                <CardHeader>
                  <div className='flex items-center gap-2'>
                    <FileText className='h-5 w-5 text-primary' />
                    <h3 className='font-semibold'>Thông tin chung</h3>
                  </div>
                </CardHeader>
                <CardContent className='space-y-4'>
                  <div className='grid grid-cols-2 gap-4'>
                    <div>
                      <Label className='text-muted-foreground'>
                        Mã đơn hàng
                      </Label>
                      <p className='font-medium'>{shipment.orderId || 'N/A'}</p>
                    </div>
                    {shipment.expectedDeliveryDate && (
                      <div>
                        <Label className='text-muted-foreground'>
                          Ngày dự kiến
                        </Label>
                        <div className='flex items-center gap-2'>
                          <Calendar className='h-4 w-4 text-muted-foreground' />
                          <span>
                            {formatDate(shipment.expectedDeliveryDate)}
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  {shipment.totalQuantity !== undefined && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Tổng số lượng
                      </Label>
                      <p className='font-medium'>{shipment.totalQuantity}</p>
                    </div>
                  )}

                  {shipment.note && (
                    <div>
                      <Label className='text-muted-foreground'>Ghi chú</Label>
                      <p className='text-sm whitespace-pre-wrap'>
                        {shipment.note}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Supplier Information */}
              {shipment.fromSupplierId && (
                <Card>
                  <CardHeader>
                    <div className='flex items-center gap-2'>
                      <Building2 className='h-5 w-5 text-primary' />
                      <h3 className='font-semibold'>Nhà cung cấp</h3>
                    </div>
                  </CardHeader>
                  <CardContent className='space-y-4'>
                    {supplier ? (
                      <>
                        <div>
                          <Label className='text-muted-foreground'>
                            Tên nhà cung cấp
                          </Label>
                          <p className='font-medium'>{supplier.name}</p>
                        </div>
                        {supplier.email && (
                          <div className='flex items-center gap-2'>
                            <Mail className='h-4 w-4 text-muted-foreground' />
                            <span>{supplier.email}</span>
                          </div>
                        )}
                        {supplier.phone && (
                          <div className='flex items-center gap-2'>
                            <Phone className='h-4 w-4 text-muted-foreground' />
                            <span>{supplier.phone}</span>
                          </div>
                        )}
                        {supplier.address?.fullAddress && (
                          <div className='flex items-start gap-2'>
                            <MapPin className='h-4 w-4 text-muted-foreground mt-1' />
                            <span className='text-sm'>
                              {supplier.address.fullAddress}
                            </span>
                          </div>
                        )}
                      </>
                    ) : (
                      <p className='text-muted-foreground'>
                        Không có thông tin nhà cung cấp
                      </p>
                    )}
                  </CardContent>
                </Card>
              )}
            </div>

            {/* Right Column - Summary */}
            <div className='space-y-6'>
              {/* Shipment Metadata */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Thông tin chi tiết</h3>
                </CardHeader>
                <CardContent className='space-y-3 text-sm'>
                  <div>
                    <Label className='text-muted-foreground'>Ngày tạo</Label>
                    <p>{formatDate(String(shipment.createdStamp))}</p>
                  </div>

                  {shipment.lastUpdatedStamp && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Cập nhật lần cuối
                      </Label>
                      <p>{formatDate(String(shipment.lastUpdatedStamp))}</p>
                    </div>
                  )}

                  {shipment.createdByUserId && (
                    <div>
                      <Label className='text-muted-foreground'>Người tạo</Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <User className='h-4 w-4 text-muted-foreground' />
                        <p className='font-medium'>
                          {formatFullname(
                            userMap.get(shipment.createdByUserId)
                          )}
                        </p>
                      </div>
                    </div>
                  )}

                  {shipment.handledByUserId && (
                    <div>
                      <Label className='text-muted-foreground'>
                        Người xử lý
                      </Label>
                      <div className='flex items-center gap-2 mt-1'>
                        <User className='h-4 w-4 text-muted-foreground' />
                        <p className='font-medium text-emerald-700 dark:text-emerald-400'>
                          {formatFullname(
                            userMap.get(shipment.handledByUserId)
                          )}
                        </p>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        <TabsContent value='items' className='space-y-6 mt-6'>
          <Card>
            <CardHeader>
              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-2'>
                  <Package className='h-5 w-5 text-primary' />
                  <h3 className='font-semibold'>Danh sách sản phẩm</h3>
                </div>
                <Badge variant='secondary'>
                  {shipment.items?.length || 0} sản phẩm
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              {shipment.items && shipment.items.length > 0 ? (
                <div className='space-y-3'>
                  {shipment.items.map((item: InventoryItemDetail, index) => {
                    const product = productMap.get(item.productId || '');
                    return (
                      <div
                        key={item.id || index}
                        className='flex items-center justify-between p-4 border rounded-lg bg-muted/30'
                      >
                        <div className='flex-1 space-y-2'>
                          <div className='flex items-start gap-3'>
                            <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10'>
                              <Package className='h-5 w-5 text-primary' />
                            </div>
                            <div className='flex-1'>
                              <p className='font-medium'>
                                {product?.name || item.productId || 'N/A'}
                              </p>
                              {product?.skuCode && (
                                <p className='text-sm text-muted-foreground'>
                                  SKU: {product.skuCode}
                                </p>
                              )}
                            </div>
                          </div>

                          <div className='grid grid-cols-2 md:grid-cols-4 gap-4 text-sm'>
                            <div>
                              <Label className='text-muted-foreground'>
                                Số lượng
                              </Label>
                              <p className='font-medium'>
                                {item.quantity} {product?.unit || ''}
                              </p>
                            </div>

                            {item.lotId && (
                              <div>
                                <Label className='text-muted-foreground'>
                                  Lô hàng
                                </Label>
                                <p className='font-medium'>{item.lotId}</p>
                              </div>
                            )}

                            {item.facilityId && (
                              <div>
                                <Label className='text-muted-foreground'>
                                  Kho
                                </Label>
                                <div className='flex items-center gap-1'>
                                  <Warehouse className='h-3 w-3 text-muted-foreground' />
                                  <p className='font-medium text-xs'>
                                    {item.facilityId}
                                  </p>
                                </div>
                              </div>
                            )}

                            {item.expirationDate && (
                              <div>
                                <Label className='text-muted-foreground'>
                                  Hạn sử dụng
                                </Label>
                                <p className='font-medium'>
                                  {formatDate(item.expirationDate)}
                                </p>
                              </div>
                            )}
                          </div>

                          {item.note && (
                            <div className='pt-2 border-t'>
                              <Label className='text-muted-foreground'>
                                Ghi chú
                              </Label>
                              <p className='text-sm mt-1'>{item.note}</p>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className='py-12 text-center text-muted-foreground'>
                  <Package className='h-12 w-12 mx-auto mb-4 opacity-50' />
                  <p>Chưa có sản phẩm nào trong phiếu kho</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};
