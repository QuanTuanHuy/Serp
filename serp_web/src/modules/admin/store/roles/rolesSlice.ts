/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Roles UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RoleFilters } from '@/modules/admin/types';
import type { RootState } from '@/lib/store';

export interface RolesUiState {
  filters: RoleFilters;
  selectedRoleId: number | null;
  dialogOpen: boolean;
  viewMode: 'list' | 'grid';
}

const initialState: RolesUiState = {
  filters: {
    page: 0,
    pageSize: 10,
    sortBy: 'id',
    sortDir: 'DESC',
  },
  selectedRoleId: null,
  dialogOpen: false,
  viewMode: 'list',
};

const rolesSlice = createSlice({
  name: 'admin/rolesUi',
  initialState,
  reducers: {
    setFilters(state, action: PayloadAction<Partial<RoleFilters>>) {
      state.filters = {
        ...state.filters,
        ...action.payload,
      } as RoleFilters;
    },
    setSearch(state, action: PayloadAction<string | undefined>) {
      state.filters.search = action.payload;
      state.filters.page = 0;
    },
    setScope(state, action: PayloadAction<string | undefined>) {
      state.filters.scope = action.payload as any;
      state.filters.page = 0;
    },
    setRoleType(state, action: PayloadAction<string | undefined>) {
      state.filters.roleType = action.payload as any;
      state.filters.page = 0;
    },
    setOrganizationId(state, action: PayloadAction<number | undefined>) {
      state.filters.organizationId = action.payload;
      state.filters.page = 0;
    },
    setModuleId(state, action: PayloadAction<number | undefined>) {
      state.filters.moduleId = action.payload;
      state.filters.page = 0;
    },
    setIsDefault(state, action: PayloadAction<boolean | undefined>) {
      state.filters.isDefault = action.payload;
      state.filters.page = 0;
    },
    setPage(state, action: PayloadAction<number>) {
      state.filters.page = action.payload;
    },
    setPageSize(state, action: PayloadAction<number>) {
      state.filters.pageSize = action.payload;
      state.filters.page = 0;
    },
    setSort(
      state,
      action: PayloadAction<{ sortBy: string; sortDir: 'ASC' | 'DESC' }>
    ) {
      state.filters.sortBy = action.payload.sortBy;
      state.filters.sortDir = action.payload.sortDir;
      state.filters.page = 0;
    },
    setSelectedRoleId(state, action: PayloadAction<number | null>) {
      state.selectedRoleId = action.payload;
    },
    clearSelectedRole(state) {
      state.selectedRoleId = null;
    },
    setDialogOpen(state, action: PayloadAction<boolean>) {
      state.dialogOpen = action.payload;
      if (!action.payload) {
        state.selectedRoleId = null;
      }
    },
    setViewMode(state, action: PayloadAction<'list' | 'grid'>) {
      state.viewMode = action.payload;
    },
  },
});

export const rolesReducer = rolesSlice.reducer;

// Actions
export const {
  setFilters,
  setSearch,
  setScope,
  setRoleType,
  setOrganizationId,
  setModuleId,
  setIsDefault,
  setPage,
  setPageSize,
  setSort,
  setSelectedRoleId,
  clearSelectedRole,
  setDialogOpen,
  setViewMode,
} = rolesSlice.actions;

// Selectors
export const selectRolesFilters = (state: RootState) =>
  state.admin.roles.filters;

export const selectRolesUiState = (state: RootState) => state.admin.roles;

export const selectSelectedRoleId = (state: RootState) =>
  state.admin.roles.selectedRoleId;

export const selectRolesDialogOpen = (state: RootState) =>
  state.admin.roles.dialogOpen;

export const selectRolesViewMode = (state: RootState) =>
  state.admin.roles.viewMode;
