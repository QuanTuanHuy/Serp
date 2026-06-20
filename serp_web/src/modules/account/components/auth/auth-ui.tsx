/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Shared UI building blocks for authentication screens
 */

'use client';

import { forwardRef, useMemo, useState } from 'react';
import type { ComponentPropsWithoutRef } from 'react';
import type { LucideIcon } from 'lucide-react';
import {
  AlertCircle,
  CheckCircle2,
  Eye,
  EyeOff,
  LayoutGrid,
  LockKeyhole,
} from 'lucide-react';

import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@/shared/components/ui/alert';
import { Badge } from '@/shared/components/ui/badge';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Progress } from '@/shared/components/ui/progress';
import { cn } from '@/shared/utils';

import {
  getPasswordStrengthScore,
  PASSWORD_REQUIREMENTS,
  type PasswordRules,
} from '../../utils/password-rules';

interface AuthInputFieldProps extends ComponentPropsWithoutRef<typeof Input> {
  label: string;
  error?: string;
  hint?: React.ReactNode;
  leadingIcon?: LucideIcon;
  containerClassName?: string;
}

interface AuthPasswordFieldProps extends AuthInputFieldProps {
  toggleLabel?: string;
}

interface PasswordStrengthPanelProps {
  password: string;
  rules: PasswordRules;
  className?: string;
}

interface AuthErrorAlertProps {
  title: string;
  message: string;
}

const PASSWORD_STRENGTH_LABELS = [
  'Add a password',
  'Weak',
  'Developing',
  'Good',
  'Strong',
] as const;

export function BrandMark() {
  return (
    <div className='flex items-center gap-3'>
      <div className='relative flex h-11 w-11 items-center justify-center overflow-hidden rounded-2xl bg-[linear-gradient(135deg,_rgba(8,145,178,0.95)_0%,_rgba(14,165,233,0.9)_48%,_rgba(245,158,11,0.85)_100%)] text-slate-950 shadow-[0_18px_40px_-22px_rgba(14,165,233,0.8)]'>
        <div className='absolute inset-[1px] rounded-[calc(1rem-1px)] bg-white/24 dark:bg-slate-950/18' />
        <LayoutGrid className='relative h-5 w-5' />
      </div>

      <div>
        <p className='text-sm font-semibold tracking-[0.24em] text-muted-foreground'>
          SERP
        </p>
        <p className='text-sm font-medium text-foreground'>Unified workspace</p>
      </div>
    </div>
  );
}

export const AuthInputField = forwardRef<HTMLInputElement, AuthInputFieldProps>(
  (
    {
      label,
      error,
      hint,
      leadingIcon: LeadingIcon,
      containerClassName,
      className,
      id,
      ...props
    },
    ref
  ) => {
    return (
      <div className={cn('space-y-2.5', containerClassName)}>
        <div className='flex items-center justify-between gap-3'>
          <Label htmlFor={id} className='text-sm font-medium text-foreground'>
            {label}
          </Label>
          {hint ? (
            <span className='text-xs font-medium text-muted-foreground'>
              {hint}
            </span>
          ) : null}
        </div>

        <div className='relative'>
          {LeadingIcon ? (
            <LeadingIcon className='pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground/70' />
          ) : null}

          <Input
            ref={ref}
            id={id}
            className={cn(
              'h-12 rounded-2xl border-border/65 bg-background/80 shadow-none transition-colors focus-visible:border-primary/60 focus-visible:ring-primary/20',
              LeadingIcon ? 'pl-11' : 'pl-4',
              error
                ? 'border-destructive focus-visible:border-destructive focus-visible:ring-destructive/20'
                : null,
              className
            )}
            {...props}
          />
        </div>

        {error ? <p className='text-sm text-destructive'>{error}</p> : null}
      </div>
    );
  }
);

export const AuthPasswordField = forwardRef<
  HTMLInputElement,
  AuthPasswordFieldProps
>(
  (
    {
      label,
      error,
      hint,
      leadingIcon: LeadingIcon = LockKeyhole,
      containerClassName,
      className,
      id,
      toggleLabel = 'Toggle password visibility',
      disabled,
      ...props
    },
    ref
  ) => {
    const [visible, setVisible] = useState(false);

    return (
      <div className={cn('space-y-2.5', containerClassName)}>
        <div className='flex items-center justify-between gap-3'>
          <Label htmlFor={id} className='text-sm font-medium text-foreground'>
            {label}
          </Label>
          {hint ? (
            <span className='text-xs font-medium text-muted-foreground'>
              {hint}
            </span>
          ) : null}
        </div>

        <div className='relative'>
          {LeadingIcon ? (
            <LeadingIcon className='pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground/70' />
          ) : null}

          <Input
            ref={ref}
            id={id}
            type={visible ? 'text' : 'password'}
            disabled={disabled}
            className={cn(
              'h-12 rounded-2xl border-border/65 bg-background/80 shadow-none transition-colors focus-visible:border-primary/60 focus-visible:ring-primary/20',
              'pl-11',
              'pr-12',
              error
                ? 'border-destructive focus-visible:border-destructive focus-visible:ring-destructive/20'
                : null,
              className
            )}
            {...props}
          />

          <Button
            type='button'
            variant='ghost'
            size='icon'
            className='absolute right-1.5 top-1/2 h-9 w-9 -translate-y-1/2 rounded-xl text-muted-foreground hover:bg-muted/70'
            onClick={() => setVisible((current) => !current)}
            aria-label={toggleLabel}
            disabled={disabled}
          >
            {visible ? (
              <EyeOff className='h-4 w-4' />
            ) : (
              <Eye className='h-4 w-4' />
            )}
          </Button>
        </div>

        {error ? <p className='text-sm text-destructive'>{error}</p> : null}
      </div>
    );
  }
);

AuthInputField.displayName = 'AuthInputField';
AuthPasswordField.displayName = 'AuthPasswordField';

export function PasswordStrengthPanel({
  password,
  rules,
  className,
}: PasswordStrengthPanelProps) {
  const { label, value } = useMemo(() => {
    const score = getPasswordStrengthScore(rules);
    const normalizedValue = password
      ? (score / PASSWORD_REQUIREMENTS.length) * 100
      : 0;

    return {
      label: PASSWORD_STRENGTH_LABELS[password ? score : 0],
      value: normalizedValue,
    };
  }, [password, rules]);

  return (
    <div
      className={cn(
        'rounded-2xl border border-border/60 bg-muted/35 p-4 shadow-[0_18px_45px_-38px_rgba(15,23,42,0.45)]',
        className
      )}
    >
      <div className='flex items-start justify-between gap-3'>
        <div className='space-y-1'>
          <p className='text-sm font-semibold text-foreground'>
            Password strength
          </p>
          <p className='text-sm text-muted-foreground'>
            Use a mix of letters and numbers to protect your workspace.
          </p>
        </div>
        <Badge
          variant='outline'
          className='rounded-full border-border/70 bg-background/60 px-3 py-1 text-[11px] uppercase tracking-[0.2em] text-muted-foreground'
        >
          {label}
        </Badge>
      </div>

      <Progress value={value} className='mt-4 h-2 rounded-full bg-primary/10' />

      <div className='mt-4 grid gap-2 sm:grid-cols-2'>
        {PASSWORD_REQUIREMENTS.map((requirement) => (
          <PasswordRequirement
            key={requirement.key}
            label={requirement.label}
            valid={rules[requirement.key]}
          />
        ))}
      </div>
    </div>
  );
}

export function AuthErrorAlert({ title, message }: AuthErrorAlertProps) {
  return (
    <Alert variant='destructive' className='rounded-2xl border-destructive/40'>
      <AlertCircle className='h-4 w-4' />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>{message}</AlertDescription>
    </Alert>
  );
}

function PasswordRequirement({
  label,
  valid,
}: {
  label: string;
  valid: boolean;
}) {
  return (
    <div className='flex items-center gap-2 rounded-xl bg-background/65 px-3 py-2 text-sm'>
      <CheckCircle2
        className={cn(
          'h-4 w-4 shrink-0',
          valid
            ? 'text-emerald-600 dark:text-emerald-400'
            : 'text-muted-foreground/50'
        )}
      />
      <span className={valid ? 'text-foreground' : 'text-muted-foreground'}>
        {label}
      </span>
    </div>
  );
}
