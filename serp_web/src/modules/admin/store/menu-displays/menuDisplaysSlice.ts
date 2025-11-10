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

interface MenuDisplaysState {
  filters: MenuDisplayFilters;
  selectedMenuDisplay: MenuDisplayDetail | null;
  isDialogOpen: boolean;
  isCreating: boolean;
  stats: MenuDisplayStats;
  expandedNodes: number[];
}

const initialState: MenuDisplaysState = {
  filters: {
    search: '',
    moduleId: undefined,
    menuType: undefined,
  },
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
  toggleNodeExpansion,
  expandAllNodes,
  collapseAllNodes,
  resetMenuDisplaysState,
} = menuDisplaysSlice.actions;

export default menuDisplaysSlice.reducer;
