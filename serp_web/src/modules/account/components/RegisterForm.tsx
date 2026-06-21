/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Guided registration form for new organizations
 */

'use client';

import { useCallback, useMemo } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Building2, Loader2, Mail, User2, Wand2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button } from '@/shared/components/ui/button';

import { useAuth } from '../hooks/useAuth';
import { evaluatePassword } from '../utils/password-rules';
import {
  AuthErrorAlert,
  AuthInputField,
  AuthPasswordField,
  PasswordStrengthPanel,
} from './auth/auth-ui';

interface RegisterFormProps {
  onSuccess?: () => void;
  onSwitchToLogin?: () => void;
}

const registerSchema = z
  .object({
    firstName: z
      .string()
      .trim()
      .min(2, 'First name must be at least 2 characters long.'),
    lastName: z
      .string()
      .trim()
      .min(2, 'Last name must be at least 2 characters long.'),
    email: z.string().email('Please enter a valid email address.'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters long.')
      .regex(/[A-Z]/, 'Password must include at least one uppercase letter.')
      .regex(/[a-z]/, 'Password must include at least one lowercase letter.')
      .regex(/\d/, 'Password must include at least one number.'),
    confirmPassword: z.string().min(1, 'Please confirm your password.'),
    organizationName: z
      .string()
      .trim()
      .min(2, 'Organization name must be at least 2 characters long.'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match.',
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

const REGISTER_DEFAULT_VALUES: RegisterFormValues = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  organizationName: '',
};

const REGISTRATION_BENEFITS = [
  'Create your organization and owner account together',
  'Use a secure password with live strength feedback',
  'Land directly in your workspace after setup',
] as const;

export const RegisterForm = ({
  onSuccess,
  onSwitchToLogin,
}: RegisterFormProps) => {
  const { register: registerAccount, isLoading, error, clearError } = useAuth();

  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: REGISTER_DEFAULT_VALUES,
    mode: 'onChange',
  });

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = form;

  const passwordValue = watch('password');
  const passwordRules = useMemo(
    () => evaluatePassword(passwordValue),
    [passwordValue]
  );
  const isBusy = isLoading || isSubmitting;

  const handleFieldInteraction = useCallback(() => {
    if (error) {
      clearError();
    }
  }, [clearError, error]);

  const onSubmit = handleSubmit(async (values) => {
    const result = await registerAccount({
      email: values.email,
      password: values.password,
      firstName: values.firstName,
      lastName: values.lastName,
      organization: {
        name: values.organizationName,
      },
    });

    if (result.success) {
      onSuccess?.();
    }
  });

  return (
    <form onSubmit={onSubmit} className='space-y-5'>
      <div className='grid gap-4 sm:grid-cols-2'>
        <AuthInputField
          id='register-first-name'
          label='First name'
          placeholder='Linh'
          autoComplete='given-name'
          leadingIcon={User2}
          error={errors.firstName?.message}
          disabled={isBusy}
          {...register('firstName', { onChange: handleFieldInteraction })}
        />

        <AuthInputField
          id='register-last-name'
          label='Last name'
          placeholder='Nguyen'
          autoComplete='family-name'
          leadingIcon={User2}
          error={errors.lastName?.message}
          disabled={isBusy}
          {...register('lastName', { onChange: handleFieldInteraction })}
        />
      </div>

      <div className='grid gap-4'>
        <AuthInputField
          id='register-email'
          type='email'
          label='Work email'
          placeholder='founder@company.com'
          autoComplete='email'
          leadingIcon={Mail}
          error={errors.email?.message}
          disabled={isBusy}
          {...register('email', { onChange: handleFieldInteraction })}
        />

        <AuthInputField
          id='register-organization'
          label='Organization name'
          placeholder='Northwind Operations'
          autoComplete='organization'
          leadingIcon={Building2}
          error={errors.organizationName?.message}
          disabled={isBusy}
          {...register('organizationName', {
            onChange: handleFieldInteraction,
          })}
        />

        <AuthPasswordField
          id='register-password'
          label='Create password'
          placeholder='Create a strong password'
          autoComplete='new-password'
          hint='8+ chars, upper/lowercase, number'
          error={errors.password?.message}
          disabled={isBusy}
          toggleLabel='Toggle password visibility'
          {...register('password', { onChange: handleFieldInteraction })}
        />

        <AuthPasswordField
          id='register-confirm-password'
          label='Confirm password'
          placeholder='Repeat your password'
          autoComplete='new-password'
          error={errors.confirmPassword?.message}
          disabled={isBusy}
          toggleLabel='Toggle confirm password visibility'
          {...register('confirmPassword', { onChange: handleFieldInteraction })}
        />
      </div>

      <PasswordStrengthPanel password={passwordValue} rules={passwordRules} />

      {error ? (
        <AuthErrorAlert title='Could not create your account' message={error} />
      ) : null}

      <Button
        type='submit'
        className='h-12 w-full rounded-2xl bg-slate-950 text-white shadow-[0_24px_40px_-24px_rgba(15,23,42,0.8)] hover:bg-slate-800 dark:bg-slate-50 dark:text-slate-950 dark:hover:bg-slate-200'
        disabled={isBusy}
      >
        {isBusy ? (
          <>
            <Loader2 className='h-4 w-4 animate-spin' />
            Creating workspace...
          </>
        ) : (
          <>Create account</>
        )}
      </Button>

      {onSwitchToLogin ? (
        <p className='text-center text-sm text-muted-foreground mt-4'>
          Already have access to a workspace?{' '}
          <button
            type='button'
            className='font-semibold text-primary hover:underline hover:cursor-pointer'
            onClick={onSwitchToLogin}
            disabled={isBusy}
          >
            Sign in
          </button>
        </p>
      ) : null}
    </form>
  );
};
