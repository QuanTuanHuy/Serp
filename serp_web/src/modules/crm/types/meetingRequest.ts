/**
 * Meeting request domain types (CRM scheduling queue).
 * Mirrors backend `MeetingRequest*` DTOs / enums.
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project
 */

import type { PreferredTimeSlot } from './customer';

export type MeetingRequestType =
  | 'DISCOVERY'
  | 'DEMO'
  | 'PROPOSAL'
  | 'NEGOTIATION'
  | 'QBR';

export type MeetingRequestStatus =
  | 'PENDING'
  | 'SCHEDULED'
  | 'FAILED'
  | 'CANCELLED';

export interface MeetingRequest {
  id: string;
  teamId: string;
  preferredUserId?: string;
  assignedTeamMemberId?: string;
  assignedUserId?: string;
  scheduledActivityId?: string;
  scheduledStartTime?: number;
  accountId: string;
  accountName?: string;
  opportunityId?: string;
  opportunityName?: string;
  contactId?: string;
  contactName?: string;
  subject?: string;
  description?: string;
  location?: string;
  meetingType: MeetingRequestType;
  preferredTimeSlot?: PreferredTimeSlot;
  earliestStart: number;
  latestStart: number;
  requestedDeadline: number;
  durationMinutes?: number;
  status: MeetingRequestStatus;
  schedulingAttempts?: number;
  priorityScore?: number;
  failureReason?: string;
  tenantId?: string;
  createdAt?: number;
  updatedAt?: number;
  createdBy?: string;
  updatedBy?: string;
}

/** POST `/meeting-requests` body (camelCase). */
export interface CreateMeetingRequestPayload {
  teamId: number;
  accountId: number;
  opportunityId?: number;
  contactId?: number;
  preferredUserId?: number;
  subject?: string;
  description?: string;
  location?: string;
  meetingType: MeetingRequestType;
  preferredTimeSlot?: PreferredTimeSlot;
  earliestStart: number;
  latestStart: number;
  requestedDeadline: number;
  durationMinutes?: number;
}

export const MEETING_REQUEST_TYPE_LABELS: Record<MeetingRequestType, string> = {
  DISCOVERY: 'Discovery',
  DEMO: 'Demo',
  PROPOSAL: 'Proposal',
  NEGOTIATION: 'Negotiation',
  QBR: 'QBR',
};

export const MEETING_REQUEST_STATUS_LABELS: Record<
  MeetingRequestStatus,
  string
> = {
  PENDING: 'Pending',
  SCHEDULED: 'Scheduled',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
};
