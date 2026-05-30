/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM skill API contract types
 */

export type PMSkillProficiency = 'NOVICE' | 'WORKING' | 'PROFICIENT' | 'EXPERT';

export type PMSkillRequirementType = 'REQUIRED' | 'PREFERRED';

export type PMSkillSource = 'MANUAL' | 'IMPORT' | 'HR_PROFILE' | 'INTEGRATION';

export interface PMSkillApi {
  id: number;
  code: string;
  name: string;
  description?: string | null;
  active?: boolean | null;
}

export interface PMCreateSkillRequest {
  code: string;
  name: string;
  description?: string | null;
}

export interface PMUpdateSkillRequest {
  code: string;
  name: string;
  description?: string | null;
}

export interface PMUserSkillApi {
  id: number;
  skillId: number;
  proficiency: PMSkillProficiency;
  confidence?: number | null;
  source?: PMSkillSource | null;
  verifiedAt?: number | null;
}

export type PMUserSkillsByUserApi = Record<string, PMUserSkillApi[]>;

export interface PMReplaceUserSkillsRequest {
  items: Array<{
    skillId: number;
    proficiency: PMSkillProficiency;
    confidence?: number | null;
    source?: PMSkillSource | null;
    verifiedAt?: number | null;
  }>;
}

export interface PMWorkItemSkillApi {
  id: number;
  skillId: number;
  requirementType: PMSkillRequirementType;
  minProficiency: PMSkillProficiency;
  weight: number;
  source?: PMSkillSource | null;
}

export interface PMReplaceWorkItemSkillsRequest {
  items: Array<{
    skillId: number;
    requirementType: PMSkillRequirementType;
    minProficiency: PMSkillProficiency;
    weight: number;
    source?: PMSkillSource | null;
  }>;
}
