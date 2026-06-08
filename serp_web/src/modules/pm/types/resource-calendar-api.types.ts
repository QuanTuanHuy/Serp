/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar API types
 */

export type PMResourceCalendarExceptionType =
  | 'UNAVAILABLE'
  | 'CAPACITY_OVERRIDE';

export interface PMResourceCalendarBlockApi {
  id: number;
  profileId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  capacityFactor: number;
}

export interface PMResourceCalendarProfileApi {
  id: number;
  tenantId: number;
  name: string;
  description: string | null;
  timezone: string;
  isDefault: boolean;
  assignmentCount: number;
  blocks: PMResourceCalendarBlockApi[];
}

export interface PMResourceCalendarAssignmentApi {
  id: number;
  userId: number;
  profileId: number;
  effectiveFrom: string;
  effectiveTo: string | null;
}

export interface PMResourceCalendarExceptionApi {
  id: number;
  userId: number;
  exceptionType: PMResourceCalendarExceptionType;
  startAt: number;
  endAt: number;
  capacityFactor: number | null;
  reason: string | null;
}

export interface PMResourceCalendarSettingsOverviewApi {
  profiles: PMResourceCalendarProfileApi[];
  assignments: PMResourceCalendarAssignmentApi[];
  upcomingExceptions: PMResourceCalendarExceptionApi[];
  unassignedUserIds: number[];
  materializedWindowStart: number | null;
  materializedWindowEnd: number | null;
  fetchedAt: number;
}

export interface PMResourceCalendarDeleteResultApi {
  deleted: boolean;
}

export interface PMCreateResourceCalendarProfileRequest {
  name: string;
  description: string | null;
  timezone: string;
  isDefault: boolean;
}

export type PMUpdateResourceCalendarProfileRequest =
  Partial<PMCreateResourceCalendarProfileRequest>;

export interface PMReplaceResourceCalendarBlocksRequest {
  blocks: Array<{
    dayOfWeek: number;
    startTime: string;
    endTime: string;
    capacityFactor: number;
  }>;
}

export interface PMReplaceResourceCalendarAssignmentsRequest {
  assignments: Array<{
    userId: number;
    effectiveFrom: string;
    effectiveTo: string | null;
  }>;
}

export interface PMCreateResourceCalendarExceptionRequest {
  userId: number;
  exceptionType: PMResourceCalendarExceptionType;
  startAt: number;
  endAt: number;
  capacityFactor: number | null;
  reason: string | null;
}

export type PMUpdateResourceCalendarExceptionRequest =
  Partial<PMCreateResourceCalendarExceptionRequest>;
