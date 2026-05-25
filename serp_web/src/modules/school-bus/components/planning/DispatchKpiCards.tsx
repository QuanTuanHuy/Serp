'use client';

import * as React from 'react';
import { cn } from '@/shared/utils';

interface DispatchKpiCardsProps {
  eligible: number;
  planned: number;
  unassigned: number;
  routes: number;
  className?: string;
}

export function DispatchKpiCards({
  eligible,
  planned,
  unassigned,
  routes,
  className,
}: DispatchKpiCardsProps) {
  return (
    <div
      className={cn(
        'flex items-center gap-0 divide-x divide-slate-200 border-b border-slate-200 bg-white',
        className
      )}
    >
      <KpiItem label='Eligible' value={eligible} color='text-slate-700' />
      <KpiItem
        label='Planned'
        value={planned}
        color='text-emerald-600'
        highlight={planned > 0 && planned === eligible}
      />
      <KpiItem
        label='Unassigned'
        value={unassigned}
        color={unassigned > 0 ? 'text-amber-600' : 'text-slate-400'}
        warn={unassigned > 0}
      />
      <KpiItem
        label='Routes'
        value={routes}
        color={routes > 0 ? 'text-sky-600' : 'text-slate-400'}
      />
    </div>
  );
}

function KpiItem({
  label,
  value,
  color,
  warn,
  highlight,
}: {
  label: string;
  value: number;
  color: string;
  warn?: boolean;
  highlight?: boolean;
}) {
  return (
    <div
      className={cn(
        'flex flex-1 flex-col items-center justify-center px-3 py-2',
        warn && 'bg-amber-50/60',
        highlight && 'bg-emerald-50/60'
      )}
    >
      <span className={cn('text-lg font-extrabold leading-none tabular-nums', color)}>
        {value}
      </span>
      <span className='mt-0.5 text-[10px] font-medium uppercase tracking-wide text-slate-400'>
        {label}
      </span>
    </div>
  );
}
