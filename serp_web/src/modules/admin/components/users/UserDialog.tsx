'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin User dialog
 */

import React, { useMemo } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import UserForm from './UserForm';
import { useSelector } from 'react-redux';
import {
  closeUserDialog,
  selectSelectedOrganizationId,
  selectSelectedUserId,
  selectUsersDialogOpen,
  selectUsersViewMode,
} from '../../store';
import { useDispatch } from 'react-redux';
import { useUsers } from '../../hooks/useUsers';
import type {
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
} from '../../types';

export function UserDialog() {
  const dispatch = useDispatch();
  const open = useSelector(selectUsersDialogOpen);
  const viewMode = useSelector(selectUsersViewMode);
  const selectedOrgId = useSelector(selectSelectedOrganizationId);
  const selectedUserId = useSelector(selectSelectedUserId);

  const { create, update, createUserStatus, updateUserStatus, users } =
    useUsers();
  const initialUser = useMemo(
    () => users.find((u) => u.id === selectedUserId),
    [users, selectedUserId]
  );

  const isCreate = useMemo(() => viewMode === 'create', [viewMode]);

  const handleClose = () => dispatch(closeUserDialog());

  const handleSubmit = async (
    payload: CreateUserForOrganizationRequest | UpdateUserInfoRequest
  ) => {
    if (isCreate) {
      await create(selectedOrgId!, payload as CreateUserForOrganizationRequest);
    } else if (selectedUserId) {
      await update(selectedUserId, payload as UpdateUserInfoRequest);
    }
    handleClose();
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isCreate ? 'Create User' : 'Edit User'}</DialogTitle>
        </DialogHeader>

        <UserForm
          mode={isCreate ? 'create' : 'edit'}
          initialUser={initialUser}
          submitting={createUserStatus.isLoading || updateUserStatus.isLoading}
          errorText={
            (createUserStatus.error as any)?.data?.message ||
            (updateUserStatus.error as any)?.data?.message ||
            null
          }
          onSubmit={handleSubmit}
        />
      </DialogContent>
    </Dialog>
  );
}

export default UserDialog;
