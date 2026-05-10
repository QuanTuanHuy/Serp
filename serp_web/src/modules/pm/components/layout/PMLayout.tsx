/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Layout wrapper for PM module
 */

'use client';

import React from 'react';
import {
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { cn } from '@/shared/utils';
import { PMAuthGuard } from '../PMAuthGuard';
import { PMHeader } from './PMHeader';
import { PMSidebar } from './PMSidebar';

interface PMLayoutProps {
  children: React.ReactNode;
}

const PMLayoutContent: React.FC<PMLayoutProps> = ({ children }) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      <PMSidebar />

      <div
        ref={containerRef}
        className={cn(
          'flex h-screen flex-1 flex-col overflow-y-auto transition-all duration-300',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <PMHeader />

        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='PM'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const PMLayout: React.FC<PMLayoutProps> = ({ children }) => {
  return (
    <PMAuthGuard>
      <SidebarProvider>
        <PMLayoutContent>{children}</PMLayoutContent>
      </SidebarProvider>
    </PMAuthGuard>
  );
};
