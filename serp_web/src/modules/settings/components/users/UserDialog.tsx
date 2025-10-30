'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings User dialog
 */

import React, { useMemo } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import SettingsUserForm from './UserForm';
import type {
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
  UserProfile,
} from '@/modules/admin/types';
import { useSettingsUsers } from '../../hooks/useUsers';

export type SettingsUserDialogMode = 'create' | 'edit';

export interface SettingsUserDialogProps {
  open: boolean;
  mode: SettingsUserDialogMode;
  initialUser?: Partial<UserProfile>;
  onOpenChange: (open: boolean) => void;
}

export function SettingsUserDialog({
  open,
  mode,
  initialUser,
  onOpenChange,
}: SettingsUserDialogProps) {
  const { create, update, createStatus, updateStatus } = useSettingsUsers();

  const isCreate = useMemo(() => mode === 'create', [mode]);

  const handleSubmit = async (
    payload: CreateUserForOrganizationRequest | UpdateUserInfoRequest
  ) => {
    if (isCreate) {
      await create(payload as CreateUserForOrganizationRequest);
    } else if (initialUser?.id) {
      await update(initialUser.id as number, payload as UpdateUserInfoRequest);
    }
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-4xl w-full max-h-[90vh]'>
        <DialogHeader>
          <DialogTitle>{isCreate ? 'Create User' : 'Edit User'}</DialogTitle>
        </DialogHeader>

        <SettingsUserForm
          mode={isCreate ? 'create' : 'edit'}
          initialUser={initialUser}
          submitting={createStatus.isLoading || updateStatus.isLoading}
          errorText={
            (createStatus.error as any)?.data?.message ||
            (updateStatus.error as any)?.data?.message ||
            null
          }
          onSubmit={handleSubmit}
        />
      </DialogContent>
    </Dialog>
  );
}

export default SettingsUserDialog;
