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
