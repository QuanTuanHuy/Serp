/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin module types
 */

// Base types from backend
export type OrganizationStatus =
  | 'PENDING'
  | 'ACTIVE'
  | 'SUSPENDED'
  | 'INACTIVE'
  | 'DELETED';

export type OrganizationType =
  | 'ENTERPRISE'
  | 'SME'
  | 'STARTUP'
  | 'NONPROFIT'
  | 'PERSONAL';

export type SubscriptionStatus =
  | 'PENDING_APPROVAL'
  | 'ACTIVE'
  | 'TRIAL'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'SUSPENDED';

export type BillingCycle = 'MONTHLY' | 'YEARLY' | 'QUARTERLY';

export type ModuleStatus = 'ACTIVE' | 'BETA' | 'DEPRECATED' | 'INACTIVE';

export type ModuleType = 'CORE' | 'ADDON' | 'INTEGRATION';

export type PricingModel = 'FREE' | 'FIXED' | 'PER_USER' | 'TIERED';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';

export type UserType = 'INTERNAL' | 'EXTERNAL' | 'SYSTEM';

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
  subscriptionExpiresAt?: number;
  currentBillingCycle?: BillingCycle;
  nextBillingDate?: number;
  status: OrganizationStatus;
  timezone?: string;
  currency?: string;
  language?: string;
  logoUrl?: string;
  primaryColor?: string;
  website?: string;
  phoneNumber?: string;
  email?: string;
  createdAt: number;
  updatedAt: number;
  createdBy?: number;
  updatedBy?: number;
}

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
  createdAt: number;
  updatedAt: number;
}

// Organization Subscription
export interface OrganizationSubscription {
  id: number;
  organizationId: number;
  planId: number;
  status: SubscriptionStatus;
  startDate: number;
  endDate?: number;
  trialStartDate?: number;
  trialEndDate?: number;
  billingCycle: BillingCycle;
  autoRenew: boolean;
  cancellationReason?: string;
  rejectionReason?: string;
  requestedBy?: number;
  approvedBy?: number;
  rejectedBy?: number;
  cancelledBy?: number;
  createdAt: number;
  updatedAt: number;
}

// Module
export interface Module {
  id: number;
  moduleName: string;
  code: string;
  description?: string;
  keycloakClientId?: string;
  category?: string;
  icon?: string;
  displayOrder?: number;
  moduleType: ModuleType;
  isGlobal: boolean;
  organizationId?: number;
  isFree: boolean;
  pricingModel: PricingModel;
  dependsOnModuleIds?: number[];
  status: ModuleStatus;
  version?: string;
  createdAt: number;
  updatedAt: number;
}

// User
export interface AdminUser {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  keycloakId: string;
  isSuperAdmin: boolean;
  primaryOrganizationId?: number;
  primaryDepartmentId?: number;
  userType: UserType;
  status: UserStatus;
  lastLoginAt?: number;
  avatarUrl?: string;
  timezone?: string;
  preferredLanguage?: string;
  createdAt: number;
  updatedAt: number;
}

// Stats & Analytics
export interface AdminStats {
  totalOrganizations: number;
  activeOrganizations: number;
  suspendedOrganizations: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  trialSubscriptions: number;
  pendingSubscriptions: number;
  expiredSubscriptions: number;
  totalRevenue: number;
  mrr: number; // Monthly Recurring Revenue
  arr: number; // Annual Recurring Revenue
  totalUsers: number;
  activeUsers: number;
}

// Filters
export interface OrganizationFilters {
  status?: OrganizationStatus[];
  organizationType?: OrganizationType[];
  planId?: number;
  industry?: string;
  createdFrom?: number;
  createdTo?: number;
  search?: string;
}

export interface SubscriptionFilters {
  status?: SubscriptionStatus[];
  planId?: number;
  organizationId?: number;
  billingCycle?: BillingCycle;
  startDateFrom?: number;
  startDateTo?: number;
  search?: string;
}

export interface ModuleFilters {
  status?: ModuleStatus[];
  category?: string;
  moduleType?: ModuleType;
  isGlobal?: boolean;
  isFree?: boolean;
  search?: string;
}

export interface UserFilters {
  status?: UserStatus[];
  userType?: UserType[];
  organizationId?: number;
  isSuperAdmin?: boolean;
  search?: string;
}
