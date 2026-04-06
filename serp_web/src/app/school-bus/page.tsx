'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function SchoolBusPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/school-bus/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading School Bus Module...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your operations dashboard
        </div>
      </div>
    </div>
  );
}
