/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Modules UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { ModuleStatus, ModuleType } from '@/modules/admin/types';
import type { RootState } from '@/lib/store';

interface ModulesUiState {
  isDialogOpen: boolean;
  selectedModuleId?: number | null;
  filters: {
    search?: string;
    status?: ModuleStatus;
    type?: ModuleType;
  };
}

const initialState: ModulesUiState = {
  isDialogOpen: false,
  selectedModuleId: null,
  filters: {
    search: '',
    status: undefined,
    type: undefined,
  },
};

const modulesUiSlice = createSlice({
  name: 'admin/modulesUi',
  initialState,
  reducers: {
    setModulesDialogOpen(state, action: PayloadAction<boolean>) {
      state.isDialogOpen = action.payload;
    },
    setSelectedModuleId(
      state,
      action: PayloadAction<number | null | undefined>
    ) {
      state.selectedModuleId = action.payload ?? null;
    },
    clearSelectedModule(state) {
      state.selectedModuleId = null;
    },
    setModulesFilters(
      state,
      action: PayloadAction<Partial<ModulesUiState['filters']>>
    ) {
      state.filters = { ...state.filters, ...action.payload };
    },
    setModulesSearch(state, action: PayloadAction<string>) {
      state.filters.search = action.payload;
    },
    setModulesStatus(state, action: PayloadAction<ModuleStatus | undefined>) {
      state.filters.status = action.payload;
    },
    setModulesType(state, action: PayloadAction<ModuleType | undefined>) {
      state.filters.type = action.payload;
    },
  },
});

export const modulesReducer = modulesUiSlice.reducer;

export const {
  setModulesDialogOpen,
  setSelectedModuleId,
  clearSelectedModule,
  setModulesFilters,
  setModulesSearch,
  setModulesStatus,
  setModulesType,
} = modulesUiSlice.actions;

export const selectModulesUi = (state: RootState) => state.admin.modules;
export const selectModulesDialogOpen = (state: RootState) =>
  state.admin.modules.isDialogOpen;
export const selectSelectedModuleId = (state: RootState) =>
  state.admin.modules.selectedModuleId;
export const selectModulesFilters = (state: RootState) =>
  state.admin.modules.filters;
