/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Change Password Form Component
 */

'use client';

import React, { useState, useEffect } from 'react';
import { Loader2, Eye, EyeOff } from 'lucide-react';
import { useChangePasswordMutation } from '../services/userApi';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';
import type { ChangePasswordRequest } from '../types';

export interface ChangePasswordFormProps {
  userId: number;
  onSuccess?: () => void;
}

export function ChangePasswordForm({
  userId,
  onSuccess,
}: ChangePasswordFormProps) {
  const [changePassword, { isLoading, isSuccess, isError, error }] =
    useChangePasswordMutation();

  const [formData, setFormData] = useState<
    ChangePasswordRequest & { confirmPassword: string }
  >({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [validationError, setValidationError] = useState('');

  useEffect(() => {
    if (isSuccess) {
      setFormData({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      onSuccess?.();
    }
  }, [isSuccess, onSuccess]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError('');

    // Validation
    if (formData.newPassword.length < 8) {
      setValidationError('New password must be at least 8 characters long');
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setValidationError('New passwords do not match');
      return;
    }

    if (formData.oldPassword === formData.newPassword) {
      setValidationError('New password must be different from old password');
      return;
    }

    try {
      await changePassword({
        userId,
        data: {
          oldPassword: formData.oldPassword,
          newPassword: formData.newPassword,
        },
      }).unwrap();
    } catch (err) {
      console.error('Failed to change password:', err);
    }
  };

  const handleChange = (field: keyof typeof formData, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
    setValidationError('');
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Change Password</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='oldPassword'>Current Password</Label>
            <div className='relative'>
              <Input
                id='oldPassword'
                type={showOldPassword ? 'text' : 'password'}
                value={formData.oldPassword}
                onChange={(e) => handleChange('oldPassword', e.target.value)}
                required
              />
              <button
                type='button'
                onClick={() => setShowOldPassword(!showOldPassword)}
                className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
              >
                {showOldPassword ? (
                  <EyeOff className='h-4 w-4' />
                ) : (
                  <Eye className='h-4 w-4' />
                )}
              </button>
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='newPassword'>New Password</Label>
            <div className='relative'>
              <Input
                id='newPassword'
                type={showNewPassword ? 'text' : 'password'}
                value={formData.newPassword}
                onChange={(e) => handleChange('newPassword', e.target.value)}
                required
              />
              <button
                type='button'
                onClick={() => setShowNewPassword(!showNewPassword)}
                className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
              >
                {showNewPassword ? (
                  <EyeOff className='h-4 w-4' />
                ) : (
                  <Eye className='h-4 w-4' />
                )}
              </button>
            </div>
            <p className='text-xs text-muted-foreground'>
              Must be at least 8 characters long
            </p>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='confirmPassword'>Confirm New Password</Label>
            <div className='relative'>
              <Input
                id='confirmPassword'
                type={showConfirmPassword ? 'text' : 'password'}
                value={formData.confirmPassword}
                onChange={(e) =>
                  handleChange('confirmPassword', e.target.value)
                }
                required
              />
              <button
                type='button'
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
              >
                {showConfirmPassword ? (
                  <EyeOff className='h-4 w-4' />
                ) : (
                  <Eye className='h-4 w-4' />
                )}
              </button>
            </div>
          </div>

          {validationError && (
            <div className='text-sm text-destructive'>{validationError}</div>
          )}

          {isError && (
            <div className='text-sm text-destructive'>
              Failed to change password. Please check your current password and
              try again.
            </div>
          )}

          {isSuccess && (
            <div className='text-sm text-green-600'>
              Password changed successfully!
            </div>
          )}

          <div className='flex justify-end pt-2'>
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              {isLoading ? 'Changing...' : 'Change Password'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
