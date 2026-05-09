/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project create flow types
 */

import type { PMProjectTemplateType } from './project-list.types';

export type PMProjectVisibility = 'PRIVATE' | 'TEAM' | 'ORGANIZATION';

export interface PMProjectTemplateDefinition {
  type: PMProjectTemplateType;
  title: string;
  description: string;
  usageHint: string;
  boardBehavior: string;
  workflowSummary: string;
  presetSummary: string[];
}
