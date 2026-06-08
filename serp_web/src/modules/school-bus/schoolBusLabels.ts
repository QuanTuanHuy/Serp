/**
 * Vietnamese label mappings for school-bus module enums.
 * Centralises all display labels so pages don't duplicate strings.
 */

/* ── Route status ──────────────────────────────────────────────── */
export const routeStatusLabel: Record<string, string> = {
  PLANNED: 'Đã lên kế hoạch',
  ASSIGNED: 'Đã phân công',
  PUBLISHED: 'Đã công bố',
  TRIP_CREATED: 'Đã tạo chuyến',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

/* ── Transport request status ──────────────────────────────────── */
export const requestStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  SUBMITTED: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
  CANCELLED: 'Đã hủy',
};

/* ── Subscription status ───────────────────────────────────────── */
export const subscriptionStatusLabel: Record<string, string> = {
  ACTIVE: 'Đang hoạt động',
  INACTIVE: 'Ngưng hoạt động',
  EXPIRED: 'Hết hạn',
  CANCELLED: 'Đã hủy',
};

/* ── Planning session status ───────────────────────────────────── */
export const sessionStatusLabel: Record<string, string> = {
  DRAFT: 'Nháp',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

/* ── Route direction ───────────────────────────────────────────── */
export const directionLabel: Record<string, string> = {
  TO_SCHOOL: 'Đến trường',
  FROM_SCHOOL: 'Về nhà',
};

/* ── Student usage type ────────────────────────────────────────── */
export const usageTypeLabel: Record<string, string> = {
  ONE_WAY_TO: 'Một chiều – Đến',
  ONE_WAY_FROM: 'Một chiều – Về',
  ROUND_TRIP: 'Hai chiều',
};

/* ── Generic helper ────────────────────────────────────────────── */
export function getLabel(map: Record<string, string>, key?: string | null): string {
  if (!key) return '—';
  return map[key.toUpperCase()] ?? key;
}
