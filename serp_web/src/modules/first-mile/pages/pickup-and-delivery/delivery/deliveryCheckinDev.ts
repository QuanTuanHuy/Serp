/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dev helpers for delivery check-in
 */

import type { DeliveryAssignedStop } from '../../../types';

const DEV_CHECKIN_STORAGE_KEY = 'serp.first-mile.delivery-dev-checkin';

export const isDeliveryCheckinDevFeatureAvailable = (): boolean =>
  process.env.NODE_ENV === 'development';

export const readDeliveryDevCheckinModePreference = (): boolean => {
  if (!isDeliveryCheckinDevFeatureAvailable()) {
    return false;
  }

  if (typeof window === 'undefined') {
    return false;
  }

  return sessionStorage.getItem(DEV_CHECKIN_STORAGE_KEY) === 'true';
};

export const writeDeliveryDevCheckinModePreference = (
  enabled: boolean
): void => {
  if (
    !isDeliveryCheckinDevFeatureAvailable() ||
    typeof window === 'undefined'
  ) {
    return;
  }

  sessionStorage.setItem(DEV_CHECKIN_STORAGE_KEY, enabled ? 'true' : 'false');
};

export const resolveValidDeliveryCheckinCoordinates = (
  order: DeliveryAssignedStop
): { latitude: number; longitude: number } | null => {
  const latitude = order.latitude;
  const longitude = order.longitude;

  if (
    latitude === undefined ||
    longitude === undefined ||
    !Number.isFinite(latitude) ||
    !Number.isFinite(longitude)
  ) {
    return null;
  }

  return { latitude, longitude };
};
