/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dev helpers for transit check-in
 */

import type { BagDistributionManifest, Hub, PostOffice } from '../../../types';
import type { CheckinMode } from './transitModels';

const TRANSIT_DEV_CHECKIN_STORAGE_KEY = 'serp.first-mile.transit-dev-checkin';

export const isTransitCheckinDevFeatureAvailable = (): boolean =>
  process.env.NODE_ENV === 'development';

export const readTransitDevCheckinPreference = (): boolean => {
  if (!isTransitCheckinDevFeatureAvailable() || typeof window === 'undefined') {
    return false;
  }

  return sessionStorage.getItem(TRANSIT_DEV_CHECKIN_STORAGE_KEY) === 'true';
};

export const writeTransitDevCheckinPreference = (enabled: boolean): void => {
  if (!isTransitCheckinDevFeatureAvailable() || typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(
    TRANSIT_DEV_CHECKIN_STORAGE_KEY,
    enabled ? 'true' : 'false'
  );
};

export interface TransitDevCheckinCoordinates {
  latitude: number;
  longitude: number;
  locationLabel: string;
}

const hasValidCoordinate = (value?: number): value is number =>
  value !== undefined && Number.isFinite(value);

const getCoordinatePair = (
  latitude?: number,
  longitude?: number
): { latitude: number; longitude: number } | null => {
  if (hasValidCoordinate(latitude) && hasValidCoordinate(longitude)) {
    return { latitude, longitude };
  }

  return null;
};

const findHub = (
  hubs: Hub[],
  hubId?: number,
  hubCode?: string
): Hub | undefined =>
  hubs.find(
    (hub) =>
      (hubId !== undefined && hub.id === hubId) ||
      (hubCode !== undefined && hub.code === hubCode)
  );

const findPostOffice = (
  postOffices: PostOffice[],
  code?: string
): PostOffice | undefined =>
  postOffices.find((postOffice) => postOffice.code === code);

export const resolveTransitDevCheckinCoordinates = (
  manifest: BagDistributionManifest,
  mode: CheckinMode,
  hubs: Hub[],
  postOffices: PostOffice[] = []
): TransitDevCheckinCoordinates | null => {
  const manifestCoordinates =
    mode === 'start'
      ? {
          latitude: manifest.driverStartLatitude,
          longitude: manifest.driverStartLongitude,
          locationLabel: 'Check-in xuất phát',
        }
      : {
          latitude: manifest.driverEndLatitude,
          longitude: manifest.driverEndLongitude,
          locationLabel: 'Check-in đến nơi',
        };

  const manifestCoordinatePair = getCoordinatePair(
    manifestCoordinates.latitude,
    manifestCoordinates.longitude
  );

  if (manifestCoordinatePair) {
    return {
      latitude: manifestCoordinatePair.latitude,
      longitude: manifestCoordinatePair.longitude,
      locationLabel: manifestCoordinates.locationLabel,
    };
  }

  if (mode === 'start') {
    const hub = findHub(hubs, manifest.originHubId, manifest.originHubCode);
    const coordinatePair = getCoordinatePair(hub?.latitude, hub?.longitude);

    if (coordinatePair) {
      return {
        latitude: coordinatePair.latitude,
        longitude: coordinatePair.longitude,
        locationLabel: `Hub xuất phát ${hub?.code ?? ''} - ${hub?.name ?? ''}`,
      };
    }

    return null;
  }

  if (manifest.destinationType === 'POST_OFFICE') {
    const postOffice = findPostOffice(
      postOffices,
      manifest.destinationPostOfficeCode
    );
    const coordinatePair = getCoordinatePair(
      postOffice?.latitude,
      postOffice?.longitude
    );

    if (coordinatePair) {
      return {
        latitude: coordinatePair.latitude,
        longitude: coordinatePair.longitude,
        locationLabel: `Bưu cục đích ${postOffice?.code ?? ''} - ${postOffice?.name ?? ''}`,
      };
    }
  }

  const hub = findHub(
    hubs,
    manifest.destinationHubId,
    manifest.destinationHubCode
  );
  const coordinatePair = getCoordinatePair(hub?.latitude, hub?.longitude);

  if (coordinatePair) {
    return {
      latitude: coordinatePair.latitude,
      longitude: coordinatePair.longitude,
      locationLabel: `Hub nhận ${hub?.code ?? ''} - ${hub?.name ?? ''}`,
    };
  }

  return null;
};
