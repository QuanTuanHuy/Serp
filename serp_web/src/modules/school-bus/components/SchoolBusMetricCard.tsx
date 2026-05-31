'use client';

import type { LucideIcon } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

type MetricTone = 'default' | 'info' | 'success' | 'warning';

interface SchoolBusMetricCardProps {
  label: string;
  value: number | string;
  hint?: string;
  icon?: LucideIcon;
  tone?: MetricTone;
  variant?: 'comfortable' | 'compact';
}

const toneClasses: Record<MetricTone, string> = {
  default: 'bg-slate-50 text-slate-600 ring-slate-100',
  info: 'bg-blue-50 text-blue-700 ring-blue-100/70',
  success: 'bg-emerald-50 text-emerald-700 ring-emerald-100/70',
  warning: 'bg-amber-50 text-amber-700 ring-amber-100/70',
};

export function SchoolBusMetricCard({
  label,
  value,
  hint,
  icon: Icon,
  tone = 'default',
  variant = 'comfortable',
}: SchoolBusMetricCardProps) {
  if (variant === 'compact') {
    return (
      <Card className='py-0 shadow-sm border border-slate-200/80 rounded-2xl bg-white'>
        <CardContent className='flex items-center gap-3 p-3'>
          {Icon ? (
            <div
              className={cn(
                'flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ring-1',
                toneClasses[tone]
              )}
            >
              <Icon className='h-4 w-4' />
            </div>
          ) : null}
          <div className='min-w-0 flex-1 leading-none'>
            <p className={cn('text-[11px] truncate', schoolBusUi.mutedText)}>{label}</p>
            <p className={cn('text-lg font-bold mt-1', schoolBusUi.heading)}>{value}</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={cn('py-0', schoolBusUi.card)}>
      <CardContent className='flex items-start justify-between gap-4 p-5'>
        <div className='space-y-2'>
          <p className={cn('text-sm', schoolBusUi.mutedText)}>{label}</p>
          <p className={cn('text-3xl font-semibold', schoolBusUi.heading)}>{value}</p>
          {hint ? (
            <p className={cn('text-xs leading-5', schoolBusUi.mutedText)}>
              {hint}
            </p>
          ) : null}
        </div>
        {Icon ? (
          <div
            className={cn(
              'flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ring-1',
              toneClasses[tone]
            )}
          >
            <Icon className='h-5 w-5' />
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}
