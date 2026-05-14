/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Route guard hook using module-specific menu APIs
 */

'use client';

import { useMemo } from 'react';
import { usePathname } from 'next/navigation';
import {
  useGetMenuDisplaysByModuleAndUserQuery,
  useGetMyModulesQuery,
} from '@/modules/account/services';
import {
  getModuleRootPath,
  isSameModuleCode,
  normalizeMenuPathForModule,
  normalizePath,
  toCanonicalModuleCode,
} from '@/shared/utils';

const LOGISTICS2_ROUTE_ALIASES: Record<string, string> = {
  '/logistics2/next-route': '/logistics2/routes',
  '/logistics2/my-routes': '/logistics2/routes',
};

interface RouteGuardResult {
  hasAccess: boolean;

  isLoading: boolean;

  error: any;

  pathname: string;

  currentModule?: {
    moduleId: number;
    moduleCode: string;
    moduleName: string;
  };
}

/**
 * Check if user has access to the current route based on menu permissions
 *
 * This hook uses the existing `/api/v1/menu-displays/get-by-module-and-user` API
 * to verify if user has permission to access the current route.
 *
 * @param moduleCode - Module code (e.g., 'PTM', 'CRM')
 * @returns Route guard result with access status and loading states
 *
 * @example
 * ```tsx
 * const { hasAccess, isLoading } = useModuleRouteGuard('PTM');
 *
 * if (isLoading) return <LoadingSpinner />;
 * if (!hasAccess) return <AccessDenied />;
 * ```
 */
export const useModuleRouteGuard = (moduleCode: string): RouteGuardResult => {
  const pathname = usePathname();
  const moduleRootPath = useMemo(
    () => getModuleRootPath(moduleCode),
    [moduleCode]
  );
  const currentPath = useMemo(() => {
    const normalizedPath = normalizeMenuPathForModule(pathname, moduleCode);

    if (toCanonicalModuleCode(moduleCode) !== 'LOGISTICS2') {
      return normalizedPath;
    }

    return LOGISTICS2_ROUTE_ALIASES[normalizedPath] || normalizedPath;
  }, [pathname, moduleCode]);

  const { data: userModules, isLoading: modulesLoading } =
    useGetMyModulesQuery();

  const currentModule = useMemo(() => {
    return userModules?.find((m) => isSameModuleCode(m.moduleCode, moduleCode));
  }, [userModules, moduleCode]);

  const {
    data: menuDisplays,
    isLoading: menusLoading,
    error,
  } = useGetMenuDisplaysByModuleAndUserQuery(currentModule?.moduleId || 0, {
    skip: !currentModule?.moduleId,
  });

  const hasAccess = useMemo(() => {
    if (!menuDisplays || menuDisplays.length === 0) {
      return false;
    }

    const normalizedModuleRootPath = normalizePath(moduleRootPath);

    const hasMatch = menuDisplays.some((menu) => {
      if (!menu.path) return false;

      const menuPath = normalizeMenuPathForModule(menu.path, moduleCode);

      if (currentPath === menuPath) return true;

      // Parent path match (e.g., /ptm/tasks/123 matches /ptm/tasks)
      if (currentPath.startsWith(menuPath + '/')) return true;

      return false;
    });

    if (hasMatch) {
      return true;
    }

    // Allow module root path (e.g., /sales) if user has any accessible child path
    // (e.g., /sales/dashboard). This prevents root redirects from being blocked.
    if (currentPath === normalizedModuleRootPath) {
      return menuDisplays.some((menu) => {
        if (!menu.path) return false;
        const menuPath = normalizeMenuPathForModule(menu.path, moduleCode);

        if (menuPath === normalizedModuleRootPath) {
          return true;
        }

        return menuPath.startsWith(normalizedModuleRootPath + '/');
      });
    }

    return false;
  }, [currentPath, menuDisplays, moduleCode, moduleRootPath]);

  const isLoading = modulesLoading || menusLoading;

  return {
    hasAccess,
    isLoading,
    error,
    pathname,
    currentModule: currentModule
      ? {
          moduleId: currentModule.moduleId,
          moduleCode: currentModule.moduleCode,
          moduleName: currentModule.moduleName,
        }
      : undefined,
  };
};
