/**
 * Purchase Dashboard Page
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Main purchase dashboard
 */

'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function PurchasePage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/purchase/purchase-orders');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading Purchase Orders...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your purchase orders page
        </div>
      </div>
    </div>
  );
}
