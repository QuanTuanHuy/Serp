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
  PMGetWorkItemBoardParams,
  PMSearchWorkItemsParams,
  PMWorkItemBoardResponse,
  PMWorkItemCreateMetaResponse,
  PMWorkItemDetailApi,
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
          ...(params?.statusIds?.length ? { statusIds: params.statusIds } : {}),
          ...(params?.priorityIds?.length
            ? { priorityIds: params.priorityIds }
            : {}),
          ...(params?.issueTypeIds?.length
            ? { issueTypeIds: params.issueTypeIds }
            : {}),
          ...(params?.assigneeIds?.length
            ? { assigneeIds: params.assigneeIds }
            : {}),
          ...(params?.reporterIds?.length
            ? { reporterIds: params.reporterIds }
            : {}),
          ...(params?.resolutionIds?.length
            ? { resolutionIds: params.resolutionIds }
            : {}),
          ...(typeof params?.parentId === 'number'
            ? { parentId: params.parentId }
            : {}),
          ...(params?.excludeStatusIds?.length
            ? { excludeStatusIds: params.excludeStatusIds }
            : {}),
          ...(params?.excludeIssueTypeIds?.length
            ? { excludeIssueTypeIds: params.excludeIssueTypeIds }
            : {}),
          ...(typeof params?.unassigned === 'boolean'
            ? { unassigned: params.unassigned }
            : {}),
          ...(typeof params?.unresolved === 'boolean'
            ? { unresolved: params.unresolved }
            : {}),
          ...(typeof params?.dueDateFrom === 'number'
            ? { dueDateFrom: params.dueDateFrom }
            : {}),
          ...(typeof params?.dueDateTo === 'number'
            ? { dueDateTo: params.dueDateTo }
            : {}),
          ...(typeof params?.createdFrom === 'number'
            ? { createdFrom: params.createdFrom }
            : {}),
          ...(typeof params?.createdTo === 'number'
            ? { createdTo: params.createdTo }
            : {}),
          ...(typeof params?.updatedFrom === 'number'
            ? { updatedFrom: params.updatedFrom }
            : {}),
          ...(typeof params?.updatedTo === 'number'
            ? { updatedTo: params.updatedTo }
            : {}),
          ...(params?.sprintIds?.length ? { sprintIds: params.sprintIds } : {}),
          ...(params?.componentIds?.length
            ? { componentIds: params.componentIds }
            : {}),
          ...(params?.fixVersionIds?.length
            ? { fixVersionIds: params.fixVersionIds }
            : {}),
          ...(typeof params?.isOverdue === 'boolean'
            ? { isOverdue: params.isOverdue }
            : {}),
          ...(typeof params?.hasTimeLogged === 'boolean'
            ? { hasTimeLogged: params.hasTimeLogged }
            : {}),
          ...(typeof params?.enriched === 'boolean'
            ? { enriched: params.enriched }
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

    getPmWorkItemById: builder.query<
      PMWorkItemDetailApi,
      { projectId: number; workItemId: number }
    >({
      query: ({ projectId, workItemId }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemDetailApi>(),
    }),

    getPmWorkItemBoard: builder.query<
      PMWorkItemBoardResponse,
      { projectId: number; params?: PMGetWorkItemBoardParams }
    >({
      query: ({ projectId, params }) => ({
        url: `/projects/${projectId}/work-items/board`,
        method: 'GET',
        params: {
          ...(params?.keyword ? { keyword: params.keyword } : {}),
          ...(params?.statusIds?.length ? { statusIds: params.statusIds } : {}),
          ...(params?.assigneeIds?.length
            ? { assigneeIds: params.assigneeIds }
            : {}),
          ...(params?.issueTypeIds?.length
            ? { issueTypeIds: params.issueTypeIds }
            : {}),
          ...(params?.priorityIds?.length
            ? { priorityIds: params.priorityIds }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemBoardResponse>(),
    }),
  }),
  overrideExisting: false,
});

export const {
  useCreatePmWorkItemMutation,
  useGetPmWorkItemByIdQuery,
  useGetPmWorkItemBoardQuery,
  useGetPmWorkItemCreateMetaQuery,
  useSearchPmWorkItemsQuery,
} = pmWorkItemApi;
