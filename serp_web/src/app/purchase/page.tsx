'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function PurchasePage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/purchase/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>
          Chuyển hướng đến dashboard mua hàng...
        </div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Đang chuyển hướng, nếu bạn không được chuyển hướng tự động, vui lòng{' '}
          <a href='/purchase/dashboard' className='text-primary underline'>
            nhấn vào đây
          </a>
          .
        </div>
      </div>
    </div>
  );
}
