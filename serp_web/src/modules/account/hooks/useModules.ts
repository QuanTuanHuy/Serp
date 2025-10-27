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

export const useModules = () => {
  const user = useAppSelector(selectUserProfile);
  const { data: modulesData, isLoading, error } = useGetMyModulesQuery();

  const modules = useMemo((): ModuleDisplayItem[] => {
    if (!user) return [];

    const moduleList: ModuleDisplayItem[] = [];

    const isSystemAdmin = user.roles?.some((role) =>
      SYSTEM_ADMIN_ROLES.includes(role)
    );

    if (isSystemAdmin) {
      moduleList.push({
        code: 'ADMIN',
        name: 'Admin',
        description: 'System Administration',
        href: '/admin',
        isActive: true,
        isAdmin: true,
      });
      moduleList.push({
        code: 'PTM',
        name: 'PTM',
        description: 'Project and Task Management',
        href: '/ptm',
        isActive: true,
        isAdmin: true,
      });
      moduleList.push({
        code: 'CRM',
        name: 'CRM',
        description: 'Customer Relationship Management',
        href: '/crm',
        isActive: true,
        isAdmin: true,
      });
      return moduleList;
    }

    if (modulesData && modulesData.length > 0) {
      const userModules = modulesData.map(
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
