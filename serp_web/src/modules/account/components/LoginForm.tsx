/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Refined login form for workspace access
 */

'use client';

import Link from 'next/link';
import { useCallback } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2, Mail, Sparkles } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button } from '@/shared/components/ui/button';

import { useAuth } from '../hooks/useAuth';
import {
  AuthErrorAlert,
  AuthInputField,
  AuthPasswordField,
} from './auth/auth-ui';

interface LoginFormProps {
  onSuccess?: () => void;
  onSwitchToRegister?: () => void;
}

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address.'),
  password: z.string().min(6, 'Password must be at least 6 characters long.'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LOGIN_DEFAULT_VALUES: LoginFormValues = {
  email: '',
  password: '',
};

export const LoginForm = ({
  onSuccess,
  onSwitchToRegister,
}: LoginFormProps) => {
  const { login, isLoading, error, clearError } = useAuth();

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: LOGIN_DEFAULT_VALUES,
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = form;

  const isBusy = isLoading || isSubmitting;

  const handleFieldInteraction = useCallback(() => {
    if (error) {
      clearError();
    }
  }, [clearError, error]);

  const onSubmit = handleSubmit(async (values) => {
    const result = await login(values);

    if (result.success) {
      onSuccess?.();
    }
  });

  return (
    <form onSubmit={onSubmit} className='space-y-5'>
      <div className='grid gap-4'>
        <AuthInputField
          id='login-email'
          type='email'
          label='Work email'
          placeholder='name@company.com'
          autoComplete='email'
          leadingIcon={Mail}
          error={errors.email?.message}
          disabled={isBusy}
          {...register('email', { onChange: handleFieldInteraction })}
        />

        <AuthPasswordField
          id='login-password'
          label='Password'
          placeholder='Enter your password'
          autoComplete='current-password'
          hint={
            <Link
              href='/auth/reset-password'
              className='text-xs font-semibold text-primary hover:underline hover:cursor-pointer'
            >
              Forgot password?
            </Link>
          }
          error={errors.password?.message}
          disabled={isBusy}
          toggleLabel='Toggle password visibility'
          {...register('password', { onChange: handleFieldInteraction })}
        />
      </div>

      {error ? (
        <AuthErrorAlert title='Could not sign you in' message={error} />
      ) : null}

      <Button
        type='submit'
        className='h-12 w-full rounded-2xl bg-slate-950 text-white shadow-[0_24px_40px_-24px_rgba(15,23,42,0.8)] hover:bg-slate-800 dark:bg-slate-50 dark:text-slate-950 dark:hover:bg-slate-200'
        disabled={isBusy}
      >
        {isBusy ? (
          <>
            <Loader2 className='h-4 w-4 animate-spin' />
            Signing in...
          </>
        ) : (
          <>
            Sign in to SERP
            <Sparkles className='h-4 w-4' />
          </>
        )}
      </Button>

      {onSwitchToRegister ? (
        <p className='text-center text-sm text-muted-foreground mt-4'>
          Need a brand new workspace?{' '}
          <button
            type='button'
            className='font-semibold text-primary hover:underline hover:cursor-pointer'
            onClick={onSwitchToRegister}
            disabled={isBusy}
          >
            Create account
          </button>
        </p>
      ) : null}
    </form>
  );
};
