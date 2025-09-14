/**
 * PTM Main Page - Redirects to Dashboard
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM main entry point
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function PTMPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/ptm/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading PTM Dashboard...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your personal task management dashboard
        </div>
      </div>
    </div>
  );
}
