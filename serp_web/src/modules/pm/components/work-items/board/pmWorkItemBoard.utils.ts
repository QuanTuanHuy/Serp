/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM work item board helpers
 */

export type BoardFilterCriterion = 'assignee' | 'workType' | 'priority';

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

export function getActiveBoardFilterCount(filters: {
  assigneeIds: number[];
  issueTypeIds: number[];
  priorityIds: number[];
}): number {
  return [
    filters.assigneeIds.length,
    filters.issueTypeIds.length,
    filters.priorityIds.length,
  ].reduce((total, item) => total + item, 0);
}
