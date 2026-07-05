/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order filters (basic/advanced)
 */

import React from 'react';
import { RefreshCw, Search, SlidersHorizontal } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { TmsFilterMode } from '../../../../components/list';
import type { FirstMileOrderStatus, PostOffice } from '../../../../types';
import type {
  OrderFilterFormState,
  TriStateFilter,
} from '../orderFilterModels';
import type { OrderStatusFilter } from '../orderPageModels';

const ALL_POST_OFFICES_VALUE = '__ALL_POST_OFFICES__';

interface OrderFiltersCardProps {
  canViewOrders: boolean;
  filterMode: TmsFilterMode;
  filterFormValues: OrderFilterFormState;
  advancedFieldCount: number;
  statusOptions: FirstMileOrderStatus[];
  postOffices: PostOffice[];
  isLoadingPostOffices: boolean;
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
  postOffices,
  isLoadingPostOffices,
  isFetching,
  onFilterModeChange,
  onFilterFieldChange,
  onApplyFilters,
  onClearFilters,
  onRefresh,
  formatStatusLabel,
}) => {
  const statusFilterOptions = [
    { value: 'ALL', label: 'Tất cả trạng thái' },
    ...statusOptions.map((statusOption) => ({
      value: statusOption,
      label: formatStatusLabel(statusOption),
    })),
  ];
  const confirmFilterOptions = [
    { value: 'ALL', label: 'Tất cả' },
    { value: 'YES', label: 'Đã xác nhận' },
    { value: 'NO', label: 'Chưa xác nhận' },
  ];
  const postOfficeFilterOptions = [
    { value: ALL_POST_OFFICES_VALUE, label: 'Tất cả bưu cục' },
    ...postOffices.map((postOffice) => ({
      value: postOffice.code,
      label: `${postOffice.code} - ${postOffice.name}`,
    })),
  ];

  const advancedFilters = (
    <>
      <div className='space-y-2'>
        <Label htmlFor='order-filter-order-code'>Mã đơn hàng</Label>
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
          Mã đơn của khách hàng
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
        <Label htmlFor='order-filter-sender-phone'>
          Số điện thoại người gửi
        </Label>
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
        <Label htmlFor='order-filter-receiver-phone'>
          Số điện thoại người nhận
        </Label>
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
        <Label htmlFor='order-filter-origin-po'>Bưu cục gốc</Label>
        <TmsCombobox
          id='order-filter-origin-po'
          value={
            filterFormValues.originPostOfficeCode || ALL_POST_OFFICES_VALUE
          }
          onValueChange={(value) =>
            onFilterFieldChange(
              'originPostOfficeCode',
              value === ALL_POST_OFFICES_VALUE ? '' : value
            )
          }
          options={postOfficeFilterOptions}
          placeholder='Tất cả bưu cục'
          emptyText='Không tìm thấy bưu cục'
          disabled={!canViewOrders || isLoadingPostOffices}
          loading={isLoadingPostOffices}
        />
      </div>

      <div className='space-y-2'>
        <Label htmlFor='order-filter-destination-po'>Bưu cục đích</Label>
        <TmsCombobox
          id='order-filter-destination-po'
          value={
            filterFormValues.destinationPostOfficeCode || ALL_POST_OFFICES_VALUE
          }
          onValueChange={(value) =>
            onFilterFieldChange(
              'destinationPostOfficeCode',
              value === ALL_POST_OFFICES_VALUE ? '' : value
            )
          }
          options={postOfficeFilterOptions}
          placeholder='Tất cả bưu cục'
          emptyText='Không tìm thấy bưu cục'
          disabled={!canViewOrders || isLoadingPostOffices}
          loading={isLoadingPostOffices}
        />
      </div>

      <div className='space-y-2'>
        <Label htmlFor='order-filter-is-confirm'>Xác nhận</Label>
        <TmsCombobox
          id='order-filter-is-confirm'
          value={filterFormValues.isConfirm}
          onValueChange={(value) =>
            onFilterFieldChange('isConfirm', value as TriStateFilter)
          }
          options={confirmFilterOptions}
          placeholder='Tất cả'
          emptyText='Không tìm thấy lựa chọn'
          disabled={!canViewOrders}
        />
      </div>

      <div className='space-y-2'>
        <Label htmlFor='order-filter-created-from'>Tạo từ</Label>
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
        <Label htmlFor='order-filter-created-to'>Tạo đến</Label>
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
        <Label htmlFor='order-filter-pickup-from'>Lấy hàng từ</Label>
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
        <Label htmlFor='order-filter-pickup-to'>Lấy hàng đến</Label>
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
  );

  return (
    <>
      <Card className='gap-3 py-5'>
        <CardHeader className='px-5 py-0'>
          <div className='flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
            <CardTitle className='text-base'>Tìm kiếm và bộ lọc</CardTitle>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() => onFilterModeChange('advanced')}
              disabled={!canViewOrders}
              className='w-full justify-center sm:w-auto'
            >
              <SlidersHorizontal className='mr-2 h-4 w-4' />
              Nâng cao
              {advancedFieldCount > 0 ? (
                <Badge variant='secondary' className='ml-2 h-5 px-1.5 text-xs'>
                  {advancedFieldCount}
                </Badge>
              ) : null}
            </Button>
          </div>
        </CardHeader>

        <CardContent className='px-5 py-0'>
          <form
            onSubmit={onApplyFilters}
            className='grid gap-3 md:grid-cols-[minmax(280px,1fr)_220px] xl:grid-cols-[minmax(320px,1fr)_220px_auto]'
          >
            <div className='space-y-2'>
              <Label htmlFor='order-filter-keyword'>Từ khóa</Label>
              <div className='relative'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  id='order-filter-keyword'
                  className='pl-10'
                  value={filterFormValues.keyword}
                  onChange={(event) =>
                    onFilterFieldChange('keyword', event.target.value)
                  }
                  placeholder='Mã đơn hàng, mã khách hàng, tên, số điện thoại...'
                  disabled={!canViewOrders}
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='order-filter-status'>Trạng thái</Label>
              <TmsCombobox
                id='order-filter-status'
                value={filterFormValues.status}
                onValueChange={(value) =>
                  onFilterFieldChange('status', value as OrderStatusFilter)
                }
                options={statusFilterOptions}
                placeholder='Tất cả trạng thái'
                emptyText='Không tìm thấy trạng thái'
                disabled={!canViewOrders}
              />
            </div>

            <div className='flex flex-wrap items-end gap-2 md:col-span-2 xl:col-span-1 xl:justify-end'>
              <Button type='submit' size='sm' disabled={!canViewOrders}>
                Áp dụng
              </Button>
              <Button
                type='button'
                size='sm'
                variant='outline'
                onClick={onClearFilters}
                disabled={!canViewOrders}
              >
                Xóa bộ lọc
              </Button>
              <Button
                type='button'
                size='sm'
                variant='outline'
                onClick={onRefresh}
                disabled={!canViewOrders || isFetching}
              >
                <RefreshCw
                  className={cn('mr-2 h-4 w-4', isFetching && 'animate-spin')}
                />
                Làm mới
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Dialog
        open={filterMode === 'advanced'}
        onOpenChange={(open) => onFilterModeChange(open ? 'advanced' : 'basic')}
      >
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-4xl'>
          <form onSubmit={onApplyFilters} className='space-y-5'>
            <DialogHeader>
              <DialogTitle>Bộ lọc nâng cao</DialogTitle>
              <DialogDescription>
                Lọc đơn hàng theo mã chính xác, bưu cục được gán, trạng thái xác
                nhận và khoảng thời gian.
              </DialogDescription>
            </DialogHeader>

            <div className='grid gap-3 sm:grid-cols-2 lg:grid-cols-3'>
              {advancedFilters}
            </div>

            <DialogFooter className='gap-2 sm:gap-0'>
              <Button
                type='button'
                variant='outline'
                onClick={onClearFilters}
                disabled={!canViewOrders}
              >
                Xóa tất cả
              </Button>
              <Button type='submit' disabled={!canViewOrders}>
                Áp dụng bộ lọc
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
};
