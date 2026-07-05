/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order page header
 */

import React from 'react';
import { Button } from '@/shared/components/ui';
import { Plus } from 'lucide-react';

interface OrderPageHeaderProps {
  canMutateOrders: boolean;
  importAction?: React.ReactNode;
  onCreateOrder: () => void;
}

export const OrderPageHeader: React.FC<OrderPageHeaderProps> = ({
  canMutateOrders,
  importAction,
  onCreateOrder,
}) => {
  return (
    <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Đơn hàng</h1>
        <p className='text-muted-foreground'>
          Theo dõi đơn hàng theo vai trò và phạm vi truy cập của bạn.
        </p>
      </div>

      <div className='flex flex-wrap items-center gap-2'>
        {importAction}
        {canMutateOrders ? (
          <Button onClick={onCreateOrder}>
            <Plus className='mr-2 h-4 w-4' />
            Tạo đơn hàng mới
          </Button>
        ) : null}
      </div>
    </div>
  );
};
