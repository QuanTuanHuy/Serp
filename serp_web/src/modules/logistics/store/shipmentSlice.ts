/**
 * Logistics Module - Shipment Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Shipment UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { ShipmentFilters } from '../types';

export interface ShipmentUiState {
  filters: ShipmentFilters;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit';
  selectedShipmentId: string | null;
  itemDialogOpen: boolean;
  selectedItemId: string | null;
}

const initialState: ShipmentUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
    query: '',
    statusId: undefined,
    shipmentTypeId: undefined,
    toCustomerId: undefined,
    fromSupplierId: undefined,
    orderId: undefined,
  },
  dialogOpen: false,
  dialogMode: 'create',
  selectedShipmentId: null,
  itemDialogOpen: false,
  selectedItemId: null,
};

const shipmentSlice = createSlice({
  name: 'logistics/shipment',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<ShipmentFilters>>) => {
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
    setCustomerFilter: (state, action: PayloadAction<string | undefined>) => {
      state.filters.toCustomerId = action.payload;
      state.filters.page = 1;
    },
    setSupplierFilter: (state, action: PayloadAction<string | undefined>) => {
      state.filters.fromSupplierId = action.payload;
      state.filters.page = 1;
    },
    setOrderFilter: (state, action: PayloadAction<string | undefined>) => {
      state.filters.orderId = action.payload;
      state.filters.page = 1;
    },
    openDialog: (
      state,
      action: PayloadAction<{
        mode: 'create' | 'edit';
        shipmentId?: string;
      }>
    ) => {
      state.dialogOpen = true;
      state.dialogMode = action.payload.mode;
      state.selectedShipmentId = action.payload.shipmentId || null;
    },
    closeDialog: (state) => {
      state.dialogOpen = false;
      state.selectedShipmentId = null;
    },
    openItemDialog: (state, action: PayloadAction<string | null>) => {
      state.itemDialogOpen = true;
      state.selectedItemId = action.payload;
    },
    closeItemDialog: (state) => {
      state.itemDialogOpen = false;
      state.selectedItemId = null;
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
  setCustomerFilter,
  setSupplierFilter,
  setOrderFilter,
  openDialog,
  closeDialog,
  openItemDialog,
  closeItemDialog,
  resetFilters,
} = shipmentSlice.actions;

// Selectors
export const selectShipmentFilters = (state: RootState) =>
  state.logistics.shipment.filters;
export const selectShipmentDialogOpen = (state: RootState) =>
  state.logistics.shipment.dialogOpen;
export const selectShipmentDialogMode = (state: RootState) =>
  state.logistics.shipment.dialogMode;
export const selectSelectedShipmentId = (state: RootState) =>
  state.logistics.shipment.selectedShipmentId;
export const selectShipmentItemDialogOpen = (state: RootState) =>
  state.logistics.shipment.itemDialogOpen;
export const selectSelectedItemId = (state: RootState) =>
  state.logistics.shipment.selectedItemId;

export default shipmentSlice.reducer;
