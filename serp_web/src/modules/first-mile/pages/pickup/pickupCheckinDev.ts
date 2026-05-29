/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dev helpers for pickup check-in
 */

import type { PickupTrackingOrder } from '../../types';

const DEV_CHECKIN_STORAGE_KEY = 'serp.first-mile.pickup-dev-checkin';

export const isPickupCheckinDevFeatureAvailable = (): boolean =>
  process.env.NODE_ENV === 'development';

export const readDevCheckinModePreference = (): boolean => {
  if (!isPickupCheckinDevFeatureAvailable()) {
    return false;
  }

  if (typeof window === 'undefined') {
    return false;
  }

  return sessionStorage.getItem(DEV_CHECKIN_STORAGE_KEY) === 'true';
};

export const writeDevCheckinModePreference = (enabled: boolean): void => {
  if (!isPickupCheckinDevFeatureAvailable() || typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(DEV_CHECKIN_STORAGE_KEY, enabled ? 'true' : 'false');
};

export const resolveValidCheckinCoordinates = (
  order: PickupTrackingOrder
): { latitude: number; longitude: number } | null => {
  const latitude = order.senderLatitude;
  const longitude = order.senderLongitude;

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return null;
  }

  return { latitude, longitude };
};
