/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM optimization frontend constants
 */

import type {
  PMOptimizationChangeScope,
  PMOptimizationObjective,
} from '../types/api';

export const PM_OPTIMIZATION_DEFAULT_ALGORITHM_KEY = 'greedy-balanced';

export const PM_OPTIMIZATION_OBJECTIVE_ALGORITHM_MAP: Record<
  PMOptimizationObjective,
  string
> = {
  BALANCED_WORKLOAD: 'greedy-balanced',
  MINIMAL_REASSIGNMENT: 'greedy-minimal-reassignment',
  SKILL_FIRST: 'greedy-skill-first',
  DEADLINE_FIRST: 'greedy-deadline-first',
};

export const PM_OPTIMIZATION_ALGORITHM_OPTIONS = [
  {
    value: 'greedy-balanced',
    label: 'Greedy balanced',
    description: 'Default balanced assignment and schedule heuristic.',
  },
  {
    value: 'greedy-skill-first',
    label: 'Greedy skill first',
    description: 'Bias assignment toward skill fit.',
  },
  {
    value: 'greedy-deadline-first',
    label: 'Greedy deadline first',
    description: 'Prioritize earlier due dates when scheduling ready work.',
  },
  {
    value: 'greedy-minimal-reassignment',
    label: 'Greedy minimal reassignment',
    description: 'Prefer current assignees unless reassignment clearly wins.',
  },
] as const;

export function getPmOptimizationAlgorithmLabel(value?: string | null) {
  if (!value) return '-';

  return (
    PM_OPTIMIZATION_ALGORITHM_OPTIONS.find((option) => option.value === value)
      ?.label || value
  );
}

export function getPmOptimizationAlgorithmKeyForObjective(
  objective: PMOptimizationObjective
) {
  return PM_OPTIMIZATION_OBJECTIVE_ALGORITHM_MAP[objective];
}

export type PMOptimizationObjectiveOption = {
  value: PMOptimizationObjective;
  label: string;
  description: string;
};

export type PMOptimizationChangeScopeOption = {
  value: PMOptimizationChangeScope;
  label: string;
  description: string;
};

export const PM_OPTIMIZATION_OBJECTIVE_OPTIONS: PMOptimizationObjectiveOption[] =
  [
    {
      value: 'BALANCED_WORKLOAD',
      label: 'Balanced workload',
      description: 'Spread work evenly.',
    },
    {
      value: 'MINIMAL_REASSIGNMENT',
      label: 'Minimal reassignment',
      description: 'Prefer current assignees.',
    },
    {
      value: 'SKILL_FIRST',
      label: 'Skill first',
      description: 'Prioritize skill fit.',
    },
    {
      value: 'DEADLINE_FIRST',
      label: 'Deadline first',
      description: 'Prefer late-risk reduction.',
    },
  ];

export const PM_OPTIMIZATION_CHANGE_SCOPE_OPTIONS: PMOptimizationChangeScopeOption[] =
  [
    {
      value: 'ASSIGNMENT_ONLY',
      label: 'Assignment only',
      description: 'Only assignees may change.',
    },
    {
      value: 'SCHEDULE_ONLY',
      label: 'Schedule only',
      description: 'Only planned dates may change.',
    },
    {
      value: 'ASSIGNMENT_AND_SCHEDULE',
      label: 'Assignment and schedule',
      description: 'Both channels may change.',
    },
  ];
