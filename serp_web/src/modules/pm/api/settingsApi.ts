/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type { PMIssueTypeApi } from '../types/api';
import type {
  PMAddWorkflowStepRequest,
  PMAddWorkflowTransitionRequest,
  PMCreateIssueTypeRequest,
  PMCreateIssueTypeSchemeRequest,
  PMCreatePriorityRequest,
  PMCreatePrioritySchemeRequest,
  PMCreateWorkflowRequest,
  PMCreateWorkflowSchemeRequest,
  PMDeleteIssueTypeResponse,
  PMDeleteIssueTypeSchemeResponse,
  PMDeletePriorityResponse,
  PMDeletePrioritySchemeResponse,
  PMDeleteWorkflowStepResponse,
  PMDeleteWorkflowTransitionResponse,
  PMDeleteWorkflowSchemeResponse,
  PMIssueTypeSettingsOverviewApi,
  PMManageIssueTypeSchemeItemsRequest,
  PMManagePrioritySchemeItemsRequest,
  PMManageWorkflowSchemeItemsRequest,
  PMPrioritySchemeSettingsApi,
  PMPrioritySettingsOverviewApi,
  PMReorderWorkflowStepsRequest,
  PMUpdateIssueTypeRequest,
  PMUpdateIssueTypeSchemeRequest,
  PMUpdatePriorityRequest,
  PMUpdatePrioritySchemeRequest,
  PMUpdateWorkflowRequest,
  PMUpdateWorkflowSchemeRequest,
  PMUpdateWorkflowTransitionRequest,
  PMWorkflowEditorApi,
  PMWorkflowOptionApi,
  PMWorkflowSchemeSettingsApi,
  PMWorkflowSettingsOverviewApi,
  PMWorkflowStepApi,
  PMWorkflowTransitionApi,
  PMWorkflowValidationApi,
  PMWorkTypeSchemeSettingsApi,
} from '../types/settings-api.types';
import type { PMPriorityApi } from '../types/work-item-api.types';

export const pmSettingsApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmIssueTypeSettingsOverview: builder.query<
      PMIssueTypeSettingsOverviewApi,
      void
    >({
      query: () => ({
        url: '/issue-type-settings',
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMIssueTypeSettingsOverviewApi>(),
      providesTags: [{ type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' }],
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
      transformResponse: createDataTransform<PMDeleteIssueTypeSchemeResponse>(),
      invalidatesTags: [
        { type: 'pm/IssueTypeScheme' as const, id: 'LIST' },
        { type: 'pm/IssueTypeSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    getPmPrioritySettingsOverview: builder.query<
      PMPrioritySettingsOverviewApi,
      void
    >({
      query: () => ({
        url: '/priority-settings',
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMPrioritySettingsOverviewApi>(),
      providesTags: [{ type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' }],
    }),

    createPmPriority: builder.mutation<PMPriorityApi, PMCreatePriorityRequest>({
      query: (body) => ({
        url: '/priorities',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMPriorityApi>(),
      invalidatesTags: [
        { type: 'pm/Priority' as const, id: 'LIST' },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmPriority: builder.mutation<
      PMPriorityApi,
      { id: number; body: PMUpdatePriorityRequest }
    >({
      query: ({ id, body }) => ({
        url: `/priorities/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMPriorityApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/Priority' as const, id },
        { type: 'pm/Priority' as const, id: 'LIST' },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmPriority: builder.mutation<PMDeletePriorityResponse, number>({
      query: (id) => ({
        url: `/priorities/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMDeletePriorityResponse>(),
      invalidatesTags: [
        { type: 'pm/Priority' as const, id: 'LIST' },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    createPmPriorityScheme: builder.mutation<
      PMPrioritySchemeSettingsApi,
      PMCreatePrioritySchemeRequest
    >({
      query: (body) => ({
        url: '/priority-schemes',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMPrioritySchemeSettingsApi>(),
      invalidatesTags: [
        { type: 'pm/PriorityScheme' as const, id: 'LIST' },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmPriorityScheme: builder.mutation<
      PMPrioritySchemeSettingsApi,
      { id: number; body: PMUpdatePrioritySchemeRequest }
    >({
      query: ({ id, body }) => ({
        url: `/priority-schemes/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMPrioritySchemeSettingsApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/PriorityScheme' as const, id },
        { type: 'pm/PriorityScheme' as const, id: 'LIST' },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    managePmPrioritySchemeItems: builder.mutation<
      PMPrioritySchemeSettingsApi,
      { id: number; body: PMManagePrioritySchemeItemsRequest }
    >({
      query: ({ id, body }) => ({
        url: `/priority-schemes/${id}/items`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMPrioritySchemeSettingsApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/PriorityScheme' as const, id },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmPriorityScheme: builder.mutation<
      PMDeletePrioritySchemeResponse,
      number
    >({
      query: (id) => ({
        url: `/priority-schemes/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMDeletePrioritySchemeResponse>(),
      invalidatesTags: [
        { type: 'pm/PriorityScheme' as const, id: 'LIST' },
        { type: 'pm/PrioritySettings' as const, id: 'OVERVIEW' },
      ],
    }),

    getPmWorkflowSettingsOverview: builder.query<
      PMWorkflowSettingsOverviewApi,
      void
    >({
      query: () => ({
        url: '/workflow-settings',
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowSettingsOverviewApi>(),
      providesTags: [{ type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' }],
    }),

    getPmWorkflowEditor: builder.query<PMWorkflowEditorApi, number>({
      query: (workflowId) => ({
        url: `/workflows/${workflowId}/editor`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowEditorApi>(),
      providesTags: (_result, _error, workflowId) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
      ],
    }),

    createPmWorkflow: builder.mutation<
      PMWorkflowOptionApi,
      PMCreateWorkflowRequest
    >({
      query: (body) => ({
        url: '/workflows',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowOptionApi>(),
      invalidatesTags: [
        { type: 'pm/Workflow' as const, id: 'LIST' },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmWorkflow: builder.mutation<
      PMWorkflowOptionApi,
      { id: number; body: PMUpdateWorkflowRequest }
    >({
      query: ({ id, body }) => ({
        url: `/workflows/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowOptionApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/Workflow' as const, id },
        { type: 'pm/WorkflowEditor' as const, id },
        { type: 'pm/Workflow' as const, id: 'LIST' },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    addPmWorkflowStep: builder.mutation<
      PMWorkflowStepApi,
      { workflowId: number; body: PMAddWorkflowStepRequest }
    >({
      query: ({ workflowId, body }) => ({
        url: `/workflows/${workflowId}/steps`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowStepApi>(),
      invalidatesTags: (_result, _error, { workflowId }) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    removePmWorkflowStep: builder.mutation<
      PMDeleteWorkflowStepResponse,
      { workflowId: number; stepId: number }
    >({
      query: ({ workflowId, stepId }) => ({
        url: `/workflows/${workflowId}/steps/${stepId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMDeleteWorkflowStepResponse>(),
      invalidatesTags: (_result, _error, { workflowId }) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
      ],
    }),

    reorderPmWorkflowSteps: builder.mutation<
      PMWorkflowStepApi[],
      { workflowId: number; body: PMReorderWorkflowStepsRequest }
    >({
      query: ({ workflowId, body }) => ({
        url: `/workflows/${workflowId}/steps/reorder`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowStepApi[]>(),
      invalidatesTags: (_result, _error, { workflowId }) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
      ],
    }),

    addPmWorkflowTransition: builder.mutation<
      PMWorkflowTransitionApi,
      { workflowId: number; body: PMAddWorkflowTransitionRequest }
    >({
      query: ({ workflowId, body }) => ({
        url: `/workflows/${workflowId}/transitions`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowTransitionApi>(),
      invalidatesTags: (_result, _error, { workflowId }) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
      ],
    }),

    updatePmWorkflowTransition: builder.mutation<
      PMWorkflowTransitionApi,
      {
        workflowId: number;
        transitionId: number;
        body: PMUpdateWorkflowTransitionRequest;
      }
    >({
      query: ({ workflowId, transitionId, body }) => ({
        url: `/workflows/${workflowId}/transitions/${transitionId}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowTransitionApi>(),
      invalidatesTags: (_result, _error, { workflowId }) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
      ],
    }),

    removePmWorkflowTransition: builder.mutation<
      PMDeleteWorkflowTransitionResponse,
      { workflowId: number; transitionId: number }
    >({
      query: ({ workflowId, transitionId }) => ({
        url: `/workflows/${workflowId}/transitions/${transitionId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse:
        createDataTransform<PMDeleteWorkflowTransitionResponse>(),
      invalidatesTags: (_result, _error, { workflowId }) => [
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
      ],
    }),

    validatePmWorkflow: builder.mutation<PMWorkflowValidationApi, number>({
      query: (workflowId) => ({
        url: `/workflows/${workflowId}/validate`,
        method: 'POST',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowValidationApi>(),
    }),

    publishPmWorkflow: builder.mutation<PMWorkflowOptionApi, number>({
      query: (workflowId) => ({
        url: `/workflows/${workflowId}/publish`,
        method: 'POST',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowOptionApi>(),
      invalidatesTags: (_result, _error, workflowId) => [
        { type: 'pm/Workflow' as const, id: workflowId },
        { type: 'pm/WorkflowEditor' as const, id: workflowId },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    createPmWorkflowScheme: builder.mutation<
      PMWorkflowSchemeSettingsApi,
      PMCreateWorkflowSchemeRequest
    >({
      query: (body) => ({
        url: '/workflow-schemes',
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowSchemeSettingsApi>(),
      invalidatesTags: [
        { type: 'pm/WorkflowScheme' as const, id: 'LIST' },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    updatePmWorkflowScheme: builder.mutation<
      PMWorkflowSchemeSettingsApi,
      { id: number; body: PMUpdateWorkflowSchemeRequest }
    >({
      query: ({ id, body }) => ({
        url: `/workflow-schemes/${id}`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowSchemeSettingsApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/WorkflowScheme' as const, id },
        { type: 'pm/WorkflowScheme' as const, id: 'LIST' },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    managePmWorkflowSchemeItems: builder.mutation<
      PMWorkflowSchemeSettingsApi,
      { id: number; body: PMManageWorkflowSchemeItemsRequest }
    >({
      query: ({ id, body }) => ({
        url: `/workflow-schemes/${id}/items`,
        method: 'PUT',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkflowSchemeSettingsApi>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'pm/WorkflowScheme' as const, id },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),

    deletePmWorkflowScheme: builder.mutation<
      PMDeleteWorkflowSchemeResponse,
      number
    >({
      query: (id) => ({
        url: `/workflow-schemes/${id}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMDeleteWorkflowSchemeResponse>(),
      invalidatesTags: [
        { type: 'pm/WorkflowScheme' as const, id: 'LIST' },
        { type: 'pm/WorkflowSettings' as const, id: 'OVERVIEW' },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useAddPmWorkflowStepMutation,
  useAddPmWorkflowTransitionMutation,
  useCreatePmWorkflowMutation,
  useCreatePmWorkflowSchemeMutation,
  useCreatePmPriorityMutation,
  useCreatePmPrioritySchemeMutation,
  useCreatePmIssueTypeMutation,
  useCreatePmIssueTypeSchemeMutation,
  useDeletePmWorkflowSchemeMutation,
  useDeletePmPriorityMutation,
  useDeletePmPrioritySchemeMutation,
  useDeletePmIssueTypeMutation,
  useDeletePmIssueTypeSchemeMutation,
  useGetPmWorkflowEditorQuery,
  useGetPmWorkflowSettingsOverviewQuery,
  useGetPmPrioritySettingsOverviewQuery,
  useGetPmIssueTypeSettingsOverviewQuery,
  useManagePmWorkflowSchemeItemsMutation,
  useManagePmPrioritySchemeItemsMutation,
  useManagePmIssueTypeSchemeItemsMutation,
  usePublishPmWorkflowMutation,
  useRemovePmWorkflowStepMutation,
  useRemovePmWorkflowTransitionMutation,
  useReorderPmWorkflowStepsMutation,
  useUpdatePmWorkflowMutation,
  useUpdatePmWorkflowSchemeMutation,
  useUpdatePmWorkflowTransitionMutation,
  useUpdatePmPriorityMutation,
  useUpdatePmPrioritySchemeMutation,
  useUpdatePmIssueTypeMutation,
  useUpdatePmIssueTypeSchemeMutation,
  useValidatePmWorkflowMutation,
} = pmSettingsApi;
