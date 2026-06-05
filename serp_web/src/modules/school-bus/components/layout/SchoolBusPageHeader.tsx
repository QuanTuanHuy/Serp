'use client';

import React, { ReactNode } from 'react';
import { cn } from '@/shared/utils';

interface SchoolBusPageHeaderProps {
  title: string;
  description?: ReactNode;
  breadcrumb?: ReactNode;
  actions?: ReactNode;
  compact?: boolean;
  className?: string;
}

/**
 * SchoolBusPageHeader
 *
 * Clean, flat page header component local to the school-bus module, aligned with SERP design patterns.
 * Displays breadcrumbs, title, optional description, and action buttons on the right.
 */
export function SchoolBusPageHeader({
  title,
  description,
  breadcrumb,
  actions,
  compact = false,
  className,
}: SchoolBusPageHeaderProps) {
  return (
    <div
      className={cn(
        'flex flex-col gap-1 border-b border-slate-200/60 pb-5 bg-transparent',
        compact && 'pb-3.5',
        className
      )}
    >
      {/* Main Title and Actions Row */}
      <div className='flex flex-wrap items-center justify-between gap-4'>
        <div className='min-w-0 flex-1'>
          <h1
            className={cn(
              'font-bold leading-tight tracking-tight text-slate-900',
              compact ? 'text-xl' : 'text-2xl sm:text-3xl'
            )}
          >
            {title}
          </h1>
          {description && (
            <div
              className={cn(
                'text-slate-500 leading-normal',
                compact ? 'text-xs mt-0.5' : 'text-sm mt-1.5 max-w-3xl'
              )}
            >
              {description}
            </div>
          )}
        </div>
        
        {actions && (
          <div className='flex shrink-0 flex-wrap items-center gap-2.5 sm:gap-3'>
            {actions}
          </div>
        )}
      </div>
    </div>
  );
}
