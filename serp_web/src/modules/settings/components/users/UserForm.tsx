'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings User form (create/update)
 */

import React, { useMemo, useState } from 'react';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Button } from '@/shared/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Card, CardContent } from '@/shared/components/ui/card';
import { Loader2 } from 'lucide-react';
import type {
  UserProfile,
  UserType,
  CreateUserForOrganizationRequest,
  UpdateUserInfoRequest,
} from '@/modules/admin/types';

type Mode = 'create' | 'edit';

export interface SettingsUserFormProps {
  mode: Mode;
  initialUser?: Partial<UserProfile>;
  submitting?: boolean;
  errorText?: string | null;
  onSubmit: (
    payload: CreateUserForOrganizationRequest | UpdateUserInfoRequest
  ) => void | Promise<void>;
}

const userTypeOptions: UserType[] = [
  'OWNER',
  'ADMIN',
  'EMPLOYEE',
  'CONTRACTOR',
  'EXTERNAL',
  'GUEST',
];

export function SettingsUserForm({
  mode,
  initialUser,
  submitting,
  errorText,
  onSubmit,
}: SettingsUserFormProps) {
  const [firstName, setFirstName] = useState(initialUser?.firstName ?? '');
  const [lastName, setLastName] = useState(initialUser?.lastName ?? '');
  const [email, setEmail] = useState(initialUser?.email ?? '');
  const [password, setPassword] = useState('');
  const [userType, setUserType] = useState<UserType>('EMPLOYEE');
  const [phoneNumber, setPhoneNumber] = useState(
    initialUser?.phoneNumber ?? ''
  );
  const [avatarUrl, setAvatarUrl] = useState(initialUser?.avatarUrl ?? '');
  const [timezone, setTimezone] = useState(initialUser?.timezone ?? '');
  const [preferredLanguage, setPreferredLanguage] = useState(
    initialUser?.preferredLanguage ?? ''
  );
  const [keycloakUserId, setKeycloakUserId] = useState<string | undefined>(
    initialUser?.keycloakId
  );

  const isCreate = useMemo(() => mode === 'create', [mode]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isCreate) {
      const payload: CreateUserForOrganizationRequest = {
        firstName,
        lastName,
        email,
        password,
        userType,
      };
      await onSubmit(payload);
      return;
    }
    const payload: UpdateUserInfoRequest = {
      firstName,
      lastName,
      phoneNumber,
      avatarUrl,
      timezone,
      preferredLanguage,
      keycloakUserId,
    };
    await onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit} className='space-y-6'>
      <Card>
        <CardContent className='pt-6 space-y-4'>
          <h3 className='text-lg font-semibold'>Basic Information</h3>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            <div>
              <Label>First Name</Label>
              <Input
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
            </div>
            <div>
              <Label>Last Name</Label>
              <Input
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>
          </div>

          {isCreate ? (
            <>
              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div>
                  <Label>Email</Label>
                  <Input
                    type='email'
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <Label>Password</Label>
                  <Input
                    type='password'
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div>
                <Label>User Type</Label>
                <Select
                  value={userType}
                  onValueChange={(v) => setUserType(v as UserType)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select user type' />
                  </SelectTrigger>
                  <SelectContent>
                    {userTypeOptions.map((t) => (
                      <SelectItem key={t} value={t}>
                        {t}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </>
          ) : (
            <>
              <h3 className='text-lg font-semibold'>Profile & Preferences</h3>
              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div>
                  <Label>Phone Number</Label>
                  <Input
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                  />
                </div>
                <div>
                  <Label>Avatar URL</Label>
                  <Input
                    value={avatarUrl}
                    onChange={(e) => setAvatarUrl(e.target.value)}
                  />
                </div>
              </div>
              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div>
                  <Label>Timezone</Label>
                  <Input
                    value={timezone}
                    onChange={(e) => setTimezone(e.target.value)}
                  />
                </div>
                <div>
                  <Label>Preferred Language</Label>
                  <Input
                    value={preferredLanguage}
                    onChange={(e) => setPreferredLanguage(e.target.value)}
                  />
                </div>
              </div>

              {/* <h3 className='text-lg font-semibold'>Keycloak</h3>
              <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
                <div>
                  <Label>Keycloak User ID</Label>
                  <Input
                    value={keycloakUserId || ''}
                    onChange={(e) =>
                      setKeycloakUserId(e.target.value || undefined)
                    }
                  />
                  <p className='text-xs text-muted-foreground mt-1'>
                    Optional: set or update the mapped Keycloak user ID
                  </p>
                </div>
              </div> */}
            </>
          )}
        </CardContent>
      </Card>

      {errorText ? (
        <div className='text-sm text-destructive'>{errorText}</div>
      ) : null}

      <div className='flex justify-end gap-2 pt-2'>
        <Button type='submit' disabled={!!submitting}>
          {!!submitting && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
          {isCreate ? 'Create User' : 'Update User'}
        </Button>
      </div>
    </form>
  );
}

export default SettingsUserForm;
