/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Form Dialog Component
*/

'use client';

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { OrderForm } from './OrderForm';
import type {
  OrderDetail,
  Product,
  CreateOrderRequest,
  UpdateOrderRequest,
} from '../../types';

interface OrderFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  order?: OrderDetail;
  products?: Product[];
  onSubmit: (data: CreateOrderRequest | UpdateOrderRequest) => Promise<void>;
  isLoading?: boolean;
}

export const OrderFormDialog: React.FC<OrderFormDialogProps> = ({
  open,
  onOpenChange,
  order,
  products,
  onSubmit,
  isLoading = false,
}) => {
  const handleCancel = () => {
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-7xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            {order ? 'Chỉnh sửa đơn hàng' : 'Tạo đơn hàng mới'}
          </DialogTitle>
        </DialogHeader>
        <OrderForm
          order={order}
          products={products}
          onSubmit={onSubmit}
          onCancel={handleCancel}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
};
