/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project — CRM working hours (minute-of-day) helpers
 */

import type { CrmDayOfWeek, WorkingHoursItem } from '../types';

export const CRM_DAYS_ORDER: readonly CrmDayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
] as const;

const DAY_LABELS: Record<CrmDayOfWeek, string> = {
  MONDAY: 'Monday',
  TUESDAY: 'Tuesday',
  WEDNESDAY: 'Wednesday',
  THURSDAY: 'Thursday',
  FRIDAY: 'Friday',
  SATURDAY: 'Saturday',
  SUNDAY: 'Sunday',
};

export function crmDayLabel(day: CrmDayOfWeek): string {
  return DAY_LABELS[day];
}

/** Formats minutes since midnight (0–1439 typical for display end exclusive logic handled separately). */
export function minutesToLabel(minutes: number | undefined): string {
  if (minutes === undefined || Number.isNaN(minutes)) {
    return '';
  }
  const clamped = Math.max(0, Math.min(1439, Math.round(minutes)));
  const h = Math.floor(clamped / 60);
  const m = clamped % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

/** Parses `HH:mm` or `H:mm` to minutes; use `allowEndOfDay` for end fields (`24:00` → 1440). */
export function parseTimeToMinutes(
  input: string,
  options?: { allowEndOfDay?: boolean }
): number | null {
  const s = input.trim();
  if (!s) return null;
  if (options?.allowEndOfDay && (s === '24:00' || s === '24:00:00')) {
    return 1440;
  }
  const match = /^(\d{1,2}):(\d{2})$/.exec(s);
  if (!match) return null;
  const h = Number(match[1]);
  const min = Number(match[2]);
  if (
    !Number.isFinite(h) ||
    !Number.isFinite(min) ||
    h < 0 ||
    h > 23 ||
    min < 0 ||
    min > 59
  ) {
    return null;
  }
  const total = h * 60 + min;
  if (total > 1439) return null;
  return total;
}

/** Label for end minute (allows 1440 → `24:00`). */
export function endMinuteToLabel(minutes: number | undefined): string {
  if (minutes === undefined || Number.isNaN(minutes)) {
    return '';
  }
  if (minutes >= 1440) {
    return '24:00';
  }
  return minutesToLabel(minutes);
}

/** Backend allows startMinute 0–1439, endMinute 0–1440. */
export function isValidStartMinute(v: number): boolean {
  return Number.isInteger(v) && v >= 0 && v <= 1439;
}

export function isValidEndMinute(v: number): boolean {
  return Number.isInteger(v) && v >= 0 && v <= 1440;
}

export function createEmptyWorkingHoursWeek(): WorkingHoursItem[] {
  return CRM_DAYS_ORDER.map((dayOfWeek) => ({
    dayOfWeek,
    workingDay: false,
  }));
}

/** Merge API rows with full week; unknown days are skipped. */
export function normalizeWorkingHoursWeek(
  rows: WorkingHoursItem[] | undefined
): WorkingHoursItem[] {
  const map = new Map<CrmDayOfWeek, WorkingHoursItem>();
  if (rows) {
    for (const row of rows) {
      map.set(row.dayOfWeek, row);
    }
  }
  return CRM_DAYS_ORDER.map((day) => {
    const existing = map.get(day);
    return existing
      ? { ...existing, dayOfWeek: day }
      : { dayOfWeek: day, workingDay: false };
  });
}

/** Omit entire workingHours when no row is a working day with valid range (optional POST/PATCH). */
export function toPersistableWorkingHours(
  rows: WorkingHoursItem[]
): WorkingHoursItem[] | undefined {
  const cleaned = rows.filter((r) => {
    if (!r.workingDay) return false;
    const start = r.startMinute;
    const end = r.endMinute;
    if (start === undefined || end === undefined) return false;
    return isValidStartMinute(start) && isValidEndMinute(end) && end > start;
  });
  return cleaned.length ? cleaned : undefined;
}

/** Split comma / newline separated strings into trimmed non-empty tags. */
export function parseCommaSeparatedList(raw: string): string[] {
  return raw
    .split(/[\n,]+/)
    .map((s) => s.trim())
    .filter(Boolean);
}

export function formatCommaSeparatedList(items: string[] | undefined): string {
  return items?.length ? items.join(', ') : '';
}
