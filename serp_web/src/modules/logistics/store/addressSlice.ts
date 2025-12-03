/**
 * Logistics Module - Address Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Address UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';

export interface AddressUiState {
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit';
  selectedAddressId: string | null;
  entityId: string | null;
  entityTypeId: string | null;
}

const initialState: AddressUiState = {
  dialogOpen: false,
  dialogMode: 'create',
  selectedAddressId: null,
  entityId: null,
  entityTypeId: null,
};

const addressSlice = createSlice({
  name: 'logistics/address',
  initialState,
  reducers: {
    openDialog: (
      state,
      action: PayloadAction<{
        mode: 'create' | 'edit';
        addressId?: string;
        entityId: string;
        entityTypeId: string;
      }>
    ) => {
      state.dialogOpen = true;
      state.dialogMode = action.payload.mode;
      state.selectedAddressId = action.payload.addressId || null;
      state.entityId = action.payload.entityId;
      state.entityTypeId = action.payload.entityTypeId;
    },
    closeDialog: (state) => {
      state.dialogOpen = false;
      state.selectedAddressId = null;
      state.entityId = null;
      state.entityTypeId = null;
    },
  },
});

export const { openDialog, closeDialog } = addressSlice.actions;

// Selectors
export const selectAddressDialogOpen = (state: RootState) =>
  state.logistics.address.dialogOpen;
export const selectAddressDialogMode = (state: RootState) =>
  state.logistics.address.dialogMode;
export const selectSelectedAddressId = (state: RootState) =>
  state.logistics.address.selectedAddressId;
export const selectAddressEntityId = (state: RootState) =>
  state.logistics.address.entityId;
export const selectAddressEntityTypeId = (state: RootState) =>
  state.logistics.address.entityTypeId;

export default addressSlice.reducer;
