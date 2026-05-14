/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Client-side quiet hours (matches notification_service entity logic)
 */

import type { QuietHoursDto } from '../types/preference.types';

function parseTimeToMinutes(s: string | undefined): number | null {
  if (!s) return null;
  const m = /^(\d{1,2}):(\d{2})$/.exec(s.trim());
  if (!m) return null;
  const h = Number(m[1]);
  const min = Number(m[2]);
  if (h > 23 || min > 59) return null;
  return h * 60 + min;
}

export function isQuietHoursNow(quietHours: QuietHoursDto): boolean {
  if (!quietHours.enabled) {
    return false;
  }
  const start = parseTimeToMinutes(quietHours.start);
  const end = parseTimeToMinutes(quietHours.end);
  if (start === null || end === null) {
    return false;
  }
  const now = new Date();
  const cur = now.getHours() * 60 + now.getMinutes();
  if (start < end) {
    return cur >= start && cur < end;
  }
  return cur >= start || cur < end;
}

export function timeStringToMinutes(s: string): number | null {
  return parseTimeToMinutes(s);
}

export function minutesToTimeString(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}
