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
  PM: '/pm',
  PURCHASE: '/purchase',
  SALES: '/sales',
  SETTINGS: '/settings',
  TMS: '/first-mile',
  SCHOOLBUS: '/school-bus',
};

const TMS_NETWORK_ROUTE_ALIASES: Record<string, string> = {
  '/first-mile/post-offices': '/first-mile/network/post-offices',
  '/first-mile/hubs': '/first-mile/network/hubs',
  '/first-mile/routes': '/first-mile/network/routes',
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
  'pm',
  'ptm',
  'purchase',
  'sales',
  'settings',
  'subscription',
  'tms',
  'school-bus',
]);

export const normalizePath = (path: string): string => {
  const withLeadingSlash = path.startsWith('/') ? path : `/${path}`;
  const compacted = withLeadingSlash.replace(/\/{2,}/g, '/');
  const trimmed = compacted.replace(/\/+$/, '');

  return trimmed || '/';
};

const normalizeTmsNetworkPath = (path: string): string => {
  const alias = Object.entries(TMS_NETWORK_ROUTE_ALIASES).find(
    ([legacyPath]) => path === legacyPath || path.startsWith(`${legacyPath}/`)
  );

  if (!alias) {
    return path;
  }

  const [legacyPath, networkPath] = alias;
  return `${networkPath}${path.slice(legacyPath.length)}`;
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
    return canonicalCode === 'TMS'
      ? normalizeTmsNetworkPath(normalized)
      : normalized;
  }

  if (canonicalCode === 'TMS' && normalized.startsWith('/tms')) {
    const suffix = normalized.slice('/tms'.length);
    return normalizeTmsNetworkPath(normalizePath(`${moduleRootPath}${suffix}`));
  }

  const firstSegment = normalized.split('/').filter(Boolean)[0]?.toLowerCase();

  if (!firstSegment || !KNOWN_APP_ROOT_SEGMENTS.has(firstSegment)) {
    const modulePath = normalizePath(`${moduleRootPath}${normalized}`);
    return canonicalCode === 'TMS'
      ? normalizeTmsNetworkPath(modulePath)
      : modulePath;
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
