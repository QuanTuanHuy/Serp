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

const MODULE_ROOT_PATHS: Record<string, string> = {
  ADMIN: '/admin',
  CRM: '/crm',
  DISCUSSION: '/discuss',
  LOGISTICS: '/logistics',
  LOGISTICS2: '/logistics2',
  PTM: '/ptm',
  PURCHASE: '/purchase',
  SALES: '/sales',
  SETTINGS: '/settings',
  TMS: '/first-mile',
};

const KNOWN_APP_ROOT_SEGMENTS = new Set([
  'admin',
  'apps',
  'auth',
  'crm',
  'discuss',
  'first-mile',
  'home',
  'logistics',
  'logistics2',
  'profile',
  'ptm',
  'purchase',
  'sales',
  'settings',
  'subscription',
  'tms',
]);

export const normalizePath = (path: string): string => {
  const withLeadingSlash = path.startsWith('/') ? path : `/${path}`;
  const compacted = withLeadingSlash.replace(/\/{2,}/g, '/');
  const trimmed = compacted.replace(/\/+$/, '');

  return trimmed || '/';
};

export const getModuleRootPath = (moduleCode?: string): string => {
  const canonicalCode = toCanonicalModuleCode(moduleCode);

  if (!canonicalCode) {
    return '/';
  }

  if (MODULE_ROOT_PATHS[canonicalCode]) {
    return MODULE_ROOT_PATHS[canonicalCode];
  }

  return `/${canonicalCode.toLowerCase().replace(/_/g, '-')}`;
};

export const normalizeMenuPathForModule = (
  menuPath: string | undefined,
  moduleCode?: string
): string => {
  if (!menuPath) {
    return '/';
  }

  const normalized = normalizePath(menuPath.trim());
  const canonicalCode = toCanonicalModuleCode(moduleCode);
  const moduleRootPath = getModuleRootPath(canonicalCode);

  if (
    normalized === moduleRootPath ||
    normalized.startsWith(moduleRootPath + '/')
  ) {
    return normalized;
  }

  if (canonicalCode === 'TMS' && normalized.startsWith('/tms')) {
    const suffix = normalized.slice('/tms'.length);
    return normalizePath(`${moduleRootPath}${suffix}`);
  }

  const firstSegment = normalized.split('/').filter(Boolean)[0]?.toLowerCase();

  if (!firstSegment || !KNOWN_APP_ROOT_SEGMENTS.has(firstSegment)) {
    return normalizePath(`${moduleRootPath}${normalized}`);
  }

  return normalized;
};

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
