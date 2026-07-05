'use client';

import type { ReactNode } from 'react';

interface DashboardChartCardProps {
  title: string;
  isLoading: boolean;
  isError: boolean;
  children: ReactNode;
  className?: string;
}

export function DashboardChartCard({
  title,
  isLoading,
  isError,
  children,
  className,
}: DashboardChartCardProps) {
  return (
    <div
      className={`rounded-2xl border border-border bg-card p-5 text-card-foreground shadow-sm ${className || ''}`}
    >
      {isLoading ? (
        <div className='space-y-4'>
          <div className='h-4 w-40 animate-pulse rounded bg-muted' />
          <div className='h-[220px] animate-pulse rounded-xl bg-muted/60' />
        </div>
      ) : isError ? (
        <div className='flex min-h-[240px] flex-col items-center justify-center text-center'>
          <p className='text-sm font-semibold text-foreground'>{title}</p>
          <p className='mt-2 text-sm text-muted-foreground'>
            Không thể tải khối thông tin này.
          </p>
        </div>
      ) : (
        children
      )}
    </div>
  );
}
