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
}

const toneClasses: Record<MetricTone, string> = {
  default: 'bg-slate-100 text-slate-700 ring-slate-200',
  info: 'bg-rose-50 text-rose-700 ring-rose-200',
  success: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  warning: 'bg-amber-50 text-amber-700 ring-amber-200',
};

export function SchoolBusMetricCard({
  label,
  value,
  hint,
  icon: Icon,
  tone = 'default',
}: SchoolBusMetricCardProps) {
  return (
    <Card className={cn('py-0', schoolBusUi.card)}>
      <CardContent className='flex items-start justify-between gap-4 p-5'>
        <div className='space-y-2'>
          <p className={cn('text-sm', schoolBusUi.mutedText)}>{label}</p>
          <p className={cn('text-3xl', schoolBusUi.heading)}>{value}</p>
          {hint ? (
            <p className={cn('text-xs leading-5', schoolBusUi.mutedText)}>
              {hint}
            </p>
          ) : null}
        </div>
        {Icon ? (
          <div
            className={cn(
              'flex h-11 w-11 items-center justify-center rounded-2xl ring-1',
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
