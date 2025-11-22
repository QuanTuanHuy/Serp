/*
Author: QuanTuanHuy
Description: Part of Serp Project - Suppliers Redux slice for UI state management
*/

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { SupplierFilters } from '../../types';

export interface SuppliersUiState {
  filters: SupplierFilters;
  selectedSupplierId: string | null;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit' | 'view';
  viewMode: 'list' | 'grid';
}

const initialState: SuppliersUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  },
  selectedSupplierId: null,
  dialogOpen: false,
  dialogMode: 'create',
  viewMode: 'list',
};

const suppliersSlice = createSlice({
  name: 'purchase/suppliersUi',
  initialState,
  reducers: {
    setQuery(state, action: PayloadAction<string | undefined>) {
      state.filters.query = action.payload;
      state.filters.page = 1; // Reset page on search
    },
    setStatusId(state, action: PayloadAction<string | undefined>) {
      state.filters.statusId = action.payload;
      state.filters.page = 1;
    },
    setPage(state, action: PayloadAction<number>) {
      state.filters.page = action.payload;
    },
    setPageSize(state, action: PayloadAction<number>) {
      state.filters.size = action.payload;
      state.filters.page = 1;
    },
    setSorting(
      state,
      action: PayloadAction<{ sortBy: string; sortDirection: 'asc' | 'desc' }>
    ) {
      state.filters.sortBy = action.payload.sortBy;
      state.filters.sortDirection = action.payload.sortDirection;
    },
    setDialogOpen(state, action: PayloadAction<boolean>) {
      state.dialogOpen = action.payload;
    },
    openCreateDialog(state) {
      state.dialogOpen = true;
      state.dialogMode = 'create';
      state.selectedSupplierId = null;
    },
    openEditDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'edit';
      state.selectedSupplierId = action.payload;
    },
    openViewDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'view';
      state.selectedSupplierId = action.payload;
    },
    setSelectedSupplier(state, action: PayloadAction<string | null>) {
      state.selectedSupplierId = action.payload;
    },
    setViewMode(state, action: PayloadAction<'list' | 'grid'>) {
      state.viewMode = action.payload;
    },
    resetFilters(state) {
      state.filters = initialState.filters;
    },
  },
});

export const {
  setQuery,
  setStatusId,
  setPage,
  setPageSize,
  setSorting,
  setDialogOpen,
  openCreateDialog,
  openEditDialog,
  openViewDialog,
  setSelectedSupplier,
  setViewMode,
  resetFilters,
} = suppliersSlice.actions;

// Selectors
export const selectSuppliersFilters = (state: RootState) =>
  state.purchase.suppliers.filters;
export const selectSuppliersDialogOpen = (state: RootState) =>
  state.purchase.suppliers.dialogOpen;
export const selectSuppliersDialogMode = (state: RootState) =>
  state.purchase.suppliers.dialogMode;
export const selectSelectedSupplierId = (state: RootState) =>
  state.purchase.suppliers.selectedSupplierId;
export const selectSuppliersViewMode = (state: RootState) =>
  state.purchase.suppliers.viewMode;
export const selectSuppliersUiState = (state: RootState) =>
  state.purchase.suppliers;

export default suppliersSlice.reducer;
