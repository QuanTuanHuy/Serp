'use client';

import React from 'react';

interface SchoolBusSectionProps {
  title: string;
  description?: string;
  children: React.ReactNode;
}

export const SchoolBusSection: React.FC<SchoolBusSectionProps> = ({
  title,
  description,
  children,
}) => {
  return (
    <section className='space-y-4 rounded-2xl border bg-card p-5 shadow-sm'>
      <div>
        <h2 className='text-lg font-semibold'>{title}</h2>
        {description ? (
          <p className='text-sm text-muted-foreground'>{description}</p>
        ) : null}
      </div>
      {children}
    </section>
  );
};
