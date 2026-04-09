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

export type LocationType = 'WAREHOUSE' | 'PORT' | 'DEPOT' | 'CUSTOMER_SITE';

export interface LocationItem {
  id: number;
  locationCode: string;
  type: LocationType;
}

// --------------------------------------------------------------------------
// Create location
// -------------------------------------------------------------------------

export interface CreateLocationPayload {
  name: string;
  code: string;
  type: LocationType;
  latitude: number;
  longitude: number;
}
