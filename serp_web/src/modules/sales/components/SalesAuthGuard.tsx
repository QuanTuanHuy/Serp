/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Sales Auth Guard
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/lib/store';

interface SalesAuthGuardProps {
  children: React.ReactNode;
}

/**
 * SalesAuthGuard - Protects sales routes with authentication
 */
export const SalesAuthGuard: React.FC<SalesAuthGuardProps> = ({ children }) => {
  const router = useRouter();
  const isAuthenticated = useAppSelector((state) => !!state.account.auth.token);

  React.useEffect(() => {
    if (!isAuthenticated) {
      router.push('/auth/login?redirect=/sales');
    }
  }, [isAuthenticated, router]);

  if (!isAuthenticated) {
    return (
      <div className='flex h-screen items-center justify-center'>
        <div className='text-center'>
          <div className='text-lg font-semibold'>Verifying access...</div>
          <div className='mt-2 text-sm text-muted-foreground'>
            Please wait while we check your permissions
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};
