/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover check-in workflow
 */

'use client';

import * as React from 'react';
import {
  CheckCircle2,
  Clock,
  Loader2,
  MapPin,
  PackageCheck,
  RefreshCw,
  Route,
  ShieldAlert,
  Truck,
} from 'lucide-react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Separator,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useNotification } from '@/shared/hooks';
import {
  useDriverCheckinHandoverManifestEndMutation,
  useDriverCheckinHandoverManifestStartMutation,
  useGetHandoverManifestsQuery,
} from '../../api';
import type { HandoverManifest } from '../../types';

const PAGE_SIZE = 50;

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }
  return new Date(value).toLocaleString('en-US');
};

const getBrowserLocation = (): Promise<{
  latitude: number;
  longitude: number;
}> =>
  new Promise((resolve, reject) => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      reject(new Error('Geolocation is not available in this browser.'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) =>
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        }),
      (error) => reject(error),
      {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: 10000,
      }
    );
  });

const canUseDriverHandover = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE') ||
  roles.includes('TMS_HUB_DRIVER');

const getTripStep = (
  manifest: HandoverManifest
): 'READY' | 'IN_TRANSIT' | 'ARRIVED' | 'RECEIVED' => {
  if (manifest.status === 'INBOUND_CONFIRMED') {
    return 'RECEIVED';
  }
  if (manifest.driverEndCheckinAt) {
    return 'ARRIVED';
  }
  if (manifest.driverStartCheckinAt) {
    return 'IN_TRANSIT';
  }
  return 'READY';
};

const getStepLabel = (manifest: HandoverManifest): string => {
  const step = getTripStep(manifest);
  if (step === 'RECEIVED') return 'Received at hub';
  if (step === 'ARRIVED') return 'Arrival checked out';
  if (step === 'IN_TRANSIT') return 'In transit';
  return 'Ready for departure';
};

const getActionLabel = (manifest: HandoverManifest): string => {
  if (!manifest.driverStartCheckinAt) return 'Departure check-in';
  if (!manifest.driverEndCheckinAt) return 'Arrival check-out';
  return 'Completed';
};

function MetricItem({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <div className='space-y-1 rounded-md border p-3'>
      <div className='text-xs font-medium uppercase text-muted-foreground'>
        {label}
      </div>
      <div className='text-sm font-medium'>{value}</div>
    </div>
  );
}

function WorkflowStep({
  label,
  active,
  done,
}: {
  label: string;
  active?: boolean;
  done?: boolean;
}) {
  return (
    <div className='flex items-center gap-2'>
      <div
        className={cn(
          'flex h-7 w-7 items-center justify-center rounded-full border text-xs',
          done && 'border-primary bg-primary text-primary-foreground',
          active && !done && 'border-primary text-primary'
        )}
      >
        {done ? (
          <CheckCircle2 className='h-4 w-4' />
        ) : (
          <Clock className='h-4 w-4' />
        )}
      </div>
      <span
        className={cn(
          'text-sm text-muted-foreground',
          (active || done) && 'font-medium text-foreground'
        )}
      >
        {label}
      </span>
    </div>
  );
}

export function DriverHandoverPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canAccess = canUseDriverHandover(roles);
  const [activeManifestId, setActiveManifestId] = React.useState<number | null>(
    null
  );

  const { data, isFetching, refetch } = useGetHandoverManifestsQuery(
    {
      page: 0,
      size: PAGE_SIZE,
    },
    { skip: !canAccess }
  );
  const [driverCheckinStart, { isLoading: isCheckingInStart }] =
    useDriverCheckinHandoverManifestStartMutation();
  const [driverCheckinEnd, { isLoading: isCheckingInEnd }] =
    useDriverCheckinHandoverManifestEndMutation();

  const isSubmitting = isCheckingInStart || isCheckingInEnd;
  const manifests = data?.items ?? [];

  const activeTrips = manifests.filter(
    (manifest) =>
      manifest.status === 'OUTBOUND_CONFIRMED' && !manifest.driverEndCheckinAt
  );
  const completedTrips = manifests.filter(
    (manifest) =>
      manifest.driverEndCheckinAt || manifest.status === 'INBOUND_CONFIRMED'
  );

  const handleDriverAction = async (manifest: HandoverManifest) => {
    if (!manifest.id || manifest.driverEndCheckinAt) {
      return;
    }

    setActiveManifestId(manifest.id);
    try {
      const location = await getBrowserLocation();
      if (!manifest.driverStartCheckinAt) {
        await driverCheckinStart({
          manifestId: manifest.id,
          body: location,
        }).unwrap();
        notification.success('Departure check-in recorded.');
      } else {
        await driverCheckinEnd({
          manifestId: manifest.id,
          body: location,
        }).unwrap();
        notification.success('Arrival check-out recorded.');
      }
      void refetch();
    } catch (error) {
      notification.error('Driver handover action failed.', {
        description: getErrorMessage(error),
      });
    } finally {
      setActiveManifestId(null);
    }
  };

  if (!canAccess) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <ShieldAlert className='h-5 w-5' />
            Access denied
          </CardTitle>
          <CardDescription>
            Driver handover requires TMS hub driver or hub operation access.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>Driver Handover</h1>
          <p className='text-muted-foreground'>
            Check trips out from the origin post office and into the target hub.
          </p>
        </div>
        <Button variant='outline' onClick={() => void refetch()}>
          <RefreshCw
            className={cn('mr-2 h-4 w-4', isFetching && 'animate-spin')}
          />
          Refresh
        </Button>
      </div>

      <div className='grid gap-4 md:grid-cols-3'>
        <MetricItem label='Ready' value={activeTrips.length} />
        <MetricItem
          label='In transit'
          value={
            manifests.filter(
              (manifest) =>
                Boolean(manifest.driverStartCheckinAt) &&
                !manifest.driverEndCheckinAt
            ).length
          }
        />
        <MetricItem label='Completed' value={completedTrips.length} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Truck className='h-5 w-5' />
            Active trips
          </CardTitle>
          <CardDescription>
            Trips are available after the post office dispatches the handover
            manifest.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          {isFetching && manifests.length === 0 ? (
            <div className='flex items-center gap-2 text-sm text-muted-foreground'>
              <Loader2 className='h-4 w-4 animate-spin' />
              Loading driver handovers...
            </div>
          ) : activeTrips.length === 0 ? (
            <div className='rounded-md border border-dashed p-6 text-sm text-muted-foreground'>
              No dispatched handovers are waiting for driver action.
            </div>
          ) : (
            activeTrips.map((manifest) => {
              const step = getTripStep(manifest);
              const isCurrentSubmitting =
                isSubmitting && activeManifestId === manifest.id;

              return (
                <div
                  key={manifest.id}
                  className='space-y-4 rounded-md border p-4'
                >
                  <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
                    <div className='space-y-2'>
                      <div className='flex flex-wrap items-center gap-2'>
                        <h2 className='text-lg font-semibold'>
                          {manifest.manifestCode || `#${manifest.id}`}
                        </h2>
                        <Badge variant='outline'>
                          {getStepLabel(manifest)}
                        </Badge>
                      </div>
                      <div className='grid gap-2 text-sm text-muted-foreground sm:grid-cols-2'>
                        <div className='flex items-center gap-2'>
                          <PackageCheck className='h-4 w-4' />
                          {manifest.originPostOfficeCode || '--'} to hub #
                          {manifest.targetHubId ?? '--'}
                        </div>
                        <div className='flex items-center gap-2'>
                          <Route className='h-4 w-4' />
                          {manifest.routeCode ||
                            `Route #${manifest.routeId ?? '--'}`}
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
                      onClick={() => void handleDriverAction(manifest)}
                      disabled={isSubmitting}
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
                    <WorkflowStep
                      label='Dispatched'
                      done
                      active={step === 'READY'}
                    />
                    <WorkflowStep
                      label='Departure check-in'
                      done={Boolean(manifest.driverStartCheckinAt)}
                      active={step === 'IN_TRANSIT'}
                    />
                    <WorkflowStep
                      label='Arrival check-out'
                      done={Boolean(manifest.driverEndCheckinAt)}
                      active={step === 'ARRIVED'}
                    />
                    <WorkflowStep
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
                          ? `${formatDateTime(
                              manifest.driverStartCheckinAt
                            )} (${Math.round(
                              manifest.driverStartDistanceM ?? 0
                            )}m)`
                          : 'Pending'
                      }
                    />
                    <MetricItem
                      label='Arrival GPS'
                      value={
                        manifest.driverEndCheckinAt
                          ? `${formatDateTime(
                              manifest.driverEndCheckinAt
                            )} (${Math.round(
                              manifest.driverEndDistanceM ?? 0
                            )}m)`
                          : 'Pending'
                      }
                    />
                  </div>
                </div>
              );
            })
          )}
        </CardContent>
      </Card>
    </div>
  );
}
