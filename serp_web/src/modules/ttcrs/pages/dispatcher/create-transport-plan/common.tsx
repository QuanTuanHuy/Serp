import React from 'react';
import { Badge, Skeleton, TableCell, TableRow } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type { RequestType } from '../../../types';
import { STEPS, TYPE_CLASS } from './constants';

export function StepIndicator({ step }: { step: 1 | 2 | 3 }) {
  return (
    <div className='mb-6 flex items-center gap-3'>
      {STEPS.map((item, index) => (
        <React.Fragment key={item.n}>
          {index > 0 && <div className='h-px max-w-16 flex-1 bg-border' />}
          <div className='flex items-center gap-2'>
            <div
              className={cn(
                'flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold',
                step === item.n
                  ? 'bg-orange-600 text-white'
                  : step > item.n
                    ? 'bg-orange-600/20 text-orange-600'
                    : 'bg-muted text-muted-foreground'
              )}
            >
              {step > item.n ? '✓' : item.n}
            </div>
            <span
              className={cn(
                'text-sm font-medium',
                step === item.n ? 'text-foreground' : 'text-muted-foreground'
              )}
            >
              {item.label}
            </span>
          </div>
        </React.Fragment>
      ))}
    </div>
  );
}

export function TypeBadge({ type }: { type: RequestType }) {
  return (
    <Badge
      className={cn(
        'rounded px-2 py-0.5 text-xs font-bold tracking-wide',
        TYPE_CLASS[type] ?? 'bg-gray-400 text-white border-transparent'
      )}
    >
      {type}
    </Badge>
  );
}

export function SkeletonRow({ cols }: { cols: number }) {
  return (
    <TableRow>
      {[...Array(cols)].map((_, index) => (
        <TableCell key={index}>
          <Skeleton className='h-4 w-full' />
        </TableCell>
      ))}
    </TableRow>
  );
}

export function VehicleStatusBadge({ status }: { status: string }) {
  const cls =
    status === 'AVAILABLE'
      ? 'bg-green-100 text-green-700 border-green-200'
      : status === 'IN_USE'
        ? 'bg-blue-100 text-blue-700 border-blue-200'
        : 'bg-amber-100 text-amber-700 border-amber-200';

  return (
    <Badge className={cn('rounded-full text-xs', cls)}>
      {status.replace('_', ' ')}
    </Badge>
  );
}
