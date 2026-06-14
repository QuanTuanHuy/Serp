/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Handover manifest metric item
 */

import type { ReactNode } from 'react';

interface MetricItemProps {
  label: string;
  value: ReactNode;
}

export function MetricItem({ label, value }: MetricItemProps) {
  return (
    <div className='space-y-1 rounded-md border p-3'>
      <div className='text-xs font-medium uppercase text-muted-foreground'>
        {label}
      </div>
      <div className='text-sm font-medium'>{value}</div>
    </div>
  );
}
