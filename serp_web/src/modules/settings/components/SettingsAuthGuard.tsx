/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings authentication guard for Organization Admin
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import {
  RoleGuard,
  RoleGuardProps,
} from '@/modules/account/components/RoleGuard';
import { ORGANIZATION_ADMIN_ROLES } from '@/shared';

interface SettingsAuthGuardProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  loading?: React.ReactNode;
  redirectTo?: string;
}

/**
 * SettingsAuthGuard - Protects routes/components for Organization Admin only
 * Requires ORG_OWNER or ORG_ADMIN role
 *
 * Usage:
 * ```tsx
 * <SettingsAuthGuard>
 *   <SettingsContent />
 * </SettingsAuthGuard>
 * ```
 */
export const SettingsAuthGuard: React.FC<SettingsAuthGuardProps> = ({
  children,
  fallback,
  loading,
  redirectTo = '/unauthorized',
}) => {
  const router = useRouter();

  // Custom fallback for settings access denial
  const settingsFallback = fallback || (
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
              d='M12 15v2m-6 0h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z'
            />
          </svg>
        </div>

        <h1 className='text-2xl font-bold'>Access Denied</h1>

        <p className='text-muted-foreground'>
          You need Organization Administrator privileges to access settings.
          <br />
          Required role: <span className='font-semibold'>
            ORG_OWNER
          </span> or <span className='font-semibold'>ORG_ADMIN</span>
        </p>

        <div className='flex gap-3 justify-center pt-4'>
          <button
            onClick={() => router.back()}
            className='px-4 py-2 border rounded-md hover:bg-accent transition-colors'
          >
            Go Back
          </button>
          <button
            onClick={() => router.push('/')}
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
      roles={ORGANIZATION_ADMIN_ROLES}
      requireAllRoles={false}
      fallback={settingsFallback}
      loading={loading}
      hideOnNoAccess={false}
    >
      {children}
    </RoleGuard>
  );
};

/**
 * HOC version for easier usage with page components
 *
 * Usage:
 * ```tsx
 * export default withSettingsAuth(SettingsPage);
 * ```
 */
export const withSettingsAuth = <P extends object>(
  Component: React.ComponentType<P>
) => {
  const GuardedComponent = (props: P) => (
    <SettingsAuthGuard>
      <Component {...props} />
    </SettingsAuthGuard>
  );

  GuardedComponent.displayName = `withSettingsAuth(${Component.displayName || Component.name})`;
  return GuardedComponent;
};
