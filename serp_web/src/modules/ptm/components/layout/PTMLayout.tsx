/**
 * PTM v2 - Main Layout Component
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Layout wrapper for PTM module
 */

'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { PTMHeader } from './PTMHeader';
import { PTMCommandPalette } from './PTMCommandPalette';
import { PTMAuthGuard } from '../PTMAuthGuard';
import { cn } from '@/shared/utils';

interface PTMLayoutProps {
  children: React.ReactNode;
}

const PTMLayoutContent: React.FC<PTMLayoutProps> = ({ children }) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Dynamic Sidebar */}
      <DynamicSidebar moduleCode='PTM' />

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
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='PTM'>{children}</RouteGuard>
          </div>
        </main>
      </div>

      {/* Command Palette */}
      <PTMCommandPalette />
    </div>
  );
};

export const PTMLayout: React.FC<PTMLayoutProps> = ({ children }) => {
  return (
    <PTMAuthGuard>
      <SidebarProvider>
        <PTMLayoutContent>{children}</PTMLayoutContent>
      </SidebarProvider>
    </PTMAuthGuard>
  );
};
