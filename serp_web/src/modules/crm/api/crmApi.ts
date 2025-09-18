// CRM API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
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
  APIResponse,
  PaginatedResponse,
  CRMMetrics,
  PipelineMetrics,
  SalesMetrics,
  LeadSourceMetrics,
  BulkOperationResult,
} from '../types';

// Define CRM API slice extending the main API
export const crmApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Customer endpoints
    getCustomers: builder.query<
      APIResponse<PaginatedResponse<Customer>>,
      { filters?: CustomerFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/crm/customers',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Customer' as const,
                id,
              })),
              { type: 'Customer', id: 'LIST' },
            ]
          : [{ type: 'Customer', id: 'LIST' }],
    }),

    getCustomer: builder.query<APIResponse<Customer>, string>({
      query: (id) => ({
        url: `/crm/customers/${id}`,
        method: 'GET',
      }),
      providesTags: (result, error, id) => [{ type: 'Customer', id }],
    }),

    createCustomer: builder.mutation<
      APIResponse<Customer>,
      CreateCustomerRequest
    >({
      query: (data) => ({
        url: '/crm/customers',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),

    updateCustomer: builder.mutation<
      APIResponse<Customer>,
      { id: string; data: UpdateCustomerRequest }
    >({
      query: ({ id, data }) => ({
        url: `/crm/customers/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
      ],
    }),

    deleteCustomer: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (id) => ({
          url: `/crm/customers/${id}`,
          method: 'DELETE',
        }),
        invalidatesTags: (result, error, id) => [
          { type: 'Customer', id },
          { type: 'Customer', id: 'LIST' },
        ],
      }
    ),

    // Lead endpoints
    getLeads: builder.query<
      APIResponse<PaginatedResponse<Lead>>,
      { filters?: LeadFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/crm/leads',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Lead' as const,
                id,
              })),
              { type: 'Lead', id: 'LIST' },
            ]
          : [{ type: 'Lead', id: 'LIST' }],
    }),

    getLead: builder.query<APIResponse<Lead>, string>({
      query: (id) => `/crm/leads/${id}`,
      providesTags: (result, error, id) => [{ type: 'Lead', id }],
    }),

    createLead: builder.mutation<APIResponse<Lead>, CreateLeadRequest>({
      query: (data) => ({
        url: '/crm/leads',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'Lead', id: 'LIST' }],
    }),

    updateLead: builder.mutation<
      APIResponse<Lead>,
      { id: string; data: UpdateLeadRequest }
    >({
      query: ({ id, data }) => ({
        url: `/crm/leads/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
      ],
    }),

    deleteLead: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (id) => ({
        url: `/crm/leads/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, id) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
      ],
    }),

    convertLead: builder.mutation<
      APIResponse<{ customer: Customer; opportunity?: Opportunity }>,
      { id: string; opportunityData?: CreateOpportunityRequest }
    >({
      query: ({ id, opportunityData }) => ({
        url: `/crm/leads/${id}/convert`,
        method: 'POST',
        body: { opportunityData },
      }),
      invalidatesTags: [
        { type: 'Lead', id: 'LIST' },
        { type: 'Customer', id: 'LIST' },
        { type: 'Opportunity', id: 'LIST' },
      ],
    }),

    // Opportunity endpoints
    getOpportunities: builder.query<
      APIResponse<PaginatedResponse<Opportunity>>,
      { filters?: OpportunityFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/crm/opportunities',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Opportunity' as const,
                id,
              })),
              { type: 'Opportunity', id: 'LIST' },
            ]
          : [{ type: 'Opportunity', id: 'LIST' }],
    }),

    getOpportunity: builder.query<APIResponse<Opportunity>, string>({
      query: (id) => `/crm/opportunities/${id}`,
      providesTags: (result, error, id) => [{ type: 'Opportunity', id }],
    }),

    createOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      CreateOpportunityRequest
    >({
      query: (data) => ({
        url: '/crm/opportunities',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'Opportunity', id: 'LIST' }],
    }),

    updateOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      { id: string; data: UpdateOpportunityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/crm/opportunities/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
      ],
    }),

    deleteOpportunity: builder.mutation<
      APIResponse<{ deleted: boolean }>,
      string
    >({
      query: (id) => ({
        url: `/crm/opportunities/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, id) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
      ],
    }),

    // Activity endpoints
    getActivities: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { filters?: ActivityFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/crm/activities',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      providesTags: (result) =>
        result?.data?.data
          ? [
              ...result.data.data.map(({ id }) => ({
                type: 'Activity' as const,
                id,
              })),
              { type: 'Activity', id: 'LIST' },
            ]
          : [{ type: 'Activity', id: 'LIST' }],
    }),

    getActivity: builder.query<APIResponse<Activity>, string>({
      query: (id) => `/crm/activities/${id}`,
      providesTags: (result, error, id) => [{ type: 'Activity', id }],
    }),

    createActivity: builder.mutation<
      APIResponse<Activity>,
      CreateActivityRequest
    >({
      query: (data) => ({
        url: '/crm/activities',
        method: 'POST',
        body: data,
      }),
      invalidatesTags: [{ type: 'Activity', id: 'LIST' }],
    }),

    updateActivity: builder.mutation<
      APIResponse<Activity>,
      { id: string; data: UpdateActivityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/crm/activities/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [
        { type: 'Activity', id },
        { type: 'Activity', id: 'LIST' },
      ],
    }),

    deleteActivity: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (id) => ({
          url: `/crm/activities/${id}`,
          method: 'DELETE',
        }),
        invalidatesTags: (result, error, id) => [
          { type: 'Activity', id },
          { type: 'Activity', id: 'LIST' },
        ],
      }
    ),

    // Analytics endpoints
    getCRMMetrics: builder.query<APIResponse<CRMMetrics>, void>({
      query: () => '/crm/analytics/metrics',
      providesTags: [{ type: 'Analytics', id: 'METRICS' }],
    }),

    getPipelineMetrics: builder.query<APIResponse<PipelineMetrics[]>, void>({
      query: () => '/crm/analytics/pipeline',
      providesTags: [{ type: 'Analytics', id: 'PIPELINE' }],
    }),

    getSalesMetrics: builder.query<
      APIResponse<SalesMetrics[]>,
      { period?: string }
    >({
      query: ({ period = 'monthly' }) => ({
        url: '/crm/analytics/sales',
        params: { period },
      }),
      providesTags: [{ type: 'Analytics', id: 'SALES' }],
    }),

    getLeadSourceMetrics: builder.query<APIResponse<LeadSourceMetrics[]>, void>(
      {
        query: () => '/crm/analytics/lead-sources',
        providesTags: [{ type: 'Analytics', id: 'LEAD_SOURCES' }],
      }
    ),

    // Bulk operations
    bulkDeleteCustomers: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/crm/customers/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),

    bulkDeleteLeads: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/crm/leads/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      invalidatesTags: [{ type: 'Lead', id: 'LIST' }],
    }),

    bulkDeleteOpportunities: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/crm/opportunities/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      invalidatesTags: [{ type: 'Opportunity', id: 'LIST' }],
    }),

    bulkDeleteActivities: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/crm/activities/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      invalidatesTags: [{ type: 'Activity', id: 'LIST' }],
    }),
  }),
});

// Export hooks for use in components
export const {
  // Customer hooks
  useGetCustomersQuery,
  useGetCustomerQuery,
  useCreateCustomerMutation,
  useUpdateCustomerMutation,
  useDeleteCustomerMutation,

  // Lead hooks
  useGetLeadsQuery,
  useGetLeadQuery,
  useCreateLeadMutation,
  useUpdateLeadMutation,
  useDeleteLeadMutation,
  useConvertLeadMutation,

  // Opportunity hooks
  useGetOpportunitiesQuery,
  useGetOpportunityQuery,
  useCreateOpportunityMutation,
  useUpdateOpportunityMutation,
  useDeleteOpportunityMutation,

  // Activity hooks
  useGetActivitiesQuery,
  useGetActivityQuery,
  useCreateActivityMutation,
  useUpdateActivityMutation,
  useDeleteActivityMutation,

  // Analytics hooks
  useGetCRMMetricsQuery,
  useGetPipelineMetricsQuery,
  useGetSalesMetricsQuery,
  useGetLeadSourceMetricsQuery,

  // Bulk operation hooks
  useBulkDeleteCustomersMutation,
  useBulkDeleteLeadsMutation,
  useBulkDeleteOpportunitiesMutation,
  useBulkDeleteActivitiesMutation,
} = crmApi;
