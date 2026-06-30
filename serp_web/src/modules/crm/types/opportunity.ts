// Opportunity Types (authors: QuanTuanHuy, Description: Part of Serp Project)

import type { BaseEntity } from './base';

export type OpportunityStage =
  | 'PROSPECTING'
  | 'QUALIFICATION'
  | 'PROPOSAL'
  | 'NEGOTIATION'
  | 'CLOSED_WON'
  | 'CLOSED_LOST';

export type OpportunityType = 'NEW_BUSINESS' | 'EXISTING_BUSINESS' | 'RENEWAL';

export interface OpportunityProduct {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  totalPrice: number;
}

export interface Opportunity extends BaseEntity {
  name: string;
  accountId?: string;
  leadId?: string;
  customerId?: string;
  customerName?: string;
  stage: OpportunityStage;
  type?: OpportunityType;
  estimatedValue?: number;
  value: number;
  actualValue?: number;
  probability?: number; // 0-100
  expectedCloseDate: string;
  actualCloseDate?: string;
  assignedTo?: string;
  assignedToName?: string;
  accountName?: string;
  leadName?: string;
  lastActivityAt?: string;
  nextActivityAt?: string;
  openActivityCount?: number;
  overdueActivityCount?: number;
  description?: string;
  tags: string[];
  products: OpportunityProduct[];
  notes?: string;
  competitors?: string[];
  nextAction?: string;
  nextActionDate?: string;
  lostReason?: string;
  reopenReason?: string;
  customFields: Record<string, any>;
}

// Filter types
export interface OpportunityFilters {
  search?: string;
  stage?: OpportunityStage[];
  leadId?: string[];
  assignedTo?: string[];
  customerId?: string[];
  unassignedOnly?: boolean;
  expectedCloseDateFrom?: string;
  expectedCloseDateTo?: string;
  actualCloseDateFrom?: string;
  actualCloseDateTo?: string;
  minValue?: number;
  maxValue?: number;
  probability?: {
    min: number;
    max: number;
  };
  createdDateFrom?: string;
  createdDateTo?: string;
  hasNotes?: boolean;
}

// Request types
export interface CreateOpportunityRequest {
  name: string;
  description?: string;
  accountId: string;
  leadId?: string;
  stage?: OpportunityStage;
  estimatedValue?: number;
  expectedCloseDate?: string;
  assignedTo?: string;
  notes?: string;
  type?: OpportunityType;
  value?: number;
  customerId?: string;
  customerName?: string;
  assignedToName?: string;
  tags?: string[];
  products?: OpportunityProduct[];
  competitors?: string[];
  nextAction?: string;
  nextActionDate?: string;
  customFields?: Record<string, any>;
}

export interface UpdateOpportunityRequest {
  name?: string;
  description?: string;
  stage?: OpportunityStage;
  estimatedValue?: number;
  expectedCloseDate?: string;
  assignedTo?: string;
  notes?: string;
  type?: OpportunityType;
  value?: number;
  customerId?: string;
  customerName?: string;
  assignedToName?: string;
  tags?: string[];
  products?: OpportunityProduct[];
  competitors?: string[];
  nextAction?: string;
  nextActionDate?: string;
  customFields?: Record<string, any>;
}

export interface ChangeOpportunityStageRequest {
  stage: OpportunityStage;
  actualValue?: number;
  lossReason?: string;
  notes?: string;
  reopenReason?: string;
}
