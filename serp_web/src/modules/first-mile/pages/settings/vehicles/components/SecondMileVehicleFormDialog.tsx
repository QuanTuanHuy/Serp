/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle form dialog
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
import type {
  SecondMileVehicleStatus,
  SecondMileVehicleType,
} from '../../../../types';
import {
  VEHICLE_STATUS_OPTIONS,
  VEHICLE_TYPE_OPTIONS,
  type VehicleFormMode,
  type VehicleFormState,
} from '../secondMileVehiclePageModels';

type UpdateVehicleFormField = <K extends keyof VehicleFormState>(
  field: K,
  value: VehicleFormState[K]
) => void;

interface VehicleSelectOption {
  value: string;
  label: string;
}

const NONE_VALUE = '__NONE__';

interface SecondMileVehicleFormDialogProps {
  open: boolean;
  formMode: VehicleFormMode;
  formValues: VehicleFormState;
  isSaving: boolean;
  hubOptions: VehicleSelectOption[];
  isLoadingHubs: boolean;
  driverOptions: VehicleSelectOption[];
  isLoadingDrivers: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onUpdateField: UpdateVehicleFormField;
}

export const SecondMileVehicleFormDialog: React.FC<
  SecondMileVehicleFormDialogProps
> = ({
  open,
  formMode,
  formValues,
  isSaving,
  hubOptions,
  isLoadingHubs,
  driverOptions,
  isLoadingDrivers,
  onOpenChange,
  onSubmit,
  onUpdateField,
}) => {
  const driverComboboxOptions = [
    { value: NONE_VALUE, label: 'Not assigned' },
    ...driverOptions,
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>
            {formMode === 'create' ? 'Create vehicle' : 'Update vehicle'}
          </DialogTitle>
          <DialogDescription>
            Second-mile vehicle linked to a hub and optional driver staff.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-license-plate'>License plate *</Label>
              <Input
                id='sm-vehicle-license-plate'
                value={formValues.licensePlate}
                onChange={(event) =>
                  onUpdateField('licensePlate', event.target.value)
                }
                disabled={isSaving}
                placeholder='17B6-72685'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-status'>Status *</Label>
              <TmsCombobox
                id='sm-vehicle-status'
                value={formValues.status}
                onValueChange={(value) =>
                  onUpdateField('status', value as SecondMileVehicleStatus)
                }
                options={VEHICLE_STATUS_OPTIONS}
                placeholder='Select status'
                emptyText='No statuses found'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-type'>Vehicle type *</Label>
              <TmsCombobox
                id='sm-vehicle-type'
                value={formValues.vehicleType}
                onValueChange={(value) =>
                  onUpdateField('vehicleType', value as SecondMileVehicleType)
                }
                options={VEHICLE_TYPE_OPTIONS}
                placeholder='Select type'
                emptyText='No vehicle types found'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-max-bags'>Max bags *</Label>
              <Input
                id='sm-vehicle-max-bags'
                type='number'
                min={0}
                step={1}
                value={formValues.maxBags}
                onChange={(event) =>
                  onUpdateField('maxBags', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-max-weight'>Max weight (kg) *</Label>
              <Input
                id='sm-vehicle-max-weight'
                type='number'
                min={0}
                step='any'
                value={formValues.maxWeight}
                onChange={(event) =>
                  onUpdateField('maxWeight', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='sm-vehicle-max-volume'>Max volume (m³) *</Label>
              <Input
                id='sm-vehicle-max-volume'
                type='number'
                min={0}
                step='any'
                value={formValues.maxVolume}
                onChange={(event) =>
                  onUpdateField('maxVolume', event.target.value)
                }
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='sm-vehicle-hub'>Hub *</Label>
              <TmsCombobox
                id='sm-vehicle-hub'
                value={formValues.hubId.trim()}
                onValueChange={(value) => {
                  const nextHubId = value === NONE_VALUE ? '' : value;

                  onUpdateField('hubId', nextHubId);

                  if (nextHubId !== formValues.hubId) {
                    onUpdateField('assignedStaffId', '');
                  }
                }}
                options={hubOptions}
                placeholder={isLoadingHubs ? 'Loading hubs...' : 'Select hub'}
                emptyText={isLoadingHubs ? 'Loading hubs...' : 'No hubs found'}
                disabled={isSaving || isLoadingHubs}
                loading={isLoadingHubs}
              />
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='sm-vehicle-driver'>Driver</Label>
              <TmsCombobox
                id='sm-vehicle-driver'
                value={formValues.assignedStaffId.trim() || NONE_VALUE}
                onValueChange={(value) =>
                  onUpdateField(
                    'assignedStaffId',
                    value === NONE_VALUE ? '' : value
                  )
                }
                options={driverComboboxOptions}
                placeholder={
                  !formValues.hubId.trim()
                    ? 'Select hub first'
                    : isLoadingDrivers
                      ? 'Loading drivers...'
                      : 'Select driver'
                }
                emptyText={
                  !formValues.hubId.trim()
                    ? 'Select hub first.'
                    : isLoadingDrivers
                      ? 'Loading drivers...'
                      : 'No drivers assigned to this hub.'
                }
                disabled={
                  isSaving || !formValues.hubId.trim() || isLoadingDrivers
                }
                loading={isLoadingDrivers}
              />
            </div>
          </div>

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
