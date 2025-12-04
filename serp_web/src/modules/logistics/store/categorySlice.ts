/**
 * Logistics Module - Category Redux Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Category UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { CategoryFilters } from '../types';

export interface CategoryUiState {
  filters: CategoryFilters;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit';
  selectedCategoryId: string | null;
}

const initialState: CategoryUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  },
  dialogOpen: false,
  dialogMode: 'create',
  selectedCategoryId: null,
};

const categorySlice = createSlice({
  name: 'logistics/category',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<CategoryFilters>>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    setPage: (state, action: PayloadAction<number>) => {
      state.filters.page = action.payload;
    },
    setPageSize: (state, action: PayloadAction<number>) => {
      state.filters.size = action.payload;
    },
    setSearchQuery: (state, action: PayloadAction<string>) => {
      state.filters.query = action.payload;
      state.filters.page = 1;
    },
    openDialog: (
      state,
      action: PayloadAction<{ mode: 'create' | 'edit'; categoryId?: string }>
    ) => {
      state.dialogOpen = true;
      state.dialogMode = action.payload.mode;
      state.selectedCategoryId = action.payload.categoryId || null;
    },
    closeDialog: (state) => {
      state.dialogOpen = false;
      state.selectedCategoryId = null;
    },
    resetFilters: (state) => {
      state.filters = initialState.filters;
    },
  },
});

export const {
  setFilters,
  setPage,
  setPageSize,
  setSearchQuery,
  openDialog,
  closeDialog,
  resetFilters,
} = categorySlice.actions;

// Selectors
export const selectCategoryFilters = (state: RootState) =>
  state.logistics.category.filters;
export const selectCategoryDialogOpen = (state: RootState) =>
  state.logistics.category.dialogOpen;
export const selectCategoryDialogMode = (state: RootState) =>
  state.logistics.category.dialogMode;
export const selectSelectedCategoryId = (state: RootState) =>
  state.logistics.category.selectedCategoryId;

export default categorySlice.reducer;
