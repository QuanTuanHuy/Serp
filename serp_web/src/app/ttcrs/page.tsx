'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { useAppSelector } from '@/shared/hooks';
import { selectUserProfile } from '@/modules/account/store';
import { getTtcrsEntryRoute } from '@/modules/ttcrs/utils/roleRouting';

export default function TtcrsPage() {
  const router = useRouter();
  const user = useAppSelector(selectUserProfile);

  useEffect(() => {
    if (user !== null) {
      router.replace(getTtcrsEntryRoute(user.roles));
    }
  }, [router, user]);

  return (
    <div className='flex items-center justify-center min-h-screen'>
      <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
    </div>
  );
}
