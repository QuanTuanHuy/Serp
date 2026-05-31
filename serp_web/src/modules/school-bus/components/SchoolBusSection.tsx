'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import { schoolBusUi } from '../theme';

interface SchoolBusSectionProps {
  title: string;
  description?: string;
  children: React.ReactNode;
  action?: React.ReactNode;
  className?: string;
  /** Optional id for in-page anchor/scroll targeting from breadcrumbs. */
  id?: string;
}

export const SchoolBusSection: React.FC<SchoolBusSectionProps> = ({
  title,
  description,
  children,
  action,
  className,
  id,
}) => {
  return (
    <section
      id={id}
      className={cn(
        'space-y-4',
        schoolBusUi.section,
        className
      )}
    >
      <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
        <div>
          <h2 className={cn('text-lg', schoolBusUi.heading)}>{title}</h2>
          {description ? (
            <p className={cn('mt-1 text-sm', schoolBusUi.mutedText)}>
              {description}
            </p>
          ) : null}
        </div>
        {action ? <div>{action}</div> : null}
      </div>
      {children}
    </section>
  );
};
