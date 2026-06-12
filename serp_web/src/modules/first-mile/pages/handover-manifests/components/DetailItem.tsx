/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Handover manifest detail item
 */

import type { ReactNode } from 'react';

interface DetailItemProps {
  label: string;
  value: ReactNode;
}

export function DetailItem({ label, value }: DetailItemProps) {
  return (
    <div className='space-y-1 rounded-md border p-3'>
      <p className='text-xs font-medium uppercase text-muted-foreground'>
        {label}
      </p>
      <div className='text-sm'>{value}</div>
    </div>
  );
}
