'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/lib/store';
import { schoolBusThemeStyle, schoolBusUi } from '../theme';

interface SchoolBusAuthGuardProps {
  children: React.ReactNode;
}

export const SchoolBusAuthGuard: React.FC<SchoolBusAuthGuardProps> = ({
  children,
}) => {
  const router = useRouter();
  const isAuthenticated = useAppSelector((state) => !!state.account.auth.token);

  React.useEffect(() => {
    if (!isAuthenticated) {
      router.push('/auth?redirect=/school-bus/dashboard');
    }
  }, [isAuthenticated, router]);

  if (!isAuthenticated) {
    return (
      <div
        className={`flex h-screen items-center justify-center ${schoolBusUi.pageGradient}`}
        style={schoolBusThemeStyle}
      >
        <div className={schoolBusUi.section}>
          <div className='text-center text-lg font-semibold text-slate-950'>
            Verifying access...
          </div>
          <div className='mt-2 text-center text-sm text-slate-500'>
            Please wait while we check your school bus permissions
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};
