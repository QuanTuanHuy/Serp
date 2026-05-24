/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item detail helpers
 */

import type { PMWorkItemDetailApi } from '../../../types/api';
import type {
  ActivityTab,
  PMWorkItemDetailFallback,
  WorkItemDetailModel,
} from './pmWorkItemDetail.types';

export function toDetailModel(
  workItemId: number | undefined,
  data?: PMWorkItemDetailApi,
  fallbackItem?: PMWorkItemDetailFallback
): WorkItemDetailModel {
  const searchItem =
    fallbackItem && 'statusName' in fallbackItem ? fallbackItem : undefined;
  const boardItem =
    fallbackItem && 'issueType' in fallbackItem ? fallbackItem : undefined;

  return {
    id: data?.id ?? fallbackItem?.id,
    key: data?.key ?? fallbackItem?.key ?? (workItemId ? `#${workItemId}` : ''),
    summary: data?.summary ?? fallbackItem?.summary ?? 'Work item',
    description: data?.description ?? fallbackItem?.description,
    statusId: data?.status?.id ?? data?.statusId ?? searchItem?.statusId,
    statusName: data?.status?.name ?? searchItem?.statusName,
    issueTypeId:
      data?.issueType?.id ??
      data?.issueTypeId ??
      searchItem?.issueTypeId ??
      boardItem?.issueType?.id,
    issueTypeName:
      data?.issueType?.name ??
      searchItem?.issueTypeName ??
      boardItem?.issueType?.name,
    issueTypeIconUrl:
      data?.issueType?.iconUrl ??
      searchItem?.issueTypeIconUrl ??
      boardItem?.issueType?.iconUrl,
    priorityId:
      data?.priority?.id ??
      data?.priorityId ??
      searchItem?.priorityId ??
      boardItem?.priority?.id,
    priorityName:
      data?.priority?.name ??
      searchItem?.priorityName ??
      boardItem?.priority?.name,
    priorityColor:
      data?.priority?.color ??
      searchItem?.priorityColor ??
      boardItem?.priority?.color,
    assigneeId:
      data?.assignee?.id ?? data?.assigneeId ?? fallbackItem?.assigneeId,
    assigneeName:
      data?.assignee?.displayName ??
      searchItem?.assigneeName ??
      boardItem?.assigneeName,
    assigneeAvatarUrl:
      data?.assignee?.avatarUrl ??
      searchItem?.assigneeAvatarUrl ??
      boardItem?.assigneeAvatarUrl,
    reporterName: data?.reporter?.displayName ?? searchItem?.reporterName,
    reporterAvatarUrl:
      data?.reporter?.avatarUrl ?? searchItem?.reporterAvatarUrl,
    parentId: data?.parent?.id ?? data?.parentId ?? fallbackItem?.parentId,
    parentKey: data?.parent?.key,
    parentSummary: data?.parent?.summary,
    startDate: data?.startDate ?? fallbackItem?.startDate,
    dueDate: data?.dueDate ?? fallbackItem?.dueDate,
    timeOriginalEstimate: data?.timeOriginalEstimate ?? null,
    timeRemainingEstimate: data?.timeRemainingEstimate ?? null,
    createdAt: data?.createdAt ?? searchItem?.createdAt,
    updatedAt: data?.updatedAt ?? searchItem?.updatedAt,
    subtaskTotal: data?.subtaskStats?.total,
    subtaskDone: data?.subtaskStats?.done,
    linkTotal: data?.linkStats?.total,
    commentTotal: data?.commentStats?.total,
  };
}

export function getActivityType(
  tab: ActivityTab
): 'ALL' | 'COMMENT' | 'HISTORY' {
  if (tab === 'comments') return 'COMMENT';
  if (tab === 'history') return 'HISTORY';
  return 'ALL';
}

export function formatDetailDate(value?: number | string | null): string {
  if (!value) return 'None';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'None';
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

export function formatRelativeTime(value?: number | string | null): string {
  if (!value) return 'Unknown';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Unknown';

  const diffMs = Date.now() - date.getTime();
  const diffSeconds = Math.max(0, Math.floor(diffMs / 1000));
  if (diffSeconds < 60) return `${diffSeconds || 1} seconds ago`;

  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `${diffMinutes} minutes ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} hours ago`;

  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays} days ago`;
}

export function getInitials(name?: string | null): string {
  if (!name) return '?';
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');
}

export function toDateInputValue(value?: number | string | null): string {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toISOString().slice(0, 10);
}
