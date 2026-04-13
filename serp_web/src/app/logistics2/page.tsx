/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Logistics2 root redirect page
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function Logistics2RootPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/logistics2/delivery-plans');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading Logistics2...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to delivery plans
        </div>
      </div>
    </div>
  );
}
