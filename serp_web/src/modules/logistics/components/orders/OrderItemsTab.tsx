/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Items Tab (Read-only for Logistics)
*/

'use client';

import React, { useMemo } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { DataTable } from '@/shared/components';
import { useGetOrderQuery } from '../../services';

interface OrderItemsTabProps {
  orderId: string;
}

export const OrderItemsTab: React.FC<OrderItemsTabProps> = ({ orderId }) => {
  const { data: orderDetail, isLoading } = useGetOrderQuery(orderId);

  const formatCurrency = (amount?: number) => {
    if (amount === undefined || amount === null) return '-';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount);
  };

  const columns = useMemo(
    () => [
      {
        id: 'orderItemSeqId',
        header: 'STT',
        accessor: 'orderItemSeqId',
        cell: ({ row }: any) => (
          <div className='text-sm text-center font-medium'>
            {row.orderItemSeqId}
          </div>
        ),
      },
      {
        id: 'productId',
        header: 'Mã sản phẩm',
        accessor: 'productId',
        cell: ({ row }: any) => (
          <div className='font-mono text-xs'>{row.productId}</div>
        ),
      },
      {
        id: 'quantity',
        header: 'Số lượng',
        accessor: 'quantity',
        cell: ({ row }: any) => (
          <div className='text-sm text-center font-medium'>
            {row.quantity || 0} {row.unit}
          </div>
        ),
      },
      {
        id: 'price',
        header: 'Đơn giá',
        accessor: 'price',
        cell: ({ row }: any) => (
          <div className='text-sm text-right'>{formatCurrency(row.price)}</div>
        ),
      },
      {
        id: 'discount',
        header: 'Giảm giá',
        accessor: 'discount',
        cell: ({ row }: any) => (
          <div className='text-sm text-center'>
            {formatCurrency(row.discount)}
          </div>
        ),
      },
      {
        id: 'tax',
        header: 'Thuế',
        accessor: 'tax',
        cell: ({ row }: any) => (
          <div className='text-sm text-center'>{row.tax}%</div>
        ),
      },
      {
        id: 'amount',
        header: 'Thành tiền',
        accessor: 'amount',
        cell: ({ row }: any) => (
          <div className='text-sm text-right font-semibold'>
            {formatCurrency(row.amount)}
          </div>
        ),
      },
      {
        id: 'statusId',
        header: 'Trạng thái',
        accessor: 'statusId',
        cell: ({ row }: any) => (
          <div className='text-sm'>{row.statusId || '-'}</div>
        ),
      },
    ],
    []
  );

  const orderItems = orderDetail?.orderItems || [];
  const totalQuantity = orderItems.reduce(
    (sum, item) => sum + (item.quantity || 0),
    0
  );
  const totalAmount = orderItems.reduce(
    (sum, item) => sum + (item.amount || 0),
    0
  );

  return (
    <div className='space-y-6'>
      <Card>
        <CardHeader>
          <CardTitle className='text-lg'>Danh sách sản phẩm</CardTitle>
        </CardHeader>
        <CardContent>
          <DataTable
            columns={columns}
            data={orderItems}
            isLoading={isLoading}
            keyExtractor={(row) => row.id}
          />

          {/* Summary */}
          {orderItems.length > 0 && (
            <div className='mt-6 space-y-2 border-t pt-4'>
              <div className='flex justify-between text-sm'>
                <span className='text-muted-foreground'>Tổng số lượng:</span>
                <span className='font-medium'>{totalQuantity}</span>
              </div>
              <div className='flex justify-between text-base border-t pt-2'>
                <span className='font-semibold'>Tổng cộng:</span>
                <span className='font-bold text-lg'>
                  {formatCurrency(totalAmount)}
                </span>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
