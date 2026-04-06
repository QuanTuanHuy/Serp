'use client';

import React from 'react';
import {
  DynamicSidebar,
  RouteGuard,
  SidebarProvider,
  useSidebarContext,
} from '@/shared/components';
import { cn } from '@/shared/utils';
import { SchoolBusHeader } from './SchoolBusHeader';
import { SchoolBusAuthGuard } from '../SchoolBusAuthGuard';

interface SchoolBusLayoutProps {
  children: React.ReactNode;
}

const SchoolBusLayoutContent: React.FC<SchoolBusLayoutProps> = ({
  children,
}) => {
  const { isCollapsed } = useSidebarContext();

  return (
    <div className='flex min-h-screen bg-background'>
      <DynamicSidebar moduleCode='SCHOOL_BUS' />
      <div
        className={cn(
          'flex min-h-screen flex-1 flex-col transition-all duration-300',
          isCollapsed ? 'pl-16' : 'pl-64'
        )}
      >
        <SchoolBusHeader />
        <main className='flex-1'>
          <div className='container mx-auto p-6'>
            <RouteGuard moduleCode='SCHOOL_BUS'>{children}</RouteGuard>
          </div>
        </main>
      </div>
    </div>
  );
};

export const SchoolBusLayout: React.FC<SchoolBusLayoutProps> = ({
  children,
}) => {
  return (
    <SchoolBusAuthGuard>
      <SidebarProvider>
        <SchoolBusLayoutContent>{children}</SchoolBusLayoutContent>
      </SidebarProvider>
    </SchoolBusAuthGuard>
  );
};
