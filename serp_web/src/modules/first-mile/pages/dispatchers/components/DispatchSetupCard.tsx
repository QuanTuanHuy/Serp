/**
 * Author: GitHub Copilot
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { CheckCircle2, Loader2, RefreshCw } from 'lucide-react';
import type { PickupShift } from '../../../types';
import type {
  BooleanMode,
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

const BOOLEAN_MODE_OPTIONS: Array<{ value: BooleanMode; label: string }> = [
  { value: 'default', label: 'Use backend default' },
  { value: 'true', label: 'Enabled' },
  { value: 'false', label: 'Disabled' },
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
  advancedValues,
  advancedHandlers,
  onPreviewPlan,
  onAutoAssign,
  onRefreshCandidateOrders,
  isOptimizing,
  isAutoAssigning,
  isFetchingOrders,
  activeAction,
  isTmsAdmin,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Dispatch Setup</CardTitle>
        <CardDescription>
          Configure post office, shift, and optimizer options before
          dispatching.
        </CardDescription>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
          <div className='space-y-2'>
            <Label htmlFor='dispatch-post-office'>Post office</Label>
            <Select
              value={selectedPostOfficeId || undefined}
              onValueChange={onPostOfficeChange}
              disabled={isLoadingPostOffices}
            >
              <SelectTrigger id='dispatch-post-office'>
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
          </div>

          <div className='space-y-2'>
            <Label htmlFor='dispatch-shift'>Shift</Label>
            <Select
              value={shift}
              onValueChange={(value) => onShiftChange(value as PickupShift)}
            >
              <SelectTrigger id='dispatch-shift'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {SHIFT_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
              <p className='text-sm font-semibold'>
                Advanced optimizer settings
              </p>
              <p className='text-xs text-muted-foreground'>
                Fine-tune route quality and speed. Leave input blank or choose
                &quot;Use backend default&quot; to keep standard behavior.
              </p>
            </div>

            <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
              <div className='space-y-2'>
                <Label htmlFor='dispatch-vehicle-profile'>
                  Vehicle profile
                </Label>
                <Select
                  value={advancedValues.vehicleOption}
                  onValueChange={(value) =>
                    advancedHandlers.onVehicleOptionChange(
                      value as RoutingVehicleOption
                    )
                  }
                >
                  <SelectTrigger id='dispatch-vehicle-profile'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ROUTING_VEHICLE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className='text-xs text-muted-foreground'>
                  Controls travel profile assumptions of the optimizer.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-average-speed'>
                  Average speed (km/h)
                </Label>
                <Input
                  id='dispatch-average-speed'
                  value={advancedValues.averageSpeedInput}
                  onChange={(event) =>
                    advancedHandlers.onAverageSpeedInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 25'
                />
                <p className='text-xs text-muted-foreground'>
                  Higher speed reduces estimated travel time.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-service-minutes'>
                  Service minutes per stop
                </Label>
                <Input
                  id='dispatch-service-minutes'
                  value={advancedValues.serviceMinutesPerStopInput}
                  onChange={(event) =>
                    advancedHandlers.onServiceMinutesPerStopInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 8'
                />
                <p className='text-xs text-muted-foreground'>
                  Time spent at each pickup point.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-max-iterations'>Max iterations</Label>
                <Input
                  id='dispatch-max-iterations'
                  value={advancedValues.maxIterationsInput}
                  onChange={(event) =>
                    advancedHandlers.onMaxIterationsInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 300'
                />
                <p className='text-xs text-muted-foreground'>
                  More iterations can improve quality but may run longer.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-max-runtime'>Max runtime (ms)</Label>
                <Input
                  id='dispatch-max-runtime'
                  value={advancedValues.maxRuntimeMillisInput}
                  onChange={(event) =>
                    advancedHandlers.onMaxRuntimeMillisInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 1500'
                />
                <p className='text-xs text-muted-foreground'>
                  Hard cap for optimization compute time.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-destroy-rate'>Destroy rate</Label>
                <Input
                  id='dispatch-destroy-rate'
                  value={advancedValues.destroyRateInput}
                  onChange={(event) =>
                    advancedHandlers.onDestroyRateInputChange(
                      event.target.value
                    )
                  }
                  placeholder='0.01 - 0.90, default 0.20'
                />
                <p className='text-xs text-muted-foreground'>
                  Percentage of route rebuilt each ALNS step.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-initial-temperature'>
                  Initial temperature
                </Label>
                <Input
                  id='dispatch-initial-temperature'
                  value={advancedValues.initialTemperatureInput}
                  onChange={(event) =>
                    advancedHandlers.onInitialTemperatureInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 50'
                />
                <p className='text-xs text-muted-foreground'>
                  Exploration level at start of the search.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-cooling-rate'>Cooling rate</Label>
                <Input
                  id='dispatch-cooling-rate'
                  value={advancedValues.coolingRateInput}
                  onChange={(event) =>
                    advancedHandlers.onCoolingRateInputChange(
                      event.target.value
                    )
                  }
                  placeholder='0.80 - 0.9999, default 0.995'
                />
                <p className='text-xs text-muted-foreground'>
                  How fast the search becomes less exploratory.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-distance-weight'>
                  Distance weight
                </Label>
                <Input
                  id='dispatch-distance-weight'
                  value={advancedValues.distanceWeightInput}
                  onChange={(event) =>
                    advancedHandlers.onDistanceWeightInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 1.0'
                />
                <p className='text-xs text-muted-foreground'>
                  Increases priority of shorter routes.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-lateness-weight'>
                  Lateness weight
                </Label>
                <Input
                  id='dispatch-lateness-weight'
                  value={advancedValues.latenessWeightInput}
                  onChange={(event) =>
                    advancedHandlers.onLatenessWeightInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 0.5'
                />
                <p className='text-xs text-muted-foreground'>
                  Penalizes arriving later than requested windows.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-unassigned-penalty'>
                  Unassigned penalty
                </Label>
                <Input
                  id='dispatch-unassigned-penalty'
                  value={advancedValues.unassignedPenaltyInput}
                  onChange={(event) =>
                    advancedHandlers.onUnassignedPenaltyInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 500'
                />
                <p className='text-xs text-muted-foreground'>
                  Higher value forces assigning more orders.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-used-route-penalty'>
                  Used route penalty
                </Label>
                <Input
                  id='dispatch-used-route-penalty'
                  value={advancedValues.usedRoutePenaltyInput}
                  onChange={(event) =>
                    advancedHandlers.onUsedRoutePenaltyInputChange(
                      event.target.value
                    )
                  }
                  placeholder='Default 3.0'
                />
                <p className='text-xs text-muted-foreground'>
                  Discourages creating too many active routes.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-allow-lateness'>Allow lateness</Label>
                <Select
                  value={advancedValues.allowLatenessMode}
                  onValueChange={(value) =>
                    advancedHandlers.onAllowLatenessModeChange(
                      value as BooleanMode
                    )
                  }
                >
                  <SelectTrigger id='dispatch-allow-lateness'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {BOOLEAN_MODE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className='text-xs text-muted-foreground'>
                  If disabled, optimizer avoids late arrivals.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-enforce-planning-end'>
                  Enforce planning end
                </Label>
                <Select
                  value={advancedValues.enforcePlanningEndMode}
                  onValueChange={(value) =>
                    advancedHandlers.onEnforcePlanningEndModeChange(
                      value as BooleanMode
                    )
                  }
                >
                  <SelectTrigger id='dispatch-enforce-planning-end'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {BOOLEAN_MODE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className='text-xs text-muted-foreground'>
                  If enabled, stops must finish within planning window.
                </p>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='dispatch-enforce-capacity'>
                  Enforce capacity
                </Label>
                <Select
                  value={advancedValues.enforceCapacityMode}
                  onValueChange={(value) =>
                    advancedHandlers.onEnforceCapacityModeChange(
                      value as BooleanMode
                    )
                  }
                >
                  <SelectTrigger id='dispatch-enforce-capacity'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {BOOLEAN_MODE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className='text-xs text-muted-foreground'>
                  If enabled, route load cannot exceed vehicle capacity.
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
          <Button
            type='button'
            variant='outline'
            onClick={onRefreshCandidateOrders}
            disabled={isFetchingOrders}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
            Refresh candidate orders
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
