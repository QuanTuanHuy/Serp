/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM API query parameter helpers
 */

import type {
  PMGetWorkItemBoardParams,
  PMGetWorkItemCalendarParams,
  PMGetWorkItemDependenciesParams,
  PMGetWorkItemTimelineParams,
  PMListProjectsParams,
  PMProjectSummaryFilterParams,
  PMProjectScopedListParams,
  PMSearchWorkItemsParams,
} from '../types/api';

type QueryValue = boolean | number | string | number[] | undefined;
type QueryParams = Record<string, QueryValue>;

const optionalString = (key: string, value?: string): QueryParams =>
  value ? { [key]: value } : {};

const optionalNumber = (key: string, value?: number): QueryParams =>
  typeof value === 'number' ? { [key]: value } : {};

const optionalBoolean = (key: string, value?: boolean): QueryParams =>
  typeof value === 'boolean' ? { [key]: value } : {};

const optionalNumberList = (key: string, values?: number[]): QueryParams =>
  values?.length ? { [key]: values } : {};

export function buildProjectListParams({
  search,
  categoryId,
  projectTypeKey,
  archived,
  page = 0,
  pageSize = 10,
  sortBy,
  sortDirection,
}: PMListProjectsParams = {}): QueryParams {
  return {
    page,
    pageSize,
    ...optionalString('search', search),
    ...optionalNumber('categoryId', categoryId),
    ...optionalString('projectTypeKey', projectTypeKey),
    ...optionalBoolean('archived', archived),
    ...optionalString('sortBy', sortBy),
    ...optionalString('sortDirection', sortDirection),
  };
}

export function buildProjectScopedListParams(
  params?: PMProjectScopedListParams
): QueryParams {
  return {
    ...optionalNumber('projectId', params?.projectId),
    ...optionalString('search', params?.search),
    ...optionalBoolean('isSystem', params?.isSystem),
    page: params?.page ?? 0,
    pageSize: params?.pageSize ?? 20,
    ...optionalString('sortBy', params?.sortBy),
    ...optionalString('sortDirection', params?.sortDirection),
  };
}

export function buildWorkItemSearchParams(
  params?: PMSearchWorkItemsParams
): QueryParams {
  return {
    ...optionalString('keyword', params?.keyword),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('priorityIds', params?.priorityIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('reporterIds', params?.reporterIds),
    ...optionalNumberList('resolutionIds', params?.resolutionIds),
    ...optionalNumber('parentId', params?.parentId),
    ...optionalNumberList('excludeStatusIds', params?.excludeStatusIds),
    ...optionalNumberList('excludeIssueTypeIds', params?.excludeIssueTypeIds),
    ...optionalBoolean('unassigned', params?.unassigned),
    ...optionalBoolean('unresolved', params?.unresolved),
    ...optionalNumber('dueDateFrom', params?.dueDateFrom),
    ...optionalNumber('dueDateTo', params?.dueDateTo),
    ...optionalNumber('createdFrom', params?.createdFrom),
    ...optionalNumber('createdTo', params?.createdTo),
    ...optionalNumber('updatedFrom', params?.updatedFrom),
    ...optionalNumber('updatedTo', params?.updatedTo),
    ...optionalNumberList('sprintIds', params?.sprintIds),
    ...optionalNumberList('componentIds', params?.componentIds),
    ...optionalNumberList('fixVersionIds', params?.fixVersionIds),
    ...optionalBoolean('isOverdue', params?.isOverdue),
    ...optionalBoolean('hasTimeLogged', params?.hasTimeLogged),
    ...optionalBoolean('hasActivePlan', params?.hasActivePlan),
    ...optionalBoolean('enriched', params?.enriched),
    page: params?.page ?? 0,
    pageSize: params?.pageSize ?? 20,
    ...optionalString('sortField', params?.sortField),
    ...optionalString('sortDirection', params?.sortDirection),
  };
}

export function buildWorkItemBoardParams(
  params?: PMGetWorkItemBoardParams
): QueryParams {
  return {
    ...optionalString('keyword', params?.keyword),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalNumberList('priorityIds', params?.priorityIds),
  };
}

export function buildWorkItemTimelineParams(
  params?: PMGetWorkItemTimelineParams
): QueryParams {
  return {
    ...optionalNumber('viewportStart', params?.viewportStart),
    ...optionalNumber('viewportEnd', params?.viewportEnd),
    ...optionalBoolean('includeUnscheduled', params?.includeUnscheduled),
    ...optionalBoolean('includeDependencies', params?.includeDependencies),
    ...optionalNumber('parentId', params?.parentId),
    ...optionalNumber('depth', params?.depth),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalNumberList('priorityIds', params?.priorityIds),
    ...optionalString('keyword', params?.keyword),
    page: params?.page ?? 0,
    pageSize: params?.pageSize ?? 200,
  };
}

export function buildWorkItemDependencyParams(
  params?: PMGetWorkItemDependenciesParams
): QueryParams {
  return {
    ...optionalString('keyword', params?.keyword),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalNumberList('priorityIds', params?.priorityIds),
    ...optionalNumber('parentId', params?.parentId),
    ...optionalNumberList('componentIds', params?.componentIds),
    ...optionalBoolean('includeOutside', params?.includeOutside),
    ...optionalBoolean('includeRelatedLinks', params?.includeRelatedLinks),
    ...optionalNumber('depth', params?.depth),
    page: params?.page ?? 0,
    pageSize: params?.pageSize ?? 100,
  };
}

export function buildWorkItemCalendarParams(
  params?: PMGetWorkItemCalendarParams
): QueryParams {
  return {
    ...optionalNumber('viewportStart', params?.viewportStart),
    ...optionalNumber('viewportEnd', params?.viewportEnd),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalString('keyword', params?.keyword),
    page: params?.page ?? 0,
    pageSize: params?.pageSize ?? 500,
  };
}

export function buildProjectSummaryParams(
  params?: PMProjectSummaryFilterParams
): QueryParams {
  return {
    ...optionalNumberList('assigneeIds', params?.assigneeIds),
    ...optionalNumberList('priorityIds', params?.priorityIds),
    ...optionalNumberList('statusIds', params?.statusIds),
    ...optionalNumberList('issueTypeIds', params?.issueTypeIds),
    ...optionalNumber('parentId', params?.parentId),
    ...optionalNumber('createdFrom', params?.createdFrom),
    ...optionalNumber('createdTo', params?.createdTo),
    ...optionalNumber('updatedFrom', params?.updatedFrom),
    ...optionalNumber('updatedTo', params?.updatedTo),
    ...optionalNumber('dueDateFrom', params?.dueDateFrom),
    ...optionalNumber('dueDateTo', params?.dueDateTo),
    activityPage: params?.activityPage ?? 0,
    activitySize: params?.activitySize ?? 20,
  };
}
