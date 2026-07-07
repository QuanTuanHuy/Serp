'use client';

import dynamic from 'next/dynamic';

export const RouteMap = dynamic(() => import('./RouteMapClient'), {
  ssr: false,
  loading: () => (
    <div className='flex h-full min-h-[320px] w-full items-center justify-center text-sm text-slate-500'>
      Đang tải tuyến map...
    </div>
  ),
});
