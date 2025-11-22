/*
Author: QuanTuanHuy
Description: Part of Serp Project - Purchase authentication guard
*/

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { RoleGuard } from '@/modules/account/components/RoleGuard';
import { PURCHASE_ROLES, SYSTEM_ADMIN_ROLES } from '@/shared';

interface PurchaseAuthGuardProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  loading?: React.ReactNode;
  redirectTo?: string;
}

/**
 * PurchaseAuthGuard - Protects routes/components for Purchase module
 */
export const PurchaseAuthGuard: React.FC<PurchaseAuthGuardProps> = ({
  children,
  fallback,
  loading,
  redirectTo = '/unauthorized',
}) => {
  const router = useRouter();

  // Custom fallback for Purchase access denial
  const purchaseFallback = fallback || (
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

        <h1 className='text-2xl font-bold text-foreground'>
          Access Denied Purchase
        </h1>

        <p className='text-muted-foreground'>
          You don&apos;t have permission to access the Purchase Management module.
          Please contact your administrator to request access.
        </p>

        <div className='flex flex-col sm:flex-row gap-3 justify-center pt-4'>
          <button
            onClick={() => router.push('/home')}
            className='px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors'
          >
            Go to Home
          </button>
          <button
            onClick={() => router.back()}
            className='px-4 py-2 bg-secondary text-secondary-foreground rounded-md hover:bg-secondary/90 transition-colors'
          >
            Go Back
          </button>
        </div>
      </div>
    </div>
  );

  const purchaseLoading = loading || (
    <div className='flex items-center justify-center min-h-screen bg-background'>
      <div className='flex flex-col items-center space-y-4'>
        <div className='h-12 w-12 border-4 border-primary border-t-transparent rounded-full animate-spin' />
        <p className='text-sm text-muted-foreground'>Verifying access...</p>
      </div>
    </div>
  );

  return (
    <RoleGuard
      roles={[...PURCHASE_ROLES, ...SYSTEM_ADMIN_ROLES]}
      fallback={purchaseFallback}
      loading={purchaseLoading}
      redirectTo={redirectTo}
    >
      {children}
    </RoleGuard>
  );
};
