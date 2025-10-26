/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Users UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store/store';

type ViewMode = 'create' | 'edit' | null;

interface UsersUiState {
  dialogOpen: boolean;
  viewMode: ViewMode;
  selectedUserId?: number;
  selectedOrganizationId?: number;
}

const initialState: UsersUiState = {
  dialogOpen: false,
  viewMode: null,
  selectedUserId: undefined,
  selectedOrganizationId: undefined,
};

const usersSlice = createSlice({
  name: 'adminUsersUi',
  initialState,
  reducers: {
    openCreateUserDialog(
      state,
      action: PayloadAction<{ organizationId?: number } | undefined>
    ) {
      state.dialogOpen = true;
      state.viewMode = 'create';
      state.selectedUserId = undefined;
      state.selectedOrganizationId = action?.payload?.organizationId;
    },
    openEditUserDialog(state, action: PayloadAction<{ userId: number }>) {
      state.dialogOpen = true;
      state.viewMode = 'edit';
      state.selectedUserId = action.payload.userId;
    },
    closeUserDialog(state) {
      state.dialogOpen = false;
      state.viewMode = null;
      state.selectedUserId = undefined;
    },
  },
});

export const { openCreateUserDialog, openEditUserDialog, closeUserDialog } =
  usersSlice.actions;

export const usersReducer = usersSlice.reducer;

// Selectors
export const selectUsersUiState = (state: RootState) => state.admin.users;
export const selectUsersDialogOpen = (state: RootState) =>
  state.admin.users.dialogOpen;
export const selectUsersViewMode = (state: RootState) =>
  state.admin.users.viewMode;
export const selectSelectedUserId = (state: RootState) =>
  state.admin.users.selectedUserId;
export const selectSelectedOrganizationId = (state: RootState) =>
  state.admin.users.selectedOrganizationId;
