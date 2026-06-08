/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher setup card
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Checkbox,
  Input,
  Label,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2 } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import type { PickupShift } from '../../../../types';
import type {
  DispatchOptimizationGoalOption,
  DispatchSetupCardProps,
  RoutingVehicleOption,
} from './types';

const SHIFT_OPTIONS: Array<{ value: PickupShift; label: string }> = [
  { value: 'MORNING', label: 'Morning (07:30 - 12:00)' },
  { value: 'AFTERNOON', label: 'Afternoon (13:30 - 18:00)' },
  { value: 'EVENING', label: 'Evening (18:30 - 22:00)' },
];

const ROUTING_VEHICLE_OPTIONS: Array<{
  value: RoutingVehicleOption;
  label: string;
}> = [
  { value: 'DEFAULT', label: 'Use backend default' },
  { value: 'CAR', label: 'Car' },
  { value: 'BIKE', label: 'Bike' },
  { value: 'TAXI', label: 'Taxi' },
  { value: 'TRUCK', label: 'Truck' },
  { value: 'HD', label: 'Heavy-duty' },
];

const OPTIMIZATION_GOAL_OPTIONS: Array<{
  value: DispatchOptimizationGoalOption;
  label: string;
}> = [
  { value: 'BALANCED', label: 'Balanced' },
  { value: 'ON_TIME_PRIORITY', label: 'Prioritize on-time pickup' },
  { value: 'COST_EFFICIENCY', label: 'Reduce travel cost' },
  { value: 'MAX_ASSIGNMENT', label: 'Maximize assigned orders' },
];

export const DispatchSetupCard: React.FC<DispatchSetupCardProps> = ({
  postOfficeOptions,
  courierOptions,
  selectedPostOfficeId,
  onPostOfficeChange,
  isLoadingPostOffices,
  isLoadingCouriers,
  shift,
  onShiftChange,
  tripDate,
  onTripDateChange,
  orderLimitInput,
  onOrderLimitInputChange,
  selectedAutoCourierIds,
  onToggleAutoCourier,
  onClearAutoCourierSelection,
  businessValues,
  businessHandlers,
  onPreviewPlan,
  onAutoAssign,
  isOptimizing,
  isAutoAssigning,
  activeAction,
  isTmsAdmin,
}) => {
  const postOfficeComboboxOptions = postOfficeOptions.map((postOffice) => ({
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Dispatch Setup</CardTitle>
        <CardDescription>
          Configure post office, shift, and business strategy before
          dispatching.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
          <div className='space-y-2'>
            <Label htmlFor='dispatch-post-office'>Post office</Label>
            <TmsCombobox
              id='dispatch-post-office'
              value={selectedPostOfficeId}
              onValueChange={onPostOfficeChange}
              options={postOfficeComboboxOptions}
              placeholder='Select post office'
              emptyText='No post offices found'
              disabled={isLoadingPostOffices}
              loading={isLoadingPostOffices}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-shift'>Shift</Label>
            <TmsCombobox
              id='dispatch-shift'
              value={shift}
              onValueChange={(value) => onShiftChange(value as PickupShift)}
              options={SHIFT_OPTIONS}
              placeholder='Select shift'
              emptyText='No shifts found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-trip-date'>Trip date</Label>
            <Input
              id='dispatch-trip-date'
              type='date'
              value={tripDate}
              onChange={(event) => onTripDateChange(event.target.value)}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-order-limit'>Order limit</Label>
            <Input
              id='dispatch-order-limit'
              value={orderLimitInput}
              onChange={(event) => onOrderLimitInputChange(event.target.value)}
              placeholder='Optional, e.g. 100'
            />
          </div>

          <div className='space-y-2 md:col-span-2 xl:col-span-4'>
            <div className='flex items-center justify-between gap-2'>
              <Label>Couriers for optimizer (optional)</Label>
              {selectedAutoCourierIds.length > 0 ? (
                <Button
                  type='button'
                  size='sm'
                  variant='outline'
                  onClick={onClearAutoCourierSelection}
                >
                  Clear courier filter
                </Button>
              ) : null}
            </div>

            {isLoadingCouriers ? (
              <p className='text-xs text-muted-foreground'>
                Loading couriers for selected post office...
              </p>
            ) : courierOptions.length > 0 ? (
              <div className='grid gap-2 md:grid-cols-2 xl:grid-cols-3'>
                {courierOptions.map((courier) => {
                  const checked = selectedAutoCourierIds.includes(courier.id);

                  return (
                    <label
                      key={courier.id}
                      className='flex items-start gap-2 rounded-md border p-2 text-sm'
                    >
                      <Checkbox
                        checked={checked}
                        onCheckedChange={(value) =>
                          onToggleAutoCourier(courier.id, Boolean(value))
                        }
                        aria-label={`Select courier ${courier.code}`}
                      />
                      <span className='leading-5'>
                        {courier.code} - {courier.fullName}
                      </span>
                    </label>
                  );
                })}
              </div>
            ) : (
              <p className='text-xs text-muted-foreground'>
                No active courier found for this post office.
              </p>
            )}

            <p className='text-xs text-muted-foreground'>
              Leave all unchecked to let backend use all eligible couriers.
            </p>
          </div>

          <div className='space-y-3 rounded-md border p-4 md:col-span-2 xl:col-span-4'>
            <div className='space-y-1'>
              <p className='text-sm font-semibold'>Dispatch strategy</p>
              <p className='text-xs text-muted-foreground'>
                Backend handles all algorithm parameters. Choose only business
                intent here.
              </p>
            </div>

            <div className='grid gap-3 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='dispatch-vehicle-profile'>
                  Vehicle profile
                </Label>
                <TmsCombobox
                  id='dispatch-vehicle-profile'
                  value={businessValues.vehicleOption}
                  onValueChange={(value) =>
                    businessHandlers.onVehicleOptionChange(
                      value as RoutingVehicleOption
                    )
                  }
                  options={ROUTING_VEHICLE_OPTIONS}
                  placeholder='Select vehicle profile'
                  emptyText='No vehicle profiles found'
                />
                <p className='text-xs text-muted-foreground'>
                  Optional. Leave as backend default in most scenarios.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-optimization-goal'>
                  Planning objective
                </Label>
                <TmsCombobox
                  id='dispatch-optimization-goal'
                  value={businessValues.optimizationGoal}
                  onValueChange={(value) =>
                    businessHandlers.onOptimizationGoalChange(
                      value as DispatchOptimizationGoalOption
                    )
                  }
                  options={OPTIMIZATION_GOAL_OPTIONS}
                  placeholder='Select planning objective'
                  emptyText='No objectives found'
                />
                <p className='text-xs text-muted-foreground'>
                  Define business priority: on-time, cost, or assignment rate.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className='flex flex-wrap gap-2'>
          <Button
            type='button'
            variant='outline'
            onClick={onPreviewPlan}
            disabled={isOptimizing || !selectedPostOfficeId}
          >
            {activeAction === 'preview' ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : null}
            Preview plan
          </Button>
          <Button
            type='button'
            onClick={onAutoAssign}
            disabled={isAutoAssigning || !selectedPostOfficeId}
          >
            {activeAction === 'auto' ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : (
              <CheckCircle2 className='mr-2 h-4 w-4' />
            )}
            Auto assign
          </Button>
        </div>

        {!isTmsAdmin ? (
          <p className='text-xs text-muted-foreground'>
            Manager scope is enforced by backend. You can dispatch only for post
            offices assigned to you.
          </p>
        ) : null}
      </CardContent>
    </Card>
  );
};
