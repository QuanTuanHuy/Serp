'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { api, useAppDispatch, useAppSelector } from '@/lib/store';
import { schoolBusThemeStyle, schoolBusUi } from '../theme';

interface SchoolBusAuthGuardProps {
  children: React.ReactNode;
}

export const SchoolBusAuthGuard: React.FC<SchoolBusAuthGuardProps> = ({
  children,
}) => {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const token = useAppSelector((state) => state.account.auth.token);
  const [preparedToken, setPreparedToken] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (!token) {
      setPreparedToken(null);
      router.push('/auth?redirect=/school-bus/dashboard');
      return;
    }

    dispatch(
      api.util.invalidateTags(['account/menus', 'account/modules'])
    );
    setPreparedToken(token);
  }, [dispatch, router, token]);

  if (!token || preparedToken !== token) {
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
