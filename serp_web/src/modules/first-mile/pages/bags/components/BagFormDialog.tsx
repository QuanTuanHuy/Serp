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
import type { Hub, SecondMileRoute, SecondMileVehicle } from '../../../types';
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
  routes: SecondMileRoute[];
  vehicles: SecondMileVehicle[];
  destinationPostOfficeOptions: Array<{ value: string; label: string }>;
  isLoadingDestinationPostOffices: boolean;
  isLoadingRoutes: boolean;
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
  routes,
  vehicles,
  destinationPostOfficeOptions,
  isLoadingDestinationPostOffices,
  isLoadingRoutes,
  isSaving,
  onOpenChange,
  onSubmit,
  onUpdateField,
}: BagFormDialogProps) {
  const hubOptions = hubs.map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  }));
  const vehicleById = new Map(vehicles.map((vehicle) => [vehicle.id, vehicle]));
  const selectedRoute = routes.find(
    (route) => String(route.id) === values.routeId
  );
  const selectedVehicle = selectedRoute?.vehicleId
    ? vehicleById.get(selectedRoute.vehicleId)
    : undefined;
  const routeOptions = routes.map((route) => {
    const vehicle = route.vehicleId ? vehicleById.get(route.vehicleId) : null;
    const driver = vehicle?.assignedStaffFullName
      ? ` | ${vehicle.assignedStaffFullName}`
      : '';
    const vehicleLabel = vehicle?.licensePlate
      ? ` | ${vehicle.licensePlate}${driver}`
      : '';

    return {
      value: String(route.id),
      label: `${route.routeCode} - ${route.routeName}${vehicleLabel}`,
    };
  });
  const vehicleOptions = selectedVehicle
    ? [
        {
          value: String(selectedVehicle.id),
          label: selectedVehicle.assignedStaffFullName
            ? `${selectedVehicle.licensePlate} - ${selectedVehicle.assignedStaffFullName}`
            : selectedVehicle.licensePlate,
        },
      ]
    : [{ value: NONE_VALUE, label: 'Select a route first' }];

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
                  onUpdateField('destinationPostOfficeCode', '');
                  onUpdateField('routeId', '');
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
                onValueChange={(value) => {
                  onUpdateField(
                    'destinationType',
                    value as BagFormValues['destinationType']
                  );
                  onUpdateField('destinationHubId', '');
                  onUpdateField('destinationPostOfficeCode', '');
                  onUpdateField('routeId', '');
                  onUpdateField('vehicleId', '');
                }}
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
                  onValueChange={(value) => {
                    onUpdateField('destinationHubId', value);
                    onUpdateField('routeId', '');
                    onUpdateField('vehicleId', '');
                  }}
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
                  Destination post office *
                </Label>
                <TmsCombobox
                  id='bag-destination-post-office'
                  value={values.destinationPostOfficeCode}
                  onValueChange={(value) => {
                    onUpdateField('destinationPostOfficeCode', value);
                    onUpdateField('routeId', '');
                    onUpdateField('vehicleId', '');
                  }}
                  options={destinationPostOfficeOptions}
                  disabled={isSaving || isLoadingDestinationPostOffices}
                  loading={isLoadingDestinationPostOffices}
                  placeholder={
                    values.originHubId
                      ? 'Select destination post office'
                      : 'Select origin hub first'
                  }
                  emptyText='No mapped post offices found'
                />
              </div>
            )}

            <div className='space-y-2'>
              <Label htmlFor='bag-route'>Route *</Label>
              <TmsCombobox
                id='bag-route'
                value={values.routeId}
                onValueChange={(value) => {
                  const route = routes.find(
                    (item) => String(item.id) === value
                  );
                  onUpdateField('routeId', value);
                  onUpdateField(
                    'vehicleId',
                    route?.vehicleId ? String(route.vehicleId) : ''
                  );
                }}
                options={routeOptions}
                placeholder={
                  values.originHubId
                    ? 'Select route'
                    : 'Select origin and destination first'
                }
                emptyText='No matching routes found'
                disabled={
                  isSaving ||
                  isLoadingRoutes ||
                  !values.originHubId ||
                  (values.destinationType === 'HUB'
                    ? !values.destinationHubId
                    : !values.destinationPostOfficeCode)
                }
                loading={isLoadingRoutes}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='bag-vehicle'>Vehicle and driver</Label>
              <TmsCombobox
                id='bag-vehicle'
                value={values.vehicleId.trim() || NONE_VALUE}
                onValueChange={() => undefined}
                options={vehicleOptions}
                placeholder='Derived from route'
                emptyText='Select a route first'
                disabled
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
