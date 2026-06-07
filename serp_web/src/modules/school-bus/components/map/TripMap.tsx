'use client';

import dynamic from 'next/dynamic';

export const TripMap = dynamic(() => import('./TripMapClient'), {
  ssr: false,
  loading: () => (
    <div className='flex h-[420px] w-full items-center justify-center text-sm text-slate-500 bg-slate-50 border border-slate-200 rounded-2xl'>
      Loading trip map...
    </div>
  ),
});
