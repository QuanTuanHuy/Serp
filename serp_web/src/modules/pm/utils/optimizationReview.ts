/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization review helpers
 */

import type {
  PMOptimizationDecision,
  PMOptimizationRunDecisionItemRequest,
  PMOptimizationRunItemApi,
  PMOptimizationScheduleAllocationApi,
} from '../types/api';

export type PMOptimizationReviewChangeScope = {
  canEditAssignment: boolean;
  canEditSchedule: boolean;
};

export type PMOptimizationScheduleRange = {
  start?: number | null;
  end?: number | null;
};

export function getEffectiveAssigneeId(item: PMOptimizationRunItemApi) {
  if (
    item.assignmentDecision === 'OVERRIDDEN' &&
    typeof item.overrideAssigneeId === 'number'
  ) {
    return item.overrideAssigneeId;
  }

  return item.suggestedAssigneeId ?? null;
}

export function getEffectiveAllocationChunks(item: PMOptimizationRunItemApi) {
  if (
    item.scheduleDecision === 'OVERRIDDEN' &&
    item.overrideAllocationChunks?.length
  ) {
    return item.overrideAllocationChunks;
  }

  return item.allocationChunks || [];
}

export function getAllocationRange(
  chunks: PMOptimizationScheduleAllocationApi[]
): PMOptimizationScheduleRange {
  if (!chunks.length) {
    return { start: null, end: null };
  }

  return {
    start: Math.min(...chunks.map((chunk) => chunk.start)),
    end: Math.max(...chunks.map((chunk) => chunk.end)),
  };
}

export function getEffectiveScheduleRange(
  item: PMOptimizationRunItemApi
): PMOptimizationScheduleRange {
  const chunks = getEffectiveAllocationChunks(item);

  if (item.scheduleDecision === 'OVERRIDDEN' && chunks.length) {
    return getAllocationRange(chunks);
  }

  if (
    item.scheduleDecision === 'OVERRIDDEN' &&
    typeof item.overridePlannedStart === 'number' &&
    typeof item.overridePlannedEnd === 'number'
  ) {
    return {
      start: item.overridePlannedStart,
      end: item.overridePlannedEnd,
    };
  }

  return {
    start: item.suggestedPlannedStart,
    end: item.suggestedPlannedEnd,
  };
}

export function hasMeaningfulAssignmentChange(
  item: PMOptimizationRunItemApi,
  canEditAssignment: boolean
) {
  if (!canEditAssignment) {
    return false;
  }

  const targetAssigneeId = getEffectiveAssigneeId(item);
  return (
    typeof targetAssigneeId === 'number' &&
    targetAssigneeId !== item.currentAssigneeId
  );
}

export function hasMeaningfulScheduleChange(
  item: PMOptimizationRunItemApi,
  canEditSchedule: boolean
) {
  if (!canEditSchedule) {
    return false;
  }

  const range = getEffectiveScheduleRange(item);
  const hasValidRange =
    typeof range.start === 'number' &&
    typeof range.end === 'number' &&
    range.start < range.end;

  if (!hasValidRange) {
    return false;
  }

  const rangeChanged =
    range.start !== item.currentPlannedStart ||
    range.end !== item.currentPlannedEnd;

  return rangeChanged || getEffectiveAllocationChunks(item).length > 0;
}

export function buildMeaningfulDecisionItem(
  item: PMOptimizationRunItemApi,
  decision: Extract<PMOptimizationDecision, 'ACCEPTED' | 'REJECTED'>,
  scope: PMOptimizationReviewChangeScope
): PMOptimizationRunDecisionItemRequest | null {
  const updateAssignment = hasMeaningfulAssignmentChange(
    item,
    scope.canEditAssignment
  );
  const updateSchedule = hasMeaningfulScheduleChange(
    item,
    scope.canEditSchedule
  );

  if (!updateAssignment && !updateSchedule) {
    return null;
  }

  return {
    workItemId: item.workItemId,
    assignmentDecision: updateAssignment ? decision : undefined,
    scheduleDecision: updateSchedule ? decision : undefined,
  };
}

export function hasActionableDecision(
  item: PMOptimizationRunItemApi,
  scope: PMOptimizationReviewChangeScope
) {
  const assignmentReady =
    scope.canEditAssignment &&
    (item.assignmentDecision === 'ACCEPTED' ||
      item.assignmentDecision === 'OVERRIDDEN');
  const scheduleReady =
    scope.canEditSchedule &&
    (item.scheduleDecision === 'ACCEPTED' ||
      item.scheduleDecision === 'OVERRIDDEN');

  return assignmentReady || scheduleReady;
}

export function getReadyApplyWorkItemIds(
  items: PMOptimizationRunItemApi[],
  selectedWorkItemIds: number[],
  scope: PMOptimizationReviewChangeScope
) {
  const selected = new Set(selectedWorkItemIds);

  return items
    .filter(
      (item) =>
        selected.has(item.workItemId) && hasActionableDecision(item, scope)
    )
    .map((item) => item.workItemId);
}
