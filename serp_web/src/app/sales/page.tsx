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
        <div className='text-lg font-semibold'>
          Chuyển hướng đến dashboard bán hàng
        </div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Đang chuyển hướng đến bảng điều khiển quản lý bán hàng, nếu bạn không
          được chuyển hướng tự động, vui lòng{' '}
          <a href='/sales/dashboard' className='text-primary underline'>
            nhấn vào đây
          </a>
          .
        </div>
      </div>
    </div>
  );
}
