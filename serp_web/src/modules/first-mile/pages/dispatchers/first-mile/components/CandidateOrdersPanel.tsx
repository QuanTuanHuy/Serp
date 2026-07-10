/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher candidate orders table
 */

import React from 'react';
import {
  ChevronLeft,
  ChevronRight,
  Loader2,
  PackageSearch,
  Search,
  X,
} from 'lucide-react';
import {
  Badge,
  Button,
  Checkbox,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import type { FirstMileOrderDetail } from '../../../../types';
import { formatOrderStatusLabel } from '../../../../utils/orderStatusLabels';
import {
  countPickupBacklogOrders,
  formatPickupBacklogDuration,
  formatPickupWindow,
  getPickupBacklogMinutes,
  isPickupBacklogOrder,
  sortOrdersByBacklogPriority,
} from '../dispatchOrderBacklog';
import type { CandidateOrdersPanelProps } from './types';

type CandidateOrderFilterKey =
  | 'order'
  | 'status'
  | 'sender'
  | 'receiver'
  | 'pickupWindow'
  | 'backlog';

type CandidateOrderFilters = Record<CandidateOrderFilterKey, string>;

interface SearchableHeaderProps {
  filterKey: CandidateOrderFilterKey;
  label: string;
  placeholder: string;
  value: string;
  openFilter: CandidateOrderFilterKey | null;
  onOpenFilterChange: (filterKey: CandidateOrderFilterKey | null) => void;
  onChange: (filterKey: CandidateOrderFilterKey, value: string) => void;
  onClear: (filterKey: CandidateOrderFilterKey) => void;
}

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
const DEFAULT_PAGE_SIZE = 10;

const DEFAULT_FILTERS: CandidateOrderFilters = {
  order: '',
  status: '',
  sender: '',
  receiver: '',
  pickupWindow: '',
  backlog: '',
};

const clampPage = (page: number, pageCount: number): number => {
  return Math.min(Math.max(page, 0), Math.max(pageCount - 1, 0));
};

const normalizeSearchValue = (value: string): string => {
  return value.trim().toLocaleLowerCase('vi-VN');
};

const includesFilter = (value: string, filterValue: string): boolean => {
  const normalizedFilter = normalizeSearchValue(filterValue);
  if (!normalizedFilter) {
    return true;
  }

  return normalizeSearchValue(value).includes(normalizedFilter);
};

const getBacklogLabel = (
  order: FirstMileOrderDetail,
  referenceTime?: Date
): string => {
  const isBacklog = isPickupBacklogOrder(order, referenceTime);
  const backlogDuration = formatPickupBacklogDuration(
    getPickupBacklogMinutes(order, referenceTime)
  );

  return isBacklog ? `Quá hạn ${backlogDuration}` : 'Trong hạn';
};

const SearchableHeader: React.FC<SearchableHeaderProps> = ({
  filterKey,
  label,
  placeholder,
  value,
  openFilter,
  onOpenFilterChange,
  onChange,
  onClear,
}) => {
  const isActive = Boolean(value.trim());

  return (
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
            title={`Tìm ${label.toLocaleLowerCase('vi-VN')}`}
            aria-label={`Tìm ${label.toLocaleLowerCase('vi-VN')}`}
          >
            <Search className='h-4 w-4' />
          </Button>
        </PopoverTrigger>
      </div>
      <PopoverContent align='start' sideOffset={8} className='w-72 p-3'>
        <form
          className='flex items-center gap-2'
          onSubmit={(event) => {
            event.preventDefault();
            onOpenFilterChange(null);
          }}
        >
          <Input
            className='h-9 bg-background'
            value={value}
            onChange={(event) => onChange(filterKey, event.target.value)}
            placeholder={placeholder}
          />
          <Button
            type='submit'
            variant='outline'
            size='icon'
            className='size-9 shrink-0'
            title='Tìm kiếm'
            aria-label={`Tìm ${label.toLocaleLowerCase('vi-VN')}`}
          >
            <Search className='h-4 w-4' />
          </Button>
          {isActive ? (
            <Button
              type='button'
              variant='ghost'
              size='icon'
              className='size-9 shrink-0'
              onClick={() => onClear(filterKey)}
              title='Xóa tìm kiếm'
              aria-label={`Xóa tìm ${label.toLocaleLowerCase('vi-VN')}`}
            >
              <X className='h-4 w-4' />
            </Button>
          ) : null}
        </form>
      </PopoverContent>
    </Popover>
  );
};

export const CandidateOrdersPanel: React.FC<CandidateOrdersPanelProps> = ({
  title,
  orders,
  loading = false,
  selectedOrderIds = [],
  onOrderToggle,
  emptyText = 'Không tìm thấy đơn hàng ứng viên.',
  selectionHint,
  referenceTime,
}) => {
  const [pageIndex, setPageIndex] = React.useState(0);
  const [pageSize, setPageSize] = React.useState(DEFAULT_PAGE_SIZE);
  const [openFilter, setOpenFilter] =
    React.useState<CandidateOrderFilterKey | null>(null);
  const [filters, setFilters] =
    React.useState<CandidateOrderFilters>(DEFAULT_FILTERS);

  const sortedOrders = React.useMemo(
    () => sortOrdersByBacklogPriority(orders, referenceTime),
    [orders, referenceTime]
  );
  const filteredOrders = React.useMemo(() => {
    return sortedOrders.filter((order) => {
      const statusText = `${formatOrderStatusLabel(order.status)} ${
        order.isConfirm ? 'Đã xác nhận' : 'Chờ xác nhận'
      }`;
      const senderText = `${order.senderName ?? ''} ${order.senderPhone ?? ''}`;
      const receiverText = `${order.receiverName ?? ''} ${
        order.receiverPhone ?? ''
      }`;
      const orderText = `${order.orderCode ?? ''} ${
        order.customerOrderCode ?? ''
      }`;

      return (
        includesFilter(orderText, filters.order) &&
        includesFilter(statusText, filters.status) &&
        includesFilter(senderText, filters.sender) &&
        includesFilter(receiverText, filters.receiver) &&
        includesFilter(formatPickupWindow(order), filters.pickupWindow) &&
        includesFilter(getBacklogLabel(order, referenceTime), filters.backlog)
      );
    });
  }, [filters, referenceTime, sortedOrders]);
  const backlogCount = React.useMemo(
    () => countPickupBacklogOrders(orders, referenceTime),
    [orders, referenceTime]
  );
  const activeFilterCount = Object.values(filters).filter((value) =>
    value.trim()
  ).length;
  const pageCount = Math.max(1, Math.ceil(filteredOrders.length / pageSize));
  const safePageIndex = clampPage(pageIndex, pageCount);
  const startIndex = safePageIndex * pageSize;
  const visibleOrders = filteredOrders.slice(startIndex, startIndex + pageSize);
  const visibleStart = filteredOrders.length === 0 ? 0 : startIndex + 1;
  const visibleEnd = Math.min(startIndex + pageSize, filteredOrders.length);
  const allVisibleSelected =
    visibleOrders.length > 0 &&
    visibleOrders.every((order) => selectedOrderIds.includes(order.id));

  React.useEffect(() => {
    setPageIndex(0);
  }, [filters, orders, pageSize]);

  React.useEffect(() => {
    setPageIndex((currentPage) => clampPage(currentPage, pageCount));
  }, [pageCount]);

  const handleFilterChange = (
    filterKey: CandidateOrderFilterKey,
    value: string
  ) => {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [filterKey]: value,
    }));
  };

  const handleClearFilter = (filterKey: CandidateOrderFilterKey) => {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [filterKey]: '',
    }));
  };

  const handleToggleVisibleOrders = (checked: boolean) => {
    if (!onOrderToggle) {
      return;
    }

    for (const order of visibleOrders) {
      if (selectedOrderIds.includes(order.id) !== checked) {
        onOrderToggle(order.id, checked);
      }
    }
  };

  const renderSearchableHeader = (
    filterKey: CandidateOrderFilterKey,
    label: string,
    placeholder: string
  ) => (
    <SearchableHeader
      filterKey={filterKey}
      label={label}
      placeholder={placeholder}
      value={filters[filterKey]}
      openFilter={openFilter}
      onOpenFilterChange={setOpenFilter}
      onChange={handleFilterChange}
      onClear={handleClearFilter}
    />
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
            {activeFilterCount > 0 ? (
              <Badge variant='outline'>
                {filteredOrders.length} khớp bộ lọc
              </Badge>
            ) : null}
          </div>
        </div>

        <div className='flex flex-wrap justify-start gap-2 md:justify-end'>
          {onOrderToggle && selectionHint ? (
            <Badge variant='secondary'>{selectionHint}</Badge>
          ) : null}
          {onOrderToggle && selectedOrderIds.length > 0 ? (
            <Badge variant='outline'>{selectedOrderIds.length} đã chọn</Badge>
          ) : null}
        </div>
      </div>

      <div className='mt-4'>
        {loading ? (
          <div className='flex items-center gap-2 text-sm text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải đơn hàng ứng viên...
          </div>
        ) : sortedOrders.length > 0 ? (
          <div className='space-y-3'>
            <div className='rounded-md border'>
              <Table>
                <TableHeader>
                  <TableRow>
                    {onOrderToggle ? (
                      <TableHead className='w-10'>
                        <Checkbox
                          checked={allVisibleSelected}
                          onCheckedChange={(value) =>
                            handleToggleVisibleOrders(Boolean(value))
                          }
                          aria-label='Chọn các đơn đang hiển thị'
                        />
                      </TableHead>
                    ) : null}
                    <TableHead>
                      {renderSearchableHeader(
                        'order',
                        'Mã đơn',
                        'Tìm mã đơn...'
                      )}
                    </TableHead>
                    <TableHead>
                      {renderSearchableHeader(
                        'status',
                        'Trạng thái',
                        'Tìm trạng thái...'
                      )}
                    </TableHead>
                    <TableHead>
                      {renderSearchableHeader(
                        'sender',
                        'Người gửi',
                        'Tìm người gửi...'
                      )}
                    </TableHead>
                    <TableHead>
                      {renderSearchableHeader(
                        'receiver',
                        'Người nhận',
                        'Tìm người nhận...'
                      )}
                    </TableHead>
                    <TableHead>
                      {renderSearchableHeader(
                        'pickupWindow',
                        'Khung giờ lấy',
                        'Tìm khung giờ...'
                      )}
                    </TableHead>
                    <TableHead>
                      {renderSearchableHeader(
                        'backlog',
                        'Quá hạn',
                        'Tìm quá hạn/trong hạn...'
                      )}
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {visibleOrders.length > 0 ? (
                    visibleOrders.map((order) => {
                      const isBacklog = isPickupBacklogOrder(
                        order,
                        referenceTime
                      );
                      const backlogDuration = formatPickupBacklogDuration(
                        getPickupBacklogMinutes(order, referenceTime)
                      );

                      return (
                        <TableRow key={order.id}>
                          {onOrderToggle ? (
                            <TableCell>
                              <Checkbox
                                checked={selectedOrderIds.includes(order.id)}
                                onCheckedChange={(value) =>
                                  onOrderToggle(order.id, Boolean(value))
                                }
                                aria-label={`Chọn đơn hàng ${order.orderCode}`}
                              />
                            </TableCell>
                          ) : null}
                          <TableCell className='min-w-40'>
                            <div className='space-y-1'>
                              <p className='font-medium'>{order.orderCode}</p>
                              <p className='text-xs text-muted-foreground'>
                                {order.customerOrderCode || '--'}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className='flex flex-wrap gap-1'>
                              <Badge variant='outline'>
                                {formatOrderStatusLabel(order.status)}
                              </Badge>
                              <Badge variant='secondary'>
                                {order.isConfirm
                                  ? 'Đã xác nhận'
                                  : 'Chờ xác nhận'}
                              </Badge>
                            </div>
                          </TableCell>
                          <TableCell className='min-w-44'>
                            <div className='space-y-1'>
                              <p>{order.senderName || '--'}</p>
                              <p className='text-xs text-muted-foreground'>
                                {order.senderPhone || '--'}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell className='min-w-44'>
                            <div className='space-y-1'>
                              <p>{order.receiverName || '--'}</p>
                              <p className='text-xs text-muted-foreground'>
                                {order.receiverPhone || '--'}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell className='min-w-48 text-xs text-muted-foreground'>
                            {formatPickupWindow(order)}
                          </TableCell>
                          <TableCell>
                            {isBacklog ? (
                              <Badge variant='destructive'>
                                {backlogDuration || 'Quá hạn'}
                              </Badge>
                            ) : (
                              <Badge variant='outline'>Trong hạn</Badge>
                            )}
                          </TableCell>
                        </TableRow>
                      );
                    })
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={onOrderToggle ? 7 : 6}
                        className='h-20 text-center text-sm text-muted-foreground'
                      >
                        Không có đơn khớp bộ lọc hiện tại.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            <div className='flex flex-col gap-3 text-sm text-muted-foreground lg:flex-row lg:items-center lg:justify-between'>
              <div>
                Hiển thị {visibleStart}-{visibleEnd} / {filteredOrders.length}{' '}
                đơn
              </div>
              <div className='flex flex-wrap items-center gap-2'>
                <span>Số đơn/trang</span>
                <Select
                  value={String(pageSize)}
                  onValueChange={(value) => setPageSize(Number(value))}
                >
                  <SelectTrigger size='sm' className='w-24'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {PAGE_SIZE_OPTIONS.map((option) => (
                      <SelectItem key={option} value={String(option)}>
                        {option}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <div className='flex items-center gap-1'>
                  <Button
                    type='button'
                    variant='outline'
                    size='sm'
                    disabled={safePageIndex === 0}
                    onClick={() =>
                      setPageIndex((currentPage) =>
                        clampPage(currentPage - 1, pageCount)
                      )
                    }
                  >
                    <ChevronLeft className='h-4 w-4' />
                    Trước
                  </Button>
                  <span className='px-2'>
                    Trang {safePageIndex + 1} / {pageCount}
                  </span>
                  <Button
                    type='button'
                    variant='outline'
                    size='sm'
                    disabled={safePageIndex >= pageCount - 1}
                    onClick={() =>
                      setPageIndex((currentPage) =>
                        clampPage(currentPage + 1, pageCount)
                      )
                    }
                  >
                    Sau
                    <ChevronRight className='h-4 w-4' />
                  </Button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <p className='text-sm text-muted-foreground'>{emptyText}</p>
        )}
      </div>
    </section>
  );
};
