/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Subscriptions UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type {
  SubscriptionFilters,
  SubscriptionStatus,
} from '@/modules/admin/types';
import type { RootState } from '@/lib/store';

export interface SubscriptionsUiState {
  filters: SubscriptionFilters;
  selectedSubscriptionId: number | null;
}

const initialState: SubscriptionsUiState = {
  filters: {
    page: 0,
    pageSize: 10,
    sortBy: 'id',
    sortDir: 'DESC',
  },
  selectedSubscriptionId: null,
};

const subscriptionsSlice = createSlice({
  name: 'admin/subscriptionsUi',
  initialState,
  reducers: {
    setFilters(state, action: PayloadAction<Partial<SubscriptionFilters>>) {
      state.filters = {
        ...state.filters,
        ...action.payload,
      } as SubscriptionFilters;
    },
    setStatus(state, action: PayloadAction<SubscriptionStatus | undefined>) {
      state.filters.status = action.payload;
      state.filters.page = 0;
    },
    setOrganizationId(state, action: PayloadAction<number | undefined>) {
      state.filters.organizationId = action.payload;
      state.filters.page = 0;
    },
    setPlanId(state, action: PayloadAction<number | undefined>) {
      state.filters.planId = action.payload;
      state.filters.page = 0;
    },
    setBillingCycle(state, action: PayloadAction<string | undefined>) {
      state.filters.billingCycle = action.payload as any;
      state.filters.page = 0;
    },
    setPage(state, action: PayloadAction<number>) {
      state.filters.page = action.payload;
    },
    setPageSize(state, action: PayloadAction<number>) {
      state.filters.pageSize = action.payload;
      state.filters.page = 0;
    },
    setSort(
      state,
      action: PayloadAction<{ sortBy: string; sortDir: 'ASC' | 'DESC' }>
    ) {
      state.filters.sortBy = action.payload.sortBy;
      state.filters.sortDir = action.payload.sortDir;
      state.filters.page = 0;
    },
    setSelectedSubscriptionId(state, action: PayloadAction<number | null>) {
      state.selectedSubscriptionId = action.payload;
    },
    clearSelectedSubscription(state) {
      state.selectedSubscriptionId = null;
    },
  },
});

export const subscriptionsReducer = subscriptionsSlice.reducer;

// Actions
export const {
  setFilters: setSubscriptionsFilters,
  setStatus: setSubscriptionsStatus,
  setOrganizationId: setSubscriptionsOrganizationId,
  setPlanId: setSubscriptionsPlanId,
  setBillingCycle: setSubscriptionsBillingCycle,
  setPage: setSubscriptionsPage,
  setPageSize: setSubscriptionsPageSize,
  setSort: setSubscriptionsSort,
  setSelectedSubscriptionId,
  clearSelectedSubscription,
} = subscriptionsSlice.actions;

// Selectors
export const selectSubscriptionsFilters = (state: RootState) =>
  state.admin.subscriptions.filters;

export const selectSelectedSubscriptionId = (state: RootState) =>
  state.admin.subscriptions.selectedSubscriptionId;
