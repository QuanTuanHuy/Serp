'use client';

import type { ReactNode } from 'react';
import { cn } from '@/shared/utils';
import { SchoolBusPageHeader } from './layout/SchoolBusPageHeader';

interface SchoolBusPageShellProps {
  title: string;
  description?: ReactNode;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
  /** Compact variant: smaller hero, tighter spacing - used for workspace-heavy pages. */
  compact?: boolean;
  /**
   * Optional breadcrumb rendered above the module label in the hero card.
   * Accepts a <SchoolBusBreadcrumb> node.
   */
  breadcrumb?: ReactNode;
}

/**
 * SchoolBusPageShell
 *
 * Standard page wrapper for the school-bus module.
 * Integrates SchoolBusPageHeader for flat visual style.
 */
export function SchoolBusPageShell({
  title,
  description,
  actions,
  children,
  className,
  compact = false,
  breadcrumb,
}: SchoolBusPageShellProps) {
  return (
    <div
      className={cn(
        'flex flex-col gap-6 bg-transparent',
        compact && 'gap-4',
        className
      )}
    >
      <SchoolBusPageHeader
        title={title}
        description={description}
        breadcrumb={breadcrumb}
        actions={actions}
        compact={compact}
      />

      <div className='w-full'>{children}</div>
    </div>
  );
}

