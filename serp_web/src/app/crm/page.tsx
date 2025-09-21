/**
 * CRM Main Page - Redirects to Dashboard
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - CRM main entry point
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function CRMPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/crm/dashboard');
  }, [router]);

  return (
    <div className='flex h-full items-center justify-center'>
      <div className='text-center'>
        <div className='text-lg font-semibold'>Loading CRM Dashboard...</div>
        <div className='mt-2 text-sm text-muted-foreground'>
          Redirecting to your customer relationship management dashboard
        </div>
      </div>
    </div>
  );
}
