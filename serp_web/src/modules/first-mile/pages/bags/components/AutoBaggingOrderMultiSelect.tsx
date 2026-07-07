/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Auto bagging order multi-select
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

import type { SecondMileOrder } from '../../../types';

interface AutoBaggingOrderMultiSelectProps {
  id?: string;
  orders: SecondMileOrder[];
  selectedOrderCodes: string[];
  onSelectionChange: (orderCodes: string[]) => void;
  disabled?: boolean;
  loading?: boolean;
  placeholder?: string;
}

const buildOrderSearchValue = (order: SecondMileOrder): string => {
  return [
    order.orderCode,
    order.customerOrderCode,
    order.originPostOfficeCode,
    order.destinationPostOfficeCode,
    order.status,
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();
};

const buildOrderLabel = (order: SecondMileOrder): string => {
  const customerCode = order.customerOrderCode
    ? ` | ${order.customerOrderCode}`
    : '';
  return `${order.orderCode ?? ''}${customerCode}`;
};

export const AutoBaggingOrderMultiSelect: React.FC<
  AutoBaggingOrderMultiSelectProps
> = ({
  id,
  orders,
  selectedOrderCodes,
  onSelectionChange,
  disabled = false,
  loading = false,
  placeholder = 'Chọn đơn hàng...',
}) => {
  const [open, setOpen] = React.useState(false);

  const ordersWithCode = React.useMemo(
    () => orders.filter((order) => Boolean(order.orderCode)),
    [orders]
  );

  const orderByCode = React.useMemo(
    () =>
      new Map(ordersWithCode.map((order) => [String(order.orderCode), order])),
    [ordersWithCode]
  );

  const handleToggle = (orderCode: string) => {
    if (selectedOrderCodes.includes(orderCode)) {
      onSelectionChange(
        selectedOrderCodes.filter((code) => code !== orderCode)
      );
      return;
    }

    onSelectionChange([...selectedOrderCodes, orderCode]);
  };

  const handleRemove = (
    event: React.MouseEvent<HTMLDivElement>,
    orderCode: string
  ) => {
    event.preventDefault();
    event.stopPropagation();
    onSelectionChange(selectedOrderCodes.filter((code) => code !== orderCode));
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          id={id}
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
          ) : selectedOrderCodes.length > 0 ? (
            <div className='flex flex-wrap items-center gap-1'>
              {selectedOrderCodes.length > 3 ? (
                <Badge variant='secondary'>
                  Đã chọn {selectedOrderCodes.length} đơn
                </Badge>
              ) : (
                selectedOrderCodes.map((orderCode) => {
                  const order = orderByCode.get(orderCode);
                  return (
                    <Badge key={orderCode} variant='secondary' className='mr-1'>
                      {order ? buildOrderLabel(order) : orderCode}
                      <div
                        className='ml-1 cursor-pointer rounded-full outline-none ring-offset-background focus:ring-2 focus:ring-ring focus:ring-offset-2'
                        onMouseDown={(event) => {
                          event.preventDefault();
                          event.stopPropagation();
                        }}
                        onClick={(event) => handleRemove(event, orderCode)}
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
          <CommandInput placeholder='Tìm theo mã đơn, bưu cục...' />
          <CommandList>
            <CommandEmpty>Không tìm thấy đơn phù hợp.</CommandEmpty>
            <CommandGroup heading='Đơn hàng phù hợp'>
              {ordersWithCode.map((order) => {
                const orderCode = String(order.orderCode);
                const isSelected = selectedOrderCodes.includes(orderCode);

                return (
                  <CommandItem
                    key={orderCode}
                    value={buildOrderSearchValue(order)}
                    onSelect={() => handleToggle(orderCode)}
                    className='cursor-pointer'
                  >
                    <div className='flex w-full items-start gap-2'>
                      <div className='flex min-w-0 flex-1 flex-col'>
                        <span className='font-medium'>{order.orderCode}</span>
                        <span className='text-xs text-muted-foreground'>
                          {order.customerOrderCode || '--'} |{' '}
                          {order.status || '--'} |{' '}
                          {order.originPostOfficeCode || '--'}
                        </span>
                      </div>
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
