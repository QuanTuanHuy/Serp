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
export type CustomerType = 'PROSPECT' | 'CUSTOMER';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE';

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

export type AccountType = CustomerType;
export type AccountStatus = CustomerStatus;
export type Account = Customer;

// Lead related types
export type LeadSource =
  | 'WEBSITE'
  | 'SOCIAL_MEDIA'
  | 'REFERRAL'
  | 'COLD_CALL'
  | 'EMAIL_CAMPAIGN'
  // Temporary compatibility values for legacy lead UI/mock code.
  | 'EMAIL'
  | 'PHONE'
  | 'TRADE_SHOW'
  | 'OTHER';
export type LeadStatus =
  | 'NEW'
  | 'CONTACTED'
  | 'NURTURING'
  | 'QUALIFIED'
  | 'DISQUALIFIED'
  | 'CONVERTED'
  // Temporary compatibility value for legacy lead UI/mock code.
  | 'LOST';

export interface LeadAddress {
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
}

export interface Lead extends BaseEntity {
  name?: string;
  email: string;
  phone?: string;
  company?: string;
  industry?: string;
  companySize?: string;
  website?: string;
  jobTitle?: string;
  address?: LeadAddress;
  leadSource?: LeadSource;
  leadStatus?: LeadStatus;
  assignedTo?: string;
  estimatedValue?: number;
  leadScore?: number;
  followUpDate?: string;
  notes?: string;

  convertedOpportunityId?: string;
  convertedAccountId?: string;
  tenantId?: string;
  createdBy?: string;
  updatedBy?: string;

  // Temporary compatibility fields for legacy CRM lead UI.
  firstName: string;
  lastName: string;
  source: LeadSource;
  status: LeadStatus;
  priority: Priority;
  expectedCloseDate?: string;
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

export type Contact = import('../components/contacts/ContactCard').Contact;

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

export type AccountFilters = CustomerFilters;

export interface LeadFilters {
  search?: string;
  status?: LeadStatus[];
  source?: LeadSource[];
  assignedTo?: string[];
  industries?: string[];
  createdDateFrom?: string;
  createdDateTo?: string;
  followUpDateFrom?: string;
  followUpDateTo?: string;
  minValue?: number;
  maxValue?: number;
  minScore?: number;
  maxScore?: number;
  country?: string;
  city?: string;
  qualifiedOnly?: boolean;
  convertedOnly?: boolean;
  unassignedOnly?: boolean;
  hasEmail?: boolean;
  hasPhone?: boolean;
}

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
> & {
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  companySize?: string;
  paymentTerms?: string;
  creditLimit?: number;
};
export type UpdateCustomerRequest = Partial<CreateCustomerRequest>;

export type CreateAccountRequest = CreateCustomerRequest;
export type UpdateAccountRequest = UpdateCustomerRequest;

export type CreateLeadRequest = {
  firstName: string;
  lastName: string;
  company?: string;
  industry?: string;
  companySize?: string;
  website?: string;
  name: string;
  email: string;
  phone?: string;
  jobTitle?: string;
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  leadSource?: LeadSource;
  assignedTo?: string;
  estimatedValue?: number;
  leadScore?: number;
  followUpDate?: string;
  notes?: string;
  source: LeadSource;
  status: LeadStatus;
  priority: Priority;
  expectedCloseDate?: string;
  tags: string[];
  customFields: Record<string, any>;
  isActive: boolean;
};
export type UpdateLeadRequest = Partial<CreateLeadRequest>;

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
