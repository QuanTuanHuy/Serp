/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CRM authentication guard
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { RoleGuard } from '@/modules/account/components/RoleGuard';
import { CRM_ROLES, SYSTEM_ADMIN_ROLES } from '@/shared';

interface CRMAuthGuardProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  loading?: React.ReactNode;
  redirectTo?: string;
}

/**
 * CRMAuthGuard - Protects routes/components for CRM module
 */
export const CRMAuthGuard: React.FC<CRMAuthGuardProps> = ({
  children,
  fallback,
  loading,
  redirectTo = '/unauthorized',
}) => {
  const router = useRouter();

  // Custom fallback for CRM access denial
  const crmFallback = fallback || (
    <div className='flex flex-col items-center justify-center min-h-screen bg-background'>
      <div className='max-w-md w-full p-8 text-center space-y-4'>
        <div className='h-16 w-16 bg-destructive/10 rounded-full flex items-center justify-center mx-auto'>
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
          You need appropriate permissions to access the CRM module.
        </p>

        <div className='flex gap-3 justify-center pt-4'>
          <button
            onClick={() => router.back()}
            className='px-4 py-2 border rounded-md hover:bg-accent transition-colors'
          >
            Go Back
          </button>
          <button
            onClick={() => router.push('/home')}
            className='px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors'
          >
            Go to Home
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <RoleGuard
      roles={[...CRM_ROLES, ...SYSTEM_ADMIN_ROLES]}
      requireAllRoles={false}
      fallback={crmFallback}
      loading={loading}
      hideOnNoAccess={false}
    >
      {children}
    </RoleGuard>
  );
};

/**
 * HOC version for easier usage with page components
 */
export const withCRMAuth = <P extends object>(
  Component: React.ComponentType<P>
) => {
  const GuardedComponent = (props: P) => (
    <CRMAuthGuard>
      <Component {...props} />
    </CRMAuthGuard>
  );

  GuardedComponent.displayName = `withCRMAuth(${Component.displayName || Component.name})`;
  return GuardedComponent;
};
