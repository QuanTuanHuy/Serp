/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Driver handover check-in workflow
 */

'use client';

import * as React from 'react';
import { RefreshCw, ShieldAlert } from 'lucide-react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Button,
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { useNotification } from '@/shared/hooks';
import {
  useDriverCheckinHandoverManifestEndMutation,
  useDriverCheckinHandoverManifestStartMutation,
  useGetHandoverManifestsQuery,
} from '../../api';
import type { HandoverManifest } from '../../types';
import {
  DriverCheckinDialog,
  DriverHandoverStats,
  DriverHandoverTripsCard,
} from './components';
import {
  buildCheckinFormData,
  canUseDriverHandover,
  DRIVER_PAGE_SIZE,
  getBrowserLocation,
  type DriverHandoverAction,
} from './handoverManifestModels';

export function DriverHandoverPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canAccess = canUseDriverHandover(roles);
  const [activeManifestId, setActiveManifestId] = React.useState<number | null>(
    null
  );
  const [checkinManifest, setCheckinManifest] =
    React.useState<HandoverManifest | null>(null);
  const [checkinPhoto, setCheckinPhoto] = React.useState<File | null>(null);
  const [checkinLatitude, setCheckinLatitude] = React.useState('');
  const [checkinLongitude, setCheckinLongitude] = React.useState('');
  const [isDevMode, setIsDevMode] = React.useState(false);

  const { data, isFetching, refetch } = useGetHandoverManifestsQuery(
    {
      page: 0,
      size: DRIVER_PAGE_SIZE,
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
    (manifest) => manifest.status === 'OUTBOUND_CONFIRMED'
  );
  const readyTrips = activeTrips.filter(
    (manifest) => !manifest.driverStartCheckinAt
  );
  const inTransitTrips = activeTrips.filter(
    (manifest) => manifest.driverStartCheckinAt && !manifest.driverEndCheckinAt
  );
  const arrivedTrips = activeTrips.filter(
    (manifest) => manifest.driverEndCheckinAt
  );
  const completedTrips = manifests.filter(
    (manifest) => manifest.status === 'INBOUND_CONFIRMED'
  );
  const checkinAction: DriverHandoverAction =
    checkinManifest?.driverStartCheckinAt ? 'END' : 'START';
  const checkinTitle =
    checkinAction === 'START' ? 'Departure check-in' : 'Arrival check-out';

  const applyValidCheckinCoordinates = React.useCallback(
    (manifest: HandoverManifest) => {
      if (manifest.driverStartCheckinAt) {
        if (
          typeof manifest.targetHubLatitude !== 'number' ||
          typeof manifest.targetHubLongitude !== 'number'
        ) {
          notification.error('Target hub coordinates are missing.');
          return;
        }
        setCheckinLatitude(manifest.targetHubLatitude.toFixed(6));
        setCheckinLongitude(manifest.targetHubLongitude.toFixed(6));
        return;
      }
      if (
        typeof manifest.originPostOfficeLatitude !== 'number' ||
        typeof manifest.originPostOfficeLongitude !== 'number'
      ) {
        notification.error('Origin post office coordinates are missing.');
        return;
      }
      setCheckinLatitude(manifest.originPostOfficeLatitude.toFixed(6));
      setCheckinLongitude(manifest.originPostOfficeLongitude.toFixed(6));
    },
    [notification]
  );

  React.useEffect(() => {
    if (isDevMode && checkinManifest) {
      applyValidCheckinCoordinates(checkinManifest);
    }
  }, [applyValidCheckinCoordinates, checkinManifest, isDevMode]);

  const resetCheckinDialog = React.useCallback(() => {
    setCheckinManifest(null);
    setCheckinPhoto(null);
    setCheckinLatitude('');
    setCheckinLongitude('');
  }, []);

  const handleOpenCheckinDialog = async (manifest: HandoverManifest) => {
    if (!manifest.id || manifest.driverEndCheckinAt) {
      return;
    }
    setCheckinManifest(manifest);
    setCheckinPhoto(null);
    setCheckinLatitude('');
    setCheckinLongitude('');
    if (isDevMode) {
      applyValidCheckinCoordinates(manifest);
    }
  };

  const handleUseCurrentLocation = async () => {
    try {
      const location = await getBrowserLocation();
      setCheckinLatitude(location.latitude.toFixed(6));
      setCheckinLongitude(location.longitude.toFixed(6));
    } catch (error) {
      notification.error('Unable to read current location.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleSubmitDriverAction = async () => {
    if (!checkinManifest?.id) {
      notification.error('Please select a handover manifest.');
      return;
    }
    if (!checkinPhoto) {
      notification.error('Please select a check-in photo.');
      return;
    }

    const latitude = Number(checkinLatitude);
    const longitude = Number(checkinLongitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Please provide a valid GPS location.');
      return;
    }

    const formData = buildCheckinFormData(latitude, longitude, checkinPhoto);
    setActiveManifestId(checkinManifest.id);
    try {
      if (!checkinManifest.driverStartCheckinAt) {
        await driverCheckinStart({
          manifestId: checkinManifest.id,
          formData,
        }).unwrap();
        notification.success('Departure check-in recorded.');
      } else {
        await driverCheckinEnd({
          manifestId: checkinManifest.id,
          formData,
        }).unwrap();
        notification.success('Arrival check-out recorded.');
      }
      resetCheckinDialog();
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

      <DriverHandoverStats
        readyCount={readyTrips.length}
        inTransitCount={inTransitTrips.length}
        arrivedCount={arrivedTrips.length}
        completedCount={completedTrips.length}
      />

      <DriverHandoverTripsCard
        activeManifestId={activeManifestId}
        activeTrips={activeTrips}
        isFetching={isFetching}
        isSubmitting={isSubmitting}
        manifestCount={manifests.length}
        onOpenCheckin={(manifest) => void handleOpenCheckinDialog(manifest)}
      />

      <DriverCheckinDialog
        checkinLatitude={checkinLatitude}
        checkinLongitude={checkinLongitude}
        checkinManifest={checkinManifest}
        checkinTitle={checkinTitle}
        isDevMode={isDevMode}
        isSubmitting={isSubmitting}
        onCancel={resetCheckinDialog}
        onDevModeChange={setIsDevMode}
        onLatitudeChange={setCheckinLatitude}
        onLongitudeChange={setCheckinLongitude}
        onOpenChange={(open) => {
          if (!open) {
            resetCheckinDialog();
          }
        }}
        onPhotoChange={setCheckinPhoto}
        onSubmit={() => void handleSubmitDriverAction()}
        onUseCurrentLocation={() => void handleUseCurrentLocation()}
      />
    </div>
  );
}
