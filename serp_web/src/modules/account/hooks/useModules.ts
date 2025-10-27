/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module access hook
 */

import { useMemo } from 'react';
import { useGetMyModulesQuery } from '../services';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '../store';
import { SYSTEM_ADMIN_ROLES } from '@/shared/types';
import { getModuleRoute } from '@/shared';
import type { ModuleDisplayItem, UserModuleAccess } from '../types';
import { isSuccessResponse } from '@/lib/store';

/**
 * Hook to get user's accessible modules with admin module injection
 */
export const useModules = () => {
  const user = useAppSelector(selectUserProfile);
  const { data: modulesData, isLoading, error } = useGetMyModulesQuery();

  const modules = useMemo((): ModuleDisplayItem[] => {
    if (!user) return [];

    const moduleList: ModuleDisplayItem[] = [];

    // Check if user is system admin
    const isSystemAdmin = user.roles?.some((role) =>
      SYSTEM_ADMIN_ROLES.includes(role)
    );

    // Add Admin module first if user is system admin
    if (isSystemAdmin) {
      moduleList.push({
        code: 'ADMIN',
        name: 'Admin',
        description: 'System Administration',
        href: '/admin',
        isActive: true,
        isAdmin: true,
      });
    }

    // Add user's accessible modules
    if (modulesData && isSuccessResponse(modulesData) && modulesData.data) {
      const userModules = modulesData.data.map(
        (module: UserModuleAccess): ModuleDisplayItem => ({
          code: module.moduleCode,
          name: module.moduleName,
          description: module.moduleDescription || '',
          href: getModuleRoute(module.moduleCode),
          isActive: module.isActive,
          isAdmin: false,
        })
      );

      moduleList.push(...userModules);
    }

    return moduleList;
  }, [user, modulesData]);

  return {
    modules,
    isLoading,
    error,
    hasModules: modules.length > 0,
  };
};
