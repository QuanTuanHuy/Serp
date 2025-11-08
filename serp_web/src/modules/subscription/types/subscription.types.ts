/*
Author: QuanTuanHuy
Description: Part of Serp Project - Subscription Request/Response Types
*/

import { BillingCycle } from './plan.types';

export interface SubscribeRequest {
  planId: number;
  billingCycle: BillingCycle;
  isAutoRenew?: boolean;
  notes?: string;
}

export interface SubscribeCustomPlanRequest {
  billingCycle: BillingCycle;
  isAutoRenew?: boolean;
  notes?: string;
  moduleIds: number[];
}
