'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSchoolBusAccess } from '@/modules/school-bus/security/schoolBusAccess';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';

export default function SchoolBusPage() {
  const router = useRouter();
  const user = useAppSelector(selectUserProfile);
  const access = useSchoolBusAccess();

  useEffect(() => {
    // Wait until we know the user's roles before redirecting
    if (user !== null) {
      router.replace(access.landingPath);
    }
  }, [router, user, access.landingPath]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading School Bus Module...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your workspace...
        </div>
      </div>
    </div>
  );
}
