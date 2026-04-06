'use client';

import React from 'react';

interface SchoolBusHeaderProps {
  title?: string;
}

export const SchoolBusHeader: React.FC<SchoolBusHeaderProps> = ({
  title = 'School Bus Operations',
}) => {
  return (
    <header className='sticky top-0 z-20 border-b bg-background/95 backdrop-blur'>
      <div className='container mx-auto flex items-center justify-between px-6 py-4'>
        <div>
          <p className='text-xs font-semibold uppercase tracking-[0.24em] text-muted-foreground'>
            Multi-tenant operations
          </p>
          <h1 className='text-2xl font-semibold tracking-tight'>{title}</h1>
        </div>
      </div>
    </header>
  );
};
