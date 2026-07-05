/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM Calendar Helpers
 */

import moment from 'moment';

export type CRMCalendarView = 'week' | 'month';

const VIETNAM_UTC_OFFSET_MINUTES = 7 * 60;

export function toVietnamMoment(value: number | Date | string) {
  return moment(value).utcOffset(VIETNAM_UTC_OFFSET_MINUTES);
}

export function getCalendarViewport(
  date: Date | string | number,
  view: CRMCalendarView
) {
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
  date: Date | string | number,
  view: CRMCalendarView
) {
  const { viewportStart, viewportEnd } = getCalendarViewport(date, view);
  const days: moment.Moment[] = [];
  const cursor = toVietnamMoment(viewportStart).startOf('day');
  const end = toVietnamMoment(viewportEnd).startOf('day');

  while (cursor.isSameOrBefore(end, 'day')) {
    days.push(cursor.clone());
    cursor.add(1, 'day');
  }

  return days;
}

export function getCalendarDayKey(value?: string | number | null) {
  if (!value) return null;
  return toVietnamMoment(value).format('YYYY-MM-DD');
}
