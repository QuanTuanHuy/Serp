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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2 } from 'lucide-react';
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
            <Select
              value={selectedProvinceCode || undefined}
              onValueChange={onProvinceChange}
              disabled={isLoadingProvinces}
            >
              <SelectTrigger id='manual-province'>
                <SelectValue placeholder='Select province' />
              </SelectTrigger>
              <SelectContent>
                {provinceOptions.map((province: Province) => (
                  <SelectItem
                    key={province.provinceCode}
                    value={province.provinceCode}
                  >
                    {province.name} ({province.provinceCode})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-ward'>Ward</Label>
            <Select
              value={selectedWardCode || undefined}
              onValueChange={onWardChange}
              disabled={!selectedProvinceCode || isLoadingWards}
            >
              <SelectTrigger id='manual-ward'>
                <SelectValue
                  placeholder={
                    selectedProvinceCode
                      ? 'Select ward (optional)'
                      : 'Select province first'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All wards in province</SelectItem>
                {wardOptions.map((ward: Ward) => (
                  <SelectItem key={ward.wardCode} value={ward.wardCode}>
                    {ward.name} ({ward.wardCode})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-post-office'>Post office</Label>
            <Select
              value={selectedPostOfficeId || undefined}
              onValueChange={onPostOfficeChange}
              disabled={!canSelectPostOffice || isLoadingPostOffices}
            >
              <SelectTrigger id='manual-post-office'>
                <SelectValue placeholder='Select post office' />
              </SelectTrigger>
              <SelectContent>
                {postOfficeOptions.map((postOffice) => (
                  <SelectItem key={postOffice.id} value={String(postOffice.id)}>
                    {postOffice.code} - {postOffice.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {isLoadingPostOffices ? (
              <p className='text-xs text-muted-foreground'>
                Loading post offices...
              </p>
            ) : null}
          </div>

          <div className='space-y-2'>
            <Label htmlFor='manual-shift'>Shift</Label>
            <Select
              value={shift}
              onValueChange={(value) => onShiftChange(value as PickupShift)}
            >
              <SelectTrigger id='manual-shift'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {MANUAL_SHIFT_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
            <Select
              value={selectedManualCourierId || undefined}
              onValueChange={onManualCourierIdChange}
              disabled={!selectedPostOfficeId || isLoadingCouriers}
            >
              <SelectTrigger id='manual-courier-select'>
                <SelectValue placeholder='Select courier for manual assignment' />
              </SelectTrigger>
              <SelectContent>
                {courierOptions.map((courier) => (
                  <SelectItem key={courier.id} value={String(courier.id)}>
                    {courier.code} - {courier.fullName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
