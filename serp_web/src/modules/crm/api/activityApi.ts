// CRM Activity API Endpoints (authors: QuanTuanHuy, Description: Part of Serp Project)

import { api } from '@/lib/store/api';
import {
  mapActivityArrayResponse,
  mapActivityFiltersToBackendRequest,
  mapActivityFormToBackendPayload,
  mapActivityListResponse,
  mapBackendStatsToActivityStats,
  mapBulkActivityRequestToBackend,
  mapBulkActivityResponse,
  mapSingleActivityResponse,
} from './mappers';
import type {
  Activity,
  CreateActivityRequest,
  UpdateActivityRequest,
  CompleteActivityRequest,
  RescheduleActivityRequest,
  ActivityFilters,
  PaginationParams,
  APIResponse,
  PaginatedResponse,
  ActivityStats,
  BulkActivityRequest,
  BulkActivityResult,
  BulkOperationResult,
} from '../types';

export const activityApi = api.injectEndpoints({
  endpoints: (builder) => ({
    // Activity endpoints
    searchActivities: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { filters?: ActivityFilters; pagination: PaginationParams }
    >({
      query: ({ filters = {}, pagination }) => ({
        url: '/activities/search',
        method: 'POST',
        body: mapActivityFiltersToBackendRequest(filters, pagination),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
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

    getActivityStats: builder.query<APIResponse<ActivityStats>, void>({
      query: () => ({
        url: '/activities/stats',
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapBackendStatsToActivityStats,
      providesTags: [{ type: 'Activity', id: 'STATS' }],
    }),

    getActivity: builder.query<APIResponse<Activity>, string>({
      query: (id) => ({
        url: `/activities/${id}`,
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleActivityResponse,
      providesTags: (result, error, id) => [{ type: 'Activity', id }],
    }),

    getActivitiesByType: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { type: string; pagination: PaginationParams }
    >({
      query: ({ type, pagination }) => ({
        url: `/activities/type/${type}`,
        method: 'GET',
        params: { page: pagination.page, size: pagination.limit },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: [{ type: 'Activity', id: 'LIST' }],
    }),

    getActivitiesByStatus: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { status: string; pagination: PaginationParams }
    >({
      query: ({ status, pagination }) => ({
        url: `/activities/status/${status}`,
        method: 'GET',
        params: { page: pagination.page, size: pagination.limit },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: [{ type: 'Activity', id: 'LIST' }],
    }),

    getActivitiesByAssignee: builder.query<
      APIResponse<PaginatedResponse<Activity>>,
      { assigneeId: string; pagination: PaginationParams }
    >({
      query: ({ assigneeId, pagination }) => ({
        url: `/activities/assignee/${assigneeId}`,
        method: 'GET',
        params: { page: pagination.page, size: pagination.limit },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityListResponse,
      providesTags: [{ type: 'Activity', id: 'LIST' }],
    }),

    getOverdueActivities: builder.query<APIResponse<Activity[]>, void>({
      query: () => ({
        url: '/activities/overdue',
        method: 'GET',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityArrayResponse,
      providesTags: [{ type: 'Activity', id: 'OVERDUE' }],
    }),

    getUpcomingActivities: builder.query<
      APIResponse<Activity[]>,
      { startDate: string; endDate: string }
    >({
      query: ({ startDate, endDate }) => ({
        url: '/activities/upcoming',
        method: 'GET',
        params: { startDate, endDate },
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapActivityArrayResponse,
      providesTags: [{ type: 'Activity', id: 'UPCOMING' }],
    }),

    createActivity: builder.mutation<
      APIResponse<Activity>,
      CreateActivityRequest
    >({
      query: (data) => ({
        url: '/activities',
        method: 'POST',
        body: mapActivityFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleActivityResponse,
      invalidatesTags: [{ type: 'Activity', id: 'LIST' }],
    }),

    updateActivity: builder.mutation<
      APIResponse<Activity>,
      { id: string; data: UpdateActivityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/activities/${id}`,
        method: 'PUT',
        body: mapActivityFormToBackendPayload(data),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleActivityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Activity', id },
        { type: 'Activity', id: 'LIST' },
      ],
    }),

    completeActivity: builder.mutation<
      APIResponse<Activity>,
      { id: string; data?: CompleteActivityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/activities/${id}/complete`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleActivityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Activity', id },
        { type: 'Activity', id: 'LIST' },
        { type: 'Activity', id: 'OVERDUE' },
        { type: 'Activity', id: 'UPCOMING' },
        { type: 'Activity', id: 'STATS' },
      ],
    }),

    cancelActivity: builder.mutation<APIResponse<Activity>, string>({
      query: (id) => ({
        url: `/activities/${id}/cancel`,
        method: 'PUT',
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleActivityResponse,
      invalidatesTags: (result, error, id) => [
        { type: 'Activity', id },
        { type: 'Activity', id: 'LIST' },
        { type: 'Activity', id: 'OVERDUE' },
        { type: 'Activity', id: 'UPCOMING' },
        { type: 'Activity', id: 'STATS' },
      ],
    }),

    rescheduleActivity: builder.mutation<
      APIResponse<Activity>,
      { id: string; data: RescheduleActivityRequest }
    >({
      query: ({ id, data }) => ({
        url: `/activities/${id}/reschedule`,
        method: 'PUT',
        body: data,
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapSingleActivityResponse,
      invalidatesTags: (result, error, { id }) => [
        { type: 'Activity', id },
        { type: 'Activity', id: 'LIST' },
        { type: 'Activity', id: 'OVERDUE' },
        { type: 'Activity', id: 'UPCOMING' },
        { type: 'Activity', id: 'STATS' },
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
          { type: 'Activity', id: 'OVERDUE' },
          { type: 'Activity', id: 'UPCOMING' },
          { type: 'Activity', id: 'STATS' },
        ],
      }
    ),

    bulkActivityOperations: builder.mutation<
      APIResponse<BulkActivityResult>,
      BulkActivityRequest
    >({
      query: (payload) => ({
        url: '/activities/bulk',
        method: 'POST',
        body: mapBulkActivityRequestToBackend(payload),
      }),
      extraOptions: { service: 'crm' },
      transformResponse: mapBulkActivityResponse,
      invalidatesTags: [
        { type: 'Activity', id: 'LIST' },
        { type: 'Activity', id: 'OVERDUE' },
        { type: 'Activity', id: 'UPCOMING' },
        { type: 'Activity', id: 'STATS' },
      ],
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

export const {
  useSearchActivitiesQuery,
  useGetActivityStatsQuery,
  useGetActivityQuery,
  useGetActivitiesByTypeQuery,
  useGetActivitiesByStatusQuery,
  useGetActivitiesByAssigneeQuery,
  useGetOverdueActivitiesQuery,
  useGetUpcomingActivitiesQuery,
  useCreateActivityMutation,
  useUpdateActivityMutation,
  useCompleteActivityMutation,
  useCancelActivityMutation,
  useRescheduleActivityMutation,
  useDeleteActivityMutation,
  useBulkActivityOperationsMutation,
  useBulkDeleteActivitiesMutation,
} = activityApi;
