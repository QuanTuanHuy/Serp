/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project create flow types
 */

import type { PMProjectTemplateType } from './project-list.types';

export type PMProjectVisibility = 'PRIVATE' | 'TEAM' | 'ORGANIZATION';

export interface PMProjectVisibilityOption {
  value: PMProjectVisibility;
  label: string;
  description: string;
}

export interface PMProjectTemplateDefinition {
  type: PMProjectTemplateType;
  title: string;
  description: string;
  usageHint: string;
  boardBehavior: string;
  workflowSummary: string;
  presetSummary: string[];
}

export interface PMProjectBlueprintOption extends PMProjectTemplateDefinition {
  blueprintId: number;
  blueprintName: string;
  avatarUrl?: string;
}

export interface PMProjectCreateLeadOption {
  id: string;
  name: string;
  email?: string;
  avatarUrl?: string;
}

export interface PMProjectCreateCategoryOption {
  id: string;
  name: string;
  description?: string;
}
