/**
 * Logistics Module - Facility Slice
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Facility UI state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { FacilityFilters } from '../types';

export interface FacilityUiState {
  filters: FacilityFilters;
  dialogOpen: boolean;
  dialogMode: 'create' | 'edit';
  selectedFacilityId: string | null;
}

const initialState: FacilityUiState = {
  filters: {
    page: 1,
    size: 10,
    sortBy: 'createdStamp',
    sortDirection: 'desc',
    query: '',
    statusId: undefined,
  },
  dialogOpen: false,
  dialogMode: 'create',
  selectedFacilityId: null,
};

const facilitySlice = createSlice({
  name: 'logistics/facility',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<FacilityFilters>>) => {
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
    openDialog: (
      state,
      action: PayloadAction<{
        mode: 'create' | 'edit';
        facilityId?: string;
      }>
    ) => {
      state.dialogOpen = true;
      state.dialogMode = action.payload.mode;
      state.selectedFacilityId = action.payload.facilityId || null;
    },
    closeDialog: (state) => {
      state.dialogOpen = false;
      state.selectedFacilityId = null;
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
  openDialog,
  closeDialog,
  resetFilters,
} = facilitySlice.actions;

// Selectors
export const selectFacilityFilters = (state: RootState) =>
  state.logistics.facility.filters;
export const selectFacilityDialogOpen = (state: RootState) =>
  state.logistics.facility.dialogOpen;
export const selectFacilityDialogMode = (state: RootState) =>
  state.logistics.facility.dialogMode;
export const selectSelectedFacilityId = (state: RootState) =>
  state.logistics.facility.selectedFacilityId;

export default facilitySlice.reducer;
