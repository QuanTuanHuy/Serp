/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Overview Tab (Read-only for Logistics)
*/

'use client';

import React, { useMemo } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Badge,
} from '@/shared/components/ui';
import type { OrderDetail } from '../../types';
import { useSuppliers, useCustomers } from '../../hooks';

interface OrderOverviewTabProps {
  order: OrderDetail;
}

export const OrderOverviewTab: React.FC<OrderOverviewTabProps> = ({
  order,
}) => {
  const { suppliers } = useSuppliers();
  const { customers } = useCustomers();

  // Create maps for quick lookup
  const supplierMap = useMemo(() => {
    const map = new Map<string, string>();
    suppliers?.forEach((supplier) => {
      map.set(supplier.id, supplier.name);
    });
    return map;
  }, [suppliers]);

  const customerMap = useMemo(() => {
    const map = new Map<string, string>();
    customers?.forEach((customer) => {
      map.set(customer.id, customer.name);
    });
    return map;
  }, [customers]);
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const formatDateTime = (dateTimeString?: string) => {
    if (!dateTimeString) return '-';
    const date = new Date(dateTimeString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatCurrency = (amount?: number) => {
    if (amount === undefined || amount === null) return '-';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const getStatusBadgeVariant = (
    statusId?: string
  ): 'default' | 'destructive' | 'outline' | 'secondary' => {
    switch (statusId?.toUpperCase()) {
      case 'CREATED':
        return 'default';
      case 'APPROVED':
        return 'outline';
      case 'CANCELLED':
        return 'destructive';
      case 'COMPLETED':
        return 'secondary';
      default:
        return 'secondary';
    }
  };

  return (
    <div className='space-y-6'>
      {/* Order Information */}
      <Card>
        <CardHeader>
          <CardTitle className='text-lg'>Thông tin đơn hàng</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Mã đơn hàng
              </label>
              <p className='mt-1 font-mono text-sm'>{order.id}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Tên đơn hàng
              </label>
              <p className='mt-1 font-medium'>{order.orderName}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Loại đơn hàng
              </label>
              <div className='mt-1'>
                <Badge variant='outline'>{order.orderTypeId || 'N/A'}</Badge>
              </div>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Trạng thái
              </label>
              <div className='mt-1'>
                <Badge variant={getStatusBadgeVariant(order.statusId)}>
                  {order.statusId || 'N/A'}
                </Badge>
              </div>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Ngày đặt hàng
              </label>
              <p className='mt-1'>{formatDate(order.orderDate)}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Giao hàng từ ngày
              </label>
              <p className='mt-1'>{formatDate(order.deliveryAfterDate)}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Giao hàng đến ngày
              </label>
              <p className='mt-1'>{formatDate(order.deliveryBeforeDate)}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Độ ưu tiên
              </label>
              <p className='mt-1'>{order.priority || '-'}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Kênh bán hàng
              </label>
              <p className='mt-1'>{order.saleChannelId || '-'}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Tổng số lượng
              </label>
              <p className='mt-1 font-medium'>{order.totalQuantity || 0}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Tổng tiền
              </label>
              <p className='mt-1 font-semibold text-lg'>
                {formatCurrency(order.totalAmount || 0)}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Delivery Information */}
      {(order.deliveryFullAddress || order.deliveryPhone || order.note) && (
        <Card>
          <CardHeader>
            <CardTitle className='text-lg'>Thông tin giao hàng</CardTitle>
          </CardHeader>
          <CardContent>
            <div className='space-y-3'>
              {order.deliveryFullAddress && (
                <div>
                  <label className='text-sm font-medium text-muted-foreground'>
                    Địa chỉ giao hàng
                  </label>
                  <p className='mt-1'>{order.deliveryFullAddress}</p>
                </div>
              )}
              {order.deliveryPhone && (
                <div>
                  <label className='text-sm font-medium text-muted-foreground'>
                    Số điện thoại
                  </label>
                  <p className='mt-1'>{order.deliveryPhone}</p>
                </div>
              )}
              {order.note && (
                <div>
                  <label className='text-sm font-medium text-muted-foreground'>
                    Ghi chú
                  </label>
                  <p className='mt-1'>{order.note}</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Customer & Supplier Information */}
      {(order.toCustomerId || order.fromSupplierId) && (
        <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
          {order.toCustomerId && (
            <Card>
              <CardHeader>
                <CardTitle className='text-lg'>Khách hàng</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='space-y-3'>
                  <div>
                    <label className='text-sm font-medium text-muted-foreground'>
                      Tên khách hàng
                    </label>
                    <p className='mt-1 font-medium'>
                      {customerMap.get(order.toCustomerId) ||
                        order.toCustomerId}
                    </p>
                  </div>
                  <div>
                    <label className='text-sm font-medium text-muted-foreground'>
                      Mã khách hàng
                    </label>
                    <p className='mt-1 font-mono text-xs text-muted-foreground'>
                      {order.toCustomerId}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {order.fromSupplierId && (
            <Card>
              <CardHeader>
                <CardTitle className='text-lg'>Nhà cung cấp</CardTitle>
              </CardHeader>
              <CardContent>
                <div className='space-y-3'>
                  <div>
                    <label className='text-sm font-medium text-muted-foreground'>
                      Tên nhà cung cấp
                    </label>
                    <p className='mt-1 font-medium'>
                      {supplierMap.get(order.fromSupplierId) ||
                        order.fromSupplierId}
                    </p>
                  </div>
                  <div>
                    <label className='text-sm font-medium text-muted-foreground'>
                      Mã nhà cung cấp
                    </label>
                    <p className='mt-1 font-mono text-xs text-muted-foreground'>
                      {order.fromSupplierId}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* Timestamps */}
      <Card>
        <CardHeader>
          <CardTitle className='text-lg'>Thông tin hệ thống</CardTitle>
        </CardHeader>
        <CardContent>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4 text-sm'>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Người tạo
              </label>
              <p className='mt-1'>User ID: {order.createdByUserId}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Người duyệt
              </label>
              <p className='mt-1'>
                {order.userApprovedId
                  ? `User ID: ${order.userApprovedId}`
                  : '-'}
              </p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Ngày tạo
              </label>
              <p className='mt-1'>{formatDateTime(order.createdStamp)}</p>
            </div>
            <div>
              <label className='text-sm font-medium text-muted-foreground'>
                Cập nhật lần cuối
              </label>
              <p className='mt-1'>{formatDateTime(order.lastUpdatedStamp)}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
