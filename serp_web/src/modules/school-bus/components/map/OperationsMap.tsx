'use client';

import dynamic from 'next/dynamic';
import { schoolBusUi } from '../../theme';

export const OperationsMap = dynamic(() => import('./OperationsMapClient'), {
  ssr: false,
  loading: () => (
    <div
      className={`flex h-[420px] items-center justify-center text-sm text-slate-500 ${schoolBusUi.mapFrame}`}
    >
      Đang tải bản đồ vận hành...
    </div>
  ),
});
