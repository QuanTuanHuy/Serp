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
  Input,
  Label,
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
  isUploadingImage: boolean;
  vehicle?: SecondMileVehicle;
  driverLabelByStaffId: Record<number, string>;
  hubById: Record<number, Hub>;
  imageRefreshKey?: number;
  onOpenChange: (open: boolean) => void;
  onEdit: () => void;
  onUploadImage: (file: File) => void;
}

export const SecondMileVehicleDetailDialog: React.FC<
  SecondMileVehicleDetailDialogProps
> = ({
  open,
  canManage,
  isFetching,
  isUploadingImage,
  vehicle,
  driverLabelByStaffId,
  hubById,
  imageRefreshKey,
  onOpenChange,
  onEdit,
  onUploadImage,
}) => {
  const imageUrl = vehicle?.imageUrl?.trim();
  const imageSrc =
    imageUrl && imageRefreshKey
      ? `${imageUrl}${imageUrl.includes('?') ? '&' : '?'}v=${imageRefreshKey}`
      : imageUrl;
  const driverLabel = vehicle?.assignedStaffId
    ? (driverLabelByStaffId[vehicle.assignedStaffId] ??
      `Driver #${vehicle.assignedStaffId}`)
    : '—';

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Vehicle details</DialogTitle>
          <DialogDescription>
            Second-mile vehicle information.
          </DialogDescription>
        </DialogHeader>

        {isFetching ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading...
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
                  No image
                </div>
              )}
            </div>

            {canManage && (
              <div className='space-y-2'>
                <Label htmlFor='vehicle-image-upload'>Upload image</Label>
                <Input
                  id='vehicle-image-upload'
                  type='file'
                  accept='image/*'
                  disabled={isUploadingImage}
                  onChange={(event) => {
                    const file = event.target.files?.[0];
                    if (file) {
                      onUploadImage(file);
                      event.target.value = '';
                    }
                  }}
                />
              </div>
            )}

            <dl className='grid gap-3 text-sm sm:grid-cols-2'>
              <div>
                <dt className='text-muted-foreground'>License plate</dt>
                <dd className='font-medium'>{vehicle.licensePlate}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Type</dt>
                <dd>{formatVehicleType(vehicle.vehicleType)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Status</dt>
                <dd>{formatStatusLabel(vehicle.status)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Hub</dt>
                <dd>{buildHubLabel(vehicle.hubId, hubById)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Driver</dt>
                <dd>{driverLabel}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Max bags</dt>
                <dd>{vehicle.maxBags}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Max weight (kg)</dt>
                <dd>{formatOptionalNumber(vehicle.maxWeight)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Max volume (m³)</dt>
                <dd>{formatOptionalNumber(vehicle.maxVolume)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Created</dt>
                <dd>{formatDateTime(vehicle.createdAt)}</dd>
              </div>
              <div>
                <dt className='text-muted-foreground'>Updated</dt>
                <dd>{formatDateTime(vehicle.updatedAt)}</dd>
              </div>
            </dl>
          </div>
        ) : (
          <p className='text-muted-foreground'>Vehicle not found.</p>
        )}

        <DialogFooter>
          {canManage && vehicle && (
            <Button type='button' variant='outline' onClick={onEdit}>
              <Pencil className='mr-2 h-4 w-4' />
              Edit
            </Button>
          )}
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
