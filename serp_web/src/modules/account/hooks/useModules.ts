/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module access hook
 */

import { useMemo } from 'react';
import { useAppSelector } from '@/shared/hooks';
import {
  DEFAULT_ADMIN_MODULES,
  DEFAULT_ORG_ADMIN_MODULES,
  DEFAULT_ORG_USER_MODULES,
} from '@/shared/constants/adminModules';
import { getModuleRoute } from '@/shared/constants/moduleIcons';
import {
  ORGANIZATION_ADMIN_ROLES,
  ORGANIZATION_USER_ROLES,
  SYSTEM_ADMIN_ROLES,
} from '@/shared/types/role.types';
import { useGetMyModulesQuery } from '../services/moduleApi';
import { selectIsAuthenticated } from '../store/authSlice';
import { selectToken } from '../store/authSlice';
import { selectUserProfile } from '../store/userSlice';
import type { ModuleDisplayItem, UserModuleAccess } from '../types/module';

function hasAnyRole(userRoles: string[] | undefined, allowedRoles: string[]) {
  return allowedRoles.some((role) => userRoles?.includes(role));
}

function canAccessReservedModule({
  moduleCode,
  isSystemAdmin,
  isOrgAdmin,
}: {
  moduleCode: string;
  isSystemAdmin: boolean;
  isOrgAdmin: boolean;
}) {
  if (moduleCode === 'ADMIN') return isSystemAdmin;
  if (moduleCode === 'SETTINGS') return isOrgAdmin;

  return true;
}

export const useModules = () => {
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const token = useAppSelector(selectToken);
  const user = useAppSelector(selectUserProfile);
  const shouldFetchModules = isAuthenticated && Boolean(token);

  const {
    data: modulesData,
    isLoading,
    error,
  } = useGetMyModulesQuery(undefined, {
    skip: !shouldFetchModules,
  });

  const roleFlags = useMemo(
    () => ({
      isSystemAdmin: hasAnyRole(user?.roles, SYSTEM_ADMIN_ROLES),
      isOrgAdmin: hasAnyRole(user?.roles, ORGANIZATION_ADMIN_ROLES),
      isOrgUser: hasAnyRole(user?.roles, ORGANIZATION_USER_ROLES),
    }),
    [user?.roles]
  );

  const modules = useMemo((): ModuleDisplayItem[] => {
    const moduleMap = new Map<string, ModuleDisplayItem>();

    const addModules = (items: ModuleDisplayItem[]) => {
      items.forEach((item) => {
        if (!moduleMap.has(item.code)) {
          moduleMap.set(item.code, item);
        }
      });
    };

    if (roleFlags.isSystemAdmin) {
      addModules(DEFAULT_ADMIN_MODULES);
    }

    if (roleFlags.isOrgAdmin) {
      addModules(DEFAULT_ORG_ADMIN_MODULES);
    }

    if (roleFlags.isOrgUser) {
      addModules(DEFAULT_ORG_USER_MODULES);
    }

    if (modulesData && modulesData.length > 0) {
      const userModules = modulesData
        .filter((module: UserModuleAccess) =>
          canAccessReservedModule({
            moduleCode: module.moduleCode,
            isSystemAdmin: roleFlags.isSystemAdmin,
            isOrgAdmin: roleFlags.isOrgAdmin,
          })
        )
        .map(
          (module: UserModuleAccess): ModuleDisplayItem => ({
            code: module.moduleCode,
            name: module.moduleName,
            description: module.moduleDescription || '',
            href: getModuleRoute(module.moduleCode),
            isActive: module.isActive,
            isAdmin:
              module.moduleCode === 'ADMIN' || module.moduleCode === 'SETTINGS',
          })
        );

      addModules(userModules);
    }

    return Array.from(moduleMap.values());
  }, [
    modulesData,
    roleFlags.isOrgAdmin,
    roleFlags.isOrgUser,
    roleFlags.isSystemAdmin,
    user,
  ]);

  return {
    modules,
    isLoading: shouldFetchModules ? isLoading : false,
    error,
    hasModules: modules.length > 0,
  };
};
