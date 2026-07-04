/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office results card
 */

import Image from 'next/image';
import React from 'react';
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components/ui';
import { Eye, ImageUp, Loader2, Pencil, Search, Trash2, X } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type {
  FirstMilePaginatedData,
  PostOffice,
  PostOfficeStatus,
  Province,
  Ward,
} from '../../../../types';
import {
  formatPostOfficeStatusLabel,
  POST_OFFICE_STATUS_OPTIONS,
} from '../postOfficeForm';
import type { PostOfficeFilterFormState } from '../postOfficeFilterModels';

interface PostOfficeResultsCardProps {
  data?: FirstMilePaginatedData<PostOffice>;
  isLoading: boolean;
  isFetching: boolean;
  isTmsAdmin: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  isUploadingImage: boolean;
  filterFormValues: PostOfficeFilterFormState;
  provinceSelectOptions: Province[];
  filterWardOptions: Ward[];
  selectedFilterProvinceCode: string;
  selectedFilterWardCode: string;
  isFetchingWardsForFilter: boolean;
  onViewDetails: (postOffice: PostOffice) => void;
  onEdit: (postOffice: PostOffice) => void;
  onUploadImage: (postOffice: PostOffice, file: File) => void;
  onDelete: (postOffice: PostOffice) => void;
  onFilterFieldChange: <K extends keyof PostOfficeFilterFormState>(
    field: K,
    value: PostOfficeFilterFormState[K]
  ) => void;
  onSearchSubmit: (event: React.FormEvent) => void;
  onClearSearch: () => void;
  pageSize: number;
  onPageSizeChange: (pageSize: number) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  getProvinceLabel: (provinceCode?: string) => string;
  getWardLabel: (provinceCode?: string, wardCode?: string) => string;
  getStatusBadgeVariant: (
    status: PostOfficeStatus
  ) => 'default' | 'secondary' | 'outline' | 'destructive';
}

const ACTION_COLUMN_CLASS =
  'sticky right-0 z-10 w-[188px] min-w-[188px] border-l bg-card';
const ACTION_HEADER_CLASS = `${ACTION_COLUMN_CLASS} z-20`;
const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

export const PostOfficeResultsCard: React.FC<PostOfficeResultsCardProps> = ({
  data,
  isLoading,
  isFetching,
  isTmsAdmin,
  isSaving,
  isDeleting,
  isUploadingImage,
  filterFormValues,
  provinceSelectOptions,
  filterWardOptions,
  selectedFilterProvinceCode,
  selectedFilterWardCode,
  isFetchingWardsForFilter,
  onViewDetails,
  onEdit,
  onUploadImage,
  onDelete,
  onFilterFieldChange,
  onSearchSubmit,
  onClearSearch,
  pageSize,
  onPageSizeChange,
  onPreviousPage,
  onNextPage,
  getProvinceLabel,
  getWardLabel,
  getStatusBadgeVariant,
}) => {
  const [openFilter, setOpenFilter] = React.useState<
    | 'code'
    | 'name'
    | 'status'
    | 'location'
    | 'pickupLoad'
    | 'deliveryLoad'
    | null
  >(null);
  const codeInputRef = React.useRef<HTMLInputElement>(null);
  const nameInputRef = React.useRef<HTMLInputElement>(null);

  React.useEffect(() => {
    if (openFilter !== 'code' && openFilter !== 'name') {
      return;
    }

    window.setTimeout(() => {
      if (openFilter === 'code') {
        codeInputRef.current?.focus();
        codeInputRef.current?.select();
      } else {
        nameInputRef.current?.focus();
        nameInputRef.current?.select();
      }
    }, 0);
  }, [openFilter]);

  const handleSearchSubmit = (event: React.FormEvent) => {
    onSearchSubmit(event);
    setOpenFilter(null);
  };

  const statusOptions = [
    { value: 'ALL', label: 'Tất cả trạng thái' },
    ...POST_OFFICE_STATUS_OPTIONS,
  ];
  const provinceOptions = [
    { value: 'ALL', label: 'Tất cả tỉnh/thành phố' },
    ...provinceSelectOptions.flatMap((province) =>
      province.provinceCode
        ? [
            {
              value: province.provinceCode,
              label: `${province.name} (${province.provinceCode})`,
            },
          ]
        : []
    ),
  ];
  const wardOptions = [
    { value: 'ALL', label: 'Tất cả phường/xã' },
    ...filterWardOptions.flatMap((ward) =>
      ward.wardCode
        ? [
            {
              value: ward.wardCode,
              label: `${ward.name} (${ward.wardCode})`,
            },
          ]
        : []
    ),
  ];
  const isCodeFilterActive = Boolean(filterFormValues.code.trim());
  const isNameFilterActive = Boolean(filterFormValues.name.trim());
  const isStatusFilterActive = filterFormValues.status !== 'ALL';
  const isLocationFilterActive =
    Boolean(selectedFilterProvinceCode) || Boolean(selectedFilterWardCode);
  const isPickupLoadFilterActive =
    Boolean(filterFormValues.minCurrentLoad.trim()) ||
    Boolean(filterFormValues.maxCurrentLoad.trim());
  const isDeliveryLoadFilterActive =
    Boolean(filterFormValues.minCurrentDeliveryLoad.trim()) ||
    Boolean(filterFormValues.maxCurrentDeliveryLoad.trim());

  return (
    <Card>
      <CardHeader>
        <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
      </CardHeader>
      <CardContent className='space-y-4'>
        {isLoading ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải bưu cục...
          </div>
        ) : data ? (
          <>
            <div className='rounded-md border'>
              <Table className='min-w-[1460px]'>
                <TableHeader>
                  <TableRow>
                    <TableHead className='w-[96px]'>Ảnh</TableHead>
                    <TableHead className='w-[170px]'>
                      <Popover
                        open={openFilter === 'code'}
                        onOpenChange={(open) =>
                          setOpenFilter(open ? 'code' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Mã bưu cục</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={isCodeFilterActive ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              title='Tìm mã bưu cục'
                              aria-label='Tìm mã bưu cục'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-72 p-3'
                        >
                          <form
                            className='flex items-center gap-2'
                            onSubmit={handleSearchSubmit}
                          >
                            <Input
                              ref={codeInputRef}
                              className='h-9 bg-background'
                              value={filterFormValues.code}
                              onChange={(event) =>
                                onFilterFieldChange('code', event.target.value)
                              }
                              placeholder='Tìm mã bưu cục...'
                              disabled={isFetching}
                            />
                            <Button
                              type='submit'
                              variant='outline'
                              size='icon'
                              className='size-9 shrink-0'
                              disabled={isFetching}
                              title='Tìm kiếm'
                              aria-label='Tìm mã bưu cục'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                            {isCodeFilterActive ? (
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                onClick={() => {
                                  onFilterFieldChange('code', '');
                                }}
                                title='Xóa tìm kiếm'
                                aria-label='Xóa tìm kiếm mã bưu cục'
                              >
                                <X className='h-4 w-4' />
                              </Button>
                            ) : null}
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[220px]'>
                      <Popover
                        open={openFilter === 'name'}
                        onOpenChange={(open) =>
                          setOpenFilter(open ? 'name' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Tên bưu cục</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={isNameFilterActive ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              title='Tìm tên bưu cục'
                              aria-label='Tìm tên bưu cục'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-72 p-3'
                        >
                          <form
                            className='flex items-center gap-2'
                            onSubmit={handleSearchSubmit}
                          >
                            <Input
                              ref={nameInputRef}
                              className='h-9 bg-background'
                              value={filterFormValues.name}
                              onChange={(event) =>
                                onFilterFieldChange('name', event.target.value)
                              }
                              placeholder='Tìm tên bưu cục...'
                              disabled={isFetching}
                            />
                            <Button
                              type='submit'
                              variant='outline'
                              size='icon'
                              className='size-9 shrink-0'
                              disabled={isFetching}
                              title='Tìm kiếm'
                              aria-label='Tìm tên bưu cục'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                            {isNameFilterActive ? (
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                onClick={() => {
                                  onFilterFieldChange('name', '');
                                }}
                                title='Xóa tìm kiếm'
                                aria-label='Xóa tìm kiếm tên bưu cục'
                              >
                                <X className='h-4 w-4' />
                              </Button>
                            ) : null}
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[150px]'>
                      <Popover
                        open={openFilter === 'status'}
                        onOpenChange={(open) =>
                          setOpenFilter(open ? 'status' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Trạng thái</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isStatusFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc trạng thái'
                              aria-label='Lọc trạng thái'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-60 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleSearchSubmit}
                          >
                            <Select
                              value={filterFormValues.status}
                              onValueChange={(value) =>
                                onFilterFieldChange(
                                  'status',
                                  value as PostOfficeFilterFormState['status']
                                )
                              }
                              disabled={isFetching}
                            >
                              <SelectTrigger className='h-9'>
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent>
                                {statusOptions.map((option) => (
                                  <SelectItem
                                    key={option.value}
                                    value={option.value}
                                  >
                                    {option.label}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            <div className='flex justify-end gap-2'>
                              {isStatusFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() =>
                                    onFilterFieldChange('status', 'ALL')
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
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[240px]'>
                      <Popover
                        open={openFilter === 'location'}
                        onOpenChange={(open) =>
                          setOpenFilter(open ? 'location' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Tỉnh/Phường xã</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isLocationFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc tỉnh/phường xã'
                              aria-label='Lọc tỉnh/phường xã'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-80 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleSearchSubmit}
                          >
                            <TmsCombobox
                              id='post-office-table-filter-province'
                              value={selectedFilterProvinceCode || 'ALL'}
                              onValueChange={(value) => {
                                const nextProvinceCode =
                                  value === 'ALL' ? '' : value;
                                onFilterFieldChange(
                                  'provinceCode',
                                  nextProvinceCode
                                );
                                onFilterFieldChange('wardCode', '');
                              }}
                              options={provinceOptions}
                              placeholder='Tất cả tỉnh/thành phố'
                              emptyText='Không tìm thấy tỉnh/thành phố'
                              disabled={isFetching}
                            />
                            <TmsCombobox
                              id='post-office-table-filter-ward'
                              value={selectedFilterWardCode || 'ALL'}
                              onValueChange={(value) =>
                                onFilterFieldChange(
                                  'wardCode',
                                  value === 'ALL' ? '' : value
                                )
                              }
                              options={wardOptions}
                              placeholder={
                                selectedFilterProvinceCode
                                  ? 'Tất cả phường/xã'
                                  : 'Chọn tỉnh/thành phố trước'
                              }
                              emptyText={
                                isFetchingWardsForFilter
                                  ? 'Đang tải phường/xã...'
                                  : 'Không có phường/xã phù hợp'
                              }
                              disabled={
                                isFetching || !selectedFilterProvinceCode
                              }
                              loading={isFetchingWardsForFilter}
                            />
                            <div className='flex justify-end gap-2'>
                              {isLocationFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    onFilterFieldChange('provinceCode', '');
                                    onFilterFieldChange('wardCode', '');
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
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='min-w-[360px]'>Địa chỉ</TableHead>
                    <TableHead className='w-[170px]'>
                      <Popover
                        open={openFilter === 'pickupLoad'}
                        onOpenChange={(open) =>
                          setOpenFilter(open ? 'pickupLoad' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Tải lấy hàng</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isPickupLoadFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc tải lấy hàng'
                              aria-label='Lọc tải lấy hàng'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-72 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleSearchSubmit}
                          >
                            <div className='grid grid-cols-2 gap-2'>
                              <Input
                                type='number'
                                min={0}
                                step={1}
                                className='h-9 bg-background'
                                value={filterFormValues.minCurrentLoad}
                                onChange={(event) =>
                                  onFilterFieldChange(
                                    'minCurrentLoad',
                                    event.target.value
                                  )
                                }
                                placeholder='Từ'
                                disabled={isFetching}
                              />
                              <Input
                                type='number'
                                min={0}
                                step={1}
                                className='h-9 bg-background'
                                value={filterFormValues.maxCurrentLoad}
                                onChange={(event) =>
                                  onFilterFieldChange(
                                    'maxCurrentLoad',
                                    event.target.value
                                  )
                                }
                                placeholder='Đến'
                                disabled={isFetching}
                              />
                            </div>
                            <div className='flex justify-end gap-2'>
                              {isPickupLoadFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    onFilterFieldChange('minCurrentLoad', '');
                                    onFilterFieldChange('maxCurrentLoad', '');
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
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[170px]'>
                      <Popover
                        open={openFilter === 'deliveryLoad'}
                        onOpenChange={(open) =>
                          setOpenFilter(open ? 'deliveryLoad' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Tải giao hàng</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isDeliveryLoadFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc tải giao hàng'
                              aria-label='Lọc tải giao hàng'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-72 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleSearchSubmit}
                          >
                            <div className='grid grid-cols-2 gap-2'>
                              <Input
                                type='number'
                                min={0}
                                step={1}
                                className='h-9 bg-background'
                                value={filterFormValues.minCurrentDeliveryLoad}
                                onChange={(event) =>
                                  onFilterFieldChange(
                                    'minCurrentDeliveryLoad',
                                    event.target.value
                                  )
                                }
                                placeholder='Từ'
                                disabled={isFetching}
                              />
                              <Input
                                type='number'
                                min={0}
                                step={1}
                                className='h-9 bg-background'
                                value={filterFormValues.maxCurrentDeliveryLoad}
                                onChange={(event) =>
                                  onFilterFieldChange(
                                    'maxCurrentDeliveryLoad',
                                    event.target.value
                                  )
                                }
                                placeholder='Đến'
                                disabled={isFetching}
                              />
                            </div>
                            <div className='flex justify-end gap-2'>
                              {isDeliveryLoadFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    onFilterFieldChange(
                                      'minCurrentDeliveryLoad',
                                      ''
                                    );
                                    onFilterFieldChange(
                                      'maxCurrentDeliveryLoad',
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
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[180px]'>Tọa độ</TableHead>
                    <TableHead className={ACTION_HEADER_CLASS}>
                      <span className='flex justify-end'>Thao tác</span>
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.items.length > 0 ? (
                    data.items.map((item) => {
                      const imageUrl = item.imageUrl?.trim();
                      const hasCoordinates =
                        item.latitude !== undefined &&
                        item.latitude !== null &&
                        item.longitude !== undefined &&
                        item.longitude !== null;

                      return (
                        <TableRow key={item.id}>
                          <TableCell>
                            <div className='relative h-12 w-16 overflow-hidden rounded-md border bg-muted'>
                              {imageUrl ? (
                                <Image
                                  src={imageUrl}
                                  alt={`Bưu cục ${item.code}`}
                                  fill
                                  unoptimized
                                  sizes='64px'
                                  className='object-cover'
                                />
                              ) : (
                                <div className='flex h-full w-full items-center justify-center text-[10px] text-muted-foreground'>
                                  Chưa có ảnh
                                </div>
                              )}
                            </div>
                          </TableCell>
                          <TableCell className='font-mono text-xs'>
                            {item.code}
                          </TableCell>
                          <TableCell className='font-medium'>
                            {item.name}
                          </TableCell>
                          <TableCell>
                            <Badge variant={getStatusBadgeVariant(item.status)}>
                              {formatPostOfficeStatusLabel(item.status)}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <div className='space-y-1'>
                              <p>{getProvinceLabel(item.provinceCode)}</p>
                              <p className='text-xs text-muted-foreground'>
                                {getWardLabel(item.provinceCode, item.wardCode)}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell className='max-w-[280px] whitespace-normal'>
                            <span className='line-clamp-2'>
                              {item.addressDetail || '--'}
                            </span>
                          </TableCell>
                          <TableCell>
                            {item.currentLoad ?? '--'} /{' '}
                            {item.dailyCapacity ?? '--'}
                          </TableCell>
                          <TableCell>
                            {item.currentDeliveryLoad ?? '--'} /{' '}
                            {item.deliveryCapacity ?? '--'}
                          </TableCell>
                          <TableCell className='font-mono text-xs'>
                            {hasCoordinates
                              ? `${item.latitude}, ${item.longitude}`
                              : '--'}
                          </TableCell>
                          <TableCell className={ACTION_COLUMN_CLASS}>
                            <div className='flex justify-end gap-2'>
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <Button
                                    type='button'
                                    variant='outline'
                                    size='icon'
                                    aria-label={`Xem chi tiết ${item.code}`}
                                    onClick={() => onViewDetails(item)}
                                    disabled={
                                      isSaving || isDeleting || isUploadingImage
                                    }
                                  >
                                    <Eye className='h-4 w-4' />
                                  </Button>
                                </TooltipTrigger>
                                <TooltipContent>Chi tiết</TooltipContent>
                              </Tooltip>

                              {isTmsAdmin ? (
                                <>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        asChild
                                        variant='outline'
                                        size='icon'
                                        disabled={
                                          isSaving ||
                                          isDeleting ||
                                          isUploadingImage
                                        }
                                        aria-label={`Tải ảnh cho ${item.code}`}
                                      >
                                        <label>
                                          <ImageUp className='h-4 w-4' />
                                          <input
                                            type='file'
                                            accept='image/*'
                                            className='sr-only'
                                            disabled={
                                              isSaving ||
                                              isDeleting ||
                                              isUploadingImage
                                            }
                                            onChange={(event) => {
                                              const file =
                                                event.target.files?.[0];
                                              if (file) {
                                                onUploadImage(item, file);
                                              }
                                              event.target.value = '';
                                            }}
                                          />
                                        </label>
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Tải ảnh lên</TooltipContent>
                                  </Tooltip>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='outline'
                                        size='icon'
                                        aria-label={`Sửa ${item.code}`}
                                        onClick={() => onEdit(item)}
                                        disabled={
                                          isSaving ||
                                          isDeleting ||
                                          isUploadingImage
                                        }
                                      >
                                        <Pencil className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Sửa</TooltipContent>
                                  </Tooltip>
                                  <Tooltip>
                                    <TooltipTrigger asChild>
                                      <Button
                                        type='button'
                                        variant='destructive'
                                        size='icon'
                                        aria-label={`Xóa ${item.code}`}
                                        onClick={() => onDelete(item)}
                                        disabled={
                                          isSaving ||
                                          isDeleting ||
                                          isUploadingImage
                                        }
                                      >
                                        <Trash2 className='h-4 w-4' />
                                      </Button>
                                    </TooltipTrigger>
                                    <TooltipContent>Xóa</TooltipContent>
                                  </Tooltip>
                                </>
                              ) : null}
                            </div>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={10}
                        className='h-24 text-center text-muted-foreground'
                      >
                        Không tìm thấy bưu cục.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            <div className='flex flex-col gap-3 pt-2 md:flex-row md:items-center md:justify-between'>
              <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                <span>Số kết quả/trang</span>
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
                    Trước
                  </Button>
                  <Button
                    variant='outline'
                    onClick={onNextPage}
                    disabled={!data.hasNext || isFetching}
                  >
                    Sau
                  </Button>
                </div>
              </div>
            </div>
          </>
        ) : (
          <p className='text-muted-foreground'>Không tìm thấy bưu cục.</p>
        )}
      </CardContent>
    </Card>
  );
};
