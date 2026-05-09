/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item API endpoints
 */

import { api } from '@/lib/store/api';
import {
  createDataTransform,
  createPaginatedTransform,
} from '@/lib/store/api/utils';
import type { PaginatedResponse } from '@/lib/store/api/types';
import type {
  PMCreateWorkItemRequest,
  PMCreateWorkItemResponse,
  PMSearchWorkItemsParams,
  PMWorkItemCreateMetaResponse,
  PMWorkItemSearchApi,
} from '../types/api';

export const pmWorkItemApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPmWorkItemCreateMeta: builder.query<
      PMWorkItemCreateMetaResponse,
      { projectId: number; issueTypeId?: number }
    >({
      query: ({ projectId, issueTypeId }) => ({
        url: `/projects/${projectId}/work-items/create-meta`,
        method: 'GET',
        params: {
          ...(typeof issueTypeId === 'number' ? { issueTypeId } : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemCreateMetaResponse>(),
    }),

    createPmWorkItem: builder.mutation<
      PMCreateWorkItemResponse,
      { projectId: number; body: PMCreateWorkItemRequest }
    >({
      query: ({ projectId, body }) => ({
        url: `/projects/${projectId}/work-items`,
        method: 'POST',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMCreateWorkItemResponse>(),
    }),

    searchPmWorkItems: builder.query<
      PaginatedResponse<PMWorkItemSearchApi>,
      { projectId: number; params?: PMSearchWorkItemsParams }
    >({
      query: ({ projectId, params }) => ({
        url: `/projects/${projectId}/work-items`,
        method: 'GET',
        params: {
          ...(params?.keyword ? { keyword: params.keyword } : {}),
          ...(params?.issueTypeIds?.length
            ? { issueTypeIds: params.issueTypeIds }
            : {}),
          page: params?.page ?? 0,
          pageSize: params?.pageSize ?? 20,
          ...(params?.sortField ? { sortField: params.sortField } : {}),
          ...(params?.sortDirection
            ? { sortDirection: params.sortDirection }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMWorkItemSearchApi>(),
    }),
  }),
  overrideExisting: false,
});

export const {
  useCreatePmWorkItemMutation,
  useGetPmWorkItemCreateMetaQuery,
  useSearchPmWorkItemsQuery,
} = pmWorkItemApi;
