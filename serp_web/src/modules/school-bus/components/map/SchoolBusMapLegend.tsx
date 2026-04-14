'use client';

import { cn } from '@/shared/utils';
import { schoolBusBrand } from '../../theme';

interface SchoolBusMapLegendProps {
  className?: string;
}

export function SchoolBusMapLegend({ className }: SchoolBusMapLegendProps) {
  return (
    <div
      className={cn(
        'rounded-2xl border border-slate-200 bg-white/95 p-3 shadow-[0_12px_28px_rgba(15,23,42,0.08)]',
        className
      )}
    >
      <p className='text-xs font-semibold uppercase tracking-[0.14em] text-slate-500'>
        Map legend
      </p>
      <div className='mt-3 grid gap-2 text-xs text-slate-700'>
        <LegendRow label='Start point' token='A' tone='rose' />
        <LegendRow label='End point' token='Z' tone='rose' />
        <LegendRow label='School' token='S' tone='rose' />
        <LegendRow label='Depot' token='D' tone='amber' />
        <LegendRow label='Pickup stop' token='P' tone='sky' />
        <LegendRow label='Drop-off stop' token='R' tone='emerald' />
        <LegendRow label='Bus context' token='B' tone='indigo' />
      </div>
      <div className='mt-3 space-y-1 text-xs'>
        <p className='flex items-center gap-2 text-rose-700'>
          <span
            className='block h-[2px] w-8 rounded-full'
            style={{ backgroundColor: schoolBusBrand.rose }}
          />
          Outbound route
        </p>
        <p className='flex items-center gap-2 text-emerald-700'>
          <span
            className='block h-[2px] w-8 rounded-full border-t-2 border-dashed'
            style={{ borderColor: schoolBusBrand.emerald }}
          />
          Return route
        </p>
      </div>
    </div>
  );
}

function LegendRow({
  label,
  token,
  tone,
}: {
  label: string;
  token: string;
  tone: 'rose' | 'amber' | 'sky' | 'emerald' | 'indigo';
}) {
  const colors: Record<typeof tone, { bg: string; border: string; text: string }> = {
    rose: { bg: '#fff1f2', border: '#be123c', text: '#be123c' },
    amber: { bg: '#fffbeb', border: '#b45309', text: '#b45309' },
    sky: { bg: '#eff6ff', border: '#0369a1', text: '#0369a1' },
    emerald: { bg: '#ecfdf5', border: '#047857', text: '#047857' },
    indigo: { bg: '#eef2ff', border: '#4338ca', text: '#4338ca' },
  };
  const color = colors[tone];
  return (
    <p className='flex items-center gap-2'>
      <span
        className='inline-flex h-5 w-5 items-center justify-center rounded-full border text-[10px] font-bold'
        style={{
          backgroundColor: color.bg,
          borderColor: color.border,
          color: color.text,
        }}
      >
        {token}
      </span>
      {label}
    </p>
  );
}
