/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Modern authentication experience with login and register modes
 */

'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  ArrowRight,
  CheckCircle2,
  Globe2,
  ShieldCheck,
  Sparkles,
  TimerReset,
} from 'lucide-react';

import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
  Separator,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  ThemeToggle,
} from '@/shared/components/ui';
import { BrandMark } from './auth/auth-ui';
import { useAuth } from '../hooks';

type AuthMode = 'login' | 'register';

interface AuthLayoutProps {
  defaultMode?: AuthMode;
  onAuthSuccess?: () => void;
}

const TRUST_MARKERS = [
  'Single workspace for teams, approvals, and operations',
  'Fast onboarding with guided account setup',
  'Protected sessions with token-based authentication',
] as const;

const VALUE_PILLARS = [
  {
    title: 'Launch faster',
    description:
      'Go from invite to operational workspace in a guided, low-friction flow.',
    icon: Sparkles,
  },
  {
    title: 'Control access',
    description:
      'Keep modules, users, and permissions coordinated from one secure hub.',
    icon: ShieldCheck,
  },
  {
    title: 'Scale globally',
    description:
      'Run finance, CRM, logistics, and planning inside the same platform.',
    icon: Globe2,
  },
] as const;

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

  const headerCopy = useMemo(
    () =>
      mode === 'login'
        ? {
            badge: 'Operational access',
            title: 'Welcome back to the command center.',
            description:
              'Sign in to continue managing customers, workflows, and every moving part of your business from one secure workspace.',
          }
        : {
            badge: 'Create your workspace',
            title: 'Set up a modern ERP workspace in minutes.',
            description:
              'Create your SERP account, start your organization, and bring your team into a cleaner operating rhythm from day one.',
          },
    [mode]
  );

  return (
    <main className='relative isolate min-h-screen overflow-hidden bg-[linear-gradient(180deg,_rgba(255,255,255,0.96)_0%,_rgba(241,245,249,0.98)_100%)] text-foreground dark:bg-[linear-gradient(180deg,_rgba(2,6,23,0.98)_0%,_rgba(15,23,42,0.98)_100%)]'>
      <div className='pointer-events-none absolute inset-0'>
        <div className='absolute left-[-6rem] top-[-7rem] h-72 w-72 rounded-full bg-cyan-400/20 blur-3xl dark:bg-cyan-500/20' />
        <div className='absolute right-[-8rem] top-[10%] h-96 w-96 rounded-full bg-amber-300/20 blur-3xl dark:bg-amber-400/10' />
        <div className='absolute bottom-[-10rem] left-[18%] h-96 w-96 rounded-full bg-sky-200/30 blur-3xl dark:bg-sky-500/10' />
        <div className='absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(14,165,233,0.08),_transparent_36%),linear-gradient(rgba(148,163,184,0.07)_1px,_transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.07)_1px,_transparent_1px)] bg-[size:auto,72px_72px,72px_72px] [mask-image:linear-gradient(180deg,rgba(0,0,0,0.8),transparent_92%)] dark:bg-[radial-gradient(circle_at_top,_rgba(34,211,238,0.14),_transparent_36%),linear-gradient(rgba(148,163,184,0.07)_1px,_transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.07)_1px,_transparent_1px)]' />
      </div>

      <div className='relative mx-auto flex min-h-screen w-full max-w-7xl flex-col px-4 py-6 sm:px-6 lg:px-8'>
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

        <div className='relative flex flex-1 items-center py-10 lg:py-14'>
          <div className='grid w-full gap-8 lg:grid-cols-[1.08fr_0.92fr] lg:gap-10'>
            <section className='order-2 flex flex-col justify-between gap-6 lg:order-1 lg:pr-8'>
              <div className='max-w-2xl space-y-6'>
                <Badge className='rounded-full border border-cyan-500/20 bg-cyan-500/10 px-4 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-cyan-700 dark:border-cyan-400/20 dark:bg-cyan-400/10 dark:text-cyan-200'>
                  {headerCopy.badge}
                </Badge>

                <div className='space-y-4'>
                  <h1 className='max-w-xl text-4xl font-semibold leading-tight tracking-[-0.04em] text-balance text-slate-950 dark:text-slate-50 sm:text-5xl'>
                    {headerCopy.title}
                  </h1>
                  <p className='max-w-2xl text-base leading-8 text-slate-600 dark:text-slate-300 sm:text-lg'>
                    {headerCopy.description}
                  </p>
                </div>

                <div className='flex flex-wrap gap-3'>
                  {TRUST_MARKERS.map((item) => (
                    <div
                      key={item}
                      className='inline-flex items-center gap-2 rounded-full border border-white/70 bg-white/85 px-3.5 py-2 text-sm text-slate-700 shadow-[0_16px_36px_-28px_rgba(15,23,42,0.45)] backdrop-blur dark:border-white/10 dark:bg-slate-900/70 dark:text-slate-200'
                    >
                      <CheckCircle2 className='h-4 w-4 text-emerald-600 dark:text-emerald-400' />
                      <span>{item}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className='grid gap-4 sm:grid-cols-3'>
                {VALUE_PILLARS.map((pillar) => (
                  <article
                    key={pillar.title}
                    className='rounded-[1.75rem] border border-white/70 bg-white/82 p-5 shadow-[0_26px_60px_-40px_rgba(15,23,42,0.45)] backdrop-blur dark:border-white/10 dark:bg-slate-900/72'
                  >
                    <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-sm dark:bg-white dark:text-slate-950'>
                      <pillar.icon className='h-5 w-5' />
                    </div>
                    <h2 className='mt-4 text-base font-semibold text-slate-950 dark:text-slate-50'>
                      {pillar.title}
                    </h2>
                    <p className='mt-2 text-sm leading-6 text-slate-600 dark:text-slate-300'>
                      {pillar.description}
                    </p>
                  </article>
                ))}
              </div>
            </section>

            <section className='order-1 lg:order-2 lg:pl-4'>
              <Card className='overflow-hidden rounded-[2rem] border-white/70 bg-white/88 py-0 shadow-[0_40px_120px_-48px_rgba(15,23,42,0.45)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/78'>
                <div className='border-b border-border/60 px-6 py-5 sm:px-8'>
                  <div className='flex items-start justify-between gap-4'>
                    <div className='space-y-2'>
                      <h2 className='text-2xl font-semibold tracking-[-0.03em] text-slate-950 dark:text-slate-50'>
                        {mode === 'login' ? 'Sign in' : 'Create account'}
                      </h2>
                      <p className='max-w-md text-sm leading-6 text-muted-foreground'>
                        {mode === 'login'
                          ? 'Continue where your team left off with a focused sign-in flow.'
                          : 'Start your organization with a polished onboarding experience.'}
                      </p>
                    </div>
                  </div>
                </div>

                <CardContent className='space-y-6 px-6 py-6 sm:px-8 sm:py-8'>
                  <Tabs
                    value={mode}
                    onValueChange={(value) =>
                      handleModeChange(value as AuthMode)
                    }
                    className='space-y-6'
                  >
                    <TabsList className='grid h-auto grid-cols-2 rounded-2xl border border-border/70 bg-muted/45 p-1'>
                      <TabsTrigger
                        value='login'
                        disabled={isLocked}
                        className='rounded-xl px-4 py-3 text-sm font-semibold data-[state=active]:border-transparent data-[state=active]:bg-background data-[state=active]:shadow-sm'
                      >
                        Sign in
                      </TabsTrigger>
                      <TabsTrigger
                        value='register'
                        disabled={isLocked}
                        className='rounded-xl px-4 py-3 text-sm font-semibold data-[state=active]:border-transparent data-[state=active]:bg-background data-[state=active]:shadow-sm'
                      >
                        Create account
                      </TabsTrigger>
                    </TabsList>

                    <TabsContent value='login' className='mt-0'>
                      <LoginForm
                        onSuccess={handleAuthSuccess}
                        onSwitchToRegister={() => handleModeChange('register')}
                      />
                    </TabsContent>

                    <TabsContent value='register' className='mt-0'>
                      <RegisterForm
                        onSuccess={handleAuthSuccess}
                        onSwitchToLogin={() => handleModeChange('login')}
                      />
                    </TabsContent>
                  </Tabs>

                  <Separator />

                  <Alert className='rounded-2xl border-border/70 bg-muted/30'>
                    <TimerReset className='h-4 w-4' />
                    <AlertTitle>Need help signing in?</AlertTitle>
                    <AlertDescription className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
                      <span>
                        If you already have a reset link from your
                        administrator, continue directly to the password
                        recovery flow.
                      </span>
                      <Button
                        variant='outline'
                        size='sm'
                        className='h-10 rounded-xl border-border/70 bg-background/70 px-4'
                        asChild
                      >
                        <Link href='/auth/reset-password'>
                          Reset password
                          <ArrowRight className='h-4 w-4' />
                        </Link>
                      </Button>
                    </AlertDescription>
                  </Alert>

                  <p className='text-center text-xs leading-6 text-muted-foreground'>
                    By continuing, you agree to SERP&apos;s terms of service and
                    privacy policy.
                  </p>
                </CardContent>
              </Card>
            </section>
          </div>
        </div>
      </div>
    </main>
  );
};
