/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User registration form
 */

'use client';

import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Button,
  Input,
  Label,
} from '@/shared/components';
import type { RegisterFormData } from '../types';

interface RegisterFormProps {
  onSuccess?: () => void;
  onSwitchToLogin?: () => void;
}

export const RegisterForm: React.FC<RegisterFormProps> = ({
  onSuccess,
  onSwitchToLogin,
}) => {
  const { register, isLoading, error, clearError } = useAuth();

  const [formData, setFormData] = useState<RegisterFormData>({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    organizationId: undefined,
  });

  const [validationErrors, setValidationErrors] = useState<
    Partial<RegisterFormData>
  >({});

  // Form validation
  const validateForm = (): boolean => {
    const errors: Partial<RegisterFormData> = {};

    if (!formData.firstName) {
      errors.firstName = 'First name is required';
    } else if (formData.firstName.length < 2) {
      errors.firstName = 'First name must be at least 2 characters';
    }

    if (!formData.lastName) {
      errors.lastName = 'Last name is required';
    } else if (formData.lastName.length < 2) {
      errors.lastName = 'Last name must be at least 2 characters';
    }

    if (!formData.email) {
      errors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = 'Please enter a valid email';
    }

    if (!formData.password) {
      errors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      errors.password = 'Password must be at least 6 characters';
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle input change
  const handleInputChange = (
    field: keyof RegisterFormData,
    value: string | number | undefined
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    if (validationErrors[field]) {
      setValidationErrors((prev) => ({ ...prev, [field]: undefined }));
    }

    if (error) {
      clearError();
    }
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const result = await register({
      email: formData.email,
      password: formData.password,
      firstName: formData.firstName,
      lastName: formData.lastName,
      organizationId: formData.organizationId,
    });

    if (result.success) {
      onSuccess?.();
    }
  };

  return (
    <Card className='w-full max-w-md mx-auto'>
      <CardHeader className='text-center'>
        <CardTitle className='text-2xl font-bold'>Create Account</CardTitle>
        <CardDescription>
          Join SERP to manage your business efficiently
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
          {/* First Name Field */}
          <div className='space-y-2'>
            <Label htmlFor='firstName'>First Name</Label>
            <Input
              id='firstName'
              type='text'
              placeholder='Enter your first name'
              value={formData.firstName}
              onChange={(e) => handleInputChange('firstName', e.target.value)}
              className={validationErrors.firstName ? 'border-red-500' : ''}
              disabled={isLoading}
            />
            {validationErrors.firstName && (
              <p className='text-sm text-red-600'>
                {validationErrors.firstName}
              </p>
            )}
          </div>

          {/* Last Name Field */}
          <div className='space-y-2'>
            <Label htmlFor='lastName'>Last Name</Label>
            <Input
              id='lastName'
              type='text'
              placeholder='Enter your last name'
              value={formData.lastName}
              onChange={(e) => handleInputChange('lastName', e.target.value)}
              className={validationErrors.lastName ? 'border-red-500' : ''}
              disabled={isLoading}
            />
            {validationErrors.lastName && (
              <p className='text-sm text-red-600'>
                {validationErrors.lastName}
              </p>
            )}
          </div>

          {/* Email Field */}
          <div className='space-y-2'>
            <Label htmlFor='email'>Email</Label>
            <Input
              id='email'
              type='email'
              placeholder='Enter your email'
              value={formData.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              className={validationErrors.email ? 'border-red-500' : ''}
              disabled={isLoading}
            />
            {validationErrors.email && (
              <p className='text-sm text-red-600'>{validationErrors.email}</p>
            )}
          </div>

          {/* Password Field */}
          <div className='space-y-2'>
            <Label htmlFor='password'>Password</Label>
            <Input
              id='password'
              type='password'
              placeholder='Create a password'
              value={formData.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              className={validationErrors.password ? 'border-red-500' : ''}
              disabled={isLoading}
            />
            {validationErrors.password && (
              <p className='text-sm text-red-600'>
                {validationErrors.password}
              </p>
            )}
          </div>

          {/* Confirm Password Field */}
          <div className='space-y-2'>
            <Label htmlFor='confirmPassword'>Confirm Password</Label>
            <Input
              id='confirmPassword'
              type='password'
              placeholder='Confirm your password'
              value={formData.confirmPassword}
              onChange={(e) =>
                handleInputChange('confirmPassword', e.target.value)
              }
              className={
                validationErrors.confirmPassword ? 'border-red-500' : ''
              }
              disabled={isLoading}
            />
            {validationErrors.confirmPassword && (
              <p className='text-sm text-red-600'>
                {validationErrors.confirmPassword}
              </p>
            )}
          </div>

          {/* Auth Error */}
          {error && (
            <div className='p-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded'>
              {error}
            </div>
          )}

          {/* Submit Button */}
          <Button type='submit' className='w-full' disabled={isLoading}>
            {isLoading ? 'Creating account...' : 'Create Account'}
          </Button>

          {/* Switch to Login */}
          {onSwitchToLogin && (
            <div className='text-center text-sm'>
              <span className='text-muted-foreground'>
                Already have an account?{' '}
              </span>
              <Button
                type='button'
                variant='link'
                className='p-0 h-auto font-normal'
                onClick={onSwitchToLogin}
                disabled={isLoading}
              >
                Sign in here
              </Button>
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
};
