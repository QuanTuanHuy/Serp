/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle detail dialog
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
import type { Vehicle } from '../../../../types';
import {
  buildPostOfficeLabel,
  formatDateTime,
  formatOptionalNumber,
  formatStatusLabel,
  formatVehicleType,
} from '../firstMileVehiclePageModels';

interface VehicleDetailDialogProps {
  open: boolean;
  canManageVehicles: boolean;
  isFetchingVehicleDetail: boolean;
  vehicleDetail?: Vehicle;
  onOpenChange: (open: boolean) => void;
  onEditFromDetails: () => void;
  resolveCourierLabel: (postOfficeStaffId?: number) => string;
}

export const VehicleDetailDialog: React.FC<VehicleDetailDialogProps> = ({
  open,
  canManageVehicles,
  isFetchingVehicleDetail,
  vehicleDetail,
  onOpenChange,
  onEditFromDetails,
  resolveCourierLabel,
}) => {
  const imageUrl = vehicleDetail?.imageUrl?.trim();

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Vehicle details</DialogTitle>
          <DialogDescription>
            Review complete vehicle information and audit fields.
          </DialogDescription>
        </DialogHeader>

        {isFetchingVehicleDetail ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading vehicle details...
          </div>
        ) : vehicleDetail ? (
          <div className='space-y-4'>
            <div className='relative h-40 w-full overflow-hidden rounded-lg border bg-muted'>
              {imageUrl ? (
                <Image
                  src={imageUrl}
                  alt={`Vehicle ${vehicleDetail.licensePlate}`}
                  fill
                  unoptimized
                  sizes='(min-width: 640px) 672px, 100vw'
                  className='object-cover'
                />
              ) : (
                <div className='flex h-full w-full items-center justify-center text-sm text-muted-foreground'>
                  No image
                </div>
              )}
            </div>

            <div className='grid gap-3 text-sm sm:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Vehicle ID</p>
                <p className='font-medium'>{vehicleDetail.id}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>License plate</p>
                <p className='font-medium'>{vehicleDetail.licensePlate}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Status</p>
                <p className='font-medium'>
                  {formatStatusLabel(vehicleDetail.status)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Vehicle type</p>
                <p className='font-medium'>
                  {formatVehicleType(vehicleDetail.vehicleType)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Max weight (kg)</p>
                <p className='font-medium'>
                  {formatOptionalNumber(vehicleDetail.maxWeight)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Max volume (m3)</p>
                <p className='font-medium'>
                  {formatOptionalNumber(vehicleDetail.maxVolume)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Post office</p>
                <p className='font-medium'>
                  {buildPostOfficeLabel(vehicleDetail)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Courier staff</p>
                <p className='font-medium'>
                  {resolveCourierLabel(vehicleDetail.postOfficeStaffId)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Created by</p>
                <p className='font-medium'>{vehicleDetail.createdBy || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Updated by</p>
                <p className='font-medium'>{vehicleDetail.updatedBy || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Created at</p>
                <p className='font-medium'>
                  {formatDateTime(vehicleDetail.createdAt)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Updated at</p>
                <p className='font-medium'>
                  {formatDateTime(vehicleDetail.updatedAt)}
                </p>
              </div>
            </div>
          </div>
        ) : (
          <p className='text-sm text-muted-foreground'>
            Vehicle details are unavailable.
          </p>
        )}

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Close
          </Button>
          {canManageVehicles && vehicleDetail && (
            <Button type='button' onClick={onEditFromDetails}>
              <Pencil className='mr-2 h-4 w-4' />
              Edit vehicle
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
