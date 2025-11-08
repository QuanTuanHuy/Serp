/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PTM main layout with sidebar and header
 */

'use client';

import React, { useRef } from 'react';
import { Sidebar } from './Sidebar';
import { PTMHeader } from './PTMHeader';
import { PTMAuthGuard } from '../PTMAuthGuard';
import {
  PTMSidebarProvider,
  usePTMSidebar,
} from '../../contexts/PTMSidebarContext';
import { cn } from '@/shared/utils';

interface PTMLayoutProps {
  children: React.ReactNode;
}

const PTMLayoutContent: React.FC<PTMLayoutProps> = ({ children }) => {
  const { isCollapsed } = usePTMSidebar();
  const containerRef = React.useRef<HTMLDivElement | null>(null);

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Fixed Sidebar - 64px or 256px width */}
      <Sidebar />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <PTMHeader scrollContainerRef={containerRef} />

        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>{children}</div>
        </main>
      </div>
    </div>
  );
};

export const PTMLayout: React.FC<PTMLayoutProps> = ({ children }) => {
  return (
    <PTMAuthGuard>
      <PTMSidebarProvider>
        <PTMLayoutContent>{children}</PTMLayoutContent>
      </PTMSidebarProvider>
    </PTMAuthGuard>
  );
};
