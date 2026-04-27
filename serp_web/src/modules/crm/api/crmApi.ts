// CRM API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import {
  mapAccountFormToBackendPayload,
  mapAccountListResponse,
  mapActivityListResponse,
  mapContactListResponse,
  mapCustomerFiltersToAccountSearch,
  mapLeadConversionResponse,
  mapLeadFiltersToSearchRequest,
  mapLeadFormToBackendPayload,
  mapLeadListResponse,
  mapOpportunityFiltersToSearchRequest,
  mapOpportunityFormToBackendPayload,
  mapOpportunityListResponse,
  mapOpportunityPipelineResponse,
  mapSingleAccountResponse,
  mapSingleLeadResponse,
  mapSingleOpportunityResponse,
} from './mappers';
import type {
  Account,
  Customer,
  Contact,
  Lead,
  Opportunity,
  Activity,
  CreateAccountRequest,
  CreateCustomerRequest,
  CreateLeadRequest,
  CreateOpportunityRequest,
  CreateActivityRequest,
  UpdateAccountRequest,
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
        url: '/accounts/search',
        method: 'POST',
        body: mapCustomerFiltersToAccountSearch(filters, pagination),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapAccountListResponse,
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
        url: `/accounts/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleAccountResponse,
      providesTags: (result, error, id) => [{ type: 'Customer', id }],
    }),

    createCustomer: builder.mutation<
      APIResponse<Customer>,
      CreateCustomerRequest
    >({
      query: (data) => ({
        url: '/accounts',
        method: 'POST',
        body: mapAccountFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleAccountResponse,
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),

    updateCustomer: builder.mutation<
      APIResponse<Customer>,
      { id: string; data: UpdateCustomerRequest }
    >({
      query: ({ id, data }) => ({
        url: `/accounts/${id}`,
        method: 'PUT',
        body: mapAccountFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleAccountResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
      ],
    }),

    deleteCustomer: builder.mutation<APIResponse<{ deleted: boolean }>, string>(
      {
        query: (id) => ({
          url: `/accounts/${id}`,
          method: 'DELETE',
        }),
        extraOptions: { service: 'crm' },
        invalidatesTags: (result, error, id) => [
          { type: 'Customer', id },
          { type: 'Customer', id: 'LIST' },
        ],
      }
    ),

    getAccountContacts: builder.query<APIResponse<Contact[]>, string>({
      query: (accountId) => ({
        url: `/accounts/${accountId}/contacts`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapContactListResponse,
      providesTags: (result, error, accountId) => [
        { type: 'Customer', id: `${accountId}-CONTACTS` },
      ],
    }),

    createAccountContact: builder.mutation<
      APIResponse<Contact>,
      { accountId: string; data: Record<string, unknown> }
    >({
      query: ({ accountId, data }) => ({
        url: `/accounts/${accountId}/contacts`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: (result, error, { accountId }) => [
        { type: 'Customer', id: `${accountId}-CONTACTS` },
      ],
    }),

    updateContact: builder.mutation<APIResponse<Contact>, { id: string; data: Record<string, unknown> }>({
      query: ({ id, data }) => ({
        url: `/contacts/${id}`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
    }),

    deleteContact: builder.mutation<APIResponse<{ deleted: boolean }>, string>({
      query: (id) => ({
        url: `/contacts/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'crm' },
    }),

    setPrimaryContact: builder.mutation<APIResponse<Contact>, string>({
      query: (id) => ({
        url: `/contacts/${id}/set-primary`,
        method: 'PUT',
      }),
      extraOptions: { service: 'crm' },
    }),

    deactivateContact: builder.mutation<APIResponse<Contact>, string>({
      query: (id) => ({
        url: `/contacts/${id}/deactivate`,
        method: 'PUT',
      }),
      extraOptions: { service: 'crm' },
    }),

    getAccountActivities: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { accountId: string; page?: number; size?: number }
    >({
      query: ({ accountId, page = 1, size = 20 }) => ({
        url: `/accounts/${accountId}/activities`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: (result, error, { accountId }) => [
        { type: 'Activity', id: `${accountId}-ACCOUNT` },
      ],
    }),

    // Lead endpoints
    getLeads: builder.query<
      APIResponse<PaginatedResponse<Lead>>,
      { filters?: LeadFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/leads/search',
        method: 'POST',
        body: mapLeadFiltersToSearchRequest(filters, pagination),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapLeadListResponse,
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
      transformResponse: mapSingleLeadResponse,
      providesTags: (result, error, id) => [{ type: 'Lead', id }],
    }),

    getLeadActivities: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { leadId: string; page?: number; size?: number }
    >({
      query: ({ leadId, page = 1, size = 20 }) => ({
        url: `/leads/${leadId}/activities`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: (result, error, { leadId }) => [
        { type: 'Activity', id: `${leadId}-LEAD` },
      ],
    }),

    createLead: builder.mutation<APIResponse<Lead>, CreateLeadRequest>({
      query: (data) => ({
        url: '/leads',
        method: 'POST',
        body: mapLeadFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleLeadResponse,
      invalidatesTags: [{ type: 'Lead', id: 'LIST' }],
    }),

    updateLead: builder.mutation<
      APIResponse<Lead>,
      { id: string; data: UpdateLeadRequest }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}`,
        method: 'PUT',
        body: mapLeadFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleLeadResponse,
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
      APIResponse<{
        leadId: string;
        accountId?: string;
        opportunityId?: string;
        contactId?: string;
        message?: string;
      }>,
      { id: string; data: Record<string, unknown> }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}/convert`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapLeadConversionResponse,
      invalidatesTags: [
        { type: 'Lead', id: 'LIST' },
        { type: 'Customer', id: 'LIST' },
        { type: 'Opportunity', id: 'LIST' },
      ],
    }),

    qualifyLead: builder.mutation<
      APIResponse<Lead>,
      {
        id: string;
        data: {
          notes: string;
          budgetConfirmed?: boolean;
          hasAuthority?: boolean;
          needIdentified?: boolean;
          timelineEstablished?: boolean;
        };
      }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}/qualify`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleLeadResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
      ],
    }),

    disqualifyLead: builder.mutation<
      APIResponse<Lead>,
      { id: string; data: { notes: string } }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}/disqualify`,
        method: 'POST',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleLeadResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
      ],
    }),

    assignLead: builder.mutation<
      APIResponse<Lead>,
      { id: string; data: { assignedTo: number; notes?: string } }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}/assign`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleLeadResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
      ],
    }),

    bulkAssignLeads: builder.mutation<
      APIResponse<{ assignedCount: number; assignedTo: number }>,
      { leadIds: number[]; assignedTo: number; notes?: string }
    >({
      query: (data) => ({
        url: '/leads/bulk-assign',
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      invalidatesTags: [{ type: 'Lead', id: 'LIST' }],
    }),

    // Opportunity endpoints
    getOpportunities: builder.query<
      APIResponse<PaginatedResponse<Opportunity>>,
      { filters?: OpportunityFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/opportunities/search',
        method: 'POST',
        body: mapOpportunityFiltersToSearchRequest(filters, pagination),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapOpportunityListResponse,
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
      transformResponse: mapSingleOpportunityResponse,
      providesTags: (result, error, id) => [{ type: 'Opportunity', id }],
    }),

    getOpportunityActivities: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { opportunityId: string; page?: number; size?: number }
    >({
      query: ({ opportunityId, page = 1, size = 20 }) => ({
        url: `/opportunities/${opportunityId}/activities`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: (result, error, { opportunityId }) => [
        { type: 'Activity', id: `${opportunityId}-OPPORTUNITY` },
      ],
    }),

    getOpportunityPipeline: builder.query<
      APIResponse<{
        stages: Array<{
          stage: Opportunity['stage'];
          count: number;
          totalValue: number;
          weightedValue: number;
          opportunities: Opportunity[];
        }>;
        summary: {
          totalOpportunities: number;
          totalPipelineValue: number;
          weightedPipelineValue: number;
          averageDealSize: number;
        };
      }>,
      {
        assignedTo?: string;
        accountId?: string;
        fromDate?: string;
        toDate?: string;
      }
    >({
      query: ({ assignedTo, accountId, fromDate, toDate } = {}) => ({
        url: '/opportunities/pipeline',
        method: 'GET',
        params: {
          assignedTo:
            assignedTo && assignedTo.trim() !== ''
              ? Number(assignedTo)
              : undefined,
          accountId:
            accountId && accountId.trim() !== '' ? Number(accountId) : undefined,
          fromDate: fromDate || undefined,
          toDate: toDate || undefined,
        },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapOpportunityPipelineResponse,
      providesTags: [{ type: 'Opportunity', id: 'PIPELINE' }],
    }),

    createOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      CreateOpportunityRequest
    >({
      query: (data) => ({
        url: '/opportunities',
        method: 'POST',
        body: mapOpportunityFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: [{ type: 'Opportunity', id: 'LIST' }],
    }),

    updateOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      { id: string; data: UpdateOpportunityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}`,
        method: 'PUT',
        body: mapOpportunityFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
      ],
    }),

    changeOpportunityStage: builder.mutation<
      APIResponse<Opportunity>,
      {
        id: string;
        data: {
          stage: Opportunity['stage'];
          lossReason?: string;
          notes?: string;
        };
      }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}/stage`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
        { type: 'Opportunity', id: 'PIPELINE' },
      ],
    }),

    closeOpportunityWon: builder.mutation<
      APIResponse<Opportunity>,
      { id: string; data?: { actualValue?: number; notes?: string } }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}/close-won`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
        { type: 'Opportunity', id: 'PIPELINE' },
      ],
    }),

    closeOpportunityLost: builder.mutation<
      APIResponse<Opportunity>,
      { id: string; data: { lossReason: string } }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}/close-lost`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
        { type: 'Opportunity', id: 'PIPELINE' },
      ],
    }),

    assignOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      { id: string; data: { assignedTo: number } }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}/assign`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
        { type: 'Opportunity', id: 'PIPELINE' },
      ],
    }),

    reopenOpportunity: builder.mutation<
      APIResponse<Opportunity>,
      {
        id: string;
        data: { stage: Opportunity['stage']; reopenReason: string };
      }
    >({
      query: ({ id, data }) => ({
        url: `/opportunities/${id}/reopen`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleOpportunityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Opportunity', id },
        { type: 'Opportunity', id: 'LIST' },
        { type: 'Opportunity', id: 'PIPELINE' },
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
        { type: 'Opportunity', id: 'PIPELINE' },
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
  useGetAccountContactsQuery,
  useCreateAccountContactMutation,
  useUpdateContactMutation,
  useDeleteContactMutation,
  useSetPrimaryContactMutation,
  useDeactivateContactMutation,
  useGetAccountActivitiesQuery,

  // Lead hooks
  useGetLeadsQuery,
  useGetLeadQuery,
  useGetLeadActivitiesQuery,
  useCreateLeadMutation,
  useUpdateLeadMutation,
  useDeleteLeadMutation,
  useConvertLeadMutation,
  useQualifyLeadMutation,
  useDisqualifyLeadMutation,
  useAssignLeadMutation,
  useBulkAssignLeadsMutation,

  // Opportunity hooks
  useGetOpportunitiesQuery,
  useGetOpportunityQuery,
  useGetOpportunityActivitiesQuery,
  useGetOpportunityPipelineQuery,
  useCreateOpportunityMutation,
  useUpdateOpportunityMutation,
  useChangeOpportunityStageMutation,
  useCloseOpportunityWonMutation,
  useCloseOpportunityLostMutation,
  useAssignOpportunityMutation,
  useReopenOpportunityMutation,
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
  useBulkDeleteActivitiesMutation,
} = crmApi;

export const useGetAccountsQuery = useGetCustomersQuery;
export const useGetAccountQuery = useGetCustomerQuery;
export const useCreateAccountMutation = useCreateCustomerMutation;
export const useUpdateAccountMutation = useUpdateCustomerMutation;
export const useDeleteAccountMutation = useDeleteCustomerMutation;
