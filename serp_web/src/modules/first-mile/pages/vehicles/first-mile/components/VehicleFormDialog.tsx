/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle form dialog
 */

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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type { VehicleStatus, VehicleType } from '../../../../types';
import {
  VEHICLE_STATUS_OPTIONS,
  VEHICLE_TYPE_OPTIONS,
  type VehicleFormMode,
  type VehicleFormState,
} from '../vehiclePageModels';

type UpdateVehicleFormField = <K extends keyof VehicleFormState>(
  field: K,
  value: VehicleFormState[K]
) => void;

interface VehicleSelectOption {
  value: string;
  label: string;
}

const NONE_VALUE = '__NONE__';

interface VehicleFormDialogProps {
  open: boolean;
  formMode: VehicleFormMode;
  formValues: VehicleFormState;
  isSaving: boolean;
  postOfficeOptions: VehicleSelectOption[];
  courierOptions: VehicleSelectOption[];
  isLoadingPostOffices: boolean;
  isLoadingCouriers: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onUpdateField: UpdateVehicleFormField;
}

export const VehicleFormDialog: React.FC<VehicleFormDialogProps> = ({
  open,
  formMode,
  formValues,
  isSaving,
  postOfficeOptions,
  courierOptions,
  isLoadingPostOffices,
  isLoadingCouriers,
  onOpenChange,
  onSubmit,
  onUpdateField,
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>
            {formMode === 'create' ? 'Create vehicle' : 'Update vehicle'}
          </DialogTitle>
          <DialogDescription>
            Fill in required fields to{' '}
            {formMode === 'create' ? 'create a new' : 'update the'} vehicle.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='vehicle-license-plate'>License plate *</Label>
              <Input
                id='vehicle-license-plate'
                value={formValues.licensePlate}
                onChange={(event) =>
                  onUpdateField('licensePlate', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-status'>Status *</Label>
              <Select
                value={formValues.status}
                onValueChange={(value) =>
                  onUpdateField('status', value as VehicleStatus)
                }
                disabled={isSaving}
              >
                <SelectTrigger id='vehicle-status' className='w-full'>
                  <SelectValue placeholder='Select status' />
                </SelectTrigger>
                <SelectContent>
                  {VEHICLE_STATUS_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-type'>Vehicle type *</Label>
              <Select
                value={formValues.vehicleType}
                onValueChange={(value) =>
                  onUpdateField('vehicleType', value as VehicleType)
                }
                disabled={isSaving}
              >
                <SelectTrigger id='vehicle-type' className='w-full'>
                  <SelectValue placeholder='Select type' />
                </SelectTrigger>
                <SelectContent>
                  {VEHICLE_TYPE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-max-weight'>Max weight (kg)</Label>
              <Input
                id='vehicle-max-weight'
                type='number'
                min={0}
                step='any'
                value={formValues.maxWeight}
                onChange={(event) =>
                  onUpdateField('maxWeight', event.target.value)
                }
                disabled={isSaving}
                placeholder='e.g. 150'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-max-volume'>Max volume (m3)</Label>
              <Input
                id='vehicle-max-volume'
                type='number'
                min={0}
                step='any'
                value={formValues.maxVolume}
                onChange={(event) =>
                  onUpdateField('maxVolume', event.target.value)
                }
                disabled={isSaving}
                placeholder='e.g. 3.2'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-post-office-id'>Post office</Label>
              <Select
                value={formValues.postOfficeId.trim() || NONE_VALUE}
                onValueChange={(value) => {
                  const nextPostOfficeId = value === NONE_VALUE ? '' : value;

                  onUpdateField('postOfficeId', nextPostOfficeId);

                  if (nextPostOfficeId !== formValues.postOfficeId) {
                    onUpdateField('postOfficeStaffId', '');
                  }
                }}
                disabled={isSaving || isLoadingPostOffices}
              >
                <SelectTrigger id='vehicle-post-office-id' className='w-full'>
                  <SelectValue
                    placeholder={
                      isLoadingPostOffices
                        ? 'Loading post offices...'
                        : 'Select post office'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={NONE_VALUE}>Not assigned</SelectItem>
                  {postOfficeOptions.length > 0 ? (
                    postOfficeOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))
                  ) : (
                    <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                      {isLoadingPostOffices
                        ? 'Loading post offices...'
                        : 'No post offices available.'}
                    </p>
                  )}
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-post-office-staff-id'>
                Courier staff
              </Label>
              <Select
                value={formValues.postOfficeStaffId.trim() || NONE_VALUE}
                onValueChange={(value) =>
                  onUpdateField(
                    'postOfficeStaffId',
                    value === NONE_VALUE ? '' : value
                  )
                }
                disabled={
                  isSaving ||
                  !formValues.postOfficeId.trim() ||
                  isLoadingCouriers
                }
              >
                <SelectTrigger
                  id='vehicle-post-office-staff-id'
                  className='w-full'
                >
                  <SelectValue
                    placeholder={
                      !formValues.postOfficeId.trim()
                        ? 'Select post office first'
                        : isLoadingCouriers
                          ? 'Loading couriers...'
                          : 'Select courier'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={NONE_VALUE}>Not assigned</SelectItem>
                  {formValues.postOfficeId.trim() ? (
                    courierOptions.length > 0 ? (
                      courierOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))
                    ) : (
                      <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                        {isLoadingCouriers
                          ? 'Loading couriers...'
                          : 'No couriers available for this post office.'}
                      </p>
                    )
                  ) : (
                    <p className='px-2 py-1.5 text-sm text-muted-foreground'>
                      Select post office first.
                    </p>
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>

          <p className='text-xs text-muted-foreground'>
            For manager role, available post offices and couriers are limited to
            your managed scope.
          </p>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              onClick={() => onOpenChange(false)}
              disabled={isSaving}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSaving}>
              {isSaving && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              {formMode === 'create' ? 'Create' : 'Save changes'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
