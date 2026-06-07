/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  PMCreateResourceCalendarExceptionRequest,
  PMCreateResourceCalendarProfileRequest,
  PMReplaceResourceCalendarAssignmentsRequest,
  PMReplaceResourceCalendarBlocksRequest,
  PMResourceCalendarAssignmentApi,
  PMResourceCalendarDeleteResultApi,
  PMResourceCalendarExceptionApi,
  PMResourceCalendarProfileApi,
  PMResourceCalendarSettingsOverviewApi,
  PMUpdateResourceCalendarExceptionRequest,
  PMUpdateResourceCalendarProfileRequest,
} from '../types/resource-calendar-api.types';

export const pmResourceCalendarApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmResourceCalendarSettingsOverview:
      builder.query<PMResourceCalendarSettingsOverviewApi, void>({
        query: () => ({
          url: '/resource-calendar-settings/overview',
          method: 'GET',
        }),
        extraOptions: { service: 'pm' },
        transformResponse:
          createDataTransform<PMResourceCalendarSettingsOverviewApi>(),
        providesTags: [
          { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
        ],
      }),

    createPmResourceCalendarProfile: builder.mutation<
      PMResourceCalendarProfileApi,
      PMCreateResourceCalendarProfileRequest
    >({
      query: (body) => ({
        url: '/resource-calendar-profiles',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMResourceCalendarProfileApi>(),
      invalidatesTags: [
        { type: 'pm/ResourceCalendarProfile' as const, id: 'LIST' },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmResourceCalendarProfile: builder.mutation<
      PMResourceCalendarProfileApi,
      { id: number; body: PMUpdateResourceCalendarProfileRequest }
    >({
      query: ({ id, body }) => ({
        url: `/resource-calendar-profiles/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMResourceCalendarProfileApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/ResourceCalendarProfile' as const, id },
        { type: 'pm/ResourceCalendarProfile' as const, id: 'LIST' },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmResourceCalendarProfile: builder.mutation<
      PMResourceCalendarDeleteResultApi,
      number
    >({
      query: (id) => ({
        url: `/resource-calendar-profiles/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse:
        createDataTransform<PMResourceCalendarDeleteResultApi>(),
      invalidatesTags: [
        { type: 'pm/ResourceCalendarProfile' as const, id: 'LIST' },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    replacePmResourceCalendarBlocks: builder.mutation<
      PMResourceCalendarProfileApi,
      { id: number; body: PMReplaceResourceCalendarBlocksRequest }
    >({
      query: ({ id, body }) => ({
        url: `/resource-calendar-profiles/${id}/blocks`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMResourceCalendarProfileApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/ResourceCalendarProfile' as const, id },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    replacePmResourceCalendarAssignments: builder.mutation<
      PMResourceCalendarAssignmentApi[],
      { id: number; body: PMReplaceResourceCalendarAssignmentsRequest }
    >({
      query: ({ id, body }) => ({
        url: `/resource-calendar-profiles/${id}/assignments`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse:
        createDataTransform<PMResourceCalendarAssignmentApi[]>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/ResourceCalendarProfile' as const, id },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    createPmResourceCalendarException: builder.mutation<
      PMResourceCalendarExceptionApi,
      PMCreateResourceCalendarExceptionRequest
    >({
      query: (body) => ({
        url: '/resource-calendar-exceptions',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMResourceCalendarExceptionApi>(),
      invalidatesTags: [
        { type: 'pm/ResourceCalendarException' as const, id: 'LIST' },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmResourceCalendarException: builder.mutation<
      PMResourceCalendarExceptionApi,
      { id: number; body: PMUpdateResourceCalendarExceptionRequest }
    >({
      query: ({ id, body }) => ({
        url: `/resource-calendar-exceptions/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMResourceCalendarExceptionApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/ResourceCalendarException' as const, id },
        { type: 'pm/ResourceCalendarException' as const, id: 'LIST' },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmResourceCalendarException: builder.mutation<
      PMResourceCalendarDeleteResultApi,
      number
    >({
      query: (id) => ({
        url: `/resource-calendar-exceptions/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse:
        createDataTransform<PMResourceCalendarDeleteResultApi>(),
      invalidatesTags: [
        { type: 'pm/ResourceCalendarException' as const, id: 'LIST' },
        { type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useCreatePmResourceCalendarExceptionMutation,
  useCreatePmResourceCalendarProfileMutation,
  useDeletePmResourceCalendarExceptionMutation,
  useDeletePmResourceCalendarProfileMutation,
  useGetPmResourceCalendarSettingsOverviewQuery,
  useReplacePmResourceCalendarAssignmentsMutation,
  useReplacePmResourceCalendarBlocksMutation,
  useUpdatePmResourceCalendarExceptionMutation,
  useUpdatePmResourceCalendarProfileMutation,
} = pmResourceCalendarApi;
