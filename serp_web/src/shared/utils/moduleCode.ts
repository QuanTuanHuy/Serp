/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Module code normalization utilities
 */

const TMS_MODULE_ALIASES = new Set([
  'TMS',
  'FIRSTMILE',
  'SECONDMILE',
  'LASTMILE',
]);

export const normalizeModuleCode = (moduleCode?: string): string => {
  return (moduleCode || '').replace(/[-_]/g, '').toUpperCase();
};

export const toCanonicalModuleCode = (moduleCode?: string): string => {
  const normalizedCode = normalizeModuleCode(moduleCode);

  if (TMS_MODULE_ALIASES.has(normalizedCode)) {
    return 'TMS';
  }

  return normalizedCode;
};

export const isSameModuleCode = (
  leftModuleCode?: string,
  rightModuleCode?: string
): boolean => {
  return (
    toCanonicalModuleCode(leftModuleCode) ===
    toCanonicalModuleCode(rightModuleCode)
  );
};
