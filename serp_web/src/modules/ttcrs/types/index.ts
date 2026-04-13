// -------------------------------------------------------------------------
// Enums (matching Java constants)
// -------------------------------------------------------------------------

export type RequestStatus =
  | 'PENDING'
  | 'PLANNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export type RequestType = 'OF' | 'IF' | 'OE' | 'IE';

export type ContainerSize = 'TWENTY_FEET' | 'FORTY_FEET' | 'FORTY_FIVE_FEET';

// -------------------------------------------------------------------------
// Response DTOs
// -------------------------------------------------------------------------

export interface TtcrsRequest {
  id: number;
  tenantId: number;
  customerId: number | null;
  srcLocationCode: string;
  destLocationCode: string;
  earlyAtSrc: string | null;
  lateAtSrc: string | null;
  earlyAtDest: string | null;
  lateAtDest: string | null;
  weight: number | null;
  containerSize: ContainerSize | null;
  dropTrailerRequired: boolean | null;
  reason: string | null;
  status: RequestStatus;
  type: RequestType;
  transportPlanId: number | null;
  createdBy: number | null;
  createdAt: string;
  lastUpdatedStamp: string;
}

export interface TtcrsPageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface TtcrsApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

// -------------------------------------------------------------------------
// Filter / Query params
// -------------------------------------------------------------------------

export interface RequestFilterParams {
  statuses?: RequestStatus[];
  type?: RequestType;
  srcLocationCode?: string;
  destLocationCode?: string;
  createdFrom?: string;
  createdTo?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// -------------------------------------------------------------------------
// Create Request
// -------------------------------------------------------------------------

export interface CreateRequestPayload {
  customerId: number;
  type: RequestType;
  srcLocationCode: string;
  destLocationCode: string;
  quantity: number;
  weight?: number | null;
  dropTrailerRequired?: boolean;
  earlyAtSrc?: string | null;
  lateAtSrc?: string | null;
  earlyAtDest?: string | null;
  lateAtDest?: string | null;
}

// -------------------------------------------------------------------------
// Location
// -------------------------------------------------------------------------

export type LocationType =
  | 'PORT'
  | 'WAREHOUSE'
  | 'DEPOT_CONTAINER'
  | 'DEPOT_TRUCK'
  | 'DEPOT_TRAILER';

export interface LocationItem {
  id: number;
  locationCode: string;
  type: LocationType;
  lat: number | null;
  lng: number | null;
  createdStamp: string | null;
}

// -------------------------------------------------------------------------
// Create location
// -------------------------------------------------------------------------

export interface CreateLocationPayload {
  locationCode: string;
  type: LocationType;
  lat: number;
  lng: number;
}

// -------------------------------------------------------------------------
// Resources
// -------------------------------------------------------------------------

export type VehicleStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE';
export type DriverStatus = 'AVAILABLE' | 'IN_USE' | 'OFF';
export type ResourceContainerSize = 'TWENTY' | 'FORTY';
export type ResourceKind = 'CONTAINER' | 'TRUCK' | 'TRAILER' | 'DRIVER';

export interface ContainerItem {
  id: number;
  code: string;
  size: ResourceContainerSize | null;
  status: VehicleStatus;
  currentLocationCode: string | null;
  createdStamp: string | null;
}

export interface TruckItem {
  id: number;
  code: string;
  status: VehicleStatus;
  currentLocationCode: string | null;
  createdStamp: string | null;
}

export interface TrailerItem {
  id: number;
  code: string;
  status: VehicleStatus;
  currentLocationCode: string | null;
  createdStamp: string | null;
}

export interface DriverItem {
  id: number;
  name: string;
  status: DriverStatus;
  createdStamp: string | null;
}

// Normalized row used by the Resources page
export interface ResourceRow {
  id: number;
  kind: ResourceKind;
  identifier: string;                    // code for vehicles, name for driver
  status: string;
  size: ResourceContainerSize | null;    // containers only
  currentLocationCode: string | null;   // null for drivers
}

// -------------------------------------------------------------------------
// Create resource payloads
// -------------------------------------------------------------------------

export interface CreateContainerPayload {
  code: string;
  size?: ResourceContainerSize | null;
  currentLocationCode: string;
}

export interface CreateTruckPayload {
  code: string;
  currentLocationCode: string;
}

export interface CreateTrailerPayload {
  code: string;
  currentLocationCode: string;
}

export interface CreateDriverPayload {
  name: string;
}
