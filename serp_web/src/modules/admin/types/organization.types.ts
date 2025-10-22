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
  | 'PENDING'
  | 'ACTIVE'
  | 'SUSPENDED'
  | 'INACTIVE'
  | 'DELETED';

export type OrganizationType =
  | 'ENTERPRISE'
  | 'SMB'
  | 'STARTUP'
  | 'NONPROFIT'
  | 'PERSONAL';

export type BillingCycle = 'MONTHLY' | 'YEARLY' | 'QUARTERLY';

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
  subscriptionExpiresAt?: string;
  currentBillingCycle?: BillingCycle;
  nextBillingDate?: string;
  status: OrganizationStatus;
  timezone?: string;
  currency?: string;
  language?: string;
  logoUrl?: string;
  primaryColor?: string;
  website?: string;
  phoneNumber?: string;
  email?: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: number;
  updatedBy?: number;
}

// Filters
export interface OrganizationFilters extends SearchParams {
  status?: OrganizationStatus;
  type?: OrganizationType;
}

// Response types
export type OrganizationsResponse = BasePaginatedResponse<Organization>;
export type OrganizationResponse = ApiResponse<Organization>;
