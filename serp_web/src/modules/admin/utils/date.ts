/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin date formatting helpers
 */

export type AdminDateValue = Date | number | string | null | undefined;

function toDate(value: AdminDateValue): Date | null {
  if (value === null || value === undefined || value === '') {
    return null;
  }

  const rawValue =
    typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value;
  const date = new Date(rawValue);

  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatAdminDate(
  value: AdminDateValue,
  fallback = 'N/A'
): string {
  const date = toDate(value);

  if (!date) {
    return fallback;
  }

  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}
