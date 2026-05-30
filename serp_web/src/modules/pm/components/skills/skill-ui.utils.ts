/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM skill UI helpers
 */

import type {
  PMSkillApi,
  PMSkillProficiency,
  PMSkillRequirementType,
} from '../../types/api';

export const SKILL_PROFICIENCY_OPTIONS: Array<{
  value: PMSkillProficiency;
  label: string;
}> = [
  { value: 'NOVICE', label: 'Novice' },
  { value: 'WORKING', label: 'Working' },
  { value: 'PROFICIENT', label: 'Proficient' },
  { value: 'EXPERT', label: 'Expert' },
];

export const SKILL_REQUIREMENT_OPTIONS: Array<{
  value: PMSkillRequirementType;
  label: string;
}> = [
  { value: 'REQUIRED', label: 'Required' },
  { value: 'PREFERRED', label: 'Preferred' },
];

export function buildSkillMap(skills: PMSkillApi[] | undefined) {
  return new Map((skills ?? []).map((skill) => [skill.id, skill]));
}

export function getSkillLabel(
  skillId: number | string | undefined,
  skillsById: Map<number, PMSkillApi>
) {
  const numericId = Number(skillId);
  const skill = skillsById.get(numericId);
  if (!skill) {
    return numericId ? `Skill #${numericId}` : 'Select skill';
  }
  return `${skill.name} (${skill.code})`;
}

export function getProficiencyLabel(value?: PMSkillProficiency | null) {
  return (
    SKILL_PROFICIENCY_OPTIONS.find((option) => option.value === value)?.label ??
    '-'
  );
}

export function getRequirementLabel(value?: PMSkillRequirementType | null) {
  return (
    SKILL_REQUIREMENT_OPTIONS.find((option) => option.value === value)?.label ??
    '-'
  );
}

export function normalizeOptionalDescription(value: string) {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}
