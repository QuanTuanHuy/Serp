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
    router.replace('/logistics2/routes');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading Logistics Module</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Chuyển hướng đến trang bắt đầu...
        </div>
      </div>
    </div>
  );
}
