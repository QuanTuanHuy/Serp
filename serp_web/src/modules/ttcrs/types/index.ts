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
  srcLocationCode: string | null;
  destLocationCode: string;
  earlyAtSrc: string | null;
  lateAtSrc: string | null;
  earlyAtDest: string | null;
  lateAtDest: string | null;
  weight: number | null;
  containerSize: ContainerSize | null;
  dropTrailerRequired: boolean | null;
  reason: string | null;
  evidenceAtSrc: string | null;
  evidenceAtDest: string | null;
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
  srcLocationCode?: string | null;
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
  userId: number;
  name: string;
  email: string;
  status: DriverStatus;
}

// -------------------------------------------------------------------------
// Update requests status (batch)
// -------------------------------------------------------------------------

export interface UpdateRequestsStatusPayload {
  requestIds: number[];
  status: RequestStatus;
}

// -------------------------------------------------------------------------
// Create transport plan input (Step 2 final submit)
// -------------------------------------------------------------------------

/**
 * Maps one resource (by DB id) to the depot codes it is allowed to return to.
 * Mirrors ResourceReturnDepotDTO on the backend.
 */
export interface ResourceReturnDepotConfig {
  resourceId: number;
  returnDepotCodes: string[];
}

export interface CreateTransportPlanInputPayload {
  /** IDs of the PLANNED requests to include. */
  requestIds: number[];
  /** Per-container return depot configuration. */
  containerReturnDepots: ResourceReturnDepotConfig[];
  /** Per-trailer return depot configuration. */
  trailerReturnDepots: ResourceReturnDepotConfig[];
  /** Per-truck return depot configuration. */
  truckReturnDepots: ResourceReturnDepotConfig[];
  /** IDs of trucks made available for this plan. */
  truckIds: number[];
  /** IDs of trailers made available for this plan. */
  trailerIds: number[];
  /** IDs of containers made available for this plan. */
  containerIds: number[];
}

// -------------------------------------------------------------------------
// Transport Plan Result (algorithm output)
// -------------------------------------------------------------------------

export interface AlgorithmRouteElement {
  locationCode: string;
  action: string;
  arrivalTime: string;
  departureTime: string;
  travelTime: number;
  requestId: number | null;
  trailerId?: number | null;
}

export interface AlgorithmTruck {
  code: string;
  depotTruckCode: string;
}

export interface AlgorithmTruckRoute {
  truck: AlgorithmTruck;
  nbStops: number;
  travelTime: number;
  nodes: AlgorithmRouteElement[];
}

export interface AlgorithmStatistics {
  totalRequests: number;
  totalRejectedRequests: number;
  totalDistance: number;
  numberTrucks: number;
}

export interface TransportPlanResult {
  truckRoutes: AlgorithmTruckRoute[];
  unscheduledExEmptyRequests: unknown[];
  unscheduledExLadenRequests: unknown[];
  unscheduledImEmptyRequests: unknown[];
  unscheduledImLadenRequests: unknown[];
  statisticInformation: AlgorithmStatistics;
}

// -------------------------------------------------------------------------
// Save transport plan (Step 3 Finish)
// -------------------------------------------------------------------------

export interface SaveTransportPlanStopPayload {
  sequence: number;
  locationCode: string;
  action: string;
  plannedArrival: string;
  requestId: number | null;
  trailerId?: number | null;
}

export interface SaveTransportPlanItemPayload {
  truckCode: string;
  driverId: number | null;
  stops: SaveTransportPlanStopPayload[];
}

export interface SaveTransportPlanPayload {
  plans: SaveTransportPlanItemPayload[];
}

export type TransportPlanStatus =
  | 'CREATED'
  | 'EXECUTING'
  | 'COMPLETED'
  | 'CANCELLED';

export type StopAction =
  | 'DEPOT_START'
  | 'DEPOT_END'
  | 'PICKUP_TRAILER'
  | 'DROP_TRAILER'
  | 'PICKUP_CONTAINER'
  | 'DELIVERY_CONTAINER';

export interface TransportPlanSavedItem {
  id: number;
  truckId: number;
  truckCode: string;
  driverId: number | null;
  driverName: string | null;
  startTime: string | null;
  endTime: string | null;
  status: TransportPlanStatus;
  stopCount: number;
  createdStamp: string | null;
}

// Re-used for the list view
export type TransportPlanListItem = TransportPlanSavedItem;

export interface TransportPlanStopDetail {
  id: number;
  sequence: number;
  locationCode: string;
  lat: number | null;
  lng: number | null;
  action: StopAction;
  plannedArrivalTime: string | null;
  actualArrivalTime: string | null;
  isCompleted: boolean;
  requestId: number | null;
  requestSrcLocationCode: string | null;
  requestDestLocationCode: string | null;
  evidenceUrl: string | null;
}

export interface TransportPlanDetail {
  id: number;
  truckId: number;
  truckCode: string;
  driverId: number | null;
  driverName: string | null;
  startTime: string | null;
  endTime: string | null;
  status: TransportPlanStatus;
  createdStamp: string | null;
  stops: TransportPlanStopDetail[];
  // Driver execution fields
  cancelReason: string | null;
  currentStop: number | null;
  actualStartTime: string | null;
  actualEndTime: string | null;
  totalDistance: number | null;
  totalTime: number | null;
}

// -------------------------------------------------------------------------
// Driver action payloads
// -------------------------------------------------------------------------

export interface CancelRoutePayload {
  reason: string;
}

export interface CompleteStopPayload {
  evidenceUrl: string | null;
  totalDistance: number | null;
}

export interface UploadEvidenceResponse {
  url: string;
}

// Normalized row used by the Resources page
export interface ResourceRow {
  id: number;
  kind: ResourceKind;
  identifier: string; // code for vehicles, name for driver
  status: string;
  size: ResourceContainerSize | null; // containers only
  currentLocationCode: string | null; // null for drivers
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

// Drivers are Account users with TTCRS_DRIVER role — no creation payload needed

// -------------------------------------------------------------------------
// Update payloads
// -------------------------------------------------------------------------

export interface UpdateRequestPayload {
  srcLocationCode?: string;
  destLocationCode?: string;
  earlyAtSrc?: string | null;
  lateAtSrc?: string | null;
  earlyAtDest?: string | null;
  lateAtDest?: string | null;
  weight?: number | null;
  containerSize?: ContainerSize | null;
  dropTrailerRequired?: boolean;
  reason?: string | null;
}

export interface UpdateLocationPayload {
  type?: LocationType;
  lat?: number;
  lng?: number;
}

export interface UpdateContainerPayload {
  status?: VehicleStatus;
  currentLocationCode?: string;
}

export interface UpdateTruckPayload {
  status?: VehicleStatus;
  currentLocationCode?: string;
}

export interface UpdateTrailerPayload {
  status?: VehicleStatus;
  currentLocationCode?: string;
}

export interface UpdateDriverPayload {
  status: DriverStatus;
}
