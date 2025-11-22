/*
Author: QuanTuanHuy
Description: Part of Serp Project - Products Redux slice for UI state management
*/

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { ProductFilters } from '../../types';

export interface ProductsUiState {
  filters: ProductFilters;
  selectedProductId: string | null;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit' | 'view';
  viewMode: 'list' | 'grid';
}

const initialState: ProductsUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  },
  selectedProductId: null,
  dialogOpen: false,
  dialogMode: 'create',
  viewMode: 'grid',
};

const productsSlice = createSlice({
  name: 'purchase/productsUi',
  initialState,
  reducers: {
    setQuery(state, action: PayloadAction<string | undefined>) {
      state.filters.query = action.payload;
      state.filters.page = 1; // Reset page on search
    },
    setCategoryId(state, action: PayloadAction<string | undefined>) {
      state.filters.categoryId = action.payload;
      state.filters.page = 1;
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
      state.selectedProductId = null;
    },
    openEditDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'edit';
      state.selectedProductId = action.payload;
    },
    openViewDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'view';
      state.selectedProductId = action.payload;
    },
    setSelectedProduct(state, action: PayloadAction<string | null>) {
      state.selectedProductId = action.payload;
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
  setCategoryId,
  setStatusId,
  setPage,
  setPageSize,
  setSorting,
  setDialogOpen,
  openCreateDialog,
  openEditDialog,
  openViewDialog,
  setSelectedProduct,
  setViewMode,
  resetFilters,
} = productsSlice.actions;

// Selectors
export const selectProductsFilters = (state: RootState) =>
  state.purchase.products.filters;
export const selectProductsDialogOpen = (state: RootState) =>
  state.purchase.products.dialogOpen;
export const selectProductsDialogMode = (state: RootState) =>
  state.purchase.products.dialogMode;
export const selectSelectedProductId = (state: RootState) =>
  state.purchase.products.selectedProductId;
export const selectProductsViewMode = (state: RootState) =>
  state.purchase.products.viewMode;
export const selectProductsUiState = (state: RootState) =>
  state.purchase.products;

export default productsSlice.reducer;
