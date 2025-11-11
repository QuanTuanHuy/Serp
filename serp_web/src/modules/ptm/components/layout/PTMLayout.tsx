/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PTM main layout with sidebar and header
 */

'use client';

import React from 'react';
import { DynamicSidebar, RouteGuard } from '@/shared/components';
import { PTMHeader } from './PTMHeader';
import { PTMAuthGuard } from '../PTMAuthGuard';

interface PTMLayoutProps {
  children: React.ReactNode;
}

const PTMLayoutContent: React.FC<PTMLayoutProps> = ({ children }) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Sidebar */}
      <DynamicSidebar moduleCode='PTM' />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className='flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto pl-64'
      >
        {/* Header */}
        <PTMHeader scrollContainerRef={containerRef} />

        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='PTM'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const PTMLayout: React.FC<PTMLayoutProps> = ({ children }) => {
  return (
    <PTMAuthGuard>
      <PTMLayoutContent>{children}</PTMLayoutContent>
    </PTMAuthGuard>
  );
};
