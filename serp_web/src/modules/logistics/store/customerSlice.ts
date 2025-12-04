/**
 * Logistics Module - Customer Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Customer UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';

export interface CustomerUiState {
  selectedCustomerId: string | null;
}

const initialState: CustomerUiState = {
  selectedCustomerId: null,
};

const customerSlice = createSlice({
  name: 'logistics/customer',
  initialState,
  reducers: {
    selectCustomer: (state, action: PayloadAction<string | null>) => {
      state.selectedCustomerId = action.payload;
    },
  },
});

export const { selectCustomer } = customerSlice.actions;

// Selectors
export const selectSelectedCustomerId = (state: RootState) =>
  state.logistics.customer.selectedCustomerId;

export default customerSlice.reducer;
