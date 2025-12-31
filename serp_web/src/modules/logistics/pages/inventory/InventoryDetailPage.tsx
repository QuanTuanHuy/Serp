/**
 * Inventory Detail Page - Logistics Module
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - View and edit inventory item details
 */

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Badge,
  Input,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  MoreVertical,
  Edit2,
  Trash2,
  Save,
  X,
  Package,
  Warehouse,
  AlertTriangle,
  Calendar,
  MapPin,
  CheckCircle2,
  XCircle,
  Box,
  Boxes,
  Archive,
  Truck,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import { toast } from 'sonner';
import {
  useGetInventoryItemQuery,
  useUpdateInventoryItemMutation,
  useDeleteInventoryItemMutation,
  useGetProductQuery,
  useGetFacilityQuery,
} from '../../api/logisticsApi';
import type { InventoryItemStatus } from '../../types';

interface InventoryDetailPageProps {
  itemId: string;
}

const statusStyles = {
  VALID: {
    label: 'Hợp lệ',
    bg: 'bg-emerald-100 dark:bg-emerald-900/30',
    text: 'text-emerald-700 dark:text-emerald-400',
    dot: 'bg-emerald-500',
    icon: CheckCircle2,
  },
  EXPIRED: {
    label: 'Đã hết hạn',
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

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('vi-VN');
};

const formatDateForInput = (dateString?: string) => {
  if (!dateString) return '';
  try {
    return new Date(dateString).toISOString().split('T')[0];
  } catch {
    return '';
  }
};

const StatsCard = ({
  title,
  value,
  icon: Icon,
  variant = 'default',
}: {
  title: string;
  value: number | string;
  icon: any;
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger';
}) => {
  const variantStyles = {
    default: {
      card: 'bg-card border-border',
      icon: 'bg-muted text-muted-foreground',
      iconRing: '',
    },
    primary: {
      card: 'bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/30 dark:to-blue-900/20 border-blue-200/50 dark:border-blue-800/30',
      icon: 'bg-blue-500 text-white shadow-blue-500/25',
      iconRing: 'ring-4 ring-blue-500/10',
    },
    success: {
      card: 'bg-gradient-to-br from-emerald-50 to-emerald-100/50 dark:from-emerald-950/30 dark:to-emerald-900/20 border-emerald-200/50 dark:border-emerald-800/30',
      icon: 'bg-emerald-500 text-white shadow-emerald-500/25',
      iconRing: 'ring-4 ring-emerald-500/10',
    },
    warning: {
      card: 'bg-gradient-to-br from-amber-50 to-amber-100/50 dark:from-amber-950/30 dark:to-amber-900/20 border-amber-200/50 dark:border-amber-800/30',
      icon: 'bg-amber-500 text-white shadow-amber-500/25',
      iconRing: 'ring-4 ring-amber-500/10',
    },
    danger: {
      card: 'bg-gradient-to-br from-rose-50 to-rose-100/50 dark:from-rose-950/30 dark:to-rose-900/20 border-rose-200/50 dark:border-rose-800/30',
      icon: 'bg-rose-500 text-white shadow-rose-500/25',
      iconRing: 'ring-4 ring-rose-500/10',
    },
  };

  const styles = variantStyles[variant];

  return (
    <Card className={cn('p-5 shadow-sm', styles.card)}>
      <div className='flex items-start justify-between gap-4'>
        <div className='space-y-1 min-w-0 flex-1'>
          <p className='text-sm font-medium text-muted-foreground truncate'>
            {title}
          </p>
          <p className='text-2xl font-bold tracking-tight truncate'>{value}</p>
        </div>

        <div
          className={cn(
            'flex h-12 w-12 shrink-0 items-center justify-center rounded-xl shadow-lg',
            styles.icon,
            styles.iconRing
          )}
        >
          <Icon className='h-6 w-6' />
        </div>
      </div>
    </Card>
  );
};

export const InventoryDetailPage: React.FC<InventoryDetailPageProps> = ({
  itemId,
}) => {
  const router = useRouter();

  const {
    data: itemResponse,
    isLoading,
    error,
  } = useGetInventoryItemQuery(itemId);
  const item = itemResponse?.data;

  const { data: productResponse } = useGetProductQuery(item?.productId || '', {
    skip: !item?.productId,
  });
  const { data: facilityResponse } = useGetFacilityQuery(
    item?.facilityId || '',
    {
      skip: !item?.facilityId,
    }
  );

  const product = productResponse?.data;
  const facility = facilityResponse?.data;

  const [updateItem, { isLoading: isUpdating }] =
    useUpdateInventoryItemMutation();
  const [deleteItem, { isLoading: isDeleting }] =
    useDeleteInventoryItemMutation();

  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const [editData, setEditData] = useState<{
    quantity?: number;
    statusId?: InventoryItemStatus;
    manufacturingDate?: string;
    expirationDate?: string;
  }>({});

  useEffect(() => {
    if (item) {
      setEditData({
        quantity: item.quantityOnHand,
        statusId: (item as any).statusId,
        manufacturingDate: formatDateForInput(item.manufacturingDate),
        expirationDate: formatDateForInput(item.expirationDate),
      });
    }
  }, [item]);

  if (isLoading) {
    return (
      <div className='space-y-6'>
        <div className='flex items-center gap-4'>
          <div className='h-10 w-10 bg-muted rounded-lg animate-pulse' />
          <div className='flex-1'>
            <div className='h-6 bg-muted rounded w-1/3 mb-2 animate-pulse' />
            <div className='h-4 bg-muted rounded w-1/4 animate-pulse' />
          </div>
        </div>
        <div className='grid grid-cols-4 gap-4'>
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className='h-24 bg-muted rounded-lg animate-pulse' />
          ))}
        </div>
      </div>
    );
  }

  if (error || !item) {
    return (
      <div className='flex flex-col items-center justify-center py-16'>
        <div className='w-20 h-20 bg-muted rounded-full flex items-center justify-center mb-4'>
          <AlertTriangle className='w-10 h-10 text-muted-foreground' />
        </div>
        <h3 className='text-lg font-semibold mb-2'>Không tìm thấy mặt hàng</h3>
        <p className='text-muted-foreground mb-6'>
          Mặt hàng có thể đã bị xóa hoặc bạn không có quyền xem.
        </p>
        <Button onClick={() => router.push('/logistics/inventory')}>
          <ArrowLeft className='h-4 w-4 mr-2' />
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  const status =
    statusStyles[(item as any)?.statusId as keyof typeof statusStyles] ||
    statusStyles.VALID;
  const StatusIcon = status.icon;

  const isExpiringSoon =
    item?.expirationDate &&
    new Date(item.expirationDate) <
      new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) &&
    new Date(item.expirationDate) >= new Date();

  const isExpired =
    item?.expirationDate && new Date(item.expirationDate) < new Date();

  const quantityAvailable = item?.quantityOnHand || 0;

  const handleUpdate = async () => {
    try {
      await updateItem({
        inventoryItemId: itemId,
        data: editData,
      }).unwrap();

      toast.success('Cập nhật tồn kho thành công');
      setIsEditing(false);
    } catch (error) {
      toast.error('Không thể cập nhật tồn kho');
      console.error('Update error:', error);
    }
  };

  const handleDelete = async () => {
    try {
      await deleteItem(itemId).unwrap();
      toast.success('Xóa tồn kho thành công');
      router.push('/logistics/inventory');
    } catch (error) {
      toast.error('Không thể xóa tồn kho');
      console.error('Delete error:', error);
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    if (item) {
      setEditData({
        quantity: item.quantityOnHand,
        statusId: (item as any).statusId,
        manufacturingDate: formatDateForInput(item.manufacturingDate),
        expirationDate: formatDateForInput(item.expirationDate),
      });
    }
  };

  return (
    <div className='space-y-6'>
      {/* Header */}
      <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4'>
        <div className='flex items-center gap-4'>
          <Button
            variant='ghost'
            size='icon'
            onClick={() => router.push('/logistics/inventory')}
          >
            <ArrowLeft className='h-5 w-5' />
          </Button>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>
              Chi tiết tồn kho
            </h1>
            <p className='text-muted-foreground'>Lot {item?.lotId}</p>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          {isEditing ? (
            <>
              <Button
                variant='outline'
                onClick={handleCancelEdit}
                disabled={isUpdating}
              >
                <X className='h-4 w-4 mr-2' />
                Hủy
              </Button>
              <Button onClick={handleUpdate} disabled={isUpdating}>
                <Save className='h-4 w-4 mr-2' />
                {isUpdating ? 'Đang lưu...' : 'Lưu thay đổi'}
              </Button>
            </>
          ) : (
            <div className='relative'>
              <Button
                variant='outline'
                size='icon'
                onClick={() => setShowMoreMenu(!showMoreMenu)}
              >
                <MoreVertical className='h-4 w-4' />
              </Button>
              {showMoreMenu && (
                <div className='absolute right-0 top-full mt-2 w-48 bg-card border rounded-lg shadow-lg z-10'>
                  <button
                    onClick={() => {
                      setIsEditing(true);
                      setShowMoreMenu(false);
                    }}
                    className='flex items-center gap-2 w-full px-4 py-2 text-sm hover:bg-muted rounded-t-lg'
                  >
                    <Edit2 className='h-4 w-4' />
                    Chỉnh sửa
                  </button>
                  <button
                    onClick={() => {
                      setShowDeleteDialog(true);
                      setShowMoreMenu(false);
                    }}
                    className='flex items-center gap-2 w-full px-4 py-2 text-sm hover:bg-muted text-destructive rounded-b-lg'
                  >
                    <Trash2 className='h-4 w-4' />
                    Xóa
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Status Badges */}
      <div className='flex flex-wrap gap-2'>
        <Badge
          variant='secondary'
          className={cn('gap-1', status.bg, status.text)}
        >
          <StatusIcon className='h-3 w-3' />
          <span className={cn('h-1.5 w-1.5 rounded-full', status.dot)} />
          {status.label}
        </Badge>
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

      {/* Stats Grid */}
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
                    value={editData.quantity}
                    onChange={(e) =>
                      setEditData({
                        ...editData,
                        quantity: parseInt(e.target.value) || 0,
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

      {/* Info Cards */}
      <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
        {/* Product Info */}
        <Card>
          <CardHeader>
            <CardTitle className='flex items-center gap-2'>
              <Package className='h-5 w-5' />
              Thông tin sản phẩm
            </CardTitle>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Tên sản phẩm</span>
              <span className='font-medium'>{product?.name || 'N/A'}</span>
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Đơn vị</span>
              <span className='font-medium'>{product?.unit || 'N/A'}</span>
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Lot ID</span>
              <span className='font-medium'>{item?.lotId}</span>
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Trạng thái</span>
              {isEditing ? (
                <select
                  value={editData.statusId}
                  onChange={(e) =>
                    setEditData({
                      ...editData,
                      statusId: e.target.value as InventoryItemStatus,
                    })
                  }
                  className='px-3 py-1 border rounded-lg bg-background'
                >
                  <option value='VALID'>Hợp lệ</option>
                  <option value='EXPIRED'>Đã hết hạn</option>
                  <option value='DAMAGED'>Hư hỏng</option>
                </select>
              ) : (
                <Badge
                  variant='secondary'
                  className={cn('gap-1', status.bg, status.text)}
                >
                  {status.label}
                </Badge>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Facility & Dates */}
        <Card>
          <CardHeader>
            <CardTitle className='flex items-center gap-2'>
              <Warehouse className='h-5 w-5' />
              Kho & Thời gian
            </CardTitle>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Kho</span>
              <span className='font-medium flex items-center gap-2'>
                <MapPin className='h-4 w-4 text-muted-foreground' />
                {facility?.name || 'N/A'}
              </span>
            </div>
            <div className='flex justify-between'>
              <span className='text-muted-foreground'>Địa chỉ</span>
              <span className='font-medium'>
                {facility?.address?.fullAddress || 'N/A'}
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
                  {formatDate(item?.manufacturingDate)}
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
                  {formatDate(item?.expirationDate)}
                </span>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Delete Confirmation Dialog */}
      {showDeleteDialog && (
        <div className='fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center'>
          <Card className='w-full max-w-md'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2 text-destructive'>
                <AlertTriangle className='h-5 w-5' />
                Xác nhận xóa
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <p className='text-muted-foreground'>
                Bạn có chắc chắn muốn xóa mặt hàng này khỏi tồn kho? Thao tác
                này không thể hoàn tác.
              </p>
              <div className='flex justify-end gap-2'>
                <Button
                  variant='outline'
                  onClick={() => setShowDeleteDialog(false)}
                  disabled={isDeleting}
                >
                  Hủy
                </Button>
                <Button
                  variant='destructive'
                  onClick={handleDelete}
                  disabled={isDeleting}
                >
                  <Trash2 className='h-4 w-4 mr-2' />
                  {isDeleting ? 'Đang xóa...' : 'Xóa'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
};
