/**
 * Vietnamese label mappings for school-bus module enums.
 * Centralises all display labels so pages don't duplicate strings.
 */

/* -- Route status ------------------------------------------------ */
export const routeStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  GENERATED: 'Đã tạo',
  REVIEWING: 'Đang rà soát',
  PUBLISHED: 'Đã phát hành',
  ASSIGNED: 'Đã phân công',
  TRIP_CREATED: 'Đã tạo chuyến',
  CANCELLED: 'Đã hủy',
};

/* -- Transport request status ------------------------------------ */
export const requestStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  SUBMITTED: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
  CANCELLED: 'Đã hủy',
};

/* -- Subscription status ----------------------------------------- */
export const subscriptionStatusLabel: Record<string, string> = {
  PENDING: 'Chờ kích hoạt',
  ACTIVE: 'Đang hoạt động',
  PAUSED: 'Tạm dừng',
  STOPPED: 'Đã dừng',
  EXPIRED: 'Hết hạn',
};

/* -- Planning session status ------------------------------------- */
export const planningSessionStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  GENERATED: 'Đã tạo phương án',
  REVIEWING: 'Đang rà soát',
  PUBLISHED: 'Đã phát hành',
  CANCELLED: 'Đã hủy',
};

export const sessionStatusLabel = planningSessionStatusLabel;

/* -- Route direction --------------------------------------------- */
export const directionLabel: Record<string, string> = {
  TO_SCHOOL: 'Chiều đi học',
  FROM_SCHOOL: 'Chiều về nhà',
  OUTBOUND: 'Chiều đi học',
  RETURN: 'Chiều về nhà',
};

/* -- Student usage type ------------------------------------------ */
export const usageTypeLabel: Record<string, string> = {
  ONE_WAY_TO: 'Một chiều đi học',
  ONE_WAY_FROM: 'Một chiều về nhà',
  ROUND_TRIP: 'Hai chiều',
  ONE_WAY: 'Một chiều',
};

export const requestTypeLabel: Record<string, string> = {
  NEW_SERVICE: 'Đăng ký mới',
  CHANGE_SERVICE: 'Thay đổi dịch vụ',
  PAUSE_SERVICE: 'Tạm dừng dịch vụ',
  RESUME_SERVICE: 'Tiếp tục dịch vụ',
  STOP_SERVICE: 'Dừng dịch vụ',
  RENEW_SERVICE: 'Gia hạn dịch vụ',
};

export const tripOptionLabel: Record<string, string> = {
  MORNING: 'Chỉ chiều đi học',
  AFTERNOON: 'Chỉ chiều về nhà',
  ROUND_TRIP: 'Hai chiều',
};

export const locationTypeLabel: Record<string, string> = {
  SCHOOL: 'Trường học',
  DEPOT: 'Bãi xe',
  PICKUP_POINT: 'Điểm đón/trả',
  PICKUP: 'Điểm đón',
  DROPOFF: 'Điểm trả',
};

export const profileStatusLabel: Record<string, string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
};

export const staffStatusLabel: Record<string, string> = {
  AVAILABLE: 'Sẵn sàng',
  ASSIGNED: 'Đã phân công',
  ON_LEAVE: 'Nghỉ phép',
  INACTIVE: 'Ngừng hoạt động',
};

export const busStatusLabel: Record<string, string> = {
  AVAILABLE: 'Sẵn sàng',
  ASSIGNED: 'Đã phân công',
  MAINTENANCE: 'Bảo trì',
  INACTIVE: 'Ngừng hoạt động',
};

export const pickupUsageTypeLabel: Record<string, string> = {
  PICKUP_DROPOFF: 'Đón và trả',
  PICKUP_ONLY: 'Chỉ đón',
  DROPOFF_ONLY: 'Chỉ trả',
  PICKUP: 'Chỉ đón',
  DROPOFF: 'Chỉ trả',
};

export const routeAssignmentStatusLabel: Record<string, string> = {
  ASSIGNED: 'Đã phân công',
  CONFIRMED: 'Đã xác nhận',
  CANCELLED: 'Đã hủy',
  REPLACED: 'Đã thay thế',
};

export const tripStatusLabel: Record<string, string> = {
  PLANNED: 'Đã lên kế hoạch',
  ASSIGNED: 'Đã phân công',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

export const tripStopStatusLabel: Record<string, string> = {
  PENDING: 'Đang chờ',
  ARRIVED: 'Đã đến điểm',
  BOARDING: 'Đang đón/trả',
  DEPARTED: 'Đã rời điểm',
  SKIPPED: 'Đã bỏ qua',
};

export const tripStudentStatusLabel: Record<string, string> = {
  PLANNED: 'Chưa điểm danh',
  BOARDED: 'Đã lên xe',
  ABSENT: 'Vắng mặt',
  DROPPED_OFF: 'Đã xuống xe',
  NO_SHOW: 'Không có mặt tại điểm đón',
  NOT_SERVED: 'Chưa phục vụ',
};

export const attendanceTypeLabel: Record<string, string> = {
  BOARDING: 'Lên xe',
  DROPOFF: 'Xuống xe',
  ABSENCE: 'Vắng mặt',
};

export const attendanceStatusLabel: Record<string, string> = {
  PRESENT: 'Có mặt',
  ABSENT: 'Vắng mặt',
};

export const attendanceRecordStatusLabel = attendanceStatusLabel;

export const routeCalculationStatusLabel: Record<string, string> = {
  SUCCESS: 'Thành công',
  FAILED: 'Thất bại',
  PARTIAL: 'Một phần',
};

export const routeReadinessStatusLabel: Record<string, string> = {
  READY: 'Sẵn sàng',
  MISSING_BUS: 'Thiếu xe',
  MISSING_DRIVER: 'Thiếu tài xế',
  MISSING_ATTENDANT: 'Thiếu phụ xe',
};

export const pickupDropoffReadinessStatusLabel: Record<string, string> = {
  READY: 'Sẵn sàng',
  MISSING_PICKUP_WINDOW: 'Thiếu khung giờ đón',
  MISSING_COORDINATES: 'Thiếu tọa độ',
  UNSUPPORTED_USAGE_TYPE: 'Loại điểm chưa hỗ trợ',
  NOT_CHECKED: 'Chưa kiểm tra',
};

export const operationEventLabel: Record<string, string> = {
  TRIP_STARTED: 'Đã bắt đầu chuyến',
  TRIP_COMPLETED: 'Đã hoàn thành chuyến',
  TRIP_CANCELLED: 'Đã hủy chuyến',
  STOP_ARRIVED: 'Đã đến điểm dừng',
  STOP_BOARDING_STARTED: 'Đã bắt đầu đón/trả',
  STOP_DEPARTED: 'Đã rời điểm dừng',
  STOP_SKIPPED: 'Đã bỏ qua điểm dừng',
  STUDENT_BOARDED: 'Học sinh đã lên xe',
  STUDENT_ABSENT: 'Học sinh vắng mặt',
  STUDENT_NO_SHOW: 'Học sinh không có mặt',
  STUDENT_DROPPED_OFF: 'Học sinh đã xuống xe',
  STUDENT_NOT_SERVED: 'Học sinh chưa được phục vụ',
};

export const statusLabel: Record<string, string> = {
  ...routeStatusLabel,
  ...requestStatusLabel,
  ...subscriptionStatusLabel,
  ...planningSessionStatusLabel,
  ...profileStatusLabel,
  ...staffStatusLabel,
  ...busStatusLabel,
  ...routeAssignmentStatusLabel,
  ...tripStatusLabel,
  ...tripStopStatusLabel,
  ...tripStudentStatusLabel,
  ...attendanceStatusLabel,
  ...routeCalculationStatusLabel,
  ...routeReadinessStatusLabel,
  ...pickupDropoffReadinessStatusLabel,
  ...operationEventLabel,
};

/* -- Generic helper ---------------------------------------------- */
export function getLabel(
  map: Record<string, string>,
  key?: string | null
): string {
  if (!key) return '-';
  return map[key.toUpperCase()] || key;
}
