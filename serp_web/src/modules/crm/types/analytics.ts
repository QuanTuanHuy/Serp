// Analytics & Metrics Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { OpportunityStage } from './opportunity';
import type { LeadSource } from './lead';

export interface CRMMetrics {
  totalCustomers: number;
  activeLeads: number;
  openOpportunities: number;
  totalPipelineValue: number;
  monthlyRecurringRevenue: number;
  conversionRate: number;
  averageDealSize: number;
  salesCycleLength: number;
  activitiesThisWeek: number;
  overdueActivities: number;
}

export interface PipelineMetrics {
  stage: OpportunityStage;
  count: number;
  value: number;
  averageValue: number;
  conversionRate: number;
}

export interface SalesMetrics {
  period: string;
  revenue: number;
  deals: number;
  averageDealSize: number;
  conversionRate: number;
}

export interface LeadSourceMetrics {
  source: LeadSource;
  count: number;
  conversionRate: number;
  averageValue: number;
}
