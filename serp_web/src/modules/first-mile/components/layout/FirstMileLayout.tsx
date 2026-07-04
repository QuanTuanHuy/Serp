/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile layout
 */

'use client';

import React from 'react';
import {
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { cn } from '@/shared/utils';
import { FirstMileAuthGuard } from '../FirstMileAuthGuard';
import { FirstMileHeader } from './FirstMileHeader';
import { TmsDynamicSidebar } from './TmsDynamicSidebar';

interface FirstMileLayoutProps {
  children: React.ReactNode;
}

const FirstMileLayoutContent: React.FC<FirstMileLayoutProps> = ({
  children,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      <TmsDynamicSidebar />

      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <FirstMileHeader scrollContainerRef={containerRef} />

        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='TMS'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const FirstMileLayout: React.FC<FirstMileLayoutProps> = ({
  children,
}) => {
  return (
    <FirstMileAuthGuard>
      <SidebarProvider>
        <FirstMileLayoutContent>{children}</FirstMileLayoutContent>
      </SidebarProvider>
    </FirstMileAuthGuard>
  );
};
