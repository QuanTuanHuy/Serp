/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag results table
 */

import React from 'react';
import {
  ArrowDownAZ,
  ArrowUpAZ,
  Eye,
  ListPlus,
  Lock,
  Pencil,
  RotateCcw,
  Search,
  Trash2,
  X,
} from 'lucide-react';

import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Progress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components';

import { TmsCombobox } from '../../../components/TmsCombobox';
import type {
  FirstMilePaginatedData,
  Hub,
  SecondMileBag,
  SecondMileBagDestinationType,
  SecondMileBagListFilters,
  SecondMileBagStatus,
  SecondMileVehicle,
} from '../../../types';
import {
  formatDateTime,
  formatNumber,
  getBagStatusLabel,
  getBagStatusVariant,
  getDestinationLabel,
  getHubLabel,
  getVehicleLabel,
} from '../bagPageModels';

const ALL_VALUE = '__ALL__';

type ComboboxOption = {
  value: string;
  label: string;
};

type BagTableFilterKey =
  | 'bagCode'
  | 'originHub'
  | 'destination'
  | 'vehicle'
  | 'status'
  | 'orders'
  | 'weight'
  | 'volume'
  | 'sealedAt';

interface BagResultsTableProps {
  data?: FirstMilePaginatedData<SecondMileBag>;
  hubs: Hub[];
  vehicles: SecondMileVehicle[];
  filters: SecondMileBagListFilters;
  hubOptions: ComboboxOption[];
  vehicleOptions: ComboboxOption[];
  destinationTypeOptions: ComboboxOption[];
  postOfficeOptions: ComboboxOption[];
  statusOptions: ComboboxOption[];
  isLoadingPostOffices: boolean;
  isFetching?: boolean;
  canManage: boolean;
  canOperate: boolean;
  page: number;
  onPageChange: (page: number) => void;
  onFilterChange: <K extends keyof SecondMileBagListFilters>(
    field: K,
    value: SecondMileBagListFilters[K] | undefined
  ) => void;
  onClearFilters: () => void;
  onView: (bag: SecondMileBag) => void;
  onEdit: (bag: SecondMileBag) => void;
  onDelete: (bag: SecondMileBag) => void;
  onScan: (bag: SecondMileBag) => void;
  onSeal: (bag: SecondMileBag) => void;
  onReopen: (bag: SecondMileBag) => void;
}

export function BagResultsTable({
  data,
  hubs,
  vehicles,
  filters,
  hubOptions,
  vehicleOptions,
  destinationTypeOptions,
  postOfficeOptions,
  statusOptions,
  isLoadingPostOffices,
  isFetching,
  canManage,
  canOperate,
  page,
  onPageChange,
  onFilterChange,
  onClearFilters,
  onView,
  onEdit,
  onDelete,
  onScan,
  onSeal,
  onReopen,
}: BagResultsTableProps) {
  const bags = data?.items ?? [];
  const [openFilter, setOpenFilter] = React.useState<BagTableFilterKey | null>(
    null
  );

  const isBagCodeFilterActive = Boolean(filters.bagCode?.trim());
  const isOriginHubFilterActive = filters.originHubId !== undefined;
  const isDestinationFilterActive = Boolean(
    filters.destinationType ||
      filters.destinationHubId !== undefined ||
      filters.destinationPostOfficeCode?.trim()
  );
  const isVehicleFilterActive = filters.vehicleId !== undefined;
  const isStatusFilterActive = Boolean(filters.status);
  const isOrdersFilterActive =
    filters.minOrders !== undefined || filters.maxOrders !== undefined;
  const isWeightFilterActive =
    filters.minWeight !== undefined || filters.maxWeight !== undefined;
  const isVolumeFilterActive =
    filters.minVolume !== undefined || filters.maxVolume !== undefined;
  const isSealedAtFilterActive = Boolean(
    filters.sealedFrom ||
      filters.sealedTo ||
      filters.sortBy === 'sealed_at' ||
      filters.sortDirection
  );
  const hasActiveFilters =
    isBagCodeFilterActive ||
    isOriginHubFilterActive ||
    isDestinationFilterActive ||
    isVehicleFilterActive ||
    isStatusFilterActive ||
    isOrdersFilterActive ||
    isWeightFilterActive ||
    isVolumeFilterActive ||
    isSealedAtFilterActive;

  const updateNumberFilter = (
    field:
      | 'minOrders'
      | 'maxOrders'
      | 'minWeight'
      | 'maxWeight'
      | 'minVolume'
      | 'maxVolume',
    value: string
  ) => {
    onFilterChange(field, value === '' ? undefined : Number(value));
  };

  const submitFilter = (event: React.FormEvent) => {
    event.preventDefault();
    setOpenFilter(null);
  };

  const toggleSealedAtSort = () => {
    const nextDirection = filters.sortDirection === 'asc' ? 'desc' : 'asc';
    onFilterChange('sortBy', 'sealed_at');
    onFilterChange('sortDirection', nextDirection);
  };

  return (
    <Card>
      <CardHeader>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
          {hasActiveFilters ? (
            <Button
              type='button'
              variant='outline'
              size='sm'
              disabled={isFetching}
              onClick={onClearFilters}
              className='w-full sm:w-auto'
            >
              <X className='h-4 w-4' />
              Xóa bộ lọc
            </Button>
          ) : null}
        </div>
      </CardHeader>
      <CardContent>
        <div className='overflow-x-auto'>
          <Table>
            <TableHeader>
              <TableRow>
                <FilterableHeader
                  label='Mã túi'
                  filterKey='bagCode'
                  openFilter={openFilter}
                  isActive={isBagCodeFilterActive}
                  title='Tìm mã túi'
                  disabled={Boolean(isFetching)}
                  widthClass='min-w-[150px]'
                  onOpenFilterChange={setOpenFilter}
                >
                  <form className='space-y-3' onSubmit={submitFilter}>
                    <Input
                      className='h-9 bg-background'
                      value={filters.bagCode ?? ''}
                      onChange={(event) =>
                        onFilterChange(
                          'bagCode',
                          event.target.value || undefined
                        )
                      }
                      placeholder='BAG-001'
                      disabled={isFetching}
                    />
                    <FilterActions
                      isActive={isBagCodeFilterActive}
                      isFetching={isFetching}
                      onClear={() => onFilterChange('bagCode', undefined)}
                    />
                  </form>
                </FilterableHeader>

                <FilterableHeader
                  label='Hub gốc'
                  filterKey='originHub'
                  openFilter={openFilter}
                  isActive={isOriginHubFilterActive}
                  title='Lọc Hub gốc'
                  disabled={Boolean(isFetching)}
                  widthClass='min-w-[200px]'
                  onOpenFilterChange={setOpenFilter}
                >
                  <form className='space-y-3' onSubmit={submitFilter}>
                    <TmsCombobox
                      id='bag-table-origin-hub-filter'
                      value={
                        filters.originHubId
                          ? String(filters.originHubId)
                          : ALL_VALUE
                      }
                      onValueChange={(value) =>
                        onFilterChange(
                          'originHubId',
                          value === ALL_VALUE ? undefined : Number(value)
                        )
                      }
                      options={hubOptions}
                      placeholder='Tất cả hub'
                      emptyText='Không tìm thấy hub'
                      disabled={isFetching}
                    />
                    <FilterActions
                      isActive={isOriginHubFilterActive}
                      isFetching={isFetching}
                      onClear={() => onFilterChange('originHubId', undefined)}
                    />
                  </form>
                </FilterableHeader>

                <FilterableHeader
                  label='Điểm đến'
                  filterKey='destination'
                  openFilter={openFilter}
                  isActive={isDestinationFilterActive}
                  title='Lọc điểm đến'
                  disabled={Boolean(isFetching)}
                  widthClass='min-w-[220px]'
                  onOpenFilterChange={setOpenFilter}
                >
                  <form className='space-y-3' onSubmit={submitFilter}>
                    <TmsCombobox
                      id='bag-table-destination-type-filter'
                      value={filters.destinationType ?? ALL_VALUE}
                      onValueChange={(value) => {
                        onFilterChange(
                          'destinationType',
                          value === ALL_VALUE
                            ? undefined
                            : (value as SecondMileBagDestinationType)
                        );
                        onFilterChange('destinationHubId', undefined);
                        onFilterChange('destinationPostOfficeCode', undefined);
                      }}
                      options={destinationTypeOptions}
                      placeholder='Tất cả điểm đến'
                      emptyText='Không tìm thấy điểm đến'
                      disabled={isFetching}
                    />
                    {filters.destinationType === 'HUB' ? (
                      <TmsCombobox
                        id='bag-table-destination-hub-filter'
                        value={
                          filters.destinationHubId
                            ? String(filters.destinationHubId)
                            : ALL_VALUE
                        }
                        onValueChange={(value) =>
                          onFilterChange(
                            'destinationHubId',
                            value === ALL_VALUE ? undefined : Number(value)
                          )
                        }
                        options={hubOptions}
                        placeholder='Tất cả hub đích'
                        emptyText='Không tìm thấy hub'
                        disabled={isFetching}
                      />
                    ) : null}
                    {filters.destinationType === 'POST_OFFICE' ? (
                      <TmsCombobox
                        id='bag-table-destination-post-office-filter'
                        value={filters.destinationPostOfficeCode ?? ALL_VALUE}
                        onValueChange={(value) =>
                          onFilterChange(
                            'destinationPostOfficeCode',
                            value === ALL_VALUE ? undefined : value
                          )
                        }
                        options={postOfficeOptions}
                        placeholder='Tất cả bưu cục'
                        emptyText='Không tìm thấy bưu cục'
                        loading={isLoadingPostOffices}
                        disabled={isFetching || isLoadingPostOffices}
                      />
                    ) : null}
                    <FilterActions
                      isActive={isDestinationFilterActive}
                      isFetching={isFetching}
                      onClear={() => {
                        onFilterChange('destinationType', undefined);
                        onFilterChange('destinationHubId', undefined);
                        onFilterChange('destinationPostOfficeCode', undefined);
                      }}
                    />
                  </form>
                </FilterableHeader>

                <FilterableHeader
                  label='Xe'
                  filterKey='vehicle'
                  openFilter={openFilter}
                  isActive={isVehicleFilterActive}
                  title='Lọc xe'
                  disabled={Boolean(isFetching)}
                  widthClass='min-w-[150px]'
                  onOpenFilterChange={setOpenFilter}
                >
                  <form className='space-y-3' onSubmit={submitFilter}>
                    <TmsCombobox
                      id='bag-table-vehicle-filter'
                      value={
                        filters.vehicleId
                          ? String(filters.vehicleId)
                          : ALL_VALUE
                      }
                      onValueChange={(value) =>
                        onFilterChange(
                          'vehicleId',
                          value === ALL_VALUE ? undefined : Number(value)
                        )
                      }
                      options={vehicleOptions}
                      placeholder='Tất cả xe'
                      emptyText='Không tìm thấy xe'
                      disabled={isFetching}
                    />
                    <FilterActions
                      isActive={isVehicleFilterActive}
                      isFetching={isFetching}
                      onClear={() => onFilterChange('vehicleId', undefined)}
                    />
                  </form>
                </FilterableHeader>

                <FilterableHeader
                  label='Trạng thái'
                  filterKey='status'
                  openFilter={openFilter}
                  isActive={isStatusFilterActive}
                  title='Lọc trạng thái'
                  disabled={Boolean(isFetching)}
                  widthClass='min-w-[170px]'
                  onOpenFilterChange={setOpenFilter}
                >
                  <form className='space-y-3' onSubmit={submitFilter}>
                    <TmsCombobox
                      id='bag-table-status-filter'
                      value={filters.status ?? ALL_VALUE}
                      onValueChange={(value) =>
                        onFilterChange(
                          'status',
                          value === ALL_VALUE
                            ? undefined
                            : (value as SecondMileBagStatus)
                        )
                      }
                      options={statusOptions}
                      placeholder='Tất cả trạng thái'
                      emptyText='Không tìm thấy trạng thái'
                      disabled={isFetching}
                    />
                    <FilterActions
                      isActive={isStatusFilterActive}
                      isFetching={isFetching}
                      onClear={() => onFilterChange('status', undefined)}
                    />
                  </form>
                </FilterableHeader>

                <RangeFilterHeader
                  label='Đơn hàng'
                  filterKey='orders'
                  openFilter={openFilter}
                  isActive={isOrdersFilterActive}
                  title='Lọc số đơn hàng'
                  disabled={Boolean(isFetching)}
                  minValue={filters.minOrders}
                  maxValue={filters.maxOrders}
                  minLabel='Từ số đơn'
                  maxLabel='Đến số đơn'
                  onOpenFilterChange={setOpenFilter}
                  onMinChange={(value) =>
                    updateNumberFilter('minOrders', value)
                  }
                  onMaxChange={(value) =>
                    updateNumberFilter('maxOrders', value)
                  }
                  onClear={() => {
                    onFilterChange('minOrders', undefined);
                    onFilterChange('maxOrders', undefined);
                  }}
                  onSubmit={submitFilter}
                />

                <RangeFilterHeader
                  label='Khối lượng'
                  filterKey='weight'
                  openFilter={openFilter}
                  isActive={isWeightFilterActive}
                  title='Lọc khối lượng'
                  disabled={Boolean(isFetching)}
                  minValue={filters.minWeight}
                  maxValue={filters.maxWeight}
                  minLabel='Từ kg'
                  maxLabel='Đến kg'
                  onOpenFilterChange={setOpenFilter}
                  onMinChange={(value) =>
                    updateNumberFilter('minWeight', value)
                  }
                  onMaxChange={(value) =>
                    updateNumberFilter('maxWeight', value)
                  }
                  onClear={() => {
                    onFilterChange('minWeight', undefined);
                    onFilterChange('maxWeight', undefined);
                  }}
                  onSubmit={submitFilter}
                />

                <RangeFilterHeader
                  label='Thể tích'
                  filterKey='volume'
                  openFilter={openFilter}
                  isActive={isVolumeFilterActive}
                  title='Lọc thể tích'
                  disabled={Boolean(isFetching)}
                  minValue={filters.minVolume}
                  maxValue={filters.maxVolume}
                  minLabel='Từ m3'
                  maxLabel='Đến m3'
                  onOpenFilterChange={setOpenFilter}
                  onMinChange={(value) =>
                    updateNumberFilter('minVolume', value)
                  }
                  onMaxChange={(value) =>
                    updateNumberFilter('maxVolume', value)
                  }
                  onClear={() => {
                    onFilterChange('minVolume', undefined);
                    onFilterChange('maxVolume', undefined);
                  }}
                  onSubmit={submitFilter}
                />

                <FilterableHeader
                  label='Niêm phong lúc'
                  filterKey='sealedAt'
                  openFilter={openFilter}
                  isActive={isSealedAtFilterActive}
                  title='Lọc thời gian niêm phong'
                  disabled={Boolean(isFetching)}
                  widthClass='min-w-[190px]'
                  sortButton={
                    <Button
                      type='button'
                      variant={
                        filters.sortBy === 'sealed_at' ? 'outline' : 'ghost'
                      }
                      size='icon'
                      className='size-7'
                      title={
                        filters.sortDirection === 'asc'
                          ? 'Sắp xếp niêm phong giảm dần'
                          : 'Sắp xếp niêm phong tăng dần'
                      }
                      aria-label='Sắp xếp niêm phong'
                      disabled={isFetching}
                      onClick={toggleSealedAtSort}
                    >
                      {filters.sortDirection === 'asc' ? (
                        <ArrowUpAZ className='h-4 w-4' />
                      ) : (
                        <ArrowDownAZ className='h-4 w-4' />
                      )}
                    </Button>
                  }
                  onOpenFilterChange={setOpenFilter}
                >
                  <form className='space-y-3' onSubmit={submitFilter}>
                    <div className='space-y-2'>
                      <Label htmlFor='bag-table-sealed-from'>Từ</Label>
                      <Input
                        id='bag-table-sealed-from'
                        type='datetime-local'
                        className='h-9 bg-background'
                        value={filters.sealedFrom ?? ''}
                        onChange={(event) =>
                          onFilterChange(
                            'sealedFrom',
                            event.target.value || undefined
                          )
                        }
                        disabled={isFetching}
                      />
                    </div>
                    <div className='space-y-2'>
                      <Label htmlFor='bag-table-sealed-to'>Đến</Label>
                      <Input
                        id='bag-table-sealed-to'
                        type='datetime-local'
                        className='h-9 bg-background'
                        value={filters.sealedTo ?? ''}
                        onChange={(event) =>
                          onFilterChange(
                            'sealedTo',
                            event.target.value || undefined
                          )
                        }
                        disabled={isFetching}
                      />
                    </div>
                    <FilterActions
                      isActive={isSealedAtFilterActive}
                      isFetching={isFetching}
                      onClear={() => {
                        onFilterChange('sealedFrom', undefined);
                        onFilterChange('sealedTo', undefined);
                        onFilterChange('sortBy', undefined);
                        onFilterChange('sortDirection', undefined);
                      }}
                    />
                  </form>
                </FilterableHeader>

                <TableHead className='text-right'>Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {bags.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={10}
                    className='h-24 text-center text-muted-foreground'
                  >
                    {isFetching ? 'Đang tải túi...' : 'Không tìm thấy túi nào.'}
                  </TableCell>
                </TableRow>
              ) : (
                bags.map((bag) => (
                  <TableRow key={bag.id}>
                    <TableCell className='font-medium'>
                      {bag.bagCode ?? `Túi #${bag.id}`}
                    </TableCell>
                    <TableCell className='min-w-48 text-xs text-muted-foreground'>
                      {getHubLabel(hubs, bag.originHubId)}
                    </TableCell>
                    <TableCell className='min-w-48 text-xs text-muted-foreground'>
                      {getDestinationLabel(bag, hubs)}
                    </TableCell>
                    <TableCell className='text-xs text-muted-foreground'>
                      {getVehicleLabel(vehicles, bag.vehicleId)}
                    </TableCell>
                    <TableCell>
                      <Badge variant={getBagStatusVariant(bag.status)}>
                        {getBagStatusLabel(bag.status)}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {formatNumber(bag.currentOrders, 0)} /{' '}
                      {formatNumber(bag.maxOrders, 0)}
                    </TableCell>
                    <TableCell className='min-w-36'>
                      <CapacityValue
                        current={bag.currentWeight}
                        max={bag.maxWeight}
                        suffix='kg'
                      />
                    </TableCell>
                    <TableCell className='min-w-36'>
                      <CapacityValue
                        current={bag.currentVolume}
                        max={bag.maxVolume}
                        suffix='m3'
                      />
                    </TableCell>
                    <TableCell className='min-w-36 text-xs text-muted-foreground'>
                      {formatDateTime(bag.sealedAt)}
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-1'>
                        <IconAction
                          label='Xem chi tiết'
                          onClick={() => onView(bag)}
                        >
                          <Eye className='h-4 w-4' />
                        </IconAction>
                        {bag.status === 'CREATED' && canOperate ? (
                          <IconAction
                            label='Thêm đơn hàng'
                            onClick={() => onScan(bag)}
                          >
                            <ListPlus className='h-4 w-4' />
                          </IconAction>
                        ) : null}
                        {bag.status === 'CREATED' && canManage ? (
                          <>
                            <IconAction label='Sửa' onClick={() => onEdit(bag)}>
                              <Pencil className='h-4 w-4' />
                            </IconAction>
                            <IconAction
                              label='Niêm phong túi'
                              onClick={() => onSeal(bag)}
                              disabled={(bag.currentOrders ?? 0) <= 0}
                            >
                              <Lock className='h-4 w-4' />
                            </IconAction>
                            <IconAction
                              label='Xóa'
                              onClick={() => onDelete(bag)}
                              variant='destructive'
                            >
                              <Trash2 className='h-4 w-4' />
                            </IconAction>
                          </>
                        ) : null}
                        {bag.status === 'SEALED' && canManage ? (
                          <IconAction
                            label='Mở lại'
                            onClick={() => onReopen(bag)}
                          >
                            <RotateCcw className='h-4 w-4' />
                          </IconAction>
                        ) : null}
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        <div className='mt-4 flex items-center justify-between text-sm text-muted-foreground'>
          <span>
            Trang {page + 1} / {Math.max(data?.totalPages ?? 1, 1)}
          </span>
          <div className='flex gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={!data?.hasPrevious}
              onClick={() => onPageChange(Math.max(page - 1, 0))}
            >
              Trang trước
            </Button>
            <Button
              variant='outline'
              size='sm'
              disabled={!data?.hasNext}
              onClick={() => onPageChange(page + 1)}
            >
              Trang sau
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

interface FilterableHeaderProps {
  label: string;
  filterKey: BagTableFilterKey;
  openFilter: BagTableFilterKey | null;
  isActive: boolean;
  title: string;
  disabled: boolean;
  widthClass: string;
  children: React.ReactNode;
  sortButton?: React.ReactNode;
  onOpenFilterChange: (filter: BagTableFilterKey | null) => void;
}

function FilterableHeader({
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
}: FilterableHeaderProps) {
  return (
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
}

interface RangeFilterHeaderProps {
  label: string;
  filterKey: BagTableFilterKey;
  openFilter: BagTableFilterKey | null;
  isActive: boolean;
  title: string;
  disabled: boolean;
  minValue?: number;
  maxValue?: number;
  minLabel: string;
  maxLabel: string;
  onOpenFilterChange: (filter: BagTableFilterKey | null) => void;
  onMinChange: (value: string) => void;
  onMaxChange: (value: string) => void;
  onClear: () => void;
  onSubmit: (event: React.FormEvent) => void;
}

function RangeFilterHeader({
  label,
  filterKey,
  openFilter,
  isActive,
  title,
  disabled,
  minValue,
  maxValue,
  minLabel,
  maxLabel,
  onOpenFilterChange,
  onMinChange,
  onMaxChange,
  onClear,
  onSubmit,
}: RangeFilterHeaderProps) {
  return (
    <FilterableHeader
      label={label}
      filterKey={filterKey}
      openFilter={openFilter}
      isActive={isActive}
      title={title}
      disabled={disabled}
      widthClass='min-w-[150px]'
      onOpenFilterChange={onOpenFilterChange}
    >
      <form className='space-y-3' onSubmit={onSubmit}>
        <div className='space-y-2'>
          <Label>{minLabel}</Label>
          <Input
            type='number'
            min={0}
            className='h-9 bg-background'
            value={minValue ?? ''}
            onChange={(event) => onMinChange(event.target.value)}
            disabled={disabled}
          />
        </div>
        <div className='space-y-2'>
          <Label>{maxLabel}</Label>
          <Input
            type='number'
            min={0}
            className='h-9 bg-background'
            value={maxValue ?? ''}
            onChange={(event) => onMaxChange(event.target.value)}
            disabled={disabled}
          />
        </div>
        <FilterActions
          isActive={isActive}
          isFetching={disabled}
          onClear={onClear}
        />
      </form>
    </FilterableHeader>
  );
}

interface FilterActionsProps {
  isActive: boolean;
  isFetching?: boolean;
  onClear: () => void;
}

function FilterActions({ isActive, isFetching, onClear }: FilterActionsProps) {
  return (
    <div className='flex justify-end gap-2'>
      {isActive ? (
        <Button
          type='button'
          variant='ghost'
          size='sm'
          disabled={isFetching}
          onClick={onClear}
        >
          Xóa
        </Button>
      ) : null}
      <Button type='submit' variant='outline' size='sm' disabled={isFetching}>
        Tìm kiếm
      </Button>
    </div>
  );
}

interface IconActionProps {
  label: string;
  onClick: () => void;
  children: React.ReactNode;
  disabled?: boolean;
  variant?: 'ghost' | 'destructive';
}

function IconAction({
  label,
  onClick,
  children,
  disabled,
  variant = 'ghost',
}: IconActionProps) {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          type='button'
          variant={variant}
          size='icon'
          disabled={disabled}
          onClick={onClick}
          aria-label={label}
        >
          {children}
        </Button>
      </TooltipTrigger>
      <TooltipContent>{label}</TooltipContent>
    </Tooltip>
  );
}

interface CapacityValueProps {
  current?: number;
  max?: number;
  suffix: string;
}

function CapacityValue({ current, max, suffix }: CapacityValueProps) {
  const ratio =
    max && max > 0 ? Math.min(((current ?? 0) / max) * 100, 100) : 0;
  return (
    <div className='space-y-1'>
      <div className='text-xs text-muted-foreground'>
        {formatNumber(current)} / {formatNumber(max)} {suffix}
      </div>
      <Progress value={ratio} className='h-1.5' />
    </div>
  );
}
