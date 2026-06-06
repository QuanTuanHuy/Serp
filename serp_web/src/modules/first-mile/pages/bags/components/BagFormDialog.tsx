/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag form dialog
 */

import type React from 'react';

import { Loader2 } from 'lucide-react';

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
  Textarea,
} from '@/shared/components';

import { TmsCombobox } from '../../../components/TmsCombobox';
import type { Hub, SecondMileVehicle } from '../../../types';
import {
  BAG_DESTINATION_TYPE_OPTIONS,
  type BagFormValues,
} from '../bagPageModels';

const NONE_VALUE = '__NONE__';

interface BagFormDialogProps {
  open: boolean;
  mode: 'create' | 'edit';
  values: BagFormValues;
  hubs: Hub[];
  vehicles: SecondMileVehicle[];
  isSaving: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (event: React.FormEvent) => void;
  onUpdateField: <K extends keyof BagFormValues>(
    field: K,
    value: BagFormValues[K]
  ) => void;
}

export function BagFormDialog({
  open,
  mode,
  values,
  hubs,
  vehicles,
  isSaving,
  onOpenChange,
  onSubmit,
  onUpdateField,
}: BagFormDialogProps) {
  const hubOptions = hubs.map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  }));
  const vehicleOptions = [
    { value: NONE_VALUE, label: 'No vehicle' },
    ...vehicles
      .filter(
        (vehicle) =>
          !values.originHubId || String(vehicle.hubId) === values.originHubId
      )
      .map((vehicle) => ({
        value: String(vehicle.id),
        label: vehicle.licensePlate,
      })),
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? 'New bag' : 'Edit bag'}
          </DialogTitle>
          <DialogDescription>
            Configure the bag destination and capacity before scanning orders.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className='space-y-4'>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='bag-code'>Bag code *</Label>
              <Input
                id='bag-code'
                value={values.bagCode}
                onChange={(event) =>
                  onUpdateField('bagCode', event.target.value)
                }
                disabled={isSaving}
                placeholder='BAG-001'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='bag-origin-hub'>Origin hub *</Label>
              <TmsCombobox
                id='bag-origin-hub'
                value={values.originHubId}
                onValueChange={(value) => {
                  onUpdateField('originHubId', value);
                  if (values.destinationHubId === value) {
                    onUpdateField('destinationHubId', '');
                  }
                  onUpdateField('vehicleId', '');
                }}
                options={hubOptions}
                placeholder='Select origin hub'
                emptyText='No hubs found'
                disabled={isSaving}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='bag-destination-type'>Destination type *</Label>
              <TmsCombobox
                id='bag-destination-type'
                value={values.destinationType}
                onValueChange={(value) =>
                  onUpdateField(
                    'destinationType',
                    value as BagFormValues['destinationType']
                  )
                }
                options={BAG_DESTINATION_TYPE_OPTIONS}
                placeholder='Select destination type'
                emptyText='No destination types found'
                disabled={isSaving}
              />
            </div>

            {values.destinationType === 'HUB' ? (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-hub'>Destination hub *</Label>
                <TmsCombobox
                  id='bag-destination-hub'
                  value={values.destinationHubId}
                  onValueChange={(value) =>
                    onUpdateField('destinationHubId', value)
                  }
                  options={hubOptions.filter(
                    (hub) => hub.value !== values.originHubId
                  )}
                  placeholder='Select destination hub'
                  emptyText='No destination hubs found'
                  disabled={isSaving}
                />
              </div>
            ) : (
              <div className='space-y-2'>
                <Label htmlFor='bag-destination-post-office'>
                  Destination post office code *
                </Label>
                <Input
                  id='bag-destination-post-office'
                  value={values.destinationPostOfficeCode}
                  onChange={(event) =>
                    onUpdateField(
                      'destinationPostOfficeCode',
                      event.target.value
                    )
                  }
                  disabled={isSaving}
                  placeholder='PO-DST-001'
                />
              </div>
            )}

            <div className='space-y-2'>
              <Label htmlFor='bag-vehicle'>Vehicle</Label>
              <TmsCombobox
                id='bag-vehicle'
                value={values.vehicleId.trim() || NONE_VALUE}
                onValueChange={(value) =>
                  onUpdateField('vehicleId', value === NONE_VALUE ? '' : value)
                }
                options={vehicleOptions}
                placeholder='Select vehicle'
                emptyText='No vehicles found'
                disabled={isSaving}
              />
            </div>

            <div className='grid grid-cols-3 gap-3'>
              <div className='space-y-2'>
                <Label htmlFor='bag-max-weight'>Max kg</Label>
                <Input
                  id='bag-max-weight'
                  type='number'
                  min='0'
                  step='0.01'
                  value={values.maxWeight}
                  onChange={(event) =>
                    onUpdateField('maxWeight', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-max-volume'>Max m3</Label>
                <Input
                  id='bag-max-volume'
                  type='number'
                  min='0'
                  step='0.01'
                  value={values.maxVolume}
                  onChange={(event) =>
                    onUpdateField('maxVolume', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='bag-max-orders'>Max orders</Label>
                <Input
                  id='bag-max-orders'
                  type='number'
                  min='1'
                  step='1'
                  value={values.maxOrders}
                  onChange={(event) =>
                    onUpdateField('maxOrders', event.target.value)
                  }
                  disabled={isSaving}
                />
              </div>
            </div>

            <div className='space-y-2 sm:col-span-2'>
              <Label htmlFor='bag-note'>Note</Label>
              <Textarea
                id='bag-note'
                value={values.note}
                onChange={(event) => onUpdateField('note', event.target.value)}
                disabled={isSaving}
                rows={3}
                placeholder='Operational note'
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type='button'
              variant='outline'
              disabled={isSaving}
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type='submit' disabled={isSaving}>
              {isSaving && <Loader2 className='h-4 w-4 animate-spin' />}
              {mode === 'create' ? 'Create bag' : 'Save changes'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
