/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Plans UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';

type ViewMode = 'grid' | 'table';

interface PlansUiState {
  viewMode: ViewMode;
  isDialogOpen: boolean;
  selectedPlanId?: number | null;
}

const initialState: PlansUiState = {
  viewMode: 'table',
  isDialogOpen: false,
  selectedPlanId: null,
};

const plansUiSlice = createSlice({
  name: 'admin/plansUi',
  initialState,
  reducers: {
    setViewMode(state, action: PayloadAction<ViewMode>) {
      state.viewMode = action.payload;
    },
    setDialogOpen(state, action: PayloadAction<boolean>) {
      state.isDialogOpen = action.payload;
    },
    setSelectedPlanId(state, action: PayloadAction<number | null | undefined>) {
      state.selectedPlanId = action.payload ?? null;
    },
    clearSelectedPlan(state) {
      state.selectedPlanId = null;
    },
  },
});

export const plansReducer = plansUiSlice.reducer;

// Actions
export const {
  setViewMode,
  setDialogOpen,
  setSelectedPlanId,
  clearSelectedPlan,
} = plansUiSlice.actions;

// Selectors
export const selectPlansUiState = (state: RootState) => state.admin.plans;
export const selectPlansViewMode = (state: RootState) =>
  state.admin.plans.viewMode;
export const selectPlansDialogOpen = (state: RootState) =>
  state.admin.plans.isDialogOpen;
export const selectSelectedPlanId = (state: RootState) =>
  state.admin.plans.selectedPlanId;

export type { ViewMode };
