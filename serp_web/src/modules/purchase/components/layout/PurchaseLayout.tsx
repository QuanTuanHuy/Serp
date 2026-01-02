/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Purchase main layout with sidebar and header
 */

'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { PurchaseHeader } from './PurchaseHeader';
import { PurchaseAuthGuard } from '../PurchaseAuthGuard';
import { cn } from '@/shared/utils';

interface PurchaseLayoutProps {
  children: React.ReactNode;
}

/**
 * PurchaseLayout - Main layout wrapper for Purchase pages
 */
const PurchaseLayoutContent: React.FC<PurchaseLayoutProps> = ({ children }) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Dynamic Sidebar */}
      <DynamicSidebar moduleCode='PURCHASE' />

      {/* Main Content Area */}
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        {/* Header */}
        <PurchaseHeader scrollContainerRef={containerRef} />

        {/* Page Content */}
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='PURCHASE'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const PurchaseLayout: React.FC<PurchaseLayoutProps> = ({ children }) => {
  return (
    <PurchaseAuthGuard>
      <SidebarProvider>
        <PurchaseLayoutContent>{children}</PurchaseLayoutContent>
      </SidebarProvider>
    </PurchaseAuthGuard>
  );
};
