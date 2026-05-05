/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Layout wrapper for PM module
 */

'use client';

import React from 'react';
import { PMAuthGuard } from '../PMAuthGuard';
import { PMHeader } from './PMHeader';
import { PMSidebar } from './PMSidebar';
import { cn } from '@/shared/utils';

interface PMLayoutProps {
  children: React.ReactNode;
}

const PM_SIDEBAR_WIDTH_CLASS = 'pl-64';

const PMLayoutContent: React.FC<PMLayoutProps> = ({ children }) => {
  return (
    <div className='flex min-h-screen bg-muted/30'>
      <PMSidebar />

      <div
        className={cn(
          'flex min-h-screen flex-1 flex-col transition-all duration-300',
          PM_SIDEBAR_WIDTH_CLASS
        )}
      >
        <PMHeader />

        <main className='flex-1'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
};

export const PMLayout: React.FC<PMLayoutProps> = ({ children }) => {
  return (
    <PMAuthGuard>
      <PMLayoutContent>{children}</PMLayoutContent>
    </PMAuthGuard>
  );
};
