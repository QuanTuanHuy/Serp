/*
Author: QuanTuanHuy
Description: Part of Serp Project - Addresses Redux slice for UI state management
*/

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { AddressFilters } from '../../types';

export interface AddressesUiState {
  filters: AddressFilters;
  selectedAddressId: string | null;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit' | 'view';
}

const initialState: AddressesUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
  },
  selectedAddressId: null,
  dialogOpen: false,
  dialogMode: 'create',
};

const addressesSlice = createSlice({
  name: 'purchase/addressesUi',
  initialState,
  reducers: {
    setEntityId(state, action: PayloadAction<string | undefined>) {
      state.filters.entityId = action.payload;
      state.filters.page = 1;
    },
    setEntityType(state, action: PayloadAction<string | undefined>) {
      state.filters.entityType = action.payload;
      state.filters.page = 1;
    },
    setAddressType(state, action: PayloadAction<string | undefined>) {
      state.filters.addressType = action.payload;
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
      state.selectedAddressId = null;
    },
    openEditDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'edit';
      state.selectedAddressId = action.payload;
    },
    openViewDialog(state, action: PayloadAction<string>) {
      state.dialogOpen = true;
      state.dialogMode = 'view';
      state.selectedAddressId = action.payload;
    },
    setSelectedAddress(state, action: PayloadAction<string | null>) {
      state.selectedAddressId = action.payload;
    },
    resetFilters(state) {
      state.filters = initialState.filters;
    },
  },
});

export const {
  setEntityId,
  setEntityType,
  setAddressType,
  setPage,
  setPageSize,
  setSorting,
  setDialogOpen,
  openCreateDialog,
  openEditDialog,
  openViewDialog,
  setSelectedAddress,
  resetFilters,
} = addressesSlice.actions;

// Selectors
export const selectAddressesFilters = (state: RootState) =>
  state.purchase.addresses.filters;
export const selectAddressesDialogOpen = (state: RootState) =>
  state.purchase.addresses.dialogOpen;
export const selectAddressesDialogMode = (state: RootState) =>
  state.purchase.addresses.dialogMode;
export const selectSelectedAddressId = (state: RootState) =>
  state.purchase.addresses.selectedAddressId;
export const selectAddressesUiState = (state: RootState) =>
  state.purchase.addresses;

export default addressesSlice.reducer;
