/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile root redirect page
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function FirstMileRootPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/first-mile/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading TMS...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to dashboard
        </div>
      </div>
    </div>
  );
}
