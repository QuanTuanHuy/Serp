/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Project Management module auth guard
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { RoleGuard } from '@/modules/account/components/RoleGuard';
import { PM_ROLES, SYSTEM_ADMIN_ROLES } from '@/shared';

interface PMAuthGuardProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  loading?: React.ReactNode;
  redirectTo?: string;
}

export const PMAuthGuard: React.FC<PMAuthGuardProps> = ({
  children,
  fallback,
  loading,
  redirectTo = '/unauthorized',
}) => {
  const router = useRouter();

  const pmFallback = fallback || (
    <div className='flex min-h-screen flex-col items-center justify-center bg-background'>
      <div className='w-full max-w-md space-y-4 p-8 text-center'>
        <div className='mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-destructive/10'>
          <svg
            className='h-8 w-8 text-destructive'
            fill='none'
            viewBox='0 0 24 24'
            stroke='currentColor'
          >
            <path
              strokeLinecap='round'
              strokeLinejoin='round'
              strokeWidth={2}
              d='M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z'
            />
          </svg>
        </div>

        <h1 className='text-2xl font-bold'>Access Denied</h1>

        <p className='text-muted-foreground'>
          You need PM_USER or PM_ADMIN access to use Project Management.
        </p>

        <div className='flex justify-center gap-3 pt-4'>
          <button
            type='button'
            onClick={() => router.back()}
            className='rounded-md border px-4 py-2 transition-colors hover:bg-accent'
          >
            Go Back
          </button>
          <button
            type='button'
            onClick={() => router.push('/home')}
            className='rounded-md bg-primary px-4 py-2 text-primary-foreground transition-colors hover:bg-primary/90'
          >
            Go to Home
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <RoleGuard
      roles={[...PM_ROLES, ...SYSTEM_ADMIN_ROLES]}
      requireAllRoles={false}
      fallback={pmFallback}
      loading={loading}
      hideOnNoAccess={false}
      redirectTo={redirectTo}
    >
      {children}
    </RoleGuard>
  );
};
