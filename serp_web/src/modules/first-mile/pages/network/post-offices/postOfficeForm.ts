/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office form model and helpers
 */

import type {
  CreatePostOfficeRequest,
  PostOffice,
  PostOfficeStatus,
} from '../../../types';

export type PostOfficeFormMode = 'create' | 'edit';
export type PostOfficeViewMode = 'list' | 'grid';

export interface PostOfficeFormState {
  code: string;
  name: string;
  province_code: string;
  ward_code: string;
  address_detail: string;
  phone_number: string;
  operational_start_date: string;
  operational_end_date: string;
  working_start_time: string;
  working_end_time: string;
  service_radius_m: string;
  daily_capacity: string;
  current_load: string;
  delivery_capacity: string;
  current_delivery_load: string;
  priority: string;
  latitude: string;
  longitude: string;
  status: PostOfficeStatus;
}

export type UpdatePostOfficeFormField = <K extends keyof PostOfficeFormState>(
  field: K,
  value: PostOfficeFormState[K]
) => void;

export const POST_OFFICE_STATUS_OPTIONS: Array<{
  value: PostOfficeStatus;
  label: string;
}> = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'SUSPENDED', label: 'Suspended' },
];

export const DEFAULT_POST_OFFICE_FORM: PostOfficeFormState = {
  code: '',
  name: '',
  province_code: '',
  ward_code: '',
  address_detail: '',
  phone_number: '',
  operational_start_date: '',
  operational_end_date: '',
  working_start_time: '',
  working_end_time: '',
  service_radius_m: '1',
  daily_capacity: '0',
  current_load: '0',
  delivery_capacity: '0',
  current_delivery_load: '0',
  priority: '0',
  latitude: '',
  longitude: '',
  status: 'ACTIVE',
};

export const normalizeLocationCode = (value?: string | null): string =>
  value?.trim() ?? '';

const toDateInputValue = (value?: string): string => {
  if (!value) {
    return '';
  }

  return value.slice(0, 10);
};

const toTimeInputValue = (value?: string): string => {
  if (!value) {
    return '';
  }

  return value.slice(0, 5);
};

export const mapPostOfficeToFormState = (
  postOffice: PostOffice
): PostOfficeFormState => {
  return {
    code: postOffice.code || '',
    name: postOffice.name || '',
    province_code: postOffice.provinceCode || '',
    ward_code: postOffice.wardCode || '',
    address_detail: postOffice.addressDetail || '',
    phone_number: postOffice.phoneNumber || '',
    operational_start_date: toDateInputValue(postOffice.operationalStartDate),
    operational_end_date: toDateInputValue(postOffice.operationalEndDate),
    working_start_time: toTimeInputValue(postOffice.workingStartTime),
    working_end_time: toTimeInputValue(postOffice.workingEndTime),
    service_radius_m: String(postOffice.serviceRadiusM ?? 1),
    daily_capacity: String(postOffice.dailyCapacity ?? 0),
    current_load: String(postOffice.currentLoad ?? 0),
    delivery_capacity: String(postOffice.deliveryCapacity ?? 0),
    current_delivery_load: String(postOffice.currentDeliveryLoad ?? 0),
    priority: String(postOffice.priority ?? 0),
    latitude:
      postOffice.latitude === undefined || postOffice.latitude === null
        ? ''
        : String(postOffice.latitude),
    longitude:
      postOffice.longitude === undefined || postOffice.longitude === null
        ? ''
        : String(postOffice.longitude),
    status: postOffice.status || 'ACTIVE',
  };
};

export const validatePostOfficeForm = (
  values: PostOfficeFormState
): string | null => {
  if (!values.code.trim()) {
    return 'Code is required.';
  }

  if (!values.name.trim()) {
    return 'Name is required.';
  }

  if (!values.province_code.trim()) {
    return 'Province code is required.';
  }

  if (!values.ward_code.trim()) {
    return 'Ward code is required.';
  }

  if (!values.address_detail.trim()) {
    return 'Address detail is required.';
  }

  const serviceRadius = Number(values.service_radius_m);
  if (!Number.isInteger(serviceRadius) || serviceRadius < 1) {
    return 'Service radius must be an integer greater than or equal to 1.';
  }

  const dailyCapacity = Number(values.daily_capacity);
  if (!Number.isInteger(dailyCapacity) || dailyCapacity < 0) {
    return 'Pickup order capacity must be an integer greater than or equal to 0.';
  }

  const currentLoad = Number(values.current_load);
  if (!Number.isInteger(currentLoad) || currentLoad < 0) {
    return 'Current pickup load must be an integer greater than or equal to 0.';
  }

  const deliveryCapacity = Number(values.delivery_capacity);
  if (!Number.isInteger(deliveryCapacity) || deliveryCapacity < 0) {
    return 'Delivery capacity must be an integer greater than or equal to 0.';
  }

  const currentDeliveryLoad = Number(values.current_delivery_load);
  if (!Number.isInteger(currentDeliveryLoad) || currentDeliveryLoad < 0) {
    return 'Current delivery load must be an integer greater than or equal to 0.';
  }

  const priority = Number(values.priority);
  if (!Number.isInteger(priority) || priority < 0) {
    return 'Priority must be an integer greater than or equal to 0.';
  }

  if (values.latitude.trim()) {
    const latitude = Number(values.latitude);
    if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) {
      return 'Latitude must be between -90 and 90.';
    }
  }

  if (values.longitude.trim()) {
    const longitude = Number(values.longitude);
    if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
      return 'Longitude must be between -180 and 180.';
    }
  }

  return null;
};

export const buildCreatePostOfficeRequest = (
  values: PostOfficeFormState
): CreatePostOfficeRequest => {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    province_code: values.province_code.trim(),
    ward_code: values.ward_code.trim(),
    address_detail: values.address_detail.trim(),
    service_radius_m: Number(values.service_radius_m),
    daily_capacity: Number(values.daily_capacity),
    current_load: Number(values.current_load),
    delivery_capacity: Number(values.delivery_capacity),
    current_delivery_load: Number(values.current_delivery_load),
    priority: Number(values.priority),
    status: values.status,
    ...(values.phone_number.trim()
      ? { phone_number: values.phone_number.trim() }
      : {}),
    ...(values.operational_start_date
      ? { operational_start_date: values.operational_start_date }
      : {}),
    ...(values.operational_end_date
      ? { operational_end_date: values.operational_end_date }
      : {}),
    ...(values.working_start_time
      ? { working_start_time: values.working_start_time }
      : {}),
    ...(values.working_end_time
      ? { working_end_time: values.working_end_time }
      : {}),
    ...(values.latitude.trim() ? { latitude: Number(values.latitude) } : {}),
    ...(values.longitude.trim() ? { longitude: Number(values.longitude) } : {}),
  };
};

export const getStatusBadgeVariant = (
  status: PostOfficeStatus
): 'default' | 'secondary' | 'outline' | 'destructive' => {
  if (status === 'ACTIVE') {
    return 'default';
  }

  if (status === 'SUSPENDED') {
    return 'destructive';
  }

  if (status === 'MAINTENANCE') {
    return 'outline';
  }

  return 'secondary';
};
