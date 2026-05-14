/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Legacy path; redirects to user notification settings
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function SettingsNotificationsRedirectPage() {
  const router = useRouter();
  useEffect(() => {
    router.replace('/notifications/settings');
  }, [router]);
  return (
    <div className='flex min-h-[40vh] items-center justify-center text-sm text-muted-foreground'>
      Đang chuyển hướng…
    </div>
  );
}
