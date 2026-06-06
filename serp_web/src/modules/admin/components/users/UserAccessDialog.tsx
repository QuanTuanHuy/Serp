'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User access dialog
 */

import { useEffect, useState } from 'react';
import {
  Badge,
  Button,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  ScrollArea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Separator,
  Skeleton,
} from '@/shared/components/ui';
import {
  useGetUserDetailQuery,
  useGetOrganizationRolesQuery,
  useUpdateUserRolesMutation,
  useUpdateUserTypeMutation,
} from '@/modules/admin/services/users/usersApi';
import type { Role, UserType } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';
import { AdminStatusBadge } from '@/modules/admin/components/shared/AdminStatusBadge';

interface UserAccessDialogProps {
  open: boolean;
  organizationId?: number;
  userId?: number;
  onOpenChange: (open: boolean) => void;
}

const userTypeOptions: Array<{ value: UserType; label: string }> = [
  { value: 'OWNER', label: 'Owner' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'CONTRACTOR', label: 'Contractor' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GUEST', label: 'Guest' },
];

export function UserAccessDialog({
  open,
  organizationId,
  userId,
  onOpenChange,
}: UserAccessDialogProps) {
  const { success, error: showError } = useNotification();
  const { data: detailResponse, isLoading: isLoadingDetail } =
    useGetUserDetailQuery(
      { organizationId: organizationId ?? 0, userId: userId ?? 0 },
      { skip: !open || !organizationId || !userId }
    );
  const { data: rolesResponse, isLoading: isLoadingRoles } =
    useGetOrganizationRolesQuery(organizationId ?? 0, {
      skip: !open || !organizationId,
    });

  const [updateUserRoles, updateRolesState] = useUpdateUserRolesMutation();
  const [updateUserType, updateTypeState] = useUpdateUserTypeMutation();

  const user = detailResponse?.data;
  const availableRoles = (rolesResponse?.data ?? []) as Role[];

  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [selectedUserType, setSelectedUserType] =
    useState<UserType>('EMPLOYEE');

  useEffect(() => {
    if (user) {
      setSelectedUserType(user.userType);
      setSelectedRoleIds(user.roles.map((role) => role.id));
    }
  }, [user]);

  const toggleRole = (roleId: number) => {
    setSelectedRoleIds((current) =>
      current.includes(roleId)
        ? current.filter((id) => id !== roleId)
        : [...current, roleId]
    );
  };

  const handleSave = async () => {
    if (!organizationId || !userId || !user) {
      return;
    }

    try {
      if (selectedUserType !== user.userType) {
        await updateUserType({
          organizationId,
          userId,
          body: { userType: selectedUserType },
        }).unwrap();
      }

      const originalRoleIds = user.roles.map((role) => role.id).sort();
      const nextRoleIds = [...selectedRoleIds].sort();
      const rolesChanged =
        originalRoleIds.length !== nextRoleIds.length ||
        originalRoleIds.some((roleId, index) => roleId !== nextRoleIds[index]);

      if (rolesChanged) {
        await updateUserRoles({
          organizationId,
          userId,
          body: { roleIds: selectedRoleIds },
        }).unwrap();
      }

      success('User access updated successfully.');
      onOpenChange(false);
    } catch (error) {
      showError(getErrorMessage(error));
    }
  };

  const isLoading = isLoadingDetail || isLoadingRoles;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[85vh] max-w-3xl overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Manage access</DialogTitle>
          <DialogDescription>
            Update user type and role assignments for the selected organization.
          </DialogDescription>
        </DialogHeader>

        {isLoading || !user ? (
          <div className='space-y-4'>
            <Skeleton className='h-16 w-full' />
            <Skeleton className='h-72 w-full' />
          </div>
        ) : (
          <div className='space-y-5'>
            <section className='flex items-center justify-between gap-4 rounded-lg border p-4'>
              <div className='min-w-0'>
                <p className='truncate text-sm font-medium'>
                  {user.firstName} {user.lastName}
                </p>
                <p className='truncate text-sm text-muted-foreground'>
                  {user.email}
                </p>
              </div>
              <div className='flex items-center gap-2'>
                <AdminStatusBadge status={user.status} />
                <Badge variant='outline'>{user.userType}</Badge>
              </div>
            </section>

            <section className='space-y-3'>
              <div>
                <h3 className='text-sm font-semibold'>User type</h3>
                <p className='text-sm text-muted-foreground'>
                  Controls the account category and default access baseline.
                </p>
              </div>
              <div className='grid gap-2 sm:max-w-xs'>
                <Label>User type</Label>
                <Select
                  value={selectedUserType}
                  onValueChange={(value) =>
                    setSelectedUserType(value as UserType)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {userTypeOptions.map((item) => (
                      <SelectItem key={item.value} value={item.value}>
                        {item.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </section>

            <Separator />

            <section className='space-y-3'>
              <div>
                <h3 className='text-sm font-semibold'>Roles</h3>
                <p className='text-sm text-muted-foreground'>
                  Choose the roles assigned to this user.
                </p>
              </div>
              <ScrollArea className='h-64 rounded-md border'>
                <div className='space-y-1 p-2'>
                  {availableRoles.length > 0 ? (
                    availableRoles.map((role) => (
                      <label
                        key={role.id}
                        className='flex items-start gap-2 rounded-md px-2 py-2 text-sm hover:bg-muted/50'
                      >
                        <Checkbox
                          checked={selectedRoleIds.includes(role.id)}
                          onCheckedChange={() => toggleRole(role.id)}
                          className='mt-0.5'
                        />
                        <div className='min-w-0 flex-1'>
                          <div className='flex items-center gap-2'>
                            <span className='font-medium'>{role.name}</span>
                            <Badge variant='outline' className='text-[10px]'>
                              {role.scope}
                            </Badge>
                          </div>
                          {role.description ? (
                            <p className='truncate text-xs text-muted-foreground'>
                              {role.description}
                            </p>
                          ) : null}
                        </div>
                      </label>
                    ))
                  ) : (
                    <p className='p-3 text-sm text-muted-foreground'>
                      No roles available for this organization.
                    </p>
                  )}
                </div>
              </ScrollArea>
            </section>

            <section className='space-y-3'>
              <div>
                <h3 className='text-sm font-semibold'>Departments</h3>
                <p className='text-sm text-muted-foreground'>
                  Current department membership is shown in the details drawer.
                </p>
              </div>
              <div className='flex flex-wrap gap-2'>
                {user.departments.length > 0 ? (
                  user.departments.map((department) => (
                    <Badge key={department.id} variant='secondary'>
                      {department.name || `Department #${department.id}`}
                    </Badge>
                  ))
                ) : (
                  <p className='text-sm text-muted-foreground'>
                    No department membership.
                  </p>
                )}
              </div>
            </section>
          </div>
        )}

        <DialogFooter>
          <Button
            type='button'
            variant='outline'
            onClick={() => onOpenChange(false)}
          >
            Cancel
          </Button>
          <Button
            type='button'
            onClick={handleSave}
            disabled={updateRolesState.isLoading || updateTypeState.isLoading}
          >
            Save
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
