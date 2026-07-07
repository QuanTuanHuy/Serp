export const REQUEST_TYPE_OPTIONS = [
  { value: 'NEW_SERVICE', label: 'Đăng ký mới' },
  { value: 'CHANGE_SERVICE', label: 'Thay đổi dịch vụ' },
  { value: 'PAUSE_SERVICE', label: 'Tạm dừng dịch vụ' },
  { value: 'RESUME_SERVICE', label: 'Tiếp tục dịch vụ' },
  { value: 'STOP_SERVICE', label: 'Dừng dịch vụ' },
  { value: 'RENEW_SERVICE', label: 'Gia hạn dịch vụ' },
] as const;

export const TRIP_OPTION_OPTIONS = [
  { value: 'MORNING', label: 'Chỉ chiều đi học' },
  { value: 'AFTERNOON', label: 'Chỉ chiều về nhà' },
  { value: 'ROUND_TRIP', label: 'Hai chiều' },
] as const;

export const ROUTE_DIRECTION_OPTIONS = [
  { value: 'OUTBOUND', label: 'Chiều đi học' },
  { value: 'RETURN', label: 'Chiều về nhà' },
] as const;

export const ROUTE_LOCATION_TYPE_OPTIONS = [
  { value: 'SCHOOL', label: 'Trường học' },
  { value: 'DEPOT', label: 'Bãi xe' },
] as const;

export const PROFILE_STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
] as const;

export const STAFF_STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: 'Sẵn sàng' },
  { value: 'ASSIGNED', label: 'Đã phân công' },
  { value: 'ON_LEAVE', label: 'Nghỉ phép' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
] as const;

export const BUS_STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: 'Sẵn sàng' },
  { value: 'ASSIGNED', label: 'Đã phân công' },
  { value: 'MAINTENANCE', label: 'Bảo trì' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
] as const;

export const SCHOOL_BUS_MAP_DEFAULT_CENTER = {
  lat: 16.047079,
  lng: 108.20623,
};

export const SCHOOL_BUS_MAP_DEFAULT_ZOOM = 6;
export const SCHOOL_BUS_MAP_DETAIL_ZOOM = 14;

// TODO: replace with module lookup by code=SCHOOL_BUS when account exposes a stable option endpoint.
export const SCHOOL_BUS_ACCOUNT_MODULE_ID = 15;
