/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle results table card
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
  Vehicle,
  VehicleStatus,
  VehicleType,
} from '../../../../types';
import {
  buildPostOfficeLabel,
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
  getStatusBadgeVariant,
} from '../firstMileVehiclePageModels';

interface VehicleResultsCardProps {
  canViewVehicles: boolean;
  canManageVehicles: boolean;
  data?: FirstMilePaginatedData<Vehicle>;
  isLoading: boolean;
  isFetching: boolean;
  isSaving: boolean;
  isDeleting: boolean;
  isUploadingImage: boolean;
  pageSize: number;
  keywordInput: string;
  vehicleTypeFilter?: VehicleType;
  statusFilter?: VehicleStatus;
  postOfficeKeywordInput: string;
  courierKeywordInput: string;
  onViewDetails: (vehicleId: number) => void;
  onEdit: (vehicle: Vehicle) => void;
  onUploadImage: (vehicle: Vehicle, file: File) => void;
  onDelete: (vehicle: Vehicle) => void;
  onKeywordInputChange: (value: string) => void;
  onSearchSubmit: (event: React.FormEvent) => void;
  onClearSearch: () => void;
  onVehicleTypeFilterChange: (value?: VehicleType) => void;
  onStatusFilterChange: (value?: VehicleStatus) => void;
  onPostOfficeKeywordInputChange: (value: string) => void;
  onPostOfficeSearchSubmit: (event: React.FormEvent) => void;
  onClearPostOfficeSearch: () => void;
  onCourierKeywordInputChange: (value: string) => void;
  onCourierSearchSubmit: (event: React.FormEvent) => void;
  onClearCourierSearch: () => void;
  onPageSizeChange: (pageSize: number) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  resolveCourierLabel: (postOfficeStaffId?: number) => string;
}

const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
const ALL_FILTER_VALUE = 'ALL';
const VEHICLE_TYPE_OPTIONS: VehicleType[] = ['BIKE', 'TRUCK'];
const VEHICLE_STATUS_OPTIONS: VehicleStatus[] = [
  'ACTIVE',
  'INACTIVE',
  'MAINTENANCE',
  'IN_USE',
  'FULL',
];

export const VehicleResultsCard: React.FC<VehicleResultsCardProps> = ({
  canViewVehicles,
  canManageVehicles,
  data,
  isLoading,
  isFetching,
  isSaving,
  isDeleting,
  isUploadingImage,
  pageSize,
  keywordInput,
  vehicleTypeFilter,
  statusFilter,
  postOfficeKeywordInput,
  courierKeywordInput,
  onViewDetails,
  onEdit,
  onUploadImage,
  onDelete,
  onKeywordInputChange,
  onSearchSubmit,
  onClearSearch,
  onVehicleTypeFilterChange,
  onStatusFilterChange,
  onPostOfficeKeywordInputChange,
  onPostOfficeSearchSubmit,
  onClearPostOfficeSearch,
  onCourierKeywordInputChange,
  onCourierSearchSubmit,
  onClearCourierSearch,
  onPageSizeChange,
  onPreviousPage,
  onNextPage,
  resolveCourierLabel,
}) => {
  const [isLicenseSearchOpen, setIsLicenseSearchOpen] = React.useState(false);
  const [isVehicleTypeFilterOpen, setIsVehicleTypeFilterOpen] =
    React.useState(false);
  const [isStatusFilterOpen, setIsStatusFilterOpen] = React.useState(false);
  const [isPostOfficeSearchOpen, setIsPostOfficeSearchOpen] =
    React.useState(false);
  const [isCourierSearchOpen, setIsCourierSearchOpen] = React.useState(false);
  const licenseSearchInputRef = React.useRef<HTMLInputElement>(null);
  const postOfficeSearchInputRef = React.useRef<HTMLInputElement>(null);
  const courierSearchInputRef = React.useRef<HTMLInputElement>(null);

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
    if (!isPostOfficeSearchOpen) {
      return;
    }

    window.setTimeout(() => {
      postOfficeSearchInputRef.current?.focus();
      postOfficeSearchInputRef.current?.select();
    }, 0);
  }, [isPostOfficeSearchOpen]);

  React.useEffect(() => {
    if (!isCourierSearchOpen) {
      return;
    }

    window.setTimeout(() => {
      courierSearchInputRef.current?.focus();
      courierSearchInputRef.current?.select();
    }, 0);
  }, [isCourierSearchOpen]);

  const handleLicenseSearchSubmit = (event: React.FormEvent) => {
    onSearchSubmit(event);
    setIsLicenseSearchOpen(false);
  };

  const handleVehicleTypeFilterChange = (value: string) => {
    onVehicleTypeFilterChange(
      value === ALL_FILTER_VALUE ? undefined : (value as VehicleType)
    );
    setIsVehicleTypeFilterOpen(false);
  };

  const handleStatusFilterChange = (value: string) => {
    onStatusFilterChange(
      value === ALL_FILTER_VALUE ? undefined : (value as VehicleStatus)
    );
    setIsStatusFilterOpen(false);
  };

  const handlePostOfficeSearchSubmit = (event: React.FormEvent) => {
    onPostOfficeSearchSubmit(event);
    setIsPostOfficeSearchOpen(false);
  };

  const handleCourierSearchSubmit = (event: React.FormEvent) => {
    onCourierSearchSubmit(event);
    setIsCourierSearchOpen(false);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Kết quả ({data?.totalItems ?? 0})</CardTitle>
      </CardHeader>
      <CardContent>
        {!canViewVehicles ? (
          <p className='text-muted-foreground'>
            Bạn không có quyền truy cập dữ liệu phương tiện.
          </p>
        ) : isLoading ? (
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
                      <TableHead className='min-w-[120px]'>Ảnh</TableHead>
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
                                variant='ghost'
                                size='icon'
                                className='size-7'
                                disabled={!canViewVehicles}
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
                                disabled={!canViewVehicles || isFetching}
                              />
                              <Button
                                type='submit'
                                variant='outline'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={!canViewVehicles || isFetching}
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
                                  disabled={!canViewVehicles || isFetching}
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
                                disabled={!canViewVehicles}
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
                              disabled={!canViewVehicles || isFetching}
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
                                disabled={!canViewVehicles}
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
                              disabled={!canViewVehicles || isFetching}
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
                      <TableHead className='min-w-[200px]'>
                        <Popover
                          open={isPostOfficeSearchOpen}
                          onOpenChange={setIsPostOfficeSearchOpen}
                        >
                          <div className='flex items-center gap-1'>
                            <span>Bưu cục</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  postOfficeKeywordInput ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                disabled={!canViewVehicles}
                                title='Tìm kiếm bưu cục'
                                aria-label='Tìm kiếm bưu cục'
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
                              onSubmit={handlePostOfficeSearchSubmit}
                            >
                              <Input
                                ref={postOfficeSearchInputRef}
                                className='h-9 bg-background'
                                value={postOfficeKeywordInput}
                                onChange={(event) =>
                                  onPostOfficeKeywordInputChange(
                                    event.target.value
                                  )
                                }
                                placeholder='Tìm mã hoặc tên bưu cục...'
                                disabled={!canViewVehicles || isFetching}
                              />
                              <Button
                                type='submit'
                                variant='outline'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={!canViewVehicles || isFetching}
                                title='Tìm kiếm'
                                aria-label='Tìm kiếm bưu cục'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                              {postOfficeKeywordInput ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='size-9 shrink-0'
                                  disabled={!canViewVehicles || isFetching}
                                  onClick={onClearPostOfficeSearch}
                                  title='Xóa tìm kiếm'
                                  aria-label='Xóa tìm kiếm bưu cục'
                                >
                                  <X className='h-4 w-4' />
                                </Button>
                              ) : null}
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='min-w-[180px]'>
                        <Popover
                          open={isCourierSearchOpen}
                          onOpenChange={setIsCourierSearchOpen}
                        >
                          <div className='flex items-center gap-1'>
                            <span>Nhân viên giao nhận</span>
                            <PopoverTrigger asChild>
                              <Button
                                type='button'
                                variant={
                                  courierKeywordInput ? 'outline' : 'ghost'
                                }
                                size='icon'
                                className='size-7'
                                disabled={!canViewVehicles}
                                title='Tìm kiếm nhân viên giao nhận'
                                aria-label='Tìm kiếm nhân viên giao nhận'
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
                              onSubmit={handleCourierSearchSubmit}
                            >
                              <Input
                                ref={courierSearchInputRef}
                                className='h-9 bg-background'
                                value={courierKeywordInput}
                                onChange={(event) =>
                                  onCourierKeywordInputChange(
                                    event.target.value
                                  )
                                }
                                placeholder='Tìm mã hoặc tên nhân viên...'
                                disabled={!canViewVehicles || isFetching}
                              />
                              <Button
                                type='submit'
                                variant='outline'
                                size='icon'
                                className='size-9 shrink-0'
                                disabled={!canViewVehicles || isFetching}
                                title='Tìm kiếm'
                                aria-label='Tìm kiếm nhân viên giao nhận'
                              >
                                <Search className='h-4 w-4' />
                              </Button>
                              {courierKeywordInput ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='icon'
                                  className='size-9 shrink-0'
                                  disabled={!canViewVehicles || isFetching}
                                  onClick={onClearCourierSearch}
                                  title='Xóa tìm kiếm'
                                  aria-label='Xóa tìm kiếm nhân viên giao nhận'
                                >
                                  <X className='h-4 w-4' />
                                </Button>
                              ) : null}
                            </form>
                          </PopoverContent>
                        </Popover>
                      </TableHead>
                      <TableHead className='min-w-[200px]'>
                        Sức chứa
                      </TableHead>
                      <TableHead className='sticky right-0 z-20 border-l bg-card text-right'>
                        Thao tác
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {data.items.length > 0 ? (
                      data.items.map((vehicle) => {
                        const imageUrl = vehicle.imageUrl?.trim();

                        return (
                          <TableRow key={vehicle.id} className='group'>
                            <TableCell>
                              <div className='relative h-12 w-16 overflow-hidden rounded border bg-muted'>
                                {imageUrl ? (
                                  <Image
                                    src={imageUrl}
                                    alt={`Phương tiện ${vehicle.licensePlate}`}
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
                              {buildPostOfficeLabel(vehicle)}
                            </TableCell>
                            <TableCell className='text-xs text-muted-foreground'>
                              {resolveCourierLabel(vehicle.postOfficeStaffId)}
                            </TableCell>
                            <TableCell className='text-xs text-muted-foreground'>
                              {formatOptionalNumber(vehicle.maxWeight)} kg |{' '}
                              {formatOptionalNumber(vehicle.maxVolume)} m3
                            </TableCell>
                            <TableCell className='sticky right-0 z-10 border-l bg-background text-right group-hover:bg-muted/50'>
                              <div className='flex items-center justify-end gap-1'>
                                <Button
                                  type='button'
                                  variant='outline'
                                  size='icon'
                                  onClick={() => onViewDetails(vehicle.id)}
                                  title='Chi tiết'
                                  aria-label={`Xem chi tiết ${vehicle.licensePlate}`}
                                >
                                  <Eye className='h-4 w-4' />
                                </Button>
                                {canManageVehicles && (
                                  <>
                                    <Button
                                      asChild
                                      variant='outline'
                                      size='icon'
                                      disabled={
                                        isSaving ||
                                        isDeleting ||
                                        isUploadingImage
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
                                            const file =
                                              event.target.files?.[0];
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

            <div className='flex flex-col gap-3 pt-2 md:flex-row md:items-center md:justify-between'>
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
