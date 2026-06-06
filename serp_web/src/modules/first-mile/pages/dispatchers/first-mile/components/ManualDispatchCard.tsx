/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher manual dispatch card
 */

import React from 'react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type { PickupShift, Province, Ward } from '../../../../types';
import { OrderMultiSelect } from './OrderMultiSelect';
import type { ManualDispatchCardProps } from './types';

const MANUAL_SHIFT_OPTIONS: Array<{ value: PickupShift; label: string }> = [
  { value: 'MORNING', label: 'Morning (07:30 - 12:00)' },
  { value: 'AFTERNOON', label: 'Afternoon (13:30 - 18:00)' },
  { value: 'EVENING', label: 'Evening (18:30 - 22:00)' },
];

export const ManualDispatchCard: React.FC<ManualDispatchCardProps> = ({
  provinceOptions,
  wardOptions,
  selectedProvinceCode,
  onProvinceChange,
  selectedWardCode,
  onWardChange,
  isLoadingProvinces,
  isLoadingWards,
  postOfficeOptions,
  selectedPostOfficeId,
  onPostOfficeChange,
  isLoadingPostOffices,
  shift,
  onShiftChange,
  tripDate,
  onTripDateChange,
  courierOptions,
  selectedManualCourierId,
  onManualCourierIdChange,
  isLoadingCouriers,
  suggestedCouriers,
  onQuickPickCourier,
  isLoadingOrders,
  candidateOrders,
  selectedOrderIds,
  onOrderSelectionChange,
  onClearSelectedOrders,
  onManualAssign,
  isManualAssigning,
  activeAction,
}) => {
  const canSelectPostOffice = Boolean(selectedProvinceCode);
  const provinceComboboxOptions = provinceOptions.flatMap(
    (province: Province) =>
      province.provinceCode
        ? [
            {
              value: province.provinceCode,
              label: `${province.name} (${province.provinceCode})`,
            },
          ]
        : []
  );
  const wardComboboxOptions = [
    { value: 'ALL', label: 'All wards in province' },
    ...wardOptions.flatMap((ward: Ward) =>
      ward.wardCode
        ? [
            {
              value: ward.wardCode,
              label: `${ward.name} (${ward.wardCode})`,
            },
          ]
        : []
    ),
  ];
  const postOfficeComboboxOptions = postOfficeOptions.map((postOffice) => ({
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  }));
  const courierComboboxOptions = courierOptions.map((courier) => ({
    value: String(courier.id),
    label: `${courier.code} - ${courier.fullName}`,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Manual Dispatch</CardTitle>
        <CardDescription>
          Filter post office by location, then select orders and assign them to
          a courier. This section uses its own post office scope, separate from
          Dispatch Setup.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-3'>
          <div className='space-y-2'>
            <Label htmlFor='manual-province'>Province</Label>
            <TmsCombobox
              id='manual-province'
              value={selectedProvinceCode}
              onValueChange={onProvinceChange}
              options={provinceComboboxOptions}
              placeholder='Select province'
              emptyText='No provinces found'
              disabled={isLoadingProvinces}
              loading={isLoadingProvinces}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-ward'>Ward</Label>
            <TmsCombobox
              id='manual-ward'
              value={selectedWardCode}
              onValueChange={onWardChange}
              options={wardComboboxOptions}
              placeholder={
                selectedProvinceCode
                  ? 'Select ward (optional)'
                  : 'Select province first'
              }
              emptyText={isLoadingWards ? 'Loading wards...' : 'No wards found'}
              disabled={!selectedProvinceCode || isLoadingWards}
              loading={isLoadingWards}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-post-office'>Post office</Label>
            <TmsCombobox
              id='manual-post-office'
              value={selectedPostOfficeId}
              onValueChange={onPostOfficeChange}
              options={postOfficeComboboxOptions}
              placeholder='Select post office'
              emptyText='No post offices found'
              disabled={!canSelectPostOffice || isLoadingPostOffices}
              loading={isLoadingPostOffices}
            />
            {isLoadingPostOffices ? (
              <p className='text-xs text-muted-foreground'>
                Loading post offices...
              </p>
            ) : null}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-shift'>Shift</Label>
            <TmsCombobox
              id='manual-shift'
              value={shift}
              onValueChange={(value) => onShiftChange(value as PickupShift)}
              options={MANUAL_SHIFT_OPTIONS}
              placeholder='Select shift'
              emptyText='No shifts found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-trip-date'>Trip date</Label>
            <Input
              id='manual-trip-date'
              type='date'
              value={tripDate}
              onChange={(event) => onTripDateChange(event.target.value)}
            />
          </div>
        </div>

        <div className='grid gap-3 md:grid-cols-2'>
          <div className='space-y-2'>
            <Label htmlFor='manual-courier-select'>Courier</Label>
            <TmsCombobox
              id='manual-courier-select'
              value={selectedManualCourierId}
              onValueChange={onManualCourierIdChange}
              options={courierComboboxOptions}
              placeholder='Select courier for manual assignment'
              emptyText='No couriers found'
              disabled={!selectedPostOfficeId || isLoadingCouriers}
              loading={isLoadingCouriers}
            />
            {isLoadingCouriers ? (
              <p className='text-xs text-muted-foreground'>
                Loading couriers...
              </p>
            ) : null}
          </div>

          <div className='space-y-2'>
            <Label>Quick pick courier</Label>
            {suggestedCouriers.length > 0 ? (
              <div className='flex flex-wrap gap-2'>
                {suggestedCouriers.map((courier) => (
                  <Button
                    key={courier.id}
                    type='button'
                    variant='outline'
                    size='sm'
                    onClick={() => onQuickPickCourier(courier.id)}
                  >
                    {courier.label}
                  </Button>
                ))}
              </div>
            ) : (
              <p className='text-xs text-muted-foreground'>
                Run preview or auto assign in Dispatch Setup to get courier
                suggestions.
              </p>
            )}
          </div>
        </div>

        <div className='space-y-2'>
          <div className='flex flex-wrap items-center justify-between gap-2'>
            <Label htmlFor='manual-orders-select'>Orders</Label>
            <div className='flex items-center gap-2'>
              <Badge variant='secondary'>
                Selected: {selectedOrderIds.length}
              </Badge>
              {selectedOrderIds.length > 0 ? (
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  onClick={onClearSelectedOrders}
                >
                  Clear selection
                </Button>
              ) : null}
            </div>
          </div>
          <OrderMultiSelect
            orders={candidateOrders}
            selectedOrderIds={selectedOrderIds}
            onSelectionChange={onOrderSelectionChange}
            disabled={!selectedPostOfficeId}
            loading={isLoadingOrders}
            placeholder={
              selectedPostOfficeId
                ? 'Select orders to assign...'
                : 'Select post office first'
            }
          />
          {!isLoadingOrders && selectedPostOfficeId ? (
            <p className='text-xs text-muted-foreground'>
              {candidateOrders.length} candidate order(s) with CREATED or
              PICKUP_FAILED status.
            </p>
          ) : null}
        </div>

        <div className='pt-1'>
          <Button
            type='button'
            onClick={onManualAssign}
            disabled={
              isManualAssigning ||
              selectedOrderIds.length === 0 ||
              !selectedPostOfficeId
            }
          >
            {activeAction === 'manual' ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='mr-2 h-4 w-4' />
            )}
            Manual assign selected orders
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};
