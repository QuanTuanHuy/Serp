/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order results card
 */

import React from 'react';
import {
  ArrowDownAZ,
  ArrowUpAZ,
  Building2,
  CheckCircle2,
  Eye,
  Loader2,
  Pencil,
  RefreshCw,
  Search,
  Trash2,
  X,
  XCircle,
} from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { cn } from '@/shared/utils';
import type {
  FirstMileOrderDetail,
  FirstMileOrderPickupMethod,
  FirstMileOrderStatus,
  FirstMilePaginatedData,
  PostOffice,
} from '../../../../types';
import type { OrderFilterFormState } from '../orderFilterModels';

interface OrderResultsCardProps {
  canViewOrders: boolean;
  canMutateOrders: boolean;
  canConfirmDropOffAtPostOffice: boolean;
  data?: FirstMilePaginatedData<FirstMileOrderDetail>;
  isLoading: boolean;
  isFetching: boolean;
  filterFormValues: OrderFilterFormState;
  statusOptions: FirstMileOrderStatus[];
  postOffices: PostOffice[];
  isLoadingPostOffices: boolean;
  loadingOrderActionId: number | null;
  confirmingOrderId: number | null;
  loadingDropOffSuggestionOrderId: number | null;
  onFilterFieldChange: <K extends keyof OrderFilterFormState>(
    field: K,
    value: OrderFilterFormState[K]
  ) => void;
  onApplyFilters: (values?: OrderFilterFormState) => void;
  onClearFilters: () => void;
  onRefresh: () => void;
  onViewDetail: (orderId: number) => void;
  onEdit: (order: FirstMileOrderDetail) => void;
  onRequestCancel: (order: FirstMileOrderDetail) => void;
  onRequestDelete: (order: FirstMileOrderDetail) => void;
  onConfirm: (order: FirstMileOrderDetail) => void;
  onOpenDropOffSuggestions: (order: FirstMileOrderDetail) => void;
  onOpenManagerDropOffConfirm: (order: FirstMileOrderDetail) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  pageSize: number;
  onPageSizeChange: (pageSize: number) => void;
  formatStatusLabel: (status: FirstMileOrderStatus) => string;
  formatPickupMethodLabel: (
    pickupMethod?: FirstMileOrderDetail['pickupMethod']
  ) => string;
  getStatusBadgeVariant: (
    status: FirstMileOrderStatus
  ) => 'default' | 'secondary' | 'outline' | 'destructive';
  isDraftOrder: (order: FirstMileOrderDetail) => boolean;
  isConfirmableStatus: (status: FirstMileOrderStatus) => boolean;
  isDropOffOrder: (order: FirstMileOrderDetail) => boolean;
  buildOrderAddressLabel: (
    name?: string,
    phone?: string,
    addressDetail?: string
  ) => string;
  buildPostOfficeAssignmentLabel: (order: FirstMileOrderDetail) => string;
  formatDateTime: (value?: string) => string;
}

const ALL_POST_OFFICES_VALUE = '__ALL_POST_OFFICES__';
const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

type OrderTableFilterKey =
  | 'order'
  | 'status'
  | 'pickupMethod'
  | 'sender'
  | 'receiver'
  | 'postOffice'
  | 'pickupWindow'
  | 'updated';

const ORDER_PICKUP_METHOD_FILTER_OPTIONS: Array<{
  value: 'ALL' | FirstMileOrderPickupMethod;
  label: string;
}> = [
  { value: 'ALL', label: 'Tất cả phương thức' },
  { value: 'COURIER_PICKUP', label: 'Nhân viên lấy hàng' },
  { value: 'DROP_OFF_AT_POST_OFFICE', label: 'Gửi tại bưu cục' },
];

const updatedSortOptions = [
  { value: 'NONE', label: 'Không sắp xếp' },
  { value: 'asc', label: 'Cập nhật tăng dần' },
  { value: 'desc', label: 'Cập nhật giảm dần' },
];

interface FilterableHeaderProps {
  label: string;
  filterKey: OrderTableFilterKey;
  openFilter: OrderTableFilterKey | null;
  isActive: boolean;
  title: string;
  disabled: boolean;
  widthClass: string;
  children: React.ReactNode;
  sortButton?: React.ReactNode;
  onOpenFilterChange: (filter: OrderTableFilterKey | null) => void;
}

const FilterableHeader: React.FC<FilterableHeaderProps> = ({
  label,
  filterKey,
  openFilter,
  isActive,
  title,
  disabled,
  widthClass,
  children,
  sortButton,
  onOpenFilterChange,
}) => (
  <TableHead className={widthClass}>
    <Popover
      open={openFilter === filterKey}
      onOpenChange={(open) => onOpenFilterChange(open ? filterKey : null)}
    >
      <div className='flex items-center gap-1'>
        <span>{label}</span>
        <PopoverTrigger asChild>
          <Button
            type='button'
            variant={isActive ? 'outline' : 'ghost'}
            size='icon'
            className='size-7'
            title={title}
            aria-label={title}
            disabled={disabled}
          >
            <Search className='h-4 w-4' />
          </Button>
        </PopoverTrigger>
        {sortButton}
      </div>
      <PopoverContent align='start' sideOffset={8} className='w-72 p-3'>
        {children}
      </PopoverContent>
    </Popover>
  </TableHead>
);

export const OrderResultsCard: React.FC<OrderResultsCardProps> = ({
  canViewOrders,
  canMutateOrders,
  canConfirmDropOffAtPostOffice,
  data,
  isLoading,
  isFetching,
  filterFormValues,
  statusOptions,
  postOffices,
  isLoadingPostOffices,
  loadingOrderActionId,
  confirmingOrderId,
  loadingDropOffSuggestionOrderId,
  onFilterFieldChange,
  onApplyFilters,
  onClearFilters,
  onRefresh,
  onViewDetail,
  onEdit,
  onRequestCancel,
  onRequestDelete,
  onConfirm,
  onOpenDropOffSuggestions,
  onOpenManagerDropOffConfirm,
  onPreviousPage,
  onNextPage,
  pageSize,
  onPageSizeChange,
  formatStatusLabel,
  formatPickupMethodLabel,
  getStatusBadgeVariant,
  isDraftOrder,
  isConfirmableStatus,
  isDropOffOrder,
  buildOrderAddressLabel,
  buildPostOfficeAssignmentLabel,
  formatDateTime,
}) => {
  const [openTableFilter, setOpenTableFilter] =
    React.useState<OrderTableFilterKey | null>(null);
  const orderFilterInputRef = React.useRef<HTMLInputElement>(null);
  const senderFilterInputRef = React.useRef<HTMLInputElement>(null);
  const receiverFilterInputRef = React.useRef<HTMLInputElement>(null);

  React.useEffect(() => {
    if (!openTableFilter) return;

    window.setTimeout(() => {
      if (openTableFilter === 'order') {
        orderFilterInputRef.current?.focus();
        orderFilterInputRef.current?.select();
      } else if (openTableFilter === 'sender') {
        senderFilterInputRef.current?.focus();
        senderFilterInputRef.current?.select();
      } else if (openTableFilter === 'receiver') {
        receiverFilterInputRef.current?.focus();
        receiverFilterInputRef.current?.select();
      }
    }, 0);
  }, [openTableFilter]);

  const statusFilterOptions = [
    { value: 'ALL', label: 'Tất cả trạng thái' },
    ...statusOptions.map((statusOption) => ({
      value: statusOption,
      label: formatStatusLabel(statusOption),
    })),
  ];
  const confirmFilterOptions = [
    { value: 'ALL', label: 'Tất cả xác nhận' },
    { value: 'YES', label: 'Đã xác nhận' },
    { value: 'NO', label: 'Chờ xác nhận' },
  ];
  const postOfficeFilterOptions = [
    { value: ALL_POST_OFFICES_VALUE, label: 'Tất cả bưu cục' },
    ...postOffices.map((postOffice) => ({
      value: postOffice.code,
      label: `${postOffice.code} - ${postOffice.name}`,
    })),
  ];

  const isOrderFilterActive = Boolean(
    filterFormValues.orderCode.trim() ||
      filterFormValues.customerOrderCode.trim()
  );
  const isStatusFilterActive =
    filterFormValues.status !== 'ALL' || filterFormValues.isConfirm !== 'ALL';
  const isPickupMethodFilterActive = filterFormValues.pickupMethod !== 'ALL';
  const isSenderFilterActive = Boolean(
    filterFormValues.senderKeyword.trim() || filterFormValues.senderPhone.trim()
  );
  const isReceiverFilterActive = Boolean(
    filterFormValues.receiverKeyword.trim() ||
      filterFormValues.receiverPhone.trim()
  );
  const isPostOfficeFilterActive = Boolean(
    filterFormValues.originPostOfficeCode.trim() ||
      filterFormValues.destinationPostOfficeCode.trim()
  );
  const isPickupWindowFilterActive = Boolean(
    filterFormValues.pickupFrom.trim() || filterFormValues.pickupTo.trim()
  );
  const isUpdatedFilterActive = Boolean(
    filterFormValues.updatedFrom.trim() ||
      filterFormValues.updatedTo.trim() ||
      filterFormValues.updatedSortDirection !== 'NONE'
  );
  const hasActiveFilters =
    Boolean(filterFormValues.keyword.trim()) ||
    isOrderFilterActive ||
    isStatusFilterActive ||
    isPickupMethodFilterActive ||
    isSenderFilterActive ||
    isReceiverFilterActive ||
    isPostOfficeFilterActive ||
    isPickupWindowFilterActive ||
    isUpdatedFilterActive;

  const handleTableFilterSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    onApplyFilters();
    setOpenTableFilter(null);
  };

  const toggleUpdatedSort = () => {
    const nextDirection =
      filterFormValues.updatedSortDirection === 'asc' ? 'desc' : 'asc';
    const nextValues: OrderFilterFormState = {
      ...filterFormValues,
      updatedSortDirection: nextDirection,
    };

    onFilterFieldChange('updatedSortDirection', nextDirection);
    onApplyFilters(nextValues);
  };

  const pageStartItem = data
    ? data.totalItems > 0
      ? data.currentPage * pageSize + 1
      : 0
    : 0;
  const pageEndItem = data
    ? Math.min((data.currentPage + 1) * pageSize, data.totalItems)
    : 0;

  return (
    <Card>
      <CardHeader>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
          <div className='flex flex-wrap gap-2'>
            {hasActiveFilters ? (
              <Button
                type='button'
                variant='outline'
                size='sm'
                onClick={onClearFilters}
                disabled={!canViewOrders || isFetching}
              >
                <X className='mr-2 h-4 w-4' />
                Xóa bộ lọc
              </Button>
            ) : null}
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={onRefresh}
              disabled={!canViewOrders || isFetching}
            >
              <RefreshCw
                className={cn('mr-2 h-4 w-4', isFetching && 'animate-spin')}
              />
              Làm mới
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {!canViewOrders ? (
          <p className='text-muted-foreground'>
            Bạn không có quyền truy cập dữ liệu đơn hàng.
          </p>
        ) : isLoading ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải đơn hàng...
          </div>
        ) : data ? (
          <div className='space-y-4'>
            <Table>
              <TableHeader>
                <TableRow>
                  <FilterableHeader
                    label='Đơn hàng'
                    filterKey='order'
                    openFilter={openTableFilter}
                    isActive={isOrderFilterActive}
                    title='Tìm đơn hàng'
                    disabled={!canViewOrders}
                    widthClass='min-w-[180px]'
                    sortButton={
                      <Button
                        type='button'
                        variant={
                          filterFormValues.updatedSortDirection !== 'NONE'
                            ? 'outline'
                            : 'ghost'
                        }
                        size='icon'
                        className='size-7'
                        title={
                          filterFormValues.updatedSortDirection === 'asc'
                            ? 'Sắp xếp cập nhật giảm dần'
                            : 'Sắp xếp cập nhật tăng dần'
                        }
                        aria-label='Sắp xếp cập nhật'
                        disabled={!canViewOrders || isFetching}
                        onClick={toggleUpdatedSort}
                      >
                        {filterFormValues.updatedSortDirection === 'asc' ? (
                          <ArrowUpAZ className='h-4 w-4' />
                        ) : (
                          <ArrowDownAZ className='h-4 w-4' />
                        )}
                      </Button>
                    }
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <Input
                        ref={orderFilterInputRef}
                        className='h-9 bg-background'
                        value={filterFormValues.orderCode}
                        onChange={(event) =>
                          onFilterFieldChange('orderCode', event.target.value)
                        }
                        placeholder='Mã đơn hàng'
                        disabled={isFetching}
                      />
                      <Input
                        className='h-9 bg-background'
                        value={filterFormValues.customerOrderCode}
                        onChange={(event) =>
                          onFilterFieldChange(
                            'customerOrderCode',
                            event.target.value
                          )
                        }
                        placeholder='Mã khách hàng'
                        disabled={isFetching}
                      />
                      <div className='flex justify-end gap-2'>
                        {isOrderFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() => {
                              onFilterFieldChange('orderCode', '');
                              onFilterFieldChange('customerOrderCode', '');
                            }}
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Trạng thái'
                    filterKey='status'
                    openFilter={openTableFilter}
                    isActive={isStatusFilterActive}
                    title='Lọc trạng thái'
                    disabled={!canViewOrders}
                    widthClass='min-w-[200px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <TmsCombobox
                        id='order-table-filter-status'
                        value={filterFormValues.status}
                        onValueChange={(value) =>
                          onFilterFieldChange(
                            'status',
                            value as OrderFilterFormState['status']
                          )
                        }
                        options={statusFilterOptions}
                        placeholder='Tất cả trạng thái'
                        emptyText='Không tìm thấy trạng thái'
                        disabled={isFetching}
                      />
                      <TmsCombobox
                        id='order-table-filter-confirm'
                        value={filterFormValues.isConfirm}
                        onValueChange={(value) =>
                          onFilterFieldChange(
                            'isConfirm',
                            value as OrderFilterFormState['isConfirm']
                          )
                        }
                        options={confirmFilterOptions}
                        placeholder='Tất cả xác nhận'
                        emptyText='Không tìm thấy lựa chọn'
                        disabled={isFetching}
                      />
                      <div className='flex justify-end gap-2'>
                        {isStatusFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() => {
                              onFilterFieldChange('status', 'ALL');
                              onFilterFieldChange('isConfirm', 'ALL');
                            }}
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Phương thức lấy hàng'
                    filterKey='pickupMethod'
                    openFilter={openTableFilter}
                    isActive={isPickupMethodFilterActive}
                    title='Lọc phương thức lấy hàng'
                    disabled={!canViewOrders}
                    widthClass='min-w-[180px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <TmsCombobox
                        id='order-table-filter-pickup-method'
                        value={filterFormValues.pickupMethod}
                        onValueChange={(value) =>
                          onFilterFieldChange(
                            'pickupMethod',
                            value as OrderFilterFormState['pickupMethod']
                          )
                        }
                        options={ORDER_PICKUP_METHOD_FILTER_OPTIONS}
                        placeholder='Tất cả phương thức'
                        emptyText='Không tìm thấy phương thức'
                        disabled={isFetching}
                      />
                      <div className='flex justify-end gap-2'>
                        {isPickupMethodFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() =>
                              onFilterFieldChange('pickupMethod', 'ALL')
                            }
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Người gửi'
                    filterKey='sender'
                    openFilter={openTableFilter}
                    isActive={isSenderFilterActive}
                    title='Tìm người gửi'
                    disabled={!canViewOrders}
                    widthClass='min-w-[260px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <Input
                        ref={senderFilterInputRef}
                        className='h-9 bg-background'
                        value={filterFormValues.senderKeyword}
                        onChange={(event) =>
                          onFilterFieldChange(
                            'senderKeyword',
                            event.target.value
                          )
                        }
                        placeholder='Tên, số điện thoại, địa chỉ'
                        disabled={isFetching}
                      />
                      <Input
                        className='h-9 bg-background'
                        value={filterFormValues.senderPhone}
                        onChange={(event) =>
                          onFilterFieldChange('senderPhone', event.target.value)
                        }
                        placeholder='Số điện thoại'
                        disabled={isFetching}
                      />
                      <div className='flex justify-end gap-2'>
                        {isSenderFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() => {
                              onFilterFieldChange('senderKeyword', '');
                              onFilterFieldChange('senderPhone', '');
                            }}
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Người nhận'
                    filterKey='receiver'
                    openFilter={openTableFilter}
                    isActive={isReceiverFilterActive}
                    title='Tìm người nhận'
                    disabled={!canViewOrders}
                    widthClass='min-w-[260px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <Input
                        ref={receiverFilterInputRef}
                        className='h-9 bg-background'
                        value={filterFormValues.receiverKeyword}
                        onChange={(event) =>
                          onFilterFieldChange(
                            'receiverKeyword',
                            event.target.value
                          )
                        }
                        placeholder='Tên, số điện thoại, địa chỉ'
                        disabled={isFetching}
                      />
                      <Input
                        className='h-9 bg-background'
                        value={filterFormValues.receiverPhone}
                        onChange={(event) =>
                          onFilterFieldChange(
                            'receiverPhone',
                            event.target.value
                          )
                        }
                        placeholder='Số điện thoại'
                        disabled={isFetching}
                      />
                      <div className='flex justify-end gap-2'>
                        {isReceiverFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() => {
                              onFilterFieldChange('receiverKeyword', '');
                              onFilterFieldChange('receiverPhone', '');
                            }}
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Bưu cục'
                    filterKey='postOffice'
                    openFilter={openTableFilter}
                    isActive={isPostOfficeFilterActive}
                    title='Lọc bưu cục'
                    disabled={!canViewOrders || isLoadingPostOffices}
                    widthClass='min-w-[160px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <TmsCombobox
                        id='order-table-filter-origin-po'
                        value={
                          filterFormValues.originPostOfficeCode ||
                          ALL_POST_OFFICES_VALUE
                        }
                        onValueChange={(value) =>
                          onFilterFieldChange(
                            'originPostOfficeCode',
                            value === ALL_POST_OFFICES_VALUE ? '' : value
                          )
                        }
                        options={postOfficeFilterOptions}
                        placeholder='Bưu cục gốc'
                        emptyText='Không tìm thấy bưu cục'
                        disabled={isFetching || isLoadingPostOffices}
                        loading={isLoadingPostOffices}
                      />
                      <TmsCombobox
                        id='order-table-filter-destination-po'
                        value={
                          filterFormValues.destinationPostOfficeCode ||
                          ALL_POST_OFFICES_VALUE
                        }
                        onValueChange={(value) =>
                          onFilterFieldChange(
                            'destinationPostOfficeCode',
                            value === ALL_POST_OFFICES_VALUE ? '' : value
                          )
                        }
                        options={postOfficeFilterOptions}
                        placeholder='Bưu cục đích'
                        emptyText='Không tìm thấy bưu cục'
                        disabled={isFetching || isLoadingPostOffices}
                        loading={isLoadingPostOffices}
                      />
                      <div className='flex justify-end gap-2'>
                        {isPostOfficeFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() => {
                              onFilterFieldChange('originPostOfficeCode', '');
                              onFilterFieldChange(
                                'destinationPostOfficeCode',
                                ''
                              );
                            }}
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Khung giờ lấy hàng'
                    filterKey='pickupWindow'
                    openFilter={openTableFilter}
                    isActive={isPickupWindowFilterActive}
                    title='Lọc khung giờ lấy hàng'
                    disabled={!canViewOrders}
                    widthClass='min-w-[220px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <Input
                        type='datetime-local'
                        className='h-9 bg-background'
                        value={filterFormValues.pickupFrom}
                        onChange={(event) =>
                          onFilterFieldChange('pickupFrom', event.target.value)
                        }
                        disabled={isFetching}
                      />
                      <Input
                        type='datetime-local'
                        className='h-9 bg-background'
                        value={filterFormValues.pickupTo}
                        onChange={(event) =>
                          onFilterFieldChange('pickupTo', event.target.value)
                        }
                        disabled={isFetching}
                      />
                      <div className='flex justify-end gap-2'>
                        {isPickupWindowFilterActive ? (
                          <Button
                            type='button'
                            variant='ghost'
                            size='sm'
                            disabled={isFetching}
                            onClick={() => {
                              onFilterFieldChange('pickupFrom', '');
                              onFilterFieldChange('pickupTo', '');
                            }}
                          >
                            Xóa
                          </Button>
                        ) : null}
                        <Button
                          type='submit'
                          variant='outline'
                          size='sm'
                          disabled={isFetching}
                        >
                          Tìm kiếm
                        </Button>
                      </div>
                    </form>
                  </FilterableHeader>

                  <FilterableHeader
                    label='Cập nhật'
                    filterKey='updated'
                    openFilter={openTableFilter}
                    isActive={isUpdatedFilterActive}
                    title='Lọc và sắp xếp cập nhật'
                    disabled={!canViewOrders}
                    widthClass='min-w-[180px]'
                    onOpenFilterChange={setOpenTableFilter}
                  >
                    <form
                      className='space-y-3'
                      onSubmit={handleTableFilterSubmit}
                    >
                      <Input
                        type='datetime-local'
                        className='h-9 bg-background'
                        value={filterFormValues.updatedFrom}
                        onChange={(event) =>
                          onFilterFieldChange('updatedFrom', event.target.value)
                        }
                        disabled={isFetching}
                      />
                      <Input
                        type='datetime-local'
                        className='h-9 bg-background'
                        value={filterFormValues.updatedTo}
                        onChange={(event) =>
                          onFilterFieldChange('updatedTo', event.target.value)
                        }
                        disabled={isFetching}
                      />
                      <TmsCombobox
                        id='order-table-filter-updated-sort'
                        value={filterFormValues.updatedSortDirection}
                        onValueChange={(value) =>
                          onFilterFieldChange(
                            'updatedSortDirection',
                            value as OrderFilterFormState['updatedSortDirection']
                          )
                        }
                        options={updatedSortOptions}
                        placeholder='Sắp xếp cập nhật'
                        emptyText='Không tìm thấy lựa chọn'
                        disabled={isFetching}
                      />
                      <div className='flex justify-between gap-2'>
                        <div className='flex items-center gap-1 text-xs text-muted-foreground'>
                          {filterFormValues.updatedSortDirection === 'asc' ? (
                            <ArrowUpAZ className='h-3.5 w-3.5' />
                          ) : filterFormValues.updatedSortDirection ===
                            'desc' ? (
                            <ArrowDownAZ className='h-3.5 w-3.5' />
                          ) : null}
                        </div>
                        <div className='flex justify-end gap-2'>
                          {isUpdatedFilterActive ? (
                            <Button
                              type='button'
                              variant='ghost'
                              size='sm'
                              disabled={isFetching}
                              onClick={() => {
                                onFilterFieldChange('updatedFrom', '');
                                onFilterFieldChange('updatedTo', '');
                                onFilterFieldChange(
                                  'updatedSortDirection',
                                  'NONE'
                                );
                              }}
                            >
                              Xóa
                            </Button>
                          ) : null}
                          <Button
                            type='submit'
                            variant='outline'
                            size='sm'
                            disabled={isFetching}
                          >
                            Tìm kiếm
                          </Button>
                        </div>
                      </div>
                    </form>
                  </FilterableHeader>

                  <TableHead className='sticky right-0 z-20 border-l bg-card text-right'>
                    Thao tác
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.items.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={9}
                      className='h-24 text-center text-muted-foreground'
                    >
                      Không tìm thấy đơn hàng nào.
                    </TableCell>
                  </TableRow>
                ) : null}
                {data.items.map((order) => {
                  const isLoadingDetail = loadingOrderActionId === order.id;
                  const isConfirming = confirmingOrderId === order.id;
                  const isLoadingSuggestions =
                    loadingDropOffSuggestionOrderId === order.id;
                  const canUpdateDraft = canMutateOrders && isDraftOrder(order);
                  const dropOffOrder = isDropOffOrder(order);
                  const showDropOffSuggestions =
                    canMutateOrders &&
                    dropOffOrder &&
                    isConfirmableStatus(order.status) &&
                    !order.isConfirm;
                  const showCourierConfirm =
                    canMutateOrders &&
                    !dropOffOrder &&
                    isConfirmableStatus(order.status) &&
                    !order.isConfirm;
                  const showManagerDropOffConfirm =
                    canConfirmDropOffAtPostOffice &&
                    dropOffOrder &&
                    isConfirmableStatus(order.status) &&
                    !order.isConfirm;

                  return (
                    <TableRow key={order.id} className='group'>
                      <TableCell className='whitespace-normal'>
                        <p className='font-medium'>{order.orderCode}</p>
                        <p className='text-xs text-muted-foreground'>
                          Mã khách hàng: {order.customerOrderCode || '--'}
                        </p>
                      </TableCell>
                      <TableCell className='whitespace-normal'>
                        <div className='flex flex-wrap gap-1.5'>
                          <Badge variant={getStatusBadgeVariant(order.status)}>
                            {formatStatusLabel(order.status)}
                          </Badge>
                          <Badge variant='outline'>
                            {order.isConfirm ? 'Đã xác nhận' : 'Chờ xác nhận'}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {formatPickupMethodLabel(order.pickupMethod)}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {buildOrderAddressLabel(
                          order.senderName,
                          order.senderPhone,
                          order.senderAddressDetail
                        )}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {buildOrderAddressLabel(
                          order.receiverName,
                          order.receiverPhone,
                          order.receiverAddressDetail
                        )}
                      </TableCell>
                      <TableCell className='text-xs text-muted-foreground'>
                        {buildPostOfficeAssignmentLabel(order)}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        {formatDateTime(order.pickupTimeStart)} -{' '}
                        {formatDateTime(order.pickupTimeEnd)}
                      </TableCell>
                      <TableCell className='whitespace-normal text-xs text-muted-foreground'>
                        <p>{formatDateTime(order.updatedAt)}</p>
                      </TableCell>
                      <TableCell className='sticky right-0 z-10 border-l bg-background text-right group-hover:bg-muted/50'>
                        <div className='flex items-center justify-end gap-1'>
                          <Button
                            size='icon'
                            variant='outline'
                            title='Xem chi tiết'
                            onClick={() => onViewDetail(order.id)}
                            disabled={isLoadingDetail}
                            className='h-8 w-8'
                          >
                            {isLoadingDetail ? (
                              <Loader2 className='h-3.5 w-3.5 animate-spin' />
                            ) : (
                              <Eye className='h-3.5 w-3.5' />
                            )}
                            <span className='sr-only'>Xem chi tiết</span>
                          </Button>

                          {canUpdateDraft ? (
                            <>
                              <Button
                                size='icon'
                                variant='outline'
                                title='Sửa đơn hàng'
                                onClick={() => onEdit(order)}
                                disabled={isLoadingDetail}
                                className='h-8 w-8'
                              >
                                <Pencil className='h-3.5 w-3.5' />
                                <span className='sr-only'>Sửa đơn hàng</span>
                              </Button>
                              <Button
                                size='icon'
                                variant='outline'
                                title='Hủy đơn hàng'
                                onClick={() => onRequestCancel(order)}
                                className='h-8 w-8'
                              >
                                <XCircle className='h-3.5 w-3.5' />
                                <span className='sr-only'>Hủy đơn hàng</span>
                              </Button>
                              <Button
                                size='icon'
                                variant='destructive'
                                title='Xóa đơn hàng'
                                onClick={() => onRequestDelete(order)}
                                className='h-8 w-8'
                              >
                                <Trash2 className='h-3.5 w-3.5' />
                                <span className='sr-only'>Xóa đơn hàng</span>
                              </Button>
                            </>
                          ) : null}

                          {showCourierConfirm ? (
                            <Button
                              size='icon'
                              variant='outline'
                              title='Xác nhận đơn hàng'
                              onClick={() => onConfirm(order)}
                              disabled={isConfirming}
                              className='h-8 w-8'
                            >
                              {isConfirming ? (
                                <Loader2 className='h-3.5 w-3.5 animate-spin' />
                              ) : (
                                <CheckCircle2 className='h-3.5 w-3.5' />
                              )}
                              <span className='sr-only'>Xác nhận đơn hàng</span>
                            </Button>
                          ) : null}

                          {showDropOffSuggestions ? (
                            <Button
                              size='icon'
                              variant='outline'
                              title='Xem bưu cục gợi ý'
                              onClick={() => onOpenDropOffSuggestions(order)}
                              disabled={isLoadingSuggestions}
                              className='h-8 w-8'
                            >
                              {isLoadingSuggestions ? (
                                <Loader2 className='h-3.5 w-3.5 animate-spin' />
                              ) : (
                                <Building2 className='h-3.5 w-3.5' />
                              )}
                              <span className='sr-only'>Xem bưu cục gợi ý</span>
                            </Button>
                          ) : null}

                          {showManagerDropOffConfirm ? (
                            <Button
                              size='icon'
                              variant='outline'
                              title='Xác nhận gửi tại bưu cục'
                              onClick={() => onOpenManagerDropOffConfirm(order)}
                              className='h-8 w-8'
                            >
                              <CheckCircle2 className='h-3.5 w-3.5' />
                              <span className='sr-only'>
                                Xác nhận gửi tại bưu cục
                              </span>
                            </Button>
                          ) : null}
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>

            <div className='flex flex-col gap-3 pt-2 md:flex-row md:items-center md:justify-between'>
              <div className='flex flex-wrap items-center gap-2 text-sm text-muted-foreground'>
                <span>Số bản ghi/trang</span>
                <Select
                  value={String(pageSize)}
                  onValueChange={(value) => onPageSizeChange(Number(value))}
                  disabled={isFetching}
                >
                  <SelectTrigger className='h-8 w-[88px]'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {PAGE_SIZE_OPTIONS.map((size) => (
                      <SelectItem key={size} value={String(size)}>
                        {size}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <span>
                  {data.totalItems > 0
                    ? `Hiển thị ${pageStartItem} đến ${pageEndItem} / ${data.totalItems} bản ghi`
                    : 'Không có bản ghi nào'}
                </span>
              </div>

              <div className='flex items-center justify-end gap-3'>
                <span className='text-sm text-muted-foreground'>
                  Trang {data.currentPage + 1} / {Math.max(data.totalPages, 1)}
                </span>
                <div className='flex items-center gap-2'>
                  <Button
                    variant='outline'
                    onClick={onPreviousPage}
                    disabled={!data.hasPrevious || isFetching}
                  >
                    Trang trước
                  </Button>
                  <Button
                    variant='outline'
                    onClick={onNextPage}
                    disabled={!data.hasNext || isFetching}
                  >
                    Trang sau
                  </Button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <p className='text-muted-foreground'>Không tìm thấy đơn hàng nào.</p>
        )}
      </CardContent>
    </Card>
  );
};
