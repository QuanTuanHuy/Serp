/**
 * Author: Tran Ngoc Hung
 * Description: Part of Serp Project - TTCRS dispatcher layout (sidebar + header)
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import { TtcrsSidebarProvider, useTtcrsSidebar } from '../../contexts/TtcrsSidebarContext';
import { TtcrsSidebar } from './TtcrsSidebar';
import { TtcrsHeader } from './TtcrsHeader';

interface TtcrsLayoutProps {
  children: React.ReactNode;
}

const TtcrsLayoutContent: React.FC<TtcrsLayoutProps> = ({ children }) => {
  const { isCollapsed } = useTtcrsSidebar();
  const containerRef = React.useRef<HTMLDivElement | null>(null);

  return (
    <div className='flex min-h-screen bg-background'>
      {/* Fixed sidebar */}
      <TtcrsSidebar />

      {/* Main content area — shifts right to make room for the sidebar */}
      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <TtcrsHeader scrollContainerRef={containerRef} />

        <main className='flex-1'>
          {children}
        </main>
      </div>
    </div>
  );
};

export const TtcrsLayout: React.FC<TtcrsLayoutProps> = ({ children }) => {
  return (
    <TtcrsSidebarProvider>
      <TtcrsLayoutContent>{children}</TtcrsLayoutContent>
    </TtcrsSidebarProvider>
  );
};
