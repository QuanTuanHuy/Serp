/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Form to assign a user to a module
 */

'use client';

import { useState, useEffect, useMemo } from 'react';
import { Shield, UserPlus, Loader2, CheckCircle2 } from 'lucide-react';
import { Button } from '@/shared/components/ui/button';
import { Label } from '@/shared/components/ui/label';
import { Combobox, ComboboxItem } from '@/shared/components/ui/combobox';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Avatar, AvatarFallback } from '@/shared/components/ui/avatar';
import { Badge } from '@/shared/components/ui/badge';
import type { UserProfile } from '@/modules/admin/types';
import type { ModuleRole } from '@/modules/settings/types/module-access.types';
import { getInitials } from '@/shared/utils';

export interface AssignUserFormProps {
  users: UserProfile[];
  roles: ModuleRole[];
  existingUserIds: Set<number>;
  isLoadingRoles: boolean;
  isAssigning: boolean;
  onAssign: (userId: number, roleId?: number) => void;
  onSearchChange: (search: string) => void;
}

export function AssignUserForm({
  users,
  roles,
  existingUserIds,
  isLoadingRoles,
  isAssigning,
  onAssign,
  onSearchChange,
}: AssignUserFormProps) {
  const [userSearch, setUserSearch] = useState('');
  const [selectedUserId, setSelectedUserId] = useState<
    string | number | undefined
  >();
  const [selectedRoleId, setSelectedRoleId] = useState<string | undefined>();

  useEffect(() => {
    const timer = setTimeout(() => {
      onSearchChange(userSearch);
    }, 300);
    return () => clearTimeout(timer);
  }, [userSearch, onSearchChange]);

  const handleAssign = () => {
    if (!selectedUserId) return;
    onAssign(
      Number(selectedUserId),
      selectedRoleId ? Number(selectedRoleId) : undefined
    );
    setSelectedUserId(undefined);
    setSelectedRoleId(undefined);
    setUserSearch('');
  };

  const canAssign = useMemo(() => {
    if (!selectedUserId) return false;
    if (existingUserIds.has(Number(selectedUserId))) return false;
    return true;
  }, [selectedUserId, existingUserIds]);

  const selectedUser = useMemo(() => {
    if (!selectedUserId) return null;
    return users.find((u) => u.id === Number(selectedUserId));
  }, [selectedUserId, users]);

  const selectedRole = useMemo(() => {
    if (!selectedRoleId) return null;
    return roles.find((r) => r.id === Number(selectedRoleId));
  }, [selectedRoleId, roles]);

  // Convert users to combobox items
  const userItems: ComboboxItem[] = useMemo(() => {
    return users.map((u) => ({
      value: u.id,
      label: `${u.firstName} ${u.lastName} (${u.email})${
        existingUserIds.has(u.id) ? ' - Already assigned' : ''
      }`,
    }));
  }, [users, existingUserIds]);

  return (
    <div className='space-y-6'>
      {/* User Selection */}
      <div className='space-y-3'>
        <Label className='text-sm font-medium'>Search & Select User</Label>
        <Combobox
          value={selectedUserId}
          onChange={(val) => setSelectedUserId(val)}
          items={userItems}
          placeholder='Search users by name or email...'
          emptyText={
            userSearch
              ? 'No users found matching your search'
              : 'No users available'
          }
          onSearch={(query) => setUserSearch(query)}
          clearable={true}
          className='w-full'
        />
      </div>

      {/* Role Selection */}
      <div className='space-y-3'>
        <div className='flex items-center justify-between'>
          <Label className='text-sm font-medium'>
            Assign Role{' '}
            <span className='text-xs text-muted-foreground font-normal'>
              (optional)
            </span>
          </Label>
          {selectedRoleId && (
            <Button
              variant='ghost'
              size='sm'
              onClick={() => setSelectedRoleId(undefined)}
              className='h-auto p-1 text-xs'
            >
              Clear
            </Button>
          )}
        </div>
        <Select value={selectedRoleId} onValueChange={setSelectedRoleId}>
          <SelectTrigger>
            <SelectValue
              placeholder={
                isLoadingRoles
                  ? 'Loading available roles...'
                  : 'No specific role (use default permissions)'
              }
            />
          </SelectTrigger>
          <SelectContent>
            {isLoadingRoles ? (
              <div className='p-4 text-sm text-muted-foreground flex items-center gap-2'>
                <Loader2 className='h-4 w-4 animate-spin' /> Loading roles...
              </div>
            ) : roles && roles.length > 0 ? (
              roles.map((r) => (
                <SelectItem key={r.id} value={String(r.id)}>
                  <div className='flex items-center gap-2 py-1'>
                    <Shield className='h-4 w-4 text-purple-500' />
                    <div className='flex flex-col'>
                      <span className='text-sm font-medium'>{r.name}</span>
                      {r.description && (
                        <span className='text-xs text-muted-foreground'>
                          {r.description}
                        </span>
                      )}
                    </div>
                  </div>
                </SelectItem>
              ))
            ) : (
              <div className='p-4 text-sm text-center text-muted-foreground'>
                No roles defined for this module
              </div>
            )}
          </SelectContent>
        </Select>
      </div>

      {/* Preview & Submit */}
      {selectedUser && (
        <div className='p-4 border rounded-lg bg-muted/50 space-y-3'>
          <div className='flex items-center justify-between'>
            <span className='text-sm font-medium text-muted-foreground'>
              Assignment Preview
            </span>
            <CheckCircle2 className='h-4 w-4 text-green-500' />
          </div>
          <div className='flex items-center gap-3'>
            <Avatar className='h-10 w-10'>
              <AvatarFallback>
                {getInitials({
                  firstName: selectedUser.firstName,
                  lastName: selectedUser.lastName,
                  email: selectedUser.email,
                })}
              </AvatarFallback>
            </Avatar>
            <div className='flex-1 min-w-0'>
              <div className='text-sm font-medium'>
                {selectedUser.firstName} {selectedUser.lastName}
              </div>
              <div className='text-xs text-muted-foreground'>
                {selectedUser.email}
              </div>
            </div>
          </div>
          {selectedRole && (
            <div className='flex items-center gap-2 text-sm'>
              <Shield className='h-4 w-4 text-purple-500' />
              <span className='font-medium'>Role:</span>
              <Badge variant='secondary'>{selectedRole.name}</Badge>
            </div>
          )}
        </div>
      )}

      <Button
        onClick={handleAssign}
        disabled={!canAssign || isAssigning}
        className='w-full'
        size='lg'
      >
        {isAssigning ? (
          <>
            <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            Assigning...
          </>
        ) : (
          <>
            <UserPlus className='mr-2 h-4 w-4' />
            Assign User to Module
          </>
        )}
      </Button>
    </div>
  );
}
