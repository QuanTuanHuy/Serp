/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics main layout with sidebar and header
*/

'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { LogisticsHeader } from './LogisticsHeader';
import { LogisticsAuthGuard } from '../LogisticsAuthGuard';
import { cn } from '@/shared/utils';

interface LogisticsLayoutProps {
  children: React.ReactNode;
}

/**
 * LogisticsLayout - Main layout wrapper for Logistics pages
 */
const LogisticsLayoutContent: React.FC<LogisticsLayoutProps> = ({
  children,
}) => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Dynamic Sidebar */}
      <DynamicSidebar moduleCode='LOGISTICS' />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <LogisticsHeader scrollContainerRef={containerRef} />

        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='LOGISTICS'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const LogisticsLayout: React.FC<LogisticsLayoutProps> = ({
  children,
}) => {
  return (
    <LogisticsAuthGuard>
      <SidebarProvider>
        <LogisticsLayoutContent>{children}</LogisticsLayoutContent>
      </SidebarProvider>
    </LogisticsAuthGuard>
  );
};
