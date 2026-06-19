// CRM Lead API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import {
  mapActivityListResponse,
  mapLeadFiltersToSearchRequest,
  mapLeadFormToBackendPayload,
  mapLeadListResponse,
  mapLeadStatusTransitionResponse,
  mapSingleLeadResponse,
} from './mappers';
import type {
  Lead,
  Activity,
  CreateLeadRequest,
  UpdateLeadRequest,
  UpdateLeadStatusRequest,
  LeadFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
  LeadStatusTransitionResult,
} from '../types';

export const leadApi = api.injectEndpoints({
  endpoints: (builder) => ({
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

    updateLeadStatus: builder.mutation<
      APIResponse<LeadStatusTransitionResult>,
      { id: string; data: UpdateLeadStatusRequest }
    >({
      query: ({ id, data }) => ({
        url: `/leads/${id}/status`,
        method: 'PATCH',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapLeadStatusTransitionResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Lead', id },
        { type: 'Lead', id: 'LIST' },
        { type: 'Customer', id: 'LIST' },
        { type: 'Opportunity', id: 'LIST' },
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
  }),
});

export const {
  useGetLeadsQuery,
  useGetLeadQuery,
  useGetLeadActivitiesQuery,
  useCreateLeadMutation,
  useUpdateLeadMutation,
  useUpdateLeadStatusMutation,
  useDeleteLeadMutation,
  useAssignLeadMutation,
  useBulkAssignLeadsMutation,
} = leadApi;
