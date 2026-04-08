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
import { getModuleRootRoute } from '@/shared/constants/moduleIcons';

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
  const moduleRootPath = getModuleRootRoute(moduleCode);

  const { data: userModules, isLoading: modulesLoading } =
    useGetMyModulesQuery();

  const currentModule = useMemo(() => {
    return userModules?.find((m) => m.moduleCode === moduleCode);
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

    const normalizePath = (path: string) => {
      return path.replace(/\/+$/, ''); // Remove trailing slashes
    };

    const buildEquivalentPaths = (path: string) => {
      const normalizedPath = normalizePath(path);
      const equivalentPaths = new Set([normalizedPath]);

      if (moduleCode === 'SCHOOL_BUS') {
        if (normalizedPath === '/bds' || normalizedPath.startsWith('/bds/')) {
          equivalentPaths.add(normalizedPath.replace(/^\/bds/, '/school-bus'));
        }

        if (
          normalizedPath === '/school-bus' ||
          normalizedPath.startsWith('/school-bus/')
        ) {
          equivalentPaths.add(normalizedPath.replace(/^\/school-bus/, '/bds'));
        }
      }

      return Array.from(equivalentPaths);
    };

    const currentPaths = buildEquivalentPaths(pathname);

    const hasMatch = menuDisplays.some((menu) => {
      if (!menu.path) return false;

      const menuPaths = buildEquivalentPaths(menu.path);

      return currentPaths.some((currentPath) =>
        menuPaths.some((menuPath) => {
          if (currentPath === menuPath) return true;
          if (currentPath.startsWith(menuPath + '/')) return true;
          return false;
        })
      );
    });

    if (hasMatch) {
      return true;
    }

    if (currentPaths.includes(normalizePath(moduleRootPath))) {
      return menuDisplays.some((menu) => {
        if (!menu.path) return false;
        return buildEquivalentPaths(menu.path).some((menuPath) =>
          menuPath.startsWith(normalizePath(moduleRootPath) + '/')
        );
      });
    }

    return false;
  }, [menuDisplays, pathname, moduleRootPath]);

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
