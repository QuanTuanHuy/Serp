/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription types
 */

import type {
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
  SearchParams,
} from '@/lib/store/api/types';

import type { BillingCycle } from './organization.types';

export type SubscriptionStatus =
  | 'PENDING_APPROVAL'
  | 'ACTIVE'
  | 'TRIAL'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'SUSPENDED';

// Subscription Plan
export interface SubscriptionPlan {
  id: number;
  planName: string;
  planCode: string;
  description?: string;
  monthlyPrice: number;
  yearlyPrice: number;
  maxUsers?: number;
  trialDays?: number;
  isActive: boolean;
  isCustom: boolean;
  organizationId?: number;
  displayOrder?: number;
  createdBy?: number;
  updatedBy?: number;
  createdAt: string;
  updatedAt: string;
}

// Organization Subscription
export interface OrganizationSubscription {
  id: number;
  organizationId: number;
  planId: number;
  status: SubscriptionStatus;
  startDate: string;
  endDate?: string;
  trialStartDate?: string;
  trialEndDate?: string;
  billingCycle: BillingCycle;
  autoRenew: boolean;
  cancellationReason?: string;
  rejectionReason?: string;
  requestedBy?: number;
  approvedBy?: number;
  rejectedBy?: number;
  cancelledBy?: number;
  createdAt: string;
  updatedAt: string;
}
export interface SubscriptionFilters extends SearchParams {
  status?: SubscriptionStatus;
  planId?: number;
  organizationId?: number;
}

// Response types
export type SubscriptionsResponse =
  BasePaginatedResponse<OrganizationSubscription>;
export type SubscriptionResponse = ApiResponse<OrganizationSubscription>;
export type PlansResponse = ApiResponse<SubscriptionPlan[]>;
export type PlanResponse = ApiResponse<SubscriptionPlan>;
