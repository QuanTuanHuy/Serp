'use client';

import type { LucideIcon } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

type MetricTone =
  | 'default'
  | 'info'
  | 'success'
  | 'warning'
  | 'school'
  | 'pickup'
  | 'linked'
  | 'student';

interface SchoolBusMetricCardProps {
  label: string;
  value: number | string;
  hint?: string;
  icon?: LucideIcon;
  tone?: MetricTone;
  variant?: 'comfortable' | 'compact';
}

const toneClasses: Record<MetricTone, string> = {
  default: 'bg-muted text-muted-foreground ring-border',
  info: 'bg-blue-50 text-blue-700 ring-blue-100/70 dark:bg-blue-950/40 dark:text-blue-300 dark:ring-blue-900',
  success:
    'bg-emerald-50 text-emerald-700 ring-emerald-100/70 dark:bg-emerald-950/40 dark:text-emerald-300 dark:ring-emerald-900',
  warning:
    'bg-amber-50 text-amber-700 ring-amber-100/70 dark:bg-amber-950/40 dark:text-amber-300 dark:ring-amber-900',
  school:
    'bg-red-50 text-[#C81E3A] ring-red-100/70 dark:bg-red-950/40 dark:text-red-300 dark:ring-red-900',
  pickup:
    'bg-blue-50 text-blue-700 ring-blue-100/70 dark:bg-blue-950/40 dark:text-blue-300 dark:ring-blue-900',
  linked:
    'bg-indigo-50 text-indigo-700 ring-indigo-100/70 dark:bg-indigo-950/40 dark:text-indigo-300 dark:ring-indigo-900',
  student:
    'bg-violet-50 text-violet-700 ring-violet-100/70 dark:bg-violet-950/40 dark:text-violet-300 dark:ring-violet-900',
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
      <Card className='rounded-[20px] border border-border bg-card py-0 text-card-foreground shadow-none'>
        <CardContent className='flex items-center gap-3 px-4 py-3'>
          {Icon ? (
            <div
              className={cn(
                'flex h-8 w-8 shrink-0 items-center justify-center rounded-xl ring-1',
                toneClasses[tone]
              )}
            >
              <Icon className='h-3.5 w-3.5' />
            </div>
          ) : null}
          <div className='min-w-0 flex-1 leading-none'>
            <p className={cn('text-[11px] truncate', schoolBusUi.mutedText)}>
              {label}
            </p>
            <p className={cn('text-lg font-bold mt-1.5', schoolBusUi.heading)}>
              {value}
            </p>
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
          <p className={cn('text-3xl font-semibold', schoolBusUi.heading)}>
            {value}
          </p>
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
