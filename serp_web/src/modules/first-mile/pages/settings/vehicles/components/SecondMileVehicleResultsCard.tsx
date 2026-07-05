/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle results table
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
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { Eye, ImageUp, Loader2, Pencil, Search, Trash2, X } from 'lucide-react';
import type {
  FirstMilePaginatedData,
  Hub,
  SecondMileVehicle,
  SecondMileVehicleStatus,
  SecondMileVehicleType,
} from '../../../../types';
import {
  buildHubLabel,
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
  getStatusBadgeVariant,
} from '../secondMileVehiclePageModels';

interface SecondMileVehicleResultsCardProps {
  canManage: boolean;
  data?: FirstMilePaginatedData<SecondMileVehicle>;
  driverLabelByStaffId: Record<number, string>;
  hubById: Record<number, Hub>;
  imageRefreshKey?: number;
  isLoading: boolean;
  isFetching: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  isUploadingImage: boolean;
  pageSize: number;
  keywordInput: string;
  vehicleTypeFilter?: SecondMileVehicleType;
  statusFilter?: SecondMileVehicleStatus;
  hubKeywordInput: string;
  driverKeywordInput: string;
  onView: (id: number) => void;
  onEdit: (vehicle: SecondMileVehicle) => void;
  onUploadImage: (vehicle: SecondMileVehicle, file: File) => void;
  onDelete: (vehicle: SecondMileVehicle) => void;
  onKeywordInputChange: (value: string) => void;
  onSearchSubmit: (event: React.FormEvent) => void;
  onClearSearch: () => void;
  onVehicleTypeFilterChange: (value?: SecondMileVehicleType) => void;
  onStatusFilterChange: (value?: SecondMileVehicleStatus) => void;
  onHubKeywordInputChange: (value: string) => void;
  onHubSearchSubmit: (event: React.FormEvent) => void;
  onClearHubSearch: () => void;
  onDriverKeywordInputChange: (value: string) => void;
  onDriverSearchSubmit: (event: React.FormEvent) => void;
  onClearDriverSearch: () => void;
  onPageSizeChange: (pageSize: number) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
}

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
const ALL_FILTER_VALUE = 'ALL';
const VEHICLE_TYPE_OPTIONS: SecondMileVehicleType[] = ['TRUCK', 'VAN'];
const VEHICLE_STATUS_OPTIONS: SecondMileVehicleStatus[] = [
  'ACTIVE',
  'INACTIVE',
  'MAINTENANCE',
];

export const SecondMileVehicleResultsCard: React.FC<
  SecondMileVehicleResultsCardProps
> = ({
  canManage,
  data,
  driverLabelByStaffId,
  hubById,
  imageRefreshKey,
  isLoading,
  isFetching,
  isSaving,
  isDeleting,
  isUploadingImage,
  pageSize,
  keywordInput,
  vehicleTypeFilter,
  statusFilter,
  hubKeywordInput,
  driverKeywordInput,
  onView,
  onEdit,
  onUploadImage,
  onDelete,
  onKeywordInputChange,
  onSearchSubmit,
  onClearSearch,
  onVehicleTypeFilterChange,
  onStatusFilterChange,
  onHubKeywordInputChange,
  onHubSearchSubmit,
  onClearHubSearch,
  onDriverKeywordInputChange,
  onDriverSearchSubmit,
  onClearDriverSearch,
  onPageSizeChange,
  onPreviousPage,
  onNextPage,
}) => {
  const [isLicenseSearchOpen, setIsLicenseSearchOpen] = React.useState(false);
  const [isVehicleTypeFilterOpen, setIsVehicleTypeFilterOpen] =
    React.useState(false);
  const [isStatusFilterOpen, setIsStatusFilterOpen] = React.useState(false);
  const [isHubSearchOpen, setIsHubSearchOpen] = React.useState(false);
  const [isDriverSearchOpen, setIsDriverSearchOpen] = React.useState(false);
  const licenseSearchInputRef = React.useRef<HTMLInputElement>(null);
  const hubSearchInputRef = React.useRef<HTMLInputElement>(null);
  const driverSearchInputRef = React.useRef<HTMLInputElement>(null);

  React.useEffect(() => {
    if (!isLicenseSearchOpen) {
      return;
    }

    window.setTimeout(() => {
      licenseSearchInputRef.current?.focus();
      licenseSearchInputRef.current?.select();
    }, 0);
  }, [isLicenseSearchOpen]);

  React.useEffect(() => {
    if (!isHubSearchOpen) {
      return;
    }

    window.setTimeout(() => {
      hubSearchInputRef.current?.focus();
      hubSearchInputRef.current?.select();
    }, 0);
  }, [isHubSearchOpen]);

  React.useEffect(() => {
    if (!isDriverSearchOpen) {
      return;
    }

    window.setTimeout(() => {
      driverSearchInputRef.current?.focus();
      driverSearchInputRef.current?.select();
    }, 0);
  }, [isDriverSearchOpen]);

  const handleLicenseSearchSubmit = (event: React.FormEvent) => {
    onSearchSubmit(event);
    setIsLicenseSearchOpen(false);
  };

  const handleVehicleTypeFilterChange = (value: string) => {
    onVehicleTypeFilterChange(
      value === ALL_FILTER_VALUE
        ? undefined
        : (value as SecondMileVehicleType)
    );
    setIsVehicleTypeFilterOpen(false);
  };

  const handleStatusFilterChange = (value: string) => {
    onStatusFilterChange(
      value === ALL_FILTER_VALUE
        ? undefined
        : (value as SecondMileVehicleStatus)
    );
    setIsStatusFilterOpen(false);
  };

  const handleHubSearchSubmit = (event: React.FormEvent) => {
    onHubSearchSubmit(event);
    setIsHubSearchOpen(false);
  };

  const handleDriverSearchSubmit = (event: React.FormEvent) => {
    onDriverSearchSubmit(event);
    setIsDriverSearchOpen(false);
  };

  return (
  <Card>
    <CardHeader>
      <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
    </CardHeader>
    <CardContent>
      {isLoading ? (
        <div className='flex items-center gap-2 text-muted-foreground'>
          <Loader2 className='h-4 w-4 animate-spin' />
          Đang tải phương tiện...
        </div>
      ) : data ? (
        <div className='space-y-4'>
          <div className='overflow-hidden rounded-md border'>
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Ảnh</TableHead>
                    <TableHead className='min-w-[180px]'>
                      <Popover
                        open={isLicenseSearchOpen}
                        onOpenChange={setIsLicenseSearchOpen}
                      >
                        <div className='flex items-center gap-1'>
                          <span>Biển số xe</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={keywordInput ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              disabled={isFetching}
                              title='Tìm kiếm biển số xe'
                              aria-label='Tìm kiếm biển số xe'
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
                            onSubmit={handleLicenseSearchSubmit}
                          >
                            <Input
                              ref={licenseSearchInputRef}
                              className='h-9 bg-background'
                              value={keywordInput}
                              onChange={(event) =>
                                onKeywordInputChange(event.target.value)
                              }
                              placeholder='Tìm biển số...'
                              disabled={isFetching}
                            />
                            <Button
                              type='submit'
                              variant='outline'
                              size='icon'
                              className='size-9 shrink-0'
                              disabled={isFetching}
                              title='Tìm kiếm'
                              aria-label='Tìm kiếm biển số xe'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                            {keywordInput ? (
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                onClick={onClearSearch}
                                title='Xóa tìm kiếm'
                                aria-label='Xóa tìm kiếm biển số xe'
                              >
                                <X className='h-4 w-4' />
                              </Button>
                            ) : null}
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='min-w-[120px]'>
                      <Popover
                        open={isVehicleTypeFilterOpen}
                        onOpenChange={setIsVehicleTypeFilterOpen}
                      >
                        <div className='flex items-center gap-1'>
                          <span>Loại xe</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={vehicleTypeFilter ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              disabled={isFetching}
                              title='Tìm kiếm loại xe'
                              aria-label='Tìm kiếm loại xe'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-56 p-3'
                        >
                          <Select
                            value={vehicleTypeFilter ?? ALL_FILTER_VALUE}
                            onValueChange={handleVehicleTypeFilterChange}
                            disabled={isFetching}
                          >
                            <SelectTrigger className='h-9'>
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value={ALL_FILTER_VALUE}>
                                Tất cả loại xe
                              </SelectItem>
                              {VEHICLE_TYPE_OPTIONS.map((vehicleType) => (
                                <SelectItem
                                  key={vehicleType}
                                  value={vehicleType}
                                >
                                  {formatVehicleType(vehicleType)}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='min-w-[140px]'>
                      <Popover
                        open={isStatusFilterOpen}
                        onOpenChange={setIsStatusFilterOpen}
                      >
                        <div className='flex items-center gap-1'>
                          <span>Trạng thái</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={statusFilter ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              disabled={isFetching}
                              title='Tìm kiếm trạng thái'
                              aria-label='Tìm kiếm trạng thái'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-56 p-3'
                        >
                          <Select
                            value={statusFilter ?? ALL_FILTER_VALUE}
                            onValueChange={handleStatusFilterChange}
                            disabled={isFetching}
                          >
                            <SelectTrigger className='h-9'>
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value={ALL_FILTER_VALUE}>
                                Tất cả trạng thái
                              </SelectItem>
                              {VEHICLE_STATUS_OPTIONS.map((status) => (
                                <SelectItem key={status} value={status}>
                                  {formatStatusLabel(status)}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='min-w-[180px]'>
                      <Popover
                        open={isHubSearchOpen}
                        onOpenChange={setIsHubSearchOpen}
                      >
                        <div className='flex items-center gap-1'>
                          <span>Hub</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={hubKeywordInput ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              disabled={isFetching}
                              title='Tìm kiếm hub'
                              aria-label='Tìm kiếm hub'
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
                            onSubmit={handleHubSearchSubmit}
                          >
                            <Input
                              ref={hubSearchInputRef}
                              className='h-9 bg-background'
                              value={hubKeywordInput}
                              onChange={(event) =>
                                onHubKeywordInputChange(event.target.value)
                              }
                              placeholder='Tìm mã hoặc tên hub...'
                              disabled={isFetching}
                            />
                            <Button
                              type='submit'
                              variant='outline'
                              size='icon'
                              className='size-9 shrink-0'
                              disabled={isFetching}
                              title='Tìm kiếm'
                              aria-label='Tìm kiếm hub'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                            {hubKeywordInput ? (
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                onClick={onClearHubSearch}
                                title='Xóa tìm kiếm'
                                aria-label='Xóa tìm kiếm hub'
                              >
                                <X className='h-4 w-4' />
                              </Button>
                            ) : null}
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='min-w-[160px]'>
                      <Popover
                        open={isDriverSearchOpen}
                        onOpenChange={setIsDriverSearchOpen}
                      >
                        <div className='flex items-center gap-1'>
                          <span>Tài xế</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={driverKeywordInput ? 'outline' : 'ghost'}
                              size='icon'
                              className='size-7'
                              disabled={isFetching}
                              title='Tìm kiếm tài xế'
                              aria-label='Tìm kiếm tài xế'
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
                            onSubmit={handleDriverSearchSubmit}
                          >
                            <Input
                              ref={driverSearchInputRef}
                              className='h-9 bg-background'
                              value={driverKeywordInput}
                              onChange={(event) =>
                                onDriverKeywordInputChange(event.target.value)
                              }
                              placeholder='Tìm mã hoặc tên tài xế...'
                              disabled={isFetching}
                            />
                            <Button
                              type='submit'
                              variant='outline'
                              size='icon'
                              className='size-9 shrink-0'
                              disabled={isFetching}
                              title='Tìm kiếm'
                              aria-label='Tìm kiếm tài xế'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                            {driverKeywordInput ? (
                              <Button
                                type='button'
                                variant='ghost'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={isFetching}
                                onClick={onClearDriverSearch}
                                title='Xóa tìm kiếm'
                                aria-label='Xóa tìm kiếm tài xế'
                              >
                                <X className='h-4 w-4' />
                              </Button>
                            ) : null}
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead>Sức chứa</TableHead>
                    <TableHead className='text-right'>Thao tác</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.items.length > 0 ? (
                    data.items.map((vehicle) => {
                      const imageUrl = vehicle.imageUrl?.trim();
                      const imageSrc =
                        imageUrl && imageRefreshKey
                          ? `${imageUrl}${imageUrl.includes('?') ? '&' : '?'}v=${imageRefreshKey}`
                          : imageUrl;
                      const driverLabel = vehicle.assignedStaffId
                        ? (driverLabelByStaffId[vehicle.assignedStaffId] ??
                          `Tài xế #${vehicle.assignedStaffId}`)
                        : '-';

                      return (
                        <TableRow key={vehicle.id}>
                          <TableCell>
                            <div className='relative h-12 w-16 overflow-hidden rounded border bg-muted'>
                              {imageSrc ? (
                                <Image
                                  src={imageSrc}
                                  alt={vehicle.licensePlate}
                                  fill
                                  unoptimized
                                  sizes='64px'
                                  className='object-cover'
                                />
                              ) : (
                                <div className='flex h-full items-center justify-center text-[10px] text-muted-foreground'>
                                  -
                                </div>
                              )}
                            </div>
                          </TableCell>
                          <TableCell className='font-medium'>
                            {vehicle.licensePlate}
                          </TableCell>
                          <TableCell>
                            <Badge variant='outline'>
                              {formatVehicleType(vehicle.vehicleType)}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant={getStatusBadgeVariant(vehicle.status)}
                            >
                              {formatStatusLabel(vehicle.status)}
                            </Badge>
                          </TableCell>
                          <TableCell className='text-xs text-muted-foreground'>
                            {buildHubLabel(vehicle.hubId, hubById)}
                          </TableCell>
                          <TableCell className='text-xs text-muted-foreground'>
                            {driverLabel}
                          </TableCell>
                          <TableCell className='text-xs text-muted-foreground'>
                            {vehicle.maxBags} bao |{' '}
                            {formatOptionalNumber(vehicle.maxWeight)} kg |{' '}
                            {formatOptionalNumber(vehicle.maxVolume)} m3
                          </TableCell>
                          <TableCell className='text-right'>
                            <div className='flex justify-end gap-1'>
                              <Button
                                type='button'
                                variant='outline'
                                size='icon'
                                onClick={() => onView(vehicle.id)}
                                title='Xem'
                                aria-label={`Xem ${vehicle.licensePlate}`}
                              >
                                <Eye className='h-4 w-4' />
                              </Button>
                              {canManage && (
                                <>
                                  <Button
                                    asChild
                                    variant='outline'
                                    size='icon'
                                    disabled={
                                      isSaving || isDeleting || isUploadingImage
                                    }
                                    title='Tải ảnh lên'
                                    aria-label={`Tải ảnh cho ${vehicle.licensePlate}`}
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
                                          const file = event.target.files?.[0];
                                          if (file) {
                                            onUploadImage(vehicle, file);
                                          }
                                          event.target.value = '';
                                        }}
                                      />
                                    </label>
                                  </Button>
                                  <Button
                                    type='button'
                                    variant='outline'
                                    size='icon'
                                    onClick={() => onEdit(vehicle)}
                                    disabled={isSaving || isDeleting}
                                    title='Sửa'
                                    aria-label={`Sửa ${vehicle.licensePlate}`}
                                  >
                                    <Pencil className='h-4 w-4' />
                                  </Button>
                                  <Button
                                    type='button'
                                    variant='destructive'
                                    size='icon'
                                    onClick={() => onDelete(vehicle)}
                                    disabled={isSaving || isDeleting}
                                    title='Xóa'
                                    aria-label={`Xóa ${vehicle.licensePlate}`}
                                  >
                                    <Trash2 className='h-4 w-4' />
                                  </Button>
                                </>
                              )}
                            </div>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={8}
                        className='h-24 text-center text-muted-foreground'
                      >
                        Không tìm thấy phương tiện.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          </div>
          <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
            <div className='flex items-center gap-2 text-sm text-muted-foreground'>
              <span>Số dòng/trang</span>
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
        </div>
      ) : (
        <p className='text-muted-foreground'>Không tìm thấy phương tiện.</p>
      )}
    </CardContent>
  </Card>
  );
};
