/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dev helpers for transit check-in
 */

import type { BagDistributionManifest, Hub } from '../../../types';
import type { CheckinMode } from './transitModels';

const TRANSIT_DEV_CHECKIN_STORAGE_KEY =
  'serp.first-mile.transit-dev-checkin';

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

type HubWithCoordinate = Hub & {
  latitude: number;
  longitude: number;
};

const hasValidCoordinate = (hub?: Hub): hub is HubWithCoordinate =>
  hub?.latitude !== undefined &&
  hub.longitude !== undefined &&
  Number.isFinite(hub.latitude) &&
  Number.isFinite(hub.longitude);

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

export const resolveTransitDevCheckinCoordinates = (
  manifest: BagDistributionManifest,
  mode: CheckinMode,
  hubs: Hub[]
): TransitDevCheckinCoordinates | null => {
  const hub =
    mode === 'start'
      ? findHub(hubs, manifest.originHubId, manifest.originHubCode)
      : manifest.destinationType === 'HUB'
        ? findHub(hubs, manifest.destinationHubId, manifest.destinationHubCode)
        : undefined;

  if (!hasValidCoordinate(hub)) {
    return null;
  }

  return {
    latitude: hub.latitude,
    longitude: hub.longitude,
    locationLabel: `${mode === 'start' ? 'Hub xuất phát' : 'Hub nhận'} ${
      hub.code
    } - ${hub.name}`,
  };
};
