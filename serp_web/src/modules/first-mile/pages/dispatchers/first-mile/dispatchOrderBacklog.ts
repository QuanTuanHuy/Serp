/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile dispatch backlog helpers
 */

import type { FirstMileOrderDetail } from '../../../types';

const parseDate = (value?: string): Date | null => {
  if (!value) {
    return null;
  }

  const parsedDate = new Date(value);
  return Number.isNaN(parsedDate.getTime()) ? null : parsedDate;
};

const toComparableTime = (value?: string): number => {
  return parseDate(value)?.getTime() ?? Number.MAX_SAFE_INTEGER;
};

export const isPickupBacklogOrder = (
  order: Pick<FirstMileOrderDetail, 'pickupTimeEnd'>,
  referenceTime: Date = new Date()
): boolean => {
  const pickupEnd = parseDate(order.pickupTimeEnd);
  return pickupEnd !== null && pickupEnd.getTime() < referenceTime.getTime();
};

export const getPickupBacklogMinutes = (
  order: Pick<FirstMileOrderDetail, 'pickupTimeEnd'>,
  referenceTime: Date = new Date()
): number | null => {
  const pickupEnd = parseDate(order.pickupTimeEnd);
  if (!pickupEnd || pickupEnd.getTime() >= referenceTime.getTime()) {
    return null;
  }

  return Math.max(
    1,
    Math.floor((referenceTime.getTime() - pickupEnd.getTime()) / 60000)
  );
};

export const formatPickupBacklogDuration = (minutes: number | null): string => {
  if (minutes === null) {
    return '';
  }

  if (minutes < 60) {
    return `Quá hạn ${minutes} phút`;
  }

  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  if (hours < 24) {
    return remainingMinutes > 0
      ? `Quá hạn ${hours} giờ ${remainingMinutes} phút`
      : `Quá hạn ${hours} giờ`;
  }

  const days = Math.floor(hours / 24);
  const remainingHours = hours % 24;
  return remainingHours > 0
    ? `Quá hạn ${days} ngày ${remainingHours} giờ`
    : `Quá hạn ${days} ngày`;
};

export const formatPickupWindow = (
  order: Pick<FirstMileOrderDetail, 'pickupTimeStart' | 'pickupTimeEnd'>
): string => {
  const start = parseDate(order.pickupTimeStart);
  const end = parseDate(order.pickupTimeEnd);

  if (!start && !end) {
    return '--';
  }

  const formatter = new Intl.DateTimeFormat('vi-VN', {
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });

  if (start && end) {
    return `${formatter.format(start)} - ${formatter.format(end)}`;
  }

  if (start) {
    return `Từ ${formatter.format(start)}`;
  }

  return `Đến ${formatter.format(end as Date)}`;
};

export const countPickupBacklogOrders = (
  orders: FirstMileOrderDetail[],
  referenceTime: Date = new Date()
): number => {
  return orders.filter((order) => isPickupBacklogOrder(order, referenceTime))
    .length;
};

export const sortOrdersByBacklogPriority = (
  orders: FirstMileOrderDetail[],
  referenceTime: Date = new Date()
): FirstMileOrderDetail[] => {
  return [...orders].sort((first, second) => {
    const firstBacklog = isPickupBacklogOrder(first, referenceTime);
    const secondBacklog = isPickupBacklogOrder(second, referenceTime);
    if (firstBacklog !== secondBacklog) {
      return firstBacklog ? -1 : 1;
    }

    const pickupCompare =
      toComparableTime(first.pickupTimeEnd) -
      toComparableTime(second.pickupTimeEnd);
    if (pickupCompare !== 0) {
      return pickupCompare;
    }

    return first.id - second.id;
  });
};
