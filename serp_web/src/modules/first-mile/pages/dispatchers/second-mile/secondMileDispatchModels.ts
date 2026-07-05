/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile dispatcher page models
 */

import type { TmsComboboxOption } from '../../../components';
import type {
  BagDistributionManifest,
  BagDistributionManifestStatus,
  Hub,
  PostOffice,
  SecondMileBagDestinationType,
  SecondMileRoute,
  SecondMileVehicle,
} from '../../../types';

export type TabValue = 'ready' | 'planning';
export type DestinationFilter = 'ALL' | SecondMileBagDestinationType;
export type CheckinMode = 'start' | 'end';

export interface PlanningState {
  originHubId?: number;
  destinationType: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  routeId?: number;
  vehicleId?: number;
  plannedDepartureAt: string;
  plannedArrivalAt: string;
  sealedSlaHours: string;
  note: string;
}

export interface CheckinState {
  latitude: string;
  longitude: string;
  locationLabel: string;
  photo?: File;
}

export interface SelectedBagSummary {
  sameDestination: boolean;
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  totalWeight: number;
  totalVolume: number;
  totalOrders: number;
}

export const STATUS_LABELS: Record<BagDistributionManifestStatus, string> = {
  CREATED: 'Đã tạo',
  OUTBOUND_CONFIRMED: 'Đã xuất khỏi hub',
  INBOUND_CONFIRMED: 'Đã nhập tại điểm đến',
  CANCELLED: 'Đã hủy',
};

export const HINT_LABELS: Record<string, string> = {
  HIGH_PRIORITY: 'Ưu tiên cao',
  CAPACITY_RISK: 'Rủi ro tải trọng',
  LOW_UTILIZATION: 'Chưa tối ưu tải',
  NO_ROUTE: 'Chưa có tuyến',
  NO_DRIVER: 'Chưa có tài xế',
  SCHEDULE_CONFLICT: 'Trùng lịch',
};

export const destinationOptions: Array<{
  value: SecondMileBagDestinationType;
  label: string;
}> = [
  { value: 'HUB', label: 'Hub' },
  { value: 'POST_OFFICE', label: 'Bưu cục' },
];

export const makeDefaultPlanningState = (): PlanningState => {
  const departure = new Date();
  departure.setMinutes(departure.getMinutes() + 30);
  departure.setSeconds(0, 0);
  const arrival = new Date(departure);
  arrival.setHours(arrival.getHours() + 3);

  return {
    destinationType: 'HUB',
    plannedDepartureAt: toDateTimeLocalValue(departure),
    plannedArrivalAt: toDateTimeLocalValue(arrival),
    sealedSlaHours: '24',
    note: '',
  };
};

export const canManageDistribution = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') || roles.includes('TMS_HUB_MANAGER');

export const canOperateDistribution = (roles: string[]): boolean =>
  canManageDistribution(roles) || roles.includes('TMS_HUB_EMPLOYEE');

export const canDriverCheckin = (roles: string[]): boolean =>
  canOperateDistribution(roles) || roles.includes('TMS_HUB_DRIVER');

export function toDateTimeLocalValue(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function toApiDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}

export function formatDateTime(value?: string): string {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatNumber(value?: number, digits = 1): string {
  return new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: digits,
  }).format(value ?? 0);
}

export function parseNumber(value?: string): number | undefined {
  if (!value) return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

export function statusVariant(status?: BagDistributionManifestStatus) {
  if (status === 'CANCELLED') return 'destructive' as const;
  if (status === 'INBOUND_CONFIRMED') return 'default' as const;
  if (status === 'OUTBOUND_CONFIRMED') return 'secondary' as const;
  return 'outline' as const;
}

export function destinationLabel(
  destinationType?: SecondMileBagDestinationType,
  destinationHubId?: number,
  destinationHubCode?: string,
  destinationPostOfficeCode?: string
): string {
  if (destinationType === 'HUB') {
    return (
      destinationHubCode ||
      (destinationHubId ? `Hub #${destinationHubId}` : 'Hub')
    );
  }

  if (destinationType === 'POST_OFFICE') {
    return destinationPostOfficeCode || 'Bưu cục';
  }

  return '-';
}

export function buildHubOptions(hubs?: Hub[]): TmsComboboxOption[] {
  return (hubs ?? []).map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  }));
}

export function buildPostOfficeOptions(
  postOffices?: PostOffice[]
): TmsComboboxOption[] {
  return (postOffices ?? []).map((postOffice) => ({
    value: postOffice.code,
    label: `${postOffice.code} - ${postOffice.name}`,
  }));
}

export function buildRouteOptions(
  routes?: SecondMileRoute[]
): TmsComboboxOption[] {
  return (routes ?? []).map((route) => ({
    value: String(route.id),
    label: `${route.routeCode} - ${route.routeName}`,
  }));
}

export function buildVehicleOptions(
  vehicles?: SecondMileVehicle[]
): TmsComboboxOption[] {
  return (vehicles ?? []).map((vehicle) => ({
    value: String(vehicle.id),
    label: [
      vehicle.licensePlate,
      vehicle.assignedStaffFullName
        ? `Tài xế: ${vehicle.assignedStaffFullName}`
        : vehicle.assignedStaffId
          ? `Tài xế #${vehicle.assignedStaffId}`
          : 'Chưa phân công tài xế',
    ].join(' - '),
  }));
}

export function getManifestBagStats(manifest: BagDistributionManifest) {
  const bags = manifest.bags ?? [];
  return {
    totalBags: bags.length,
    scannedOut: bags.filter((bag) => Boolean(bag.scanOutTime)).length,
    scannedIn: bags.filter((bag) => Boolean(bag.scanInTime)).length,
    totalOrders: bags.reduce(
      (sum, bag) => sum + (bag.totalOrdersSnapshot ?? 0),
      0
    ),
    totalWeight: bags.reduce(
      (sum, bag) => sum + (bag.totalWeightSnapshot ?? 0),
      0
    ),
    totalVolume: bags.reduce(
      (sum, bag) => sum + (bag.totalVolumeSnapshot ?? 0),
      0
    ),
  };
}
