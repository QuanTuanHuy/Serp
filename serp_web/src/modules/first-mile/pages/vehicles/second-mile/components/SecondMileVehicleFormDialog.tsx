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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type {
  SecondMileVehicleStatus,
  SecondMileVehicleType,
} from '../../../../types';
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

interface SecondMileVehicleFormDialogProps {
  open: boolean;
  formMode: VehicleFormMode;
  formValues: VehicleFormState;
  isSaving: boolean;
  hubOptions: VehicleSelectOption[];
  isLoadingHubs: boolean;
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
  onOpenChange,
  onSubmit,
  onUpdateField,
}) => (
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
            <Select
              value={formValues.status}
              onValueChange={(value) =>
                onUpdateField('status', value as SecondMileVehicleStatus)
              }
              disabled={isSaving}
            >
              <SelectTrigger id='sm-vehicle-status' className='w-full'>
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
            <Label htmlFor='sm-vehicle-type'>Vehicle type *</Label>
            <Select
              value={formValues.vehicleType}
              onValueChange={(value) =>
                onUpdateField('vehicleType', value as SecondMileVehicleType)
              }
              disabled={isSaving}
            >
              <SelectTrigger id='sm-vehicle-type' className='w-full'>
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
            <Label htmlFor='sm-vehicle-max-bags'>Max bags *</Label>
            <Input
              id='sm-vehicle-max-bags'
              type='number'
              min={0}
              step={1}
              value={formValues.maxBags}
              onChange={(event) => onUpdateField('maxBags', event.target.value)}
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
            <Select
              value={formValues.hubId.trim() || NONE_VALUE}
              onValueChange={(value) =>
                onUpdateField('hubId', value === NONE_VALUE ? '' : value)
              }
              disabled={isSaving || isLoadingHubs}
            >
              <SelectTrigger id='sm-vehicle-hub' className='w-full'>
                <SelectValue
                  placeholder={
                    isLoadingHubs ? 'Loading hubs...' : 'Select hub'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={NONE_VALUE} disabled>
                  Select hub
                </SelectItem>
                {hubOptions.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2 sm:col-span-2'>
            <Label htmlFor='sm-vehicle-driver-id'>Driver staff ID</Label>
            <Input
              id='sm-vehicle-driver-id'
              type='number'
              min={1}
              step={1}
              value={formValues.assignedStaffId}
              onChange={(event) =>
                onUpdateField('assignedStaffId', event.target.value)
              }
              disabled={isSaving}
              placeholder='Optional — hub staff with DRIVER role'
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
