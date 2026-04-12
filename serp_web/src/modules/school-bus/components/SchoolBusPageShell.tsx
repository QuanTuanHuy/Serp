'use client';

import type { ReactNode } from 'react';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

interface SchoolBusPageShellProps {
  eyebrow?: string;
  title: string;
  description: string;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
}

export function SchoolBusPageShell({
  eyebrow = 'Bus Dispatch System',
  title,
  description,
  actions,
  children,
  className,
}: SchoolBusPageShellProps) {
  return (
    <div className={cn('space-y-6', className)}>
      <div className={cn('flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between', schoolBusUi.hero)}>
        <div className='pointer-events-none absolute -right-16 -top-20 h-48 w-48 rounded-full bg-rose-100/80 blur-3xl' />
        <div className='pointer-events-none absolute bottom-0 right-10 h-24 w-24 rounded-full bg-sky-100/70 blur-2xl' />
        <div className='relative space-y-3'>
          <p className={schoolBusUi.eyebrow}>
            {eyebrow}
          </p>
          <div className='space-y-2'>
            <h2 className={cn('text-3xl', schoolBusUi.heading)}>{title}</h2>
            <p className={cn('max-w-3xl text-sm leading-6', schoolBusUi.mutedText)}>
              {description}
            </p>
          </div>
        </div>
        {actions ? (
          <div className='relative flex flex-wrap items-center gap-3'>{actions}</div>
        ) : null}
      </div>
      {children}
    </div>
  );
}
