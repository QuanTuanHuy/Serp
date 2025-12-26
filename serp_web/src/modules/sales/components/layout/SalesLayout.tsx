/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Sales main layout with sidebar and header
 */

'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { SalesHeader } from './SalesHeader';
import { SalesAuthGuard } from '../SalesAuthGuard';
import { cn } from '@/shared/utils';

interface SalesLayoutProps {
  children: React.ReactNode;
}

/**
 * SalesLayout - Main layout wrapper for Sales pages
 */
const SalesLayoutContent: React.FC<SalesLayoutProps> = ({ children }) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Dynamic Sidebar */}
      <DynamicSidebar moduleCode='SALES' />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <SalesHeader scrollContainerRef={containerRef} />

        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='SALES'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const SalesLayout: React.FC<SalesLayoutProps> = ({ children }) => {
  return (
    <SalesAuthGuard>
      <SidebarProvider>
        <SalesLayoutContent>{children}</SalesLayoutContent>
      </SidebarProvider>
    </SalesAuthGuard>
  );
};
