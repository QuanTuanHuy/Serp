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
  PMGetWorkItemTimelineParams,
  PMIssueTypeApi,
  PMPriorityApi,
  PMProjectScopedListParams,
  PMSearchWorkItemsParams,
  PMStatusApi,
  PMUpdateWorkItemRequest,
  PMUpdateWorkItemResponse,
  PMWorkItemActivityApi,
  PMWorkItemBoardResponse,
  PMWorkItemChildApi,
  PMWorkItemCommentApi,
  PMWorkItemCreateMetaResponse,
  PMWorkItemDetailApi,
  PMWorkItemLinkApi,
  PMWorkItemTimelineResponse,
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
      invalidatesTags: [{ type: 'pm/WorkItem', id: 'LIST' }],
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
      providesTags: (result) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'pm/WorkItem' as const,
                id,
              })),
              { type: 'pm/WorkItem', id: 'LIST' },
            ]
          : [{ type: 'pm/WorkItem', id: 'LIST' }],
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
      providesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItem', id: workItemId },
      ],
    }),

    updatePmWorkItem: builder.mutation<
      PMUpdateWorkItemResponse,
      {
        projectId: number;
        workItemId: number;
        body: PMUpdateWorkItemRequest;
      }
    >({
      query: ({ projectId, workItemId, body }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}`,
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMUpdateWorkItemResponse>(),
      invalidatesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItem', id: workItemId },
        { type: 'pm/WorkItem', id: 'LIST' },
        { type: 'pm/WorkItemActivities', id: workItemId },
      ],
    }),

    getPmWorkItemChildren: builder.query<
      PMWorkItemChildApi[],
      { projectId: number; workItemId: number }
    >({
      query: ({ projectId, workItemId }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/children`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemChildApi[]>(),
      providesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItemChildren', id: workItemId },
      ],
    }),

    getPmWorkItemLinks: builder.query<
      PMWorkItemLinkApi[],
      { projectId: number; workItemId: number }
    >({
      query: ({ projectId, workItemId }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/links`,
        method: 'GET',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemLinkApi[]>(),
      providesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItemLinks', id: workItemId },
      ],
    }),

    getPmWorkItemComments: builder.query<
      PaginatedResponse<PMWorkItemCommentApi>,
      { projectId: number; workItemId: number; page?: number; size?: number }
    >({
      query: ({ projectId, workItemId, page = 0, size = 20 }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/comments`,
        method: 'GET',
        params: { page, size },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMWorkItemCommentApi>(),
      providesTags: (result, _error, { workItemId }) =>
        result?.data.items
          ? [
              ...result.data.items.map(({ id }) => ({
                type: 'pm/WorkItemComments' as const,
                id,
              })),
              { type: 'pm/WorkItemComments', id: workItemId },
            ]
          : [{ type: 'pm/WorkItemComments', id: workItemId }],
    }),

    getPmWorkItemActivities: builder.query<
      PaginatedResponse<PMWorkItemActivityApi>,
      {
        projectId: number;
        workItemId: number;
        page?: number;
        size?: number;
        type?: 'ALL' | 'COMMENT' | 'HISTORY';
      }
    >({
      query: ({
        projectId,
        workItemId,
        page = 0,
        size = 20,
        type = 'ALL',
      }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/activities`,
        method: 'GET',
        params: { page, size, type },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMWorkItemActivityApi>(),
      providesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItemActivities', id: workItemId },
      ],
    }),

    createPmWorkItemComment: builder.mutation<
      PMWorkItemCommentApi,
      { projectId: number; workItemId: number; body: string }
    >({
      query: ({ projectId, workItemId, body }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/comments`,
        method: 'POST',
        body: { body },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemCommentApi>(),
      invalidatesTags: (_result, _error, { workItemId }) => [
        { type: 'pm/WorkItem', id: workItemId },
        { type: 'pm/WorkItemComments', id: workItemId },
        { type: 'pm/WorkItemActivities', id: workItemId },
      ],
    }),

    updatePmWorkItemComment: builder.mutation<
      PMWorkItemCommentApi,
      {
        projectId: number;
        workItemId: number;
        commentId: number;
        body: string;
      }
    >({
      query: ({ projectId, workItemId, commentId, body }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/comments/${commentId}`,
        method: 'PUT',
        body: { body },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemCommentApi>(),
      invalidatesTags: (_result, _error, { workItemId, commentId }) => [
        { type: 'pm/WorkItem', id: workItemId },
        { type: 'pm/WorkItemComments', id: workItemId },
        { type: 'pm/WorkItemComments', id: commentId },
        { type: 'pm/WorkItemActivities', id: workItemId },
      ],
    }),

    deletePmWorkItemComment: builder.mutation<
      void,
      { projectId: number; workItemId: number; commentId: number }
    >({
      query: ({ projectId, workItemId, commentId }) => ({
        url: `/projects/${projectId}/work-items/${workItemId}/comments/${commentId}`,
        method: 'DELETE',
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<void>(),
      invalidatesTags: (_result, _error, { workItemId, commentId }) => [
        { type: 'pm/WorkItem', id: workItemId },
        { type: 'pm/WorkItemComments', id: workItemId },
        { type: 'pm/WorkItemComments', id: commentId },
        { type: 'pm/WorkItemActivities', id: workItemId },
      ],
    }),

    getPmStatuses: builder.query<
      PaginatedResponse<PMStatusApi>,
      PMProjectScopedListParams | void
    >({
      query: (params) => ({
        url: '/statuses',
        method: 'GET',
        params: {
          ...(params?.projectId ? { projectId: params.projectId } : {}),
          ...(params?.search ? { search: params.search } : {}),
          page: params?.page ?? 0,
          pageSize: params?.pageSize ?? 20,
          ...(params?.sortBy ? { sortBy: params.sortBy } : {}),
          ...(params?.sortDirection
            ? { sortDirection: params.sortDirection }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMStatusApi>(),
    }),

    getPmPriorities: builder.query<
      PaginatedResponse<PMPriorityApi>,
      PMProjectScopedListParams | void
    >({
      query: (params) => ({
        url: '/priorities',
        method: 'GET',
        params: {
          ...(params?.projectId ? { projectId: params.projectId } : {}),
          ...(params?.search ? { search: params.search } : {}),
          page: params?.page ?? 0,
          pageSize: params?.pageSize ?? 20,
          ...(params?.sortBy ? { sortBy: params.sortBy } : {}),
          ...(params?.sortDirection
            ? { sortDirection: params.sortDirection }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMPriorityApi>(),
    }),

    getPmIssueTypes: builder.query<
      PaginatedResponse<PMIssueTypeApi>,
      PMProjectScopedListParams | void
    >({
      query: (params) => ({
        url: '/issue-types',
        method: 'GET',
        params: {
          ...(params?.projectId ? { projectId: params.projectId } : {}),
          ...(params?.search ? { search: params.search } : {}),
          page: params?.page ?? 0,
          pageSize: params?.pageSize ?? 20,
          ...(params?.sortBy ? { sortBy: params.sortBy } : {}),
          ...(params?.sortDirection
            ? { sortDirection: params.sortDirection }
            : {}),
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createPaginatedTransform<PMIssueTypeApi>(),
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
      providesTags: (result) => {
        const items =
          result?.columns.flatMap((column) => column.items || []) || [];
        return items.length
          ? [
              ...items.map(({ id }) => ({
                type: 'pm/WorkItem' as const,
                id,
              })),
              { type: 'pm/WorkItem', id: 'LIST' },
            ]
          : [{ type: 'pm/WorkItem', id: 'LIST' }];
      },
    }),

    getPmWorkItemTimeline: builder.query<
      PMWorkItemTimelineResponse,
      { projectId: number; params?: PMGetWorkItemTimelineParams }
    >({
      query: ({ projectId, params }) => ({
        url: `/projects/${projectId}/timeline/work-items`,
        method: 'GET',
        params: {
          ...(typeof params?.viewportStart === 'number'
            ? { viewportStart: params.viewportStart }
            : {}),
          ...(typeof params?.viewportEnd === 'number'
            ? { viewportEnd: params.viewportEnd }
            : {}),
          ...(typeof params?.includeUnscheduled === 'boolean'
            ? { includeUnscheduled: params.includeUnscheduled }
            : {}),
          ...(typeof params?.includeDependencies === 'boolean'
            ? { includeDependencies: params.includeDependencies }
            : {}),
          ...(typeof params?.parentId === 'number'
            ? { parentId: params.parentId }
            : {}),
          ...(typeof params?.depth === 'number' ? { depth: params.depth } : {}),
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
          ...(params?.keyword ? { keyword: params.keyword } : {}),
          page: params?.page ?? 0,
          pageSize: params?.pageSize ?? 200,
        },
      }),
      extraOptions: { service: 'pm' },
      transformResponse: createDataTransform<PMWorkItemTimelineResponse>(),
      providesTags: (result) =>
        result?.items.length
          ? [
              ...result.items.map(({ id }) => ({
                type: 'pm/WorkItem' as const,
                id,
              })),
              { type: 'pm/WorkItem', id: 'LIST' },
            ]
          : [{ type: 'pm/WorkItem', id: 'LIST' }],
    }),
  }),
  overrideExisting: false,
});

export const {
  useCreatePmWorkItemCommentMutation,
  useCreatePmWorkItemMutation,
  useDeletePmWorkItemCommentMutation,
  useGetPmWorkItemActivitiesQuery,
  useGetPmWorkItemByIdQuery,
  useGetPmWorkItemBoardQuery,
  useGetPmWorkItemChildrenQuery,
  useGetPmWorkItemCommentsQuery,
  useGetPmWorkItemCreateMetaQuery,
  useGetPmWorkItemLinksQuery,
  useGetPmIssueTypesQuery,
  useGetPmPrioritiesQuery,
  useGetPmStatusesQuery,
  useGetPmWorkItemTimelineQuery,
  useSearchPmWorkItemsQuery,
  useUpdatePmWorkItemCommentMutation,
  useUpdatePmWorkItemMutation,
} = pmWorkItemApi;
