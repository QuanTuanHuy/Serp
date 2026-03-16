/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Password reset confirmation form with improved feedback
 */

'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import { CheckCircle2, Loader2, ShieldCheck } from 'lucide-react';

import { getErrorMessage, isSuccessResponse } from '@/lib/store/api';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';

import {
  useConfirmPasswordResetMutation,
  useValidatePasswordResetTokenQuery,
} from '../services';
import type { ResetPasswordFormData } from '../types';
import { evaluatePassword } from '../utils/password-rules';
import {
  AuthErrorAlert,
  AuthPasswordField,
  PasswordStrengthPanel,
} from './auth/auth-ui';

interface ResetPasswordFormProps {
  token: string | null;
}

const EMPTY_FORM: ResetPasswordFormData = {
  newPassword: '',
  confirmPassword: '',
};

export function ResetPasswordForm({ token }: ResetPasswordFormProps) {
  const notification = useNotification();

  const [formData, setFormData] = useState<ResetPasswordFormData>(EMPTY_FORM);
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

  const hasStrongPassword = Object.values(rules).every(Boolean);
  const passwordMatches =
    formData.confirmPassword.length > 0 &&
    formData.newPassword === formData.confirmPassword;
  const canSubmit =
    Boolean(token) && hasStrongPassword && passwordMatches && !completed;

  const isTokenValid = Boolean(
    token && data && isSuccessResponse(data) && data.data?.valid
  );

  const validationMessage = useMemo(() => {
    if (!token) {
      return 'This password reset link is missing a token. Please reopen the email from your administrator.';
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
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!token) {
      setFormError('Password reset token is missing.');
      return;
    }

    if (!hasStrongPassword) {
      setFormError(
        'Please choose a stronger password that satisfies every security rule.'
      );
      return;
    }

    if (!passwordMatches) {
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
    <Card className='mx-auto w-full max-w-[34rem] rounded-[2rem] border-white/70 bg-white/88 py-0 shadow-[0_40px_120px_-48px_rgba(15,23,42,0.45)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/78'>
      <CardHeader className='space-y-4 border-b border-border/60 px-6 py-6 text-left sm:px-8'>
        <div className='flex items-center gap-3'>
          <div className='flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
            <ShieldCheck className='h-6 w-6' />
          </div>
          <div>
            <Badge className='rounded-full border border-primary/15 bg-primary/10 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.2em] text-primary'>
              Secure recovery
            </Badge>
          </div>
        </div>

        <div className='space-y-2'>
          <CardTitle className='text-2xl tracking-[-0.03em]'>
            Reset your password
          </CardTitle>
          <CardDescription className='max-w-lg text-sm leading-6'>
            Create a strong password, invalidate previous sessions, and return
            to your SERP workspace safely.
          </CardDescription>
        </div>
      </CardHeader>

      <CardContent className='space-y-5 px-6 py-6 sm:px-8 sm:py-8'>
        {isValidating ? (
          <div className='flex min-h-72 flex-col items-center justify-center gap-3 text-center text-sm text-muted-foreground'>
            <Loader2 className='h-5 w-5 animate-spin text-primary' />
            <p>Verifying your reset link...</p>
          </div>
        ) : completed ? (
          <div className='space-y-4'>
            <Alert className='rounded-2xl border-emerald-500/20 bg-emerald-500/8'>
              <CheckCircle2 className='h-4 w-4 text-emerald-600 dark:text-emerald-400' />
              <AlertTitle>Password updated</AlertTitle>
              <AlertDescription>
                Your password has been reset successfully. Return to sign in and
                continue using SERP.
              </AlertDescription>
            </Alert>

            <Button className='h-12 w-full rounded-2xl' asChild>
              <Link href='/auth'>Back to sign in</Link>
            </Button>
          </div>
        ) : !isTokenValid ? (
          <div className='space-y-4'>
            <AuthErrorAlert
              title='Link unavailable'
              message={
                validationMessage ||
                'This password reset link is invalid or has expired. Ask your administrator for a new email.'
              }
            />

            <div className='flex flex-col gap-3 sm:flex-row'>
              <Button
                variant='outline'
                className='h-11 flex-1 rounded-2xl border-border/70 bg-background/70'
                asChild
              >
                <Link href='/auth'>Go to sign in</Link>
              </Button>
              <Button className='h-11 flex-1 rounded-2xl' asChild>
                <Link href='/auth/reset-password'>Try another link</Link>
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className='space-y-5'>
            <AuthPasswordField
              id='reset-new-password'
              label='New password'
              placeholder='Create a strong password'
              autoComplete='new-password'
              hint='8+ chars, upper/lowercase, number'
              error={undefined}
              disabled={isSubmitting}
              toggleLabel='Toggle new password visibility'
              value={formData.newPassword}
              onChange={(event) =>
                handleChange('newPassword', event.target.value)
              }
            />

            <AuthPasswordField
              id='reset-confirm-password'
              label='Confirm password'
              placeholder='Repeat your new password'
              autoComplete='new-password'
              error={
                formData.confirmPassword.length > 0 && !passwordMatches
                  ? 'Password confirmation does not match.'
                  : undefined
              }
              disabled={isSubmitting}
              toggleLabel='Toggle confirm password visibility'
              value={formData.confirmPassword}
              onChange={(event) =>
                handleChange('confirmPassword', event.target.value)
              }
            />

            <PasswordStrengthPanel
              password={formData.newPassword}
              rules={rules}
            />

            {formError ? (
              <AuthErrorAlert
                title='Could not update password'
                message={formError}
              />
            ) : null}

            <Button
              type='submit'
              className='h-12 w-full rounded-2xl'
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

            <p className='text-center text-xs leading-6 text-muted-foreground'>
              Need a new link? Contact your organization administrator for a new
              reset email.
            </p>
          </form>
        )}
      </CardContent>
    </Card>
  );
}
