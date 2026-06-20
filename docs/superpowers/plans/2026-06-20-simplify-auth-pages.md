# Simplify Register & Login Pages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Simplify the register and login flows in `serp_web` to a single centered column layout and unify their background aesthetics with the home page.

**Architecture:** We will modify `auth-ui.tsx` to allow ReactNode hints for forgot-password links, adjust `LoginForm` and `RegisterForm` to include contextual navigation links, and rebuild `AuthLayout` to use a centered card layout with identical background wrapper properties, gradients, and blurry blobs as `/home`.

**Tech Stack:** React 19, Next.js 15, Tailwind CSS, Lucide React.

---

### Task 1: Update Auth Input Field Hints in Auth UI

**Files:**
- Modify: `serp_web/src/modules/account/components/auth/auth-ui.tsx:38-48`

- [ ] **Step 1: Allow React.ReactNode as hint type in component interfaces**

Replace:
```typescript
interface AuthInputFieldProps extends ComponentPropsWithoutRef<typeof Input> {
  label: string;
  error?: string;
  hint?: string;
  leadingIcon?: LucideIcon;
  containerClassName?: string;
}
```
With:
```typescript
interface AuthInputFieldProps extends ComponentPropsWithoutRef<typeof Input> {
  label: string;
  error?: string;
  hint?: React.ReactNode;
  leadingIcon?: LucideIcon;
  containerClassName?: string;
}
```

- [ ] **Step 2: Save and verify no compilation errors**
Run: `npx tsc --noEmit` from `d:\User2\open_source\serp\serp_web`
Expected: Passes with no errors related to the `hint` type.

---

### Task 2: Simplify LoginForm and Add Forgot Password Link

**Files:**
- Modify: `serp_web/src/modules/account/components/LoginForm.tsx:75-151`

- [ ] **Step 1: Update password field to include a Forgot Password link and change switch link layout**

Modify the form body to:
1. Pass a Next.js `<Link>` component inside the `hint` prop of `<AuthPasswordField>` to link to `/auth/reset-password`.
2. Clean up the bottom action buttons, replacing the switch button with a simple text link: *"Need a brand new workspace? **Create account**"*.

Implementation code:
```tsx
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
              className='text-xs font-semibold text-primary hover:underline'
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
```

---

### Task 3: Simplify RegisterForm and Link to Sign In

**Files:**
- Modify: `serp_web/src/modules/account/components/RegisterForm.tsx:124-267`

- [ ] **Step 1: Simplify form headers, next-steps section, and replace switch-mode layout with a text link**

Change the JSX return inside `RegisterForm` to:
```tsx
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
        className='h-12 w-full rounded-2xl bg-[linear-gradient(135deg,_rgb(8,145,178)_0%,_rgb(14,165,233)_55%,_rgb(245,158,11)_100%)] text-slate-950 shadow-[0_24px_40px_-24px_rgba(14,165,233,0.9)] hover:opacity-95'
        disabled={isBusy}
      >
        {isBusy ? (
          <>
            <Loader2 className='h-4 w-4 animate-spin' />
            Creating workspace...
          </>
        ) : (
          <>
            Create account
            <Wand2 className='h-4 w-4' />
          </>
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
```

---

### Task 4: Rebuild AuthLayout for Centered Single-Column Layout & Home Background

**Files:**
- Modify: `serp_web/src/modules/account/components/AuthLayout.tsx`

- [ ] **Step 1: Simplify AuthLayout to show a single centered Card and apply identical background styling as `/home`**

Remove unused constants (`TRUST_MARKERS`, `VALUE_PILLARS`), clean up imports (remove `Tabs`, `TabsContent`, `TabsList`, `TabsTrigger`, `ArrowRight`, `CheckCircle2`, `Globe2`, `ShieldCheck`, `Sparkles`, `TimerReset`, `Alert`, `AlertTitle`, `AlertDescription`), and replace the JSX:

```tsx
import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';
import {
  Button,
  Card,
  CardContent,
  Separator,
  ThemeToggle,
} from '@/shared/components/ui';
import { BrandMark } from './auth/auth-ui';
import { useAuth } from '../hooks';

type AuthMode = 'login' | 'register';

interface AuthLayoutProps {
  defaultMode?: AuthMode;
  onAuthSuccess?: () => void;
}

export const AuthLayout = ({
  defaultMode = 'login',
  onAuthSuccess,
}: AuthLayoutProps) => {
  const router = useRouter();
  const { isAuthenticated, isLoading } = useAuth();
  const [mode, setMode] = useState<AuthMode>(defaultMode);
  const isLocked = isLoading;

  useEffect(() => {
    setMode(defaultMode);
  }, [defaultMode]);

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.replace('/home');
    }
  }, [isAuthenticated, isLoading, router]);

  const handleModeChange = useCallback(
    (nextMode: AuthMode) => {
      if (isLocked) {
        return;
      }

      setMode(nextMode);
      router.replace(nextMode === 'login' ? '/auth' : '/auth?mode=register', {
        scroll: false,
      });
    },
    [isLocked, router]
  );

  const handleAuthSuccess = useCallback(() => {
    onAuthSuccess?.();
  }, [onAuthSuccess]);

  return (
    <main className='relative min-h-screen overflow-hidden bg-background text-foreground flex flex-col justify-between'>
      {/* Background decoration matching src/app/home/page.tsx exactly */}
      <div className='absolute inset-x-0 top-0 -z-10 h-[28rem] bg-gradient-to-b from-primary/10 via-background to-background' />
      <div className='absolute left-0 top-24 -z-10 h-64 w-64 rounded-full bg-primary/10 blur-3xl' />
      <div className='absolute right-0 top-16 -z-10 h-72 w-72 rounded-full bg-sky-500/10 blur-3xl' />

      <div className='relative mx-auto flex w-full max-w-7xl flex-col px-4 py-6 sm:px-6 lg:px-8'>
        <header className='flex items-center justify-between gap-4'>
          <BrandMark />

          <div className='flex items-center gap-3'>
            <Button
              variant='ghost'
              size='sm'
              className='hidden rounded-full px-4 text-sm text-muted-foreground hover:bg-background/60 md:inline-flex'
              asChild
            >
              <Link href='/'>Explore product</Link>
            </Button>
            <ThemeToggle />
          </div>
        </header>
      </div>

      <div className='relative flex flex-1 items-center justify-center px-4 py-10 lg:py-14'>
        <div className='w-full max-w-md'>
          <Card className='overflow-hidden rounded-[2rem] border-white/70 bg-white/88 py-0 shadow-[0_40px_120px_-48px_rgba(15,23,42,0.45)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/78'>
            <div className='border-b border-border/60 px-6 py-5 sm:px-8'>
              <div className='space-y-2'>
                <h2 className='text-2xl font-semibold tracking-[-0.03em] text-slate-950 dark:text-slate-50'>
                  {mode === 'login' ? 'Sign in' : 'Create account'}
                </h2>
                <p className='text-sm leading-6 text-muted-foreground'>
                  {mode === 'login'
                    ? 'Continue where your team left off with a focused sign-in flow.'
                    : 'Start your organization with a polished onboarding experience.'}
                </p>
              </div>
            </div>

            <CardContent className='space-y-6 px-6 py-6 sm:px-8 sm:py-8'>
              {mode === 'login' ? (
                <LoginForm
                  onSuccess={handleAuthSuccess}
                  onSwitchToRegister={() => handleModeChange('register')}
                />
              ) : (
                <RegisterForm
                  onSuccess={handleAuthSuccess}
                  onSwitchToLogin={() => handleModeChange('login')}
                />
              )}

              <Separator />

              <p className='text-center text-xs leading-6 text-muted-foreground'>
                By continuing, you agree to SERP&apos;s terms of service and privacy policy.
              </p>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Uncluttered bottom spacing */}
      <footer className='py-6 text-center text-xs text-muted-foreground' />
    </main>
  );
};
```

---

### Task 5: Build and Quality Assurance

**Files:**
- Test: Build output and lint rules.

- [ ] **Step 1: Check TypeScript compiler output**
Run: `npm run type-check` from `serp_web/`
Expected: PASS

- [ ] **Step 2: Run linter and formatting**
Run: `npm run lint` from `serp_web/`
Expected: PASS

- [ ] **Step 3: Run full production build**
Run: `npm run build` from `serp_web/`
Expected: PASS (No bundler/component compilation issues)
