/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Users UI slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { UserStatus } from '@/modules/admin/types';

type ViewMode = 'create' | 'edit' | null;

interface UsersUiState {
  dialogOpen: boolean;
  viewMode: ViewMode;
  selectedUserId?: number;
  selectedOrganizationId?: number;
  detailDrawerOpen: boolean;
  accessDialogOpen: boolean;
  statusDialogOpen: boolean;
  selectedStatus?: UserStatus;
}

const initialState: UsersUiState = {
  dialogOpen: false,
  viewMode: null,
  selectedUserId: undefined,
  selectedOrganizationId: undefined,
  detailDrawerOpen: false,
  accessDialogOpen: false,
  statusDialogOpen: false,
  selectedStatus: undefined,
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
    openUserDetailDrawer(
      state,
      action: PayloadAction<{ userId: number; organizationId?: number }>
    ) {
      state.selectedUserId = action.payload.userId;
      state.selectedOrganizationId = action.payload.organizationId;
      state.detailDrawerOpen = true;
    },
    closeUserDetailDrawer(state) {
      state.detailDrawerOpen = false;
    },
    openUserAccessDialog(
      state,
      action: PayloadAction<{ userId: number; organizationId?: number }>
    ) {
      state.selectedUserId = action.payload.userId;
      state.selectedOrganizationId = action.payload.organizationId;
      state.accessDialogOpen = true;
    },
    closeUserAccessDialog(state) {
      state.accessDialogOpen = false;
    },
    openUserStatusDialog(
      state,
      action: PayloadAction<{
        userId: number;
        organizationId?: number;
        status?: UserStatus;
      }>
    ) {
      state.selectedUserId = action.payload.userId;
      state.selectedOrganizationId = action.payload.organizationId;
      state.selectedStatus = action.payload.status;
      state.statusDialogOpen = true;
    },
    closeUserStatusDialog(state) {
      state.statusDialogOpen = false;
      state.selectedStatus = undefined;
    },
    closeUserDialog(state) {
      state.dialogOpen = false;
      state.viewMode = null;
      state.selectedUserId = undefined;
    },
  },
});

export const {
  openCreateUserDialog,
  openEditUserDialog,
  openUserDetailDrawer,
  closeUserDetailDrawer,
  openUserAccessDialog,
  closeUserAccessDialog,
  openUserStatusDialog,
  closeUserStatusDialog,
  closeUserDialog,
} = usersSlice.actions;

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
export const selectUsersDetailDrawerOpen = (state: RootState) =>
  state.admin.users.detailDrawerOpen;
export const selectUsersAccessDialogOpen = (state: RootState) =>
  state.admin.users.accessDialogOpen;
export const selectUsersStatusDialogOpen = (state: RootState) =>
  state.admin.users.statusDialogOpen;
export const selectUsersSelectedStatus = (state: RootState) =>
  state.admin.users.selectedStatus;
