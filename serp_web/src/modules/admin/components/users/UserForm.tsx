'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin User form (create/update)
 */

import React, { useEffect, useMemo, useState } from 'react';
import { Loader2 } from 'lucide-react';
import {
  Badge,
  Button,
  Checkbox,
  Input,
  Label,
  ScrollArea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Separator,
} from '@/shared/components/ui';
import type {
  CreateUserForOrganizationRequest,
  Role,
  UpdateUserInfoRequest,
  UserProfile,
  UserType,
} from '../../types';

type Mode = 'create' | 'edit';

export interface UserFormProps {
  mode: Mode;
  initialUser?: Partial<UserProfile>;
  submitting?: boolean;
  errorText?: string | null;
  availableRoles?: Role[];
  onSubmit: (
    payload: CreateUserForOrganizationRequest | UpdateUserInfoRequest
  ) => void | Promise<void>;
}

const userTypeOptions: Array<{ value: UserType; label: string }> = [
  { value: 'OWNER', label: 'Owner' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'CONTRACTOR', label: 'Contractor' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GUEST', label: 'Guest' },
];

export function UserForm({
  mode,
  initialUser,
  submitting,
  errorText,
  availableRoles = [],
  onSubmit,
}: UserFormProps) {
  const isCreate = useMemo(() => mode === 'create', [mode]);

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [userType, setUserType] = useState<UserType>('EMPLOYEE');
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [timezone, setTimezone] = useState('');
  const [preferredLanguage, setPreferredLanguage] = useState('');
  const [keycloakUserId, setKeycloakUserId] = useState<string | undefined>();
  const [localError, setLocalError] = useState<string | null>(null);

  useEffect(() => {
    setFirstName(initialUser?.firstName ?? '');
    setLastName(initialUser?.lastName ?? '');
    setEmail(initialUser?.email ?? '');
    setUserType(initialUser?.userType ?? 'EMPLOYEE');
    setPhoneNumber(initialUser?.phoneNumber ?? '');
    setAvatarUrl(initialUser?.avatarUrl ?? '');
    setTimezone(initialUser?.timezone ?? '');
    setPreferredLanguage(initialUser?.preferredLanguage ?? '');
    setKeycloakUserId(initialUser?.keycloakId);
    setPassword('');
    setConfirmPassword('');
    setSelectedRoleIds([]);
    setLocalError(null);
  }, [initialUser, mode]);

  const toggleRole = (roleId: number) => {
    setSelectedRoleIds((current) =>
      current.includes(roleId)
        ? current.filter((id) => id !== roleId)
        : [...current, roleId]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);

    if (isCreate) {
      if (password !== confirmPassword) {
        setLocalError('Passwords do not match.');
        return;
      }

      const payload: CreateUserForOrganizationRequest = {
        firstName,
        lastName,
        email,
        password,
        userType,
        roleIds: selectedRoleIds.length > 0 ? selectedRoleIds : undefined,
      };
      await onSubmit(payload);
      return;
    }

    const payload: UpdateUserInfoRequest = {
      firstName,
      lastName,
      phoneNumber: phoneNumber || undefined,
      avatarUrl: avatarUrl || undefined,
      timezone: timezone || undefined,
      preferredLanguage: preferredLanguage || undefined,
      keycloakUserId: keycloakUserId || undefined,
    };
    await onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit} className='space-y-6'>
      <section className='space-y-4 rounded-lg border p-4'>
        <div>
          <h3 className='text-sm font-semibold'>Basic information</h3>
          <p className='text-sm text-muted-foreground'>
            Identity fields used across the workspace.
          </p>
        </div>
        <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
          <div className='space-y-2'>
            <Label htmlFor='admin-user-first-name'>First name</Label>
            <Input
              id='admin-user-first-name'
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='admin-user-last-name'>Last name</Label>
            <Input
              id='admin-user-last-name'
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />
          </div>
          {isCreate ? (
            <div className='space-y-2 md:col-span-2'>
              <Label htmlFor='admin-user-email'>Email</Label>
              <Input
                id='admin-user-email'
                type='email'
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          ) : null}
        </div>
      </section>

      {isCreate ? (
        <>
          <section className='space-y-4 rounded-lg border p-4'>
            <div>
              <h3 className='text-sm font-semibold'>Credentials</h3>
              <p className='text-sm text-muted-foreground'>
                Initial password for the new account.
              </p>
            </div>
            <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='admin-user-password'>Password</Label>
                <Input
                  id='admin-user-password'
                  type='password'
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='admin-user-confirm-password'>
                  Confirm password
                </Label>
                <Input
                  id='admin-user-confirm-password'
                  type='password'
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>
            </div>
          </section>

          <section className='space-y-4 rounded-lg border p-4'>
            <div>
              <h3 className='text-sm font-semibold'>Access</h3>
              <p className='text-sm text-muted-foreground'>
                Assign the initial user type and roles.
              </p>
            </div>
            <div className='grid gap-4 md:grid-cols-[220px_minmax(0,1fr)]'>
              <div className='space-y-2'>
                <Label>User type</Label>
                <Select
                  value={userType}
                  onValueChange={(value) => setUserType(value as UserType)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {userTypeOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Roles</Label>
                <ScrollArea className='h-44 rounded-md border'>
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
                        Select an organization with roles to assign access.
                      </p>
                    )}
                  </div>
                </ScrollArea>
              </div>
            </div>
          </section>
        </>
      ) : (
        <section className='space-y-4 rounded-lg border p-4'>
          <div>
            <h3 className='text-sm font-semibold'>Profile and preferences</h3>
            <p className='text-sm text-muted-foreground'>
              Keep account access changes in the access dialog.
            </p>
          </div>
          <div className='grid grid-cols-1 gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='admin-user-phone'>Phone number</Label>
              <Input
                id='admin-user-phone'
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='admin-user-avatar'>Avatar URL</Label>
              <Input
                id='admin-user-avatar'
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='admin-user-timezone'>Timezone</Label>
              <Input
                id='admin-user-timezone'
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='admin-user-language'>Preferred language</Label>
              <Input
                id='admin-user-language'
                value={preferredLanguage}
                onChange={(e) => setPreferredLanguage(e.target.value)}
              />
            </div>
          </div>

          <Separator />

          <div className='space-y-2'>
            <Label htmlFor='admin-user-keycloak'>Keycloak user ID</Label>
            <Input
              id='admin-user-keycloak'
              value={keycloakUserId || ''}
              onChange={(e) => setKeycloakUserId(e.target.value || undefined)}
            />
          </div>
        </section>
      )}

      {localError || errorText ? (
        <div className='text-sm text-destructive'>
          {localError || errorText}
        </div>
      ) : null}

      <div className='flex justify-end gap-2 pt-2'>
        <Button type='submit' disabled={!!submitting}>
          {!!submitting && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          {isCreate ? 'Create user' : 'Update user'}
        </Button>
      </div>
    </form>
  );
}

export default UserForm;
