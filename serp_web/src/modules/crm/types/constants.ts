// CRM Constants (authors: QuanTuanHuy, Description: Part of Serp Project)

import type {
  CustomerType,
  CustomerStatus,
  LeadSource,
  LeadStatus,
  OpportunityStage,
  OpportunityType,
  ActivityType,
  ActivityStatus,
  Priority,
} from './index';

// Customer constants
export const CUSTOMER_TYPES: { value: CustomerType; label: string }[] = [
  { value: 'INDIVIDUAL', label: 'Individual' },
  { value: 'COMPANY', label: 'Company' },
];

export const CUSTOMER_STATUSES: {
  value: CustomerStatus;
  label: string;
  color: string;
}[] = [
  { value: 'ACTIVE', label: 'Active', color: 'green' },
  { value: 'INACTIVE', label: 'Inactive', color: 'gray' },
  { value: 'POTENTIAL', label: 'Potential', color: 'blue' },
  { value: 'BLOCKED', label: 'Blocked', color: 'red' },
];

// Lead constants
export const LEAD_SOURCES: {
  value: LeadSource;
  label: string;
  icon: string;
}[] = [
  { value: 'WEBSITE', label: 'Website', icon: 'Globe' },
  { value: 'REFERRAL', label: 'Referral', icon: 'Users' },
  { value: 'EMAIL', label: 'Email', icon: 'Mail' },
  { value: 'PHONE', label: 'Phone', icon: 'Phone' },
  { value: 'SOCIAL_MEDIA', label: 'Social Media', icon: 'Share' },
  { value: 'TRADE_SHOW', label: 'Trade Show', icon: 'Calendar' },
  { value: 'OTHER', label: 'Other', icon: 'MoreHorizontal' },
];

export const LEAD_STATUSES: {
  value: LeadStatus;
  label: string;
  color: string;
}[] = [
  { value: 'NEW', label: 'New', color: 'blue' },
  { value: 'CONTACTED', label: 'Contacted', color: 'yellow' },
  { value: 'QUALIFIED', label: 'Qualified', color: 'purple' },
  { value: 'CONVERTED', label: 'Converted', color: 'green' },
  { value: 'LOST', label: 'Lost', color: 'red' },
];

// Opportunity constants
export const OPPORTUNITY_STAGES: {
  value: OpportunityStage;
  label: string;
  color: string;
  probability: number;
}[] = [
  {
    value: 'PROSPECTING',
    label: 'Prospecting',
    color: 'blue',
    probability: 10,
  },
  {
    value: 'QUALIFICATION',
    label: 'Qualification',
    color: 'indigo',
    probability: 25,
  },
  { value: 'PROPOSAL', label: 'Proposal', color: 'purple', probability: 50 },
  {
    value: 'NEGOTIATION',
    label: 'Negotiation',
    color: 'yellow',
    probability: 75,
  },
  {
    value: 'CLOSED_WON',
    label: 'Closed Won',
    color: 'green',
    probability: 100,
  },
  { value: 'CLOSED_LOST', label: 'Closed Lost', color: 'red', probability: 0 },
];

export const OPPORTUNITY_TYPES: { value: OpportunityType; label: string }[] = [
  { value: 'NEW_BUSINESS', label: 'New Business' },
  { value: 'EXISTING_BUSINESS', label: 'Existing Business' },
  { value: 'RENEWAL', label: 'Renewal' },
];

// Activity constants
export const ACTIVITY_TYPES: {
  value: ActivityType;
  label: string;
  icon: string;
  color: string;
}[] = [
  { value: 'CALL', label: 'Call', icon: 'Phone', color: 'blue' },
  { value: 'EMAIL', label: 'Email', icon: 'Mail', color: 'purple' },
  { value: 'MEETING', label: 'Meeting', icon: 'Users', color: 'green' },
  { value: 'TASK', label: 'Task', icon: 'CheckSquare', color: 'orange' },
  { value: 'NOTE', label: 'Note', icon: 'FileText', color: 'gray' },
  { value: 'DEMO', label: 'Demo', icon: 'Monitor', color: 'indigo' },
  { value: 'PROPOSAL', label: 'Proposal', icon: 'File', color: 'yellow' },
  { value: 'FOLLOW_UP', label: 'Follow Up', icon: 'ArrowRight', color: 'cyan' },
];

export const ACTIVITY_STATUSES: {
  value: ActivityStatus;
  label: string;
  color: string;
}[] = [
  { value: 'PLANNED', label: 'Planned', color: 'blue' },
  { value: 'IN_PROGRESS', label: 'In Progress', color: 'yellow' },
  { value: 'COMPLETED', label: 'Completed', color: 'green' },
  { value: 'CANCELLED', label: 'Cancelled', color: 'gray' },
  { value: 'OVERDUE', label: 'Overdue', color: 'red' },
];

// Priority constants
export const PRIORITIES: {
  value: Priority;
  label: string;
  color: string;
  icon: string;
}[] = [
  { value: 'LOW', label: 'Low', color: 'green', icon: 'ArrowDown' },
  { value: 'MEDIUM', label: 'Medium', color: 'yellow', icon: 'Minus' },
  { value: 'HIGH', label: 'High', color: 'orange', icon: 'ArrowUp' },
  { value: 'URGENT', label: 'Urgent', color: 'red', icon: 'AlertTriangle' },
];

// Configuration constants
export const CRM_CONFIG = {
  pagination: {
    defaultPageSize: 20,
    pageSizeOptions: [10, 20, 50, 100],
  },

  filters: {
    debounceMs: 300,
    maxSelectedItems: 50,
  },

  validation: {
    maxNameLength: 100,
    maxEmailLength: 255,
    maxPhoneLength: 20,
    maxNotesLength: 2000,
    maxTagLength: 50,
    maxTags: 10,
  },

  ui: {
    maxRecentItems: 10,
    autoSaveDelayMs: 2000,
    notificationDurationMs: 5000,
  },

  charts: {
    defaultColors: [
      '#3B82F6',
      '#8B5CF6',
      '#10B981',
      '#F59E0B',
      '#EF4444',
      '#6B7280',
      '#EC4899',
      '#14B8A6',
    ],
    animationDurationMs: 300,
  },

  calendar: {
    defaultView: 'week' as const,
    timeFormat: '24h' as const,
    firstDayOfWeek: 1, // Monday
    workingHours: { start: 9, end: 17 },
  },

  export: {
    maxRows: 10000,
    formats: ['csv', 'xlsx', 'pdf'] as const,
    timeout: 30000, // 30 seconds
  },
} as const;

// Default values for forms
export const DEFAULT_VALUES = {
  customer: {
    customerType: 'INDIVIDUAL' as CustomerType,
    status: 'POTENTIAL' as CustomerStatus,
    tags: [],
    customFields: {},
    isActive: true,
  },

  lead: {
    status: 'NEW' as LeadStatus,
    priority: 'MEDIUM' as Priority,
    source: 'WEBSITE' as LeadSource,
    tags: [],
    customFields: {},
    isActive: true,
  },

  opportunity: {
    stage: 'PROSPECTING' as OpportunityStage,
    type: 'NEW_BUSINESS' as OpportunityType,
    probability: 10,
    tags: [],
    products: [],
    customFields: {},
    isActive: true,
  },

  activity: {
    type: 'CALL' as ActivityType,
    status: 'PLANNED' as ActivityStatus,
    priority: 'MEDIUM' as Priority,
    duration: 30,
    tags: [],
    customFields: {},
    isActive: true,
  },
} as const;

// Utility functions for constants
export const getStatusColor = (
  status: string,
  type: 'customer' | 'lead' | 'opportunity' | 'activity'
): string => {
  switch (type) {
    case 'customer':
      return CUSTOMER_STATUSES.find((s) => s.value === status)?.color || 'gray';
    case 'lead':
      return LEAD_STATUSES.find((s) => s.value === status)?.color || 'gray';
    case 'opportunity':
      return (
        OPPORTUNITY_STAGES.find((s) => s.value === status)?.color || 'gray'
      );
    case 'activity':
      return ACTIVITY_STATUSES.find((s) => s.value === status)?.color || 'gray';
    default:
      return 'gray';
  }
};

export const getPriorityColor = (priority: Priority): string => {
  return PRIORITIES.find((p) => p.value === priority)?.color || 'gray';
};

export const getActivityTypeIcon = (type: ActivityType): string => {
  return ACTIVITY_TYPES.find((t) => t.value === type)?.icon || 'Circle';
};

export const getLeadSourceIcon = (source: LeadSource): string => {
  return LEAD_SOURCES.find((s) => s.value === source)?.icon || 'Circle';
};

export const getOpportunityProbability = (stage: OpportunityStage): number => {
  return OPPORTUNITY_STAGES.find((s) => s.value === stage)?.probability || 0;
};
