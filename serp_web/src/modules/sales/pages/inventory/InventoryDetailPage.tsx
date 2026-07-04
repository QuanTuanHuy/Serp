'use client';

import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  Package,
  MapPin,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Box,
  Archive,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetInventoryItemQuery,
  useGetProductQuery,
  useGetFacilityQuery,
} from '../../api/salesApi';
import { formatDateStringVN } from '@/shared/utils/format';

interface InventoryDetailPageProps {
  itemId: string;
}

const STATUS_CONFIG = {
  VALID: {
    label: 'Còn hạn',
    color: 'text-emerald-700 dark:text-emerald-400',
    bgColor: 'bg-emerald-100 dark:bg-emerald-900/30',
    icon: CheckCircle2,
  },
  EXPIRED: {
    label: 'Đã hết hạn',
    color: 'text-rose-700 dark:text-rose-400',
    bgColor: 'bg-rose-100 dark:bg-rose-900/30',
    icon: XCircle,
  },
  EXPIRING_SOON: {
    label: 'Sắp hết hạn',
    color: 'text-amber-700 dark:text-amber-400',
    bgColor: 'bg-amber-100 dark:bg-amber-900/30',
    icon: AlertCircle,
  },
};

export const InventoryDetailPage: React.FC<InventoryDetailPageProps> = ({
  itemId,
}) => {
  const router = useRouter();

  // Fetch inventory item data
  const {
    data: itemResponse,
    isLoading,
    isError,
  } = useGetInventoryItemQuery(itemId);

  const item = itemResponse?.data;

  // Fetch related data
  const { data: productResponse } = useGetProductQuery(item?.productId || '', {
    skip: !item?.productId,
  });
  const { data: facilityResponse } = useGetFacilityQuery(
    item?.facilityId || '',
    { skip: !item?.facilityId }
  );

  const product = productResponse?.data;
  const facility = facilityResponse?.data;

  const statusConfig = item
    ? STATUS_CONFIG[item.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.VALID
    : STATUS_CONFIG.VALID;
  const StatusIcon = statusConfig.icon;

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <div className='text-center'>
          <div className='h-8 w-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4' />
          <p className='text-muted-foreground'>Đang tải...</p>
        </div>
      </div>
    );
  }

  if (isError || !item) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <Card className='max-w-md'>
          <CardContent className='pt-6 text-center'>
            <AlertCircle className='h-12 w-12 text-destructive mx-auto mb-4' />
            <h3 className='text-lg font-semibold mb-2'>
              Không tìm thấy mục tồn kho
            </h3>
            <p className='text-muted-foreground mb-4'>
              Mục tồn kho không tồn tại hoặc đã bị xóa.
            </p>
            <Button onClick={() => router.push('/sales/inventory')}>
              <ArrowLeft className='h-4 w-4 mr-2' />
              Quay lại danh sách
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const isExpired =
    item.expirationDate && new Date(item.expirationDate) < new Date();
  const isExpiringSoon =
    item.expirationDate &&
    new Date(item.expirationDate) <
      new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) &&
    new Date(item.expirationDate) > new Date();

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div className='flex items-center gap-4'>
          <Button
            variant='outline'
            size='icon'
            onClick={() => router.back()}
            className='h-9 w-9'
          >
            <ArrowLeft className='h-4 w-4' />
          </Button>
          <div>
            <div className='flex items-center gap-3 mb-1'>
              <h1 className='text-2xl font-bold tracking-tight'>
                {product?.name ||
                  item.productId?.slice(0, 8) ||
                  'Sản phẩm không rõ'}
              </h1>
              <Badge
                variant='secondary'
                className={cn(
                  'gap-1',
                  statusConfig.bgColor,
                  statusConfig.color
                )}
              >
                <StatusIcon className='h-3 w-3' />
                {statusConfig.label}
              </Badge>
            </div>
            <div className='flex items-center gap-2 text-sm text-muted-foreground'>
              <span>Lô hàng: {item.lotId || 'N/A'}</span>
              <span>•</span>
              <span>ID: {item.id?.slice(0, 8) || 'N/A'}...</span>
            </div>
          </div>
        </div>
      </div>

      {/* Warning Badges */}
      {(isExpiringSoon || isExpired) && (
        <div className='flex gap-2'>
          {isExpiringSoon && !isExpired && (
            <Badge
              variant='secondary'
              className='gap-1 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400'
            >
              <AlertCircle className='h-3 w-3' />
              Sắp hết hạn
            </Badge>
          )}
          {isExpired && (
            <Badge
              variant='destructive'
              className='gap-1 bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400'
            >
              <XCircle className='h-3 w-3' />
              Đã hết hạn
            </Badge>
          )}
        </div>
      )}

      {/* Inventory Stats */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card>
          <CardContent className='pt-6'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground mb-1'>
                  Tồn kho thực
                </p>
                <p className='text-2xl font-bold'>{item.quantityOnHand || 0}</p>
              </div>
              <div className='flex h-12 w-12 items-center justify-center rounded-xl bg-blue-100 dark:bg-blue-900/30'>
                <Box className='h-6 w-6 text-blue-600 dark:text-blue-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className='pt-6'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Chưa giao</p>
                <p className='text-2xl font-bold text-purple-600 dark:text-purple-400'>
                  {item.quantityCommitted || 0}
                </p>
              </div>
              <div className='flex h-12 w-12 items-center justify-center rounded-xl bg-purple-100 dark:bg-purple-900/30'>
                <Truck className='h-6 w-6 text-purple-600 dark:text-purple-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className='pt-6'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Chưa xuất</p>
                <p className='text-2xl font-bold text-amber-600 dark:text-amber-400'>
                  {item.quantityReserved || 0}
                </p>
              </div>
              <div className='flex h-12 w-12 items-center justify-center rounded-xl bg-amber-100 dark:bg-amber-900/30'>
                <Archive className='h-6 w-6 text-amber-600 dark:text-amber-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className='pt-6'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground mb-1'>Khả dụng</p>
                <p className='text-2xl font-bold text-emerald-600 dark:text-emerald-400'>
                  {(item.quantityOnHand || 0) -
                    (item.quantityCommitted || 0) -
                    (item.quantityReserved || 0)}
                </p>
              </div>
              <div className='flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-100 dark:bg-emerald-900/30'>
                <CheckCircle2 className='h-6 w-6 text-emerald-600 dark:text-emerald-400' />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Product & Location Info */}
      <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
        {/* Product Info */}
        <Card>
          <CardHeader>
            <div className='flex items-center gap-2'>
              <Package className='h-5 w-5 text-primary' />
              <h3 className='font-semibold'>Thông tin sản phẩm</h3>
            </div>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Tên sản phẩm</span>
              <span className='font-medium'>
                {product?.name || item.productId?.slice(0, 8) || 'N/A'}
              </span>
            </div>
            {product && (
              <>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Đơn vị</span>
                  <span className='font-medium'>{product.unit || 'N/A'}</span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Giá vốn</span>
                  <span className='font-medium'>
                    đ{product.costPrice?.toLocaleString() || 0}
                  </span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Giá bán</span>
                  <span className='font-medium'>
                    đ{product.retailPrice?.toLocaleString() || 0}
                  </span>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Location Info */}
        <Card>
          <CardHeader>
            <div className='flex items-center gap-2'>
              <MapPin className='h-5 w-5 text-primary' />
              <h3 className='font-semibold'>Vị trí & Thời gian</h3>
            </div>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Kho hàng</span>
              <span className='font-medium'>
                {facility?.name || item.facilityId?.slice(0, 8) || 'N/A'}
              </span>
            </div>
            {facility?.address && (
              <div className='flex justify-between'>
                <span className='text-muted-foreground'>Địa chỉ</span>
                <span className='font-medium text-right max-w-[200px]'>
                  {facility?.address.fullAddress}
                </span>
              </div>
            )}
            <div className='flex justify-between pt-2 border-t'>
              <span className='text-muted-foreground'>Ngày nhập kho</span>
              <span className='font-medium'>
                {item.receivedDate
                  ? formatDateStringVN(item.receivedDate)
                  : 'Không xác định'}
              </span>
            </div>
            <div className='flex justify-between items-center'>
              <span className='text-muted-foreground'>Ngày sản xuất</span>
              <span className='font-medium'>
                {item.manufacturingDate
                  ? formatDateStringVN(item.manufacturingDate)
                  : 'Không xác định'}
              </span>
            </div>
            <div className='flex justify-between items-center'>
              <span className='text-muted-foreground'>Hạn sử dụng</span>
              <span className='font-medium'>
                {item.expirationDate
                  ? formatDateStringVN(item.expirationDate)
                  : 'Không xác định'}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
