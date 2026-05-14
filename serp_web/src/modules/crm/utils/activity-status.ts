/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM activity display status helpers
 */

import type { Activity, ActivityDisplayStatus } from '../types';

const getOverdueReferenceDate = (
  activity: Pick<Activity, 'scheduledDate' | 'followUpDate'>
) => activity.followUpDate || activity.scheduledDate;

export const isActivityOverdue = (
  activity: Pick<Activity, 'status' | 'scheduledDate' | 'followUpDate'>
): boolean => {
  if (activity.status !== 'PLANNED') {
    return false;
  }

  const overdueReference = getOverdueReferenceDate(activity);
  if (!overdueReference) {
    return false;
  }

  return new Date(overdueReference).getTime() < Date.now();
};

export const getActivityDisplayStatus = (
  activity: Pick<Activity, 'status' | 'scheduledDate' | 'followUpDate'>
): ActivityDisplayStatus => {
  if (isActivityOverdue(activity)) {
    return 'OVERDUE';
  }

  return activity.status;
};
