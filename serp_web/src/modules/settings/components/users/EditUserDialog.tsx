'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Edit User dialog with tabbed sections (Info, Roles, Type & Status)
 */

import React, { useCallback, useEffect, useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui/dialog';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '@/shared/components/ui/avatar';
import { Separator } from '@/shared/components/ui/separator';
import { Skeleton } from '@/shared/components/ui/skeleton';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import { Loader2, User, Shield, Settings2 } from 'lucide-react';
import { SettingsStatusBadge } from '../shared/SettingsStatusBadge';
import {
  useGetUserDetailQuery,
  useGetOrganizationRolesQuery,
  useUpdateOrganizationUserMutation,
  useUpdateUserRolesMutation,
  useUpdateUserTypeMutation,
  useUpdateUserStatusMutation,
} from '../../services/users/usersApi';
import type { UserType, UpdateUserInfoRequest } from '@/modules/admin/types';
import { getInitials } from '@/shared/utils';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';

const userTypeOptions: { value: UserType; label: string }[] = [
  { value: 'OWNER', label: 'Owner' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'CONTRACTOR', label: 'Contractor' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GUEST', label: 'Guest' },
];

const statusOptions: {
  value: string;
  label: string;
  destructive?: boolean;
}[] = [
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive', destructive: true },
  { value: 'SUSPENDED', label: 'Suspended', destructive: true },
];

export interface EditUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  userId: number | null;
  organizationId: number | undefined;
}

export function EditUserDialog({
  open,
  onOpenChange,
  userId,
  organizationId,
}: EditUserDialogProps) {
  const { success, error: showError } = useNotification();

  // Queries
  const { data: detailResponse, isLoading: isLoadingDetail } =
    useGetUserDetailQuery(
      { organizationId: organizationId as number, userId: userId as number },
      { skip: !open || !userId || !organizationId }
    );
  const { data: rolesResponse, isLoading: isLoadingRoles } =
    useGetOrganizationRolesQuery(
      { organizationId: organizationId as number },
      { skip: !open || !organizationId }
    );

  const user = detailResponse?.data;
  const availableRoles = rolesResponse?.data ?? [];

  // Mutations
  const [updateInfo, updateInfoStatus] = useUpdateOrganizationUserMutation();
  const [updateRolesMut, updateRolesStatus] = useUpdateUserRolesMutation();
  const [updateTypeMut, updateTypeStatus] = useUpdateUserTypeMutation();
  const [updateStatusMut, updateStatusResult] = useUpdateUserStatusMutation();

  // ==================== Info Tab State ====================
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [timezone, setTimezone] = useState('');
  const [preferredLanguage, setPreferredLanguage] = useState('');

  // ==================== Roles Tab State ====================
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);

  // ==================== Type & Status Tab State ====================
  const [userType, setUserType] = useState<UserType>('EMPLOYEE');
  const [userStatus, setUserStatus] = useState<string>('ACTIVE');
  const [activeTab, setActiveTab] = useState('info');

  // Sync state when user detail loads
  useEffect(() => {
    if (user) {
      setFirstName(user.firstName ?? '');
      setLastName(user.lastName ?? '');
      setPhoneNumber(user.phoneNumber ?? '');
      setTimezone(user.timezone ?? '');
      setPreferredLanguage(user.preferredLanguage ?? '');
      setSelectedRoleIds(user.roles?.map((r) => r.id) ?? []);
      setUserType(user.userType);
      setUserStatus(user.status);
    }
  }, [user]);

  // ==================== Handlers ====================

  const handleSaveInfo = useCallback(async () => {
    if (!userId) return;
    try {
      const body: UpdateUserInfoRequest = {
        firstName,
        lastName,
        phoneNumber: phoneNumber || undefined,
        timezone: timezone || undefined,
        preferredLanguage: preferredLanguage || undefined,
      };
      await updateInfo({ userId, body }).unwrap();
      success('User info updated');
    } catch (e: any) {
      showError(getErrorMessage(e));
    }
  }, [
    userId,
    firstName,
    lastName,
    phoneNumber,
    timezone,
    preferredLanguage,
    updateInfo,
    success,
    showError,
  ]);

  const handleSaveRoles = useCallback(async () => {
    if (!userId || !organizationId) return;
    try {
      await updateRolesMut({
        organizationId,
        userId,
        body: { roleIds: selectedRoleIds },
      }).unwrap();
      success('User roles updated');
    } catch (e: any) {
      showError(getErrorMessage(e));
    }
  }, [
    userId,
    organizationId,
    selectedRoleIds,
    updateRolesMut,
    success,
    showError,
  ]);

  const handleSaveType = useCallback(async () => {
    if (!userId || !organizationId) return;
    try {
      await updateTypeMut({
        organizationId,
        userId,
        body: { userType },
      }).unwrap();
      success('User type updated');
    } catch (e: any) {
      showError(getErrorMessage(e));
    }
  }, [userId, organizationId, userType, updateTypeMut, success, showError]);

  const handleSaveStatus = useCallback(async () => {
    if (!userId || !organizationId) return;
    try {
      await updateStatusMut({
        organizationId,
        userId,
        status: userStatus,
      }).unwrap();
      success(`User status updated to ${userStatus}`);
    } catch (e: any) {
      showError(getErrorMessage(e));
    }
  }, [userId, organizationId, userStatus, updateStatusMut, success, showError]);

  const toggleRole = (roleId: number) => {
    setSelectedRoleIds((prev) =>
      prev.includes(roleId)
        ? prev.filter((id) => id !== roleId)
        : [...prev, roleId]
    );
  };

  const isLoading = isLoadingDetail || isLoadingRoles;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl max-h-[85vh] flex flex-col'>
        <DialogHeader>
          <DialogTitle>Edit User</DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className='space-y-4'>
            <div className='flex items-center gap-4'>
              <Skeleton className='h-12 w-12 rounded-full' />
              <div className='space-y-2'>
                <Skeleton className='h-5 w-40' />
                <Skeleton className='h-4 w-56' />
              </div>
            </div>
            <Skeleton className='h-64 w-full' />
          </div>
        ) : user ? (
          <div className='flex flex-col gap-4 min-h-0'>
            {/* User Header */}
            <div className='flex items-center gap-3'>
              <Avatar className='h-10 w-10'>
                {user.avatarUrl && (
                  <AvatarImage
                    src={user.avatarUrl}
                    alt={`${user.firstName} ${user.lastName}`}
                  />
                )}
                <AvatarFallback className='bg-primary/10 text-sm'>
                  {getInitials({
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                  })}
                </AvatarFallback>
              </Avatar>
              <div className='min-w-0'>
                <p className='font-medium text-sm truncate'>
                  {user.firstName} {user.lastName}
                </p>
                <p className='text-xs text-muted-foreground truncate'>
                  {user.email}
                </p>
              </div>
              <div className='ml-auto flex items-center gap-2'>
                <SettingsStatusBadge status={user.status} />
                <Badge variant='outline'>{user.userType}</Badge>
              </div>
            </div>

            <Separator />

            {/* Tabs */}
            <Tabs
              value={activeTab}
              onValueChange={setActiveTab}
              className='flex-1 min-h-0'
            >
              <TabsList className='grid w-full grid-cols-3'>
                <TabsTrigger value='info' className='gap-1.5'>
                  <User className='h-3.5 w-3.5' />
                  Info
                </TabsTrigger>
                <TabsTrigger value='roles' className='gap-1.5'>
                  <Shield className='h-3.5 w-3.5' />
                  Roles
                </TabsTrigger>
                <TabsTrigger value='settings' className='gap-1.5'>
                  <Settings2 className='h-3.5 w-3.5' />
                  Type & Status
                </TabsTrigger>
              </TabsList>

              {/* ==================== Info Tab ==================== */}
              <TabsContent value='info' className='space-y-4 mt-4'>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='edit-firstName'>First Name</Label>
                    <Input
                      id='edit-firstName'
                      value={firstName}
                      onChange={(e) => setFirstName(e.target.value)}
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='edit-lastName'>Last Name</Label>
                    <Input
                      id='edit-lastName'
                      value={lastName}
                      onChange={(e) => setLastName(e.target.value)}
                    />
                  </div>
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='edit-phone'>Phone Number</Label>
                  <Input
                    id='edit-phone'
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    placeholder='+84...'
                  />
                </div>
                <div className='grid grid-cols-2 gap-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='edit-tz'>Timezone</Label>
                    <Input
                      id='edit-tz'
                      value={timezone}
                      onChange={(e) => setTimezone(e.target.value)}
                      placeholder='Asia/Ho_Chi_Minh'
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='edit-lang'>Preferred Language</Label>
                    <Input
                      id='edit-lang'
                      value={preferredLanguage}
                      onChange={(e) => setPreferredLanguage(e.target.value)}
                      placeholder='vi'
                    />
                  </div>
                </div>
                <div className='flex justify-end pt-2'>
                  <Button
                    onClick={handleSaveInfo}
                    disabled={updateInfoStatus.isLoading}
                  >
                    {updateInfoStatus.isLoading && (
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    )}
                    Save Info
                  </Button>
                </div>
              </TabsContent>

              {/* ==================== Roles Tab ==================== */}
              <TabsContent value='roles' className='space-y-4 mt-4'>
                <p className='text-sm text-muted-foreground'>
                  Select the roles to assign to this user. Roles define access
                  permissions within the organization.
                </p>
                <ScrollArea className='h-[280px] border rounded-md p-3'>
                  {availableRoles.length === 0 ? (
                    <p className='text-sm text-muted-foreground text-center py-8'>
                      No roles available
                    </p>
                  ) : (
                    <div className='space-y-3'>
                      {availableRoles.map((role) => (
                        <label
                          key={role.id}
                          className='flex items-start gap-3 p-2 rounded-md hover:bg-muted/50 cursor-pointer'
                        >
                          <Checkbox
                            checked={selectedRoleIds.includes(role.id)}
                            onCheckedChange={() => toggleRole(role.id)}
                            className='mt-0.5'
                          />
                          <div className='flex-1 min-w-0'>
                            <div className='flex items-center gap-2'>
                              <span className='text-sm font-medium'>
                                {role.name}
                              </span>
                              <Badge
                                variant='outline'
                                className='text-[10px] px-1.5 py-0'
                              >
                                {role.scope}
                              </Badge>
                            </div>
                            {role.description && (
                              <p className='text-xs text-muted-foreground mt-0.5'>
                                {role.description}
                              </p>
                            )}
                          </div>
                        </label>
                      ))}
                    </div>
                  )}
                </ScrollArea>
                <div className='flex items-center justify-between pt-2'>
                  <span className='text-xs text-muted-foreground'>
                    {selectedRoleIds.length} role
                    {selectedRoleIds.length !== 1 ? 's' : ''} selected
                  </span>
                  <Button
                    onClick={handleSaveRoles}
                    disabled={updateRolesStatus.isLoading}
                  >
                    {updateRolesStatus.isLoading && (
                      <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                    )}
                    Save Roles
                  </Button>
                </div>
              </TabsContent>

              {/* ==================== Type & Status Tab ==================== */}
              <TabsContent value='settings' className='space-y-6 mt-4'>
                {/* User Type Section */}
                <div className='space-y-3'>
                  <div>
                    <h4 className='text-sm font-semibold'>User Type</h4>
                    <p className='text-xs text-muted-foreground'>
                      Defines the user&apos;s classification within the
                      organization
                    </p>
                  </div>
                  <div className='flex items-center gap-3'>
                    <Select
                      value={userType}
                      onValueChange={(v) => setUserType(v as UserType)}
                    >
                      <SelectTrigger className='w-48'>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {userTypeOptions.map((opt) => (
                          <SelectItem key={opt.value} value={opt.value}>
                            {opt.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <Button
                      size='sm'
                      onClick={handleSaveType}
                      disabled={
                        updateTypeStatus.isLoading || userType === user.userType
                      }
                    >
                      {updateTypeStatus.isLoading && (
                        <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      )}
                      Update Type
                    </Button>
                  </div>
                </div>

                <Separator />

                {/* User Status Section */}
                <div className='space-y-3'>
                  <div>
                    <h4 className='text-sm font-semibold'>Account Status</h4>
                    <p className='text-xs text-muted-foreground'>
                      Controls whether the user can access the system
                    </p>
                  </div>
                  <div className='flex items-center gap-3'>
                    <Select value={userStatus} onValueChange={setUserStatus}>
                      <SelectTrigger className='w-48'>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {statusOptions.map((opt) => (
                          <SelectItem key={opt.value} value={opt.value}>
                            {opt.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <Button
                      size='sm'
                      variant={
                        userStatus !== user.status &&
                        statusOptions.find((s) => s.value === userStatus)
                          ?.destructive
                          ? 'destructive'
                          : 'default'
                      }
                      onClick={handleSaveStatus}
                      disabled={
                        updateStatusResult.isLoading ||
                        userStatus === user.status
                      }
                    >
                      {updateStatusResult.isLoading && (
                        <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                      )}
                      Update Status
                    </Button>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        ) : (
          <div className='text-center text-muted-foreground py-8'>
            User not found
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default EditUserDialog;
