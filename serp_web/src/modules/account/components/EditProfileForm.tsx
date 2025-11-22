/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Edit Profile Form Component
 */

'use client';

import React, { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import { useUpdateUserProfileMutation } from '../services/userApi';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components';
import type { User, UpdateProfileRequest } from '../types';

export interface EditProfileFormProps {
  user: User;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function EditProfileForm({
  user,
  onSuccess,
  onCancel,
}: EditProfileFormProps) {
  const [updateProfile, { isLoading, isSuccess, isError, error }] =
    useUpdateUserProfileMutation();

  const [formData, setFormData] = useState<UpdateProfileRequest>({
    firstName: user.firstName,
    lastName: user.lastName,
    phoneNumber: user.phoneNumber || '',
    timezone: user.timezone || '',
    preferredLanguage: user.preferredLanguage || '',
  });

  useEffect(() => {
    if (isSuccess) {
      onSuccess?.();
    }
  }, [isSuccess, onSuccess]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateProfile({
        userId: user.id,
        data: formData,
      }).unwrap();
    } catch (err) {
      console.error('Failed to update profile:', err);
    }
  };

  const handleChange = (field: keyof UpdateProfileRequest, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Basic Information</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='firstName'>First Name</Label>
              <Input
                id='firstName'
                value={formData.firstName || ''}
                onChange={(e) => handleChange('firstName', e.target.value)}
                required
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='lastName'>Last Name</Label>
              <Input
                id='lastName'
                value={formData.lastName || ''}
                onChange={(e) => handleChange('lastName', e.target.value)}
                required
              />
            </div>
          </div>

          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='phoneNumber'>Phone Number</Label>
              <Input
                id='phoneNumber'
                type='tel'
                value={formData.phoneNumber || ''}
                onChange={(e) => handleChange('phoneNumber', e.target.value)}
                placeholder='+1234567890'
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='timezone'>Timezone</Label>
              <Input
                id='timezone'
                value={formData.timezone || ''}
                onChange={(e) => handleChange('timezone', e.target.value)}
                placeholder='Asia/Ho_Chi_Minh'
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='preferredLanguage'>Preferred Language</Label>
            <Input
              id='preferredLanguage'
              value={formData.preferredLanguage || ''}
              onChange={(e) =>
                handleChange('preferredLanguage', e.target.value)
              }
              placeholder='en, vi, etc.'
            />
          </div>

          {isError && (
            <div className='text-sm text-destructive'>
              Failed to update profile. Please try again.
            </div>
          )}

          <div className='flex justify-end gap-2 pt-2'>
            {onCancel && (
              <Button type='button' variant='outline' onClick={onCancel}>
                Cancel
              </Button>
            )}
            <Button type='submit' disabled={isLoading}>
              {isLoading && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
              {isLoading ? 'Saving...' : 'Save Changes'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
