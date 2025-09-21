// Mock API Service (authors: QuanTuanHuy, Description: Part of Serp Project)

import type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  CreateCustomerRequest,
  CreateLeadRequest,
  CreateOpportunityRequest,
  CreateActivityRequest,
  UpdateCustomerRequest,
  UpdateLeadRequest,
  UpdateOpportunityRequest,
  UpdateActivityRequest,
  CustomerFilters,
  LeadFilters,
  OpportunityFilters,
  ActivityFilters,
  PaginationParams,
  PaginatedResponse,
  APIResponse,
  CRMMetrics,
  PipelineMetrics,
  SalesMetrics,
  LeadSourceMetrics,
  BulkOperationResult,
} from '../types';

import {
  customerService,
  leadService,
  opportunityService,
  activityService,
} from './localStorageService';

// Mock user data
const MOCK_USERS = [
  { id: '1', name: 'John Smith', email: 'john.smith@company.com' },
  { id: '2', name: 'Sarah Johnson', email: 'sarah.johnson@company.com' },
  { id: '3', name: 'Mike Wilson', email: 'mike.wilson@company.com' },
  { id: '4', name: 'Lisa Brown', email: 'lisa.brown@company.com' },
  { id: '5', name: 'David Lee', email: 'david.lee@company.com' },
];

// Helper function to get user name by ID
const getUserNameById = (userId: string): string => {
  const user = MOCK_USERS.find((u) => u.id === userId);
  return user?.name || 'Unknown User';
};

// Simulate network delay
const delay = (ms: number = 300) =>
  new Promise((resolve) => setTimeout(resolve, ms));

// Generic API response wrapper
const createApiResponse = <T>(data: T): APIResponse<T> => ({
  success: true,
  data,
  timestamp: new Date().toISOString(),
});

// Error simulation (5% chance)
const shouldSimulateError = () => Math.random() < 0.05;

const createErrorResponse = (message: string, code: string = 'API_ERROR') => ({
  success: false as const,
  error: {
    code,
    message,
  },
  timestamp: new Date().toISOString(),
});

// Customer API
export const customerApi = {
  async getCustomers(
    filters: CustomerFilters = {},
    pagination: PaginationParams
  ): Promise<APIResponse<PaginatedResponse<Customer>>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch customers', 'FETCH_ERROR');
    }

    const result = customerService.search(filters, pagination);
    return createApiResponse(result);
  },

  async getCustomer(id: string): Promise<APIResponse<Customer>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch customer', 'FETCH_ERROR');
    }

    const customer = customerService.getById(id);
    if (!customer) {
      throw createErrorResponse('Customer not found', 'NOT_FOUND');
    }

    return createApiResponse(customer);
  },

  async createCustomer(
    data: CreateCustomerRequest
  ): Promise<APIResponse<Customer>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to create customer', 'CREATE_ERROR');
    }

    // Validate required fields
    if (!data.name || !data.email) {
      throw createErrorResponse(
        'Name and email are required',
        'VALIDATION_ERROR'
      );
    }

    const customer = customerService.create({
      ...data,
      totalValue: 0,
      lastContactDate: undefined,
    });
    return createApiResponse(customer);
  },

  async updateCustomer(
    id: string,
    data: UpdateCustomerRequest
  ): Promise<APIResponse<Customer>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to update customer', 'UPDATE_ERROR');
    }

    const customer = customerService.update(id, data);
    if (!customer) {
      throw createErrorResponse('Customer not found', 'NOT_FOUND');
    }

    return createApiResponse(customer);
  },

  async deleteCustomer(id: string): Promise<APIResponse<{ deleted: boolean }>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to delete customer', 'DELETE_ERROR');
    }

    const deleted = customerService.delete(id);
    if (!deleted) {
      throw createErrorResponse('Customer not found', 'NOT_FOUND');
    }

    return createApiResponse({ deleted: true });
  },
};

// Lead API
export const leadApi = {
  async getLeads(
    filters: LeadFilters = {},
    pagination: PaginationParams
  ): Promise<APIResponse<PaginatedResponse<Lead>>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch leads', 'FETCH_ERROR');
    }

    const result = leadService.search(filters, pagination);
    return createApiResponse(result);
  },

  async getLead(id: string): Promise<APIResponse<Lead>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch lead', 'FETCH_ERROR');
    }

    const lead = leadService.getById(id);
    if (!lead) {
      throw createErrorResponse('Lead not found', 'NOT_FOUND');
    }

    return createApiResponse(lead);
  },

  async createLead(data: CreateLeadRequest): Promise<APIResponse<Lead>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to create lead', 'CREATE_ERROR');
    }

    if (!data.firstName || !data.lastName || !data.email) {
      throw createErrorResponse(
        'First name, last name, and email are required',
        'VALIDATION_ERROR'
      );
    }

    const lead = leadService.create(data);
    return createApiResponse(lead);
  },

  async updateLead(
    id: string,
    data: UpdateLeadRequest
  ): Promise<APIResponse<Lead>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to update lead', 'UPDATE_ERROR');
    }

    const lead = leadService.update(id, data);
    if (!lead) {
      throw createErrorResponse('Lead not found', 'NOT_FOUND');
    }

    return createApiResponse(lead);
  },

  async deleteLead(id: string): Promise<APIResponse<{ deleted: boolean }>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to delete lead', 'DELETE_ERROR');
    }

    const deleted = leadService.delete(id);
    if (!deleted) {
      throw createErrorResponse('Lead not found', 'NOT_FOUND');
    }

    return createApiResponse({ deleted: true });
  },

  async convertLead(
    id: string,
    opportunityData?: CreateOpportunityRequest
  ): Promise<APIResponse<{ customer: Customer; opportunity?: Opportunity }>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to convert lead', 'CONVERT_ERROR');
    }

    const lead = leadService.getById(id);
    if (!lead) {
      throw createErrorResponse('Lead not found', 'NOT_FOUND');
    }

    // Create customer from lead
    const customerData: CreateCustomerRequest = {
      name: `${lead.firstName} ${lead.lastName}`,
      email: lead.email,
      phone: lead.phone,
      customerType: lead.company ? 'COMPANY' : 'INDIVIDUAL',
      status: 'ACTIVE',
      companyName: lead.company,
      notes: lead.notes,
      assignedSalesRep: lead.assignedTo,
      tags: lead.tags,
      customFields: lead.customFields,
      isActive: true,
    };

    const customer = customerService.create({
      ...customerData,
      totalValue: 0,
      lastContactDate: undefined,
    });

    // Update lead status
    leadService.update(id, {
      status: 'CONVERTED',
      conversionDate: new Date().toISOString(),
      convertedToCustomerId: customer.id,
    });

    // Create opportunity if provided
    let opportunity: Opportunity | undefined;
    if (opportunityData) {
      opportunity = opportunityService.create({
        ...opportunityData,
        customerId: customer.id,
        customerName: customer.name,
        assignedToName: getUserNameById(opportunityData.assignedTo),
      });
    }

    return createApiResponse({ customer, opportunity });
  },
};

// Opportunity API
export const opportunityApi = {
  async getOpportunities(
    filters: OpportunityFilters = {},
    pagination: PaginationParams
  ): Promise<APIResponse<PaginatedResponse<Opportunity>>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch opportunities', 'FETCH_ERROR');
    }

    const result = opportunityService.search(filters, pagination);
    return createApiResponse(result);
  },

  async getOpportunity(id: string): Promise<APIResponse<Opportunity>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch opportunity', 'FETCH_ERROR');
    }

    const opportunity = opportunityService.getById(id);
    if (!opportunity) {
      throw createErrorResponse('Opportunity not found', 'NOT_FOUND');
    }

    return createApiResponse(opportunity);
  },

  async createOpportunity(
    data: CreateOpportunityRequest
  ): Promise<APIResponse<Opportunity>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to create opportunity', 'CREATE_ERROR');
    }

    if (!data.name || !data.customerId) {
      throw createErrorResponse(
        'Name and customer ID are required',
        'VALIDATION_ERROR'
      );
    }

    // Get customer name
    const customer = customerService.getById(data.customerId);
    if (!customer) {
      throw createErrorResponse('Customer not found', 'VALIDATION_ERROR');
    }

    const opportunity = opportunityService.create({
      ...data,
      customerName: customer.name,
      assignedToName: getUserNameById(data.assignedTo),
    });

    return createApiResponse(opportunity);
  },

  async updateOpportunity(
    id: string,
    data: UpdateOpportunityRequest
  ): Promise<APIResponse<Opportunity>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to update opportunity', 'UPDATE_ERROR');
    }

    const opportunity = opportunityService.update(id, data);
    if (!opportunity) {
      throw createErrorResponse('Opportunity not found', 'NOT_FOUND');
    }

    return createApiResponse(opportunity);
  },

  async deleteOpportunity(
    id: string
  ): Promise<APIResponse<{ deleted: boolean }>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to delete opportunity', 'DELETE_ERROR');
    }

    const deleted = opportunityService.delete(id);
    if (!deleted) {
      throw createErrorResponse('Opportunity not found', 'NOT_FOUND');
    }

    return createApiResponse({ deleted: true });
  },
};

// Activity API
export const activityApi = {
  async getActivities(
    filters: ActivityFilters = {},
    pagination: PaginationParams
  ): Promise<APIResponse<PaginatedResponse<Activity>>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch activities', 'FETCH_ERROR');
    }

    const result = activityService.search(filters, pagination);
    return createApiResponse(result);
  },

  async getActivity(id: string): Promise<APIResponse<Activity>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch activity', 'FETCH_ERROR');
    }

    const activity = activityService.getById(id);
    if (!activity) {
      throw createErrorResponse('Activity not found', 'NOT_FOUND');
    }

    return createApiResponse(activity);
  },

  async createActivity(
    data: CreateActivityRequest
  ): Promise<APIResponse<Activity>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to create activity', 'CREATE_ERROR');
    }

    if (!data.subject || !data.relatedTo) {
      throw createErrorResponse(
        'Subject and related entity are required',
        'VALIDATION_ERROR'
      );
    }

    const activity = activityService.create({
      ...data,
      assignedToName: getUserNameById(data.assignedTo),
    });
    return createApiResponse(activity);
  },

  async updateActivity(
    id: string,
    data: UpdateActivityRequest
  ): Promise<APIResponse<Activity>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to update activity', 'UPDATE_ERROR');
    }

    const activity = activityService.update(id, data);
    if (!activity) {
      throw createErrorResponse('Activity not found', 'NOT_FOUND');
    }

    return createApiResponse(activity);
  },

  async deleteActivity(id: string): Promise<APIResponse<{ deleted: boolean }>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to delete activity', 'DELETE_ERROR');
    }

    const deleted = activityService.delete(id);
    if (!deleted) {
      throw createErrorResponse('Activity not found', 'NOT_FOUND');
    }

    return createApiResponse({ deleted: true });
  },
};

// Analytics API
export const analyticsApi = {
  async getCRMMetrics(): Promise<APIResponse<CRMMetrics>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch CRM metrics', 'FETCH_ERROR');
    }

    const customers = customerService.getAll();
    const leads = leadService.getAll();
    const opportunities = opportunityService.getAll();
    const activities = activityService.getAll();

    const activeCustomers = customers.filter((c) => c.status === 'ACTIVE');
    const activeLeads = leads.filter(
      (l) => l.status !== 'CONVERTED' && l.status !== 'LOST'
    );
    const openOpportunities = opportunities.filter(
      (o) => !['CLOSED_WON', 'CLOSED_LOST'].includes(o.stage)
    );
    const closedWonOpportunities = opportunities.filter(
      (o) => o.stage === 'CLOSED_WON'
    );
    const thisWeekStart = new Date();
    thisWeekStart.setDate(thisWeekStart.getDate() - thisWeekStart.getDay());
    const activitiesThisWeek = activities.filter(
      (a) => new Date(a.createdAt) >= thisWeekStart
    );
    const overdueActivities = activities.filter(
      (a) =>
        a.status !== 'COMPLETED' &&
        a.scheduledDate &&
        new Date(a.scheduledDate) < new Date()
    );

    const metrics: CRMMetrics = {
      totalCustomers: activeCustomers.length,
      activeLeads: activeLeads.length,
      openOpportunities: openOpportunities.length,
      totalPipelineValue: openOpportunities.reduce(
        (sum, o) => sum + o.value,
        0
      ),
      monthlyRecurringRevenue:
        closedWonOpportunities.reduce((sum, o) => sum + o.value, 0) / 12,
      conversionRate:
        leads.length > 0
          ? (leads.filter((l) => l.status === 'CONVERTED').length /
              leads.length) *
            100
          : 0,
      averageDealSize:
        closedWonOpportunities.length > 0
          ? closedWonOpportunities.reduce((sum, o) => sum + o.value, 0) /
            closedWonOpportunities.length
          : 0,
      salesCycleLength: 30, // Mock average
      activitiesThisWeek: activitiesThisWeek.length,
      overdueActivities: overdueActivities.length,
    };

    return createApiResponse(metrics);
  },

  async getPipelineMetrics(): Promise<APIResponse<PipelineMetrics[]>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse(
        'Failed to fetch pipeline metrics',
        'FETCH_ERROR'
      );
    }

    const opportunities = opportunityService.getAll();
    const stages = [
      'PROSPECTING',
      'QUALIFICATION',
      'PROPOSAL',
      'NEGOTIATION',
      'CLOSED_WON',
      'CLOSED_LOST',
    ] as const;

    const metrics = stages.map((stage) => {
      const stageOpportunities = opportunities.filter((o) => o.stage === stage);
      const stageValue = stageOpportunities.reduce(
        (sum, o) => sum + o.value,
        0
      );

      return {
        stage,
        count: stageOpportunities.length,
        value: stageValue,
        averageValue:
          stageOpportunities.length > 0
            ? stageValue / stageOpportunities.length
            : 0,
        conversionRate: Math.random() * 30 + 10, // Mock conversion rate
      };
    });

    return createApiResponse(metrics);
  },

  async getSalesMetrics(
    period: string = 'monthly'
  ): Promise<APIResponse<SalesMetrics[]>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse('Failed to fetch sales metrics', 'FETCH_ERROR');
    }

    // Mock sales metrics for the last 6 periods
    const metrics: SalesMetrics[] = [];
    const now = new Date();

    for (let i = 5; i >= 0; i--) {
      const date = new Date(now);
      date.setMonth(date.getMonth() - i);

      metrics.push({
        period: date.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
        }),
        revenue: Math.random() * 100000 + 50000,
        deals: Math.floor(Math.random() * 20) + 5,
        averageDealSize: Math.random() * 10000 + 5000,
        conversionRate: Math.random() * 30 + 15,
      });
    }

    return createApiResponse(metrics);
  },

  async getLeadSourceMetrics(): Promise<APIResponse<LeadSourceMetrics[]>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse(
        'Failed to fetch lead source metrics',
        'FETCH_ERROR'
      );
    }

    const leads = leadService.getAll();
    const sources = [
      'WEBSITE',
      'REFERRAL',
      'EMAIL',
      'PHONE',
      'SOCIAL_MEDIA',
      'TRADE_SHOW',
      'OTHER',
    ] as const;

    const metrics = sources.map((source) => {
      const sourceLeads = leads.filter((l) => l.source === source);
      const convertedLeads = sourceLeads.filter(
        (l) => l.status === 'CONVERTED'
      );

      return {
        source,
        count: sourceLeads.length,
        conversionRate:
          sourceLeads.length > 0
            ? (convertedLeads.length / sourceLeads.length) * 100
            : 0,
        averageValue:
          convertedLeads.reduce((sum, l) => sum + (l.estimatedValue || 0), 0) /
          (convertedLeads.length || 1),
      };
    });

    return createApiResponse(metrics);
  },
};

// Bulk operations API
export const bulkApi = {
  async bulkDeleteCustomers(
    ids: string[]
  ): Promise<APIResponse<BulkOperationResult>> {
    await delay();

    if (shouldSimulateError()) {
      throw createErrorResponse(
        'Failed to bulk delete customers',
        'BULK_DELETE_ERROR'
      );
    }

    const result = customerService.bulkDelete(ids);

    return createApiResponse({
      successCount: result.success,
      failureCount: result.failed,
      errors: [],
    });
  },

  async bulkDeleteLeads(
    ids: string[]
  ): Promise<APIResponse<BulkOperationResult>> {
    await delay();

    const result = leadService.bulkDelete(ids);

    return createApiResponse({
      successCount: result.success,
      failureCount: result.failed,
      errors: [],
    });
  },

  async bulkDeleteOpportunities(
    ids: string[]
  ): Promise<APIResponse<BulkOperationResult>> {
    await delay();

    const result = opportunityService.bulkDelete(ids);

    return createApiResponse({
      successCount: result.success,
      failureCount: result.failed,
      errors: [],
    });
  },

  async bulkDeleteActivities(
    ids: string[]
  ): Promise<APIResponse<BulkOperationResult>> {
    await delay();

    const result = activityService.bulkDelete(ids);

    return createApiResponse({
      successCount: result.success,
      failureCount: result.failed,
      errors: [],
    });
  },
};

// Export all APIs
export const mockApi = {
  customer: customerApi,
  lead: leadApi,
  opportunity: opportunityApi,
  activity: activityApi,
  analytics: analyticsApi,
  bulk: bulkApi,
};
