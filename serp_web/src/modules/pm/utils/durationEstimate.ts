/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM duration estimate helpers
 */

export const ESTIMATE_MINUTES_PER_HOUR = 60;
export const ESTIMATE_HOURS_PER_DAY = 8;
export const ESTIMATE_DAYS_PER_WEEK = 5;

const UNIT_TO_MINUTES = {
  w:
    ESTIMATE_DAYS_PER_WEEK * ESTIMATE_HOURS_PER_DAY * ESTIMATE_MINUTES_PER_HOUR,
  d: ESTIMATE_HOURS_PER_DAY * ESTIMATE_MINUTES_PER_HOUR,
  h: ESTIMATE_MINUTES_PER_HOUR,
  m: 1,
} as const;

export function parseDurationEstimate(value: string): number | null {
  const trimmed = value.trim().toLowerCase();

  if (!trimmed) {
    return null;
  }

  if (/^\d+$/.test(trimmed)) {
    return Number(trimmed);
  }

  const compact = trimmed.replace(/\s+/g, '');
  const tokenPattern = /(\d+)([wdhm])/g;
  let total = 0;
  let consumed = '';
  let match: RegExpExecArray | null;

  while ((match = tokenPattern.exec(compact)) !== null) {
    const amount = Number(match[1]);
    const unit = match[2] as keyof typeof UNIT_TO_MINUTES;
    total += amount * UNIT_TO_MINUTES[unit];
    consumed += match[0];
  }

  if (consumed !== compact) {
    return Number.NaN;
  }

  return total;
}

export function formatDurationEstimate(value?: number | null): string {
  if (value === null || value === undefined) {
    return 'None';
  }

  let remaining = Math.round(value);
  const weeks = Math.floor(remaining / UNIT_TO_MINUTES.w);
  remaining -= weeks * UNIT_TO_MINUTES.w;
  const days = Math.floor(remaining / UNIT_TO_MINUTES.d);
  remaining -= days * UNIT_TO_MINUTES.d;
  const hours = Math.floor(remaining / UNIT_TO_MINUTES.h);
  const minutes = remaining - hours * UNIT_TO_MINUTES.h;

  const parts = [
    weeks ? `${weeks}w` : '',
    days ? `${days}d` : '',
    hours ? `${hours}h` : '',
    minutes ? `${minutes}m` : '',
  ].filter(Boolean);

  return parts.length ? parts.join(' ') : '0m';
}
