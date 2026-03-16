/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Modern password reset landing page
 */

import { CheckCircle2, ShieldCheck, TimerReset } from 'lucide-react';

import { ResetPasswordForm } from '@/modules/account';
import { Badge, ThemeToggle } from '@/shared/components/ui';

import { BrandMark } from '@/modules/account/components/auth/auth-ui';

const RECOVERY_NOTES = [
  {
    title: 'Short-lived access',
    description:
      'Every recovery link is time-limited so the reset flow stays private and safe.',
    icon: TimerReset,
  },
  {
    title: 'Session protection',
    description:
      'Saving a new password invalidates older sessions to keep your account protected.',
    icon: ShieldCheck,
  },
  {
    title: 'Back to work quickly',
    description:
      'Once confirmed, return to sign in and continue with your new credentials.',
    icon: CheckCircle2,
  },
] as const;

interface ResetPasswordPageProps {
  searchParams: Promise<{
    token?: string | string[];
  }>;
}

export default async function ResetPasswordPage({
  searchParams,
}: ResetPasswordPageProps) {
  const { token } = await searchParams;
  const normalizedToken = Array.isArray(token) ? token[0] : (token ?? null);

  return (
    <main className='relative isolate min-h-screen overflow-hidden bg-[linear-gradient(180deg,_rgba(255,255,255,0.96)_0%,_rgba(241,245,249,0.98)_100%)] px-4 py-6 text-foreground dark:bg-[linear-gradient(180deg,_rgba(2,6,23,0.98)_0%,_rgba(15,23,42,0.98)_100%)] sm:px-6 lg:px-8'>
      <div className='pointer-events-none absolute inset-0'>
        <div className='absolute left-[-4rem] top-[-6rem] h-72 w-72 rounded-full bg-cyan-400/20 blur-3xl dark:bg-cyan-500/20' />
        <div className='absolute right-[-7rem] top-[12%] h-80 w-80 rounded-full bg-amber-300/20 blur-3xl dark:bg-amber-400/12' />
        <div className='absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(14,165,233,0.08),_transparent_36%),linear-gradient(rgba(148,163,184,0.07)_1px,_transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.07)_1px,_transparent_1px)] bg-[size:auto,72px_72px,72px_72px] [mask-image:linear-gradient(180deg,rgba(0,0,0,0.8),transparent_92%)] dark:bg-[radial-gradient(circle_at_top,_rgba(34,211,238,0.12),_transparent_36%),linear-gradient(rgba(148,163,184,0.07)_1px,_transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.07)_1px,_transparent_1px)]' />
      </div>

      <div className='relative mx-auto flex min-h-screen w-full max-w-7xl flex-col'>
        <header className='flex items-center justify-between gap-4 py-2'>
          <BrandMark />
          <ThemeToggle />
        </header>

        <div className='flex flex-1 items-center py-8 lg:py-12'>
          <div className='grid w-full gap-8 lg:grid-cols-[1.02fr_0.98fr] lg:gap-10'>
            <section className='flex flex-col justify-center gap-6 lg:pr-8'>
              <div className='max-w-2xl space-y-5'>
                <Badge className='w-fit rounded-full border border-primary/15 bg-primary/10 px-4 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-primary'>
                  Secure workspace recovery
                </Badge>
                <h1 className='max-w-xl text-4xl font-semibold leading-tight tracking-[-0.04em] text-balance text-slate-950 dark:text-slate-50 sm:text-5xl'>
                  Regain access with a fresh, secure password.
                </h1>
                <p className='max-w-2xl text-base leading-8 text-slate-600 dark:text-slate-300 sm:text-lg'>
                  This recovery flow is issued by your organization
                  administrator. Reset your password once, invalidate existing
                  sessions, and get back into SERP confidently.
                </p>
              </div>

              <div className='grid gap-4 sm:grid-cols-3'>
                {RECOVERY_NOTES.map((note) => (
                  <article
                    key={note.title}
                    className='rounded-[1.75rem] border border-white/70 bg-white/82 p-5 shadow-[0_26px_60px_-40px_rgba(15,23,42,0.45)] backdrop-blur dark:border-white/10 dark:bg-slate-900/72'
                  >
                    <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-950 text-white dark:bg-white dark:text-slate-950'>
                      <note.icon className='h-5 w-5' />
                    </div>
                    <h2 className='mt-4 text-base font-semibold text-slate-950 dark:text-slate-50'>
                      {note.title}
                    </h2>
                    <p className='mt-2 text-sm leading-6 text-slate-600 dark:text-slate-300'>
                      {note.description}
                    </p>
                  </article>
                ))}
              </div>
            </section>

            <section className='flex items-center'>
              <ResetPasswordForm token={normalizedToken} />
            </section>
          </div>
        </div>
      </div>
    </main>
  );
}
