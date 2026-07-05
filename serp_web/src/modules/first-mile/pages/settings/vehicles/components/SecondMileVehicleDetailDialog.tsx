/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle detail dialog
 */

import Image from 'next/image';
import React from 'react';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { Loader2, Pencil } from 'lucide-react';
import type { Hub, SecondMileVehicle } from '../../../../types';
import {
  buildHubLabel,
  formatDateTime,
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
} from '../secondMileVehiclePageModels';

interface SecondMileVehicleDetailDialogProps {
  open: boolean;
  canManage: boolean;
  isFetching: boolean;
  vehicle?: SecondMileVehicle;
  driverLabelByStaffId: Record<number, string>;
  hubById: Record<number, Hub>;
  imageRefreshKey?: number;
  onOpenChange: (open: boolean) => void;
  onEdit: () => void;
}

export const SecondMileVehicleDetailDialog: React.FC<
  SecondMileVehicleDetailDialogProps
> = ({
  open,
  canManage,
  isFetching,
  vehicle,
  driverLabelByStaffId,
  hubById,
  imageRefreshKey,
  onOpenChange,
  onEdit,
}) => {
  const imageUrl = vehicle?.imageUrl?.trim();
  const imageSrc =
    imageUrl && imageRefreshKey
      ? `${imageUrl}${imageUrl.includes('?') ? '&' : '?'}v=${imageRefreshKey}`
      : imageUrl;
  const driverLabel = vehicle?.assignedStaffId
    ? (driverLabelByStaffId[vehicle.assignedStaffId] ??
      `Tài xế #${vehicle.assignedStaffId}`)
    : '-';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Chi tiết phương tiện</DialogTitle>
          <DialogDescription>
            Thông tin phương tiện chặng giữa.
          </DialogDescription>
        </DialogHeader>

        {isFetching ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải...
          </div>
        ) : vehicle ? (
          <div className='space-y-4'>
            <div className='relative h-40 w-full overflow-hidden rounded-lg border bg-muted'>
              {imageSrc ? (
                <Image
                  src={imageSrc}
                  alt={vehicle.licensePlate}
                  fill
                  unoptimized
                  sizes='(min-width: 640px) 672px, 100vw'
                  className='object-cover'
                />
              ) : (
                <div className='flex h-full items-center justify-center text-sm text-muted-foreground'>
                  Chưa có ảnh
                </div>
              )}
            </div>

            <dl className='grid gap-3 text-sm sm:grid-cols-2'>
              <div>
                <dt className='text-muted-foreground'>Biển số xe</dt>
                <dd className='font-medium'>{vehicle.licensePlate}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Loại xe</dt>
                <dd>{formatVehicleType(vehicle.vehicleType)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Trạng thái</dt>
                <dd>{formatStatusLabel(vehicle.status)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Hub</dt>
                <dd>{buildHubLabel(vehicle.hubId, hubById)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Tài xế</dt>
                <dd>{driverLabel}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Số bao tối đa</dt>
                <dd>{vehicle.maxBags}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Tải trọng tối đa (kg)</dt>
                <dd>{formatOptionalNumber(vehicle.maxWeight)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Thể tích tối đa (m3)</dt>
                <dd>{formatOptionalNumber(vehicle.maxVolume)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Thời gian tạo</dt>
                <dd>{formatDateTime(vehicle.createdAt)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Thời gian cập nhật</dt>
                <dd>{formatDateTime(vehicle.updatedAt)}</dd>
              </div>
            </dl>
          </div>
        ) : (
          <p className='text-muted-foreground'>Không tìm thấy phương tiện.</p>
        )}

        <DialogFooter>
          {canManage && vehicle && (
            <Button type='button' variant='outline' onClick={onEdit}>
              <Pencil className='mr-2 h-4 w-4' />
              Sửa
            </Button>
          )}
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Đóng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
