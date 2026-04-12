'use client';

import dynamic from 'next/dynamic';
import { schoolBusUi } from '../../theme';

export const RouteMap = dynamic(() => import('./RouteMapClient'), {
  ssr: false,
  loading: () => (
    <div
      className={`flex h-[420px] items-center justify-center text-sm text-slate-500 ${schoolBusUi.mapFrame}`}
    >
      Loading route map...
    </div>
  ),
});
