'use client';
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Invite User dialog
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
import { Textarea } from '@/shared/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Loader2, Send } from 'lucide-react';
import type { UserType } from '@/modules/admin/types';
import type { InviteUserRequest } from '../../types/user.types';

const userTypeOptions: UserType[] = [
  'EMPLOYEE',
  'ADMIN',
  'CONTRACTOR',
  'EXTERNAL',
  'GUEST',
];

export interface InviteUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (body: InviteUserRequest) => Promise<any>;
  isSubmitting?: boolean;
}

export function InviteUserDialog({
  open,
  onOpenChange,
  onSubmit,
  isSubmitting,
}: InviteUserDialogProps) {
  const [email, setEmail] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [userType, setUserType] = useState<string>('EMPLOYEE');
  const [message, setMessage] = useState('');

  const resetForm = () => {
    setEmail('');
    setFirstName('');
    setLastName('');
    setUserType('EMPLOYEE');
    setMessage('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await onSubmit({
        email,
        firstName,
        lastName,
        userType,
        message: message || undefined,
      });
      resetForm();
      onOpenChange(false);
    } catch {
      // Error handled by parent hook
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
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>Invite User</DialogTitle>
          <DialogDescription>
            Send an invitation email to add a new member to your organization.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='invite-firstName'>First Name</Label>
              <Input
                id='invite-firstName'
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                placeholder='John'
                required
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='invite-lastName'>Last Name</Label>
              <Input
                id='invite-lastName'
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                placeholder='Doe'
                required
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='invite-email'>Email Address</Label>
            <Input
              id='invite-email'
              type='email'
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder='john@example.com'
              required
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='invite-type'>Role Type</Label>
            <Select value={userType} onValueChange={setUserType}>
              <SelectTrigger id='invite-type'>
                <SelectValue placeholder='Select role' />
              </SelectTrigger>
              <SelectContent>
                {userTypeOptions.map((t) => (
                  <SelectItem key={t} value={t}>
                    {t.charAt(0) + t.slice(1).toLowerCase()}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='invite-message'>
              Personal Message{' '}
              <span className='text-muted-foreground'>(optional)</span>
            </Label>
            <Textarea
              id='invite-message'
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder='Welcome to our organization...'
              rows={3}
            />
          </div>

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
                <Send className='mr-2 h-4 w-4' />
              )}
              Send Invitation
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default InviteUserDialog;
