// CRM Opportunity API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import {
  mapActivityListResponse,
  mapOpportunityFiltersToSearchRequest,
  mapOpportunityFormToBackendPayload,
  mapOpportunityListResponse,
  mapOpportunityPipelineResponse,
  mapSingleOpportunityResponse,
} from './mappers';
import type {
  Opportunity,
  Activity,
  CreateOpportunityRequest,
  UpdateOpportunityRequest,
  OpportunityFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
} from '../types';

export const opportunityApi = api.injectEndpoints({
  endpoints: (builder) => ({
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
            accountId && accountId.trim() !== ''
              ? Number(accountId)
              : undefined,
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
  }),
});

export const {
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
} = opportunityApi;
