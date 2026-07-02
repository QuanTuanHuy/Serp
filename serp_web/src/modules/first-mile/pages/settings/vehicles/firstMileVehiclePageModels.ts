/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle page models and helpers
 */

import type {
  CreateVehicleRequest,
  Vehicle,
  VehicleStatus,
  VehicleType,
} from '../../../types';

export const PAGE_SIZE = 20;
export const IMPORT_PREVIEW_LIMIT = 5;

export type VehicleAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_POST_OFFICES'
  | 'COURIER_READ_ONLY'
  | 'NO_ACCESS';

export type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline';
export type VehicleFormMode = 'create' | 'edit';

export interface VehicleFormState {
  licensePlate: string;
  maxWeight: string;
  maxVolume: string;
  postOfficeId: string;
  postOfficeStaffId: string;
  status: VehicleStatus;
  vehicleType: VehicleType;
}

export const VEHICLE_STATUS_OPTIONS: Array<{
  value: VehicleStatus;
  label: string;
}> = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'IN_USE', label: 'In use' },
  { value: 'FULL', label: 'Full' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'INACTIVE', label: 'Inactive' },
];

export const VEHICLE_TYPE_OPTIONS: Array<{
  value: VehicleType;
  label: string;
}> = [
  { value: 'BIKE', label: 'Bike' },
  { value: 'TRUCK', label: 'Truck' },
];

export const DEFAULT_VEHICLE_FORM: VehicleFormState = {
  licensePlate: '',
  maxWeight: '',
  maxVolume: '',
  postOfficeId: '',
  postOfficeStaffId: '',
  status: 'ACTIVE',
  vehicleType: 'BIKE',
};

export const resolveVehicleAccessScope = (
  roles: string[]
): VehicleAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_POST_OFFICES';
  }

  if (roles.includes('TMS_POSTOFFICER')) {
    return 'COURIER_READ_ONLY';
  }

  return 'NO_ACCESS';
};

export const getScopeBadgeLabel = (scope: VehicleAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'TMS admin';
    case 'MANAGER_POST_OFFICES':
      return 'Post office manager';
    case 'COURIER_READ_ONLY':
      return 'Courier';
    default:
      return 'No access';
  }
};

export const getScopeDescription = (scope: VehicleAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'You can view all vehicles in the system.';
    case 'MANAGER_POST_OFFICES':
      return 'You can view vehicles belonging to the post offices you manage.';
    case 'COURIER_READ_ONLY':
      return 'You can view vehicles but cannot create, update, delete, or import.';
    default:
      return 'Your current account does not have permission to access the vehicle list.';
  }
};

export const getStatusBadgeVariant = (status: VehicleStatus): BadgeVariant => {
  switch (status) {
    case 'ACTIVE':
      return 'default';
    case 'IN_USE':
    case 'FULL':
      return 'secondary';
    case 'MAINTENANCE':
      return 'outline';
    case 'INACTIVE':
      return 'destructive';
    default:
      return 'outline';
  }
};

export const formatStatusLabel = (status: VehicleStatus): string =>
  status.replaceAll('_', ' ');

export const formatVehicleType = (vehicleType: VehicleType): string =>
  vehicleType === 'TRUCK' ? 'Truck' : 'Bike';

export const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('en-US');
};

export const formatOptionalNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '--';
  }

  return String(value);
};

export const buildPostOfficeLabel = (vehicle: Vehicle): string => {
  if (!vehicle.postOfficeCode) {
    return 'Not assigned';
  }

  if (!vehicle.postOfficeName) {
    return vehicle.postOfficeCode;
  }

  return `${vehicle.postOfficeCode} - ${vehicle.postOfficeName}`;
};

export const parseOptionalNumber = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isFinite(parsedValue)) {
    return undefined;
  }

  return parsedValue;
};

export const parseOptionalPositiveInteger = (
  value: string
): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
    return undefined;
  }

  return parsedValue;
};

export const mapVehicleToFormState = (vehicle: Vehicle): VehicleFormState => {
  return {
    licensePlate: vehicle.licensePlate || '',
    maxWeight:
      vehicle.maxWeight === undefined || vehicle.maxWeight === null
        ? ''
        : String(vehicle.maxWeight),
    maxVolume:
      vehicle.maxVolume === undefined || vehicle.maxVolume === null
        ? ''
        : String(vehicle.maxVolume),
    postOfficeId:
      vehicle.postOfficeId === undefined || vehicle.postOfficeId === null
        ? ''
        : String(vehicle.postOfficeId),
    postOfficeStaffId:
      vehicle.postOfficeStaffId === undefined ||
      vehicle.postOfficeStaffId === null
        ? ''
        : String(vehicle.postOfficeStaffId),
    status: vehicle.status || 'ACTIVE',
    vehicleType: vehicle.vehicleType || 'BIKE',
  };
};

export const validateVehicleForm = (
  values: VehicleFormState
): string | null => {
  if (!values.licensePlate.trim()) {
    return 'License plate is required.';
  }

  if (values.maxWeight.trim()) {
    const parsedWeight = parseOptionalNumber(values.maxWeight);
    if (parsedWeight === undefined || parsedWeight <= 0) {
      return 'Max weight must be a number greater than 0.';
    }
  }

  if (values.maxVolume.trim()) {
    const parsedVolume = parseOptionalNumber(values.maxVolume);
    if (parsedVolume === undefined || parsedVolume <= 0) {
      return 'Max volume must be a number greater than 0.';
    }
  }

  if (
    values.postOfficeId.trim() &&
    parseOptionalPositiveInteger(values.postOfficeId) === undefined
  ) {
    return 'Post office ID must be a positive integer.';
  }

  if (
    values.postOfficeStaffId.trim() &&
    parseOptionalPositiveInteger(values.postOfficeStaffId) === undefined
  ) {
    return 'Courier staff ID must be a positive integer.';
  }

  return null;
};

export const buildVehicleRequest = (
  values: VehicleFormState
): CreateVehicleRequest => {
  const maxWeight = parseOptionalNumber(values.maxWeight);
  const maxVolume = parseOptionalNumber(values.maxVolume);
  const postOfficeId = parseOptionalPositiveInteger(values.postOfficeId);
  const postOfficeStaffId = parseOptionalPositiveInteger(
    values.postOfficeStaffId
  );

  return {
    license_plate: values.licensePlate.trim(),
    status: values.status,
    vehicle_type: values.vehicleType,
    ...(maxWeight !== undefined ? { max_weight: maxWeight } : {}),
    ...(maxVolume !== undefined ? { max_volume: maxVolume } : {}),
    ...(postOfficeId !== undefined ? { post_office_id: postOfficeId } : {}),
    ...(postOfficeStaffId !== undefined
      ? { post_office_staff_id: postOfficeStaffId }
      : {}),
  };
};
