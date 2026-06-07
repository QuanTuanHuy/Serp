/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM local date helpers
 */

const DATE_ONLY_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

export function parseLocalDateValue(
  value?: number | string | Date | null
): Date | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? undefined : value;
  }

  if (typeof value === 'number') {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? undefined : date;
  }

  if (DATE_ONLY_PATTERN.test(value)) {
    const [year, month, day] = value.split('-').map((part) => Number(part));
    if (!year || !month || !day) {
      return undefined;
    }

    const date = new Date(year, month - 1, day);
    return Number.isNaN(date.getTime()) ? undefined : date;
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? undefined : parsed;
}

export function toLocalDateInputValue(
  value?: number | string | Date | null
): string {
  const date = parseLocalDateValue(value);
  if (!date) {
    return '';
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function toLocalDateEpoch(
  value?: number | string | Date | null,
  endOfDay = false
): number | undefined {
  const date = parseLocalDateValue(value);
  if (!date) {
    return undefined;
  }

  return new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
    endOfDay ? 23 : 0,
    endOfDay ? 59 : 0,
    endOfDay ? 59 : 0,
    endOfDay ? 999 : 0
  ).getTime();
}

export function fromLocalDateInputValue(
  value: string,
  endOfDay = false
): number | undefined {
  if (!value) {
    return undefined;
  }

  const date = parseLocalDateValue(value);
  if (!date) {
    return undefined;
  }

  return toLocalDateEpoch(date, endOfDay);
}

export function formatLocalDateLabel(
  value?: number | string | Date | null
): string {
  const date = parseLocalDateValue(value);
  if (!date) {
    return 'None';
  }

  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}
