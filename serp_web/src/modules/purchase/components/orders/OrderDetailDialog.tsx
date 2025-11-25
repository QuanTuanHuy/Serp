/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Detail Dialog Component
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
import type { OrderDetail } from '../../types';

interface OrderDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  order?: OrderDetail;
  onRefresh: () => void;
}

export const OrderDetailDialog: React.FC<OrderDetailDialogProps> = ({
  open,
  onOpenChange,
  order,
  onRefresh,
}) => {
  const [activeTab, setActiveTab] = useState('overview');

  if (!order) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-7xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>{order.orderName}</DialogTitle>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={setActiveTab} className='w-full'>
          <TabsList className='grid w-full grid-cols-3'>
            <TabsTrigger value='overview'>Tổng quan</TabsTrigger>
            <TabsTrigger value='items'>Sản phẩm</TabsTrigger>
            <TabsTrigger value='shipments'>Phiếu nhập</TabsTrigger>
          </TabsList>

          <TabsContent value='overview' className='space-y-4 mt-4'>
            <OrderOverviewTab order={order} onRefresh={onRefresh} />
          </TabsContent>

          <TabsContent value='items' className='space-y-4 mt-4'>
            <OrderItemsTab order={order} onRefresh={onRefresh} />
          </TabsContent>

          <TabsContent value='shipments' className='space-y-4 mt-4'>
            <OrderShipmentsTab order={order} />
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
};
