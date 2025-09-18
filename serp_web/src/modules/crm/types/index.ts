// CRM Types & Interfaces (authors: QuanTuanHuy, Description: Part of Serp Project)

// Base types
export type ActiveStatus = 'ACTIVE' | 'INACTIVE';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}

// Customer related types
export type CustomerType = 'INDIVIDUAL' | 'COMPANY';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'POTENTIAL' | 'BLOCKED';

export interface Customer extends BaseEntity {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  customerType: CustomerType;
  status: CustomerStatus;
  companyName?: string;
  taxNumber?: string;
  website?: string;
  notes?: string;
  assignedSalesRep?: string;
  tags: string[];
  customFields: Record<string, any>;
  totalValue: number;
  lastContactDate?: string;
}

// Lead related types
export type LeadSource =
  | 'WEBSITE'
  | 'REFERRAL'
  | 'EMAIL'
  | 'PHONE'
  | 'SOCIAL_MEDIA'
  | 'TRADE_SHOW'
  | 'OTHER';
export type LeadStatus =
  | 'NEW'
  | 'CONTACTED'
  | 'QUALIFIED'
  | 'CONVERTED'
  | 'LOST';

export interface Lead extends BaseEntity {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  company?: string;
  jobTitle?: string;
  source: LeadSource;
  status: LeadStatus;
  priority: Priority;
  assignedTo?: string;
  estimatedValue?: number;
  expectedCloseDate?: string;
  notes?: string;
  tags: string[];
  customFields: Record<string, any>;
  conversionDate?: string;
  convertedToCustomerId?: string;
  lastActivityDate?: string;
}

// Opportunity related types
export type OpportunityStage =
  | 'PROSPECTING'
  | 'QUALIFICATION'
  | 'PROPOSAL'
  | 'NEGOTIATION'
  | 'CLOSED_WON'
  | 'CLOSED_LOST';
export type OpportunityType = 'NEW_BUSINESS' | 'EXISTING_BUSINESS' | 'RENEWAL';

export interface Opportunity extends BaseEntity {
  name: string;
  customerId: string;
  customerName: string;
  stage: OpportunityStage;
  type: OpportunityType;
  value: number;
  probability: number; // 0-100
  expectedCloseDate: string;
  actualCloseDate?: string;
  assignedTo: string;
  assignedToName: string;
  description?: string;
  tags: string[];
  products: OpportunityProduct[];
  notes?: string;
  competitors?: string[];
  nextAction?: string;
  nextActionDate?: string;
  lostReason?: string;
  customFields: Record<string, any>;
}

export interface OpportunityProduct {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  totalPrice: number;
}

// Activity related types
export type ActivityType =
  | 'CALL'
  | 'EMAIL'
  | 'MEETING'
  | 'TASK'
  | 'NOTE'
  | 'DEMO'
  | 'PROPOSAL'
  | 'FOLLOW_UP';
export type ActivityStatus =
  | 'PLANNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'OVERDUE';

export interface Activity extends BaseEntity {
  type: ActivityType;
  status: ActivityStatus;
  subject: string;
  description?: string;
  scheduledDate?: string;
  actualDate?: string;
  duration?: number; // in minutes
  priority: Priority;
  assignedTo: string;
  assignedToName: string;
  relatedTo: {
    type: 'CUSTOMER' | 'LEAD' | 'OPPORTUNITY';
    id: string;
    name: string;
  };
  participants?: string[];
  location?: string;
  outcome?: string;
  followUpRequired?: boolean;
  followUpDate?: string;
  tags: string[];
  customFields: Record<string, any>;
}

// Analytics & Dashboard types
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

// Filter & Search types
export interface CustomerFilters {
  search?: string;
  status?: CustomerStatus[];
  type?: CustomerType[];
  assignedSalesRep?: string[];
  tags?: string[];
  createdDateFrom?: string;
  createdDateTo?: string;
  lastContactFrom?: string;
  lastContactTo?: string;
  minValue?: number;
  maxValue?: number;
}

export interface LeadFilters {
  search?: string;
  status?: LeadStatus[];
  source?: LeadSource[];
  priority?: Priority[];
  assignedTo?: string[];
  tags?: string[];
  createdDateFrom?: string;
  createdDateTo?: string;
  expectedCloseDateFrom?: string;
  expectedCloseDateTo?: string;
  minValue?: number;
  maxValue?: number;
}

export interface OpportunityFilters {
  search?: string;
  stage?: OpportunityStage[];
  type?: OpportunityType[];
  assignedTo?: string[];
  customerId?: string[];
  tags?: string[];
  expectedCloseDateFrom?: string;
  expectedCloseDateTo?: string;
  minValue?: number;
  maxValue?: number;
  probability?: {
    min: number;
    max: number;
  };
}

export interface ActivityFilters {
  search?: string;
  type?: ActivityType[];
  status?: ActivityStatus[];
  priority?: Priority[];
  assignedTo?: string[];
  relatedType?: ('CUSTOMER' | 'LEAD' | 'OPPORTUNITY')[];
  relatedId?: string;
  scheduledDateFrom?: string;
  scheduledDateTo?: string;
  tags?: string[];
}

// Pagination & Sorting
export interface PaginationParams {
  page: number;
  limit: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

// Form types for create/update operations
export type CreateCustomerRequest = Omit<
  Customer,
  'id' | 'createdAt' | 'updatedAt' | 'totalValue' | 'lastContactDate'
>;
export type UpdateCustomerRequest = Partial<CreateCustomerRequest>;

export type CreateLeadRequest = Omit<
  Lead,
  | 'id'
  | 'createdAt'
  | 'updatedAt'
  | 'conversionDate'
  | 'convertedToCustomerId'
  | 'lastActivityDate'
>;
export type UpdateLeadRequest = Partial<CreateLeadRequest>;

export type CreateOpportunityRequest = Omit<
  Opportunity,
  'id' | 'createdAt' | 'updatedAt' | 'customerName' | 'assignedToName'
>;
export type UpdateOpportunityRequest = Partial<CreateOpportunityRequest>;

export type CreateActivityRequest = Omit<
  Activity,
  'id' | 'createdAt' | 'updatedAt' | 'assignedToName'
>;
export type UpdateActivityRequest = Partial<CreateActivityRequest>;

// UI State types
export interface CRMUIState {
  activeModule:
    | 'customers'
    | 'leads'
    | 'opportunities'
    | 'activities'
    | 'analytics';
  selectedItems: string[];
  bulkActionMode: boolean;
  viewMode: 'list' | 'grid' | 'kanban' | 'calendar';
  sidebarCollapsed: boolean;
  filterPanelOpen: boolean;
}

// Export all types for barrel import
export * from './api';
export * from './constants';
