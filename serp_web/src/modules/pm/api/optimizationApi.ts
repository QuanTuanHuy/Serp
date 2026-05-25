/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  PMApplyOptimizationRunRequest,
  PMGenerateOptimizationRunRequest,
  PMOptimizationRunApi,
  PMUpdateOptimizationRunItemDecisionRequest,
} from '../types/api';

export const pmOptimizationApi = api.injectEndpoints({
  endpoints: (builder) => ({
    generatePmOptimizationRun: builder.mutation<
      PMOptimizationRunApi,
      { projectId: number; body: PMGenerateOptimizationRunRequest }
    >({
      query: ({ projectId, body }) => ({
        url: `/projects/${projectId}/optimization-runs`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMOptimizationRunApi>(),
      invalidatesTags: (_result, _error, { projectId }) => [
        { type: 'pm/OptimizationRun' as const, id: `project-${projectId}` },
      ],
    }),

    getPmOptimizationRun: builder.query<
      PMOptimizationRunApi,
      { projectId: number; runId: number }
    >({
      query: ({ projectId, runId }) => ({
        url: `/projects/${projectId}/optimization-runs/${runId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMOptimizationRunApi>(),
      providesTags: (_result, _error, { runId }) => [
        { type: 'pm/OptimizationRun' as const, id: runId },
      ],
    }),

    updatePmOptimizationRunItemDecision: builder.mutation<
      PMOptimizationRunApi,
      {
        projectId: number;
        runId: number;
        workItemId: number;
        body: PMUpdateOptimizationRunItemDecisionRequest;
      }
    >({
      query: ({ projectId, runId, workItemId, body }) => ({
        url: `/projects/${projectId}/optimization-runs/${runId}/items/${workItemId}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMOptimizationRunApi>(),
      invalidatesTags: (_result, _error, { runId }) => [
        { type: 'pm/OptimizationRun' as const, id: runId },
      ],
    }),

    applyPmOptimizationRun: builder.mutation<
      PMOptimizationRunApi,
      { projectId: number; runId: number; body: PMApplyOptimizationRunRequest }
    >({
      query: ({ projectId, runId, body }) => ({
        url: `/projects/${projectId}/optimization-runs/${runId}/apply`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMOptimizationRunApi>(),
      invalidatesTags: (_result, _error, { runId }) => [
        { type: 'pm/OptimizationRun' as const, id: runId },
      ],
    }),

    discardPmOptimizationRun: builder.mutation<
      PMOptimizationRunApi,
      { projectId: number; runId: number }
    >({
      query: ({ projectId, runId }) => ({
        url: `/projects/${projectId}/optimization-runs/${runId}/discard`,
        method: 'POST',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMOptimizationRunApi>(),
      invalidatesTags: (_result, _error, { runId }) => [
        { type: 'pm/OptimizationRun' as const, id: runId },
      ],
    }),
  }),
  overrideExisting: false,
});

export const {
  useApplyPmOptimizationRunMutation,
  useDiscardPmOptimizationRunMutation,
  useGeneratePmOptimizationRunMutation,
  useGetPmOptimizationRunQuery,
  useUpdatePmOptimizationRunItemDecisionMutation,
} = pmOptimizationApi;
