/**
 * Logistics Module - Order Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Order UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { OrderFilters } from '../types';

export interface OrderUiState {
  filters: OrderFilters;
  selectedOrderId: string | null;
}

const initialState: OrderUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
    query: '',
    statusId: undefined,
    orderTypeId: undefined,
    toCustomerId: undefined,
    fromSupplierId: undefined,
    saleChannelId: undefined,
    orderDateAfter: undefined,
    orderDateBefore: undefined,
    deliveryBefore: undefined,
    deliveryAfter: undefined,
  },
  selectedOrderId: null,
};

const orderSlice = createSlice({
  name: 'logistics/order',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<OrderFilters>>) => {
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
    selectOrder: (state, action: PayloadAction<string | null>) => {
      state.selectedOrderId = action.payload;
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
  selectOrder,
  resetFilters,
} = orderSlice.actions;

// Selectors
export const selectOrderFilters = (state: RootState) =>
  state.logistics.order.filters;
export const selectSelectedOrderId = (state: RootState) =>
  state.logistics.order.selectedOrderId;

export default orderSlice.reducer;
