/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Module sidebar hook for dynamic menu loading
 */

import { useMemo } from 'react';
import {
  useGetMenuDisplaysByModuleAndUserQuery,
  useGetMyModulesQuery,
} from '@/modules/account/services';
import type { MenuDisplayDetail } from '@/modules/admin/types';

export interface SidebarMenuItem {
  id: number;
  name: string;
  href: string;
  icon?: string;
  order: number;
  children?: SidebarMenuItem[];
  level: number;
}

/**
 * Build tree structure from flat menu display list (max 2 levels)
 */
const buildMenuTree = (
  menuDisplays: MenuDisplayDetail[]
): SidebarMenuItem[] => {
  const menuMap = new Map<number, SidebarMenuItem>();
  const rootMenus: SidebarMenuItem[] = [];

  menuDisplays.forEach((menu) => {
    if (!menu.id) return;

    menuMap.set(menu.id, {
      id: menu.id,
      name: menu.name,
      href: menu.path || '/',
      icon: menu.icon,
      order: menu.order,
      children: [],
      level: 0,
    });
  });

  menuDisplays.forEach((menu) => {
    if (!menu.id) return;

    const node = menuMap.get(menu.id)!;

    if (menu.parentId && menuMap.has(menu.parentId)) {
      const parent = menuMap.get(menu.parentId)!;

      if (parent.level === 0) {
        node.level = 1;
        parent.children = parent.children || [];
        parent.children.push(node);
      } else {
        // If parent is already level 1, treat this as root
        rootMenus.push(node);
      }
    } else {
      rootMenus.push(node);
    }
  });

  const sortByOrder = (items: SidebarMenuItem[]) => {
    items.sort((a, b) => a.order - b.order);
    items.forEach((item) => {
      if (item.children && item.children.length > 0) {
        sortByOrder(item.children);
      }
    });
  };

  sortByOrder(rootMenus);

  return rootMenus;
};

/**
 * Hook to load and manage sidebar menus for a module
 *
 * @param moduleCode - Module code (e.g., 'PTM', 'CRM')
 * @returns Sidebar menu data and loading states
 */
export const useModuleSidebar = (moduleCode: string) => {
  // Get all user modules to find the moduleId
  const { data: userModules, isLoading: modulesLoading } =
    useGetMyModulesQuery();

  const currentModule = useMemo(() => {
    return userModules?.find((m) => m.moduleCode === moduleCode);
  }, [userModules, moduleCode]);

  // Get menu displays for this module and current user
  const {
    data: menuDisplaysData,
    isLoading: menusLoading,
    error: menusError,
    refetch,
  } = useGetMenuDisplaysByModuleAndUserQuery(currentModule?.moduleId || 0, {
    skip: !currentModule?.moduleId,
  });

  const menuItems = useMemo(() => {
    if (!menuDisplaysData || menuDisplaysData.length === 0) {
      return [];
    }
    return buildMenuTree(menuDisplaysData);
  }, [menuDisplaysData]);

  const isLoading = modulesLoading || menusLoading;

  return {
    // Data
    menuItems,
    currentModule,

    // States
    isLoading,
    error: menusError,
    hasMenus: menuItems.length > 0,

    // Actions
    refetch,
  };
};
