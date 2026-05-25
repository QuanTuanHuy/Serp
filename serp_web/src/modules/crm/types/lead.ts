// Lead Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { BaseEntity, Priority } from './base';

export type LeadSource =
  | 'WEBSITE'
  | 'SOCIAL_MEDIA'
  | 'REFERRAL'
  | 'COLD_CALL'
  | 'EMAIL_CAMPAIGN'
  // Temporary compatibility values for legacy lead UI/mock code.
  | 'EMAIL'
  | 'PHONE'
  | 'TRADE_SHOW'
  | 'OTHER';

export type LeadStatus =
  | 'NEW'
  | 'CONTACTED'
  | 'NURTURING'
  | 'QUALIFIED'
  | 'DISQUALIFIED'
  | 'CONVERTED'
  // Temporary compatibility value for legacy lead UI/mock code.
  | 'LOST';

export interface LeadAddress {
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}

export interface Lead extends BaseEntity {
  name?: string;
  email: string;
  phone?: string;
  company?: string;
  industry?: string;
  companySize?: string;
  website?: string;
  jobTitle?: string;
  address?: LeadAddress;
  leadSource?: LeadSource;
  leadStatus?: LeadStatus;
  assignedTo?: string;
  estimatedValue?: number;
  leadScore?: number;
  followUpDate?: string;
  notes?: string;

  convertedOpportunityId?: string;
  convertedAccountId?: string;
  tenantId?: string;
  createdBy?: string;
  updatedBy?: string;

  // Temporary compatibility fields for legacy CRM lead UI.
  firstName: string;
  lastName: string;
  source: LeadSource;
  status: LeadStatus;
  priority: Priority;
  expectedCloseDate?: string;
  tags: string[];
  customFields: Record<string, any>;
  conversionDate?: string;
  convertedToCustomerId?: string;
  lastActivityDate?: string;
}

// Filter types
export interface LeadFilters {
  search?: string;
  status?: LeadStatus[];
  source?: LeadSource[];
  assignedTo?: string[];
  industries?: string[];
  createdDateFrom?: string;
  createdDateTo?: string;
  followUpDateFrom?: string;
  followUpDateTo?: string;
  minValue?: number;
  maxValue?: number;
  minScore?: number;
  maxScore?: number;
  country?: string;
  city?: string;
  qualifiedOnly?: boolean;
  convertedOnly?: boolean;
  unassignedOnly?: boolean;
  hasEmail?: boolean;
  hasPhone?: boolean;
}

// Request types
export type CreateLeadRequest = {
  firstName: string;
  lastName: string;
  company?: string;
  industry?: string;
  companySize?: string;
  website?: string;
  name: string;
  email: string;
  phone?: string;
  jobTitle?: string;
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  leadSource?: LeadSource;
  assignedTo?: string;
  estimatedValue?: number;
  leadScore?: number;
  followUpDate?: string;
  notes?: string;
  source: LeadSource;
  status: LeadStatus;
  priority: Priority;
  expectedCloseDate?: string;
  tags: string[];
  customFields: Record<string, any>;
  isActive: boolean;
};
export type UpdateLeadRequest = Partial<CreateLeadRequest>;

// Lead qualification and conversion types
export interface LeadQualificationData {
  budgetConfirmed?: boolean;
  hasAuthority?: boolean;
  needIdentified?: boolean;
  timelineEstablished?: boolean;
}

export interface LeadConversionOpportunityData {
  name?: string;
  amount?: number;
  expectedCloseDate?: string;
  notes?: string;
}

export interface LeadConversionAccountData {
  name?: string;
  creditLimit?: number;
  notes?: string;
}

export interface LeadConversionData {
  createOpportunity?: boolean;
  createAccount?: boolean;
  existingAccountId?: number;
  opportunityData?: LeadConversionOpportunityData;
  accountData?: LeadConversionAccountData;
}

export interface UpdateLeadStatusRequest {
  fromStatus?: LeadStatus;
  toStatus: LeadStatus;
  notes?: string;
  qualification?: LeadQualificationData;
  conversion?: LeadConversionData;
}

export interface LeadStatusTransitionResult {
  lead: Lead;
  fromStatus: LeadStatus;
  toStatus: LeadStatus;
  accountId?: string;
  opportunityId?: string;
  contactId?: string;
  message?: string;
}
