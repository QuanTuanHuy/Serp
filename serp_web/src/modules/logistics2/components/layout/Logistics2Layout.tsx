'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { cn } from '@/shared/utils';
import { Logistics2AuthGuard } from '../Logistics2AuthGuard';
import { Logistics2Header } from './Logistics2Header';

interface Logistics2LayoutProps {
  children: React.ReactNode;
}

const Logistics2LayoutContent: React.FC<Logistics2LayoutProps> = ({
  children,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      <DynamicSidebar moduleCode='LOGISTICS2' />

      <div
        ref={containerRef}
        className={cn(
          'flex flex-1 flex-col transition-all duration-300 h-screen overflow-y-auto',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <Logistics2Header scrollContainerRef={containerRef} />

        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='LOGISTICS2'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const Logistics2Layout: React.FC<Logistics2LayoutProps> = ({
  children,
}) => {
  return (
    <Logistics2AuthGuard>
      <SidebarProvider>
        <Logistics2LayoutContent>{children}</Logistics2LayoutContent>
      </SidebarProvider>
    </Logistics2AuthGuard>
  );
};
