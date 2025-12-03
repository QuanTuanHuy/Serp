/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Detail Dialog Component (Read-only for Logistics)
*/

'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { OrderOverviewTab } from './OrderOverviewTab';
import { OrderItemsTab } from './OrderItemsTab';
import { OrderShipmentsTab } from './OrderShipmentsTab';
import { useGetOrderQuery } from '../../services';
import type { Order } from '../../types';

interface OrderDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  order?: Order;
}

export const OrderDetailDialog: React.FC<OrderDetailDialogProps> = ({
  open,
  onOpenChange,
  order,
}) => {
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch full order details
  const { data: orderDetail } = useGetOrderQuery(order?.id || '', {
    skip: !order?.id,
  });

  if (!order) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-6xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>
            Chi tiết đơn hàng #{order.id.substring(0, 8)}
          </DialogTitle>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={setActiveTab} className='w-full'>
          <TabsList className='grid w-full grid-cols-3'>
            <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
            <TabsTrigger value='items'>Sản phẩm</TabsTrigger>
            <TabsTrigger value='shipments'>Phiếu nhập</TabsTrigger>
          </TabsList>

          <TabsContent value='overview' className='space-y-4 mt-4'>
            {orderDetail && <OrderOverviewTab order={orderDetail} />}
          </TabsContent>

          <TabsContent value='items' className='space-y-4 mt-4'>
            <OrderItemsTab orderId={order.id} />
          </TabsContent>

          <TabsContent value='shipments' className='space-y-4 mt-4'>
            <OrderShipmentsTab orderId={order.id} />
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
};
