/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Permissions business logic orchestration hook
 *
 * NOTE: This hook is temporarily disabled because BE endpoints are not ready:
 * - /api/v1/users/permissions/me (not implemented)
 * - /api/v1/users/menus/me (not implemented)
 *
 * For now, we use module-specific menu APIs in useModuleRouteGuard hook.
 * Once BE implements these endpoints, uncomment the API calls below.
 */

import { useCallback, useMemo } from 'react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '../store';
// import { useGetUserPermissionsQuery, useGetUserMenusQuery } from '../services';
// import { isSuccessResponse } from '@/lib/store';
import type { UserPermissions, MenuAccess, AccessConfig } from '../types';

export const usePermissions = () => {
  const user = useAppSelector(selectUserProfile);

  // TODO: Uncomment when BE implements these endpoints
  // const {
  //   data: permissionsData,
  //   isLoading: permissionsLoading,
  //   refetch: refetchPermissions,
  // } = useGetUserPermissionsQuery(undefined, {
  //   skip: !user?.id,
  //   refetchOnMountOrArgChange: 300,
  // });

  // const {
  //   data: menusData,
  //   isLoading: menusLoading,
  //   refetch: refetchMenus,
  // } = useGetUserMenusQuery(undefined, {
  //   skip: !user?.id,
  //   refetchOnMountOrArgChange: 600,
  // });

  // Temporary: No loading state since APIs are disabled
  const isLoading = false;
  const permissionsData = null;
  const menusData = null;

  // Transform API data to normalized permissions object
  const userPermissions = useMemo<UserPermissions>(() => {
    const roles = user?.roles || [];

    // Temporary: Return empty arrays until BE APIs are ready
    const permissions: string[] = [];
    const menus: MenuAccess[] = [];
    const modules: any[] = [];
    const features: any[] = [];
    const organizationPermissions: any[] = [];

    // TODO: Uncomment when BE implements the endpoints
    // Extract permissions from API response
    // if (
    //   permissionsData &&
    //   isSuccessResponse(permissionsData) &&
    //   permissionsData.data
    // ) {
    //   permissions = permissionsData.data.permissions || [];
    //   features = permissionsData.data.features || [];
    //   organizationPermissions =
    //     permissionsData.data.organizationPermissions || [];
    // }

    // Extract menus from API response
    // if (menusData && isSuccessResponse(menusData) && menusData.data) {
    //   menus = menusData.data.menus || [];
    //   modules = menusData.data.modules || [];
    // }

    return {
      roles,
      permissions,
      menus,
      modules,
      features,
      organizationPermissions,
    };
  }, [user?.roles, permissionsData, menusData]);

  // === ROLE CHECKING ===

  const hasRole = useCallback(
    (role: string): boolean => {
      return userPermissions.roles.includes(role);
    },
    [userPermissions.roles]
  );

  const hasAnyRole = useCallback(
    (roles: string[]): boolean => {
      return roles.some((role) => userPermissions.roles.includes(role));
    },
    [userPermissions.roles]
  );

  const hasAllRoles = useCallback(
    (roles: string[]): boolean => {
      return roles.every((role) => userPermissions.roles.includes(role));
    },
    [userPermissions.roles]
  );

  // === PERMISSION CHECKING ===

  const hasPermission = useCallback(
    (permission: string): boolean => {
      return userPermissions.permissions.includes(permission);
    },
    [userPermissions.permissions]
  );

  const hasAnyPermission = useCallback(
    (permissions: string[]): boolean => {
      return permissions.some((permission) =>
        userPermissions.permissions.includes(permission)
      );
    },
    [userPermissions.permissions]
  );

  const hasAllPermissions = useCallback(
    (permissions: string[]): boolean => {
      return permissions.every((permission) =>
        userPermissions.permissions.includes(permission)
      );
    },
    [userPermissions.permissions]
  );

  // === MENU ACCESS CHECKING ===

  const hasMenuAccess = useCallback(
    (menuKey: string): boolean => {
      const findMenu = (menus: MenuAccess[], key: string): boolean => {
        return menus.some((menu) => {
          if (menu.menuKey === key && menu.isVisible) return true;
          if (menu.children) return findMenu(menu.children, key);
          return false;
        });
      };
      return findMenu(userPermissions.menus, menuKey);
    },
    [userPermissions.menus]
  );

  // === MODULE ACCESS CHECKING ===

  const hasModuleAccess = useCallback(
    (moduleKey: string): boolean => {
      const module = userPermissions.modules.find(
        (m: any) => m.moduleKey === moduleKey
      );
      return module?.isEnabled ?? false;
    },
    [userPermissions.modules]
  );

  // === FEATURE ACCESS CHECKING ===

  const hasFeatureAccess = useCallback(
    (featureKey: string): boolean => {
      const feature = userPermissions.features.find(
        (f: any) => f.featureKey === featureKey
      );
      return feature?.isEnabled ?? false;
    },
    [userPermissions.features]
  );

  // === UTILITY FUNCTIONS ===

  const getAccessibleMenus = useCallback((): MenuAccess[] => {
    const filterMenus = (menus: MenuAccess[]): MenuAccess[] => {
      return menus
        .filter((menu) => menu.isVisible)
        .map((menu) => ({
          ...menu,
          children: menu.children ? filterMenus(menu.children) : undefined,
        }))
        .filter((menu) => !menu.children || menu.children.length > 0);
    };
    return filterMenus(userPermissions.menus);
  }, [userPermissions.menus]);

  // === ALL-IN-ONE ACCESS CHECKER ===

  const canAccess = useCallback(
    (config: AccessConfig): boolean => {
      const {
        roles,
        permissions,
        requireAllRoles = false,
        requireAllPermissions = false,
        menuKey,
        moduleKey,
        featureKey,
      } = config;

      if (roles?.length) {
        const roleCheck = requireAllRoles
          ? hasAllRoles(roles)
          : hasAnyRole(roles);
        if (!roleCheck) return false;
      }

      if (permissions?.length) {
        const permissionCheck = requireAllPermissions
          ? hasAllPermissions(permissions)
          : hasAnyPermission(permissions);
        if (!permissionCheck) return false;
      }

      if (menuKey && !hasMenuAccess(menuKey)) return false;

      if (moduleKey && !hasModuleAccess(moduleKey)) return false;

      if (featureKey && !hasFeatureAccess(featureKey)) return false;

      return true;
    },
    [
      hasAllRoles,
      hasAnyRole,
      hasAllPermissions,
      hasAnyPermission,
      hasMenuAccess,
      hasModuleAccess,
      hasFeatureAccess,
    ]
  );

  return {
    // Core data
    userPermissions,
    isLoading,

    // Role functions
    hasRole,
    hasAnyRole,
    hasAllRoles,

    // Permission functions
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,

    // Module/Menu/Feature functions
    hasMenuAccess,
    hasModuleAccess,
    hasFeatureAccess,

    // Utility functions
    getAccessibleMenus,
    canAccess,

    // Refetch functions (disabled until BE ready)
    refetchPermissions: () => Promise.resolve(),
    refetchMenus: () => Promise.resolve(),
  };
};
