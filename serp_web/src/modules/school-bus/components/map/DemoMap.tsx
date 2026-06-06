'use client';

import dynamic from 'next/dynamic';
import { schoolBusUi } from '../../theme';

export const DemoMap = dynamic(() => import('./DemoMapClient'), {
  ssr: false,
  loading: () => (
    <div
      className={`flex h-full items-center justify-center text-sm text-slate-500 ${schoolBusUi.mapFrame}`}
    >
      Loading demo map...
    </div>
  ),
});
