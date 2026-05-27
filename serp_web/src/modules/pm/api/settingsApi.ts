/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type { PMIssueTypeApi } from '../types/api';
import type {
  PMCreateIssueTypeRequest,
  PMCreateIssueTypeSchemeRequest,
  PMDeleteIssueTypeResponse,
  PMDeleteIssueTypeSchemeResponse,
  PMIssueTypeSettingsOverviewApi,
  PMManageIssueTypeSchemeItemsRequest,
  PMUpdateIssueTypeRequest,
  PMUpdateIssueTypeSchemeRequest,
  PMWorkTypeSchemeSettingsApi,
} from '../types/settings-api.types';

export const pmSettingsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmIssueTypeSettingsOverview:
      builder.query<PMIssueTypeSettingsOverviewApi, void>({
        query: () => ({
          url: '/issue-type-settings',
          method: 'GET',
        }),
        extraOptions: { service: 'pm' },
        transformResponse:
          createDataTransform<PMIssueTypeSettingsOverviewApi>(),
        providesTags: [
          { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
        ],
      }),

    createPmIssueType: builder.mutation<
      PMIssueTypeApi,
      PMCreateIssueTypeRequest
    >({
      query: (body) => ({
        url: '/issue-types',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMIssueTypeApi>(),
      invalidatesTags: [
        { type: 'pm/IssueType' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmIssueType: builder.mutation<
      PMIssueTypeApi,
      { id: number; body: PMUpdateIssueTypeRequest }
    >({
      query: ({ id, body }) => ({
        url: `/issue-types/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMIssueTypeApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/IssueType' as const, id },
        { type: 'pm/IssueType' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmIssueType: builder.mutation<PMDeleteIssueTypeResponse, number>({
      query: (id) => ({
        url: `/issue-types/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMDeleteIssueTypeResponse>(),
      invalidatesTags: [
        { type: 'pm/IssueType' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    createPmIssueTypeScheme: builder.mutation<
      PMWorkTypeSchemeSettingsApi,
      PMCreateIssueTypeSchemeRequest
    >({
      query: (body) => ({
        url: '/issue-type-schemes',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkTypeSchemeSettingsApi>(),
      invalidatesTags: [
        { type: 'pm/IssueTypeScheme' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmIssueTypeScheme: builder.mutation<
      PMWorkTypeSchemeSettingsApi,
      { id: number; body: PMUpdateIssueTypeSchemeRequest }
    >({
      query: ({ id, body }) => ({
        url: `/issue-type-schemes/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkTypeSchemeSettingsApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/IssueTypeScheme' as const, id },
        { type: 'pm/IssueTypeScheme' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    managePmIssueTypeSchemeItems: builder.mutation<
      PMWorkTypeSchemeSettingsApi,
      { id: number; body: PMManageIssueTypeSchemeItemsRequest }
    >({
      query: ({ id, body }) => ({
        url: `/issue-type-schemes/${id}/items`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkTypeSchemeSettingsApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/IssueTypeScheme' as const, id },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmIssueTypeScheme: builder.mutation<
      PMDeleteIssueTypeSchemeResponse,
      number
    >({
      query: (id) => ({
        url: `/issue-type-schemes/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse:
        createDataTransform<PMDeleteIssueTypeSchemeResponse>(),
      invalidatesTags: [
        { type: 'pm/IssueTypeScheme' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useCreatePmIssueTypeMutation,
  useCreatePmIssueTypeSchemeMutation,
  useDeletePmIssueTypeMutation,
  useDeletePmIssueTypeSchemeMutation,
  useGetPmIssueTypeSettingsOverviewQuery,
  useManagePmIssueTypeSchemeItemsMutation,
  useUpdatePmIssueTypeMutation,
  useUpdatePmIssueTypeSchemeMutation,
} = pmSettingsApi;
