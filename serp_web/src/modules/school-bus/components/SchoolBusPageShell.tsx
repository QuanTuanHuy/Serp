'use client';

import type { ReactNode } from 'react';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

interface SchoolBusPageShellProps {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
  /** Compact variant: smaller hero, tighter spacing — used for workspace-heavy pages. */
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
 *
 * Hero layout (top → bottom):
 *   [breadcrumb row]        ← 8px bottom margin
 *   [module eyebrow label]  ← e.g. "BUS DISPATCH SYSTEM"
 *   [title]  [actions]      ← flex row, actions right-aligned & vertically centred
 *   [description]           ← optional, muted
 */
export function SchoolBusPageShell({
  eyebrow = 'Bus Dispatch System',
  title,
  description,
  actions,
  children,
  className,
  compact = false,
  breadcrumb,
}: SchoolBusPageShellProps) {
  /* ─── Compact variant ─────────────────────────────────────────────── */
  if (compact) {
    return (
      <div className={cn('flex flex-col gap-3', className)}>
        {/* Compact hero bar */}
        <div className='relative overflow-hidden rounded-2xl border border-rose-100 bg-white px-7 py-5 shadow-[0_2px_12px_rgba(15,23,42,0.07)]'>
          {/* Decorative blob */}
          <div className='pointer-events-none absolute -right-10 -top-10 h-32 w-32 rounded-full bg-rose-100/50 blur-2xl' />

          {/* Breadcrumb row — sits at the very top with space beneath */}
          {breadcrumb && (
            <div className='relative mb-3'>{breadcrumb}</div>
          )}

          {/* Eyebrow + title + actions */}
          <div className='relative flex flex-wrap items-center justify-between gap-4'>
            <div className='min-w-0 flex-1'>
              <p className={cn('mb-1 text-[10px] font-bold uppercase tracking-[0.28em]', schoolBusUi.eyebrow)}>
                {eyebrow}
              </p>
              <h1 className={cn('text-xl font-bold leading-tight tracking-tight', schoolBusUi.heading)}>
                {title}
              </h1>
              {description && (
                <p className={cn('mt-1 text-xs leading-5', schoolBusUi.mutedText)}>
                  {description}
                </p>
              )}
            </div>
            {actions && (
              <div className='flex shrink-0 flex-wrap items-center gap-2'>{actions}</div>
            )}
          </div>
        </div>

        {children}
      </div>
    );
  }

  /* ─── Normal variant ──────────────────────────────────────────────── */
  return (
    <div className={cn('space-y-6', className)}>
      {/* Hero card */}
      <div className={cn('relative overflow-hidden rounded-[32px] border border-rose-100 bg-white px-8 py-7 shadow-[0_24px_70px_rgba(15,23,42,0.08)]')}>
        {/* Decorative blobs */}
        <div className='pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-rose-100/80 blur-3xl' />
        <div className='pointer-events-none absolute bottom-0 right-10 h-28 w-28 rounded-full bg-sky-100/70 blur-2xl' />

        {/* ① Breadcrumb row */}
        {breadcrumb && (
          <div className='relative mb-3'>{breadcrumb}</div>
        )}

        {/* ② Eyebrow label */}
        <p className={cn('relative mb-3', schoolBusUi.eyebrow)}>
          {eyebrow}
        </p>

        {/* ③ Main row: title left, actions right */}
        <div className='relative flex flex-wrap items-center justify-between gap-4'>
          <h1 className={cn('min-w-0 flex-1 text-[1.75rem] font-bold leading-tight tracking-tight', schoolBusUi.heading)}>
            {title}
          </h1>
          {actions && (
            <div className='flex shrink-0 flex-wrap items-center gap-3'>
              {actions}
            </div>
          )}
        </div>

        {/* ④ Description */}
        {description && (
          <p className={cn('relative mt-2 max-w-3xl text-sm leading-6', schoolBusUi.mutedText)}>
            {description}
          </p>
        )}
      </div>

      {children}
    </div>
  );
}
