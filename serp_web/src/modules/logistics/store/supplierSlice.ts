/**
 * Logistics Module - Supplier Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Supplier UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';

export interface SupplierUiState {
  selectedSupplierId: string | null;
}

const initialState: SupplierUiState = {
  selectedSupplierId: null,
};

const supplierSlice = createSlice({
  name: 'logistics/supplier',
  initialState,
  reducers: {
    selectSupplier: (state, action: PayloadAction<string | null>) => {
      state.selectedSupplierId = action.payload;
    },
  },
});

export const { selectSupplier } = supplierSlice.actions;

// Selectors
export const selectSelectedSupplierId = (state: RootState) =>
  state.logistics.supplier.selectedSupplierId;

export default supplierSlice.reducer;
