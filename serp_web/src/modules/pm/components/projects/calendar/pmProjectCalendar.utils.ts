/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project calendar helpers
 */

import moment from 'moment';

export type PMProjectCalendarView = 'week' | 'month';
export type PMProjectCalendarMode = 'schedule' | 'deadline';

export const DEFAULT_SCHEDULE_EFFORT_MILLIS = 8 * 60 * 60 * 1000;

export type PMProjectCalendarDragData =
  | {
      type: 'unscheduled-work-item';
      workItemId: number;
    }
  | {
      type: 'calendar-day';
      dayStart: number;
    };

const VIETNAM_UTC_OFFSET_MINUTES = 7 * 60;

export function toVietnamMoment(value: number | Date) {
  return moment(value).utcOffset(VIETNAM_UTC_OFFSET_MINUTES);
}

export function getProjectCalendarDragData(
  value: unknown
): PMProjectCalendarDragData | undefined {
  if (!value || typeof value !== 'object' || !('type' in value)) {
    return undefined;
  }

  const data = value as PMProjectCalendarDragData;
  if (
    data.type === 'unscheduled-work-item' &&
    typeof data.workItemId === 'number'
  ) {
    return data;
  }
  if (data.type === 'calendar-day' && typeof data.dayStart === 'number') {
    return data;
  }
  return undefined;
}

export function getDefaultDroppedScheduleRange(dayStart: number) {
  const start = toVietnamMoment(dayStart)
    .startOf('day')
    .hour(9)
    .minute(0)
    .second(0)
    .millisecond(0);
  const end = start.clone().hour(17);

  return {
    plannedStart: start.valueOf(),
    plannedEnd: end.valueOf(),
  };
}

export function getDefaultDroppedScheduleEffort(
  timeRemainingEstimate?: number | null,
  timeOriginalEstimate?: number | null
) {
  if (typeof timeRemainingEstimate === 'number' && timeRemainingEstimate > 0) {
    return timeRemainingEstimate;
  }
  if (typeof timeOriginalEstimate === 'number' && timeOriginalEstimate > 0) {
    return timeOriginalEstimate;
  }
  return DEFAULT_SCHEDULE_EFFORT_MILLIS;
}

export function getCalendarViewport(date: Date, view: PMProjectCalendarView) {
  const anchor = toVietnamMoment(date);
  const start =
    view === 'month'
      ? anchor.clone().startOf('month').startOf('isoWeek')
      : anchor.clone().startOf('isoWeek');
  const end =
    view === 'month'
      ? anchor.clone().endOf('month').endOf('isoWeek')
      : anchor.clone().endOf('isoWeek');

  return {
    viewportStart: start.valueOf(),
    viewportEnd: end.valueOf(),
  };
}

export function getVisibleCalendarDays(
  date: Date,
  view: PMProjectCalendarView,
  options?: { includeWeekends?: boolean }
) {
  const { viewportStart, viewportEnd } = getCalendarViewport(date, view);
  const days: moment.Moment[] = [];
  const cursor = toVietnamMoment(viewportStart).startOf('day');
  const end = toVietnamMoment(viewportEnd).startOf('day');

  while (cursor.isSameOrBefore(end, 'day')) {
    if (options?.includeWeekends || cursor.isoWeekday() <= 5) {
      days.push(cursor.clone());
    }
    cursor.add(1, 'day');
  }

  return days;
}

export function getCalendarDayKey(value?: number | null) {
  if (typeof value !== 'number') return null;
  return toVietnamMoment(value).format('YYYY-MM-DD');
}

export function formatCalendarDateRange(
  value?: number | null,
  fallback = 'No date'
) {
  if (typeof value !== 'number') return fallback;
  const date = toVietnamMoment(value);
  if (!date.isValid()) return fallback;
  return date.format('DD/MM/YYYY HH:mm');
}

export function formatCalendarTime(value?: number | null) {
  if (typeof value !== 'number') return '';
  const date = toVietnamMoment(value);
  return date.isValid() ? date.format('HH:mm') : '';
}

export function toCalendarDateTimeInputValue(value?: number | null) {
  if (typeof value !== 'number') return '';
  const date = toVietnamMoment(value);
  return date.isValid() ? date.format('YYYY-MM-DDTHH:mm') : '';
}

export function fromCalendarDateTimeInputValue(value: string) {
  if (!value) return null;
  const date = moment(value).utcOffset(VIETNAM_UTC_OFFSET_MINUTES, true);
  return date.isValid() ? date.valueOf() : null;
}

export function formatEffort(value?: number | null) {
  if (!value) return '0m';
  const minutes = Math.round(value / 60000);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours}h ${remainder}m` : `${hours}h`;
}

export function parseNumberList(value: string | null): number[] {
  if (!value) return [];
  return value
    .split(',')
    .map((part) => Number(part))
    .filter((item) => Number.isFinite(item));
}

export function serializeNumberList(values: number[]): string | undefined {
  return values.length ? values.join(',') : undefined;
}

export function countCalendarFilters(values: number[][]) {
  return values.reduce((total, value) => total + value.length, 0);
}
