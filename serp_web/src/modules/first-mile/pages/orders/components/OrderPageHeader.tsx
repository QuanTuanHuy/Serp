/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order page header
 */

import React from 'react';
import { Button } from '@/shared/components/ui';
import { Plus } from 'lucide-react';

interface OrderPageHeaderProps {
  canMutateOrders: boolean;
  onCreateOrder: () => void;
}

export const OrderPageHeader: React.FC<OrderPageHeaderProps> = ({
  canMutateOrders,
  onCreateOrder,
}) => {
  return (
    <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Orders</h1>
        <p className='text-muted-foreground'>
          Track first-mile orders based on your role and access scope.
        </p>
      </div>

      {canMutateOrders ? (
        <Button onClick={onCreateOrder}>
          <Plus className='mr-2 h-4 w-4' />
          New Order
        </Button>
      ) : null}
    </div>
  );
};
