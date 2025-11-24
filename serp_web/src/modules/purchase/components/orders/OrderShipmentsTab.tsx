/*
Author: QuanTuanHuy
Description: Part of Serp Project - Order Shipments Tab Component (Placeholder)
*/

'use client';

import React from 'react';
import { Card, CardContent } from '@/shared/components/ui';
import { Package } from 'lucide-react';
import type { OrderDetail } from '../../types';

interface OrderShipmentsTabProps {
  order: OrderDetail;
}

export const OrderShipmentsTab: React.FC<OrderShipmentsTabProps> = ({
  order,
}) => {
  return (
    <Card>
      <CardContent className='p-8'>
        <div className='flex flex-col items-center justify-center space-y-4 text-center'>
          <div className='rounded-full bg-muted p-4'>
            <Package className='h-8 w-8 text-muted-foreground' />
          </div>
          <div>
            <h3 className='text-lg font-semibold'>Phiếu nhập hàng</h3>
            <p className='text-sm text-muted-foreground mt-2'>
              Tính năng quản lý phiếu nhập hàng sẽ được triển khai trong phiên
              bản tiếp theo
            </p>
          </div>
          {order.statusId === 'CREATED' && (
            <p className='text-xs text-muted-foreground italic'>
              Đơn hàng chưa được phê duyệt
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
