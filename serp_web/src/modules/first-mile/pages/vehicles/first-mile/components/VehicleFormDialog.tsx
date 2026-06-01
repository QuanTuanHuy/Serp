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
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
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
  const postOfficeComboboxOptions = [
    { value: NONE_VALUE, label: 'Not assigned' },
    ...postOfficeOptions,
  ];
  const courierComboboxOptions = [
    { value: NONE_VALUE, label: 'Not assigned' },
    ...courierOptions,
  ];

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
              <TmsCombobox
                id='vehicle-status'
                value={formValues.status}
                onValueChange={(value) =>
                  onUpdateField('status', value as VehicleStatus)
                }
                options={VEHICLE_STATUS_OPTIONS}
                placeholder='Select status'
                emptyText='No statuses found'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-type'>Vehicle type *</Label>
              <TmsCombobox
                id='vehicle-type'
                value={formValues.vehicleType}
                onValueChange={(value) =>
                  onUpdateField('vehicleType', value as VehicleType)
                }
                options={VEHICLE_TYPE_OPTIONS}
                placeholder='Select type'
                emptyText='No vehicle types found'
                disabled={isSaving}
              />
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
              <TmsCombobox
                id='vehicle-post-office-id'
                value={formValues.postOfficeId.trim() || NONE_VALUE}
                onValueChange={(value) => {
                  const nextPostOfficeId = value === NONE_VALUE ? '' : value;

                  onUpdateField('postOfficeId', nextPostOfficeId);

                  if (nextPostOfficeId !== formValues.postOfficeId) {
                    onUpdateField('postOfficeStaffId', '');
                  }
                }}
                options={postOfficeComboboxOptions}
                placeholder={
                  isLoadingPostOffices
                    ? 'Loading post offices...'
                    : 'Select post office'
                }
                emptyText={
                  isLoadingPostOffices
                    ? 'Loading post offices...'
                    : 'No post offices available.'
                }
                disabled={isSaving || isLoadingPostOffices}
                loading={isLoadingPostOffices}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='vehicle-post-office-staff-id'>
                Courier staff
              </Label>
              <TmsCombobox
                id='vehicle-post-office-staff-id'
                value={formValues.postOfficeStaffId.trim() || NONE_VALUE}
                onValueChange={(value) =>
                  onUpdateField(
                    'postOfficeStaffId',
                    value === NONE_VALUE ? '' : value
                  )
                }
                options={courierComboboxOptions}
                placeholder={
                  !formValues.postOfficeId.trim()
                    ? 'Select post office first'
                    : isLoadingCouriers
                      ? 'Loading couriers...'
                      : 'Select courier'
                }
                emptyText={
                  !formValues.postOfficeId.trim()
                    ? 'Select post office first.'
                    : isLoadingCouriers
                      ? 'Loading couriers...'
                      : 'No couriers available for this post office.'
                }
                disabled={
                  isSaving ||
                  !formValues.postOfficeId.trim() ||
                  isLoadingCouriers
                }
                loading={isLoadingCouriers}
              />
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
