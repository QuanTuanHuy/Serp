/*
Author: QuanTuanHuy
Description: Part of Serp Project - Inventory Item Detail Page
*/

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
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
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Input,
  Label,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
  Package,
  MapPin,
  Calendar,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Box,
  TrendingUp,
  TrendingDown,
  Archive,
  Save,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetInventoryItemQuery,
  useUpdateInventoryItemMutation,
  useDeleteInventoryItemMutation,
  useGetProductQuery,
  useGetFacilityQuery,
} from '../../api/salesApi';
import type { InventoryItemStatus, InventoryItemUpdateForm } from '../../types';

interface InventoryDetailPageProps {
  itemId: string;
}

const STATUS_CONFIG = {
  VALID: {
    label: 'Hợp lệ',
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
  DAMAGED: {
    label: 'Hư hỏng',
    color: 'text-amber-700 dark:text-amber-400',
    bgColor: 'bg-amber-100 dark:bg-amber-900/30',
    icon: AlertCircle,
  },
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const formatDateTime = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const InventoryDetailPage: React.FC<InventoryDetailPageProps> = ({
  itemId,
}) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isEditing, setIsEditing] = useState(false);

  // Fetch inventory item data
  const {
    data: itemResponse,
    isLoading,
    isError,
  } = useGetInventoryItemQuery(itemId);
  const [updateInventoryItem, { isLoading: isUpdating }] =
    useUpdateInventoryItemMutation();
  const [deleteInventoryItem, { isLoading: isDeleting }] =
    useDeleteInventoryItemMutation();

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

  // Edit form state
  const [editData, setEditData] = useState({
    quantityOnHand: item?.quantityOnHand || 0,
    expirationDate: item?.expirationDate || '',
    manufacturingDate: item?.manufacturingDate || '',
    statusId: item?.statusId || 'VALID',
  });

  const statusConfig = item
    ? STATUS_CONFIG[item.statusId as keyof typeof STATUS_CONFIG] ||
      STATUS_CONFIG.VALID
    : STATUS_CONFIG.VALID;
  const StatusIcon = statusConfig.icon;

  const handleEdit = () => {
    setEditData({
      quantityOnHand: item?.quantityOnHand || 0,
      expirationDate: item?.expirationDate || '',
      manufacturingDate: item?.manufacturingDate || '',
      statusId: item?.statusId || 'VALID',
    });
    setIsEditing(true);
  };

  const handleSaveEdit = async () => {
    if (!item) return;

    try {
      const updatePayload: InventoryItemUpdateForm = {
        quantity: editData.quantityOnHand,
        statusId: editData.statusId as InventoryItemStatus,
      };

      if (editData.expirationDate) {
        updatePayload.expirationDate = editData.expirationDate;
      }
      if (editData.manufacturingDate) {
        updatePayload.manufacturingDate = editData.manufacturingDate;
      }

      await updateInventoryItem({
        inventoryItemId: item.id,
        data: updatePayload,
      }).unwrap();

      toast.success('Cập nhật thành công');
      setIsEditing(false);
    } catch (error: any) {
      const errorMessage =
        error?.data?.message || 'Không thể cập nhật. Vui lòng thử lại.';
      toast.error(errorMessage);
    }
  };

  const handleDelete = async () => {
    if (!item) return;
    if (!confirm('Bạn có chắc chắn muốn xóa mục tồn kho này không?')) return;

    try {
      await deleteInventoryItem(item.id).unwrap();
      toast.success('Xóa thành công');
      router.push('/sales/inventory');
    } catch (error: any) {
      const errorMessage =
        error?.data?.message || 'Không thể xóa. Vui lòng thử lại.';
      toast.error(errorMessage);
    }
  };

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
                {product?.name || item.productId.slice(0, 8)}
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
              <span>Lô hàng: {item.lotId}</span>
              <span>•</span>
              <span>ID: {item.id.slice(0, 8)}...</span>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className='flex items-center gap-2'>
          {isEditing ? (
            <>
              <Button
                onClick={handleSaveEdit}
                disabled={isUpdating}
                className='gap-2'
              >
                <Save className='h-4 w-4' />
                Lưu
              </Button>
              <Button
                variant='outline'
                onClick={() => setIsEditing(false)}
                disabled={isUpdating}
              >
                Hủy
              </Button>
            </>
          ) : (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant='outline' size='icon'>
                  <MoreHorizontal className='h-4 w-4' />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align='end'>
                <DropdownMenuItem onClick={handleEdit}>
                  <Edit className='h-4 w-4 mr-2' />
                  Chỉnh sửa
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={handleDelete}
                  className='text-destructive'
                  disabled={isDeleting}
                >
                  <Trash2 className='h-4 w-4 mr-2' />
                  Xóa
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </div>

      {/* Warning Badges */}
      {(isExpiringSoon || isExpired) && (
        <div className='flex gap-2'>
          {isExpiringSoon && (
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
                {isEditing ? (
                  <Input
                    type='number'
                    value={editData.quantityOnHand}
                    onChange={(e) =>
                      setEditData({
                        ...editData,
                        quantityOnHand: parseInt(e.target.value) || 0,
                      })
                    }
                    className='w-32'
                  />
                ) : (
                  <p className='text-2xl font-bold'>{item.quantityOnHand}</p>
                )}
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
                  {item.quantityCommitted}
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
                <p className='text-sm text-muted-foreground mb-1'>Đã đặt</p>
                <p className='text-2xl font-bold text-amber-600 dark:text-amber-400'>
                  {item.quantityReserved}
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
                  {item.quantityOnHand -
                    item.quantityCommitted -
                    item.quantityReserved}
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
                {product?.name || item.productId.slice(0, 8)}
              </span>
            </div>
            {product && (
              <>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Đơn vị</span>
                  <span className='font-medium'>{product.unit}</span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Giá vốn</span>
                  <span className='font-medium'>
                    đ{product.costPrice.toLocaleString()}
                  </span>
                </div>
                <div className='flex justify-between'>
                  <span className='text-muted-foreground'>Giá bán</span>
                  <span className='font-medium'>
                    đ{product.retailPrice.toLocaleString()}
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
                {facility?.name || item.facilityId.slice(0, 8)}
              </span>
            </div>
            {facility?.address && (
              <div className='flex justify-between'>
                <span className='text-muted-foreground'>Địa chỉ</span>
                <span className='font-medium'>
                  {facility?.address.fullAddress}
                </span>
              </div>
            )}
            <div className='flex justify-between pt-2 border-t'>
              <span className='text-muted-foreground'>Ngày nhận</span>
              <span className='font-medium'>
                {formatDate(item.receivedDate)}
              </span>
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Ngày SX</span>
              {isEditing ? (
                <Input
                  type='date'
                  value={editData.manufacturingDate}
                  onChange={(e) =>
                    setEditData({
                      ...editData,
                      manufacturingDate: e.target.value,
                    })
                  }
                  className='w-40 h-8'
                />
              ) : (
                <span className='font-medium'>
                  {formatDate(item.manufacturingDate)}
                </span>
              )}
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Ngày HSD</span>
              {isEditing ? (
                <Input
                  type='date'
                  value={editData.expirationDate}
                  onChange={(e) =>
                    setEditData({
                      ...editData,
                      expirationDate: e.target.value,
                    })
                  }
                  className='w-40 h-8'
                />
              ) : (
                <span className='font-medium'>
                  {formatDate(item.expirationDate)}
                </span>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
