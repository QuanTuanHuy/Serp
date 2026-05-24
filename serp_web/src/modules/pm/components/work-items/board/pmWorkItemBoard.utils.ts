/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board helpers
 */

import {
  countActiveFilters,
  parseNumberList,
  serializeNumberList,
} from '../workItemView.utils';

export type BoardFilterCriterion = 'assignee' | 'workType' | 'priority';

export { parseNumberList, serializeNumberList };

export type BoardDragData =
  | {
      type: 'work-item';
      workItemId: number;
      statusId: number;
    }
  | {
      type: 'column';
      statusId: number;
    };

export function getBoardCardDndId(workItemId: number): string {
  return `work-item:${workItemId}`;
}

export function getBoardColumnDndId(statusId: number): string {
  return `status-column:${statusId}`;
}

export function getActiveBoardFilterCount(filters: {
  assigneeIds: number[];
  issueTypeIds: number[];
  priorityIds: number[];
}): number {
  return countActiveFilters([
    filters.assigneeIds.length,
    filters.issueTypeIds.length,
    filters.priorityIds.length,
  ]);
}
