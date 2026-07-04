/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution planning tab
 */

import { ClipboardList, Loader2, Play, Sparkles } from 'lucide-react';

import {
  TmsCombobox,
  type TmsComboboxOption,
} from '@/modules/first-mile/components';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Textarea } from '@/shared/components/ui/textarea';

import type {
  BagDistributionPlan,
  Hub,
  PostOffice,
  SecondMileBag,
  SecondMileBagDestinationType,
  SecondMileRoute,
  SecondMileVehicle,
} from '../../../types';
import type {
  PlanningState,
  SelectedBagSummary,
} from '../bagDistributionModels';
import {
  destinationOptions,
  formatNumber,
  parseNumber,
} from '../bagDistributionModels';
import { PlanResultCard } from './PlanResultCard';
import { SummaryItem } from './SummaryItem';

interface PlanningTabProps {
  planning: PlanningState;
  canManage: boolean;
  hubs: Hub[];
  postOffices: PostOffice[];
  routes: SecondMileRoute[];
  vehicles: SecondMileVehicle[];
  hubOptions: TmsComboboxOption[];
  postOfficeOptions: TmsComboboxOption[];
  routeOptions: TmsComboboxOption[];
  vehicleOptions: TmsComboboxOption[];
  isFetchingHubs: boolean;
  isFetchingPostOffices: boolean;
  isFetchingRoutes: boolean;
  isFetchingVehicles: boolean;
  selectedBags: SecondMileBag[];
  selectedDestinationSummary: SelectedBagSummary | null;
  planResult: BagDistributionPlan | null;
  isPlanning: boolean;
  isCreatingManifest: boolean;
  onUpdatePlanning: <K extends keyof PlanningState>(
    key: K,
    value: PlanningState[K]
  ) => void;
  onAutoPlan: (execute: boolean) => void;
  onCreateManual: () => void;
}

export function PlanningTab({
  planning,
  canManage,
  hubs,
  postOffices,
  routes,
  vehicles,
  hubOptions,
  postOfficeOptions,
  routeOptions,
  vehicleOptions,
  isFetchingHubs,
  isFetchingPostOffices,
  isFetchingRoutes,
  isFetchingVehicles,
  selectedBags,
  selectedDestinationSummary,
  planResult,
  isPlanning,
  isCreatingManifest,
  onUpdatePlanning,
  onAutoPlan,
  onCreateManual,
}: PlanningTabProps) {
  const originHub = hubs.find((hub) => hub.id === planning.originHubId);
  const destinationHub = hubs.find(
    (hub) => hub.id === planning.destinationHubId
  );
  const destinationPostOffice = postOffices.find(
    (postOffice) => postOffice.code === planning.destinationPostOfficeCode
  );
  const route = routes.find((item) => item.id === planning.routeId);
  const vehicle = vehicles.find((item) => item.id === planning.vehicleId);

  return (
    <div className='grid gap-4 xl:grid-cols-[minmax(0,430px)_1fr]'>
      <Card>
        <CardHeader>
          <CardTitle>Plan setup</CardTitle>
          <CardDescription>
            Choose one origin, one destination, and a time window. Auto-plan can
            suggest route runs; manual creation uses selected ready bags.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {!canManage && (
            <div className='rounded-md border bg-muted/40 p-3 text-sm text-muted-foreground'>
              You can review manifests, but creating plans requires TMS_ADMIN or
              TMS_HUB_MANAGER.
            </div>
          )}

          <div className='space-y-2'>
            <Label>Origin hub *</Label>
            <TmsCombobox
              value={planning.originHubId ? String(planning.originHubId) : ''}
              onValueChange={(value) =>
                onUpdatePlanning('originHubId', parseNumber(value))
              }
              options={hubOptions}
              placeholder={isFetchingHubs ? 'Loading hubs...' : 'Select hub'}
              emptyText='No active hubs found'
              loading={isFetchingHubs}
              clearable
            />
          </div>

          <div className='space-y-2'>
            <Label>Destination type *</Label>
            <Select
              value={planning.destinationType}
              onValueChange={(value) =>
                onUpdatePlanning(
                  'destinationType',
                  value as SecondMileBagDestinationType
                )
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {destinationOptions.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {planning.destinationType === 'HUB' ? (
            <div className='space-y-2'>
              <Label>Destination hub *</Label>
              <TmsCombobox
                value={
                  planning.destinationHubId
                    ? String(planning.destinationHubId)
                    : ''
                }
                onValueChange={(value) =>
                  onUpdatePlanning('destinationHubId', parseNumber(value))
                }
                options={hubOptions}
                placeholder='Select destination hub'
                emptyText='No active hubs found'
                loading={isFetchingHubs}
                clearable
              />
            </div>
          ) : (
            <div className='space-y-2'>
              <Label>Destination post office *</Label>
              <TmsCombobox
                value={planning.destinationPostOfficeCode ?? ''}
                onValueChange={(value) =>
                  onUpdatePlanning(
                    'destinationPostOfficeCode',
                    value || undefined
                  )
                }
                options={postOfficeOptions}
                placeholder={
                  isFetchingPostOffices
                    ? 'Loading post offices...'
                    : 'Select post office'
                }
                emptyText='No active post offices found'
                loading={isFetchingPostOffices}
                clearable
              />
            </div>
          )}

          <div className='grid gap-3 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='distribution-departure'>Departure *</Label>
              <Input
                id='distribution-departure'
                type='datetime-local'
                value={planning.plannedDepartureAt}
                onChange={(event) =>
                  onUpdatePlanning('plannedDepartureAt', event.target.value)
                }
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='distribution-arrival'>Arrival *</Label>
              <Input
                id='distribution-arrival'
                type='datetime-local'
                value={planning.plannedArrivalAt}
                onChange={(event) =>
                  onUpdatePlanning('plannedArrivalAt', event.target.value)
                }
              />
            </div>
          </div>

          <div className='grid gap-3 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label>Route</Label>
              <TmsCombobox
                value={planning.routeId ? String(planning.routeId) : ''}
                onValueChange={(value) =>
                  onUpdatePlanning('routeId', parseNumber(value))
                }
                options={routeOptions}
                placeholder={
                  isFetchingRoutes ? 'Loading routes...' : 'Select route'
                }
                emptyText='No active route for this lane'
                loading={isFetchingRoutes}
                clearable
              />
            </div>
            <div className='space-y-2'>
              <Label>Vehicle</Label>
              <TmsCombobox
                value={planning.vehicleId ? String(planning.vehicleId) : ''}
                onValueChange={(value) =>
                  onUpdatePlanning('vehicleId', parseNumber(value))
                }
                options={vehicleOptions}
                placeholder={
                  isFetchingVehicles ? 'Loading vehicles...' : 'Select vehicle'
                }
                emptyText='No active vehicles at origin hub'
                loading={isFetchingVehicles}
                clearable
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='sealed-sla-hours'>Sealed SLA hours</Label>
            <Input
              id='sealed-sla-hours'
              type='number'
              min={1}
              value={planning.sealedSlaHours}
              onChange={(event) =>
                onUpdatePlanning('sealedSlaHours', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='distribution-note'>Note</Label>
            <Textarea
              id='distribution-note'
              value={planning.note}
              onChange={(event) => onUpdatePlanning('note', event.target.value)}
              placeholder='Optional instruction for driver or destination staff'
              rows={3}
            />
          </div>

          <div className='flex flex-col gap-2 sm:flex-row'>
            <Button
              disabled={!canManage || isPlanning}
              onClick={() => onAutoPlan(false)}
            >
              {isPlanning ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : (
                <Sparkles className='h-4 w-4' />
              )}
              Preview auto plan
            </Button>
            <Button
              variant='secondary'
              disabled={!canManage || isPlanning}
              onClick={() => onAutoPlan(true)}
            >
              <Play className='h-4 w-4' />
              Execute auto plan
            </Button>
            <Button
              variant='outline'
              disabled={!canManage || isCreatingManifest}
              onClick={onCreateManual}
            >
              {isCreatingManifest ? (
                <Loader2 className='h-4 w-4 animate-spin' />
              ) : (
                <ClipboardList className='h-4 w-4' />
              )}
              Create manual manifest
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className='space-y-4'>
        <Card>
          <CardHeader>
            <CardTitle>Plan summary</CardTitle>
            <CardDescription>
              Review lane, resource, and selected-bag totals before creating a
              manifest.
            </CardDescription>
          </CardHeader>
          <CardContent className='grid gap-3 md:grid-cols-2'>
            <SummaryItem label='Origin' value={originHub?.code ?? '-'} />
            <SummaryItem
              label='Destination'
              value={
                planning.destinationType === 'HUB'
                  ? (destinationHub?.code ?? '-')
                  : (destinationPostOffice?.code ?? '-')
              }
            />
            <SummaryItem label='Route' value={route?.routeCode ?? '-'} />
            <SummaryItem label='Vehicle' value={vehicle?.licensePlate ?? '-'} />
            <SummaryItem
              label='Selected bags'
              value={formatNumber(selectedBags.length, 0)}
            />
            <SummaryItem
              label='Selected orders'
              value={formatNumber(selectedDestinationSummary?.totalOrders, 0)}
            />
            <SummaryItem
              label='Total weight'
              value={`${formatNumber(selectedDestinationSummary?.totalWeight)} kg`}
            />
            <SummaryItem
              label='Total volume'
              value={`${formatNumber(selectedDestinationSummary?.totalVolume)} m3`}
            />
          </CardContent>
        </Card>

        <PlanResultCard plan={planResult} />
      </div>
    </div>
  );
}
