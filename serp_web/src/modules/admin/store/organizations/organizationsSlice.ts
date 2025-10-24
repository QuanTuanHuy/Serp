/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Organizations UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { OrganizationFilters } from '@/modules/admin/types';
import type { RootState } from '@/lib/store';

export interface OrganizationsUiState {
  filters: OrganizationFilters;
}

const initialState: OrganizationsUiState = {
  filters: {
    page: 0,
    pageSize: 10,
    sortBy: 'id',
    sortDir: 'DESC',
  },
};

const organizationsSlice = createSlice({
  name: 'admin/organizationsUi',
  initialState,
  reducers: {
    setFilters(state, action: PayloadAction<Partial<OrganizationFilters>>) {
      state.filters = {
        ...state.filters,
        ...action.payload,
      } as OrganizationFilters;
    },
    setSearch(state, action: PayloadAction<string | undefined>) {
      state.filters.search = action.payload;
      state.filters.page = 0;
    },
    setStatus(state, action: PayloadAction<string | undefined>) {
      state.filters.status = action.payload as any;
      state.filters.page = 0;
    },
    setType(state, action: PayloadAction<string | undefined>) {
      state.filters.type = action.payload as any;
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
  },
});

export const organizationsReducer = organizationsSlice.reducer;

// Actions
export const {
  setFilters,
  setSearch,
  setStatus,
  setType,
  setPage,
  setPageSize,
  setSort,
} = organizationsSlice.actions;

// Selectors
export const selectOrganizationsFilters = (state: RootState) =>
  state.admin.organizations.filters;
