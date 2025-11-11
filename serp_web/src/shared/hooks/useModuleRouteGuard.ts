/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Route guard hook using module-specific menu APIs
 */

import { useMemo } from 'react';
import { usePathname } from 'next/navigation';
import {
  useGetMenuDisplaysByModuleAndUserQuery,
  useGetMyModulesQuery,
} from '@/modules/account/services';

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

    const currentPath = normalizePath(pathname);

    const hasMatch = menuDisplays.some((menu) => {
      if (!menu.path) return false;

      const menuPath = normalizePath(menu.path);

      if (currentPath === menuPath) return true;

      // Parent path match (e.g., /ptm/tasks/123 matches /ptm/tasks)
      if (currentPath.startsWith(menuPath + '/')) return true;

      return false;
    });

    return hasMatch;
  }, [menuDisplays, pathname]);

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
