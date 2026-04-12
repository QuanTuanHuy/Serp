'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Create User dialog (direct creation for organization)
 */

import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/components/ui/dialog';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Badge } from '@/shared/components/ui/badge';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { ScrollArea } from '@/shared/components/ui/scroll-area';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Loader2, UserPlus } from 'lucide-react';
import type {
  UserType,
  CreateUserForOrganizationRequest,
  Role,
} from '@/modules/admin/types';

const userTypeOptions: { value: UserType; label: string }[] = [
  { value: 'EMPLOYEE', label: 'Employee' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'CONTRACTOR', label: 'Contractor' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GUEST', label: 'Guest' },
];

export interface CreateUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (body: CreateUserForOrganizationRequest) => Promise<any>;
  isSubmitting?: boolean;
  availableRoles?: Role[];
}

export function CreateUserDialog({
  open,
  onOpenChange,
  onSubmit,
  isSubmitting,
  availableRoles = [],
}: CreateUserDialogProps) {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [userType, setUserType] = useState<UserType>('EMPLOYEE');
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);

  const resetForm = () => {
    setFirstName('');
    setLastName('');
    setEmail('');
    setPassword('');
    setUserType('EMPLOYEE');
    setSelectedRoleIds([]);
  };

  const toggleRole = (roleId: number) => {
    setSelectedRoleIds((prev) =>
      prev.includes(roleId)
        ? prev.filter((id) => id !== roleId)
        : [...prev, roleId]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await onSubmit({
        firstName,
        lastName,
        email,
        password,
        userType,
        roleIds: selectedRoleIds.length > 0 ? selectedRoleIds : undefined,
      });
      resetForm();
      onOpenChange(false);
    } catch {
      // Error handled by parent
    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={(o) => {
        if (!o) resetForm();
        onOpenChange(o);
      }}
    >
      <DialogContent className='sm:max-w-lg max-h-[85vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Create User</DialogTitle>
          <DialogDescription>
            Create a new user account directly in the organization.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='create-firstName'>First Name</Label>
              <Input
                id='create-firstName'
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                placeholder='John'
                required
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='create-lastName'>Last Name</Label>
              <Input
                id='create-lastName'
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                placeholder='Doe'
                required
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='create-email'>Email Address</Label>
            <Input
              id='create-email'
              type='email'
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder='john@example.com'
              required
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='create-password'>Password</Label>
            <Input
              id='create-password'
              type='password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='create-type'>User Type</Label>
            <Select
              value={userType}
              onValueChange={(v) => setUserType(v as UserType)}
            >
              <SelectTrigger id='create-type'>
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
          </div>

          {availableRoles.length > 0 && (
            <div className='space-y-2'>
              <Label>
                Assign Roles{' '}
                <span className='text-muted-foreground font-normal'>
                  (optional)
                </span>
              </Label>
              <ScrollArea className='h-[160px] border rounded-md p-2'>
                <div className='space-y-2'>
                  {availableRoles.map((role) => (
                    <label
                      key={role.id}
                      className='flex items-start gap-2.5 p-1.5 rounded hover:bg-muted/50 cursor-pointer'
                    >
                      <Checkbox
                        checked={selectedRoleIds.includes(role.id)}
                        onCheckedChange={() => toggleRole(role.id)}
                        className='mt-0.5'
                      />
                      <div className='flex-1 min-w-0'>
                        <div className='flex items-center gap-1.5'>
                          <span className='text-sm'>{role.name}</span>
                          <Badge
                            variant='outline'
                            className='text-[10px] px-1 py-0'
                          >
                            {role.scope}
                          </Badge>
                        </div>
                        {role.description && (
                          <p className='text-xs text-muted-foreground'>
                            {role.description}
                          </p>
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </ScrollArea>
              {selectedRoleIds.length > 0 && (
                <p className='text-xs text-muted-foreground'>
                  {selectedRoleIds.length} role
                  {selectedRoleIds.length !== 1 ? 's' : ''} selected
                </p>
              )}
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
            <Button type='submit' disabled={isSubmitting}>
              {isSubmitting ? (
                <Loader2 className='mr-2 h-4 w-4 animate-spin' />
              ) : (
                <UserPlus className='mr-2 h-4 w-4' />
              )}
              Create User
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default CreateUserDialog;
