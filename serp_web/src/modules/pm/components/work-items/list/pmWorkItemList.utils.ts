/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item list helpers
 */

import type { PMWorkItemSearchApi } from '../../../types/api';
import {
  countActiveFilters,
  formatDate,
  getInitials,
  parseIssueId,
  parseNumberList,
  serializeNumberList,
} from '../workItemView.utils';

export {
  formatDate,
  getInitials,
  parseIssueId,
  parseNumberList,
  serializeNumberList,
};

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

export function getWorkItemLabel(item: PMWorkItemSearchApi): string {
  return item.issueTypeName ?? 'Work item';
}

export function getActiveFilterCount(filters: {
  parentId?: number;
  assigneeIds: number[];
  issueTypeIds: number[];
  statusIds: number[];
  priorityIds: number[];
  reporterIds: number[];
}): number {
  return countActiveFilters([
    filters.parentId,
    filters.assigneeIds.length,
    filters.issueTypeIds.length,
    filters.statusIds.length,
    filters.priorityIds.length,
    filters.reporterIds.length,
  ]);
}
