/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Modern authentication experience with login and register modes
 */

'use client';

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
        <div className={`w-full transition-all duration-300 ease-in-out ${mode === 'login' ? 'max-w-md' : 'max-w-2xl'}`}>
          <Card className='overflow-hidden rounded-[2rem] border-white/70 bg-white/88 py-0 shadow-[0_40px_120px_-48px_rgba(15,23,42,0.45)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/78 transition-all duration-300 ease-in-out'>
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
                  onSuccess={() => handleModeChange('login')}
                  onSwitchToLogin={() => handleModeChange('login')}
                />
              )}

              <Separator />

              <p className='text-center text-xs leading-6 text-muted-foreground'>
                By continuing, you agree to SERP&apos;s terms of service and
                privacy policy.
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
