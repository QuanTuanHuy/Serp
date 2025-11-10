/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Menu Displays slice for client-side state
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type {
  MenuDisplayDetail,
  MenuDisplayFilters,
  MenuDisplayStats,
} from '../../types';
import { useAppSelector } from '@/shared';
import { RootState } from '@/lib';

interface MenuDisplaysState {
  filters: MenuDisplayFilters;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDir: 'ASC' | 'DESC';

  selectedMenuDisplay: MenuDisplayDetail | null;
  isDialogOpen: boolean;
  isCreating: boolean;

  stats: MenuDisplayStats;

  expandedNodes: number[];

  totalItems: number;
  totalPages: number;
}

const initialState: MenuDisplaysState = {
  filters: {
    search: '',
    moduleId: undefined,
    menuType: undefined, // kept client-side only (not yet supported by BE)
  },
  page: 0,
  pageSize: 20,
  sortBy: 'id',
  sortDir: 'DESC',
  selectedMenuDisplay: null,
  isDialogOpen: false,
  isCreating: false,
  stats: {
    total: 0,
    byModule: {},
    byType: {
      SIDEBAR: 0,
      TOPBAR: 0,
      DROPDOWN: 0,
      ACTION: 0,
    },
    visible: 0,
    hidden: 0,
  },
  expandedNodes: [],
  totalItems: 0,
  totalPages: 0,
};

const menuDisplaysSlice = createSlice({
  name: 'admin/menuDisplays',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<MenuDisplayFilters>>) => {
      state.filters = { ...state.filters, ...action.payload };
    },

    setSearch: (state, action: PayloadAction<string>) => {
      state.filters.search = action.payload;
    },

    setModuleFilter: (state, action: PayloadAction<number | undefined>) => {
      state.filters.moduleId = action.payload;
    },

    setMenuTypeFilter: (
      state,
      action: PayloadAction<MenuDisplayFilters['menuType']>
    ) => {
      state.filters.menuType = action.payload;
    },

    setMenuDisplaysPage: (state, action: PayloadAction<number>) => {
      state.page = action.payload;
    },
    setMenuDisplaysPageSize: (state, action: PayloadAction<number>) => {
      state.pageSize = action.payload;
      state.page = 0;
    },
    setMenuDisplaysSort: (
      state,
      action: PayloadAction<{ sortBy: string; sortDir: 'ASC' | 'DESC' }>
    ) => {
      state.sortBy = action.payload.sortBy;
      state.sortDir = action.payload.sortDir;
      state.page = 0;
    },

    clearFilters: (state) => {
      state.filters = initialState.filters;
    },

    openCreateDialog: (state) => {
      state.isDialogOpen = true;
      state.isCreating = true;
      state.selectedMenuDisplay = null;
    },

    openEditDialog: (state, action: PayloadAction<MenuDisplayDetail>) => {
      state.isDialogOpen = true;
      state.isCreating = false;
      state.selectedMenuDisplay = action.payload;
    },

    closeDialog: (state) => {
      state.isDialogOpen = false;
      state.selectedMenuDisplay = null;
    },

    setStats: (state, action: PayloadAction<MenuDisplayStats>) => {
      state.stats = action.payload;
    },

    setMenuDisplaysPaginationMeta: (
      state,
      action: PayloadAction<{
        totalItems: number;
        totalPages: number;
        currentPage: number;
      }>
    ) => {
      state.totalItems = action.payload.totalItems;
      state.totalPages = action.payload.totalPages;
      state.page = action.payload.currentPage;
    },

    toggleNodeExpansion: (state, action: PayloadAction<number>) => {
      const nodeId = action.payload;
      const index = state.expandedNodes.indexOf(nodeId);

      if (index !== -1) {
        // Node is expanded, collapse it
        state.expandedNodes.splice(index, 1);
      } else {
        // Node is collapsed, expand it
        state.expandedNodes.push(nodeId);
      }
    },

    expandAllNodes: (state, action: PayloadAction<number[]>) => {
      state.expandedNodes = action.payload;
    },

    collapseAllNodes: (state) => {
      state.expandedNodes = [];
    },

    resetMenuDisplaysState: () => initialState,
  },
});

export const {
  setFilters,
  setSearch,
  setModuleFilter,
  setMenuTypeFilter,
  clearFilters,
  openCreateDialog,
  openEditDialog,
  closeDialog,
  setStats,
  setMenuDisplaysPaginationMeta,
  toggleNodeExpansion,
  expandAllNodes,
  collapseAllNodes,
  resetMenuDisplaysState,
  setMenuDisplaysPage,
  setMenuDisplaysPageSize,
  setMenuDisplaysSort,
} = menuDisplaysSlice.actions;

export const selectMenuDisplayUI = (state: RootState) =>
  state.admin.menuDisplays;
export const selectMenuDisplaysFilters = (state: RootState) =>
  state.admin.menuDisplays.filters;
export const selectMenuDisplaysStats = (state: RootState) =>
  state.admin.menuDisplays.stats;
export const selectMenuDisplaysPagination = (state: RootState) => ({
  page: state.admin.menuDisplays.page,
  pageSize: state.admin.menuDisplays.pageSize,
  sortBy: state.admin.menuDisplays.sortBy,
  sortDir: state.admin.menuDisplays.sortDir,
  totalItems: state.admin.menuDisplays.totalItems,
  totalPages: state.admin.menuDisplays.totalPages,
});
export const selectMenuDisplaysDialogOpen = (state: RootState) =>
  state.admin.menuDisplays.isDialogOpen;
export const selectSelectedMenuDisplay = (state: RootState) =>
  state.admin.menuDisplays.selectedMenuDisplay;
export const selectIsCreatingMenuDisplay = (state: RootState) =>
  state.admin.menuDisplays.isCreating;
export const selectExpandedMenuDisplayNodes = (state: RootState) =>
  state.admin.menuDisplays.expandedNodes;

export default menuDisplaysSlice.reducer;
