/**
 * English label mappings for school-bus module enums.
 * Centralises all display labels so pages don't duplicate strings.
 */

/* ── Route status ──────────────────────────────────────────────── */
export const routeStatusLabel: Record<string, string> = {
  DRAFT: 'Draft',
  GENERATED: 'Generated',
  REVIEWING: 'Reviewing',
  PLANNED: 'Planned',
  ASSIGNED: 'Assigned',
  PUBLISHED: 'Published',
  TRIP_CREATED: 'Trip Created',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

/* ── Transport request status ──────────────────────────────────── */
export const requestStatusLabel: Record<string, string> = {
  DRAFT: 'Draft',
  SUBMITTED: 'Submitted',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  CANCELLED: 'Cancelled',
};

/* ── Subscription status ───────────────────────────────────────── */
export const subscriptionStatusLabel: Record<string, string> = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  EXPIRED: 'Expired',
  CANCELLED: 'Cancelled',
};

/* ── Planning session status ───────────────────────────────────── */
export const sessionStatusLabel: Record<string, string> = {
  DRAFT: 'Draft',
  IN_PROGRESS: 'In Progress',
  PUBLISHED: 'Published',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

/* ── Route direction ───────────────────────────────────────────── */
export const directionLabel: Record<string, string> = {
  TO_SCHOOL: 'Outbound',
  FROM_SCHOOL: 'Return',
  OUTBOUND: 'Outbound',
  RETURN: 'Return',
};

/* ── Student usage type ────────────────────────────────────────── */
export const usageTypeLabel: Record<string, string> = {
  ONE_WAY_TO: 'One Way - To School',
  ONE_WAY_FROM: 'One Way - From School',
  ROUND_TRIP: 'Round Trip',
  ONE_WAY: 'One Way',
};

/* ── Generic helper ────────────────────────────────────────────── */
export function getLabel(
  map: Record<string, string>,
  key?: string | null
): string {
  if (!key) return '-';
  return map[key.toUpperCase()] ?? key;
}
