/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM root redirect
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function PMPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/pm/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading…</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to Project Management
        </div>
      </div>
    </div>
  );
}
