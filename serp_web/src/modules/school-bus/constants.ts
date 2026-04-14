export const REQUEST_TYPE_OPTIONS = [
  { value: 'PICKUP', label: 'Pickup' },
  { value: 'DROPOFF', label: 'Drop-off' },
  { value: 'ROUND_TRIP', label: 'Round trip' },
] as const;

export const SHIFT_TYPE_OPTIONS = [
  { value: 'MORNING', label: 'Morning' },
  { value: 'AFTERNOON', label: 'Afternoon' },
] as const;

export const ROUTE_DIRECTION_OPTIONS = [
  { value: 'OUTBOUND', label: 'Chieu di' },
  { value: 'RETURN', label: 'Chieu ve' },
] as const;

export const ROUTE_LOCATION_TYPE_OPTIONS = [
  { value: 'SCHOOL', label: 'Truong hoc' },
  { value: 'DEPOT', label: 'Bai xe / depot' },
] as const;

export const PROFILE_STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
] as const;

export const BUS_STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'IN_SERVICE', label: 'In service' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
  { value: 'INACTIVE', label: 'Inactive' },
] as const;

export const SCHOOL_BUS_MAP_DEFAULT_CENTER = {
  lat: 16.047079,
  lng: 108.20623,
};

export const SCHOOL_BUS_MAP_DEFAULT_ZOOM = 6;
export const SCHOOL_BUS_MAP_DETAIL_ZOOM = 14;

// TODO: replace with module lookup by code=SCHOOL_BUS when account exposes a stable option endpoint.
export const SCHOOL_BUS_ACCOUNT_MODULE_ID = 15;
