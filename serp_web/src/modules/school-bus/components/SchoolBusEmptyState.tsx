'use client';

import type { LucideIcon } from 'lucide-react';
import { Inbox } from 'lucide-react';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

interface SchoolBusEmptyStateProps {
  title: string;
  description: string;
  icon?: LucideIcon;
  className?: string;
}

export function SchoolBusEmptyState({
  title,
  description,
  icon: Icon = Inbox,
  className,
}: SchoolBusEmptyStateProps) {
  return (
    <div
      className={cn(
        'flex min-h-[220px] flex-col items-center justify-center rounded-[28px] border border-dashed border-slate-200 bg-slate-50/40 px-6 text-center',
        className
      )}
    >
      <div className='flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-slate-600 shadow-sm ring-1 ring-slate-100'>
        <Icon className='h-5 w-5' />
      </div>
      <div className='mt-4 space-y-2'>
        <p className={cn('text-base', schoolBusUi.heading)}>{title}</p>
        <p className={cn('max-w-md text-sm leading-6', schoolBusUi.mutedText)}>
          {description}
        </p>
      </div>
    </div>
  );
}
