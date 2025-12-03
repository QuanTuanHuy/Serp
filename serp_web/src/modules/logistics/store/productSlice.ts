/**
 * Logistics Module - Product Redux Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Product UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { ProductFilters } from '../types';

export interface ProductUiState {
  filters: ProductFilters;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit';
  selectedProductId: string | null;
  viewMode: 'grid' | 'list';
}

const initialState: ProductUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  },
  dialogOpen: false,
  dialogMode: 'create',
  selectedProductId: null,
  viewMode: 'grid',
};

const productSlice = createSlice({
  name: 'logistics/product',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<ProductFilters>>) => {
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
    setCategoryFilter: (state, action: PayloadAction<string | undefined>) => {
      state.filters.categoryId = action.payload;
      state.filters.page = 1;
    },
    setViewMode: (state, action: PayloadAction<'grid' | 'list'>) => {
      state.viewMode = action.payload;
    },
    openDialog: (
      state,
      action: PayloadAction<{ mode: 'create' | 'edit'; productId?: string }>
    ) => {
      state.dialogOpen = true;
      state.dialogMode = action.payload.mode;
      state.selectedProductId = action.payload.productId || null;
    },
    closeDialog: (state) => {
      state.dialogOpen = false;
      state.selectedProductId = null;
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
  setCategoryFilter,
  setViewMode,
  openDialog,
  closeDialog,
  resetFilters,
} = productSlice.actions;

// Selectors
export const selectProductFilters = (state: RootState) =>
  state.logistics.product.filters;
export const selectProductDialogOpen = (state: RootState) =>
  state.logistics.product.dialogOpen;
export const selectProductDialogMode = (state: RootState) =>
  state.logistics.product.dialogMode;
export const selectSelectedProductId = (state: RootState) =>
  state.logistics.product.selectedProductId;
export const selectProductViewMode = (state: RootState) =>
  state.logistics.product.viewMode;

export default productSlice.reducer;
