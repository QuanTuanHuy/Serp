'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin User dialog
 */

import { useEffect, useMemo, useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Label,
} from '@/shared/components/ui';
import { Combobox } from '@/shared/components/ui/combobox';
import UserForm from './UserForm';
import {
  closeUserDialog,
  selectSelectedOrganizationId,
  selectSelectedUserId,
  selectUsersDialogOpen,
  selectUsersViewMode,
} from '../../store';
import { useUsers } from '../../hooks/useUsers';
import type {
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
} from '../../types';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useGetOrganizationsQuery } from '../../services/organizations/organizationsApi';
import { useGetOrganizationRolesQuery } from '../../services/users/usersApi';

export function UserDialog() {
  const dispatch = useAppDispatch();
  const open = useAppSelector(selectUsersDialogOpen);
  const viewMode = useAppSelector(selectUsersViewMode);
  const selectedOrgId = useAppSelector(selectSelectedOrganizationId);
  const selectedUserId = useAppSelector(selectSelectedUserId);

  const { create, update, createUserStatus, updateUserStatus, users } =
    useUsers();

  const [organizationId, setOrganizationId] = useState<number | undefined>();
  const [organizationSearch, setOrganizationSearch] = useState('');
  const [dialogError, setDialogError] = useState<string | null>(null);

  const isCreate = useMemo(() => viewMode === 'create', [viewMode]);
  const initialUser = useMemo(
    () => users.find((user) => user.id === selectedUserId),
    [users, selectedUserId]
  );

  useEffect(() => {
    if (open && isCreate) {
      setOrganizationId(selectedOrgId);
      setDialogError(null);
    }
  }, [open, isCreate, selectedOrgId]);

  const { data: orgsResponse, isFetching: isFetchingOrganizations } =
    useGetOrganizationsQuery(
      {
        page: 0,
        pageSize: 50,
        sortBy: 'name',
        sortDir: 'ASC',
        search: organizationSearch || undefined,
      },
      { skip: !open || !isCreate }
    );

  const { data: rolesResponse } = useGetOrganizationRolesQuery(
    organizationId ?? 0,
    { skip: !open || !isCreate || !organizationId }
  );

  const organizations = orgsResponse?.data.items ?? [];
  const availableRoles = rolesResponse?.data ?? [];

  const handleClose = () => dispatch(closeUserDialog());

  const handleSubmit = async (
    payload: CreateUserForOrganizationRequest | UpdateUserInfoRequest
  ) => {
    if (isCreate) {
      if (!organizationId) {
        setDialogError('Organization is required to create a user.');
        return;
      }
      await create(organizationId, payload as CreateUserForOrganizationRequest);
    } else if (selectedUserId) {
      await update(selectedUserId, payload as UpdateUserInfoRequest);
    }
    handleClose();
  };

  return (
    <Dialog open={open} onOpenChange={(nextOpen) => !nextOpen && handleClose()}>
      <DialogContent className='max-h-[90vh] w-full overflow-y-auto sm:max-w-4xl'>
        <DialogHeader>
          <DialogTitle>{isCreate ? 'Create user' : 'Edit user'}</DialogTitle>
          <DialogDescription>
            {isCreate
              ? 'Create a user under an organization and assign initial access.'
              : 'Update profile information and preferences.'}
          </DialogDescription>
        </DialogHeader>

        {isCreate ? (
          <section className='space-y-3 rounded-lg border p-4'>
            <div>
              <h3 className='text-sm font-semibold'>Organization</h3>
              <p className='text-sm text-muted-foreground'>
                Choose where this account will be created.
              </p>
            </div>
            <div className='space-y-2'>
              <Label>Organization</Label>
              <Combobox
                value={organizationId}
                modal={true}
                onChange={(value) => {
                  setOrganizationId(
                    value !== undefined ? Number(value) : undefined
                  );
                  setDialogError(null);
                }}
                items={organizations.map((organization) => ({
                  value: organization.id,
                  label: organization.name,
                }))}
                placeholder='Select organization'
                loading={isFetchingOrganizations}
                onSearch={setOrganizationSearch}
              />
            </div>
          </section>
        ) : null}

        <UserForm
          mode={isCreate ? 'create' : 'edit'}
          initialUser={initialUser}
          availableRoles={availableRoles}
          submitting={createUserStatus.isLoading || updateUserStatus.isLoading}
          errorText={
            dialogError ||
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
