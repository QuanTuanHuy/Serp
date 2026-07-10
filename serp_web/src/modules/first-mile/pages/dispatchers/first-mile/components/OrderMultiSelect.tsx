/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Multi-select dropdown for dispatch orders
 */

'use client';

import React from 'react';
import { Check, ChevronsUpDown, Loader2, X } from 'lucide-react';
import { Badge, Button } from '@/shared/components/ui';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/shared/components/ui/command';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/shared/components/ui/popover';
import type { FirstMileOrderDetail } from '../../../../types';
import { formatOrderStatusLabel } from '../../../../utils/orderStatusLabels';
import {
  formatPickupBacklogDuration,
  formatPickupWindow,
  getPickupBacklogMinutes,
  isPickupBacklogOrder,
  sortOrdersByBacklogPriority,
} from '../dispatchOrderBacklog';

export interface OrderMultiSelectProps {
  orders: FirstMileOrderDetail[];
  selectedOrderIds: number[];
  onSelectionChange: (orderIds: number[]) => void;
  disabled?: boolean;
  loading?: boolean;
  placeholder?: string;
}

const buildOrderSearchValue = (order: FirstMileOrderDetail): string => {
  return [
    order.orderCode,
    order.customerOrderCode,
    order.senderName,
    order.senderPhone,
    order.receiverName,
    order.status,
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();
};

const buildOrderLabel = (order: FirstMileOrderDetail): string => {
  const customerCode = order.customerOrderCode
    ? ` | ${order.customerOrderCode}`
    : '';
  return `${order.orderCode}${customerCode}`;
};

export const OrderMultiSelect: React.FC<OrderMultiSelectProps> = ({
  orders,
  selectedOrderIds,
  onSelectionChange,
  disabled = false,
  loading = false,
  placeholder = 'Chọn đơn hàng...',
}) => {
  const [open, setOpen] = React.useState(false);

  const orderById = React.useMemo(
    () => new Map(orders.map((order) => [order.id, order])),
    [orders]
  );
  const sortedOrders = React.useMemo(
    () => sortOrdersByBacklogPriority(orders),
    [orders]
  );

  const handleToggle = (orderId: number) => {
    if (selectedOrderIds.includes(orderId)) {
      onSelectionChange(selectedOrderIds.filter((id) => id !== orderId));
      return;
    }

    onSelectionChange([...selectedOrderIds, orderId]);
  };

  const handleRemove = (
    event: React.MouseEvent<HTMLDivElement>,
    orderId: number
  ) => {
    event.preventDefault();
    event.stopPropagation();
    onSelectionChange(selectedOrderIds.filter((id) => id !== orderId));
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type='button'
          variant='outline'
          role='combobox'
          aria-expanded={open}
          className='min-h-[44px] h-auto w-full justify-between'
          disabled={disabled || loading}
        >
          {loading ? (
            <span className='flex items-center gap-2 text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Đang tải đơn hàng...
            </span>
          ) : selectedOrderIds.length > 0 ? (
            <div className='flex flex-wrap items-center gap-1'>
              {selectedOrderIds.length > 3 ? (
                <Badge variant='secondary'>
                  {selectedOrderIds.length} đơn đã chọn
                </Badge>
              ) : (
                selectedOrderIds.map((orderId) => {
                  const order = orderById.get(orderId);
                  return (
                    <Badge key={orderId} variant='secondary' className='mr-1'>
                      {order ? buildOrderLabel(order) : `#${orderId}`}
                      <div
                        className='ml-1 cursor-pointer rounded-full outline-none ring-offset-background focus:ring-2 focus:ring-ring focus:ring-offset-2'
                        onMouseDown={(event) => {
                          event.preventDefault();
                          event.stopPropagation();
                        }}
                        onClick={(event) => handleRemove(event, orderId)}
                      >
                        <X className='h-3 w-3 text-muted-foreground hover:text-foreground' />
                      </div>
                    </Badge>
                  );
                })
              )}
            </div>
          ) : (
            <span className='text-muted-foreground'>{placeholder}</span>
          )}
          <ChevronsUpDown className='ml-2 h-4 w-4 shrink-0 opacity-50' />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className='w-[var(--radix-popover-trigger-width)] p-0'
        align='start'
      >
        <Command>
          <CommandInput placeholder='Tìm theo mã đơn, mã đơn khách hàng...' />
          <CommandList>
            <CommandEmpty>Không tìm thấy đơn hàng ứng viên.</CommandEmpty>
            <CommandGroup heading='Đơn hàng ứng viên'>
              {sortedOrders.map((order) => {
                const isSelected = selectedOrderIds.includes(order.id);
                const isBacklog = isPickupBacklogOrder(order);
                const backlogDuration = formatPickupBacklogDuration(
                  getPickupBacklogMinutes(order)
                );

                return (
                  <CommandItem
                    key={order.id}
                    value={buildOrderSearchValue(order)}
                    onSelect={() => handleToggle(order.id)}
                    className='cursor-pointer'
                  >
                    <div className='flex w-full items-start gap-2'>
                      <div className='flex min-w-0 flex-1 flex-col'>
                        <span className='font-medium'>{order.orderCode}</span>
                        <span className='text-xs text-muted-foreground'>
                          {order.customerOrderCode || '--'} |{' '}
                          {formatOrderStatusLabel(order.status)} |{' '}
                          {order.senderName || '--'}
                        </span>
                        <span className='text-xs text-muted-foreground'>
                          Khung giờ lấy hàng: {formatPickupWindow(order)}
                          {backlogDuration ? ` | ${backlogDuration}` : ''}
                        </span>
                      </div>
                      {isBacklog ? (
                        <Badge variant='destructive' className='mt-0.5'>
                          Quá hạn
                        </Badge>
                      ) : null}
                      {isSelected ? (
                        <Check className='mt-0.5 h-4 w-4 shrink-0 text-primary' />
                      ) : null}
                    </div>
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
};
