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
import type { FirstMileOrderStatus } from '../../../../types';

const formatOrderStatusLabel = (status: FirstMileOrderStatus): string => {
  switch (status) {
    case 'CREATED':
      return 'Mới tạo';
    case 'ASSIGNED_TO_PICKUP':
      return 'Đã phân công lấy hàng';
    case 'PICKING_UP':
      return 'Đang lấy hàng';
    case 'PICKUP_FAILED':
      return 'Lấy hàng thất bại';
    case 'PICKED_UP':
      return 'Đã lấy hàng';
    case 'PENDING_ORIGIN_POST_OFFICE_INBOUND':
      return 'Chờ nhập bưu cục gốc';
    case 'AT_ORIGIN_POST_OFFICE':
      return 'Tại bưu cục gốc';
    case 'CANCELLED':
      return 'Đã hủy';
    case 'LOST_OR_DAMAGED':
      return 'Thất lạc / hư hỏng';
    default:
      return status.replaceAll('_', ' ');
  }
};

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
