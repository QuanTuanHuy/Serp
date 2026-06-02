'use client';

import dynamic from 'next/dynamic';
import type { LocationPickerMapClientProps } from './LocationPickerMapClient';
import { schoolBusUi } from '../../theme';

export const LocationPickerMap = dynamic<LocationPickerMapClientProps>(
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
