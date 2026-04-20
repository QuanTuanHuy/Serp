/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - Dispatcher candidate order item
 */

import React from 'react';
import { Badge, Checkbox } from '@/shared/components/ui';
import type { CandidateOrderItemProps } from './types';

export const CandidateOrderItem: React.FC<CandidateOrderItemProps> = ({
  order,
  checked,
  onToggle,
}) => {
  return (
    <div className='rounded-md border p-3'>
      <div className='flex items-start gap-3'>
        <Checkbox
          checked={checked}
          onCheckedChange={(value) => onToggle(order.id, Boolean(value))}
          aria-label={`Select order ${order.orderCode}`}
        />
        <div className='flex-1 min-w-0 space-y-1'>
          <div className='flex flex-wrap items-center gap-2'>
            <p className='font-medium'>{order.orderCode}</p>
            <Badge variant='outline'>{order.status}</Badge>
            <Badge variant='secondary'>
              {order.isConfirm ? 'Confirmed' : 'Pending confirm'}
            </Badge>
          </div>
          <p className='text-xs text-muted-foreground'>
            Customer order: {order.customerOrderCode || '--'}
          </p>
          <p className='text-xs text-muted-foreground'>
            Sender: {order.senderName || '--'} ({order.senderPhone || '--'})
            {' | '}Receiver: {order.receiverName || '--'} (
            {order.receiverPhone || '--'})
          </p>
        </div>
      </div>
    </div>
  );
};
