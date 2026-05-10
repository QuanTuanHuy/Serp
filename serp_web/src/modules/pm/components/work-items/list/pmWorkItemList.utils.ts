/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list helpers
 */

import type { PMWorkItemSearchApi } from '../../../types/api';

export type WorkItemListViewMode = 'list' | 'detail';

export type FilterCriterion =
  | 'parent'
  | 'assignee'
  | 'workType'
  | 'status'
  | 'priority'
  | 'reporter';

export function parseViewMode(value: string | null): WorkItemListViewMode {
  return value === 'detail' ? 'detail' : 'list';
}

export function parseIssueId(value: string | null): number | undefined {
  if (!value) return undefined;
  const issueId = Number(value);
  return Number.isFinite(issueId) ? issueId : undefined;
}

export function parseNumberList(value: string | null): number[] {
  if (!value) return [];
  return value
    .split(',')
    .map((part) => Number(part))
    .filter((item) => Number.isFinite(item));
}

export function serializeNumberList(values: number[]): string | undefined {
  return values.length ? values.join(',') : undefined;
}

export function formatDate(value?: number | null): string {
  if (!value) return 'No date';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'No date';
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
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

export function getWorkItemLabel(item: PMWorkItemSearchApi): string {
  return item.issueTypeName ?? 'Work item';
}
