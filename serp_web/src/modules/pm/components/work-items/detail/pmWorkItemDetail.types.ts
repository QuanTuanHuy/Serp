/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail view types
 */

import type {
  PMWorkItemBoardCardApi,
  PMWorkItemSearchApi,
} from '../../../types/api';

export type PMWorkItemDetailFallback =
  | PMWorkItemSearchApi
  | PMWorkItemBoardCardApi;

export type ActivityTab = 'comments' | 'history' | 'worklogs';

export type WorkItemDetailModel = {
  id?: number;
  key: string;
  summary: string;
  description?: string | null;
  statusId?: number | null;
  statusName?: string | null;
  issueTypeId?: number | null;
  issueTypeName?: string | null;
  issueTypeIconUrl?: string | null;
  priorityId?: number | null;
  priorityName?: string | null;
  priorityColor?: string | null;
  assigneeId?: number | null;
  assigneeName?: string | null;
  assigneeAvatarUrl?: string | null;
  reporterName?: string | null;
  reporterAvatarUrl?: string | null;
  parentId?: number | null;
  parentKey?: string | null;
  parentSummary?: string | null;
  startDate?: number | string | null;
  dueDate?: number | string | null;
  timeOriginalEstimate?: number | null;
  timeRemainingEstimate?: number | null;
  createdAt?: number | string | null;
  updatedAt?: number | string | null;
  subtaskTotal?: number;
  subtaskDone?: number;
  linkTotal?: number;
  commentTotal?: number;
};

export type DetailQueryState<T> = {
  data?: T;
  error?: unknown;
  isFetching: boolean;
  isLoading: boolean;
};
