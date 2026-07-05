/**
 * Vietnamese label mappings for school-bus module enums.
 * Centralises all display labels so pages don't duplicate strings.
 */

/* -- Route status ------------------------------------------------ */
export const routeStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  GENERATED: 'Đã tạo',
  REVIEWING: 'Đang rà soát',
  PLANNED: 'Đã lập kế hoạch',
  ASSIGNED: 'Đã phân công',
  PUBLISHED: 'Đã phát hành',
  TRIP_CREATED: 'Đã tạo chuyến',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
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
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngừng hoạt động',
  EXPIRED: 'Hết hạn',
  CANCELLED: 'Đã hủy',
  PAUSED: 'Tạm dừng',
  STOPPED: 'Đã dừng',
  SCHEDULED: 'Đã lên lịch',
};

/* -- Planning session status ------------------------------------- */
export const sessionStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  IN_PROGRESS: 'Đang thực hiện',
  PUBLISHED: 'Đã phát hành',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

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
};

export const tripStatusLabel: Record<string, string> = {
  CREATED: 'Đã tạo',
  SCHEDULED: 'Đã lên lịch',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

export const tripStopStatusLabel: Record<string, string> = {
  CREATED: 'Đã tạo',
  PENDING: 'Đang chờ',
  ARRIVED: 'Đã đến điểm',
  BOARDING: 'Đang đón/trả',
  DEPARTED: 'Đã rời điểm',
  SKIPPED: 'Đã bỏ qua',
  COMPLETED: 'Hoàn thành',
};

export const tripStudentStatusLabel: Record<string, string> = {
  CREATED: 'Đã tạo',
  WAITING: 'Đang chờ',
  BOARDED: 'Đã lên xe',
  DROPPED_OFF: 'Đã xuống xe',
  ABSENT: 'Vắng mặt',
  NO_SHOW: 'Không có mặt',
  NOT_SERVED: 'Chưa phục vụ',
};

export const attendanceTypeLabel: Record<string, string> = {
  BOARDING: 'Lên xe',
  DROPOFF: 'Xuống xe',
  ABSENCE: 'Vắng mặt',
};

export const attendanceStatusLabel: Record<string, string> = {
  BOARDED: 'Đã lên xe',
  DROPPED_OFF: 'Đã xuống xe',
  ABSENT: 'Vắng mặt',
  NO_SHOW: 'Không có mặt',
  NOT_SERVED: 'Chưa phục vụ',
};

export const statusLabel: Record<string, string> = {
  ...routeStatusLabel,
  ...requestStatusLabel,
  ...subscriptionStatusLabel,
  ...sessionStatusLabel,
  ...profileStatusLabel,
  ...staffStatusLabel,
  ...busStatusLabel,
  ...tripStatusLabel,
  ...tripStopStatusLabel,
  ...tripStudentStatusLabel,
  ...attendanceStatusLabel,
};

/* -- Generic helper ---------------------------------------------- */
export function getLabel(
  map: Record<string, string>,
  key?: string | null
): string {
  if (!key) return '-';
  return map[key.toUpperCase()] || key;
}
