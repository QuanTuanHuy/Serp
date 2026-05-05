/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub form model and helpers
 */

import type {
  CreateHubRequest,
  Hub,
  HubStatus,
  HubType,
  UpdateHubRequest,
} from '../../types';

export type HubFormMode = 'create' | 'edit';

export interface HubFormState {
  code: string;
  name: string;
  hub_type: HubType;
  province_code: string;
  ward_code: string;
  address_detail: string;
  phone_number: string;
  working_start_time: string;
  working_end_time: string;
  daily_capacity: string;
  current_load: string;
  latitude: string;
  longitude: string;
  status: HubStatus;
}

export type UpdateHubFormField = <K extends keyof HubFormState>(
  field: K,
  value: HubFormState[K]
) => void;

export const HUB_TYPE_OPTIONS: Array<{ value: HubType; label: string }> = [
  { value: 'REGIONAL', label: 'Regional' },
  { value: 'LOCAL', label: 'Local' },
];

export const HUB_FORM_STATUS_OPTIONS: Array<{ value: HubStatus; label: string }> =
  [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'INACTIVE', label: 'Inactive' },
    { value: 'MAINTENANCE', label: 'Maintenance' },
  ];

/** Status filter options (same values as form). */
export const HUB_STATUS_OPTIONS = HUB_FORM_STATUS_OPTIONS;

export function getHubTypeLabel(hubType: string): string {
  switch (hubType) {
    case 'REGIONAL':
      return 'Regional hub';
    case 'LOCAL':
      return 'Local hub';
    default:
      return hubType;
  }
}

export const DEFAULT_HUB_FORM: HubFormState = {
  code: '',
  name: '',
  hub_type: 'REGIONAL',
  province_code: '',
  ward_code: '',
  address_detail: '',
  phone_number: '',
  working_start_time: '',
  working_end_time: '',
  daily_capacity: '0',
  current_load: '0',
  latitude: '',
  longitude: '',
  status: 'ACTIVE',
};

const WORKING_TIME_ANCHOR_DATE = '1970-01-01';

export const normalizeLocationCode = (value?: string | null): string =>
  value?.trim() ?? '';

const toTimeInputFromDateTime = (value?: string): string => {
  if (!value) {
    return '';
  }
  const tail = value.includes('T') ? (value.split('T')[1] ?? '') : value;
  return tail.slice(0, 5);
};

function timeToMinutes(hhmm: string): number {
  const [h, m] = hhmm.split(':').map((x) => Number(x));
  if (!Number.isFinite(h) || !Number.isFinite(m)) {
    return NaN;
  }
  return h * 60 + m;
}

function timeToLocalDateTime(time: string): string | undefined {
  const t = time.trim();
  if (!t) {
    return undefined;
  }
  const withSeconds = t.length === 5 ? `${t}:00` : t;
  return `${WORKING_TIME_ANCHOR_DATE}T${withSeconds}`;
}

export const mapHubToFormState = (hub: Hub): HubFormState => {
  return {
    code: hub.code || '',
    name: hub.name || '',
    hub_type: hub.hubType || 'REGIONAL',
    province_code: hub.provinceCode || '',
    ward_code: hub.wardCode || '',
    address_detail: hub.addressDetail || '',
    phone_number: hub.phoneNumber || '',
    working_start_time: toTimeInputFromDateTime(hub.workingStartTime),
    working_end_time: toTimeInputFromDateTime(hub.workingEndTime),
    daily_capacity: String(hub.dailyCapacity ?? 0),
    current_load: String(hub.currentLoad ?? 0),
    latitude:
      hub.latitude === undefined || hub.latitude === null
        ? ''
        : String(hub.latitude),
    longitude:
      hub.longitude === undefined || hub.longitude === null
        ? ''
        : String(hub.longitude),
    status: hub.status || 'ACTIVE',
  };
};

export const validateHubForm = (values: HubFormState): string | null => {
  if (!values.code.trim()) {
    return 'Code is required.';
  }
  if (!values.name.trim()) {
    return 'Name is required.';
  }
  if (!values.province_code.trim()) {
    return 'Province is required.';
  }
  if (!values.ward_code.trim()) {
    return 'Ward is required.';
  }
  if (!values.address_detail.trim()) {
    return 'Address detail is required.';
  }

  const ws = values.working_start_time.trim();
  const we = values.working_end_time.trim();
  if (ws !== we && (!ws || !we)) {
    return 'Provide both working start and end times, or leave both empty.';
  }
  if (ws && we) {
    const startM = timeToMinutes(ws);
    const endM = timeToMinutes(we);
    if (!Number.isFinite(startM) || !Number.isFinite(endM)) {
      return 'Working times must use HH:mm format.';
    }
    if (endM <= startM) {
      return 'Working end time must be after start time.';
    }
  }

  const latStr = values.latitude.trim();
  const lngStr = values.longitude.trim();
  if (latStr !== lngStr && (!latStr || !lngStr)) {
    return 'Provide both latitude and longitude, or leave both empty.';
  }
  if (latStr && lngStr) {
    const latitude = Number(latStr);
    const longitude = Number(lngStr);
    if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) {
      return 'Latitude must be between -90 and 90.';
    }
    if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
      return 'Longitude must be between -180 and 180.';
    }
  }

  const dailyCapacity = Number(values.daily_capacity);
  const currentLoad = Number(values.current_load);
  if (!Number.isInteger(dailyCapacity) || dailyCapacity < 0) {
    return 'Daily capacity must be a non-negative integer.';
  }
  if (!Number.isInteger(currentLoad) || currentLoad < 0) {
    return 'Current load must be a non-negative integer.';
  }

  return null;
};

export const buildCreateHubRequest = (
  values: HubFormState
): CreateHubRequest => {
  const ws = values.working_start_time.trim();
  const we = values.working_end_time.trim();
  const latStr = values.latitude.trim();
  const lngStr = values.longitude.trim();
  const dailyCapacity = Number(values.daily_capacity);
  const currentLoad = Number(values.current_load);

  const body: CreateHubRequest = {
    code: values.code.trim(),
    name: values.name.trim(),
    hub_type: values.hub_type,
    province_code: values.province_code.trim(),
    ward_code: values.ward_code.trim(),
    address_detail: values.address_detail.trim(),
    status: values.status,
  };

  if (values.phone_number.trim()) {
    body.phone_number = values.phone_number.trim();
  }
  if (ws && we) {
    body.working_start_time = timeToLocalDateTime(ws);
    body.working_end_time = timeToLocalDateTime(we);
  }
  if (latStr && lngStr) {
    body.latitude = Number(latStr);
    body.longitude = Number(lngStr);
  }
  if (values.daily_capacity.trim() && Number.isInteger(dailyCapacity)) {
    body.daily_capacity = dailyCapacity;
  }
  if (values.current_load.trim() && Number.isInteger(currentLoad)) {
    body.current_load = currentLoad;
  }

  return body;
};

export const buildUpdateHubRequest = (
  values: HubFormState
): UpdateHubRequest => {
  const ws = values.working_start_time.trim();
  const we = values.working_end_time.trim();
  const latStr = values.latitude.trim();
  const lngStr = values.longitude.trim();

  const body: UpdateHubRequest = {
    code: values.code.trim(),
    name: values.name.trim(),
    hub_type: values.hub_type,
    province_code: values.province_code.trim(),
    ward_code: values.ward_code.trim(),
    address_detail: values.address_detail.trim(),
    daily_capacity: Number(values.daily_capacity),
    current_load: Number(values.current_load),
    status: values.status,
  };

  if (values.phone_number.trim()) {
    body.phone_number = values.phone_number.trim();
  }
  if (ws && we) {
    body.working_start_time = timeToLocalDateTime(ws);
    body.working_end_time = timeToLocalDateTime(we);
  }
  if (latStr && lngStr) {
    body.latitude = Number(latStr);
    body.longitude = Number(lngStr);
  }

  return body;
};
