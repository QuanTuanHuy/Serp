'use client';

import * as React from 'react';
import { cn } from '@/shared/utils';
import { Users, CheckCircle2, AlertTriangle, Route } from 'lucide-react';

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
        'flex items-center gap-0 divide-x divide-slate-100 border-b border-slate-250 bg-white sticky top-0 z-10 h-11 shrink-0',
        className
      )}
    >
      <KpiItem
        label='Đủ điều kiện'
        value={eligible}
        icon={Users}
        color='text-violet-600'
        bgColor='bg-violet-50/20'
      />
      <KpiItem
        label='Đã lập tuyến'
        value={planned}
        icon={CheckCircle2}
        color='text-emerald-600'
        bgColor='bg-emerald-50/20'
      />
      <KpiItem
        label='Chưa gán'
        value={unassigned}
        icon={AlertTriangle}
        color={unassigned > 0 ? 'text-amber-600' : 'text-slate-400'}
        bgColor={unassigned > 0 ? 'bg-amber-50/30' : undefined}
      />
      <KpiItem
        label='Tuyến'
        value={routes}
        icon={Route}
        color='text-blue-600'
        bgColor='bg-blue-50/20'
      />
    </div>
  );
}

function KpiItem({
  label,
  value,
  icon: Icon,
  color,
  bgColor,
}: {
  label: string;
  value: number;
  icon: React.ComponentType<{ className?: string }>;
  color: string;
  bgColor?: string;
}) {
  return (
    <div
      className={cn(
        'flex flex-1 items-center justify-center gap-2 h-full px-3 py-1 transition-colors duration-200',
        bgColor
      )}
    >
      <Icon className={cn('h-4 w-4 shrink-0', color)} />
      <div className='flex items-baseline gap-1.5 min-w-0'>
        <span
          className={cn(
            'text-sm font-extrabold leading-none tabular-nums',
            color
          )}
        >
          {value}
        </span>
        <span className='text-[10px] font-bold uppercase tracking-wider text-slate-400 truncate'>
          {label}
        </span>
      </div>
    </div>
  );
}
