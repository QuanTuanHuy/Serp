/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover trip card
 */

import {
  Clock,
  Loader2,
  MapPin,
  PackageCheck,
  Route,
  Truck,
} from 'lucide-react';

import { Badge, Button, Separator } from '@/shared/components/ui';
import type { HandoverManifest } from '../../../types';
import {
  formatDateTime,
  getActionLabel,
  getStepLabel,
  getTripStep,
} from '../handoverManifestModels';
import { DriverWorkflowStep } from './DriverWorkflowStep';
import { MetricItem } from './MetricItem';

interface DriverHandoverTripCardProps {
  isCurrentSubmitting: boolean;
  isSubmitting: boolean;
  manifest: HandoverManifest;
  onOpenCheckin: (manifest: HandoverManifest) => void;
}

export function DriverHandoverTripCard({
  isCurrentSubmitting,
  isSubmitting,
  manifest,
  onOpenCheckin,
}: DriverHandoverTripCardProps) {
  const step = getTripStep(manifest);

  return (
    <div className='space-y-4 rounded-md border p-4'>
      <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-2'>
          <div className='flex flex-wrap items-center gap-2'>
            <h2 className='text-lg font-semibold'>
              {manifest.manifestCode || `#${manifest.id}`}
            </h2>
            <Badge variant='outline'>{getStepLabel(manifest)}</Badge>
          </div>
          <div className='grid gap-2 text-sm text-muted-foreground sm:grid-cols-2'>
            <div className='flex items-center gap-2'>
              <PackageCheck className='h-4 w-4' />
              {manifest.originPostOfficeCode || '--'} to hub #
              {manifest.targetHubId ?? '--'}
            </div>
            <div className='flex items-center gap-2'>
              <Route className='h-4 w-4' />
              {manifest.routeCode || `Route #${manifest.routeId ?? '--'}`}
            </div>
            <div className='flex items-center gap-2'>
              <Truck className='h-4 w-4' />
              {manifest.vehicleLicensePlate ||
                `Vehicle #${manifest.vehicleId ?? '--'}`}
            </div>
            <div className='flex items-center gap-2'>
              <Clock className='h-4 w-4' />
              {formatDateTime(manifest.plannedDepartureAt)} to{' '}
              {formatDateTime(manifest.plannedArrivalAt)}
            </div>
          </div>
        </div>

        <Button
          onClick={() => onOpenCheckin(manifest)}
          disabled={isSubmitting || Boolean(manifest.driverEndCheckinAt)}
        >
          {isCurrentSubmitting ? (
            <Loader2 className='mr-2 h-4 w-4 animate-spin' />
          ) : (
            <MapPin className='mr-2 h-4 w-4' />
          )}
          {getActionLabel(manifest)}
        </Button>
      </div>

      <Separator />

      <div className='grid gap-3 md:grid-cols-4'>
        <DriverWorkflowStep label='Dispatched' done active={step === 'READY'} />
        <DriverWorkflowStep
          label='Departure check-in'
          done={Boolean(manifest.driverStartCheckinAt)}
          active={step === 'IN_TRANSIT'}
        />
        <DriverWorkflowStep
          label='Arrival check-in'
          done={Boolean(manifest.driverEndCheckinAt)}
          active={step === 'ARRIVED'}
        />
        <DriverWorkflowStep
          label='Hub inbound'
          done={manifest.status === 'INBOUND_CONFIRMED'}
          active={step === 'RECEIVED'}
        />
      </div>

      <div className='grid gap-3 sm:grid-cols-2'>
        <MetricItem
          label='Departure GPS'
          value={
            manifest.driverStartCheckinAt
              ? `${formatDateTime(manifest.driverStartCheckinAt)} (${Math.round(
                  manifest.driverStartDistanceM ?? 0
                )}m)`
              : 'Pending'
          }
        />
        <MetricItem
          label='Arrival GPS'
          value={
            manifest.driverEndCheckinAt
              ? `${formatDateTime(manifest.driverEndCheckinAt)} (${Math.round(
                  manifest.driverEndDistanceM ?? 0
                )}m)`
              : 'Pending'
          }
        />
      </div>
    </div>
  );
}
