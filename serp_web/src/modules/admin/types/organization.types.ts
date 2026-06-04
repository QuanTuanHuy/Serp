/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organization types
 */

import type {
  SearchParams,
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
} from '@/lib/store/api/types';

export type OrganizationStatus =
  | 'ACTIVE'
  | 'TRIAL'
  | 'SUSPENDED'
  | 'EXPIRED'
  | 'CLOSED';

export type OrganizationType =
  | 'ENTERPRISE'
  | 'SMB'
  | 'STARTUP'
  | 'PERSONAL'
  | 'NON_PROFIT'
  | 'GOVERNMENT';

export type BillingCycle = 'MONTHLY' | 'YEARLY' | 'QUARTERLY';

export type AdminTimestamp = string | number;

// Organization
export interface Organization {
  id: number;
  name: string;
  code: string;
  description?: string;
  address?: string;
  ownerId: number;
  organizationType: OrganizationType;
  industry?: string;
  employeeCount?: number;
  subscriptionId?: number;
  subscriptionExpiresAt?: AdminTimestamp;
  currentBillingCycle?: BillingCycle;
  nextBillingDate?: AdminTimestamp;
  status: OrganizationStatus;
  timezone?: string;
  currency?: string;
  language?: string;
  logoUrl?: string;
  primaryColor?: string;
  website?: string;
  phoneNumber?: string;
  email?: string;
  createdAt: AdminTimestamp;
  updatedAt: AdminTimestamp;
  createdBy?: number;
  updatedBy?: number;
}

export interface OrganizationStatusUpdateResponse {
  organization: Organization;
  affectedUsers: number;
  activatedUsers: number;
  suspendedUsers: number;
}

// Filters
export interface OrganizationFilters extends SearchParams {
  status?: OrganizationStatus;
  type?: OrganizationType;
}

// Response types
export type OrganizationsResponse = BasePaginatedResponse<Organization>;
export type OrganizationResponse = ApiResponse<Organization>;
