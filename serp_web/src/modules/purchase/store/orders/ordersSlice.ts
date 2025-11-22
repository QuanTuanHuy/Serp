/*
Author: QuanTuanHuy
Description: Part of Serp Project - Orders Redux slice for UI state management
*/

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { OrderFilters } from '../../types';

export interface OrdersUiState {
  filters: OrderFilters;
  selectedOrderId: string | null;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit' | 'view';
  viewMode: 'list' | 'grid';
}

const initialState: OrdersUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  },
  selectedOrderId: null,
  dialogOpen: false,
  dialogMode: 'create',
  viewMode: 'list',
};

const ordersSlice = createSlice({
  name: 'purchase/ordersUi',
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
    setFromSupplierId(state, action: PayloadAction<string | undefined>) {
      state.filters.fromSupplierId = action.payload;
      state.filters.page = 1;
    },
    setSalesChannelId(state, action: PayloadAction<string | undefined>) {
      state.filters.salesChannelId = action.payload;
      state.filters.page = 1;
    },
    setDateRange(
      state,
      action: PayloadAction<{ deliveryAfter?: string; deliveryBefore?: string }>
    ) {
      state.filters.deliveryAfter = action.payload.deliveryAfter;
      state.filters.deliveryBefore = action.payload.deliveryBefore;
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
      state.selectedOrderId = null;
    },
    openEditDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'edit';
      state.selectedOrderId = action.payload;
    },
    openViewDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'view';
      state.selectedOrderId = action.payload;
    },
    setSelectedOrder(state, action: PayloadAction<string | null>) {
      state.selectedOrderId = action.payload;
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
  setFromSupplierId,
  setSalesChannelId,
  setDateRange,
  setPage,
  setPageSize,
  setSorting,
  setDialogOpen,
  openCreateDialog,
  openEditDialog,
  openViewDialog,
  setSelectedOrder,
  setViewMode,
  resetFilters,
} = ordersSlice.actions;

// Selectors
export const selectOrdersFilters = (state: RootState) =>
  state.purchase.orders.filters;
export const selectOrdersDialogOpen = (state: RootState) =>
  state.purchase.orders.dialogOpen;
export const selectOrdersDialogMode = (state: RootState) =>
  state.purchase.orders.dialogMode;
export const selectSelectedOrderId = (state: RootState) =>
  state.purchase.orders.selectedOrderId;
export const selectOrdersViewMode = (state: RootState) =>
  state.purchase.orders.viewMode;
export const selectOrdersUiState = (state: RootState) => state.purchase.orders;

export default ordersSlice.reducer;
