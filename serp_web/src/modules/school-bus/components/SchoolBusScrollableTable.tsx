'use client';

import * as React from 'react';

interface SchoolBusScrollableTableProps {
  children: React.ReactNode;
  footer?: React.ReactNode;
}

export function SchoolBusScrollableTable({
  children,
  footer,
}: SchoolBusScrollableTableProps) {
  return (
    <div className='overflow-hidden rounded-[22px] border border-border bg-card text-card-foreground shadow-sm'>
      <div className='overflow-x-auto'>
        <div className='min-w-max whitespace-nowrap [&_table]:min-w-max [&_td]:whitespace-nowrap [&_th]:whitespace-nowrap'>
          {children}
        </div>
      </div>
      {footer}
    </div>
  );
}
