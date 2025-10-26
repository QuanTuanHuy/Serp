/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Subscription plan types
 */

import type { ApiResponse } from '@/lib/store/api/types';

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

export type PlansResponse = ApiResponse<SubscriptionPlan[]>;
export type PlanResponse = ApiResponse<SubscriptionPlan>;
