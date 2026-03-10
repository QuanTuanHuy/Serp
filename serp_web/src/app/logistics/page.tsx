/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Module Redirect
*/

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function LogisticsPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/logistics/inventory');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading Inventory...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your inventory management page
        </div>
      </div>
    </div>
  );
}
