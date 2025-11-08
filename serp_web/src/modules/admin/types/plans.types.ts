/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription plan types
 */

import type { ApiResponse, PaginatedResponse } from '@/lib/store/api/types';

export type LicenseType =
  | 'FREE'
  | 'BASIC'
  | 'PROFESSIONAL'
  | 'ENTERPRISE'
  | 'TRIAL'
  | 'CUSTOM';

export interface PlanModule {
  id: number;
  moduleId: number;
  moduleName?: string;
  moduleCode?: string;
  isIncluded: boolean;
  licenseType: LicenseType;
  maxUsersPerModule?: number;
}

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

export interface AddModuleToPlanRequest {
  moduleId: number;
  licenseType: LicenseType;
  isIncluded?: boolean;
  maxUsersPerModule?: number;
}

export type PlansResponse = PaginatedResponse<SubscriptionPlan>;
export type PlanResponse = ApiResponse<SubscriptionPlan>;
export type PlanModulesResponse = ApiResponse<PlanModule[]>;
