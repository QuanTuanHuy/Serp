/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile vehicle page models
 */

import type {
  Hub,
  SecondMileCreateVehicleRequest,
  SecondMileVehicle,
  SecondMileVehicleStatus,
  SecondMileVehicleType,
} from '../../../types';

export const PAGE_SIZE = 20;
export const IMPORT_PREVIEW_LIMIT = 5;

export type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline';
export type VehicleFormMode = 'create' | 'edit';

export interface VehicleFormState {
  licensePlate: string;
  maxBags: string;
  maxWeight: string;
  maxVolume: string;
  hubId: string;
  assignedStaffId: string;
  status: SecondMileVehicleStatus;
  vehicleType: SecondMileVehicleType;
}

export const VEHICLE_STATUS_OPTIONS: Array<{
  value: SecondMileVehicleStatus;
  label: string;
}> = [
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
  { value: 'MAINTENANCE', label: 'Bảo trì' },
];

export const VEHICLE_TYPE_OPTIONS: Array<{
  value: SecondMileVehicleType;
  label: string;
}> = [
  { value: 'TRUCK', label: 'Xe tải' },
  { value: 'VAN', label: 'Xe van' },
];

export const DEFAULT_VEHICLE_FORM: VehicleFormState = {
  licensePlate: '',
  maxBags: '0',
  maxWeight: '',
  maxVolume: '',
  hubId: '',
  assignedStaffId: '',
  status: 'ACTIVE',
  vehicleType: 'TRUCK',
};

export const getStatusBadgeVariant = (
  status: SecondMileVehicleStatus
): BadgeVariant => {
  switch (status) {
    case 'ACTIVE':
      return 'default';
    case 'MAINTENANCE':
      return 'outline';
    case 'INACTIVE':
      return 'destructive';
    default:
      return 'outline';
  }
};

export const formatStatusLabel = (status: SecondMileVehicleStatus): string =>
  VEHICLE_STATUS_OPTIONS.find((option) => option.value === status)?.label ??
  status.replaceAll('_', ' ');

export const formatVehicleType = (
  vehicleType: SecondMileVehicleType
): string => (vehicleType === 'TRUCK' ? 'Xe tải' : 'Xe van');

export const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('vi-VN');
};

export const formatOptionalNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '--';
  }

  return String(value);
};

export const buildHubLabel = (
  hubId?: number,
  hubById?: Record<number, Hub>
): string => {
  if (!hubId) {
    return 'Chưa phân công';
  }

  const hub = hubById?.[hubId];
  if (!hub) {
    return `Hub #${hubId}`;
  }

  if (hub.code && hub.name) {
    return `${hub.code} - ${hub.name}`;
  }

  return hub.code || hub.name || `Hub #${hubId}`;
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

export const parseNonNegativeInteger = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isInteger(parsedValue) || parsedValue < 0) {
    return undefined;
  }

  return parsedValue;
};

export const mapVehicleToFormState = (
  vehicle: SecondMileVehicle
): VehicleFormState => ({
  licensePlate: vehicle.licensePlate || '',
  maxBags: String(vehicle.maxBags ?? 0),
  maxWeight: String(vehicle.maxWeight ?? ''),
  maxVolume: String(vehicle.maxVolume ?? ''),
  hubId: vehicle.hubId ? String(vehicle.hubId) : '',
  assignedStaffId:
    vehicle.assignedStaffId !== undefined && vehicle.assignedStaffId !== null
      ? String(vehicle.assignedStaffId)
      : '',
  status: vehicle.status || 'ACTIVE',
  vehicleType: vehicle.vehicleType || 'TRUCK',
});

export const validateVehicleForm = (
  values: VehicleFormState
): string | null => {
  if (!values.licensePlate.trim()) {
    return 'Vui lòng nhập biển số xe.';
  }

  const maxBags = parseNonNegativeInteger(values.maxBags);
  if (maxBags === undefined) {
    return 'Số bao tối đa phải là số nguyên không âm.';
  }

  const maxWeight = parseOptionalNumber(values.maxWeight);
  if (maxWeight === undefined || maxWeight <= 0) {
    return 'Tải trọng tối đa phải là số lớn hơn 0.';
  }

  const maxVolume = parseOptionalNumber(values.maxVolume);
  if (maxVolume === undefined || maxVolume <= 0) {
    return 'Thể tích tối đa phải là số lớn hơn 0.';
  }

  if (
    !values.hubId.trim() ||
    parseOptionalPositiveInteger(values.hubId) === undefined
  ) {
    return 'Vui lòng chọn hub.';
  }

  if (
    values.assignedStaffId.trim() &&
    parseOptionalPositiveInteger(values.assignedStaffId) === undefined
  ) {
    return 'Tài xế phải được chọn từ danh sách.';
  }

  return null;
};

export const buildVehicleRequest = (
  values: VehicleFormState
): SecondMileCreateVehicleRequest => {
  const assignedStaffId = parseOptionalPositiveInteger(values.assignedStaffId);

  return {
    license_plate: values.licensePlate.trim().toUpperCase(),
    vehicle_type: values.vehicleType,
    max_bags: parseNonNegativeInteger(values.maxBags) ?? 0,
    max_weight: parseOptionalNumber(values.maxWeight) ?? 0,
    max_volume: parseOptionalNumber(values.maxVolume) ?? 0,
    hub_id: parseOptionalPositiveInteger(values.hubId) as number,
    status: values.status,
    ...(assignedStaffId !== undefined
      ? { assigned_staff_id: assignedStaffId }
      : {}),
  };
};
