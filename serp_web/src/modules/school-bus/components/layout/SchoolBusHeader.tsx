'use client';

import React from 'react';
import { schoolBusUi } from '../../theme';

interface SchoolBusHeaderProps {
  title?: string;
}

export const SchoolBusHeader: React.FC<SchoolBusHeaderProps> = ({
  title = 'School Bus Operations',
}) => {
  return (
    <header className={`sticky top-0 z-20 ${schoolBusUi.header}`}>
      <div className='container mx-auto flex items-center justify-between px-6 py-4'>
        <div>
          <p className={schoolBusUi.eyebrow}>
            Multi-tenant operations
          </p>
          <h1 className={`mt-1 text-2xl ${schoolBusUi.heading}`}>{title}</h1>
        </div>
      </div>
    </header>
  );
};
