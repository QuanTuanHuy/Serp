/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile API transform helpers
 */

import { transformTimestampFields } from '@/lib/store/api/utils';
import type {
  FirstMileApiResponse,
  FirstMilePageResponse,
  FirstMilePaginatedData,
  BagDistributionManifest,
  BagDistributionManifestBag,
  BagDistributionPlan,
  BagDistributionPlanItem,
  HandoverManifest,
  HandoverManifestOrderItem,
  HubPostOfficeMapping,
  ImportHistory,
  ImportHistoryStatus,
  ImportType,
  PostOfficeStaff,
  PostOfficeStaffAssignment,
  AutoSecondMileBaggingPlan,
  SecondMileBag,
  SecondMileBagCapacitySettings,
  SecondMileBagDestinationType,
  SecondMileBagOrder,
  SecondMileBagStatus,
  SecondMileBagSuggestion,
  SecondMileBaggingKpi,
  SecondMileBaggingValidation,
  SecondMileBaggingValidationItem,
  SecondMileHubStaffAssignment,
  SecondMileHubStaff,
  SecondMileOrder,
  SecondMileRoute,
  SecondMileRouteDestinationType,
  SecondMileRouteEndpointType,
  SecondMileRouteStatus,
  SecondMileVehicle,
  SecondMileVehicleStatus,
  SecondMileVehicleType,
} from '../types';

const readField = <T>(
  raw: Record<string, unknown>,
  snakeKey: string,
  camelKey: string,
  ...alternateKeys: string[]
): T | undefined => {
  for (const key of [snakeKey, camelKey, ...alternateKeys]) {
    const value = raw[key];
    if (value !== undefined && value !== null) {
      return value as T;
    }
  }
  return undefined;
};

const readOptionalNumber = (
  raw: Record<string, unknown>,
  snakeKey: string,
  camelKey: string,
  ...alternateKeys: string[]
): number | undefined => {
  const value = readField<unknown>(raw, snakeKey, camelKey, ...alternateKeys);
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const numeric = Number(value);
  return Number.isFinite(numeric) ? numeric : undefined;
};

export const normalizeHubPostOfficeMapping = (
  raw: unknown
): HubPostOfficeMapping => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    hubId: Number(readField(record, 'hub_id', 'hubId') ?? 0),
    postOfficeCode: String(
      readField(record, 'post_office_code', 'postOfficeCode') ?? ''
    ),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    tenantId: readField<number>(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizeSecondMileHubStaffAssignment = (
  raw: unknown
): SecondMileHubStaffAssignment => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    hubId: readField<number>(record, 'hub_id', 'hubId'),
    hubCode: readField<string>(record, 'hub_code', 'hubCode'),
    hubName: readField<string>(record, 'hub_name', 'hubName'),
    staffId: readField<number>(record, 'staff_id', 'staffId'),
    staffCode: readField<string>(record, 'staff_code', 'staffCode'),
    staffFullName: readField<string>(
      record,
      'staff_full_name',
      'staffFullName'
    ),
    staffRole: readField(record, 'staff_role', 'staffRole'),
    staffStatus: readField(record, 'staff_status', 'staffStatus'),
    assignedFrom: readField<string>(record, 'assigned_from', 'assignedFrom'),
    assignedTo: readField<string>(record, 'assigned_to', 'assignedTo'),
    isPrimary: readField<boolean>(record, 'is_primary', 'isPrimary'),
    notes: readField<string>(record, 'notes', 'notes'),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
  };
};

export const normalizeSecondMileHubStaff = (
  raw: unknown
): SecondMileHubStaff => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    code: readField<string>(record, 'code', 'code'),
    fullName: readField<string>(record, 'full_name', 'fullName'),
    role: readField(record, 'role', 'role'),
    status: readField(record, 'status', 'status'),
  };
};

export const normalizePostOfficeStaff = (raw: unknown): PostOfficeStaff => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    code: readField<string>(record, 'code', 'code'),
    fullName: readField<string>(record, 'full_name', 'fullName'),
    phoneNumber: readField<string>(record, 'phone_number', 'phoneNumber'),
    email: readField<string>(record, 'email', 'email'),
    avatarUrl: readField<string>(record, 'avatar_url', 'avatarUrl'),
    role: readField(record, 'role', 'role'),
    status: readField(record, 'status', 'status'),
    hireDate: readField<string>(record, 'hire_date', 'hireDate'),
    maxDailyStops: readField<number>(
      record,
      'max_daily_stops',
      'maxDailyStops'
    ),
    maxDailyParcels: readField<number>(
      record,
      'max_daily_parcels',
      'maxDailyParcels'
    ),
    notes: readField<string>(record, 'notes', 'notes'),
    userId: readField<number>(record, 'user_id', 'userId'),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    createdBy: readField<string>(record, 'created_by', 'createdBy'),
    updatedBy: readField<string>(record, 'updated_by', 'updatedBy'),
    tenantId: readField<number>(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizePostOfficeStaffAssignment = (
  raw: unknown
): PostOfficeStaffAssignment => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    postOfficeId: readField<number>(record, 'post_office_id', 'postOfficeId'),
    postOfficeCode: readField<string>(
      record,
      'post_office_code',
      'postOfficeCode'
    ),
    postOfficeName: readField<string>(
      record,
      'post_office_name',
      'postOfficeName'
    ),
    staffId: readField<number>(record, 'staff_id', 'staffId'),
    staffCode: readField<string>(record, 'staff_code', 'staffCode'),
    staffFullName: readField<string>(
      record,
      'staff_full_name',
      'staffFullName'
    ),
    staffRole: readField(record, 'staff_role', 'staffRole'),
    staffStatus: readField(record, 'staff_status', 'staffStatus'),
    assignedFrom: readField<string>(record, 'assigned_from', 'assignedFrom'),
    assignedTo: readField<string>(record, 'assigned_to', 'assignedTo'),
    isPrimary: readField<boolean>(record, 'is_primary', 'isPrimary'),
    notes: readField<string>(record, 'notes', 'notes'),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    createdBy: readField<string>(record, 'created_by', 'createdBy'),
    updatedBy: readField<string>(record, 'updated_by', 'updatedBy'),
    tenantId: readField<number>(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizeSecondMileOrder = (raw: unknown): SecondMileOrder => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    orderCode: readField<string>(record, 'order_code', 'orderCode'),
    customerOrderCode: readField<string>(
      record,
      'customer_order_code',
      'customerOrderCode'
    ),
    originPostOfficeCode: readField<string>(
      record,
      'origin_post_office_code',
      'originPostOfficeCode'
    ),
    destinationPostOfficeCode: readField<string>(
      record,
      'destination_post_office_code',
      'destinationPostOfficeCode'
    ),
    status: readField(record, 'status', 'status'),
    totalWeight: readField<number>(record, 'total_weight', 'totalWeight'),
    totalVolume: readField<number>(record, 'total_volume', 'totalVolume'),
  };
};

export const normalizeSecondMileVehicle = (raw: unknown): SecondMileVehicle => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    licensePlate: String(
      readField(record, 'license_plate', 'licensePlate') ?? ''
    ),
    vehicleType:
      readField<SecondMileVehicleType>(record, 'vehicle_type', 'vehicleType') ??
      'TRUCK',
    maxWeight: readOptionalNumber(record, 'max_weight', 'maxWeight') ?? 0,
    maxVolume: readOptionalNumber(record, 'max_volume', 'maxVolume') ?? 0,
    maxBags: readOptionalNumber(record, 'max_bags', 'maxBags') ?? 0,
    imageUrl: readField<string>(record, 'image_url', 'imageUrl'),
    hubId: readOptionalNumber(record, 'hub_id', 'hubId') ?? 0,
    assignedStaffId: readOptionalNumber(
      record,
      'assigned_staff_id',
      'assignedStaffId',
      'driver_id',
      'driverId',
      'staff_id',
      'staffId'
    ),
    assignedStaffCode: readField<string>(
      record,
      'assigned_staff_code',
      'assignedStaffCode',
      'driver_code',
      'driverCode',
      'staff_code',
      'staffCode'
    ),
    assignedStaffFullName: readField<string>(
      record,
      'assigned_staff_full_name',
      'assignedStaffFullName',
      'assigned_staff_name',
      'assignedStaffName',
      'driver_name',
      'driverName',
      'staff_full_name',
      'staffFullName',
      'staff_name',
      'staffName'
    ),
    status:
      readField<SecondMileVehicleStatus>(record, 'status', 'status') ??
      'ACTIVE',
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    createdBy: readField<string>(record, 'created_by', 'createdBy'),
    updatedBy: readField<string>(record, 'updated_by', 'updatedBy'),
    tenantId: readOptionalNumber(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizeSecondMileBagOrder = (
  raw: unknown
): SecondMileBagOrder => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    orderId: readOptionalNumber(record, 'order_id', 'orderId'),
    orderCode: readField<string>(record, 'order_code', 'orderCode'),
  };
};

export const normalizeSecondMileBag = (raw: unknown): SecondMileBag => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const orders = readField<unknown[]>(record, 'orders', 'orders') ?? [];

  return {
    id: Number(record.id ?? 0),
    bagCode: readField<string>(record, 'bag_code', 'bagCode'),
    originHubId: readOptionalNumber(record, 'origin_hub_id', 'originHubId'),
    destinationType: readField<SecondMileBagDestinationType>(
      record,
      'destination_type',
      'destinationType'
    ),
    destinationHubId: readOptionalNumber(
      record,
      'destination_hub_id',
      'destinationHubId'
    ),
    destinationPostOfficeCode: readField<string>(
      record,
      'destination_post_office_code',
      'destinationPostOfficeCode'
    ),
    vehicleId: readOptionalNumber(record, 'vehicle_id', 'vehicleId'),
    routeId: readOptionalNumber(record, 'route_id', 'routeId'),
    maxWeight: readOptionalNumber(record, 'max_weight', 'maxWeight') ?? 0,
    maxVolume: readOptionalNumber(record, 'max_volume', 'maxVolume') ?? 0,
    maxOrders: readOptionalNumber(record, 'max_orders', 'maxOrders') ?? 0,
    currentWeight:
      readOptionalNumber(record, 'current_weight', 'currentWeight') ?? 0,
    currentVolume:
      readOptionalNumber(record, 'current_volume', 'currentVolume') ?? 0,
    currentOrders:
      readOptionalNumber(record, 'current_orders', 'currentOrders') ?? 0,
    status:
      readField<SecondMileBagStatus>(record, 'status', 'status') ?? 'CREATED',
    sealedAt: readField<string>(record, 'sealed_at', 'sealedAt'),
    note: readField<string>(record, 'note', 'note'),
    orders: orders.map((order) => normalizeSecondMileBagOrder(order)),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    createdBy: readField<string>(record, 'created_by', 'createdBy'),
    updatedBy: readField<string>(record, 'updated_by', 'updatedBy'),
    tenantId: readOptionalNumber(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizeSecondMileBagCapacitySettings = (
  raw: unknown
): SecondMileBagCapacitySettings => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: readOptionalNumber(record, 'id', 'id'),
    maxWeight: readOptionalNumber(record, 'max_weight', 'maxWeight') ?? 0,
    maxVolume: readOptionalNumber(record, 'max_volume', 'maxVolume') ?? 0,
    maxOrders: readOptionalNumber(record, 'max_orders', 'maxOrders') ?? 0,
  };
};

export const normalizeSecondMileBagSuggestion = (
  raw: unknown
): SecondMileBagSuggestion => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    bagId: readOptionalNumber(record, 'bag_id', 'bagId') ?? 0,
    bagCode: readField<string>(record, 'bag_code', 'bagCode'),
    remainingWeight:
      readOptionalNumber(record, 'remaining_weight', 'remainingWeight') ?? 0,
    remainingVolume:
      readOptionalNumber(record, 'remaining_volume', 'remainingVolume') ?? 0,
    remainingOrders:
      readOptionalNumber(record, 'remaining_orders', 'remainingOrders') ?? 0,
  };
};

export const normalizeSecondMileRoute = (raw: unknown): SecondMileRoute => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    routeCode: String(readField(record, 'route_code', 'routeCode') ?? ''),
    routeName: String(readField(record, 'route_name', 'routeName') ?? ''),
    originType:
      readField<SecondMileRouteEndpointType>(
        record,
        'origin_type',
        'originType'
      ) ?? 'HUB',
    originHubId: readOptionalNumber(record, 'origin_hub_id', 'originHubId'),
    originPostOfficeCode: readField<string>(
      record,
      'origin_post_office_code',
      'originPostOfficeCode'
    ),
    destinationType:
      readField<SecondMileRouteDestinationType>(
        record,
        'destination_type',
        'destinationType'
      ) ?? 'HUB',
    destinationHubId: readOptionalNumber(
      record,
      'destination_hub_id',
      'destinationHubId'
    ),
    destinationPostOfficeCode: readField<string>(
      record,
      'destination_post_office_code',
      'destinationPostOfficeCode'
    ),
    vehicleId: readOptionalNumber(record, 'vehicle_id', 'vehicleId'),
    estimatedDistanceKm: readOptionalNumber(
      record,
      'estimated_distance_km',
      'estimatedDistanceKm'
    ),
    estimatedDurationMinutes: readOptionalNumber(
      record,
      'estimated_duration_minutes',
      'estimatedDurationMinutes'
    ),
    fixedDepartureTime: readField<string>(
      record,
      'fixed_departure_time',
      'fixedDepartureTime'
    ),
    status:
      readField<SecondMileRouteStatus>(record, 'status', 'status') ?? 'ACTIVE',
    note: readField<string>(record, 'note', 'note'),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
    createdBy: readField<string>(record, 'created_by', 'createdBy'),
    updatedBy: readField<string>(record, 'updated_by', 'updatedBy'),
    tenantId: readOptionalNumber(record, 'tenant_id', 'tenantId'),
  };
};

export const normalizeHandoverManifestOrder = (
  raw: unknown
): HandoverManifestOrderItem => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: readField<number>(record, 'id', 'id'),
    orderId: readField<number>(record, 'order_id', 'orderId'),
    orderCode: readField<string>(record, 'order_code', 'orderCode'),
    customerOrderCode: readField<string>(
      record,
      'customer_order_code',
      'customerOrderCode'
    ),
    status: readField(record, 'status', 'status'),
    scanOutTime: readField<string>(record, 'scan_out_time', 'scanOutTime'),
    scanInTime: readField<string>(record, 'scan_in_time', 'scanInTime'),
  };
};

export const normalizeHandoverManifest = (raw: unknown): HandoverManifest => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const ordersRaw = record.orders;
  const orders = Array.isArray(ordersRaw)
    ? ordersRaw.map(normalizeHandoverManifestOrder)
    : undefined;

  return {
    id: Number(record.id ?? 0),
    manifestCode: readField<string>(record, 'manifest_code', 'manifestCode'),
    originPostOfficeId: readField<number>(
      record,
      'origin_post_office_id',
      'originPostOfficeId'
    ),
    originPostOfficeCode: readField<string>(
      record,
      'origin_post_office_code',
      'originPostOfficeCode'
    ),
    targetHubId: readField<number>(record, 'target_hub_id', 'targetHubId'),
    vehicleId: readField<number>(record, 'vehicle_id', 'vehicleId'),
    vehicleLicensePlate: readField<string>(
      record,
      'vehicle_license_plate',
      'vehicleLicensePlate'
    ),
    assignedDriverId: readField<number>(
      record,
      'assigned_driver_id',
      'assignedDriverId'
    ),
    routeId: readField<number>(record, 'route_id', 'routeId'),
    routeCode: readField<string>(record, 'route_code', 'routeCode'),
    plannedDepartureAt: readField<string>(
      record,
      'planned_departure_at',
      'plannedDepartureAt'
    ),
    plannedArrivalAt: readField<string>(
      record,
      'planned_arrival_at',
      'plannedArrivalAt'
    ),
    originPostOfficeLatitude: readField<number>(
      record,
      'origin_post_office_latitude',
      'originPostOfficeLatitude'
    ),
    originPostOfficeLongitude: readField<number>(
      record,
      'origin_post_office_longitude',
      'originPostOfficeLongitude'
    ),
    targetHubLatitude: readField<number>(
      record,
      'target_hub_latitude',
      'targetHubLatitude'
    ),
    targetHubLongitude: readField<number>(
      record,
      'target_hub_longitude',
      'targetHubLongitude'
    ),
    driverStartCheckinAt: readField<string>(
      record,
      'driver_start_checkin_at',
      'driverStartCheckinAt'
    ),
    driverStartLatitude: readField<number>(
      record,
      'driver_start_latitude',
      'driverStartLatitude'
    ),
    driverStartLongitude: readField<number>(
      record,
      'driver_start_longitude',
      'driverStartLongitude'
    ),
    driverStartDistanceM: readField<number>(
      record,
      'driver_start_distance_m',
      'driverStartDistanceM'
    ),
    driverStartPhotoUrl: readField<string>(
      record,
      'driver_start_photo_url',
      'driverStartPhotoUrl'
    ),
    driverEndCheckinAt: readField<string>(
      record,
      'driver_end_checkin_at',
      'driverEndCheckinAt'
    ),
    driverEndLatitude: readField<number>(
      record,
      'driver_end_latitude',
      'driverEndLatitude'
    ),
    driverEndLongitude: readField<number>(
      record,
      'driver_end_longitude',
      'driverEndLongitude'
    ),
    driverEndDistanceM: readField<number>(
      record,
      'driver_end_distance_m',
      'driverEndDistanceM'
    ),
    driverEndPhotoUrl: readField<string>(
      record,
      'driver_end_photo_url',
      'driverEndPhotoUrl'
    ),
    status: readField(record, 'status', 'status'),
    totalOrders: readField<number>(record, 'total_orders', 'totalOrders'),
    scannedOutOrders: readField<number>(
      record,
      'scanned_out_orders',
      'scannedOutOrders'
    ),
    scannedInOrders: readField<number>(
      record,
      'scanned_in_orders',
      'scannedInOrders'
    ),
    dispatchedAt: readField<string>(record, 'dispatched_at', 'dispatchedAt'),
    inboundConfirmedAt: readField<string>(
      record,
      'inbound_confirmed_at',
      'inboundConfirmedAt'
    ),
    sealCode: readField<string>(record, 'seal_code', 'sealCode'),
    note: readField<string>(record, 'note', 'note'),
    orders,
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
  };
};

export const normalizeBagDistributionManifestBag = (
  raw: unknown
): BagDistributionManifestBag => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    id: Number(record.id ?? 0),
    bagId: readOptionalNumber(record, 'bag_id', 'bagId'),
    bagCode: readField<string>(record, 'bag_code', 'bagCode'),
    originHubId: readOptionalNumber(record, 'origin_hub_id', 'originHubId'),
    destinationType: readField(record, 'destination_type', 'destinationType'),
    destinationHubId: readOptionalNumber(
      record,
      'destination_hub_id',
      'destinationHubId'
    ),
    destinationPostOfficeCode: readField<string>(
      record,
      'destination_post_office_code',
      'destinationPostOfficeCode'
    ),
    totalWeightSnapshot: readOptionalNumber(
      record,
      'total_weight_snapshot',
      'totalWeightSnapshot'
    ),
    totalVolumeSnapshot: readOptionalNumber(
      record,
      'total_volume_snapshot',
      'totalVolumeSnapshot'
    ),
    totalOrdersSnapshot: readOptionalNumber(
      record,
      'total_orders_snapshot',
      'totalOrdersSnapshot'
    ),
    scanOutTime: readField<string>(record, 'scan_out_time', 'scanOutTime'),
    scanInTime: readField<string>(record, 'scan_in_time', 'scanInTime'),
  };
};

export const normalizeBagDistributionManifest = (
  raw: unknown
): BagDistributionManifest => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const bagsRaw = readField<unknown[]>(record, 'bags', 'bags') ?? [];

  return {
    id: Number(record.id ?? 0),
    manifestCode: readField<string>(record, 'manifest_code', 'manifestCode'),
    originHubId: readOptionalNumber(record, 'origin_hub_id', 'originHubId'),
    originHubCode: readField<string>(
      record,
      'origin_hub_code',
      'originHubCode'
    ),
    destinationType: readField(record, 'destination_type', 'destinationType'),
    destinationHubId: readOptionalNumber(
      record,
      'destination_hub_id',
      'destinationHubId'
    ),
    destinationHubCode: readField<string>(
      record,
      'destination_hub_code',
      'destinationHubCode'
    ),
    destinationPostOfficeCode: readField<string>(
      record,
      'destination_post_office_code',
      'destinationPostOfficeCode'
    ),
    routeId: readOptionalNumber(record, 'route_id', 'routeId'),
    routeCode: readField<string>(record, 'route_code', 'routeCode'),
    vehicleId: readOptionalNumber(record, 'vehicle_id', 'vehicleId'),
    vehicleLicensePlate: readField<string>(
      record,
      'vehicle_license_plate',
      'vehicleLicensePlate'
    ),
    assignedDriverId: readOptionalNumber(
      record,
      'assigned_driver_id',
      'assignedDriverId'
    ),
    plannedDepartureAt: readField<string>(
      record,
      'planned_departure_at',
      'plannedDepartureAt'
    ),
    plannedArrivalAt: readField<string>(
      record,
      'planned_arrival_at',
      'plannedArrivalAt'
    ),
    actualDepartureAt: readField<string>(
      record,
      'actual_departure_at',
      'actualDepartureAt'
    ),
    actualArrivalAt: readField<string>(
      record,
      'actual_arrival_at',
      'actualArrivalAt'
    ),
    driverStartCheckinId: readOptionalNumber(
      record,
      'driver_start_checkin_id',
      'driverStartCheckinId'
    ),
    driverStartCheckinAt: readField<string>(
      record,
      'driver_start_checkin_at',
      'driverStartCheckinAt'
    ),
    driverStartLatitude: readOptionalNumber(
      record,
      'driver_start_latitude',
      'driverStartLatitude'
    ),
    driverStartLongitude: readOptionalNumber(
      record,
      'driver_start_longitude',
      'driverStartLongitude'
    ),
    driverStartDistanceM: readOptionalNumber(
      record,
      'driver_start_distance_m',
      'driverStartDistanceM'
    ),
    driverStartLocationLabel: readField<string>(
      record,
      'driver_start_location_label',
      'driverStartLocationLabel'
    ),
    driverStartPhotoUrl: readField<string>(
      record,
      'driver_start_photo_url',
      'driverStartPhotoUrl'
    ),
    driverEndCheckinId: readOptionalNumber(
      record,
      'driver_end_checkin_id',
      'driverEndCheckinId'
    ),
    driverEndCheckinAt: readField<string>(
      record,
      'driver_end_checkin_at',
      'driverEndCheckinAt'
    ),
    driverEndLatitude: readOptionalNumber(
      record,
      'driver_end_latitude',
      'driverEndLatitude'
    ),
    driverEndLongitude: readOptionalNumber(
      record,
      'driver_end_longitude',
      'driverEndLongitude'
    ),
    driverEndDistanceM: readOptionalNumber(
      record,
      'driver_end_distance_m',
      'driverEndDistanceM'
    ),
    driverEndLocationLabel: readField<string>(
      record,
      'driver_end_location_label',
      'driverEndLocationLabel'
    ),
    driverEndPhotoUrl: readField<string>(
      record,
      'driver_end_photo_url',
      'driverEndPhotoUrl'
    ),
    status: readField(record, 'status', 'status'),
    note: readField<string>(record, 'note', 'note'),
    bags: bagsRaw.map((item) => normalizeBagDistributionManifestBag(item)),
    createdAt: readField<string>(record, 'created_at', 'createdAt'),
    updatedAt: readField<string>(record, 'updated_at', 'updatedAt'),
  };
};

export const normalizeHubPostOfficeMappingPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<HubPostOfficeMapping>>
    | FirstMilePageResponse<HubPostOfficeMapping>
): FirstMilePaginatedData<HubPostOfficeMapping> => {
  const page = unwrapFirstMilePageResultOrRaw<HubPostOfficeMapping>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeHubPostOfficeMapping(item)),
  };
};

export const normalizeSecondMileOrderPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<SecondMileOrder>>
    | FirstMilePageResponse<SecondMileOrder>
): FirstMilePaginatedData<SecondMileOrder> => {
  const page = unwrapFirstMilePageResultOrRaw<SecondMileOrder>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeSecondMileOrder(item)),
  };
};

export const normalizeSecondMileVehiclePage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<SecondMileVehicle>>
    | FirstMilePageResponse<SecondMileVehicle>
): FirstMilePaginatedData<SecondMileVehicle> => {
  const page = unwrapFirstMilePageResultOrRaw<SecondMileVehicle>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeSecondMileVehicle(item)),
  };
};

export const normalizeSecondMileBagPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<SecondMileBag>>
    | FirstMilePageResponse<SecondMileBag>
): FirstMilePaginatedData<SecondMileBag> => {
  const page = unwrapFirstMilePageResultOrRaw<SecondMileBag>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeSecondMileBag(item)),
  };
};

export const normalizeSecondMileRoutePage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<SecondMileRoute>>
    | FirstMilePageResponse<SecondMileRoute>
): FirstMilePaginatedData<SecondMileRoute> => {
  const page = unwrapFirstMilePageResultOrRaw<SecondMileRoute>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeSecondMileRoute(item)),
  };
};

export const normalizeHandoverManifestPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<HandoverManifest>>
    | FirstMilePageResponse<HandoverManifest>
): FirstMilePaginatedData<HandoverManifest> => {
  const page = unwrapFirstMilePageResultOrRaw<HandoverManifest>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeHandoverManifest(item)),
  };
};

export const normalizeBagDistributionManifestPage = (
  response:
    | FirstMileApiResponse<FirstMilePageResponse<BagDistributionManifest>>
    | FirstMilePageResponse<BagDistributionManifest>
): FirstMilePaginatedData<BagDistributionManifest> => {
  const page =
    unwrapFirstMilePageResultOrRaw<BagDistributionManifest>(response);
  return {
    ...page,
    items: page.items.map((item) => normalizeBagDistributionManifest(item)),
  };
};

export const unwrapFirstMileResult = <T>(
  response: FirstMileApiResponse<T>
): T => {
  return transformTimestampFields((response?.result ?? null) as T);
};

export const unwrapFirstMileResultOrRaw = <T>(
  response: FirstMileApiResponse<T> | T
): T => {
  if (
    response &&
    typeof response === 'object' &&
    'result' in (response as Record<string, unknown>)
  ) {
    return transformTimestampFields(
      ((response as FirstMileApiResponse<T>).result ?? null) as T
    );
  }

  return transformTimestampFields(response as T);
};

export const normalizeSecondMileBaggingValidation = (
  raw: unknown
): SecondMileBaggingValidation => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const items = readField<unknown[]>(record, 'items', 'items') ?? [];

  return {
    bagId: readOptionalNumber(record, 'bag_id', 'bagId') ?? 0,
    acceptedCount:
      readOptionalNumber(record, 'accepted_count', 'acceptedCount') ?? 0,
    rejectedCount:
      readOptionalNumber(record, 'rejected_count', 'rejectedCount') ?? 0,
    items: items.map((item) => {
      const itemRecord = (item ?? {}) as Record<string, unknown>;
      return {
        orderCode:
          readField<string>(itemRecord, 'order_code', 'orderCode') ?? '',
        accepted: Boolean(
          readField<boolean>(itemRecord, 'accepted', 'accepted') ?? false
        ),
        reason: readField<string>(itemRecord, 'reason', 'reason'),
      } satisfies SecondMileBaggingValidationItem;
    }),
  };
};

export const normalizeAutoSecondMileBaggingPlan = (
  raw: unknown
): AutoSecondMileBaggingPlan => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const items = readField<unknown[]>(record, 'items', 'items') ?? [];

  return {
    executed: Boolean(readField<boolean>(record, 'executed', 'executed')),
    bagCount: readOptionalNumber(record, 'bag_count', 'bagCount') ?? 0,
    items: items.map((item) => {
      const itemRecord = (item ?? {}) as Record<string, unknown>;
      return {
        bagCode: readField<string>(itemRecord, 'bag_code', 'bagCode') ?? '',
        orderCodes:
          readField<string[]>(itemRecord, 'order_codes', 'orderCodes') ?? [],
        totalWeight:
          readOptionalNumber(itemRecord, 'total_weight', 'totalWeight') ?? 0,
        totalVolume:
          readOptionalNumber(itemRecord, 'total_volume', 'totalVolume') ?? 0,
      };
    }),
  };
};

export const normalizeSecondMileBaggingKpi = (
  raw: unknown
): SecondMileBaggingKpi => {
  const record = (raw ?? {}) as Record<string, unknown>;
  return {
    originHubId:
      readOptionalNumber(record, 'origin_hub_id', 'originHubId') ?? 0,
    sealedBagCount:
      readOptionalNumber(record, 'sealed_bag_count', 'sealedBagCount') ?? 0,
    avgFillRateWeight:
      readOptionalNumber(record, 'avg_fill_rate_weight', 'avgFillRateWeight') ??
      0,
    avgFillRateVolume:
      readOptionalNumber(record, 'avg_fill_rate_volume', 'avgFillRateVolume') ??
      0,
    avgOrdersPerBag:
      readOptionalNumber(record, 'avg_orders_per_bag', 'avgOrdersPerBag') ?? 0,
  };
};

export const normalizeBagDistributionPlan = (
  raw: unknown
): BagDistributionPlan => {
  const record = (raw ?? {}) as Record<string, unknown>;
  const items = readField<unknown[]>(record, 'items', 'items') ?? [];

  return {
    executed: Boolean(readField<boolean>(record, 'executed', 'executed')),
    manifestCount:
      readOptionalNumber(record, 'manifest_count', 'manifestCount') ?? 0,
    items: items.map((item) => {
      const itemRecord = (item ?? {}) as Record<string, unknown>;
      return {
        originHubId: readOptionalNumber(
          itemRecord,
          'origin_hub_id',
          'originHubId'
        ),
        destinationType: readField(
          itemRecord,
          'destination_type',
          'destinationType'
        ),
        destinationHubId: readOptionalNumber(
          itemRecord,
          'destination_hub_id',
          'destinationHubId'
        ),
        destinationPostOfficeCode: readField<string>(
          itemRecord,
          'destination_post_office_code',
          'destinationPostOfficeCode'
        ),
        routeId: readOptionalNumber(itemRecord, 'route_id', 'routeId'),
        routeCode: readField<string>(itemRecord, 'route_code', 'routeCode'),
        vehicleId: readOptionalNumber(itemRecord, 'vehicle_id', 'vehicleId'),
        vehicleLicensePlate: readField<string>(
          itemRecord,
          'vehicle_license_plate',
          'vehicleLicensePlate'
        ),
        assignedDriverId: readOptionalNumber(
          itemRecord,
          'assigned_driver_id',
          'assignedDriverId'
        ),
        plannedDepartureAt: readField<string>(
          itemRecord,
          'planned_departure_at',
          'plannedDepartureAt'
        ),
        plannedArrivalAt: readField<string>(
          itemRecord,
          'planned_arrival_at',
          'plannedArrivalAt'
        ),
        bagIds: readField<number[]>(itemRecord, 'bag_ids', 'bagIds') ?? [],
        bagCodes:
          readField<string[]>(itemRecord, 'bag_codes', 'bagCodes') ?? [],
        totalWeight:
          readOptionalNumber(itemRecord, 'total_weight', 'totalWeight') ?? 0,
        totalVolume:
          readOptionalNumber(itemRecord, 'total_volume', 'totalVolume') ?? 0,
        totalOrders:
          readOptionalNumber(itemRecord, 'total_orders', 'totalOrders') ?? 0,
        score: readOptionalNumber(itemRecord, 'score', 'score'),
        hints: readField<string[]>(itemRecord, 'hints', 'hints') ?? [],
        createdManifestId: readOptionalNumber(
          itemRecord,
          'created_manifest_id',
          'createdManifestId'
        ),
        createdManifestCode: readField<string>(
          itemRecord,
          'created_manifest_code',
          'createdManifestCode'
        ),
      } satisfies BagDistributionPlanItem;
    }),
  };
};

export const unwrapFirstMilePageResult = <T>(
  response: FirstMileApiResponse<FirstMilePageResponse<T>>
): FirstMilePaginatedData<T> => {
  return unwrapFirstMilePageResultOrRaw(response);
};

/**
 * second-mile hub import returns {@link ImportHistoryResponse} without ApiResponse wrapper.
 */
export const normalizeSecondMileHubImportHistory = (
  raw: unknown
): ImportHistory => {
  const r = raw as Record<string, unknown>;
  return {
    id: Number(r.id ?? 0),
    file_id: String(r.file_id ?? r.fileId ?? ''),
    file_name: String(r.file_name ?? r.fileName ?? ''),
    status: (r.status ?? 'PENDING') as ImportHistoryStatus,
    total_records: Number(r.total_records ?? r.totalRecords ?? 0),
    success_records: Number(r.success_records ?? r.successRecords ?? 0),
    failed_records: Number(r.failed_records ?? r.failedRecords ?? 0),
    error_message: (r.error_message ?? r.errorMessage) as string | undefined,
    started_at: (r.started_at ?? r.startedAt) as string | undefined,
    finished_at: (r.finished_at ?? r.finishedAt) as string | undefined,
    type: (r.type ?? 'HUB') as ImportType,
  };
};

export const unwrapFirstMilePageResultOrRaw = <T>(
  response:
    | FirstMileApiResponse<FirstMilePageResponse<T>>
    | FirstMilePageResponse<T>
): FirstMilePaginatedData<T> => {
  const wrappedResponse = response as FirstMileApiResponse<
    FirstMilePageResponse<T>
  >;
  const pageData = wrappedResponse?.result
    ? wrappedResponse.result
    : (response as FirstMilePageResponse<T>);

  if (!pageData) {
    return {
      items: [],
      totalItems: 0,
      totalPages: 0,
      currentPage: 0,
      pageSize: 0,
      hasNext: false,
      hasPrevious: false,
    };
  }

  return {
    items: transformTimestampFields(pageData.items || []),
    totalItems: pageData.totalElements || 0,
    totalPages: pageData.totalPages || 0,
    currentPage: pageData.page || 0,
    pageSize: pageData.size || 0,
    hasNext: !!pageData.hasNext,
    hasPrevious: !!pageData.hasPrevious,
  };
};
