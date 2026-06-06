/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin organizations UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { OrganizationStatus } from '@/modules/admin/types';

export interface OrganizationsUiState {
  selectedOrganizationId?: number;
  detailDrawerOpen: boolean;
  statusDialogOpen: boolean;
  statusDialogStatus?: OrganizationStatus;
}

const initialState: OrganizationsUiState = {
  selectedOrganizationId: undefined,
  detailDrawerOpen: false,
  statusDialogOpen: false,
  statusDialogStatus: undefined,
};

const organizationsUiSlice = createSlice({
  name: 'admin/organizationsUiState',
  initialState,
  reducers: {
    openOrganizationDetailDrawer(
      state,
      action: PayloadAction<{ organizationId: number }>
    ) {
      state.selectedOrganizationId = action.payload.organizationId;
      state.detailDrawerOpen = true;
    },
    closeOrganizationDetailDrawer(state) {
      state.detailDrawerOpen = false;
    },
    openOrganizationStatusDialog(
      state,
      action: PayloadAction<{
        organizationId: number;
        status: OrganizationStatus;
      }>
    ) {
      state.selectedOrganizationId = action.payload.organizationId;
      state.statusDialogOpen = true;
      state.statusDialogStatus = action.payload.status;
    },
    closeOrganizationStatusDialog(state) {
      state.statusDialogOpen = false;
      state.statusDialogStatus = undefined;
    },
  },
});

export const organizationsUiReducer = organizationsUiSlice.reducer;

export const {
  openOrganizationDetailDrawer,
  closeOrganizationDetailDrawer,
  openOrganizationStatusDialog,
  closeOrganizationStatusDialog,
} = organizationsUiSlice.actions;

export const selectOrganizationsUiState = (state: RootState) =>
  state.admin.organizationsUi;
export const selectSelectedOrganizationId = (state: RootState) =>
  state.admin.organizationsUi.selectedOrganizationId;
export const selectOrganizationsDetailDrawerOpen = (state: RootState) =>
  state.admin.organizationsUi.detailDrawerOpen;
export const selectOrganizationsStatusDialogOpen = (state: RootState) =>
  state.admin.organizationsUi.statusDialogOpen;
export const selectOrganizationsStatusDialogStatus = (state: RootState) =>
  state.admin.organizationsUi.statusDialogStatus;
