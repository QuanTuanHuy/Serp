/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User login form
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
import type { LoginFormData } from '../types';

interface LoginFormProps {
  onSuccess?: () => void;
  onSwitchToRegister?: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({
  onSuccess,
  onSwitchToRegister,
}) => {
  const { login, isLoading, error, clearError } = useAuth();

  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
  });

  const [validationErrors, setValidationErrors] = useState<
    Partial<LoginFormData>
  >({});

  // Form validation
  const validateForm = (): boolean => {
    const errors: Partial<LoginFormData> = {};

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

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle input change
  const handleInputChange = (field: keyof LoginFormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    // Clear validation error for this field
    if (validationErrors[field]) {
      setValidationErrors((prev) => ({ ...prev, [field]: undefined }));
    }

    // Clear auth error
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

    const result = await login(formData);

    if (result.success) {
      onSuccess?.();
    }
  };

  return (
    <Card className='w-full max-w-md mx-auto'>
      <CardHeader className='text-center'>
        <CardTitle className='text-2xl font-bold'>Welcome Back</CardTitle>
        <CardDescription>
          Sign in to your SERP account to continue
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit} className='space-y-4'>
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
              placeholder='Enter your password'
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

          {/* Auth Error */}
          {error && (
            <div className='p-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded'>
              {error}
            </div>
          )}

          {/* Submit Button */}
          <Button type='submit' className='w-full' disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign In'}
          </Button>

          {/* Switch to Register */}
          {onSwitchToRegister && (
            <div className='text-center text-sm'>
              <span className='text-muted-foreground'>
                Don&apos;t have an account?{' '}
              </span>
              <Button
                type='button'
                variant='link'
                className='p-0 h-auto font-normal'
                onClick={onSwitchToRegister}
                disabled={isLoading}
              >
                Sign up here
              </Button>
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
};
