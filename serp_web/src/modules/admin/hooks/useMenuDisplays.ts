/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Displays management hook
 */

import { useMemo, useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetAllMenuDisplaysQuery,
  useCreateMenuDisplayMutation,
  useUpdateMenuDisplayMutation,
  useDeleteMenuDisplayMutation,
} from '../services/adminApi';
import {
  setMenuDisplaysFilters,
  setMenuDisplaysSearch,
  setMenuDisplaysModuleFilter,
  setMenuDisplaysMenuTypeFilter,
  clearMenuDisplaysFilters,
  openMenuDisplayCreateDialog,
  openMenuDisplayEditDialog,
  closeMenuDisplayDialog,
  setMenuDisplaysStats,
  toggleMenuDisplayNode,
  expandAllMenuDisplayNodes,
  collapseAllMenuDisplayNodes,
} from '../store';
import type {
  MenuDisplayDetail,
  MenuDisplayTreeNode,
  MenuDisplayFilters,
  MenuDisplayStats,
  CreateMenuDisplayRequest,
  UpdateMenuDisplayRequest,
} from '../types';
import { toast } from 'sonner';
import type { RootState } from '@/lib/store/store';

/**
 * Build tree structure from flat menu display list
 */
const buildMenuTree = (
  menuDisplays: MenuDisplayDetail[]
): MenuDisplayTreeNode[] => {
  const menuMap = new Map<number, MenuDisplayTreeNode>();
  const rootMenus: MenuDisplayTreeNode[] = [];

  // First pass: create all nodes
  menuDisplays.forEach((menu) => {
    menuMap.set(menu.id!, {
      ...menu,
      children: [],
      level: 0,
    });
  });

  // Second pass: build tree structure
  menuDisplays.forEach((menu) => {
    const node = menuMap.get(menu.id!)!;

    if (menu.parentId && menuMap.has(menu.parentId)) {
      const parent = menuMap.get(menu.parentId)!;
      node.level = (parent.level || 0) + 1;
      parent.children = parent.children || [];
      parent.children.push(node);
    } else {
      rootMenus.push(node);
    }
  });

  // Sort children by order
  const sortByOrder = (nodes: MenuDisplayTreeNode[]) => {
    nodes.sort((a, b) => a.order - b.order);
    nodes.forEach((node) => {
      if (node.children && node.children.length > 0) {
        sortByOrder(node.children);
      }
    });
  };

  sortByOrder(rootMenus);

  return rootMenus;
};

/**
 * Calculate statistics from menu displays
 */
const calculateStats = (
  menuDisplays: MenuDisplayDetail[]
): MenuDisplayStats => {
  const stats: MenuDisplayStats = {
    total: menuDisplays.length,
    byModule: {},
    byType: {
      SIDEBAR: 0,
      TOPBAR: 0,
      DROPDOWN: 0,
      ACTION: 0,
    },
    visible: 0,
    hidden: 0,
  };

  menuDisplays.forEach((menu) => {
    // Count by module
    const moduleName = menu.moduleName || 'Unknown';
    stats.byModule[moduleName] = (stats.byModule[moduleName] || 0) + 1;

    // Count by type
    if (menu.menuType) {
      stats.byType[menu.menuType]++;
    }

    // Count visibility
    if (menu.isVisible) {
      stats.visible++;
    } else {
      stats.hidden++;
    }
  });

  return stats;
};

/**
 * Filter menu displays based on current filters
 */
const applyFilters = (
  menuDisplays: MenuDisplayDetail[],
  filters: MenuDisplayFilters
): MenuDisplayDetail[] => {
  return menuDisplays.filter((menu) => {
    // Search filter
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      const matchesName = menu.name?.toLowerCase().includes(searchLower);
      const matchesPath = menu.path?.toLowerCase().includes(searchLower);
      const matchesDescription = menu.description
        ?.toLowerCase()
        .includes(searchLower);
      const matchesModule = menu.moduleName
        ?.toLowerCase()
        .includes(searchLower);

      if (
        !matchesName &&
        !matchesPath &&
        !matchesDescription &&
        !matchesModule
      ) {
        return false;
      }
    }

    // Module filter
    if (filters.moduleId !== undefined && menu.moduleId !== filters.moduleId) {
      return false;
    }

    // Menu type filter
    if (filters.menuType && menu.menuType !== filters.menuType) {
      return false;
    }

    return true;
  });
};

/**
 * Get all node IDs for expand/collapse all functionality
 */
const getAllNodeIds = (nodes: MenuDisplayTreeNode[]): number[] => {
  const ids: number[] = [];

  const traverse = (node: MenuDisplayTreeNode) => {
    if (node.id) {
      ids.push(node.id);
    }
    if (node.children && node.children.length > 0) {
      node.children.forEach(traverse);
    }
  };

  nodes.forEach(traverse);
  return ids;
};

export const useMenuDisplays = () => {
  const dispatch = useAppDispatch();

  // Selectors
  const filters = useAppSelector(
    (state: RootState) => state.admin.menuDisplays.filters
  );
  const isDialogOpen = useAppSelector(
    (state: RootState) => state.admin.menuDisplays.isDialogOpen
  );
  const isCreating = useAppSelector(
    (state: RootState) => state.admin.menuDisplays.isCreating
  );
  const selectedMenuDisplay = useAppSelector(
    (state: RootState) => state.admin.menuDisplays.selectedMenuDisplay
  );
  const stats = useAppSelector(
    (state: RootState) => state.admin.menuDisplays.stats
  );
  const expandedNodes = useAppSelector(
    (state: RootState) => state.admin.menuDisplays.expandedNodes
  );

  // Convert to Set for easier lookup in components
  const expandedNodesSet = useMemo(() => {
    return new Set(expandedNodes);
  }, [expandedNodes]);

  // API hooks
  const {
    data: menuDisplaysResponse,
    isLoading,
    error,
    refetch,
  } = useGetAllMenuDisplaysQuery();
  const [createMenuDisplay, { isLoading: isCreatingMenuDisplay }] =
    useCreateMenuDisplayMutation();
  const [updateMenuDisplay, { isLoading: isUpdatingMenuDisplay }] =
    useUpdateMenuDisplayMutation();
  const [deleteMenuDisplay, { isLoading: isDeletingMenuDisplay }] =
    useDeleteMenuDisplayMutation();

  // Extract items from paginated response
  const menuDisplaysData = menuDisplaysResponse?.data?.items || [];

  // Filtered and tree-structured menu displays
  const filteredMenuDisplays = useMemo(() => {
    if (!menuDisplaysData) return [];
    return applyFilters(menuDisplaysData, filters);
  }, [menuDisplaysData, filters]);

  const menuTree = useMemo(() => {
    return buildMenuTree(filteredMenuDisplays);
  }, [filteredMenuDisplays]);

  // Calculate and update stats
  useMemo(() => {
    if (menuDisplaysData && menuDisplaysData.length > 0) {
      const newStats = calculateStats(menuDisplaysData);
      dispatch(setMenuDisplaysStats(newStats));
    }
  }, [menuDisplaysData, dispatch]);

  // Filter actions
  const handleSearch = useCallback(
    (search: string) => {
      dispatch(setMenuDisplaysSearch(search));
    },
    [dispatch]
  );

  const handleModuleFilter = useCallback(
    (moduleId: number | undefined) => {
      dispatch(setMenuDisplaysModuleFilter(moduleId));
    },
    [dispatch]
  );

  const handleMenuTypeFilter = useCallback(
    (menuType: MenuDisplayFilters['menuType']) => {
      dispatch(setMenuDisplaysMenuTypeFilter(menuType));
    },
    [dispatch]
  );

  const handleClearFilters = useCallback(() => {
    dispatch(clearMenuDisplaysFilters());
  }, [dispatch]);

  // Dialog actions
  const openCreateDialog = useCallback(() => {
    dispatch(openMenuDisplayCreateDialog());
  }, [dispatch]);

  const openEditDialog = useCallback(
    (menuDisplay: MenuDisplayDetail) => {
      dispatch(openMenuDisplayEditDialog(menuDisplay));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(closeMenuDisplayDialog());
  }, [dispatch]);

  // Tree expansion actions
  const toggleNode = useCallback(
    (nodeId: number) => {
      dispatch(toggleMenuDisplayNode(nodeId));
    },
    [dispatch]
  );

  const expandAll = useCallback(() => {
    const allIds = getAllNodeIds(menuTree);
    dispatch(expandAllMenuDisplayNodes(allIds));
  }, [dispatch, menuTree]);

  const collapseAll = useCallback(() => {
    dispatch(collapseAllMenuDisplayNodes());
  }, [dispatch]);

  // CRUD operations
  const handleCreateMenuDisplay = useCallback(
    async (data: CreateMenuDisplayRequest) => {
      try {
        await createMenuDisplay(data).unwrap();
        toast.success('Menu display created successfully');
        closeDialog();
        refetch();
      } catch (error: any) {
        const errorMessage =
          error?.data?.message || 'Failed to create menu display';
        toast.error(errorMessage);
        throw error;
      }
    },
    [createMenuDisplay, closeDialog, refetch]
  );

  const handleUpdateMenuDisplay = useCallback(
    async (id: number, data: UpdateMenuDisplayRequest) => {
      try {
        await updateMenuDisplay({ id, data }).unwrap();
        toast.success('Menu display updated successfully');
        closeDialog();
        refetch();
      } catch (error: any) {
        const errorMessage =
          error?.data?.message || 'Failed to update menu display';
        toast.error(errorMessage);
        throw error;
      }
    },
    [updateMenuDisplay, closeDialog, refetch]
  );

  const handleDeleteMenuDisplay = useCallback(
    async (id: number, name: string) => {
      try {
        await deleteMenuDisplay(id).unwrap();
        toast.success(`Menu display "${name}" deleted successfully`);
        refetch();
      } catch (error: any) {
        const errorMessage =
          error?.data?.message || 'Failed to delete menu display';
        toast.error(errorMessage);
        throw error;
      }
    },
    [deleteMenuDisplay, refetch]
  );

  const submitMenuDisplay = useCallback(
    async (data: CreateMenuDisplayRequest | UpdateMenuDisplayRequest) => {
      if (isCreating) {
        await handleCreateMenuDisplay(data as CreateMenuDisplayRequest);
      } else if (selectedMenuDisplay?.id) {
        await handleUpdateMenuDisplay(
          selectedMenuDisplay.id,
          data as UpdateMenuDisplayRequest
        );
      }
    },
    [
      isCreating,
      selectedMenuDisplay,
      handleCreateMenuDisplay,
      handleUpdateMenuDisplay,
    ]
  );

  return {
    // Data
    menuDisplays: menuDisplaysData || [],
    filteredMenuDisplays,
    menuTree,
    stats,
    expandedNodes: expandedNodesSet,

    // Loading states
    isLoading,
    error,
    isCreatingMenuDisplay,
    isUpdatingMenuDisplay,
    isDeletingMenuDisplay,

    // Dialog state
    isDialogOpen,
    isCreating,
    selectedMenuDisplay,

    // Filters
    filters,
    handleSearch,
    handleModuleFilter,
    handleMenuTypeFilter,
    handleClearFilters,

    // Dialog actions
    openCreateDialog,
    openEditDialog,
    closeDialog,

    // Tree actions
    toggleNode,
    expandAll,
    collapseAll,

    // CRUD actions
    handleCreateMenuDisplay,
    handleUpdateMenuDisplay,
    handleDeleteMenuDisplay,
    submitMenuDisplay,
    refetch,
  };
};
