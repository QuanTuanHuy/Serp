// Activity Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { BaseEntity, Priority } from './base';

export type ActivityType =
  | 'CALL'
  | 'EMAIL'
  | 'MEETING'
  | 'TASK'
  | 'NOTE'
  | 'DEMO'
  | 'PROPOSAL'
  | 'FOLLOW_UP';

export type ActivityStatus = 'PLANNED' | 'COMPLETED' | 'CANCELLED';
export type ActivityDisplayStatus = ActivityStatus | 'OVERDUE';

export type ActivityOutcome =
  | 'REACHED'
  | 'VOICEMAIL'
  | 'NO_ANSWER'
  | 'BUSY'
  | 'WRONG_NUMBER'
  | 'OCCURRED'
  | 'NO_SHOW'
  | 'RESCHEDULED'
  | 'CANCELLED_BY_CUSTOMER';

export interface Activity extends BaseEntity {
  type: ActivityType;
  status: ActivityStatus;
  subject: string;
  description?: string;
  scheduledDate?: string;
  actualDate?: string;
  duration?: number; // in minutes
  priority: Priority;
  assignedTo: string;
  assignedToName: string;
  relatedTo: {
    type: 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY';
    id: string;
    name: string;
  };
  participants?: string[];
  location?: string;
  outcome?: string;
  followUpRequired?: boolean;
  followUpDate?: string;
  tags: string[];
  customFields: Record<string, any>;
}

export type Contact = import('../components/contacts/ContactCard').Contact;

// Filter types
export interface ActivityFilters {
  keyword?: string;
  types?: ActivityType[];
  statuses?: ActivityStatus[];
  priorities?: Priority[];
  assignedTo?: string;
  leadId?: string;
  accountId?: string;
  opportunityId?: string;
  contactId?: string;
  activityDateFrom?: string;
  activityDateTo?: string;
  dueDateFrom?: string;
  dueDateTo?: string;
}

export interface ActivityStats {
  total: number;
  overdue: number;
  upcoming: number;
  byStatus: Partial<Record<ActivityStatus, number>>;
  byType: Partial<Record<ActivityType, number>>;
  byPriority: Partial<Record<Priority, number>>;
}

// Backend activity types (subset for API)
export type BackendActivityType = 'CALL' | 'EMAIL' | 'MEETING' | 'TASK';
export type BackendActivityStatus = ActivityStatus;

// Request types
export interface CreateActivityRequest {
  subject: string;
  description?: string;
  activityType: BackendActivityType;
  status?: BackendActivityStatus;
  location?: string;
  leadId?: number;
  accountId?: number;
  opportunityId?: number;
  contactId?: number;
  assignedTo?: number;
  activityDate?: number;
  dueDate?: number;
  reminderDate?: number;
  durationMinutes?: number;
  priority?: Priority;
  progressPercent?: number;
  notes?: string;
  outcome?: ActivityOutcome;
  attachments?: string[];
}

export type UpdateActivityRequest = Partial<CreateActivityRequest>;

export interface CompleteActivityRequest {
  outcome?: ActivityOutcome;
  notes?: string;
}

export interface RescheduleActivityRequest {
  dueDate: number;
  reminderDate?: number;
}

// Bulk operations
export type BulkActivityAction = 'COMPLETE' | 'CANCEL' | 'DELETE' | 'ASSIGN';

export interface BulkActivityRequest {
  activityIds: string[];
  action: BulkActivityAction;
  assigneeId?: string;
}

export interface BulkActivityResult {
  successCount: number;
  failedCount: number;
  message?: string;
}
