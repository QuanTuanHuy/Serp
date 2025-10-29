/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription types for settings
 */

export interface OrganizationSubscriptionDetails {
  id: string;
  organizationId: string;
  planId: string;
  planName: string;
  planDescription?: string;
  status: SubscriptionStatus;
  billingCycle: BillingCycle;
  price: number;
  currency: string;
  startDate: string;
  endDate: string;
  nextBillingDate?: string;
  autoRenew: boolean;
  usersLimit: number;
  currentUsersCount: number;
  modulesIncluded: string[];
  createdAt: string;
  updatedAt: string;
}

export type SubscriptionStatus =
  | 'ACTIVE'
  | 'TRIAL'
  | 'EXPIRED'
  | 'CANCELLED'
  | 'SUSPENDED'
  | 'PENDING';

export type BillingCycle = 'MONTHLY' | 'YEARLY' | 'LIFETIME';

export interface Invoice {
  id: string;
  invoiceNumber: string;
  subscriptionId: string;
  amount: number;
  currency: string;
  status: InvoiceStatus;
  dueDate: string;
  paidDate?: string;
  description: string;
  invoiceUrl?: string;
  createdAt: string;
}

export type InvoiceStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'PAID'
  | 'OVERDUE'
  | 'CANCELLED';

export interface UpgradeSubscriptionRequest {
  planId: string;
  billingCycle: BillingCycle;
}

export interface CancelSubscriptionRequest {
  reason?: string;
  feedback?: string;
}
