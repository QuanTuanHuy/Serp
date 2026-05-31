'use client';

import dynamic from 'next/dynamic';
import { schoolBusUi } from '../../theme';

export const LocationPickerMap = dynamic(
  () => import('./LocationPickerMapClient'),
  {
    ssr: false,
    loading: () => (
      <div
        className={`flex h-[320px] items-center justify-center text-sm text-slate-500 ${schoolBusUi.mapFrame}`}
      >
        Loading map...
      </div>
    ),
  }
);
