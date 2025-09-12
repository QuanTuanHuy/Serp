/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Simple and efficient permissions hook
 */

import { useCallback, useMemo } from 'react';
import { useAppSelector } from '@/shared/hooks';
import { selectUser } from '../store';
import { useGetUserPermissionsQuery, useGetUserMenusQuery } from '../services';
import type { UserPermissions, MenuAccess, AccessConfig } from '../types';

export const usePermissions = () => {
  const user = useAppSelector(selectUser);

  const {
    data: permissionsData,
    isLoading: permissionsLoading,
    refetch: refetchPermissions,
  } = useGetUserPermissionsQuery(undefined, {
    skip: !user?.id,
    refetchOnMountOrArgChange: 300,
  });

  const {
    data: menusData,
    isLoading: menusLoading,
    refetch: refetchMenus,
  } = useGetUserMenusQuery(undefined, {
    skip: !user?.id,
    refetchOnMountOrArgChange: 600,
  });

  const isLoading = permissionsLoading || menusLoading;

  const userPermissions = useMemo<UserPermissions>(() => {
    return {
      roles: permissionsData?.data?.roles || user?.roles || [],
      permissions: permissionsData?.data?.permissions || [],
      menus: menusData?.data?.menus || [],
      features: permissionsData?.data?.features || [],
      organizationPermissions:
        permissionsData?.data?.organizationPermissions || [],
    };
  }, [permissionsData, menusData, user?.roles]);

  // Role checking
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

  // Permission checking
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

  // Menu access checking
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

  // Feature access checking
  const hasFeatureAccess = useCallback(
    (featureKey: string): boolean => {
      const feature = userPermissions.features.find(
        (f) => f.featureKey === featureKey
      );
      return feature?.isEnabled ?? false;
    },
    [userPermissions.features]
  );

  // Get accessible menus for navigation
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

  // All-in-one access checker
  const canAccess = useCallback(
    (config: AccessConfig): boolean => {
      const {
        roles,
        permissions,
        requireAllRoles = false,
        requireAllPermissions = false,
        menuKey,
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

      if (featureKey && !hasFeatureAccess(featureKey)) return false;

      return true;
    },
    [
      hasAllRoles,
      hasAnyRole,
      hasAllPermissions,
      hasAnyPermission,
      hasMenuAccess,
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

    // Menu/Feature functions
    hasMenuAccess,
    hasFeatureAccess,

    // Utility functions
    getAccessibleMenus,
    canAccess,

    // Refetch functions
    refetchPermissions,
    refetchMenus,
  };
};
