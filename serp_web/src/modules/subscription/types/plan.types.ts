/*
Author: QuanTuanHuy
Description: Part of Serp Project - Subscription Plan Types
*/

import { PaginatedResponse } from '@/lib';

export type BillingCycle = 'monthly' | 'yearly';

export type PlanTier = 'free' | 'starter' | 'professional' | 'enterprise';

export type LicenseType =
  | 'FREE'
  | 'BASIC'
  | 'PROFESSIONAL'
  | 'ENTERPRISE'
  | 'TRIAL'
  | 'CUSTOM';

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
  modules?: PlanModule[];
  createdBy?: number;
  updatedBy?: number;
  createdAt: string;
  updatedAt: string;
}

export interface PlanModule {
  id: number;
  moduleId: number;
  moduleName?: string;
  moduleCode?: string;
  isIncluded: boolean;
  licenseType: LicenseType;
  maxUsersPerModule?: number;
}

export type SubscriptionPlansResponse = PaginatedResponse<SubscriptionPlan>;

export interface PlanFeature {
  name: string;
  included: boolean;
  tooltip?: string;
}

export interface PlanFeatureCategory {
  category: string;
  features: PlanFeature[];
}

export interface UISubscriptionPlan {
  id: string;
  tier: PlanTier;
  name: string;
  description: string;
  monthlyPrice: number;
  yearlyPrice: number;
  features: string[];
  featuresDetailed: PlanFeatureCategory[];
  maxUsers: number | 'unlimited';
  storage: string;
  popular?: boolean;
  currentPlan?: boolean;
  ctaText: string;
}
