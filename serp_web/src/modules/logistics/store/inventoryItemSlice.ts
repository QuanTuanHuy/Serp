/**
 * Logistics Module - Inventory Item Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Inventory Item UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { InventoryItemFilters } from '../types';

export interface InventoryItemUiState {
  filters: InventoryItemFilters;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit';
  selectedInventoryItemId: string | null;
}

const initialState: InventoryItemUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
    query: '',
    productId: undefined,
    facilityId: undefined,
    expirationDateFrom: undefined,
    expirationDateTo: undefined,
    manufacturingDateFrom: undefined,
    manufacturingDateTo: undefined,
    statusId: undefined,
  },
  dialogOpen: false,
  dialogMode: 'create',
  selectedInventoryItemId: null,
};

const inventoryItemSlice = createSlice({
  name: 'logistics/inventoryItem',
  initialState,
  reducers: {
    setFilters: (
      state,
      action: PayloadAction<Partial<InventoryItemFilters>>
    ) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    setPage: (state, action: PayloadAction<number>) => {
      state.filters.page = action.payload;
    },
    setPageSize: (state, action: PayloadAction<number>) => {
      state.filters.size = action.payload;
      state.filters.page = 1;
    },
    setSearchQuery: (state, action: PayloadAction<string>) => {
      state.filters.query = action.payload;
      state.filters.page = 1;
    },
    setProductFilter: (state, action: PayloadAction<string | undefined>) => {
      state.filters.productId = action.payload;
      state.filters.page = 1;
    },
    setFacilityFilter: (state, action: PayloadAction<string | undefined>) => {
      state.filters.facilityId = action.payload;
      state.filters.page = 1;
    },
    openDialog: (
      state,
      action: PayloadAction<{
        mode: 'create' | 'edit';
        inventoryItemId?: string;
      }>
    ) => {
      state.dialogOpen = true;
      state.dialogMode = action.payload.mode;
      state.selectedInventoryItemId = action.payload.inventoryItemId || null;
    },
    closeDialog: (state) => {
      state.dialogOpen = false;
      state.selectedInventoryItemId = null;
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
  setProductFilter,
  setFacilityFilter,
  openDialog,
  closeDialog,
  resetFilters,
} = inventoryItemSlice.actions;

// Selectors
export const selectInventoryItemFilters = (state: RootState) =>
  state.logistics.inventoryItem.filters;
export const selectInventoryItemDialogOpen = (state: RootState) =>
  state.logistics.inventoryItem.dialogOpen;
export const selectInventoryItemDialogMode = (state: RootState) =>
  state.logistics.inventoryItem.dialogMode;
export const selectSelectedInventoryItemId = (state: RootState) =>
  state.logistics.inventoryItem.selectedInventoryItemId;

export default inventoryItemSlice.reducer;
