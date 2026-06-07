/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM settings page shared types
 */

import type {
  PMPrioritySchemeSettingsApi,
  PMPrioritySettingsApi,
  PMWorkflowSchemeSettingsApi,
  PMWorkflowSettingsApi,
  PMWorkTypeSchemeSettingsApi,
  PMWorkTypeSettingsApi,
} from '../../types/api';

export type PMSettingsSection =
  | 'work-types'
  | 'work-type-schemes'
  | 'skills'
  | 'resource-calendars'
  | 'workflows'
  | 'workflow-schemes'
  | 'priorities'
  | 'priority-schemes';

export type WorkTypeDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMWorkTypeSettingsApi };

export type SchemeDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMWorkTypeSchemeSettingsApi };

export type PriorityDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMPrioritySettingsApi };

export type PrioritySchemeDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMPrioritySchemeSettingsApi };

export type WorkflowDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMWorkflowSettingsApi };

export type WorkflowSchemeDialogState =
  | { mode: 'create'; item?: undefined }
  | { mode: 'edit'; item: PMWorkflowSchemeSettingsApi };

export type DeleteTarget =
  | { kind: 'work-type'; item: PMWorkTypeSettingsApi }
  | { kind: 'scheme'; item: PMWorkTypeSchemeSettingsApi }
  | { kind: 'priority'; item: PMPrioritySettingsApi }
  | { kind: 'priority-scheme'; item: PMPrioritySchemeSettingsApi }
  | { kind: 'workflow-scheme'; item: PMWorkflowSchemeSettingsApi };

export const SETTINGS_ITEMS: Array<{
  key: PMSettingsSection;
  title: string;
  description: string;
  group: string;
}> = [
  {
    key: 'work-types',
    title: 'Work types',
    description: 'Manage the work item type catalog.',
    group: 'Work types',
  },
  {
    key: 'work-type-schemes',
    title: 'Work type schemes',
    description: 'Control which work types are available to projects.',
    group: 'Work types',
  },
  {
    key: 'skills',
    title: 'Skills',
    description: 'Manage skill catalog data for optimization.',
    group: 'Optimization',
  },
  {
    key: 'resource-calendars',
    title: 'Resource calendars',
    description: 'Configure working calendars and capacity.',
    group: 'Optimization',
  },
  {
    key: 'workflows',
    title: 'Workflows',
    description: 'Manage status paths and workflow lifecycle.',
    group: 'Workflows',
  },
  {
    key: 'workflow-schemes',
    title: 'Workflow schemes',
    description: 'Map work types to workflows for each project.',
    group: 'Workflows',
  },
  {
    key: 'priorities',
    title: 'Priorities',
    description: 'Manage priority order, colors, and labels.',
    group: 'Priorities',
  },
  {
    key: 'priority-schemes',
    title: 'Priority schemes',
    description: 'Associate priority sets with project spaces.',
    group: 'Priorities',
  },
];

export const SETTINGS_GROUPS = [
  'Work types',
  'Optimization',
  'Workflows',
  'Priorities',
];

export const HIERARCHY_OPTIONS = [
  { value: 0, label: '0 - Sub-task level' },
  { value: 1, label: '1 - Standard work item' },
  { value: 2, label: '2 - Epic level' },
];

export function includesText(value: string | null | undefined, needle: string) {
  return value?.toLowerCase().includes(needle) ?? false;
}

export function formatCount(value?: number | null) {
  return typeof value === 'number' ? value.toString() : '-';
}

export function normalizeOptionalText(value: string) {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

export function orderedSelectedWorkTypeIds(
  workTypes: PMWorkTypeSettingsApi[],
  selectedIds: number[]
) {
  const selected = new Set(selectedIds);
  return workTypes
    .filter((workType) => selected.has(workType.id))
    .map((workType) => workType.id);
}

export function orderedSelectedPriorityIds(
  priorities: PMPrioritySettingsApi[],
  selectedIds: number[]
) {
  const selected = new Set(selectedIds);
  return priorities
    .filter((priority) => selected.has(priority.id))
    .map((priority) => priority.id);
}

export function orderedWorkflowSchemeItems(
  workTypes: PMWorkTypeSettingsApi[],
  workflowByWorkTypeId: Record<number, number | undefined>
) {
  return workTypes
    .map((workType) => ({
      issueTypeId: workType.id,
      workflowId: workflowByWorkTypeId[workType.id],
    }))
    .filter(
      (item): item is { issueTypeId: number; workflowId: number } =>
        typeof item.workflowId === 'number'
    );
}
