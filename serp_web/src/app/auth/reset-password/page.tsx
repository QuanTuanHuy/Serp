/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Password reset landing page
 */

'use client';

import { useSearchParams } from 'next/navigation';

import { ResetPasswordForm } from '@/modules/account';

export default function ResetPasswordPage() {
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  return (
    <main className='min-h-screen bg-[radial-gradient(circle_at_top,_rgba(14,116,144,0.12),_transparent_38%),linear-gradient(180deg,_rgba(248,250,252,0.98)_0%,_rgba(241,245,249,0.96)_100%)] px-4 py-12 dark:bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.12),_transparent_38%),linear-gradient(180deg,_rgba(2,6,23,0.98)_0%,_rgba(15,23,42,0.98)_100%)] sm:px-6 lg:px-8'>
      <div className='mx-auto flex min-h-[calc(100vh-6rem)] w-full max-w-5xl items-center justify-center'>
        <div className='grid w-full gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-center'>
          <section className='hidden space-y-6 lg:block'>
            <div className='inline-flex items-center rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-sm font-medium text-primary'>
              Secure workspace recovery
            </div>
            <div className='space-y-4'>
              <h1 className='max-w-xl text-4xl font-semibold tracking-tight text-foreground'>
                Get back into SERP with a fresh, secure password.
              </h1>
              <p className='max-w-xl text-base leading-7 text-muted-foreground'>
                This reset link is issued by your organization administrator.
                Once you save a new password, all existing sessions are
                invalidated so your account stays protected.
              </p>
            </div>
            <div className='grid gap-4 sm:grid-cols-2'>
              <InfoCard
                title='Short-lived reset link'
                description='Each email contains a time-limited token so the reset flow stays private and safe.'
              />
              <InfoCard
                title='Immediate sign-in access'
                description='After confirmation, return to the login page and continue with your new credentials.'
              />
            </div>
          </section>

          <section>
            <ResetPasswordForm token={token} />
          </section>
        </div>
      </div>
    </main>
  );
}

function InfoCard({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className='rounded-2xl border border-border/60 bg-background/70 p-5 shadow-sm backdrop-blur'>
      <h2 className='text-sm font-semibold text-foreground'>{title}</h2>
      <p className='mt-2 text-sm leading-6 text-muted-foreground'>
        {description}
      </p>
    </div>
  );
}
