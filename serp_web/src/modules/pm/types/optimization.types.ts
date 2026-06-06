/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization contract types
 */

export type PMOptimizationObjective =
  | 'BALANCED_WORKLOAD'
  | 'MINIMAL_REASSIGNMENT'
  | 'SKILL_FIRST'
  | 'DEADLINE_FIRST';

export type PMOptimizationChangeScope =
  | 'ASSIGNMENT_ONLY'
  | 'SCHEDULE_ONLY'
  | 'ASSIGNMENT_AND_SCHEDULE';

export type PMOptimizationRunStatus =
  | 'GENERATED'
  | 'PARTIALLY_APPLIED'
  | 'APPLIED'
  | 'DISCARDED'
  | 'FAILED'
  | 'EXPIRED';

export type PMOptimizationDecision =
  | 'PENDING'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'OVERRIDDEN';

export type PMOptimizationApplyStatus =
  | 'NOT_APPLIED'
  | 'APPLIED'
  | 'SKIPPED'
  | 'FAILED';

export interface PMOptimizationCandidateSkillFitApi {
  suggestedAssigneeId: number;
  requiredCoveragePercent?: number;
  preferredCoveragePercent?: number;
  matchedRequiredSkills: number[];
  missingRequiredSkills: number[];
  matchedPreferredSkills: number[];
  missingPreferredSkills: number[];
  proficiencySummary?: string;
  confidence?: string;
}

export interface PMOptimizationWorkloadBucketApi {
  assigneeId: number;
  assigneeName?: string | null;
  sourceScope?: string | null;
  plannedMillis?: number | null;
  deductedMillis?: number | null;
  netMillis?: number | null;
}

export interface PMOptimizationRunSummaryApi {
  scopeSize?: number | null;
  assigneeCount?: number | null;
  dependencyCount?: number | null;
  planningStart?: number | null;
  planningEnd?: number | null;
  assignmentSuggestionCount?: number | null;
  scheduledItemCount?: number | null;
  lateItemsBefore?: number | null;
  lateItemsAfter?: number | null;
  overloadedAssigneeCountBefore?: number | null;
  overloadedAssigneeCountAfter?: number | null;
  warningsCount?: number | null;
  confidenceLevel?: string | null;
  capacitySourceMode?: string | null;
  calendarCoverageStatus?: string | null;
  workloadCoverageStatus?: string | null;
  fallbackUserIds?: number[] | null;
  calendarFetchedAt?: number | null;
  workloadFetchedAt?: number | null;
  deductedWorkloadMillis?: number | null;
  sameProjectOutsideScopeDeductedMillis?: number | null;
  crossProjectDeductedMillis?: number | null;
  workloadBuckets?: PMOptimizationWorkloadBucketApi[] | null;
  itemsWithSkillRequirements?: number | null;
  itemsMissingSkillRequirements?: number | null;
  candidatesWithSkillProfiles?: number | null;
  candidatesMissingSkillProfiles?: number | null;
  requiredSkillMismatchCount?: number | null;
  skillRankingConfidence?: string | null;
  selectedCandidateSkillFits?: PMOptimizationCandidateSkillFitApi[] | null;
}

export interface PMOptimizationRunWarningApi {
  id: number;
  workItemId?: number | null;
  severity?: string | null;
  code?: string | null;
  message?: string | null;
  detailsJson?: string | null;
}

export interface PMOptimizationRunItemApi {
  id: number;
  workItemId: number;
  workItemUpdatedAtSnapshot?: number | null;
  planUpdatedAtSnapshot?: number | null;
  currentAssigneeId?: number | null;
  suggestedAssigneeId?: number | null;
  overrideAssigneeId?: number | null;
  currentPlannedStart?: number | null;
  currentPlannedEnd?: number | null;
  suggestedPlannedStart?: number | null;
  suggestedPlannedEnd?: number | null;
  overridePlannedStart?: number | null;
  overridePlannedEnd?: number | null;
  currentDueDate?: number | null;
  assignmentDecision?: PMOptimizationDecision | null;
  scheduleDecision?: PMOptimizationDecision | null;
  assignmentApplyStatus?: PMOptimizationApplyStatus | null;
  scheduleApplyStatus?: PMOptimizationApplyStatus | null;
  score?: string | null;
  cost?: string | null;
  confidence?: string | null;
  candidateSkillFit?: PMOptimizationCandidateSkillFitApi | null;
  assignmentReasons?: string[];
  scheduleReasons?: string[];
  violations?: string[];
  appliedAt?: number | null;
  assignmentSkippedReason?: string | null;
  scheduleSkippedReason?: string | null;
}

export interface PMGenerateOptimizationRunRequest {
  scope?: string;
  algorithmKey?: string;
  objective: PMOptimizationObjective;
  changeScope: PMOptimizationChangeScope;
  planningStart: number;
  planningEnd: number;
  selectedWorkItemIds: number[];
}

export interface PMOptimizationRunDecisionItemRequest {
  workItemId: number;
  assignmentDecision?: PMOptimizationDecision | null;
  scheduleDecision?: PMOptimizationDecision | null;
  overrideAssigneeId?: number | null;
  overridePlannedStart?: number | null;
  overridePlannedEnd?: number | null;
}

export interface PMBatchUpdateOptimizationRunItemDecisionsRequest {
  items: PMOptimizationRunDecisionItemRequest[];
}

export interface PMApplyOptimizationRunRequest {
  applyAssignment: boolean;
  applySchedule: boolean;
  workItemIds: number[];
}

export interface PMOptimizationRunApi {
  id: number;
  tenantId: number;
  projectId: number;
  scope?: string | null;
  objective?: PMOptimizationObjective | string | null;
  changeScope?: PMOptimizationChangeScope | string | null;
  status?: PMOptimizationRunStatus | null;
  planningStart?: number | null;
  planningEnd?: number | null;
  selectedWorkItemCount?: number | null;
  summary?: PMOptimizationRunSummaryApi | null;
  algorithmKey?: string | null;
  algorithmVersion?: string | null;
  solverStatus?: string | null;
  objectiveScore?: string | null;
  createdAt?: number | null;
  createdBy?: number | null;
  updatedAt?: number | null;
  updatedBy?: number | null;
  items: PMOptimizationRunItemApi[];
  warnings: PMOptimizationRunWarningApi[];
}
