/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Password reset confirmation form
 */

'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import {
  AlertCircle,
  CheckCircle2,
  Eye,
  EyeOff,
  Loader2,
  ShieldCheck,
} from 'lucide-react';

import { getErrorMessage, isSuccessResponse } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components';
import { useNotification } from '@/shared/hooks';

import {
  useConfirmPasswordResetMutation,
  useValidatePasswordResetTokenQuery,
} from '../services';
import type { ResetPasswordFormData } from '../types';

interface ResetPasswordFormProps {
  token: string | null;
}

interface PasswordRules {
  minLength: boolean;
  hasUppercase: boolean;
  hasLowercase: boolean;
  hasNumber: boolean;
}

const EMPTY_FORM: ResetPasswordFormData = {
  newPassword: '',
  confirmPassword: '',
};

function evaluatePassword(value: string): PasswordRules {
  return {
    minLength: value.length >= 8,
    hasUppercase: /[A-Z]/.test(value),
    hasLowercase: /[a-z]/.test(value),
    hasNumber: /\d/.test(value),
  };
}

export function ResetPasswordForm({ token }: ResetPasswordFormProps) {
  const notification = useNotification();

  const [formData, setFormData] = useState<ResetPasswordFormData>(EMPTY_FORM);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [completed, setCompleted] = useState(false);

  const {
    data,
    isLoading: isValidating,
    error: validationError,
  } = useValidatePasswordResetTokenQuery(token ?? '', {
    skip: !token,
    refetchOnMountOrArgChange: true,
  });

  const [confirmPasswordReset, { isLoading: isSubmitting }] =
    useConfirmPasswordResetMutation();

  const rules = useMemo(
    () => evaluatePassword(formData.newPassword),
    [formData.newPassword]
  );

  const canSubmit =
    Boolean(token) &&
    Object.values(rules).every(Boolean) &&
    formData.newPassword === formData.confirmPassword &&
    !completed;

  const isTokenValid = Boolean(
    token && data && isSuccessResponse(data) && data.data?.valid
  );

  const validationMessage = useMemo(() => {
    if (!token) {
      return 'This password reset link is missing a token. Please open the link from your email again.';
    }

    if (validationError) {
      return getErrorMessage(validationError);
    }

    if (data && !isSuccessResponse(data)) {
      return data.message || 'This password reset link is no longer valid.';
    }

    return null;
  }, [data, token, validationError]);

  useEffect(() => {
    setFormError(null);
  }, [formData.newPassword, formData.confirmPassword]);

  const handleChange = (field: keyof ResetPasswordFormData, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!token) {
      setFormError('Password reset token is missing.');
      return;
    }

    if (!Object.values(rules).every(Boolean)) {
      setFormError(
        'Please choose a stronger password that satisfies all rules.'
      );
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setFormError('Password confirmation does not match.');
      return;
    }

    try {
      const response = await confirmPasswordReset({
        token,
        newPassword: formData.newPassword,
      }).unwrap();

      if (!isSuccessResponse(response)) {
        throw new Error(response.message || 'Could not reset password.');
      }

      setCompleted(true);
      setFormData(EMPTY_FORM);
      notification.success('Password updated successfully', {
        description: 'You can now sign in with your new password.',
      });
    } catch (error) {
      setFormError(getErrorMessage(error));
    }
  };

  return (
    <Card className='mx-auto w-full max-w-md border-border/70 shadow-lg'>
      <CardHeader className='space-y-3 text-center'>
        <div className='mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
          <ShieldCheck className='h-6 w-6' />
        </div>
        <div className='space-y-1'>
          <CardTitle className='text-2xl'>Reset your password</CardTitle>
          <CardDescription>
            Create a new password to secure your SERP workspace.
          </CardDescription>
        </div>
      </CardHeader>

      <CardContent className='space-y-4'>
        {isValidating ? (
          <div className='flex min-h-56 flex-col items-center justify-center gap-3 text-center text-sm text-muted-foreground'>
            <Loader2 className='h-5 w-5 animate-spin text-primary' />
            <p>Verifying your reset link...</p>
          </div>
        ) : completed ? (
          <div className='space-y-4'>
            <Alert>
              <CheckCircle2 className='h-4 w-4' />
              <AlertTitle>Password updated</AlertTitle>
              <AlertDescription>
                Your password has been reset successfully. Return to sign in and
                continue using SERP.
              </AlertDescription>
            </Alert>
            <Button className='w-full' asChild>
              <Link href='/auth'>Back to sign in</Link>
            </Button>
          </div>
        ) : !isTokenValid ? (
          <div className='space-y-4'>
            <Alert variant='destructive'>
              <AlertCircle className='h-4 w-4' />
              <AlertTitle>Link unavailable</AlertTitle>
              <AlertDescription>
                {validationMessage ||
                  'This password reset link is invalid or has expired. Request a new email from your administrator.'}
              </AlertDescription>
            </Alert>
            <div className='flex flex-col gap-2 sm:flex-row'>
              <Button variant='outline' className='flex-1' asChild>
                <Link href='/auth'>Go to sign in</Link>
              </Button>
              <Button className='flex-1' asChild>
                <Link href='/auth/reset-password'>Try another link</Link>
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className='space-y-4'>
            <div className='space-y-2'>
              <Label htmlFor='newPassword'>New password</Label>
              <div className='relative'>
                <Input
                  id='newPassword'
                  type={showPassword ? 'text' : 'password'}
                  value={formData.newPassword}
                  onChange={(event) =>
                    handleChange('newPassword', event.target.value)
                  }
                  placeholder='Create a strong password'
                  disabled={isSubmitting}
                  autoComplete='new-password'
                />
                <Button
                  type='button'
                  variant='ghost'
                  size='icon'
                  className='absolute right-1 top-1/2 h-8 w-8 -translate-y-1/2 text-muted-foreground'
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? (
                    <EyeOff className='h-4 w-4' />
                  ) : (
                    <Eye className='h-4 w-4' />
                  )}
                </Button>
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='confirmPassword'>Confirm password</Label>
              <div className='relative'>
                <Input
                  id='confirmPassword'
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={formData.confirmPassword}
                  onChange={(event) =>
                    handleChange('confirmPassword', event.target.value)
                  }
                  placeholder='Repeat your new password'
                  disabled={isSubmitting}
                  autoComplete='new-password'
                />
                <Button
                  type='button'
                  variant='ghost'
                  size='icon'
                  className='absolute right-1 top-1/2 h-8 w-8 -translate-y-1/2 text-muted-foreground'
                  onClick={() => setShowConfirmPassword((prev) => !prev)}
                  aria-label={
                    showConfirmPassword
                      ? 'Hide confirmation'
                      : 'Show confirmation'
                  }
                >
                  {showConfirmPassword ? (
                    <EyeOff className='h-4 w-4' />
                  ) : (
                    <Eye className='h-4 w-4' />
                  )}
                </Button>
              </div>
            </div>

            <div className='rounded-xl border border-border/70 bg-muted/40 p-4'>
              <p className='mb-3 text-sm font-medium'>Password requirements</p>
              <div className='grid gap-2 text-sm text-muted-foreground'>
                <PasswordRule
                  label='At least 8 characters'
                  valid={rules.minLength}
                />
                <PasswordRule
                  label='One uppercase letter'
                  valid={rules.hasUppercase}
                />
                <PasswordRule
                  label='One lowercase letter'
                  valid={rules.hasLowercase}
                />
                <PasswordRule label='One number' valid={rules.hasNumber} />
              </div>
            </div>

            {formError && (
              <Alert variant='destructive'>
                <AlertCircle className='h-4 w-4' />
                <AlertTitle>Could not update password</AlertTitle>
                <AlertDescription>{formError}</AlertDescription>
              </Alert>
            )}

            <Button
              type='submit'
              className='w-full'
              disabled={!canSubmit || isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Updating password...
                </>
              ) : (
                'Save new password'
              )}
            </Button>

            <p className='text-center text-xs text-muted-foreground'>
              Need help? Contact your organization administrator for a new reset
              email.
            </p>
          </form>
        )}
      </CardContent>
    </Card>
  );
}

function PasswordRule({ label, valid }: { label: string; valid: boolean }) {
  return (
    <div className='flex items-center gap-2'>
      <CheckCircle2
        className={
          valid
            ? 'h-4 w-4 text-emerald-600'
            : 'h-4 w-4 text-muted-foreground/50'
        }
      />
      <span className={valid ? 'text-foreground' : undefined}>{label}</span>
    </div>
  );
}
