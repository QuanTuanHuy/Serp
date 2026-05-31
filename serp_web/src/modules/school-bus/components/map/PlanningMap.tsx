'use client';

import dynamic from 'next/dynamic';
import { schoolBusUi } from '../../theme';

export const PlanningMap = dynamic(() => import('./PlanningMapClient'), {
  ssr: false,
  loading: () => (
    <div
      className={`flex h-[420px] items-center justify-center text-sm text-slate-500 ${schoolBusUi.mapFrame}`}
    >
      Loading planning map...
    </div>
  ),
});
