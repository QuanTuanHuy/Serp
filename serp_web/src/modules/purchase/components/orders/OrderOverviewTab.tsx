/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Overview Tab Component
*/

'use client';

import React, { useState, useMemo } from 'react';
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Badge,
} from '@/shared/components/ui';
import { Pencil } from 'lucide-react';
import { OrderEditDialog } from './OrderEditDialog';
import { useGetSupplierByIdQuery } from '../../services';
import { useGetUsersQuery } from '@/modules/admin';
import type { OrderDetail } from '../../types';

interface OrderOverviewTabProps {
  order: OrderDetail;
  onRefresh?: () => void;
}

export const OrderOverviewTab: React.FC<OrderOverviewTabProps> = ({
  order,
  onRefresh,
}) => {
  const [editDialogOpen, setEditDialogOpen] = useState(false);

  // Fetch supplier name
  const { data: supplierData } = useGetSupplierByIdQuery(
    order.fromSupplierId!,
    {
      skip: !order.fromSupplierId,
    }
  );

  // Collect all user IDs that need to be fetched
  const userIds = useMemo(() => {
    const ids: number[] = [];
    if (order.createdByUserId) ids.push(order.createdByUserId);
    if (order.userApprovedId) ids.push(order.userApprovedId);
    if (order.userCancelledId) ids.push(order.userCancelledId);
    return ids;
  }, [order.createdByUserId, order.userApprovedId, order.userCancelledId]);

  // Fetch users (only if there are user IDs)
  const { data: usersData } = useGetUsersQuery(
    {
      page: 0,
      pageSize: 100,
    },
    {
      skip: userIds.length === 0,
    }
  );

  // Create user ID to name map
  const userMap = useMemo(() => {
    const map = new Map<number, string>();
    usersData?.data?.items?.forEach((user) => {
      const fullName = [user.firstName, user.lastName]
        .filter(Boolean)
        .join(' ')
        .trim();
      map.set(user.id, fullName || user.email);
    });
    return map;
  }, [usersData]);

  const getUserDisplayName = (userId?: number) => {
    if (!userId) return '-';
    return userMap.get(userId) || `User ID: ${userId}`;
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const getStatusBadgeVariant = (statusId: string) => {
    switch (statusId?.toUpperCase()) {
      case 'CREATED':
        return 'default';
      case 'APPROVED':
        return 'outline';
      case 'CANCELLED':
        return 'destructive';
      case 'READY_FOR_DELIVERY':
        return 'secondary';
      case 'FULLY_DELIVERED':
        return 'default';
      default:
        return 'secondary';
    }
  };

  const getSalesChannelLabel = (channelId?: string) => {
    if (!channelId) return '-';
    return channelId === 'PARTNER'
      ? 'Partner'
      : channelId === 'ONLINE'
        ? 'Online'
        : channelId;
  };

  return (
    <div className='space-y-4'>
      <Card>
        <CardHeader className='flex flex-row items-center justify-between'>
          <CardTitle>Thông tin đơn hàng</CardTitle>
          <Button
            variant='outline'
            size='sm'
            onClick={() => setEditDialogOpen(true)}
          >
            <Pencil className='h-4 w-4 mr-2' />
            Chỉnh sửa
          </Button>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {/* Order ID */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Mã đơn hàng
              </label>
              <p className='text-sm font-mono mt-1'>{order.id}</p>
            </div>

            {/* Order Date */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Ngày đặt hàng
              </label>
              <p className='text-sm mt-1'>{formatDate(order.orderDate)}</p>
            </div>

            {/* Supplier Name */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Nhà cung ứng
              </label>
              <p className='text-sm mt-1'>
                {supplierData?.data?.name || order.fromSupplierId || '-'}
              </p>
            </div>

            {/* Status */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Trạng thái
              </label>
              <div className='mt-1'>
                <Badge variant={getStatusBadgeVariant(order.statusId)}>
                  {order.statusId}
                </Badge>
              </div>
            </div>

            {/* Delivery Before Date */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Giao trước ngày
              </label>
              <p className='text-sm mt-1'>
                {formatDate(order.deliveryBeforeDate)}
              </p>
            </div>

            {/* Delivery After Date */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Giao sau ngày
              </label>
              <p className='text-sm mt-1'>
                {formatDate(order.deliveryAfterDate)}
              </p>
            </div>

            {/* Created By User */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Người tạo
              </label>
              <p className='text-sm mt-1'>
                {getUserDisplayName(order.createdByUserId)}
              </p>
            </div>

            {/* Priority */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Độ ưu tiên
              </label>
              <p className='text-sm mt-1'>{order.priority}</p>
            </div>

            {/* Sales Channel */}
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Kênh bán
              </label>
              <p className='text-sm mt-1'>
                {getSalesChannelLabel(order.saleChannelId)}
              </p>
            </div>

            {/* Note */}
            {order.note && (
              <div className='md:col-span-2'>
                <label className='text-sm font-medium text-muted-foreground'>
                  Ghi chú
                </label>
                <p className='text-sm mt-1'>{order.note}</p>
              </div>
            )}

            {/* User Approved (if exists) */}
            {order.userApprovedId && (
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Người chấp nhận
                </label>
                <p className='text-sm mt-1'>
                  {getUserDisplayName(order.userApprovedId)}
                </p>
              </div>
            )}

            {/* User Cancelled (if exists) */}
            {order.userCancelledId && (
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Người hủy
                </label>
                <p className='text-sm mt-1'>
                  {getUserDisplayName(order.userCancelledId)}
                </p>
              </div>
            )}

            {/* Cancellation Note */}
            {order.cancellationNote && (
              <div className='md:col-span-2'>
                <label className='text-sm font-medium text-muted-foreground'>
                  Ghi chú hủy
                </label>
                <p className='text-sm mt-1 text-destructive'>
                  {order.cancellationNote}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Edit Dialog */}
      <OrderEditDialog
        open={editDialogOpen}
        onOpenChange={setEditDialogOpen}
        order={order}
        onSuccess={() => {
          setEditDialogOpen(false);
          onRefresh?.();
        }}
      />
    </div>
  );
};
