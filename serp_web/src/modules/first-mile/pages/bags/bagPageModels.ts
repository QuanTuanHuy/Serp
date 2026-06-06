/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bagging page models
 */

import type {
  CreateSecondMileBagRequest,
  Hub,
  SecondMileBag,
  SecondMileBagCapacitySettings,
  SecondMileBagDestinationType,
  SecondMileBagStatus,
  SecondMileVehicle,
} from '../../types';

export interface BagFormValues {
  bagCode: string;
  originHubId: string;
  destinationType: SecondMileBagDestinationType;
  destinationHubId: string;
  destinationPostOfficeCode: string;
  vehicleId: string;
  maxWeight: string;
  maxVolume: string;
  maxOrders: string;
  note: string;
}

export interface AutoBaggingFormValues {
  originHubId: string;
  destinationType: SecondMileBagDestinationType;
  destinationHubId: string;
  destinationPostOfficeCode: string;
  orderCodes: string[];
}

export const BAG_STATUS_LABELS = {
  CREATED: 'Open',
  SEALED: 'Sealed',
  IN_TRANSIT: 'In transit',
  ARRIVED: 'Arrived',
  CANCELLED: 'Cancelled',
} satisfies Record<SecondMileBagStatus, string>;

export const BAG_STATUS_OPTIONS: Array<{
  value: SecondMileBagStatus;
  label: string;
}> = [
  { value: 'CREATED', label: 'Open' },
  { value: 'SEALED', label: 'Sealed' },
  { value: 'IN_TRANSIT', label: 'In transit' },
  { value: 'ARRIVED', label: 'Arrived' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

export const BAG_DESTINATION_TYPE_OPTIONS: Array<{
  value: SecondMileBagDestinationType;
  label: string;
}> = [
  { value: 'HUB', label: 'Hub' },
  { value: 'POST_OFFICE', label: 'Post office' },
];

export const emptyBagFormValues: BagFormValues = {
  bagCode: '',
  originHubId: '',
  destinationType: 'HUB',
  destinationHubId: '',
  destinationPostOfficeCode: '',
  vehicleId: '',
  maxWeight: '',
  maxVolume: '',
  maxOrders: '',
  note: '',
};

export const emptyAutoBaggingFormValues: AutoBaggingFormValues = {
  originHubId: '',
  destinationType: 'HUB',
  destinationHubId: '',
  destinationPostOfficeCode: '',
  orderCodes: [],
};

export const canViewBags = (roles: string[]) =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE');

export const canManageBags = (roles: string[]) =>
  roles.includes('TMS_ADMIN') || roles.includes('TMS_HUB_MANAGER');

export const canOperateBagOrders = (roles: string[]) =>
  canManageBags(roles) || roles.includes('TMS_HUB_EMPLOYEE');

export const normalizeOrderCode = (value: string) => value.trim().toUpperCase();

export const parseOrderCodes = (value: string) => {
  const seen = new Set<string>();
  return value
    .split(/[\s,;]+/)
    .map(normalizeOrderCode)
    .filter((code) => {
      if (!code || seen.has(code)) {
        return false;
      }
      seen.add(code);
      return true;
    });
};

export const toBagFormValues = (bag?: SecondMileBag): BagFormValues => {
  if (!bag) {
    return { ...emptyBagFormValues };
  }

  return {
    bagCode: bag.bagCode ?? '',
    originHubId: bag.originHubId ? String(bag.originHubId) : '',
    destinationType: bag.destinationType ?? 'HUB',
    destinationHubId: bag.destinationHubId ? String(bag.destinationHubId) : '',
    destinationPostOfficeCode: bag.destinationPostOfficeCode ?? '',
    vehicleId: bag.vehicleId ? String(bag.vehicleId) : '',
    maxWeight: bag.maxWeight ? String(bag.maxWeight) : '',
    maxVolume: bag.maxVolume ? String(bag.maxVolume) : '',
    maxOrders: bag.maxOrders ? String(bag.maxOrders) : '',
    note: bag.note ?? '',
  };
};

export const toDefaultedBagFormValues = (
  settings?: SecondMileBagCapacitySettings
): BagFormValues => ({
  ...emptyBagFormValues,
  maxWeight: settings?.maxWeight ? String(settings.maxWeight) : '',
  maxVolume: settings?.maxVolume ? String(settings.maxVolume) : '',
  maxOrders: settings?.maxOrders ? String(settings.maxOrders) : '',
});

export const validateBagForm = (values: BagFormValues): string | null => {
  if (!values.bagCode.trim()) {
    return 'Bag code is required.';
  }
  if (!values.originHubId) {
    return 'Origin hub is required.';
  }
  if (values.destinationType === 'HUB') {
    if (!values.destinationHubId) {
      return 'Destination hub is required.';
    }
    if (values.originHubId === values.destinationHubId) {
      return 'Use a post office destination for same-hub bags.';
    }
  }
  if (
    values.destinationType === 'POST_OFFICE' &&
    !values.destinationPostOfficeCode.trim()
  ) {
    return 'Destination post office code is required.';
  }

  if (!isPositiveOptionalNumber(values.maxWeight)) {
    return 'Max weight must be greater than zero.';
  }
  if (!isPositiveOptionalNumber(values.maxVolume)) {
    return 'Max volume must be greater than zero.';
  }
  if (!isPositiveOptionalInteger(values.maxOrders)) {
    return 'Max orders must be a positive integer.';
  }

  return null;
};

export const validateAutoBaggingForm = (
  values: AutoBaggingFormValues
): string | null => {
  if (!values.originHubId) {
    return 'Origin hub is required.';
  }
  if (values.destinationType === 'HUB') {
    if (!values.destinationHubId) {
      return 'Destination hub is required.';
    }
    if (values.originHubId === values.destinationHubId) {
      return 'Use a post office destination for same-hub bags.';
    }
  }
  if (
    values.destinationType === 'POST_OFFICE' &&
    !values.destinationPostOfficeCode.trim()
  ) {
    return 'Destination post office code is required.';
  }
  if (values.orderCodes.filter((code) => code.trim()).length === 0) {
    return 'Select at least one order.';
  }
  return null;
};

export const buildBagRequest = (
  values: BagFormValues
): CreateSecondMileBagRequest => {
  const request: CreateSecondMileBagRequest = {
    bag_code: values.bagCode.trim().toUpperCase(),
    origin_hub_id: Number(values.originHubId),
    destination_type: values.destinationType,
    status: 'CREATED',
  };

  if (values.destinationType === 'HUB') {
    request.destination_hub_id = Number(values.destinationHubId);
  } else {
    request.destination_post_office_code = values.destinationPostOfficeCode
      .trim()
      .toUpperCase();
  }

  if (values.vehicleId) {
    request.vehicle_id = Number(values.vehicleId);
  }
  if (values.maxWeight.trim()) {
    request.max_weight = Number(values.maxWeight);
  }
  if (values.maxVolume.trim()) {
    request.max_volume = Number(values.maxVolume);
  }
  if (values.maxOrders.trim()) {
    request.max_orders = Number(values.maxOrders);
  }
  if (values.note.trim()) {
    request.note = values.note.trim();
  }

  return request;
};

export const getBagStatusLabel = (status?: SecondMileBagStatus) =>
  status ? BAG_STATUS_LABELS[status] : 'Unknown';

export const getBagStatusVariant = (
  status?: SecondMileBagStatus
): 'default' | 'secondary' | 'destructive' | 'outline' => {
  if (status === 'CREATED') {
    return 'default';
  }
  if (status === 'SEALED') {
    return 'secondary';
  }
  if (status === 'CANCELLED') {
    return 'destructive';
  }
  return 'outline';
};

export const formatDateTime = (value?: string) => {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('en-US');
};

export const formatNumber = (value?: number, fractionDigits = 2) =>
  (value ?? 0).toLocaleString('en-US', {
    maximumFractionDigits: fractionDigits,
  });

export const formatPercent = (value?: number) =>
  `${formatNumber((value ?? 0) * 100, 1)}%`;

export const getHubLabel = (hubs: Hub[], hubId?: number) => {
  if (!hubId) {
    return '-';
  }
  const hub = hubs.find((item) => item.id === hubId);
  if (!hub) {
    return `Hub #${hubId}`;
  }
  return `${hub.code} - ${hub.name}`;
};

export const getVehicleLabel = (
  vehicles: SecondMileVehicle[],
  vehicleId?: number
) => {
  if (!vehicleId) {
    return '-';
  }
  const vehicle = vehicles.find((item) => item.id === vehicleId);
  if (!vehicle) {
    return `Vehicle #${vehicleId}`;
  }
  return vehicle.licensePlate;
};

export const getDestinationLabel = (bag: SecondMileBag, hubs: Hub[]) => {
  if (bag.destinationType === 'HUB') {
    return getHubLabel(hubs, bag.destinationHubId);
  }
  if (bag.destinationPostOfficeCode) {
    return `Post office ${bag.destinationPostOfficeCode}`;
  }
  return '-';
};

export const toLocalDateTimeInputValue = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const isPositiveOptionalNumber = (value: string) => {
  if (!value.trim()) {
    return true;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0;
};

const isPositiveOptionalInteger = (value: string) => {
  if (!value.trim()) {
    return true;
  }
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0;
};
