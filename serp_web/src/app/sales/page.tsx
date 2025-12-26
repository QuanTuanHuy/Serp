/**
 * Sales Main Page - Redirects to Dashboard
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Sales main entry point
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function SalesPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/sales/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading Sales Dashboard...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your sales management dashboard
        </div>
      </div>
    </div>
  );
}
