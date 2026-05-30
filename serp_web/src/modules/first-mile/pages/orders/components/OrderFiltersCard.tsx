/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order filters (basic/advanced)
 */

import React from 'react';
import {
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Search } from 'lucide-react';
import {
  TmsListFilterPanel,
  type TmsFilterMode,
} from '../../../components/list';
import type { FirstMileOrderStatus } from '../../../types';
import type { OrderFilterFormState, TriStateFilter } from '../orderFilterModels';
import type { OrderStatusFilter } from '../orderPageModels';

interface OrderFiltersCardProps {
  canViewOrders: boolean;
  filterMode: TmsFilterMode;
  filterFormValues: OrderFilterFormState;
  advancedFieldCount: number;
  statusOptions: FirstMileOrderStatus[];
  isFetching: boolean;
  onFilterModeChange: (mode: TmsFilterMode) => void;
  onFilterFieldChange: <K extends keyof OrderFilterFormState>(
    field: K,
    value: OrderFilterFormState[K]
  ) => void;
  onApplyFilters: (event: React.FormEvent) => void;
  onClearFilters: () => void;
  onRefresh: () => void;
  formatStatusLabel: (status: FirstMileOrderStatus) => string;
}

export const OrderFiltersCard: React.FC<OrderFiltersCardProps> = ({
  canViewOrders,
  filterMode,
  filterFormValues,
  advancedFieldCount,
  statusOptions,
  isFetching,
  onFilterModeChange,
  onFilterFieldChange,
  onApplyFilters,
  onClearFilters,
  onRefresh,
  formatStatusLabel,
}) => {
  return (
    <TmsListFilterPanel
      title='Search & filters'
      description='Basic search uses keyword and status. Advanced filters match the orders API query parameters.'
      filterMode={filterMode}
      onFilterModeChange={onFilterModeChange}
      advancedFieldCount={advancedFieldCount}
      disabled={!canViewOrders}
      isFetching={isFetching}
      onApply={onApplyFilters}
      onClear={onClearFilters}
      onRefresh={onRefresh}
      basicFilters={
        <>
          <div className='space-y-2 sm:col-span-2'>
            <Label htmlFor='order-filter-keyword'>Keyword</Label>
            <div className='relative'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                id='order-filter-keyword'
                className='pl-10'
                value={filterFormValues.keyword}
                onChange={(event) =>
                  onFilterFieldChange('keyword', event.target.value)
                }
                placeholder='Order code, customer code, names, phones...'
                disabled={!canViewOrders}
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-status'>Status</Label>
            <Select
              value={filterFormValues.status}
              onValueChange={(value) =>
                onFilterFieldChange('status', value as OrderStatusFilter)
              }
              disabled={!canViewOrders}
            >
              <SelectTrigger id='order-filter-status' className='w-full'>
                <SelectValue placeholder='All statuses' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All statuses</SelectItem>
                {statusOptions.map((statusOption) => (
                  <SelectItem key={statusOption} value={statusOption}>
                    {formatStatusLabel(statusOption)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </>
      }
      advancedFilters={
        <>
          <div className='space-y-2'>
            <Label htmlFor='order-filter-order-code'>Order code</Label>
            <Input
              id='order-filter-order-code'
              value={filterFormValues.orderCode}
              onChange={(event) =>
                onFilterFieldChange('orderCode', event.target.value)
              }
              placeholder='ORD-...'
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-customer-order-code'>
              Customer order code
            </Label>
            <Input
              id='order-filter-customer-order-code'
              value={filterFormValues.customerOrderCode}
              onChange={(event) =>
                onFilterFieldChange('customerOrderCode', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-sender-phone'>Sender phone</Label>
            <Input
              id='order-filter-sender-phone'
              value={filterFormValues.senderPhone}
              onChange={(event) =>
                onFilterFieldChange('senderPhone', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-receiver-phone'>Receiver phone</Label>
            <Input
              id='order-filter-receiver-phone'
              value={filterFormValues.receiverPhone}
              onChange={(event) =>
                onFilterFieldChange('receiverPhone', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-origin-po'>Origin post office</Label>
            <Input
              id='order-filter-origin-po'
              value={filterFormValues.originPostOfficeCode}
              onChange={(event) =>
                onFilterFieldChange('originPostOfficeCode', event.target.value)
              }
              placeholder='Post office code'
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-destination-po'>
              Destination post office
            </Label>
            <Input
              id='order-filter-destination-po'
              value={filterFormValues.destinationPostOfficeCode}
              onChange={(event) =>
                onFilterFieldChange(
                  'destinationPostOfficeCode',
                  event.target.value
                )
              }
              placeholder='Post office code'
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-is-confirm'>Confirmed</Label>
            <Select
              value={filterFormValues.isConfirm}
              onValueChange={(value) =>
                onFilterFieldChange('isConfirm', value as TriStateFilter)
              }
              disabled={!canViewOrders}
            >
              <SelectTrigger id='order-filter-is-confirm' className='w-full'>
                <SelectValue placeholder='All' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All</SelectItem>
                <SelectItem value='YES'>Confirmed</SelectItem>
                <SelectItem value='NO'>Not confirmed</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-created-from'>Created from</Label>
            <Input
              id='order-filter-created-from'
              type='datetime-local'
              value={filterFormValues.createdFrom}
              onChange={(event) =>
                onFilterFieldChange('createdFrom', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-created-to'>Created to</Label>
            <Input
              id='order-filter-created-to'
              type='datetime-local'
              value={filterFormValues.createdTo}
              onChange={(event) =>
                onFilterFieldChange('createdTo', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-pickup-from'>Pickup from</Label>
            <Input
              id='order-filter-pickup-from'
              type='datetime-local'
              value={filterFormValues.pickupFrom}
              onChange={(event) =>
                onFilterFieldChange('pickupFrom', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='order-filter-pickup-to'>Pickup to</Label>
            <Input
              id='order-filter-pickup-to'
              type='datetime-local'
              value={filterFormValues.pickupTo}
              onChange={(event) =>
                onFilterFieldChange('pickupTo', event.target.value)
              }
              disabled={!canViewOrders}
            />
          </div>
        </>
      }
    />
  );
};
