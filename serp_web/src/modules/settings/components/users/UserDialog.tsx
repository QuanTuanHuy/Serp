'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings User dialog
 */

import React, { useMemo, useState } from 'react';
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
import {
  useSettingsCreateUserForOrganizationMutation,
  useUpdateOrganizationUserMutation,
} from '../../services/users/usersApi';
import { useGetMyOrganizationQuery } from '../../services/organizations/organizationsApi';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';

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
  const { success, error: showError } = useNotification();
  const { data: org } = useGetMyOrganizationQuery();
  const [createUser, createStatus] =
    useSettingsCreateUserForOrganizationMutation();
  const [updateUser, updateStatus] = useUpdateOrganizationUserMutation();
  const [errorText, setErrorText] = useState<string | null>(null);

  const isCreate = mode === 'create';

  const handleSubmit = async (
    payload: CreateUserForOrganizationRequest | UpdateUserInfoRequest
  ) => {
    setErrorText(null);
    try {
      if (isCreate) {
        if (!org?.id) return;
        await createUser({
          organizationId: org.id,
          body: payload as CreateUserForOrganizationRequest,
        }).unwrap();
        success('User created successfully');
      } else if (initialUser?.id) {
        await updateUser({
          userId: initialUser.id as number,
          body: payload as UpdateUserInfoRequest,
        }).unwrap();
        success('User updated successfully');
      }
      onOpenChange(false);
    } catch (e: any) {
      const msg = getErrorMessage(e);
      setErrorText(msg);
      showError(msg);
    }
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
          errorText={errorText}
          onSubmit={handleSubmit}
        />
      </DialogContent>
    </Dialog>
  );
}

export default SettingsUserDialog;
