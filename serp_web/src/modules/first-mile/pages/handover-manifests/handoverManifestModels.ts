/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Handover manifest view models
 */

import type { HandoverManifest, HandoverManifestStatus } from '../../types';
import { formatOrderStatusLabel } from '../../utils/orderStatusLabels';

export const PAGE_SIZE = 20;
export const SELECT_PAGE_SIZE = 200;
export const DRIVER_PAGE_SIZE = 50;

export type ManifestMode = 'POST_OFFICE' | 'HUB';

export type HandoverManagementDomain = 'POST_OFFICE_HUB' | 'HUB_TRANSFER';

export type DriverHandoverAction = 'START' | 'END';

export const MANIFEST_STATUS_OPTIONS: Array<{
  value: HandoverManifestStatus;
  label: string;
}> = [
  { value: 'CREATED', label: 'Mới tạo' },
  { value: 'OUTBOUND_CONFIRMED', label: 'Đã xuất đi hub' },
  { value: 'INBOUND_CONFIRMED', label: 'Hub đã nhận' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

export const MANIFEST_STATUS_LABELS: Record<HandoverManifestStatus, string> = {
  CREATED: 'Mới tạo',
  OUTBOUND_CONFIRMED: 'Đã xuất đi hub',
  INBOUND_CONFIRMED: 'Hub đã nhận',
  CANCELLED: 'Đã hủy',
};

export const formatHandoverOrderStatusLabel = formatOrderStatusLabel;

export const canManagePostOfficeHandover = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') || roles.includes('TMS_POSTOFFICER_MANAGER');

export const canReceiveHubHandover = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE') ||
  roles.includes('TMS_HUB_DRIVER');

export const isHandoverDriverOnly = (roles: string[]): boolean =>
  roles.includes('TMS_HUB_DRIVER') &&
  !roles.includes('TMS_ADMIN') &&
  !roles.includes('TMS_HUB_MANAGER') &&
  !roles.includes('TMS_HUB_EMPLOYEE');

export const canUseDriverHandover = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE') ||
  roles.includes('TMS_HUB_DRIVER');

export const getStatusBadgeVariant = (
  status?: HandoverManifestStatus
): 'default' | 'secondary' | 'outline' | 'destructive' => {
  if (status === 'INBOUND_CONFIRMED') {
    return 'default';
  }
  if (status === 'OUTBOUND_CONFIRMED') {
    return 'outline';
  }
  if (status === 'CANCELLED') {
    return 'destructive';
  }
  return 'secondary';
};

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

export const getTotalOrders = (manifest?: HandoverManifest | null): number =>
  manifest?.totalOrders ?? manifest?.orders?.length ?? 0;

export const getScannedOutOrders = (
  manifest?: HandoverManifest | null
): number =>
  manifest?.scannedOutOrders ??
  manifest?.orders?.filter((order) => Boolean(order.scanOutTime)).length ??
  0;

export const getScannedInOrders = (
  manifest?: HandoverManifest | null
): number =>
  manifest?.scannedInOrders ??
  manifest?.orders?.filter((order) => Boolean(order.scanInTime)).length ??
  0;

export const isReadyForDispatch = (manifest: HandoverManifest): boolean => {
  const totalOrders = getTotalOrders(manifest);
  return (
    manifest.status === 'CREATED' &&
    totalOrders > 0 &&
    getScannedOutOrders(manifest) >= totalOrders
  );
};

export const normalizeScanCode = (value: string): string => value.trim();

const padDatePart = (value: number): string => String(value).padStart(2, '0');

const toDateTimeLocalValue = (date: Date): string => {
  const year = date.getFullYear();
  const month = padDatePart(date.getMonth() + 1);
  const day = padDatePart(date.getDate());
  const hours = padDatePart(date.getHours());
  const minutes = padDatePart(date.getMinutes());
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

export const getDefaultDepartureTime = (): string => {
  const date = new Date();
  date.setMinutes(date.getMinutes() + 15);
  return toDateTimeLocalValue(date);
};

export const getDefaultArrivalTime = (): string => {
  const date = new Date();
  date.setMinutes(date.getMinutes() + 75);
  return toDateTimeLocalValue(date);
};

export const getBrowserLocation = (): Promise<{
  latitude: number;
  longitude: number;
}> =>
  new Promise((resolve, reject) => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      reject(new Error('Trình duyệt không hỗ trợ lấy vị trí hiện tại.'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) =>
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        }),
      (error) => reject(error),
      {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: 10000,
      }
    );
  });

export const getTripStep = (
  manifest: HandoverManifest
): 'READY' | 'IN_TRANSIT' | 'ARRIVED' | 'RECEIVED' => {
  if (manifest.status === 'INBOUND_CONFIRMED') {
    return 'RECEIVED';
  }
  if (manifest.driverEndCheckinAt) {
    return 'ARRIVED';
  }
  if (manifest.driverStartCheckinAt) {
    return 'IN_TRANSIT';
  }
  return 'READY';
};

export const getStepLabel = (manifest: HandoverManifest): string => {
  const step = getTripStep(manifest);
  if (step === 'RECEIVED') return 'Hub đã nhận';
  if (step === 'ARRIVED') return 'Đã check-in điểm đến';
  if (step === 'IN_TRANSIT') return 'Đang vận chuyển';
  return 'Sẵn sàng xuất phát';
};

export const getActionLabel = (manifest: HandoverManifest): string => {
  if (!manifest.driverStartCheckinAt) return 'Check-in xuất phát';
  if (!manifest.driverEndCheckinAt) return 'Check-in điểm đến';
  return 'Chờ hub xác nhận nhập';
};

export const buildCheckinFormData = (
  latitude: number,
  longitude: number,
  photo: File
): FormData => {
  const formData = new FormData();
  formData.append('latitude', String(latitude));
  formData.append('longitude', String(longitude));
  formData.append('photo', photo);
  return formData;
};
