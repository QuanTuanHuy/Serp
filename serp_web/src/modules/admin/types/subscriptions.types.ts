/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organization subscription types
 */

import type {
  ApiResponse,
  PaginatedResponse as BasePaginatedResponse,
  SearchParams,
} from '@/lib/store/api/types';
import type { BillingCycle } from './organization.types';

export type SubscriptionStatus =
  | 'ACTIVE'
  | 'TRIAL'
  | 'EXPIRED'
  | 'CANCELLED'
  | 'PAYMENT_FAILED'
  | 'GRACE_PERIOD'
  | 'PENDING';

export type SubscriptionBillingCycle = BillingCycle | 'TRIAL';

export interface OrganizationSubscription {
  id: number;
  organizationId: number;
  subscriptionPlanId: number;
  status: SubscriptionStatus;
  billingCycle: SubscriptionBillingCycle;
  startDate?: number;
  endDate?: number;
  trialEndsAt?: number;
  isAutoRenew: boolean;
  totalAmount?: number;
  notes?: string;
  activatedBy?: number;
  activatedAt?: number;
  cancelledBy?: number;
  cancelledAt?: number;
  cancellationReason?: string;
  createdBy?: number;
  updatedBy?: number;
  createdAt?: number;
  updatedAt?: number;
  organizationName?: string;
  planName?: string;
}

export interface SubscriptionFilters extends SearchParams {
  status?: SubscriptionStatus;
  planId?: number;
  organizationId?: number;
  billingCycle?: SubscriptionBillingCycle;
}

export type SubscriptionsResponse =
  BasePaginatedResponse<OrganizationSubscription>;
export type SubscriptionResponse = ApiResponse<OrganizationSubscription>;
