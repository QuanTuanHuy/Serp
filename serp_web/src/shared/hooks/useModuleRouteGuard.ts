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
import { selectUserProfile } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks/redux';
import { FIRST_MILE_ROLES } from '@/shared/types';
import { isSameModuleCode, toCanonicalModuleCode } from '@/shared/utils';

const FIRST_MILE_ROUTE_PREFIX = '/first-mile';
const FIRST_MILE_ROLE_SET = new Set(FIRST_MILE_ROLES);

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
  const user = useAppSelector(selectUserProfile);
  const moduleRootPath = useMemo(() => {
    const [rootSegment] = pathname.split('/').filter(Boolean);
    return rootSegment ? `/${rootSegment}` : '/';
  }, [pathname]);

  const hasFirstMileRoleAccess = useMemo(() => {
    if (!pathname.startsWith(FIRST_MILE_ROUTE_PREFIX)) {
      return false;
    }

    if (toCanonicalModuleCode(moduleCode) !== 'TMS') {
      return false;
    }

    return (user?.roles || []).some((role) => FIRST_MILE_ROLE_SET.has(role));
  }, [pathname, moduleCode, user?.roles]);

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
    // First-mile currently needs role fallback because menu API may not include
    // all paths for TMS roles in some environments.
    if (hasFirstMileRoleAccess) {
      return true;
    }

    if (!menuDisplays || menuDisplays.length === 0) {
      return false;
    }

    const normalizePath = (path: string) => {
      const normalizedPath = path.replace(/\/+$/, '');
      return normalizedPath || '/';
    };

    const currentPath = normalizePath(pathname);

    const hasMatch = menuDisplays.some((menu) => {
      if (!menu.path) return false;

      const menuPath = normalizePath(menu.path);

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
    if (currentPath === normalizePath(moduleRootPath)) {
      return menuDisplays.some((menu) => {
        if (!menu.path) return false;
        const menuPath = normalizePath(menu.path);

        if (menuPath === normalizePath(moduleRootPath)) {
          return true;
        }

        return menuPath.startsWith(normalizePath(moduleRootPath) + '/');
      });
    }

    return false;
  }, [hasFirstMileRoleAccess, menuDisplays, pathname, moduleRootPath]);

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
