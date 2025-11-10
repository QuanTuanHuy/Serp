/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Displays management hook
 */

'use client';

import { useMemo, useCallback, useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  useGetAllMenuDisplaysQuery,
  useCreateMenuDisplayMutation,
  useUpdateMenuDisplayMutation,
  useDeleteMenuDisplayMutation,
  useAssignMenuDisplaysToRoleMutation,
  useUnassignMenuDisplaysFromRoleMutation,
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
  setMenuDisplaysPage,
  setMenuDisplaysPageSize,
  setMenuDisplaysSort,
  setMenuDisplaysPaginationMeta,
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
import {
  selectExpandedMenuDisplayNodes,
  selectIsCreatingMenuDisplay,
  selectMenuDisplaysDialogOpen,
  selectMenuDisplaysFilters,
  selectMenuDisplaysStats,
  selectSelectedMenuDisplay,
  selectMenuDisplaysPagination,
} from '../store/menu-displays/menuDisplaysSlice';

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

// Note: menuType remains client-side only for now; search/moduleId handled by backend

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

  // Local state for role assignment dialog
  const [roleDialogOpen, setRoleDialogOpen] = useState(false);
  const [selectedMenuForRole, setSelectedMenuForRole] =
    useState<MenuDisplayDetail | null>(null);

  // Local state for details dialog
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedMenuForDetails, setSelectedMenuForDetails] =
    useState<MenuDisplayDetail | null>(null);

  // Selectors
  const filters = useAppSelector(selectMenuDisplaysFilters);
  const isDialogOpen = useAppSelector(selectMenuDisplaysDialogOpen);
  const isCreating = useAppSelector(selectIsCreatingMenuDisplay);
  const selectedMenuDisplay = useAppSelector(selectSelectedMenuDisplay);
  const stats = useAppSelector(selectMenuDisplaysStats);
  const expandedNodes = useAppSelector(selectExpandedMenuDisplayNodes);
  const pagination = useAppSelector(selectMenuDisplaysPagination);

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
  } = useGetAllMenuDisplaysQuery({
    search: filters.search,
    moduleId: filters.moduleId,
    page: pagination.page,
    pageSize: pagination.pageSize,
    sortBy: pagination.sortBy,
    sortDir: pagination.sortDir,
  });
  const [createMenuDisplay, { isLoading: isCreatingMenuDisplay }] =
    useCreateMenuDisplayMutation();
  const [updateMenuDisplay, { isLoading: isUpdatingMenuDisplay }] =
    useUpdateMenuDisplayMutation();
  const [deleteMenuDisplay, { isLoading: isDeletingMenuDisplay }] =
    useDeleteMenuDisplayMutation();
  const [assignMenuDisplaysToRole, { isLoading: isAssigningRole }] =
    useAssignMenuDisplaysToRoleMutation();
  const [unassignMenuDisplaysFromRole, { isLoading: isUnassigningRole }] =
    useUnassignMenuDisplaysFromRoleMutation();

  // Extract items from paginated response
  const menuDisplaysData = menuDisplaysResponse?.data?.items || [];

  // Filtered and tree-structured menu displays
  const menuTree = useMemo(() => {
    // Backend already filters by search/moduleId; we keep optional client-side menuType filter
    const clientFiltered = filters.menuType
      ? menuDisplaysData.filter((m) => m.menuType === filters.menuType)
      : menuDisplaysData;
    return buildMenuTree(clientFiltered);
  }, [menuDisplaysData, filters.menuType]);

  // Calculate stats memoized (no dispatch here to avoid loops)
  const calculatedStats = useMemo(() => {
    const items = menuDisplaysData || [];
    return calculateStats(items);
  }, [menuDisplaysData]);

  // Sync stats to Redux only when stats object actually changes
  useEffect(() => {
    // Only dispatch if stats differ (deep comparison by JSON or manual check)
    if (JSON.stringify(stats) !== JSON.stringify(calculatedStats)) {
      dispatch(setMenuDisplaysStats(calculatedStats));
    }
  }, [calculatedStats, stats, dispatch]);

  // Sync pagination meta from response only when data changes
  useEffect(() => {
    if (menuDisplaysResponse?.data) {
      const { totalItems, totalPages, currentPage } = menuDisplaysResponse.data;

      // Only dispatch if pagination meta actually changed
      if (
        pagination.totalItems !== totalItems ||
        pagination.totalPages !== totalPages ||
        pagination.page !== currentPage
      ) {
        dispatch(
          setMenuDisplaysPaginationMeta({
            totalItems,
            totalPages,
            currentPage,
          })
        );
      }
    }
  }, [
    menuDisplaysResponse?.data,
    pagination.totalItems,
    pagination.totalPages,
    pagination.page,
    dispatch,
  ]);

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

  // Pagination actions
  const handlePageChange = useCallback(
    (page: number) => {
      dispatch(setMenuDisplaysPage(page));
    },
    [dispatch]
  );

  const handlePageSizeChange = useCallback(
    (size: number) => {
      dispatch(setMenuDisplaysPageSize(size));
    },
    [dispatch]
  );

  const handleSortChange = useCallback(
    (sortBy: string, sortDir: 'ASC' | 'DESC') => {
      dispatch(setMenuDisplaysSort({ sortBy, sortDir }));
    },
    [dispatch]
  );

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

  // Role assignment operations
  const handleAssignRole = useCallback(
    async (roleId: number, menuDisplayIds: number[]) => {
      try {
        await assignMenuDisplaysToRole({ roleId, menuDisplayIds }).unwrap();
        toast.success('Role assigned successfully');
        refetch();
      } catch (error: any) {
        const errorMessage = error?.data?.message || 'Failed to assign role';
        toast.error(errorMessage);
        throw error;
      }
    },
    [assignMenuDisplaysToRole, refetch]
  );

  const handleUnassignRole = useCallback(
    async (roleId: number, menuDisplayIds: number[]) => {
      try {
        await unassignMenuDisplaysFromRole({ roleId, menuDisplayIds }).unwrap();
        toast.success('Role unassigned successfully');
        refetch();
      } catch (error: any) {
        const errorMessage = error?.data?.message || 'Failed to unassign role';
        toast.error(errorMessage);
        throw error;
      }
    },
    [unassignMenuDisplaysFromRole, refetch]
  );

  // Role dialog actions
  const openRoleDialog = useCallback((menuDisplay: MenuDisplayDetail) => {
    setSelectedMenuForRole(menuDisplay);
    setRoleDialogOpen(true);
  }, []);

  const closeRoleDialog = useCallback(() => {
    setRoleDialogOpen(false);
    setSelectedMenuForRole(null);
  }, []);

  // Details dialog actions
  const openDetailsDialog = useCallback((menuDisplay: MenuDisplayDetail) => {
    setSelectedMenuForDetails(menuDisplay);
    setDetailsDialogOpen(true);
  }, []);

  const closeDetailsDialog = useCallback(() => {
    setDetailsDialogOpen(false);
    setSelectedMenuForDetails(null);
  }, []);

  return {
    // Data
    menuDisplays: menuDisplaysData || [],
    menuTree,
    stats,
    pagination: {
      currentPage: menuDisplaysResponse?.data?.currentPage || pagination.page,
      totalPages: menuDisplaysResponse?.data?.totalPages || 0,
      totalItems: menuDisplaysResponse?.data?.totalItems || 0,
      pageSize: pagination.pageSize,
      sortBy: pagination.sortBy,
      sortDir: pagination.sortDir,
    },
    expandedNodes: expandedNodesSet,

    // Loading states
    isLoading,
    error,
    isCreatingMenuDisplay,
    isUpdatingMenuDisplay,
    isDeletingMenuDisplay,
    isAssigningRole,
    isUnassigningRole,

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
    handlePageChange,
    handlePageSizeChange,
    handleSortChange,

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
    handleAssignRole,
    handleUnassignRole,
    refetch,

    // Role dialog state & actions
    roleDialogOpen,
    selectedMenuForRole,
    openRoleDialog,
    closeRoleDialog,

    // Details dialog state & actions
    detailsDialogOpen,
    selectedMenuForDetails,
    openDetailsDialog,
    closeDetailsDialog,
  };
};
