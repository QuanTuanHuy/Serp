/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher candidate orders panel
 */

import React from 'react';
import { Loader2, PackageSearch } from 'lucide-react';
import { Badge } from '@/shared/components/ui';
import {
  countPickupBacklogOrders,
  sortOrdersByBacklogPriority,
} from '../dispatchOrderBacklog';
import { CandidateOrderItem } from './CandidateOrderItem';
import type { CandidateOrdersPanelProps } from './types';

export const CandidateOrdersPanel: React.FC<CandidateOrdersPanelProps> = ({
  title,
  orders,
  loading = false,
  selectedOrderIds = [],
  onOrderToggle,
  maxVisibleOrders = 8,
  emptyText = 'Không tìm thấy đơn hàng ứng viên.',
  referenceTime,
}) => {
  const sortedOrders = React.useMemo(
    () => sortOrdersByBacklogPriority(orders, referenceTime),
    [orders, referenceTime]
  );
  const backlogCount = React.useMemo(
    () => countPickupBacklogOrders(orders, referenceTime),
    [orders, referenceTime]
  );
  const visibleOrders = sortedOrders.slice(0, maxVisibleOrders);
  const hiddenOrderCount = Math.max(
    0,
    sortedOrders.length - visibleOrders.length
  );

  return (
    <section className='rounded-md border bg-background p-4'>
      <div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
        <div className='space-y-1'>
          <div className='flex flex-wrap items-center gap-2'>
            <PackageSearch className='h-4 w-4 text-muted-foreground' />
            <h3 className='text-sm font-semibold'>{title}</h3>
          </div>
          <div className='flex flex-wrap gap-2'>
            <Badge variant='secondary'>{orders.length} đơn ứng viên</Badge>
            <Badge variant={backlogCount > 0 ? 'destructive' : 'outline'}>
              {backlogCount} quá hạn
            </Badge>
          </div>
        </div>

        {onOrderToggle && selectedOrderIds.length > 0 ? (
          <Badge variant='outline'>{selectedOrderIds.length} đã chọn</Badge>
        ) : null}
      </div>

      <div className='mt-4'>
        {loading ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải đơn hàng ứng viên...
          </div>
        ) : visibleOrders.length > 0 ? (
          <div className='grid gap-3 xl:grid-cols-2'>
            {visibleOrders.map((order) => (
              <CandidateOrderItem
                key={order.id}
                order={order}
                checked={selectedOrderIds.includes(order.id)}
                onToggle={onOrderToggle}
                referenceTime={referenceTime}
              />
            ))}
          </div>
        ) : (
          <p className='text-sm text-muted-foreground'>{emptyText}</p>
        )}
      </div>

      {hiddenOrderCount > 0 ? (
        <p className='mt-3 text-xs text-muted-foreground'>
          Còn {hiddenOrderCount} đơn hàng chưa hiển thị trong khung này.
        </p>
      ) : null}
    </section>
  );
};
