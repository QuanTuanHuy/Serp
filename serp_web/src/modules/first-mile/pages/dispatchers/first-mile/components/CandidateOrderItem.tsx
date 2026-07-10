/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher candidate order item
 */

import React from 'react';
import { Badge, Checkbox } from '@/shared/components/ui';
import type { CandidateOrderItemProps } from './types';
import {
  formatPickupBacklogDuration,
  formatPickupWindow,
  getPickupBacklogMinutes,
  isPickupBacklogOrder,
} from '../dispatchOrderBacklog';
import { formatOrderStatusLabel } from '../../../../utils/orderStatusLabels';

export const CandidateOrderItem: React.FC<CandidateOrderItemProps> = ({
  order,
  checked = false,
  onToggle,
  referenceTime,
}) => {
  const isBacklog = isPickupBacklogOrder(order, referenceTime);
  const backlogDuration = formatPickupBacklogDuration(
    getPickupBacklogMinutes(order, referenceTime)
  );

  return (
    <div className='rounded-md border p-3'>
      <div className='flex items-start gap-3'>
        {onToggle ? (
          <Checkbox
            checked={checked}
            onCheckedChange={(value) => onToggle(order.id, Boolean(value))}
            aria-label={`Chọn đơn hàng ${order.orderCode}`}
          />
        ) : null}
        <div className='flex-1 min-w-0 space-y-1'>
          <div className='flex flex-wrap items-center gap-2'>
            <p className='font-medium'>{order.orderCode}</p>
            {isBacklog ? <Badge variant='destructive'>Quá hạn</Badge> : null}
            <Badge variant='outline'>
              {formatOrderStatusLabel(order.status)}
            </Badge>
            <Badge variant='secondary'>
              {order.isConfirm ? 'Đã xác nhận' : 'Chờ xác nhận'}
            </Badge>
          </div>
          <p className='text-xs text-muted-foreground'>
            Mã đơn khách hàng: {order.customerOrderCode || '--'}
          </p>
          <p className='text-xs text-muted-foreground'>
            Người gửi: {order.senderName || '--'} ({order.senderPhone || '--'})
            {' | '}Người nhận: {order.receiverName || '--'} (
            {order.receiverPhone || '--'})
          </p>
          <p className='text-xs text-muted-foreground'>
            Khung giờ lấy hàng: {formatPickupWindow(order)}
            {backlogDuration ? ` | ${backlogDuration}` : ''}
          </p>
        </div>
      </div>
    </div>
  );
};
