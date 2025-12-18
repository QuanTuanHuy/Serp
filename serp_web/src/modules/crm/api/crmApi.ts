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
        url: '/customers',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'crm' },
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
        url: `/customers/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, id) => [{ type: 'Customer', id }],
    }),

    createCustomer: builder.mutation<
      APIResponse<Customer>,
      CreateCustomerRequest
    >({
      query: (data) => ({
        url: '/customers',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),

    updateCustomer: builder.mutation<
      APIResponse<Customer>,
      { id: string; data: UpdateCustomerRequest }
    >({
      query: ({ id, data }) => ({
        url: `/customers/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { id }) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
      ],
    }),

    deleteCustomer: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (id) => ({
          url: `/customers/${id}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'crm' },
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
        url: '/leads',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'crm' },
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
      query: (id) => ({
        url: `/leads/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, id) => [{ type: 'Lead', id }],
    }),

    createLead: builder.mutation<APIResponse<Lead>, CreateLeadRequest>({
      query: (data) => ({
        url: '/leads',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Lead', id: 'LIST' }],
    }),

    updateLead: builder.mutation<
      APIResponse<Lead>,
      { id: string; data: UpdateLeadRequest }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { id }) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
      ],
    }),

    deleteLead: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (id) => ({
        url: `/leads/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'crm' },
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
        url: `/leads/${id}/convert`,
        method: 'POST',
        body: { opportunityData },
      }),
      extraOptions: { service: 'crm' },
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
        url: '/opportunities',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'crm' },
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
      query: (id) => ({
        url: `/opportunities/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, id) => [{ type: 'Opportunity', id }],
    }),

    createOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      CreateOpportunityRequest
    >({
      query: (data) => ({
        url: '/opportunities',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Opportunity', id: 'LIST' }],
    }),

    updateOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      { id: string; data: UpdateOpportunityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
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
        url: `/opportunities/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'crm' },
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
        url: '/activities',
        method: 'GET',
        params: { ...filters, ...pagination },
      }),
      extraOptions: { service: 'crm' },
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
      query: (id) => ({
        url: `/activities/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: (result, error, id) => [{ type: 'Activity', id }],
    }),

    createActivity: builder.mutation<
      APIResponse<Activity>,
      CreateActivityRequest
    >({
      query: (data) => ({
        url: '/activities',
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Activity', id: 'LIST' }],
    }),

    updateActivity: builder.mutation<
      APIResponse<Activity>,
      { id: string; data: UpdateActivityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/activities/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { id }) => [
        { type: 'Activity', id },
        { type: 'Activity', id: 'LIST' },
      ],
    }),

    deleteActivity: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (id) => ({
          url: `/activities/${id}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'crm' },
        invalidatesTags: (result, error, id) => [
          { type: 'Activity', id },
          { type: 'Activity', id: 'LIST' },
        ],
      }
    ),

    // Analytics endpoints
    getCRMMetrics: builder.query<APIResponse<CRMMetrics>, void>({
      query: () => ({
        url: '/analytics/metrics',
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: [{ type: 'Analytics', id: 'METRICS' }],
    }),

    getPipelineMetrics: builder.query<APIResponse<PipelineMetrics[]>, void>({
      query: () => ({
        url: '/analytics/pipeline',
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      providesTags: [{ type: 'Analytics', id: 'PIPELINE' }],
    }),

    getSalesMetrics: builder.query<
      APIResponse<SalesMetrics[]>,
      { period?: string }
    >({
      query: ({ period = 'monthly' }) => ({
        url: '/analytics/sales',
        params: { period },
      }),
      extraOptions: { service: 'crm' },
      providesTags: [{ type: 'Analytics', id: 'SALES' }],
    }),

    getLeadSourceMetrics: builder.query<APIResponse<LeadSourceMetrics[]>, void>(
      {
        query: () => ({
          url: '/analytics/lead-sources',
          method: 'GET',
        }),
        extraOptions: { service: 'crm' },
        providesTags: [{ type: 'Analytics', id: 'LEAD_SOURCES' }],
      }
    ),

    // Bulk operations
    bulkDeleteCustomers: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/customers/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),

    bulkDeleteLeads: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/leads/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Lead', id: 'LIST' }],
    }),

    bulkDeleteOpportunities: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/opportunities/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Opportunity', id: 'LIST' }],
    }),

    bulkDeleteActivities: builder.mutation<
      APIResponse<BulkOperationResult>,
      string[]
    >({
      query: (ids) => ({
        url: '/activities/bulk-delete',
        method: 'POST',
        body: { ids },
      }),
      extraOptions: { service: 'crm' },
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
