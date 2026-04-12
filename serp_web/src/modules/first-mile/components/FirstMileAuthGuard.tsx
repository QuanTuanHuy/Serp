/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile auth guard
 */

'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/lib/store';

interface FirstMileAuthGuardProps {
  children: React.ReactNode;
}

export const FirstMileAuthGuard: React.FC<FirstMileAuthGuardProps> = ({
  children,
}) => {
  const router = useRouter();
  const isAuthenticated = useAppSelector((state) => !!state.account.auth.token);

  React.useEffect(() => {
    if (!isAuthenticated) {
      router.push('/auth/login?redirect=/first-mile');
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
